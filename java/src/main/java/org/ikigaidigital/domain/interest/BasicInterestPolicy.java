package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Basic plan: 1% p.a., paid monthly, no upper limit. */
public final class BasicInterestPolicy implements InterestPolicy {

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        return deposit.getBalance() * 0.01 / 12;
    }
}
