package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Basic plan: 1% p.a., paid monthly, no upper limit. */
public final class BasicInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.01;

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
