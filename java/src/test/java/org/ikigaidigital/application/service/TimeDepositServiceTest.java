package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.outbound.TimeDepositRepository;
import org.ikigaidigital.domain.TimeDepositDetails;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit test of the orchestration: in-memory port, real calculator, no Spring. */
class TimeDepositServiceTest {

    private final InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();
    private final TimeDepositService service = new TimeDepositService(repository, new TimeDepositCalculator());

    @Test
    void updateAllBalancesLoadsAppliesInterestAndSavesBack() {
        repository.deposits.add(new TimeDeposit(1, "basic", 1000.00, 45));
        repository.deposits.add(new TimeDeposit(2, "premium", 1200.00, 10));

        service.updateAllBalances();

        assertThat(repository.saved).hasSize(2);
        assertThat(repository.saved.get(0).getBalance()).isEqualTo(1000.83);
        assertThat(repository.saved.get(1).getBalance()).isEqualTo(1200.00);
    }

    @Test
    void getAllTimeDepositsDelegatesToTheRepository() {
        repository.deposits.add(new TimeDeposit(7, "student", 500.00, 100));

        List<TimeDepositDetails> result = service.getAllTimeDeposits();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).deposit().getId()).isEqualTo(7);
        assertThat(result.get(0).withdrawals()).isEmpty();
    }

    /** Minimal fake of the outbound port – enough to observe what the service does. */
    private static class InMemoryTimeDepositRepository implements TimeDepositRepository {
        final List<TimeDeposit> deposits = new ArrayList<>();
        List<TimeDeposit> saved = List.of();

        @Override
        public List<TimeDepositDetails> findAll() {
            return deposits.stream().map(d -> new TimeDepositDetails(d, List.of())).toList();
        }

        @Override
        public List<TimeDeposit> findAllDeposits() {
            return deposits;
        }

        @Override
        public void saveBalances(List<TimeDeposit> toSave) {
            saved = List.copyOf(toSave);
        }
    }
}
