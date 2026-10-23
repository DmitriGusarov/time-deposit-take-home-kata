package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumInterestPolicyTest {

    private final PremiumInterestPolicy policy = new PremiumInterestPolicy();

    @Test
    void paysNothingUpToDay45() {
        TimeDeposit deposit = new TimeDeposit(1, "premium", 1200.00, 45);

        assertThat(policy.monthlyInterest(deposit)).isEqualTo(0.0);
    }

    @ParameterizedTest(name = "days {0}")
    @ValueSource(ints = {46, 400})
    void paysFivePercentPerYearFromDay46WithoutUpperLimit(int days) {
        TimeDeposit deposit = new TimeDeposit(1, "premium", 1200.00, days);

        assertThat(policy.monthlyInterest(deposit)).isEqualTo(1200.00 * 0.05 / 12);
    }
}
