package com.cfs.FlowPay.merchant;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant,String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Merchant e where e.id=:id")
    Optional<Merchant> lockById(@Param("id") String id);


    Optional<Merchant> findByApiKeyHash(String hash);
}
