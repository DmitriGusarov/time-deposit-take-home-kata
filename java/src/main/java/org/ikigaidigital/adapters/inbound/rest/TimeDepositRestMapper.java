package org.ikigaidigital.adapters.inbound.rest;

import org.ikigaidigital.adapters.inbound.rest.model.TimeDeposit;
import org.ikigaidigital.adapters.inbound.rest.model.Withdrawal;
import org.ikigaidigital.domain.TimeDepositDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Domain → generated DTO mapping. Kept as plain static methods: no MapStruct needed for two records. */
final class TimeDepositRestMapper {

    private TimeDepositRestMapper() {
    }

    static TimeDeposit toDto(TimeDepositDetails details) {
        var deposit = details.deposit();
        var dto = new TimeDeposit();
        dto.setId(deposit.getId());
        dto.setPlanType(deposit.getPlanType());
        // Presentation only: the stored balance is NUMERIC(19,2), so scale 2 is lossless here.
        // valueOf (not new BigDecimal) turns the Double into its shortest decimal form, e.g. 1000.83.
        dto.setBalance(BigDecimal.valueOf(deposit.getBalance()).setScale(2, RoundingMode.HALF_UP));
        dto.setDays(deposit.getDays());
        dto.setWithdrawals(details.withdrawals().stream().map(TimeDepositRestMapper::toDto).toList());
        return dto;
    }

    private static Withdrawal toDto(org.ikigaidigital.domain.Withdrawal withdrawal) {
        var dto = new Withdrawal();
        dto.setId(withdrawal.id());
        dto.setAmount(withdrawal.amount());
        dto.setDate(withdrawal.date());
        return dto;
    }
}
