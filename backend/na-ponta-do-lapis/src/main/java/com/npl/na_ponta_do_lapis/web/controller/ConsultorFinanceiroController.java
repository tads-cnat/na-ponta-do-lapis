package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import com.npl.na_ponta_do_lapis.entity.ConsultorFeedback;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.ConsultorFeedbackRepository;
import com.npl.na_ponta_do_lapis.service.ChaveIaService;
import com.npl.na_ponta_do_lapis.service.ConquistaService;
import com.npl.na_ponta_do_lapis.service.ConsultorFinanceiroService;
import com.npl.na_ponta_do_lapis.service.fatura.CredencialIa;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaSessionCookieHelper;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import com.npl.na_ponta_do_lapis.service.security.ApiRateLimitService;
import com.npl.na_ponta_do_lapis.web.dto.ConsultorFeedbackRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.ConsultorRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.ConsultorResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.FaturaErroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/consultor")
@Tag(name = "Consultor Financeiro IA", description = "Assistente financeiro somente leitura baseado nos dados do usuário")
public class ConsultorFinanceiroController {

    private final ConsultorFinanceiroService consultorService;
    private final ChaveIaService chaveIaService;
    private final FaturaSessionCookieHelper cookieHelper;
    private final ApiRateLimitService apiRateLimitService;
    private final ConquistaService conquistaService;
    private final ConsultorFeedbackRepository feedbackRepository;

    @Value("${app.consultor.ia.provedor:GEMINI}")
    private String fallbackProvedor;

    @Value("${app.consultor.ia.api-key:}")
    private String fallbackApiKey;

    public ConsultorFinanceiroController(
            ConsultorFinanceiroService consultorService,
            ChaveIaService chaveIaService,
            FaturaSessionCookieHelper cookieHelper,
            ApiRateLimitService apiRateLimitService,
            ConquistaService conquistaService,
            ConsultorFeedbackRepository feedbackRepository) {
        this.consultorService = consultorService;
        this.chaveIaService = chaveIaService;
        this.cookieHelper = cookieHelper;
        this.apiRateLimitService = apiRateLimitService;
        this.conquistaService = conquistaService;
        this.feedbackRepository = feedbackRepository;
    }

    @Operation(summary = "Verificar disponibilidade de chave de IA para o consultor")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(
            @AuthenticationPrincipal Usuario usuario,
            HttpServletRequest request) {
        Optional<ChaveIaUsuario> padrao = usuario != null ? chaveIaService.obterChavePadrao(usuario) : Optional.empty();
        boolean temChavePlataforma = fallbackApiKey != null && !fallbackApiKey.isBlank();

        return ResponseEntity.ok(Map.of(
                "pronto", padrao.isPresent() || temChavePlataforma,
                "temChaveUsuario", padrao.isPresent(),
                "temSessao", false,
                "temChavePlataforma", temChavePlataforma,
                "provedorPadrao", padrao.map(c -> c.getProvedor().name()).orElse(fallbackProvedor)
        ));
    }

    @Operation(summary = "Enviar pergunta ao consultor financeiro IA")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @PostMapping("/perguntar")
    public ResponseEntity<?> perguntar(
            @Valid @RequestBody ConsultorRequestDTO body,
            @AuthenticationPrincipal Usuario usuario,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado."));
        }

        apiRateLimitService.enforce(
                "consultor:perguntar",
                usuario.getEmail(),
                apiRateLimitService.resolveClientIp(request),
                15,
                Duration.ofMinutes(1));

        try {
            CredencialResolvida credencial = resolverCredencial(body, usuario);
            String resposta = consultorService.responder(usuario, credencial.provedor(), credencial.apiKey(), body);

            conquistaService.desbloquear(usuario, "GRAFITH_CONSULTOR");

            return ResponseEntity.ok(new ConsultorResponseDTO(
                    resposta,
                    credencial.provedor().getNomeExibicao(),
                    credencial.usouPlataforma()));
        } catch (FaturaAnaliseException ex) {
            return ResponseEntity.status(ex.getStatus())
                    .body(new FaturaErroDTO(ex.getMessage(), ex.getCodigo(), ex.getProvedores()));
        }
    }

    @Operation(summary = "Registrar avaliação (like/dislike) de uma resposta do Grafith IA")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> registrarFeedback(
            @Valid @RequestBody ConsultorFeedbackRequestDTO body,
            @AuthenticationPrincipal Usuario usuario,
            HttpServletRequest request) {

        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado."));
        }

        apiRateLimitService.enforce(
                "consultor:feedback",
                usuario.getEmail(),
                apiRateLimitService.resolveClientIp(request),
                30,
                Duration.ofMinutes(1));

        ConsultorFeedback feedback = null;
        if (body.feedbackId() != null) {
            feedback = feedbackRepository.findById(body.feedbackId())
                    .filter(f -> f.getUsuario().getId().equals(usuario.getId()))
                    .orElse(null);
        }

        if (feedback == null) {
            feedback = new ConsultorFeedback();
            feedback.setUsuario(usuario);
            feedback.setProvedor(truncar(body.provedor(), 40));
            feedback.setPerguntaResumo(truncar(body.perguntaResumo(), 250));
            feedback.setRespostaResumo(truncar(body.respostaResumo(), 400));
        }

        feedback.setUtil(Boolean.TRUE.equals(body.util()));
        ConsultorFeedback salvo = feedbackRepository.save(feedback);

        return ResponseEntity.ok(Map.of(
                "id", salvo.getId(),
                "util", salvo.isUtil()
        ));
    }

    private String truncar(String texto, int max) {
        if (texto == null) return null;
        String t = texto.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private CredencialResolvida resolverCredencial(ConsultorRequestDTO body, Usuario usuario) {
        if (body.chaveIaId() != null) {
            ChaveIaUsuario entidade = chaveIaService.obterEntidade(usuario, body.chaveIaId());
            String decifrada = chaveIaService.obterChaveDecifrada(usuario, body.chaveIaId());
            return new CredencialResolvida(entidade.getProvedor(), decifrada, false);
        }

        Optional<ChaveIaUsuario> padrao = chaveIaService.obterChavePadrao(usuario);
        if (padrao.isPresent()) {
            String decifrada = chaveIaService.obterChaveDecifrada(usuario, padrao.get().getId());
            return new CredencialResolvida(padrao.get().getProvedor(), decifrada, false);
        }

        if (fallbackApiKey != null && !fallbackApiKey.isBlank()) {
            ProvedorIa provedor = parseProvedor(fallbackProvedor, ProvedorIa.GEMINI);
            return new CredencialResolvida(provedor, fallbackApiKey.trim(), true);
        }

        throw FaturaAnaliseException.chaveIaConsultorNecessaria();
    }

    private ProvedorIa parseProvedor(String valor, ProvedorIa defaultValue) {
        if (valor == null || valor.isBlank()) {
            return defaultValue;
        }
        try {
            return ProvedorIa.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Provedor de IA inválido: " + valor);
        }
    }

    private record CredencialResolvida(ProvedorIa provedor, String apiKey, boolean usouPlataforma) {
    }
}
