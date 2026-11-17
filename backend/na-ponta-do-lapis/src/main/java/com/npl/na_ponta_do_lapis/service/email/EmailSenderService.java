package com.npl.na_ponta_do_lapis.service.email;

public interface EmailSenderService {

    boolean isEnabled();

    void sendPasswordResetEmail(String toEmail, String nomeUsuario, String resetLink);

    void sendEmailVerificationEmail(String toEmail, String nomeUsuario, String verificationLink);
}

