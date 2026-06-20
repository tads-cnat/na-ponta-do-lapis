package com.npl.na_ponta_do_lapis.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Espelha exatamente o JSON retornado pela AwesomeAPI no endpoint de
 * cotação diária (GET /json/daily/{moeda}/{dias}).
 *
 * É convertido para a Entity Cotacao dentro do CotacaoService — nunca
 * é exposto diretamente pelo Controller.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CotacaoApiDTO(
        String code,
        String codein,
        String name,
        String high,
        String low,
        String bid,
        String ask,
        String pctChange,
        @JsonProperty("create_date") String createDate
) {}