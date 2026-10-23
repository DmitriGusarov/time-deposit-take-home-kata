package org.ikigaidigital.domain;

import org.ikigaidigital.TimeDeposit;

import java.util.List;

/**
 * Read model returned by the "retrieve all time deposits" query:
 * the shared {@link TimeDeposit} plus its withdrawals.
 * <p>
 * Assumption: the GET endpoint's {@code withdrawals} field is the list of
 * withdrawal records belonging to the deposit (not a count or a sum).
 */
public record TimeDepositDetails(TimeDeposit deposit, List<Withdrawal> withdrawals) {
}
