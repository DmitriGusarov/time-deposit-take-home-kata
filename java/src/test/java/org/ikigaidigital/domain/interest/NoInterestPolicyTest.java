package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NoInterestPolicyTest {

    private final NoInterestPolicy policy = new NoInterestPolicy();

    @ParameterizedTest(name = "days {0}")
    @ValueSource(ints = {31, 46, 400})
    void alwaysPaysNothing(int days) {
        TimeDeposit deposit = new TimeDeposit(1, "gold", 1000.00, days);

        assertThat(policy.monthlyInterest(deposit)).isEqualTo(0.0);
    }
}
