package com.npl.na_ponta_do_lapis.web.dto.admin;

import com.npl.na_ponta_do_lapis.entity.enums.CategoriaConquista;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminDTOs {

    private AdminDTOs() {}

    public record SerieDiariaDTO(String data, long total) {}

    public record AdminDashboardResumoDTO(
            long totalUsuarios,
            long usuariosAtivos,
            long usuariosSuspensos,
            long ativosVerificados,
            long ativosNaoVerificados,
            long novosUltimos7Dias,
            long novosUltimos30Dias,
            long emailsVerificados,
            long usuariosAtivados,
            long totalFamilias,
            long usuariosEmFamilia,
            long convitesPendentes,
            long usuariosComIa,
            long usuariosComOpenFinance,
            long totalTransacoes,
            long totalMetas,
            // Métricas-Norte (KPIs de Produto)
            double taxaAtivacao24h,
            long usuariosAtivados24h,
            long usuariosBaseAtivacao24h,
            double indiceAutomacao,
            long transacoesManuais,
            long transacoesFaturaPdf,
            long transacoesOpenFinance,
            double fatorViralFamiliar,
            double retencaoSegundoMes,
            long usuariosRetidosM2,
            long usuariosElegiveisM2,
            double retencaoD7,
            long usuariosRetidosD7,
            long usuariosElegiveisD7,
            long usuariosRiscoChurn,
            long funilComConta,
            // KPIs Complementares
            double taxaConclusaoMetas,
            long metasConcluidas,
            double stickinessWauMau,
            long wau,
            long mau,
            // Série temporal parametrizável (7d / 30d / 90d)
            int periodoDias,
            long novosNoPeriodo,
            long novosPeriodoAnterior,
            List<SerieDiariaDTO> serieCadastros
    ) {}

    public record AdminUsuarioResumoDTO(
            Long id,
            String nome,
            String email,
            String username,
            Papel papel,
            boolean ativo,
            boolean emailVerificado,
            boolean onboardingConcluido,
            Moeda moedaPadrao,
            String familiaNome,
            LocalDateTime criadoEm,
            LocalDateTime ultimoAcessoEm
    ) {}

    public record AdminUsuarioDetalheDTO(
            Long id,
            String nome,
            String email,
            String username,
            String telefone,
            String fotoPerfil,
            Papel papel,
            boolean ativo,
            LocalDateTime suspensoEm,
            String motivoSuspensao,
            boolean emailVerificado,
            boolean onboardingConcluido,
            Moeda moedaPadrao,
            String versaoTermos,
            LocalDateTime termosAceitosEm,
            LocalDateTime criadoEm,
            LocalDateTime ultimoAcessoEm,
            Long familiaId,
            String familiaNome,
            long totalContas,
            long totalTransacoes,
            long totalMetas,
            long totalConquistas,
            boolean possuiChaveIa,
            boolean possuiOpenFinance
    ) {}

    public record AdminSuspensaoRequestDTO(
            boolean ativo,
            @Size(max = 300) String motivo
    ) {}

    public record AdminAlterarPapelRequestDTO(
            @NotNull Papel papel,
            @Size(max = 300) String motivo
    ) {}

    public record AdminExcluirUsuarioRequestDTO(
            @NotBlank String confirmacaoEmail,
            @NotBlank @Size(max = 300) String motivo
    ) {}

    public record AdminFamiliaResumoDTO(
            Long id,
            String nome,
            long totalMembros,
            String adminFamiliaNome,
            String adminFamiliaEmail,
            long convitesPendentes
    ) {}

    public record AdminCategoriaResumoDTO(
            Long id,
            String nome,
            long totalTransacoes
    ) {}

    public record AdminCategoriaRequestDTO(
            @NotBlank @Size(min = 2, max = 100) String nome
    ) {}

    public record AdminConquistaResumoDTO(
            String id,
            String titulo,
            String descricao,
            String icone,
            Integer pontosXp,
            CategoriaConquista categoria,
            long totalDesbloqueios
    ) {}

    public record AdminConquistaRequestDTO(
            @Size(max = 50) String id,
            @NotBlank @Size(max = 100) String titulo,
            @NotBlank @Size(max = 255) String descricao,
            @NotBlank @Size(max = 100) String icone,
            @NotNull @Min(0) Integer pontosXp,
            @NotNull CategoriaConquista categoria
    ) {}

    public record AdminNotificacaoRequestDTO(
            @NotBlank @Size(max = 120) String titulo,
            @NotBlank @Size(max = 500) String mensagem,
            @Size(max = 200) String linkRedirecionamento,
            Long usuarioId
    ) {}

    public record AdminNotificacaoResultadoDTO(
            int destinatarios,
            boolean global
    ) {}

    public record AdminAuditLogDTO(
            Long id,
            Long adminUsuarioId,
            String adminEmail,
            String acao,
            String entidadeAlvo,
            String alvoId,
            String alvoDescricao,
            String motivo,
            String detalhesJson,
            String ipOrigem,
            LocalDateTime criadoEm
    ) {}

    public record AdminPageResponseDTO<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
