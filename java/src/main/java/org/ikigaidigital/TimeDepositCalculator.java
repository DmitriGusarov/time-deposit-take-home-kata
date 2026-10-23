package org.ikigaidigital;

import org.ikigaidigital.domain.interest.InterestPolicyRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TimeDepositCalculator {

    private static final int GRACE_PERIOD_DAYS = 30;

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
            if (deposit.getDays() > GRACE_PERIOD_DAYS) {
                interest = policies.policyFor(deposit.getPlanType()).monthlyInterest(deposit);
            }

            double newBalance = deposit.getBalance() + (new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)).doubleValue();
            deposit.setBalance(newBalance);
        }
    }
}
