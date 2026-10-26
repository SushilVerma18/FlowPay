package com.cfs.FlowPay.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder,String > {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PaymentOrder e where e.id=:id")
    Optional<PaymentOrder> lockById(@Param("id") String id);


    List<PaymentOrder> findByMerchantIdOrderByCreatedAtDesc(String merchantId);
}
