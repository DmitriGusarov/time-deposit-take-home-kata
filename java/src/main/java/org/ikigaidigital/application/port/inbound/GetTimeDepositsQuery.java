package org.ikigaidigital.application.port.inbound;

import org.ikigaidigital.domain.TimeDepositDetails;

import java.util.List;

/** Inbound port: retrieve all time deposits with their withdrawals. */
public interface GetTimeDepositsQuery {
    List<TimeDepositDetails> getAllTimeDeposits();
}
