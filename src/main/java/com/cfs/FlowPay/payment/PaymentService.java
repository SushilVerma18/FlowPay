package com.cfs.FlowPay.payment;


import com.cfs.FlowPay.bank.MockBankService;
import com.cfs.FlowPay.common.ApiException;
import com.cfs.FlowPay.common.Idempotency;
import com.cfs.FlowPay.common.Ids;
import com.cfs.FlowPay.kafka.EventService;
import com.cfs.FlowPay.merchant.Merchant;
import com.cfs.FlowPay.merchant.MerchantService;
import com.cfs.FlowPay.order.OrderService;
import com.cfs.FlowPay.order.PaymentOrder;
import com.cfs.FlowPay.transaction.LedgerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final MerchantService merchants;
    private final OrderService orders;
    private final MockBankService bank;
    private final EventService event;
    private final LedgerService ledger;


    public PaymentService(PaymentRepository repo, MerchantService merchants, OrderService orders, MockBankService bank, EventService event, LedgerService ledger) {
        this.repo = repo;
        this.merchants = merchants;
        this.orders = orders;
        this.bank = bank;
        this.event = event;
        this.ledger = ledger;
    }

    public static long fee(long amount)
    {
        return (Math.multiplyExact(amount, 2) +99)/100;
    }

    @Transactional
    public Payment create(String m,String key,PaymentDtos.Create r)
    {
        Idempotency.validate(key);
        merchants.lock(m);
        Payment existing =  repo.findByMerchantIdAndIdempotencyKey(m,key).orElse(null);
        if(existing!=null)
        {
            if(!existing.orderId.equals(r.orderId())
            || !existing.method.equals(r.method().name())
            || !existing.scenario.equals(r.scenario().name()))
            {
                throw ApiException.conflict("Idempotency key resued with different request");
            }
            return existing;
        }

        PaymentOrder o=orders.get(m,r.orderId());
        if(o.status.equals("PAID") ||
                repo.existsByOrderIdAndStatusIn(o.id, List.of("CREATED","PENDING","SUCCESS")))
        {
            throw ApiException.conflict("Order already paid or has an active payment");
        }
        Payment p=new Payment();
        p.id= Ids.next("pay");
        p.merchantId=m;
        p.orderId=o.id;
        p.amount=o.amount;
        p.idempotencyKey=key;
        p.method=r.method().name();
        p.scenario=r.scenario().name();
        return repo.save(p);
    }

    public Payment get(String m,String id)
    {
        Payment p=repo.findById(id).orElseThrow(ApiException::missing);
        if(!p.merchantId.equals(m)) throw ApiException.missing();
        return p;
    }

    @Transactional
    public Payment process(String m,String id)
    {
        Merchant merchant=merchants.lock(m);
        Payment p =get(m,id);
        if(!p.status.equals("CREATED")) return p;
        MockBankService.Result result=bank.initiate(p.scenario);
        p.resolveAt=result.resolveAt();
        finish(merchant,p,result.status());
        return p;
    }


    private void finish(Merchant m,Payment p,String staus)
    {
        p.status=staus;
        if(staus.equals("SUCCESS"))
        {
            p.fee=fee(p.amount);
            orders.get(m.id,p.orderId).status="PAID";
            ledger.record(m,p.id,"PAYMENT",p.amount-p.fee);
           // event.create()
        }
        else if(staus.equals("FAILED"))
        {
            //event.create
        }
    }

    @Transactional
    public Payment reconcile(String m,String id)
    {
        Merchant merchant=merchants.lock(m);
        Payment p=get(m,id);
        if(p.status.equals("PENDING"))
        {
            finish(merchant,p, bank.check(p.resolveAt));
        }
        return p;
    }

    public List<Payment> list(String m)
    {
        return repo.findByMerchantIdOrderByCreatedAtDesc(m);
    }
}
