package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.MarcadorRepository;
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

    public MarcadorService(MarcadorRepository marcadorRepository) {
        this.marcadorRepository = marcadorRepository;
    }

    @Transactional
    public MarcadorResponseDTO criarMarcador(MarcadorDTO dto, Usuario usuario) {
        Marcador marcador = dto.toEntity();
        marcador.setUsuario(usuario);
        marcadorRepository.save(marcador);
        return new MarcadorResponseDTO(marcador);
    }

    public List<MarcadorResponseDTO> listarMarcadores(Usuario usuario) {
        return marcadorRepository.findByUsuario(usuario)
                .stream()
                .map(MarcadorResponseDTO::new)
                .toList();
    }

    @Transactional
    public MarcadorResponseDTO editarMarcador(Long id, MarcadorDTO dto, Usuario usuario) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Marcador não encontrado."
                ));

        // Fix IDOR — verifica se o marcador pertence ao usuário
        if (!marcador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão.");
        }

        if (dto.nome() != null) {
            if (dto.nome().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome não pode ser vazio.");
            marcador.setNome(dto.nome());
        }
        if (dto.cor() != null) {
            if (dto.cor().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cor não pode ser vazia.")
            marcador.setCor(dto.cor());
        }

        marcadorRepository.save(marcador);
        return new MarcadorResponseDTO(marcador);
    }

    @Transactional
    public void excluirMarcador(Long id, Usuario usuario) {
        Marcador marcador = marcadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Marcador não encontrado."
                ));

        // Fix IDOR — verifica se o marcador pertence ao usuário
        if (!marcador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão.");
        }

        marcadorRepository.delete(marcador);
    }
}