package com.npl.na_ponta_do_lapis.web.dto;

import java.util.List;

/** Corpo de erro da análise de faturas. */
public record FaturaErroDTO(String error, String codigo, List<String> provedores) {
}
