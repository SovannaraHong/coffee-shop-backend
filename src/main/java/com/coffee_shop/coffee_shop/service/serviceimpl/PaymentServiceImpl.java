package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.client.CutLuyClient;
import com.coffee_shop.coffee_shop.dto.response.CutLuyPaymentResponse;
import com.coffee_shop.coffee_shop.dto.response.PaymentResponse;
import com.coffee_shop.coffee_shop.entity.Order;
import com.coffee_shop.coffee_shop.entity.Payment;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.CutLuyStatusMapper;
import com.coffee_shop.coffee_shop.repository.OrderRepository;
import com.coffee_shop.coffee_shop.repository.PaymentRepository;
import com.coffee_shop.coffee_shop.service.PaymentService;
import com.coffee_shop.coffee_shop.util.enums.OrderStatus;
import com.coffee_shop.coffee_shop.util.enums.PaymentMethod;
import com.coffee_shop.coffee_shop.util.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CutLuyClient cutLuyClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /*
    V1 of payment
     */
//    @Override
//    @Transactional
//    public KhqrPaymentResult createKhqrPayment(Order order, BigDecimal amount) {
//        CutLuyPaymentResponse cutLuyPayment = cutLuyClient.createPayment(amount, buildReferenceId(order));
//
//        Payment payment = Payment.builder()
//                .order(order)
//                .method(PaymentMethod.KHQR)
//                .status(PaymentStatus.PENDING)
//                .amount(amount)
//                .transactionRef(cutLuyPayment.id())
//                .build();
//
//        payment = paymentRepository.save(payment);
//        log.info("Created KHQR payment id={} for orderId={}, cutluyPaymentId={}",
//                payment.getId(), order.getId(), cutLuyPayment.id());
//
//        return new KhqrPaymentResult(
//                toResponse(payment),
//                cutLuyPayment.checkout_url(),
//                cutLuyPayment.qr_string()
//        );
//    }

    @Override
    @Transactional
    public KhqrPaymentResult createKhqrPayment(Order order, BigDecimal amount) {
        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethod.KHQR)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .build();
        payment = paymentRepository.save(payment);

        String referenceId = "order_" + order.getId() + "_payment_" + payment.getId();
        CutLuyPaymentResponse cutLuyPayment = cutLuyClient.createPayment(amount, referenceId);
        payment.setTransactionRef(cutLuyPayment.id());
        payment = paymentRepository.save(payment);
        log.info("Created KHQR payment id={} for orderId={}, cutluyPaymentId={}",
                payment.getId(), order.getId(), cutLuyPayment.id());
        
        return new KhqrPaymentResult(
                toResponse(payment),
                cutLuyPayment.checkout_url(),
                cutLuyPayment.qr_string()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        return toResponse(getRequiredPayment(paymentId));
    }

    @Override
    @Transactional
    public PaymentResponse refreshPaymentStatus(Long paymentId) {
        Payment payment = getRequiredPayment(paymentId);

        if (isTerminal(payment.getStatus())) {
            return toResponse(payment);
        }

        CutLuyPaymentResponse remote = cutLuyClient.getPayment(payment.getTransactionRef());
        PaymentStatus mapped = CutLuyStatusMapper.toPaymentStatus(remote.status());

        if (mapped != payment.getStatus()) {
            payment.setStatus(mapped);

            if (mapped == PaymentStatus.PAID) {
                payment.setPaidAt(LocalDateTime.now());
                confirmOrderIfPending(payment.getOrder());
            }

            paymentRepository.save(payment);
            log.info("Payment id={} status updated to {}", payment.getId(), mapped);
        }

        return toResponse(payment);
    }

    // =========================
    // Private helpers
    // =========================

    private Payment getRequiredPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Payment", paymentId));
    }

    private boolean isTerminal(PaymentStatus status) {
        return status == PaymentStatus.PAID
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.REFUNDED;
    }

    private void confirmOrderIfPending(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
    }

    private String buildReferenceId(Order order) {
        return "order_" + order.getId();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getTransactionRef(),
                payment.getPaidAt()
        );
    }
}