package com.npl.na_ponta_do_lapis.service.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService("minha-chave-secreta-de-teste-12345");
    }

    @Test
    @DisplayName("Deve criptografar e descriptografar texto com sucesso")
    void deveCriptografarEDescriptografarComSucesso() {
        String textoOriginal = "AIzaSySecretApiKey123456789";

        String cifrado = cryptoService.encrypt(textoOriginal);
        assertThat(cifrado).isNotNull();
        assertThat(cifrado).isNotEqualTo(textoOriginal);

        String decifrado = cryptoService.decrypt(cifrado);
        assertThat(decifrado).isEqualTo(textoOriginal);
    }

    @Test
    @DisplayName("Deve gerar cifras diferentes para o mesmo texto devido ao IV aleatório")
    void deveGerarCifrasDiferentesDevidoAoIvAleatorio() {
        String texto = "mesmo-texto-secreto";

        String cifra1 = cryptoService.encrypt(texto);
        String cifra2 = cryptoService.encrypt(texto);

        assertThat(cifra1).isNotEqualTo(cifra2);
        assertThat(cryptoService.decrypt(cifra1)).isEqualTo(texto);
        assertThat(cryptoService.decrypt(cifra2)).isEqualTo(texto);
    }

    @Test
    @DisplayName("Deve retornar nulo ou vazio se o texto de entrada for nulo ou vazio")
    void deveTratarTextoNuloOuVazio() {
        assertThat(cryptoService.encrypt(null)).isNull();
        assertThat(cryptoService.encrypt("")).isEmpty();
        assertThat(cryptoService.decrypt(null)).isNull();
        assertThat(cryptoService.decrypt("")).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar ao decifrar texto adulterado (GCM tag verification)")
    void deveFalharAoDecifrarTextoAdulterado() {
        String original = "meu-secret-pluggy-super-confidencial";
        String cifrado = cryptoService.encrypt(original);

        byte[] bytes = Base64.getDecoder().decode(cifrado);
        bytes[bytes.length - 1] ^= 0x01; // Adultera o último byte da tag de autenticação
        String cifradoAdulterado = Base64.getEncoder().encodeToString(bytes);

        assertThatThrownBy(() -> cryptoService.decrypt(cifradoAdulterado))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Falha ao descriptografar");
    }

    @Test
    @DisplayName("Deve mascarar texto sensível preservando início e fim")
    void deveMascararTextoSensivel() {
        assertThat(cryptoService.mask("AIzaSySecret1234ApiKey9999")).isEqualTo("AIza••••••••9999");
        assertThat(cryptoService.mask("curto")).isEqualTo("••••••••");
        assertThat(cryptoService.mask(null)).isEmpty();
        assertThat(cryptoService.mask("   ")).isEmpty();
    }
}

