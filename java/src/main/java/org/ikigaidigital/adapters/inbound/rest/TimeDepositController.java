package org.ikigaidigital.adapters.inbound.rest;

import org.ikigaidigital.adapters.inbound.rest.api.TimeDepositsApi;
import org.ikigaidigital.adapters.inbound.rest.model.TimeDeposit;
import org.ikigaidigital.application.port.inbound.GetTimeDepositsQuery;
import org.ikigaidigital.application.port.inbound.UpdateBalancesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Inbound REST adapter. Implements the interface generated from openapi.yaml,
 * so the contract – not this class – is the source of truth for the API.
 */
@RestController
public class TimeDepositController implements TimeDepositsApi {

    private final GetTimeDepositsQuery getTimeDeposits;
    private final UpdateBalancesUseCase updateBalances;

    public TimeDepositController(GetTimeDepositsQuery getTimeDeposits, UpdateBalancesUseCase updateBalances) {
        this.getTimeDeposits = getTimeDeposits;
        this.updateBalances = updateBalances;
    }

    @Override
    public ResponseEntity<List<TimeDeposit>> getAllTimeDeposits() {
        List<TimeDeposit> body = getTimeDeposits.getAllTimeDeposits().stream()
                .map(TimeDepositRestMapper::toDto)
                .toList();
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<Void> updateAllBalances() {
        updateBalances.updateAllBalances();
        return ResponseEntity.noContent().build();
    }
}
