package com.cfs.FlowPay.transaction;

import com.cfs.FlowPay.common.ApiException;
import com.cfs.FlowPay.payment.Payment;
import com.cfs.FlowPay.payment.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TransactionService {

    private final PaymentService payments;
    private final LedgerEntryRepository ledger;


    public TransactionService(PaymentService payments, LedgerEntryRepository ledger) {
        this.payments = payments;
        this.ledger = ledger;
    }

    public List<Payment> list(String merchant, String status, LocalDate from,LocalDate to)
    {
        if(from!=null && to !=null && from.isAfter(to))
        {
                throw ApiException.bad("From date must not be after to date");
        }

        return payments.list(merchant).stream()
                .filter(p->status==null || status.isBlank()||status.equals(p.status))
                .filter(p->
                        from==null || !p.createdAt.isBefore(from.atStartOfDay(ZoneOffset.UTC).toInstant()))
                .filter(p->
                        to==null || p.createdAt.isBefore(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()))
                .toList();
    }

    public List<LedgerEntry> ledger(String m)
    {
        return ledger.findByMerchantIdOrderByCreatedAtDesc(m);
    }
}
