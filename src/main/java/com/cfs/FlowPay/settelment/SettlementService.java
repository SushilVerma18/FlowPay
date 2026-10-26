package com.cfs.FlowPay.settelment;

import com.cfs.FlowPay.common.ApiException;
import com.cfs.FlowPay.common.Idempotency;
import com.cfs.FlowPay.common.Ids;
import com.cfs.FlowPay.merchant.Merchant;
import com.cfs.FlowPay.merchant.MerchantService;
import com.cfs.FlowPay.transaction.LedgerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementService {

    private final SettlementRepository repo;

    private final MerchantService merchants;
    private final LedgerService ledger;


    public SettlementService(SettlementRepository repo, MerchantService merchants, LedgerService ledger) {
        this.repo = repo;
        this.merchants = merchants;
        this.ledger = ledger;
    }


    public Settlement create(String m,String key)
    {
        Idempotency.validate(key);
        Merchant merchant=merchants.lock(m);
        Settlement old=repo.findByMerchantIdAndIdempotencyKey(m,key).orElse(null);
        if(old!=null) return old;
        if(merchant.balance<0) throw ApiException.conflict("No available balance to settle");
        Settlement s= new Settlement();
        s.id= Ids.next("stl");
        s.merchantId=m;
        s.idempotencyKey=key;
        s.amount=merchant.balance;
        repo.save(s);
        ledger.record(merchant,s.id,"SETTLEMENT",-s.amount);
        return s;
    }

    public List<Settlement> list(String  s)
    {
        return repo.findByMerchantIdOrderByCreatedAtDesc(s);
    }
}
