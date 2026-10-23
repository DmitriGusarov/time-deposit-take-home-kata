package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.inbound.GetTimeDepositsQuery;
import org.ikigaidigital.application.port.inbound.UpdateBalancesUseCase;
import org.ikigaidigital.application.port.outbound.TimeDepositRepository;
import org.ikigaidigital.domain.TimeDepositDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service: orchestrates the use cases without containing business rules.
 * Interest rules live in {@link TimeDepositCalculator} (and, after the refactoring,
 * in the {@code domain.interest} policies).
 */
@Service
public class TimeDepositService implements GetTimeDepositsQuery, UpdateBalancesUseCase {

    private final TimeDepositRepository repository;
    private final TimeDepositCalculator calculator;

    public TimeDepositService(TimeDepositRepository repository, TimeDepositCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositDetails> getAllTimeDeposits() {
        return repository.findAll();
    }

    /**
     * Loads every deposit, applies one accrual period through the unchanged
     * {@code updateBalance(List)} API, and writes the balances back in one transaction.
     */
    @Override
    @Transactional
    public void updateAllBalances() {
        List<TimeDeposit> deposits = repository.findAllDeposits();
        calculator.updateBalance(deposits);
        repository.saveBalances(deposits);
    }
}
