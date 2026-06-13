package kr.co.actify.user.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kr.co.actify.user.global.config.properties.MailProperties;
import kr.co.actify.user.model.dto.mail.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {

    private static final String ENCODING = StandardCharsets.UTF_8.name();

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    /**
     * 이메일을 비동기로 전송합니다.
     *
     * 호출부에서 반환값을 사용하지 않아도 기존처럼 동작합니다.
     * 다만 필요하면 CompletableFuture를 통해 전송 실패 여부를 확인할 수 있습니다.
     */
    @Async("mailExecutor")
    public CompletableFuture<Void> sendEmail(EmailMessage emailMessage, boolean isHtml) {
        String to = getSafeTo(emailMessage);
        String subject = getSafeSubject(emailMessage);

        try {
            validateEmailMessage(emailMessage);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, ENCODING);

            helper.setFrom(new InternetAddress(
                    mailProperties.getFromAddress(),
                    mailProperties.getFromName(),
                    ENCODING
            ));

            helper.setTo(emailMessage.getTo());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getMessage(), isHtml);
            helper.setSentDate(new Date());

            javaMailSender.send(mimeMessage);

            log.info(
                    "Email sent successfully. to={}, subject={}",
                    maskEmail(emailMessage.getTo()),
                    emailMessage.getSubject()
            );

            return CompletableFuture.completedFuture(null);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(
                    "Failed to build email message. to={}, subject={}",
                    maskEmail(to),
                    subject,
                    e
            );
            return CompletableFuture.failedFuture(e);

        } catch (MailException | IllegalArgumentException e) {
            log.error(
                    "Failed to send email. to={}, subject={}",
                    maskEmail(to),
                    subject,
                    e
            );
            return CompletableFuture.failedFuture(e);
        }
    }

    private void validateEmailMessage(EmailMessage emailMessage) {
        if (emailMessage == null) {
            throw new IllegalArgumentException("이메일 메시지는 null일 수 없습니다.");
        }

        if (!StringUtils.hasText(emailMessage.getTo())) {
            throw new IllegalArgumentException("이메일 수신자 주소는 필수입니다.");
        }

        if (!StringUtils.hasText(emailMessage.getSubject())) {
            throw new IllegalArgumentException("이메일 제목은 필수입니다.");
        }

        if (!StringUtils.hasText(emailMessage.getMessage())) {
            throw new IllegalArgumentException("이메일 본문은 필수입니다.");
        }
    }

    private String getSafeTo(EmailMessage emailMessage) {
        return emailMessage == null ? "" : emailMessage.getTo();
    }

    private String getSafeSubject(EmailMessage emailMessage) {
        return emailMessage == null ? "" : emailMessage.getSubject();
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }

        int atIndex = email.indexOf("@");

        if (atIndex <= 0) {
            return "****";
        }

        return email.charAt(0) + "****" + email.substring(atIndex);
    }
}