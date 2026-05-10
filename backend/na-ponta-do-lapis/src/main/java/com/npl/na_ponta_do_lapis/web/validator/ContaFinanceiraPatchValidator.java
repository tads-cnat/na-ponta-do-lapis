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

        // Validar que pelo menos um campo foi fornecido
        boolean temAlgumCampo = contaPatchDTO.nome().isPresent() || 
                               contaPatchDTO.saldo().isPresent() ||
                               contaPatchDTO.cor().isPresent() ||
                               contaPatchDTO.usuarioId().isPresent();
        
        if (!temAlgumCampo) {
            throw new IllegalArgumentException("Pelo menos um campo deve ser fornecido para atualização parcial");
        }

        if (contaPatchDTO.nome().isPresent()) {
            String nome = contaPatchDTO.nome().get();
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome da conta não pode ser vazio");
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
        }

        if (contaPatchDTO.cor().isPresent()) {
            String cor = contaPatchDTO.cor().get();
            if (cor == null || cor.trim().isEmpty()) {
                throw new IllegalArgumentException("A cor não pode ser vazia");
            }
            if (cor.length() > 7) {
                throw new IllegalArgumentException("A não pode ter mais de 7 caracteres");
            }
        }

        // Campo usuarioId é opcional e não requer validação adicional aqui
        // - usuarioId: validação de existência deve ser feita no service/repository
        // para evitar consultas extras no validator
    }
}
