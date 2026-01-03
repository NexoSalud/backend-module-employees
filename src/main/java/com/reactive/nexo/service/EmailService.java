package com.reactive.nexo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from:${EMAIL_FROM:no-reply@nexosalud.com}}")
    private String fromEmail;

    @Value("${email.reset-password.subject:${EMAIL_RESET_PASSWORD_SUBJECT:Recuperación de Contraseña - Nexo Salud}}")
    private String resetPasswordSubject;

    @Value("${email.reset-password.text:${EMAIL_RESET_PASSWORD_TEXT:Estimado usuario, para recuperar su contraseña haga clic en el siguiente enlace:}}")
    private String resetPasswordText;

    @Value("${email.reset-password.website:${EMAIL_RESET_PASSWORD_WEBSITE:https://nexosalud.com/reset-password}}")
    private String resetPasswordWebsite;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send password reset email
     */
    public Mono<Boolean> sendPasswordResetEmail(String toEmail, String resetToken) {
        return Mono.fromCallable(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(resetPasswordSubject);
                
                String emailBody = buildPasswordResetEmailBody(resetToken);
                message.setText(emailBody);
                
                mailSender.send(message);
                log.info("Password reset email sent successfully to: {}", toEmail);
                return true;
                
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorReturn(false);
    }

    private String buildPasswordResetEmailBody(String resetToken) {
        StringBuilder body = new StringBuilder();
        body.append(resetPasswordText).append("\n\n");
        body.append(resetPasswordWebsite).append("?token=").append(resetToken).append("\n\n");
        body.append("Este enlace expirará en 1 hora.\n");
        body.append("Si no solicitó este cambio, ignore este mensaje.\n\n");
        body.append("Atentamente,\n");
        body.append("Equipo Nexo Salud");
        logger.info("Built password reset email body: {}", body.toString());
        
        return body.toString();
    }

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
}