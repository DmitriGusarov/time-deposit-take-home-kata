package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BasicInterestPolicyTest {

    private final BasicInterestPolicy policy = new BasicInterestPolicy();

    @ParameterizedTest(name = "days {0}")
    @ValueSource(ints = {31, 1000})
    void paysOnePercentPerYearRegardlessOfDays(int days) {
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1000.00, days);

        assertThat(policy.monthlyInterest(deposit)).isEqualTo(1000.00 * 0.01 / 12);
    }
}
