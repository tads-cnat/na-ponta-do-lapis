package com.npl.na_ponta_do_lapis.service;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;

import com.npl.na_ponta_do_lapis.repository.FamiliaRepository;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;

import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheService;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraFamiliaDTO;
import com.npl.na_ponta_do_lapis.web.exception.BusinessException;
import com.npl.na_ponta_do_lapis.web.exception.NaoEAdminFamiliaException;
import com.npl.na_ponta_do_lapis.web.exception.NaoEdaMesmaFamiliaException;
import com.npl.na_ponta_do_lapis.web.exception.NotFoundException;
import com.npl.na_ponta_do_lapis.web.exception.UsuarioIdNaoExisteException;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class FamiliaService {
    private final FamiliaRepository familiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;
    private final ContaFinanceiraRepository contaFinanceiraRepository;
    private final UsuarioAuthCacheService usuarioAuthCacheService;

    public FamiliaService(
            FamiliaRepository familiaRepository,
            UsuarioRepository usuarioRepository,
            ContaFinanceiraRepository contaFinanceiraRepository,
            TransacaoRepository transacaoRepository,
            UsuarioService usuarioService,
            UsuarioAuthCacheService usuarioAuthCacheService
    ) {
        this.familiaRepository = familiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioService = usuarioService;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
    }

    @Transactional
    public FamiliaResponseDTO criarFamilia(FamiliaDTO familiaDTO) {
        Usuario criador = usuarioService.buscarUsuarioLogado();
        if (criador.getFamilia() != null) {
            throw new BusinessException("Você já pertence a uma família. Saia dela antes de criar outra.");
        }

        Familia familia = familiaDTO.toEntity();
        familiaRepository.save(familia);

        criador.setFamilia(familia);
        criador.setAdminFamilia(true);
        usuarioRepository.save(criador);
        usuarioAuthCacheService.evictPorUsuario(criador);

        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public void excluirFamilia(Long id){
        validarAdminDaFamiliaOuAdminSite(id);
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        for (Usuario membro : familia.getMembros()) {
            membro.setFamilia(null);
            membro.setAdminFamilia(false);
            usuarioRepository.save(membro);
            usuarioAuthCacheService.evictPorUsuario(membro);
        }
        familiaRepository.deleteById(id);
    }

    @Transactional
    public FamiliaResponseDTO editarFamilia(Long id, FamiliaDTO familiaDTO){
        validarAdminDaFamiliaOuAdminSite(id);
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        if (familiaDTO.nome() != null){
            if (familiaDTO.nome().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da família não pode ser vazio.");
            }
            familia.setNome(familiaDTO.nome());
        }
        if (familiaDTO.foto() != null){
            familia.setFotoFamilia(familiaDTO.foto());
        }
        familiaRepository.save(familia);
        if (familia.getMembros() != null) {
            familia.getMembros().forEach(usuarioAuthCacheService::evictPorUsuario);
        }
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public FamiliaResponseDTO adicionarUsuarioNaFamilia(String username, Long id_familia){
        validarAdminDaFamiliaOuAdminSite(id_familia);
        Familia familia = familiaRepository.findById(id_familia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não foi encontrado.");
        }
        if (usuario.getFamilia() != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já pertence a uma família.");
        }
        usuario.setFamilia(familia);
        usuarioRepository.save(usuario);
        usuarioAuthCacheService.evictPorUsuario(usuario);
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public FamiliaResponseDTO removerUsuarioNaFamilia(String username, Long id_familia){
        validarAdminDaFamiliaOuAdminSite(id_familia);
        Familia familia = familiaRepository.findById(id_familia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não foi encontrado.");
        }
        if (usuario.getFamilia() == null || !usuario.getFamilia().equals(familia)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não pertence a essa família.");
        }
        boolean eraAdmin = usuario.isAdminFamilia();
        usuario.setFamilia(null);
        usuario.setAdminFamilia(false);
        usuarioRepository.save(usuario);
        usuarioAuthCacheService.evictPorUsuario(usuario);

        if (eraAdmin && familia.getMembros() != null) {
            List<Usuario> restantes = familia.getMembros().stream()
                    .filter(m -> !Objects.equals(m.getId(), usuario.getId()))
                    .toList();
            boolean temOutroAdmin = restantes.stream().anyMatch(Usuario::isAdminFamilia);
            if (!temOutroAdmin && !restantes.isEmpty()) {
                Usuario sucessor = restantes.stream()
                        .min(Comparator.comparing(Usuario::getId))
                        .orElse(restantes.get(0));
                sucessor.setAdminFamilia(true);
                usuarioRepository.save(sucessor);
                usuarioAuthCacheService.evictPorUsuario(sucessor);
            }
        }
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public UsuarioResponseDTO promoverParaAdminFamilia(Long userId, Long solicitanteId) {
        Usuario novoAdmin = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário não encontrado"));

        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Solicitante não encontrado"));

        if (!solicitante.isAdminFamilia()){
            throw new NaoEAdminFamiliaException("Apenas administradores da família podem promover outros membros.");
        }

        if (novoAdmin.getFamilia() == null || !novoAdmin.getFamilia().equals(solicitante.getFamilia())){
            throw new NaoEdaMesmaFamiliaException("Você só pode promover membros da sua própria família.");
        }

        novoAdmin.setAdminFamilia(true);
        usuarioRepository.save(novoAdmin);
        usuarioAuthCacheService.evictPorUsuario(novoAdmin);
        return new UsuarioResponseDTO(novoAdmin);
    }

    public List<FamiliaResponseDTO> listarFamilias(){
        return familiaRepository.findAll()
                .stream()
                .map(FamiliaResponseDTO::new)
                .toList();
    }

    public FamiliaResponseDTO buscarFamiliaPorID(Long id){
        validarMembroOuAdminSite(id);
        return familiaRepository.findById(id)
                .map(FamiliaResponseDTO::new)
                .orElseThrow(() -> new NotFoundException("Família de ID " + id + " não existe!"));
    }

    public List<UsuarioResponseDTO> listarMembrosDaFamilia(Long familiaId) {
        validarMembroOuAdminSite(familiaId);
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));

        if (familia.getMembros() == null) {
            return List.of();
        }

        return familia.getMembros().stream()
                .map(UsuarioResponseDTO::new)
                .toList();
    }
    public List<TransacaoFamiliaDTO> listarTransacoesDaFamilia(Long familiaId) {
        validarMembroOuAdminSite(familiaId);
        return transacaoRepository.buscarTransacoesDaFamilia(familiaId)
                .stream()
                .map(TransacaoFamiliaDTO::new)
                .toList();
    }
    public List<ContaFinanceiraFamiliaDTO> listarContasDaFamilia(Long familiaId) {
        validarMembroOuAdminSite(familiaId);
        return contaFinanceiraRepository.findByFamilia(familiaId)
                .stream()
                .map(ContaFinanceiraFamiliaDTO::new)
                .toList();
    }
    public List<TransacaoFamiliaDTO> listarTransacoesDaFamiliaPorDescricao(Long familiaId, String descricao) {
        validarMembroOuAdminSite(familiaId);
        return transacaoRepository.buscarTransacoesDaFamiliaPorDescricao(familiaId, descricao)
                .stream()
                .map(TransacaoFamiliaDTO::new)
                .toList();
    }

    private void validarMembroOuAdminSite(Long familiaId) {
        Usuario logado = tentarObterUsuarioLogado();
        if (logado == null || logado.getPapel() == Papel.ADMIN_SITE) {
            return;
        }
        Long minhaFamiliaId = logado.getFamilia() != null ? logado.getFamilia().getId() : null;
        if (!Objects.equals(minhaFamiliaId, familiaId)) {
            throw new AccessDeniedException("Você não pertence a esta família.");
        }
    }

    private void validarAdminDaFamiliaOuAdminSite(Long familiaId) {
        Usuario logado = tentarObterUsuarioLogado();
        if (logado == null || logado.getPapel() == Papel.ADMIN_SITE) {
            return;
        }
        Long minhaFamiliaId = logado.getFamilia() != null ? logado.getFamilia().getId() : null;
        if (!Objects.equals(minhaFamiliaId, familiaId) || !logado.isAdminFamilia()) {
            throw new AccessDeniedException("Apenas administradores desta família podem realizar esta ação.");
        }
    }

    private Usuario tentarObterUsuarioLogado() {
        try {
            return usuarioService.buscarUsuarioLogado();
        } catch (Exception e) {
            return null;
        }
    }
}
