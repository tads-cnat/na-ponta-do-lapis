package com.npl.na_ponta_do_lapis.service.fatura;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FaturaSanitizerServiceTest {

    private final FaturaSanitizerService sanitizer = new FaturaSanitizerService();

    @Test
    void mascaraCpfCnpjEmailCartaoCepTelefoneEEndereco() {
        String texto = """
                Titular: Joao da Silva
                CPF: 123.456.789-09
                CNPJ: 12.345.678/0001-90
                E-mail: joao.silva@exemplo.com.br
                Cartao: 4111 1111 1111 1111
                CEP: 01310-100
                Telefone: (11) 98765-4321
                Endereco: Avenida Paulista 1000
                Compra: SUPERMERCADO 150,00
                """;

        String sanitizado = sanitizer.sanitizar(texto);

        assertThat(sanitizado)
                .doesNotContain("123.456.789-09")
                .doesNotContain("12.345.678/0001-90")
                .doesNotContain("joao.silva@exemplo.com.br")
                .doesNotContain("4111 1111 1111 1111")
                .doesNotContain("01310-100")
                .doesNotContain("(11) 98765-4321")
                .doesNotContain("Avenida Paulista 1000")
                .contains("***.***.***-**")
                .contains("***@***.***")
                .contains("**** **** **** ****")
                .contains("*** endereco ***")
                .contains("SUPERMERCADO 150,00");
    }

    @Test
    void textoNuloOuVazioRetornaVazio() {
        assertThat(sanitizer.sanitizar(null)).isEmpty();
        assertThat(sanitizer.sanitizar("   ")).isEmpty();
    }

    @Test
    void naoAlteraTextoSemDadosPessoais() {
        String texto = "15/01/2026 AMAZON 50,00";

        assertThat(sanitizer.sanitizar(texto)).isEqualTo(texto);
    }
}
