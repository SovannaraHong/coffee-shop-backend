package com.coffee_shop.coffee_shop.client;

import com.coffee_shop.coffee_shop.entity.Payment;
import com.coffee_shop.coffee_shop.repository.PaymentRepository;
import com.coffee_shop.coffee_shop.service.PaymentService;
import com.coffee_shop.coffee_shop.util.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingPaymentSweeper {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedRate = 15000)
    public void sweepPendingPayments() {
        List<Payment> pending = paymentRepository.findByStatus(PaymentStatus.PENDING);

        for (Payment payment : pending) {
            if (payment.getTransactionRef() == null) continue;

            try {
                paymentService.refreshPaymentStatus(payment.getId());
            } catch (Exception e) {
                log.error("Sweep failed for paymentId={}", payment.getId(), e);
            }
        }
    }
}