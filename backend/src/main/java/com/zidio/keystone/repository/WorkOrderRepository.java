package com.zidio.keystone.repository;

import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.enums.WorkOrderStatus;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);

    Page<WorkOrder> findByCustomerId(UUID customerId, Pageable pageable);

    Page<WorkOrder> findByAssignedToId(UUID technicianId, Pageable pageable);

    boolean existsByCode(String code);

    @Query("select w from WorkOrder w where w.slaDueAt < :now and w.status not in ('CLOSED','CANCELLED','COMPLETED')")
    List<WorkOrder> findOverdue(@Param("now") Instant now);
}
