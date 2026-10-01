package com.rhsystem.infrastructure.email;

import com.rhsystem.application.port.UserNotifier;
import com.rhsystem.domain.event.PasswordResetRequested;
import com.rhsystem.domain.event.UserCreated;
import com.rhsystem.infrastructure.config.RhSystemProperties;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Email notification implementation via SMTP (Gmail).
 *
 * <p>Called asynchronously, after the commit, by
 * {@link com.rhsystem.application.listener.UserNotificationListener}.</p>
 */
@Component
public class EmailUserNotifier implements UserNotifier {

    private static final Locale EMAIL_LOCALE = Locale.of("pt", "BR");

    private final JavaMailSender mailSender;
    private final RhSystemProperties properties;
    private final MessageSource messageSource;

    public EmailUserNotifier(JavaMailSender mailSender, RhSystemProperties properties, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.messageSource = messageSource;
    }

    @Override
    public void sendActivation(UserCreated event) {
        String link = properties.getBaseUrl() + "/activate/" + event.activationToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(event.email());
        message.setSubject(messageSource.getMessage("email.activation.subject", null, EMAIL_LOCALE));
        message.setText(
                "Olá " + event.firstName() + ",\n\n"
                + "Seu usuário (" + event.username() + ") foi criado no RH System.\n"
                + "Para ativar a conta e definir sua senha, acesse o link abaixo:\n\n"
                + link + "\n\n"
                + "O link expira em " + properties.getActivationTokenValidityHours() + " horas.\n\n"
                + "RH System");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordReset(PasswordResetRequested event) {
        String link = properties.getBaseUrl() + "/reset-password/" + event.resetToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(event.email());
        message.setSubject(messageSource.getMessage("email.reset.subject", null, EMAIL_LOCALE));
        message.setText(
                "Olá " + event.firstName() + ",\n\n"
                + "Recebemos uma solicitação para redefinir a senha da sua conta (" + event.username() + ").\n"
                + "Para criar uma nova senha, acesse o link abaixo:\n\n"
                + link + "\n\n"
                + "O link expira em " + properties.getPasswordResetTokenValidityMinutes() + " minutos e só o "
                + "link mais recente funciona.\n"
                + "Se você não solicitou, ignore este email.\n\n"
                + "RH System");
        mailSender.send(message);
    }
}
