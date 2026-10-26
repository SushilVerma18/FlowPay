package com.cfs.FlowPay;

import com.cfs.FlowPay.merchant.MerchantDtos;
import com.cfs.FlowPay.merchant.Merchant;
import com.cfs.FlowPay.merchant.MerchantRepository;
import com.cfs.FlowPay.merchant.MerchantService;
import com.cfs.FlowPay.order.OrderDtos;
import com.cfs.FlowPay.order.OrderService;
import com.cfs.FlowPay.order.PaymentOrder;
import com.cfs.FlowPay.payment.Payment;
import com.cfs.FlowPay.payment.PaymentDtos;
import com.cfs.FlowPay.payment.PaymentService;
import com.cfs.FlowPay.refund.RefundDtos;
import com.cfs.FlowPay.refund.RefundService;
import com.cfs.FlowPay.transaction.LedgerEntry;
import com.cfs.FlowPay.transaction.LedgerEntryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:flowpay;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jobs-enabled=false",
		"app.kafka-enabled=false"
})
class FlowPayApplicationTests {

	@Autowired
	private MerchantService merchantService;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private LedgerEntryRepository ledgerRepository;

	@Autowired
	private RefundService refundService;

	@Test
	void contextLoads() {
	}

	@Test
	void registrationReturnsAnApiKeyThatCanAuthenticate() {
		MerchantDtos.Registered registered = merchantService.register(
				new MerchantDtos.Register("Test merchant", "merchant@example.com"));

		Assertions.assertTrue(registered.apiKey().startsWith("fp_test_"));
		Assertions.assertEquals(
				registered.merchant().id,
				merchantService.authenticate(registered.apiKey()).id);
	}

	@Test
	void successfulPaymentCreditsNetAmountToMerchantOnlyOnce() {
		Merchant merchant = merchantService.register(
				new MerchantDtos.Register("Balance test merchant", "balance@example.com"))
				.merchant();
		PaymentOrder order = orderService.create(
				merchant.id, new OrderDtos.Create(10_000, "balance-test"));
		Payment payment = paymentService.create(
				merchant.id,
				"payment-balance-test",
				new PaymentDtos.Create(
						order.id,
						PaymentDtos.Method.UPI,
						PaymentDtos.Scenario.SUCCESS));

		Payment processed = paymentService.process(merchant.id, payment.id);
		paymentService.process(merchant.id, payment.id);
		refundService.create(
				merchant.id,
				"refund-balance-test",
				new RefundDtos.Create(payment.id, 10_000));

		Merchant updatedMerchant = merchantRepository.findById(merchant.id).orElseThrow();
		Assertions.assertEquals("SUCCESS", processed.status);
		Assertions.assertEquals(0, updatedMerchant.balance);

		List<LedgerEntry> entries =
				ledgerRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.id);
		Assertions.assertEquals(2, entries.size());
		Assertions.assertTrue(entries.stream().anyMatch(entry ->
				entry.type.equals("PAYMENT")
						&& entry.amount == 9_800
						&& entry.balanceAfter == 9_800));
		Assertions.assertTrue(entries.stream().anyMatch(entry ->
				entry.type.equals("REFUND")
						&& entry.amount == -9_800
						&& entry.balanceAfter == 0));
	}

}
