package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.MarcadorRepository;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.web.dto.MarcadorDTO;
import com.npl.na_ponta_do_lapis.web.dto.MarcadorResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class MarcadorService {

    private final MarcadorRepository marcadorRepository;
    private final UsuarioService usuarioService;

    public MarcadorService(MarcadorRepository marcadorRepository, UsuarioService usuarioService) {
        this.marcadorRepository = marcadorRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public MarcadorResponseDTO criarMarcador(MarcadorDTO dto) {
        String emailUsuarioLogado =  JwtAuthFilter.getEmailUsuarioLogado();
        Usuario usuarioLogado = usuarioService.buscarUsuarioPorEmail(emailUsuarioLogado);
        Marcador marcador = dto.toEntity();
        marcador.setUsuario(usuarioLogado);
        marcadorRepository.save(marcador);
        return new MarcadorResponseDTO(marcador);
    }

    public List<MarcadorResponseDTO> listarMarcadores() {
        String emailUsuarioLogado =  JwtAuthFilter.getEmailUsuarioLogado();
        return marcadorRepository.findByUsuarioEmail(emailUsuarioLogado)
                .stream()
                .map(MarcadorResponseDTO::new)
                .toList();
    }

    @Transactional
    public MarcadorResponseDTO editarMarcador(Long id, MarcadorDTO dto) {

        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Marcador não encontrado."
                ));

        String emailUsuarioLogado =  JwtAuthFilter.getEmailUsuarioLogado();

        if(!marcador.getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este marcador.");
        }

        if (dto.nome() != null) {
            if (dto.nome().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome não pode ser vazio.");
            marcador.setNome(dto.nome());
        }
        if (dto.cor() != null) {
            if (dto.cor().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cor não pode ser vazia.");
            marcador.setCor(dto.cor());
        }

        marcadorRepository.save(marcador);
        return new MarcadorResponseDTO(marcador);
    }

    @Transactional
    public void excluirMarcador(Long id) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Marcador não encontrado."
                ));

        String emailUsuarioLogado =  JwtAuthFilter.getEmailUsuarioLogado();
        if(!marcador.getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este marcador.");
        }
        marcadorRepository.delete(marcador);
    }

    public MarcadorResponseDTO buscarMarcadorPorId(Long id) {
        return new MarcadorResponseDTO(marcadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Marcador não encontrado."
                )));
    }

    public Marcador buscarMarcadorPorIdObject(Long id) {
        return marcadorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Marcador de ID: " + id + " não existe")
        );
    }
}