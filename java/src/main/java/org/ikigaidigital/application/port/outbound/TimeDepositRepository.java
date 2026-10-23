package org.ikigaidigital.application.port.outbound;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.domain.TimeDepositDetails;

import java.util.List;

/**
 * Outbound port for time deposit persistence. Implemented by the persistence adapter;
 * the application layer never sees JPA.
 */
public interface TimeDepositRepository {

    /** All deposits, ordered by id, each with its withdrawals. */
    List<TimeDepositDetails> findAll();

    /** All deposits as mutable domain objects, ordered by id – input for the calculator. */
    List<TimeDeposit> findAllDeposits();

    /** Persists the current balance of each given deposit. */
    void saveBalances(List<TimeDeposit> deposits);
}
