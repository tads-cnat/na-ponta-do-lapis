package com.npl.na_ponta_do_lapis.web.validator;

import org.springframework.stereotype.Component;

import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraPatchDTO;

/**
 * Validador customizado para ContaFinanceiraPatchDTO.
 * Valida campos opcionais quando são fornecidos.
 */
@Component
public class ContaFinanceiraPatchValidator {

    /**
     * Valida os campos do DTO de forma customizada.
     * 
     * @param contaPatchDTO o DTO a validar
     * @throws IllegalArgumentException se alguma validação falhar
     */
    public void validate(ContaFinanceiraPatchDTO contaPatchDTO) {
        if (contaPatchDTO == null) {
            return;
        }

        if (contaPatchDTO.nome().isPresent()) {
            String nome = contaPatchDTO.nome().get();
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome da conta não pode ser em branco");
            }
            if (nome.length() > 100) {
                throw new IllegalArgumentException("O nome da conta não pode ter mais de 100 caracteres");
            }
        }

        if (contaPatchDTO.saldo().isPresent()) {
            var saldo = contaPatchDTO.saldo().get();
            if (saldo == null) {
                throw new IllegalArgumentException("O saldo não pode ser nulo");
            }
            if (saldo.signum() < 0) {
                throw new IllegalArgumentException("O saldo não pode ser negativo");
            }
        }

        // Campos tipo e usuario são opcionais e não requerem validação adicional aqui
        // - tipo: validação de enum é feita automaticamente pela serialização JSON
        // - usuario: validação de existência deve ser feita no service/repository
        // para evitar consultas extras no validator
    }
}
