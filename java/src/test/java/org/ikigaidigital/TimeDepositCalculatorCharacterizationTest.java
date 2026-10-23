package org.ikigaidigital;

import org.ikigaidigital.legacy.LegacyTimeDepositCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Characterization tests: they pin the behaviour of {@link TimeDepositCalculator#updateBalance(List)}
 * exactly as it was received, so the refactoring can be proven to be behaviour-preserving.
 * <p>
 * Every expected value below was produced by running the ORIGINAL code, not derived by hand.
 * The kata states the method "is functioning correctly" – these tests encode that claim.
 */
class TimeDepositCalculatorCharacterizationTest {

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @Nested
    @DisplayName("interest rules by plan type")
    class InterestRules {

        @ParameterizedTest(name = "{0}: balance {1} after {2} days -> {3}")
        @CsvSource({
                // plan,    balance,    days, expected
                // --- 30-day rule applies to every plan (strictly greater than 30) ---
                "basic,     1000.00,    30,   1000.00",
                "basic,     1000.00,    31,   1000.83",
                "student,   1200.00,    30,   1200.00",
                "premium,   1200.00,    30,   1200.00",
                // --- basic: 1% p.a. / 12, no upper limit ---
                "basic,     1000.00,    45,   1000.83",
                "basic,     1234567.00, 45,   1235595.81",
                "basic,     0.00,       100,  0.00",
                // --- student: 3% p.a. / 12, stops at one year (days < 366) ---
                "student,   1200.00,    31,   1203.00",
                "student,   1200.00,    365,  1203.00",
                "student,   1200.00,    366,  1200.00",
                // --- premium: 5% p.a. / 12, starts after 45 days (days > 45), never stops ---
                "premium,   1200.00,    45,   1200.00",
                "premium,   1200.00,    46,   1205.00",
                "premium,   7500.50,    400,  7531.75",
                // --- unknown / differently-cased plan types earn nothing and do not fail ---
                "gold,      1000.00,    100,  1000.00",
                "BASIC,     1000.00,    100,  1000.00",
        })
        void appliesOneMonthOfInterest(String planType, double balance, int days, double expected) {
            TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);

            calculator.updateBalance(List.of(deposit));

            assertThat(deposit.getBalance()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("rounding")
    class Rounding {

        @Test
        @DisplayName("interest is rounded HALF_UP to 2 decimals before being added")
        void roundsHalfUpToTwoDecimals() {
            // 150 * 0.01 / 12 = 0.125 exactly -> 0.13
            TimeDeposit deposit = new TimeDeposit(1, "basic", 150.00, 45);

            calculator.updateBalance(List.of(deposit));

            assertThat(deposit.getBalance()).isEqualTo(150.13);
        }

        @Test
        @DisplayName("rounding uses the exact binary value of the double, not its shortest decimal form")
        void roundsTheExactBinaryValueOfTheDouble() {
            // 18 * 0.01 / 12 prints as 0.015 but its double value is slightly BELOW 0.015,
            // so new BigDecimal(double) rounds it to 0.01. BigDecimal.valueOf(double) would
            // give 0.02. A refactoring that switches to valueOf(...) or to pure BigDecimal
            // arithmetic changes behaviour here - this test is the tripwire for that.
            TimeDeposit deposit = new TimeDeposit(1, "basic", 18.00, 45);

            calculator.updateBalance(List.of(deposit));

            assertThat(deposit.getBalance()).isEqualTo(18.01);
        }
    }

    @Nested
    @DisplayName("list handling")
    class ListHandling {

        @Test
        void updatesEveryDepositInPlaceAndIndependently() {
            TimeDeposit basic = new TimeDeposit(1, "basic", 1000.00, 45);
            TimeDeposit student = new TimeDeposit(2, "student", 1200.00, 31);
            TimeDeposit premium = new TimeDeposit(3, "premium", 1200.00, 46);
            TimeDeposit tooYoung = new TimeDeposit(4, "premium", 1200.00, 10);

            calculator.updateBalance(List.of(basic, student, premium, tooYoung));

            assertThat(basic.getBalance()).isEqualTo(1000.83);
            assertThat(student.getBalance()).isEqualTo(1203.00);
            assertThat(premium.getBalance()).isEqualTo(1205.00);
            assertThat(tooYoung.getBalance()).isEqualTo(1200.00);
        }

        @Test
        void acceptsAnEmptyList() {
            calculator.updateBalance(List.of());
        }

        @Test
        @DisplayName("calling twice compounds: the second month is computed on the new balance")
        void compoundsOnRepeatedCalls() {
            TimeDeposit deposit = new TimeDeposit(1, "basic", 1000.00, 45);

            calculator.updateBalance(List.of(deposit));
            calculator.updateBalance(List.of(deposit));

            // 1000.00 -> 1000.83 -> 1000.83 + round(1000.83 * 0.01 / 12 = 0.834025) = 1001.66
            // The shared class stores the balance as Double, so repeated additions accumulate
            // binary noise (the actual value is 1001.6600000000001). That noise is legacy behaviour,
            // not something to fix here; it disappears when the balance is persisted as NUMERIC(19,2).
            assertThat(deposit.getBalance()).isCloseTo(1001.66, within(1e-9));
        }
    }

    // Pins legacy behaviour, not desired behaviour: the plan type is only read once days > 30,
    // and a null plan type then fails. Input validation is out of scope per the kata.
    @Nested
    @DisplayName("null plan type")
    class NullPlanType {

        @Test
        void isIgnoredWhileDaysAreAtMost30() {
            TimeDeposit deposit = new TimeDeposit(1, null, 1000.00, 30);

            calculator.updateBalance(List.of(deposit));

            assertThat(deposit.getBalance()).isEqualTo(1000.00);
        }

        @Test
        void throwsNullPointerExceptionOnceDaysExceed30() {
            TimeDeposit deposit = new TimeDeposit(1, null, 1000.00, 31);

            assertThatThrownBy(() -> calculator.updateBalance(List.of(deposit)))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("equivalence with the original implementation")
    class EquivalenceWithLegacy {

        private static final String[] PLAN_TYPES = {"basic", "student", "premium", "gold", "", "Student"};

        @Test
        @DisplayName("production calculator matches the verbatim legacy copy on 20,000 random deposits")
        void matchesLegacyOnRandomInputs() {
            Random random = new Random(42); // fixed seed: deterministic, reproducible failures
            List<TimeDeposit> forNew = new ArrayList<>();
            List<TimeDeposit> forLegacy = new ArrayList<>();

            for (int i = 0; i < 20_000; i++) {
                String plan = PLAN_TYPES[random.nextInt(PLAN_TYPES.length)];
                // cents-granular balances up to 10M, plus a few with 3+ decimals as the DB will never
                // produce them but the Double-typed domain class technically allows them
                double balance = random.nextInt(10) == 0
                        ? random.nextDouble() * 1_000_000
                        : random.nextInt(1_000_000_000) / 100.0;
                int days = random.nextInt(800);
                forNew.add(new TimeDeposit(i, plan, balance, days));
                forLegacy.add(new TimeDeposit(i, plan, balance, days));
            }

            calculator.updateBalance(forNew);
            new LegacyTimeDepositCalculator().updateBalance(forLegacy);

            for (int i = 0; i < forNew.size(); i++) {
                assertThat(forNew.get(i).getBalance())
                        .as("deposit %d: plan=%s balance=%s days=%d", i,
                                forLegacy.get(i).getPlanType(), forLegacy.get(i).getBalance(), forLegacy.get(i).getDays())
                        .isEqualTo(forLegacy.get(i).getBalance());
            }
        }
    }
}
