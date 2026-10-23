package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/** Student plan: 3% p.a., paid monthly, stops after one year (only while days < 366). */
public final class StudentInterestPolicy implements InterestPolicy {

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() < 366) {
            return deposit.getBalance() * 0.03 / 12;
        }
        return 0;
    }
}
