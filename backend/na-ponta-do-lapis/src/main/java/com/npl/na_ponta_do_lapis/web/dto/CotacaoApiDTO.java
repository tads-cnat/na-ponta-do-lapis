package com.npl.na_ponta_do_lapis.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


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