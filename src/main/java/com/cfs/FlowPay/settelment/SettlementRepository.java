package com.cfs.FlowPay.settelment;

import com.cfs.FlowPay.payment.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Payment e where e.id=:id")
    Optional<Settlement> lockById(@Param("id") String id);

    Optional<Settlement> findByMerchantIdAndIdempotencyKey(String m,String k);

    List<Settlement> findByMerchantIdOrderByCreatedAtDesc(String m);
}
