package com.npl.na_ponta_do_lapis.service.fatura;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;

/** Credencial de IA lida do cookie criptografado. */
public record CredencialIa(ProvedorIa provedor, String apiKey) {
}
