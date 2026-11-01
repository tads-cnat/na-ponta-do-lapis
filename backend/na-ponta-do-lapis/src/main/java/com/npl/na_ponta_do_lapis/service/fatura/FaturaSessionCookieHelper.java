package com.npl.na_ponta_do_lapis.service.fatura;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cookie de sessão criptografado (AES-GCM) que guarda a chave de IA do usuário.
 *
 * - HttpOnly: JavaScript não lê
 * - SameSite=Strict: não acompanha requisições de outro site
 * - Path restrito a /api/v1/faturas
 * - sem Max-Age: cookie de sessão (some ao fechar o navegador)
 * - exp lógico de 1h dentro do payload
 */
@Service
public class FaturaSessionCookieHelper {

    private static final Logger log = LoggerFactory.getLogger(FaturaSessionCookieHelper.class);
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final String PATH = "/api/v1/faturas";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;

    @Value("${fatura.cookie.nome:npl_fatura_ia}")
    private String nomeCookie;

    @Value("${fatura.cookie.secret}")
    private String segredo;

    @Value("${fatura.cookie.ttl-minutes:60}")
    private long ttlMinutos;

    private SecretKeySpec chave;

    public FaturaSessionCookieHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void inicializar() {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(segredo.getBytes(StandardCharsets.UTF_8));
            this.chave = new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível inicializar a criptografia do cookie de fatura.", e);
        }
    }

    public Optional<CredencialIa> ler(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (!nomeCookie.equals(cookie.getName())) {
                continue;
            }
            try {
                JsonNode payload = objectMapper.readTree(descriptografar(cookie.getValue()));
                long exp = payload.path("exp").asLong(0);
                if (exp < System.currentTimeMillis()) {
                    return Optional.empty();
                }
                ProvedorIa provedor = ProvedorIa.valueOf(payload.path("provedor").asText());
                String apiKey = payload.path("apiKey").asText(null);
                if (apiKey == null || apiKey.isBlank()) {
                    return Optional.empty();
                }
                return Optional.of(new CredencialIa(provedor, apiKey));
            } catch (Exception e) {
                log.debug("Cookie de fatura inválido ou expirado; ignorando.");
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public void gravar(HttpServletResponse response, HttpServletRequest request,
                       ProvedorIa provedor, String apiKey) {
        if (response == null || provedor == null || apiKey == null || apiKey.isBlank()) {
            return;
        }
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("provedor", provedor.name());
            payload.put("apiKey", apiKey);
            payload.put("exp", System.currentTimeMillis() + ttlMinutos * 60_000L);

            Cookie cookie = criarCookie(request);
            cookie.setValue(criptografar(objectMapper.writeValueAsBytes(payload)));
            response.addCookie(cookie);
        } catch (Exception e) {
            log.warn("Não foi possível gravar o cookie de sessão da fatura.");
        }
    }

    public void limpar(HttpServletResponse response, HttpServletRequest request) {
        if (response == null) {
            return;
        }
        Cookie cookie = criarCookie(request);
        cookie.setValue("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Cookie criarCookie(HttpServletRequest request) {
        Cookie cookie = new Cookie(nomeCookie, "");
        cookie.setHttpOnly(true);
        cookie.setPath(PATH);
        cookie.setSecure(isHttps(request));
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    private boolean isHttps(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    }

    private String criptografar(byte[] payload) throws Exception {
        byte[] iv = new byte[GCM_IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] criptografado = cipher.doFinal(payload);

        byte[] resultado = new byte[iv.length + criptografado.length];
        System.arraycopy(iv, 0, resultado, 0, iv.length);
        System.arraycopy(criptografado, 0, resultado, iv.length, criptografado.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(resultado);
    }

    private String descriptografar(String valor) throws Exception {
        byte[] dados = Base64.getUrlDecoder().decode(valor);
        if (dados.length <= GCM_IV_BYTES) {
            throw new IllegalArgumentException("Payload criptografado inválido.");
        }
        byte[] iv = new byte[GCM_IV_BYTES];
        byte[] criptografado = new byte[dados.length - GCM_IV_BYTES];
        System.arraycopy(dados, 0, iv, 0, GCM_IV_BYTES);
        System.arraycopy(dados, GCM_IV_BYTES, criptografado, 0, criptografado.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return new String(cipher.doFinal(criptografado), StandardCharsets.UTF_8);
    }
}
