package com.cfs.FlowPay.refund;

import com.cfs.FlowPay.common.ApiException;
import com.cfs.FlowPay.common.Idempotency;
import com.cfs.FlowPay.common.Ids;
import com.cfs.FlowPay.kafka.EventService;
import com.cfs.FlowPay.merchant.Merchant;
import com.cfs.FlowPay.merchant.MerchantService;
import com.cfs.FlowPay.payment.Payment;
import com.cfs.FlowPay.payment.PaymentService;
import com.cfs.FlowPay.transaction.LedgerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefundService {

    private final RefundRepository repo;
    private final MerchantService merchants;
    private final PaymentService payments;
    private final EventService events;
    private final LedgerService ledger;

    public RefundService(RefundRepository repo, MerchantService merchants, PaymentService payments, EventService events, LedgerService ledger) {
        this.repo = repo;
        this.merchants = merchants;
        this.payments = payments;
        this.events = events;
        this.ledger = ledger;
    }


    @Transactional
    public Refund create(String m,String key,RefundDtos.Create request)
    {
        Idempotency.validate(key);
        Merchant merchant = merchants.lock(m);
        Refund old=repo.findByMerchantIdAndIdempotencyKey(m,key).orElse(null);
        if(old!=null)
        {
            if(!old.paymentId.equals(request.paymentId()) || old.amount!=request.amount())
            {
                    throw ApiException.conflict("Idempotency key reused with different refund");
            }
            return old;
        }

        Payment p=payments.get(m,request.paymentId());
        if(!p.status.equals("SUCCESS"))
            throw ApiException.conflict("Only Success payment cab be refunded");

        if(request.amount()<1 || request.amount()>p.amount-p.refunded)
            throw ApiException.conflict("Refund exceeds remaining payment amount");

        long before = p.refunded;
        long after = before+request.amount();
        long feeReturned = (p.fee * after/p.amount) - (p.fee*before/p.amount);
        long debit=request.amount()-feeReturned;
        if(merchant.balance<debit)
        {
            throw ApiException.conflict("Insufficient available balance.");
        }

        Refund r=new Refund();
        r.id= Ids.next("rfnd");
        r.merchantId=m;
        r.paymentId=p.id;
        r.idempotencyKey=key;
        r.amount=request.amount();
        p.refunded=after;
        repo.save(r);
        ledger.record(merchant,r.id,"REFUND",-debit);
        //event.create()
        return r;

    }

    public List<Refund> list(String s)
    {
        return repo.findByMerchantIdOrderByCreatedAtDesc(s);
    }

}
