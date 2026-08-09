package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.entity.Otp;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.repository.OtpRepository;
import com.coffee_shop.coffee_shop.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpRepository otpRepository;
    private final JavaMailSender javaMailSender;

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    @Override
    public void generateAndSendOtp(String email) {
        otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .filter(last -> last.getCreatedAt()
                        .isAfter(LocalDateTime.now()
                                .minusSeconds(RESEND_COOLDOWN_SECONDS)))
                .ifPresent(last -> {
                    throw new BadRequestException(
                            "Please wait before requesting another code"
                    );
                });


        String code = generateNumericCode(OTP_LENGTH);
        Otp otp = Otp.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(otp);

        sendEmail(email, code);
    }

    @Override
    public void verifyOtp(String email, String code) {
        Otp otp = otpRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() ->
                        new BadRequestException(
                                "No OTP request found for this email"
                        )
                );


        if (otp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "OTP has expired"
            );
        }
        if (!otp.getCode().equals(code)) {

            throw new BadRequestException(
                    "Invalid OTP code"
            );
        }
        otp.setUsed(true);
        otpRepository.save(otp);

    }

    //helper method
    private String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your verification code");
        message.setText("Your OTP code is: " + code +
                "\nThis code expires in "
                + EXPIRY_MINUTES + " minutes.");

        javaMailSender.send(message);
    }
}
