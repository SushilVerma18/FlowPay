package com.cfs.FlowPay.refund;

import com.cfs.FlowPay.payment.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Ref;
import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund,String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Refund e where e.id=:id")
    Optional<Refund> lockById(@Param("id") String id);

    Optional<Refund> findByMerchantIdAndIdempotencyKey(String m,String k);

    List<Refund> findByMerchantIdOrderByCreatedAtDesc(String m);

}
