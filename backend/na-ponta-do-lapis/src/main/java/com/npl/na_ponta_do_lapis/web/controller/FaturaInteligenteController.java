package com.npl.na_ponta_do_lapis.web.controller;

import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.npl.na_ponta_do_lapis.service.fatura.CredencialIa;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseService;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaSessionCookieHelper;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import com.npl.na_ponta_do_lapis.web.dto.FaturaAnaliseResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.FaturaErroDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/faturas")
@Tag(name = "Faturas", description = "Análise inteligente de faturas de cartão de crédito")
public class FaturaInteligenteController {

    private final FaturaAnaliseService faturaAnaliseService;
    private final FaturaSessionCookieHelper cookieHelper;

    public FaturaInteligenteController(FaturaAnaliseService faturaAnaliseService,
                                       FaturaSessionCookieHelper cookieHelper) {
        this.faturaAnaliseService = faturaAnaliseService;
        this.cookieHelper = cookieHelper;
    }

    @Operation(summary = "Extrair transações de uma fatura PDF",
            description = "Tenta regex local primeiro. Se necessário, usa IA com a chave do usuário. "
                    + "PDFs escaneados usam OCR local e, com consentimento, envio multimodal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transações extraídas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato não suportado"),
            @ApiResponse(responseCode = "413", description = "Arquivo acima do limite"),
            @ApiResponse(responseCode = "422", description = "Chave inválida, PDF sem texto ou resposta inválida"),
            @ApiResponse(responseCode = "428", description = "É necessário informar uma chave de IA"),
            @ApiResponse(responseCode = "429", description = "Limite de uso do provedor de IA"),
            @ApiResponse(responseCode = "502", description = "Resposta inválida do provedor"),
            @ApiResponse(responseCode = "503", description = "Provedor indisponível"),
            @ApiResponse(responseCode = "504", description = "Timeout do provedor"),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Erro interno\"}")))
    })
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping(value = "/analise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analisar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "provedor", required = false) String provedor,
            @RequestParam(value = "apiKey", required = false) String apiKey,
            @RequestParam(value = "manterNaSessao", defaultValue = "false") boolean manterNaSessao,
            @RequestParam(value = "forceAiAnalysis", defaultValue = "false") boolean forceAiAnalysis,
            @RequestParam(value = "enviarPdfParaIa", defaultValue = "false") boolean enviarPdfParaIa,
            @RequestParam(value = "consentimentoLgpd", defaultValue = "false") boolean consentimentoLgpd,
            HttpServletRequest request,
            HttpServletResponse response) {

        Optional<CredencialIa> credencial = cookieHelper.ler(request);

        try {
            ProvedorIa provedorRequisicao = parseProvedor(provedor);
            boolean chaveInformada = apiKey != null && !apiKey.isBlank();

            // Para envio multimodal, a chave da sessão só serve se for do mesmo provedor
            // selecionado; caso contrário o usuário precisa informar uma chave compatível.
            boolean podeUsarCredencialDaSessao = credencial.isPresent()
                    && (!enviarPdfParaIa
                        || provedorRequisicao == null
                        || credencial.get().provedor() == provedorRequisicao);

            ProvedorIa provedorEfetivo;
            String chaveEfetiva;
            boolean chaveVeioDoCookie = false;

            if (chaveInformada) {
                provedorEfetivo = provedorRequisicao != null
                        ? provedorRequisicao
                        : credencial.map(CredencialIa::provedor).orElse(ProvedorIa.GEMINI);
                chaveEfetiva = apiKey;
            } else if (podeUsarCredencialDaSessao) {
                provedorEfetivo = credencial.get().provedor();
                chaveEfetiva = credencial.get().apiKey();
                chaveVeioDoCookie = true;
            } else {
                provedorEfetivo = provedorRequisicao != null ? provedorRequisicao : ProvedorIa.GEMINI;
                chaveEfetiva = null;
            }

            FaturaAnaliseResponseDTO resposta = faturaAnaliseService.analisar(
                    arquivo,
                    provedorEfetivo,
                    chaveEfetiva,
                    forceAiAnalysis,
                    enviarPdfParaIa,
                    consentimentoLgpd
            );

            if (chaveEfetiva != null && !chaveEfetiva.isBlank() && (manterNaSessao || chaveVeioDoCookie)) {
                cookieHelper.gravar(response, request, provedorEfetivo, chaveEfetiva);
            }

            return ResponseEntity.ok(resposta);
        } catch (FaturaAnaliseException e) {
            if (e.isChaveInvalida()) {
                cookieHelper.limpar(response, request);
            }
            return ResponseEntity.status(e.getStatus())
                    .body(new FaturaErroDTO(e.getMessage(), e.getCodigo(), e.getProvedores()));
        }
    }

    @Operation(summary = "Esquecer a chave de IA guardada no cookie de sessão")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @DeleteMapping("/sessao")
    public ResponseEntity<Void> esquecerSessao(HttpServletRequest request, HttpServletResponse response) {
        cookieHelper.limpar(response, request);
        return ResponseEntity.noContent().build();
    }

    private ProvedorIa parseProvedor(String provedor) {
        if (provedor == null || provedor.isBlank()) {
            return null;
        }
        try {
            return ProvedorIa.valueOf(provedor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw FaturaAnaliseException.arquivoInvalido("Provedor de IA inválido.");
        }
    }
}
