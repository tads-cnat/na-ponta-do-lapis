package com.npl.na_ponta_do_lapis.web.controller;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.ChaveIaService;
import com.npl.na_ponta_do_lapis.service.security.ApiRateLimitService;
import com.npl.na_ponta_do_lapis.service.fatura.CredencialIa;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseService;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaSessionCookieHelper;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.FaturaAnaliseResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.FaturaErroDTO;
import com.npl.na_ponta_do_lapis.web.dto.FaturaSessaoDTO;
import com.npl.na_ponta_do_lapis.web.dto.FaturaValidacaoPreviaDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
    private final ChaveIaService chaveIaService;
    private final ApiRateLimitService apiRateLimitService;

    public FaturaInteligenteController(FaturaAnaliseService faturaAnaliseService,
                                       FaturaSessionCookieHelper cookieHelper,
                                       ChaveIaService chaveIaService,
                                       ApiRateLimitService apiRateLimitService) {
        this.faturaAnaliseService = faturaAnaliseService;
        this.cookieHelper = cookieHelper;
        this.chaveIaService = chaveIaService;
        this.apiRateLimitService = apiRateLimitService;
    }

    @Operation(summary = "Extrair transações de uma fatura PDF",
            description = "Com usarIa=true, a leitura é feita pela IA usando a chave do usuário. "
                    + "Com usarIa=false, usa apenas regex/OCR local e pede para ativar a IA se não conseguir ler. "
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
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @PostMapping(value = "/analise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analisar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "provedor", required = false) String provedor,
            @RequestParam(value = "apiKey", required = false) String apiKey,
            @RequestParam(value = "chaveIaId", required = false) Long chaveIaId,
            @RequestParam(value = "salvarChave", defaultValue = "false") boolean salvarChave,
            @RequestParam(value = "apelidoChave", required = false) String apelidoChave,
            @RequestParam(value = "manterNaSessao", defaultValue = "false") boolean manterNaSessao,
            @RequestParam(value = "usarIa", defaultValue = "false") boolean usarIa,
            @RequestParam(value = "enviarPdfParaIa", defaultValue = "false") boolean enviarPdfParaIa,
            @RequestParam(value = "consentimentoLgpd", defaultValue = "false") boolean consentimentoLgpd,
            @RequestParam(value = "senha", required = false) String senha,
            @AuthenticationPrincipal Usuario usuario,
            HttpServletRequest request,
            HttpServletResponse response) {

        apiRateLimitService.enforce(
                "faturas:analise",
                usuario != null ? usuario.getEmail() : "anon",
                apiRateLimitService.resolveClientIp(request),
                10,
                Duration.ofMinutes(1));

        try {
            ProvedorIa provedorRequisicao = parseProvedor(provedor);
            ProvedorIa provedorEfetivo;
            String chaveEfetiva;

            if (chaveIaId != null && usuario != null) {
                ChaveIaUsuario chaveSalva = chaveIaService.obterEntidade(usuario, chaveIaId);
                provedorEfetivo = chaveSalva.getProvedor();
                chaveEfetiva = chaveIaService.obterChaveDecifrada(usuario, chaveIaId);
            } else {
                Optional<ChaveIaUsuario> chavePadrao = usuario != null ? chaveIaService.obterChavePadrao(usuario) : Optional.empty();
                if (chavePadrao.isPresent()) {
                    provedorEfetivo = chavePadrao.get().getProvedor();
                    chaveEfetiva = chaveIaService.obterChaveDecifrada(usuario, chavePadrao.get().getId());
                } else {
                    provedorEfetivo = provedorRequisicao != null ? provedorRequisicao : ProvedorIa.GEMINI;
                    chaveEfetiva = null;
                }
            }

            FaturaAnaliseResponseDTO resposta = faturaAnaliseService.analisar(
                    arquivo,
                    provedorEfetivo,
                    chaveEfetiva,
                    senha,
                    usarIa,
                    enviarPdfParaIa,
                    consentimentoLgpd
            );

            return ResponseEntity.ok(resposta);
        } catch (FaturaAnaliseException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(new FaturaErroDTO(e.getMessage(), e.getCodigo(), e.getProvedores()));
        }
    }

    @Operation(summary = "Verifica se há chave de IA cadastrada no perfil")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @GetMapping("/sessao")
    public ResponseEntity<FaturaSessaoDTO> obterSessao(
            HttpServletRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            Optional<ChaveIaUsuario> chavePadrao = chaveIaService.obterChavePadrao(usuario);
            if (chavePadrao.isPresent()) {
                return ResponseEntity.ok(new FaturaSessaoDTO(true, chavePadrao.get().getProvedor().name()));
            }
        }
        return ResponseEntity.ok(new FaturaSessaoDTO(false, null));
    }

    @Operation(summary = "Esquecer a chave de IA guardada no cookie de sessão")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/sessao")
    public ResponseEntity<Void> esquecerSessao(HttpServletRequest request, HttpServletResponse response) {
        cookieHelper.limpar(response, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validação prévia local do arquivo de fatura",
            description = "Verifica em milissegundos se o PDF possui características de fatura e se tem senha, sem usar IA.")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_SITE')")
    @PostMapping(value = "/validar-previa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FaturaValidacaoPreviaDTO> validarPrevia(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "senha", required = false) String senha) {
        FaturaValidacaoPreviaDTO resultado = faturaAnaliseService.validarArquivoPrevia(arquivo, senha);
        return ResponseEntity.ok(resultado);
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
