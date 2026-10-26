package com.cfs.FlowPay.transaction;

import com.cfs.FlowPay.payment.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Refund e where e.id=:id")
    Optional<LedgerEntry> lockById(@Param("id") String id);


    List<LedgerEntry> findByMerchantIdOrderByCreatedAtDesc(String m);
}
