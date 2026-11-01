package com.npl.na_ponta_do_lapis.service.fatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class FaturaSessionCookieHelperTest {

    private static final String NOME_COOKIE = "npl_fatura_ia";

    private FaturaSessionCookieHelper helper;

    @BeforeEach
    void configurar() {
        helper = new FaturaSessionCookieHelper(new ObjectMapper());
        ReflectionTestUtils.setField(helper, "nomeCookie", NOME_COOKIE);
        ReflectionTestUtils.setField(helper, "segredo", "segredo-de-teste-com-pelo-menos-32-bytes");
        ReflectionTestUtils.setField(helper, "ttlMinutos", 60L);
        helper.inicializar();
    }

    @Test
    void gravaCookieCriptografadoEConsegueLerDeVolta() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        helper.gravar(response, request, ProvedorIa.CLAUDE, "sk-ant-secreta");

        Cookie cookie = capturarCookie(response);
        assertThat(cookie.getName()).isEqualTo(NOME_COOKIE);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/api/v1/faturas");
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Strict");
        assertThat(cookie.getValue()).doesNotContain("sk-ant-secreta");

        HttpServletRequest requisicaoComCookie = mock(HttpServletRequest.class);
        when(requisicaoComCookie.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<CredencialIa> credencial = helper.ler(requisicaoComCookie);
        assertThat(credencial).isPresent();
        assertThat(credencial.get().provedor()).isEqualTo(ProvedorIa.CLAUDE);
        assertThat(credencial.get().apiKey()).isEqualTo("sk-ant-secreta");
    }

    @Test
    void cookieExpiradoEhIgnorado() {
        ReflectionTestUtils.setField(helper, "ttlMinutos", -1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        helper.gravar(response, request, ProvedorIa.GEMINI, "chave");

        Cookie cookie = capturarCookie(response);
        HttpServletRequest requisicaoComCookie = mock(HttpServletRequest.class);
        when(requisicaoComCookie.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThat(helper.ler(requisicaoComCookie)).isEmpty();
    }

    @Test
    void cookieAdulteradoEhIgnorado() {
        HttpServletRequest requisicao = mock(HttpServletRequest.class);
        Cookie adulterado = new Cookie(NOME_COOKIE, "valor-invalido");
        when(requisicao.getCookies()).thenReturn(new Cookie[]{adulterado});

        assertThat(helper.ler(requisicao)).isEmpty();
    }

    @Test
    void limparRemoveOCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        helper.limpar(response, request);

        Cookie cookie = capturarCookie(response);
        assertThat(cookie.getName()).isEqualTo(NOME_COOKIE);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
    }

    private Cookie capturarCookie(HttpServletResponse response) {
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        return captor.getValue();
    }
}
