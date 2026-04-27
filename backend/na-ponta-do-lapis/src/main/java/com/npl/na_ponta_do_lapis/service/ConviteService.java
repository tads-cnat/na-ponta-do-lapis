package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.repository.ConviteRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.dto.ConviteDTO;
import com.npl.na_ponta_do_lapis.web.dto.ConviteResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final UsuarioRepository usuarioRepository;

    public ConviteService(ConviteRepository conviteRepository, UsuarioRepository usuarioRepository) {
        this.conviteRepository = conviteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ConviteResponseDTO enviarConvite(ConviteDTO conviteDTO, Usuario solicitante) {
        if (solicitante.getPapel() != Papel.ADMIN_FAMILIA) {
            throw new RuntimeException("Apenas admins podem enviar convite");
        }

        if (solicitante.getFamilia() == null) {
            throw new RuntimeException("Admin sem família?");
        }

        if (conviteDTO == null || conviteDTO.destinatarioUsername() == null || conviteDTO.destinatarioUsername().isBlank()) {
            throw new RuntimeException("Username do destinatário é obrigatório");
        }

        if (conviteDTO.familiaId() != null && !Objects.equals(conviteDTO.familiaId(), solicitante.getFamilia().getId())) {
            throw new RuntimeException("Família do convite não corresponde à família do solicitante");
        }

        Usuario destinatario = usuarioRepository.findByUsername(conviteDTO.destinatarioUsername());

        if (destinatario.equals(solicitante)) {
            throw new RuntimeException("Não pode convidar a si mesmo");
        }

        if (destinatario.getFamilia() != null) {
            throw new RuntimeException("Usuário já está em uma família");
        }

        Convite convite = new Convite();
        convite.setDestinatario(destinatario);
        convite.setFamilia(solicitante.getFamilia());
        convite.setStatus(StatusConvite.PENDENTE);

        conviteRepository.save(convite);
        return new ConviteResponseDTO(convite);
    }

    @Transactional
    public ConviteResponseDTO aceitarConvite(Long conviteId, Usuario usuario) {
        Convite convite = conviteRepository.findById(conviteId)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado"));

        if (!convite.getDestinatario().equals(usuario)) {
            throw new RuntimeException("Não autorizado");
        }

        if (usuario.getFamilia() != null) {
            throw new RuntimeException("Já está em uma família");
        }

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new RuntimeException("Convite não está mais pendente");
        }
        usuario.setFamilia(convite.getFamilia());

        convite.setStatus(StatusConvite.ACEITO);

        usuarioRepository.save(usuario);
        conviteRepository.save(convite);
        return new ConviteResponseDTO(convite);
    }

    @Transactional
    public ConviteResponseDTO recusarConvite(Long conviteId, Usuario usuario) {
        if (usuario.getFamilia() != null) {
            throw new RuntimeException("Já está em uma família");
        }
        Convite convite = conviteRepository.findById(conviteId)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado"));

        if (!usuario.equals(convite.getDestinatario())) {
            throw new RuntimeException("Não autorizado");
        }
        convite.setStatus(StatusConvite.RECUSADO);
        conviteRepository.save(convite);
        return new ConviteResponseDTO(convite);
    }

    public List<ConviteResponseDTO> listarPendentes(Usuario usuario) {
        return conviteRepository.findByDestinatarioAndStatus(usuario, StatusConvite.PENDENTE)
                .stream()
                .map(convite -> new ConviteResponseDTO(convite))
                .toList();
    }
}
