package com.npl.na_ponta_do_lapis.service;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;

import com.npl.na_ponta_do_lapis.repository.FamiliaRepository;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;

import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraFamiliaDTO;
import com.npl.na_ponta_do_lapis.web.exception.NaoEAdminFamiliaException;
import com.npl.na_ponta_do_lapis.web.exception.NaoEdaMesmaFamiliaException;
import com.npl.na_ponta_do_lapis.web.exception.UsuarioIdNaoExisteException;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FamiliaService {
    private final FamiliaRepository familiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;
    private final ContaFinanceiraRepository contaFinanceiraRepository;

    public FamiliaService(FamiliaRepository familiaRepository, UsuarioRepository usuarioRepository, ContaFinanceiraRepository contaFinanceiraRepository, TransacaoRepository transacaoRepository, UsuarioService usuarioService) {
        this.familiaRepository = familiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public FamiliaResponseDTO criarFamilia(FamiliaDTO familiaDTO) {
        Familia familia = familiaDTO.toEntity();
        familiaRepository.save(familia);

        Usuario criador = usuarioService.buscarUsuarioLogado();
        criador.setFamilia(familia);
        criador.setPapel(Papel.ADMIN_FAMILIA);
        usuarioRepository.save(criador);

        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public void excluirFamilia(Long id){
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        for (Usuario membro : familia.getMembros()) {
            membro.setFamilia(null);
            usuarioRepository.save(membro);
        }
        familiaRepository.deleteById(id);
    }

    @Transactional
    public FamiliaResponseDTO editarFamilia(Long id, FamiliaDTO familiaDTO){
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        if (familiaDTO.nome() != null){
            if (familiaDTO.nome().isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nome da família não pode ser vazio.");
            }
            familia.setNome(familiaDTO.nome());
        }
        if (familiaDTO.foto() != null){
            familia.setFotoFamilia(familiaDTO.foto());
        }
        familiaRepository.save(familia);
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public FamiliaResponseDTO adicionarUsuarioNaFamilia(String username, Long id_familia){
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
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public FamiliaResponseDTO removerUsuarioNaFamilia(String username, Long id_familia){
        Familia familia = familiaRepository.findById(id_familia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Família não foi encontrada."));
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não foi encontrado.");
        }
        if (usuario.getFamilia() == null || !usuario.getFamilia().equals(familia)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não pertence a essa família.");
        }
        usuario.setFamilia(null);
        usuarioRepository.save(usuario);
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public UsuarioResponseDTO promoverParaAdminFamilia(Long userId, Long solicitanteId) {
        Usuario novoAdmin = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário não encontrado"));

        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Solicitante não encontrado"));

        if (solicitante.getPapel() != Papel.ADMIN_FAMILIA){
            throw new NaoEAdminFamiliaException("Apenas administradores da família podem promover outros membros.");
        }

        if (novoAdmin.getFamilia() == null || !novoAdmin.getFamilia().equals(solicitante.getFamilia())){
            throw new NaoEdaMesmaFamiliaException("Você só pode promover membros da sua própria família.");
        }

        novoAdmin.setPapel(Papel.ADMIN_FAMILIA);
        usuarioRepository.save(novoAdmin);
        return new UsuarioResponseDTO(novoAdmin);
    }

    public List<FamiliaResponseDTO> listarFamilias(){
        return familiaRepository.findAll()
                .stream()
                .map(FamiliaResponseDTO::new)
                .toList();
    }

    public FamiliaResponseDTO buscarFamiliaPorID(Long id){
        return familiaRepository.findById(id)
                .map(FamiliaResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("Família de ID " + id + " não existe!"));
    }

    public List<UsuarioResponseDTO> listarMembrosDaFamilia(Long familiaId) {
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
        return transacaoRepository.buscarTransacoesDaFamilia(familiaId)
                .stream()
                .map(TransacaoFamiliaDTO::new)
                .toList();
    }
    public List<ContaFinanceiraFamiliaDTO> listarContasDaFamilia(Long familiaId) {
        return contaFinanceiraRepository.findByFamilia(familiaId)
                .stream()
                .map(ContaFinanceiraFamiliaDTO::new)
                .toList();
    }
    public List<TransacaoFamiliaDTO> listarTransacoesDaFamiliaPorDescricao(Long familiaId, String descricao) {
        return transacaoRepository.buscarTransacoesDaFamiliaPorDescricao(familiaId, descricao)
                .stream()
                .map(TransacaoFamiliaDTO::new)
                .toList();
    }
}
