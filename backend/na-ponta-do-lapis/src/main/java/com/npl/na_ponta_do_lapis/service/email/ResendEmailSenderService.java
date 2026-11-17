package com.npl.na_ponta_do_lapis.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class ResendEmailSenderService implements EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSenderService.class);
    private static final URI RESEND_ENDPOINT = URI.create("https://api.resend.com/emails");
    private static final String DEFAULT_LOGO_URL = "https://napontadolapis.app/logoReduzida.png";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String fromEmail;
    private final String logoUrl;

    public ResendEmailSenderService(
            ObjectMapper objectMapper,
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from-email:Na Ponta do Lapis <onboarding@resend.dev>}") String fromEmail,
            @Value("${resend.logo-url:" + DEFAULT_LOGO_URL + "}") String logoUrl
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.fromEmail = fromEmail == null ? "" : fromEmail.strip();
        this.logoUrl = (logoUrl == null || logoUrl.isBlank()) ? DEFAULT_LOGO_URL : logoUrl.strip();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    @Override
    public boolean isEnabled() {
        return !apiKey.isBlank() && !fromEmail.isBlank();
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String nomeUsuario, String resetLink) {
        sendBrandedEmail(
                toEmail,
                nomeUsuario,
                "Redefinição de senha — Na Ponta do Lápis",
                "Recebemos uma solicitação para redefinir a senha da sua conta. Clique no botão abaixo para criar uma nova senha:",
                "Redefinir minha senha",
                resetLink,
                "Este link é válido por <strong>15 minutos</strong> e só pode ser utilizado uma única vez.",
                "Se você não solicitou essa alteração, ignore este e-mail com segurança.",
                "Para redefinir sua senha no Na Ponta do Lápis, acesse o link abaixo (válido por 15 minutos):"
        );
    }

    @Override
    public void sendEmailVerificationEmail(String toEmail, String nomeUsuario, String verificationLink) {
        sendBrandedEmail(
                toEmail,
                nomeUsuario,
                "Confirme seu e-mail — Na Ponta do Lápis",
                "Obrigado por criar sua conta no Na Ponta do Lápis! Confirme seu endereço de e-mail clicando no botão abaixo para liberar o acesso:",
                "Confirmar meu e-mail",
                verificationLink,
                "Este link é válido por <strong>24 horas</strong> e só pode ser utilizado uma única vez.",
                "Se você não criou uma conta no Na Ponta do Lápis, ignore este e-mail com segurança.",
                "Para confirmar seu e-mail no Na Ponta do Lápis, acesse o link abaixo (válido por 24 horas):"
        );
    }

    private void sendBrandedEmail(
            String toEmail,
            String nomeUsuario,
            String subject,
            String introParagraph,
            String ctaLabel,
            String actionLink,
            String validityNoteHtml,
            String footerNote,
            String plainTextIntro
    ) {
        if (!isEnabled()) {
            log.warn("Tentativa de envio de e-mail via Resend sem RESEND_API_KEY configurada.");
            return;
        }

        String nomeSeguro = (nomeUsuario == null || nomeUsuario.isBlank()) ? "usuário" : HtmlUtils.htmlEscape(nomeUsuario);
        String linkSeguro = HtmlUtils.htmlEscape(actionLink);
        String logoSrc = HtmlUtils.htmlEscape(logoUrl);

        String html = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; background: #F8FAFC; padding: 32px 16px;">
                  <div style="max-width: 520px; margin: 0 auto; background: #FFFFFF; border-radius: 16px; overflow: hidden; border: 1px solid #E2E8F0; box-shadow: 0 4px 16px rgba(74, 37, 133, 0.08);">
                    <div style="background: #D1C4FF; padding: 24px; text-align: center; border-bottom: 4px solid #EEC046;">
                      <img src="%s" alt="Na Ponta do Lápis" width="52" height="52" style="display: block; margin: 0 auto 10px auto; border-radius: 12px;" />
                      <h1 style="color: #4A2585; margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.02em;">Na Ponta do Lápis</h1>
                    </div>
                    <div style="padding: 28px 24px; color: #1E293B; line-height: 1.6;">
                      <p style="margin-top: 0; font-size: 16px;">Olá, <strong style="color: #4A2585;">%s</strong>!</p>
                      <p style="margin: 0 0 20px 0; font-size: 15px; color: #475569;">
                        %s
                      </p>
                      <p style="margin: 28px 0; text-align: center;">
                        <a href="%s" style="display: inline-block; background: #7843CD; color: #FFFFFF; font-weight: 700; font-size: 15px; text-decoration: none; padding: 13px 28px; border-radius: 12px;">
                          %s
                        </a>
                      </p>
                      <p style="font-size: 13px; color: #4A2585; background: #F3E8FF; padding: 12px 14px; border-radius: 10px; margin: 0 0 18px 0;">
                        %s
                      </p>
                      <p style="font-size: 12px; color: #64748B; margin: 0 0 10px 0;">
                        Se o botão não funcionar, copie e cole este link no navegador:<br />
                        <a href="%s" style="color: #7843CD; word-break: break-all;">%s</a>
                      </p>
                      <p style="font-size: 12px; color: #94A3B8; margin: 16px 0 0 0; border-top: 1px solid #F1F5F9; padding-top: 14px;">
                        %s
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(
                logoSrc,
                nomeSeguro,
                HtmlUtils.htmlEscape(introParagraph),
                linkSeguro,
                HtmlUtils.htmlEscape(ctaLabel),
                validityNoteHtml,
                linkSeguro,
                linkSeguro,
                HtmlUtils.htmlEscape(footerNote)
        );

        String text = "Olá, " + (nomeUsuario == null || nomeUsuario.isBlank() ? "usuário" : nomeUsuario)
                + "!\n\n" + plainTextIntro + "\n"
                + actionLink + "\n\n" + footerNote;

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "from", fromEmail,
                    "to", new String[]{toEmail},
                    "subject", subject,
                    "html", html,
                    "text", text
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(RESEND_ENDPOINT)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Resend retornou status {} ao enviar e-mail: {}", response.statusCode(), response.body());
                throw new IllegalStateException("Falha ao enviar e-mail.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao chamar API do Resend: {}", e.getMessage());
            throw new IllegalStateException("Não foi possível enviar o e-mail.");
        }
    }
}

