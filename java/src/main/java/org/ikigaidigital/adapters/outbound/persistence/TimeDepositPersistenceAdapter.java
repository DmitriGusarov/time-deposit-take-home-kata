package org.ikigaidigital.adapters.outbound.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.outbound.TimeDepositRepository;
import org.ikigaidigital.domain.TimeDepositDetails;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Outbound persistence adapter: implements the repository port on top of Spring Data JPA.
 * <p>
 * Withdrawals are loaded in one query and grouped in memory instead of using a JPA
 * {@code @OneToMany}: the data set is small, it avoids N+1 and lazy-loading surprises,
 * and keeps both entities trivially simple.
 */
@Component
class TimeDepositPersistenceAdapter implements TimeDepositRepository {

    private final TimeDepositJpaRepository timeDeposits;
    private final WithdrawalJpaRepository withdrawals;

    TimeDepositPersistenceAdapter(TimeDepositJpaRepository timeDeposits, WithdrawalJpaRepository withdrawals) {
        this.timeDeposits = timeDeposits;
        this.withdrawals = withdrawals;
    }

    @Override
    public List<TimeDepositDetails> findAll() {
        Map<Integer, List<Withdrawal>> byDeposit = withdrawals.findAllByOrderByIdAsc().stream()
                .collect(Collectors.groupingBy(WithdrawalEntity::getTimeDepositId,
                        Collectors.mapping(TimeDepositPersistenceAdapter::toDomain, Collectors.toList())));

        return timeDeposits.findAllByOrderByIdAsc().stream()
                .map(e -> new TimeDepositDetails(toDomain(e), byDeposit.getOrDefault(e.getId(), List.of())))
                .toList();
    }

    @Override
    public List<TimeDeposit> findAllDeposits() {
        return timeDeposits.findAllByOrderByIdAsc().stream()
                .map(TimeDepositPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public void saveBalances(List<TimeDeposit> deposits) {
        Map<Integer, TimeDepositEntity> entities = timeDeposits.findAllById(
                        deposits.stream().map(TimeDeposit::getId).toList()).stream()
                .collect(Collectors.toMap(TimeDepositEntity::getId, e -> e));

        for (TimeDeposit deposit : deposits) {
            // The calculator adds a 2-dp rounded interest to a Double, which can leave binary noise
            // (e.g. 1001.6600000000001). Normalising to scale 2 here is lossless for the domain and
            // makes the rounding explicit instead of relying on NUMERIC(19,2) to do it on insert.
            entities.get(deposit.getId())
                    .setBalance(BigDecimal.valueOf(deposit.getBalance()).setScale(2, RoundingMode.HALF_UP));
        }
        timeDeposits.saveAll(entities.values());
    }

    private static TimeDeposit toDomain(TimeDepositEntity e) {
        // The shared TimeDeposit uses Double for balance (kata constraint: class unchanged).
        return new TimeDeposit(e.getId(), e.getPlanType(), e.getBalance().doubleValue(), e.getDays());
    }

    private static Withdrawal toDomain(WithdrawalEntity e) {
        return new Withdrawal(e.getId(), e.getAmount(), e.getDate());
    }
}
