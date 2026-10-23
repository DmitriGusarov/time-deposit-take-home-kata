package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Premium plan: 5% p.a., paid monthly, starts after 45 days (only while days > 45). */
public final class PremiumInterestPolicy implements InterestPolicy {

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() > 45) {
            return deposit.getBalance() * 0.05 / 12;
        }
        return 0;
    }
}
