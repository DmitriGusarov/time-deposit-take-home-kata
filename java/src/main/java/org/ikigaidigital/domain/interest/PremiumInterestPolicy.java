package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Premium plan: 5% p.a., paid monthly, starts after 45 days (only while days > 45). */
public final class PremiumInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.05;

    private static final int INTEREST_STARTS_AFTER_DAYS = 45;

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() > INTEREST_STARTS_AFTER_DAYS) {
            return deposit.getBalance() * ANNUAL_RATE / 12;
        }
        return 0;
    }
}
