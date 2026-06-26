package com.rhsystem.infrastructure.email;

import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.infrastructure.config.RhSystemProperties;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Email notification implementation via SMTP (Gmail).
 */
@Component
public class EmailUserNotifier implements UserNotifier {

    private static final Locale EMAIL_LOCALE = new Locale("pt", "BR");

    private final JavaMailSender mailSender;
    private final RhSystemProperties properties;
    private final MessageSource messageSource;

    public EmailUserNotifier(JavaMailSender mailSender, RhSystemProperties properties, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.messageSource = messageSource;
    }

    @Override
    public void sendActivation(User user, String token) {
        String link = properties.getBaseUrl() + "/activate/" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(user.getEmail());
        message.setSubject(messageSource.getMessage("email.activation.subject", null, EMAIL_LOCALE));
        message.setText(
                "Olá " + user.getFirstName() + ",\n\n"
                + "Seu usuário (" + user.getUsername() + ") foi criado no RH System.\n"
                + "Para ativar a conta e definir sua senha, acesse o link abaixo:\n\n"
                + link + "\n\n"
                + "O link expira em " + properties.getActivationTokenValidityHours() + " horas.\n\n"
                + "RH System");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordReset(User user, String token) {
        String link = properties.getBaseUrl() + "/reset-password/" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(user.getEmail());
        message.setSubject(messageSource.getMessage("email.reset.subject", null, EMAIL_LOCALE));
        message.setText(
                "Olá " + user.getFirstName() + ",\n\n"
                + "Recebemos uma solicitação para redefinir a senha da sua conta (" + user.getUsername() + ").\n"
                + "Para criar uma nova senha, acesse o link abaixo:\n\n"
                + link + "\n\n"
                + "O link expira em " + properties.getActivationTokenValidityHours() + " horas.\n"
                + "Se você não solicitou, ignore este email.\n\n"
                + "RH System");
        mailSender.send(message);
    }
}
