package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentInterestPolicyTest {

    private final StudentInterestPolicy policy = new StudentInterestPolicy();

    @Test
    void paysThreePercentPerYearUpToDay365() {
        TimeDeposit deposit = new TimeDeposit(1, "student", 1200.00, 365);

        assertThat(policy.monthlyInterest(deposit)).isEqualTo(1200.00 * 0.03 / 12);
    }

    @Test
    void paysNothingFromDay366() {
        TimeDeposit deposit = new TimeDeposit(1, "student", 1200.00, 366);

        assertThat(policy.monthlyInterest(deposit)).isEqualTo(0.0);
    }
}
