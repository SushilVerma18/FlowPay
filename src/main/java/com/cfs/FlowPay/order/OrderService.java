package com.cfs.FlowPay.order;

import com.cfs.FlowPay.common.ApiException;
import com.cfs.FlowPay.common.Ids;
import com.cfs.FlowPay.merchant.MerchantService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final PaymentOrderRepository repo;

    private final MerchantService merchants;

    public OrderService(PaymentOrderRepository repo, MerchantService merchants) {
        this.repo = repo;
        this.merchants = merchants;
    }


    @Transactional
    public PaymentOrder create(String merchantId,OrderDtos.Create r)
    {
        merchants.lock(merchantId);
        PaymentOrder o=new PaymentOrder();
        o.id= Ids.next("order");
        o.merchantId=merchantId;
        o.amount=r.amount();
        o.receipt=r.receipt();
        return repo.save(o);
    }

    public PaymentOrder get(String merchantId,String id)
    {
        PaymentOrder o=repo.findById(id).orElseThrow(ApiException::missing);
        if(!o.merchantId.equals(merchantId)) throw ApiException.missing();
        return o;
    }


    public List<PaymentOrder> list(String merchantId)
    {
        return repo.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }
}
