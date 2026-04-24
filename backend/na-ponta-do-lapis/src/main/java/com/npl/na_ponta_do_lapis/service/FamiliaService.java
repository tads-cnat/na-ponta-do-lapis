package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.repository.ConviteRepository;
import com.npl.na_ponta_do_lapis.repository.FamiliaRepository;

import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FamiliaService {
    private final FamiliaRepository familiaRepository;
    private final UsuarioRepository usuarioRepository;

    public FamiliaService(FamiliaRepository familiaRepository, UsuarioRepository usuarioRepository) {
        this.familiaRepository = familiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public FamiliaResponseDTO criarFamilia(FamiliaDTO familiaDTO) {
        Familia familia = familiaDTO.toEntity();
        familiaRepository.save(familia);
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public void excluirFamilia(Long id){
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
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não foi encontrado."));
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
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não foi encontrado."));
        if (usuario.getFamilia() == null || !usuario.getFamilia().equals(familia)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não pertence a essa família.");
        }
        usuario.setFamilia(null);
        usuarioRepository.save(usuario);
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public UsuarioResponseDTO promoverParaAdmin(Long userId, Usuario solicitante) {

        if (solicitante.getPapel() != Papel.ADMIN_FAMILIA) {
            throw new RuntimeException("Apenas admins podem promover");
        }

        Usuario novo_admin = usuarioRepository.findById(userId).orElseThrow();

        if (novo_admin.getFamilia() == null ||
                !novo_admin.getFamilia().equals(solicitante.getFamilia())) {
            throw new RuntimeException("Não pertence à mesma família");
        }

        novo_admin.setPapel(Papel.ADMIN_FAMILIA);
        usuarioRepository.save(novo_admin);
        return new UsuarioResponseDTO(novo_admin);
    }

    public List<FamiliaResponseDTO> listarFamilias(){
        return familiaRepository.findAll()
                .stream()
                .map(familia -> new FamiliaResponseDTO(familia))
                .toList();
    }

    public FamiliaResponseDTO buscarFamiliaPorID(Long id){
        return familiaRepository.findById(id)
                .map(familia -> new FamiliaResponseDTO(familia))
                .orElseThrow(() -> new RuntimeException("Família de ID " + id + " não existe!"));
    }

}
