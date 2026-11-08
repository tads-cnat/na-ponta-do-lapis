package com.npl.na_ponta_do_lapis.service.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public CryptoService(@Value("${app.security.encryption.key:NaPontaDoLapisSecretEncryptionKey2026AES256GCM!}") String secretKeyConfig) {
        this.secretKey = deriveKey(secretKeyConfig);
        this.secureRandom = new SecureRandom();
    }

    private SecretKey deriveKey(String secretKeyConfig) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(secretKeyConfig.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 não disponível", e);
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criptografar dados sensíveis", e);
        }
    }

    public String decrypt(String ciphertextBase64) {
        if (ciphertextBase64 == null || ciphertextBase64.isEmpty()) {
            return ciphertextBase64;
        }

        try {
            byte[] cipherMessage = Base64.getDecoder().decode(ciphertextBase64);
            if (cipherMessage.length < GCM_IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Texto cifrado inválido: tamanho insuficiente para IV");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao descriptografar dados sensíveis", e);
        }
    }

    public String mask(String sensitiveText) {
        if (sensitiveText == null || sensitiveText.isBlank()) {
            return "";
        }
        int length = sensitiveText.length();
        if (length <= 8) {
            return "••••••••";
        }
        String prefix = sensitiveText.substring(0, 4);
        String suffix = sensitiveText.substring(length - 4);
        return prefix + "••••••••" + suffix;
    }
}

