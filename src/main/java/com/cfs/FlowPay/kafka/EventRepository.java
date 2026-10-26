package com.cfs.FlowPay.kafka;

import com.cfs.FlowPay.refund.Refund;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event,String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id=:id")
    Optional<Event> lockById(@Param("id") String id);

    List<Event> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
