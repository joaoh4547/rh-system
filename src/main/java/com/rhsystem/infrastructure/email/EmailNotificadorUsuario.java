package com.rhsystem.infrastructure.email;

import com.rhsystem.application.port.NotificadorUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.infrastructure.config.RhSystemProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Implementação da notificação por email via SMTP (Gmail).
 */
@Component
public class EmailNotificadorUsuario implements NotificadorUsuario {

    private final JavaMailSender mailSender;
    private final RhSystemProperties properties;

    public EmailNotificadorUsuario(JavaMailSender mailSender, RhSystemProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void enviarAtivacao(Usuario usuario, String token) {
        String link = properties.getBaseUrl() + "/ativar/" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(properties.getMailFrom());
        mensagem.setTo(usuario.getEmail());
        mensagem.setSubject("Ative sua conta - RH System");
        mensagem.setText(
                "Olá " + usuario.getNome() + ",\n\n"
                + "Seu usuário (" + usuario.getUsername() + ") foi criado no RH System.\n"
                + "Para ativar a conta e definir sua senha, acesse o link abaixo:\n\n"
                + link + "\n\n"
                + "O link expira em " + properties.getAtivacaoTokenValidadeHoras() + " horas.\n\n"
                + "RH System");
        mailSender.send(mensagem);
    }

    @Override
    public void enviarRedefinicaoSenha(Usuario usuario, String token) {
        String link = properties.getBaseUrl() + "/redefinir/" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(properties.getMailFrom());
        mensagem.setTo(usuario.getEmail());
        mensagem.setSubject("Redefinição de senha - RH System");
        mensagem.setText(
                "Olá " + usuario.getNome() + ",\n\n"
                + "Recebemos uma solicitação para redefinir a senha da sua conta (" + usuario.getUsername() + ").\n"
                + "Para criar uma nova senha, acesse o link abaixo:\n\n"
                + link + "\n\n"
                + "O link expira em " + properties.getAtivacaoTokenValidadeHoras() + " horas.\n"
                + "Se você não solicitou, ignore este email.\n\n"
                + "RH System");
        mailSender.send(mensagem);
    }
}
