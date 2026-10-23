package org.ikigaidigital.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TimeDepositJpaRepository extends JpaRepository<TimeDepositEntity, Integer> {
    List<TimeDepositEntity> findAllByOrderByIdAsc();
}
