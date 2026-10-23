package org.ikigaidigital.application.port.inbound;

/** Inbound port: apply one month of interest to every time deposit and persist the result. */
public interface UpdateBalancesUseCase {
    void updateAllBalances();
}
