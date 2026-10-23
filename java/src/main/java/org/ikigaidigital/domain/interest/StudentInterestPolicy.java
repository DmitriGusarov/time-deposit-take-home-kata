package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Student plan: 3% p.a., paid monthly, stops after one year (only while days < 366). */
public final class StudentInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.03;

    /** First day without interest; named for the value the legacy code compares against. */
    private static final int INTEREST_ENDS_AT_DAY = 366;

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() < INTEREST_ENDS_AT_DAY) {
            return deposit.getBalance() * ANNUAL_RATE / 12;
        }
        return 0;
    }
}
