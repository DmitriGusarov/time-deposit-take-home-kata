package org.ikigaidigital;

import org.ikigaidigital.domain.interest.InterestPolicyRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TimeDepositCalculator {

    private final InterestPolicyRegistry policies;

    public TimeDepositCalculator() {
        this(InterestPolicyRegistry.defaults());
    }

    public TimeDepositCalculator(InterestPolicyRegistry policies) {
        this.policies = policies;
    }

    public void updateBalance(List<TimeDeposit> xs) {
        for (TimeDeposit deposit : xs) {
            double interest = 0;

            // The 30-day guard stays here rather than in the policies because it is common
            // to all plans. It also keeps the legacy evaluation order: the plan type is only
            // looked up (and a null plan type only fails) once days > 30.
            if (deposit.getDays() > 30) {
                interest = policies.policyFor(deposit.getPlanType()).monthlyInterest(deposit);
            }

            double a2d = deposit.getBalance() + (new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)).doubleValue();
            deposit.setBalance(a2d);
        }
    }
}
