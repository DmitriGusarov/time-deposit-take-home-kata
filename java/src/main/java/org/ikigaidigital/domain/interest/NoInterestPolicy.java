package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Fallback for unknown plan types: earns nothing and does not fail (legacy behaviour). */
public final class NoInterestPolicy implements InterestPolicy {

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        return 0;
    }
}
