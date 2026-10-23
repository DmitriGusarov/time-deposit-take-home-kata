package org.ikigaidigital.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A withdrawal made against a time deposit.
 * <p>
 * Kept outside the shared {@link org.ikigaidigital.TimeDeposit} class on purpose:
 * that class must not change (kata constraint), so withdrawals are composed
 * alongside it in {@link TimeDepositDetails} rather than embedded in it.
 */
public record Withdrawal(int id, BigDecimal amount, LocalDate date) {
}
