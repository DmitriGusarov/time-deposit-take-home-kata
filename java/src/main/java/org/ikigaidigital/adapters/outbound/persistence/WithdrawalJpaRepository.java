package org.ikigaidigital.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface WithdrawalJpaRepository extends JpaRepository<WithdrawalEntity, Integer> {
    List<WithdrawalEntity> findAllByOrderByIdAsc();
}
