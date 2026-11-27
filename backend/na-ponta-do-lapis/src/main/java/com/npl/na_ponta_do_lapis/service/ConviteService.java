package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.repository.ConviteRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheService;
import com.npl.na_ponta_do_lapis.web.dto.ConviteDTO;
import com.npl.na_ponta_do_lapis.web.dto.ConviteResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.BusinessException;
import com.npl.na_ponta_do_lapis.web.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAuthCacheService usuarioAuthCacheService;

    public ConviteService(
            ConviteRepository conviteRepository,
            UsuarioRepository usuarioRepository,
            UsuarioAuthCacheService usuarioAuthCacheService
    ) {
        this.conviteRepository = conviteRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
    }

    @Transactional
    public ConviteResponseDTO enviarConvite(ConviteDTO conviteDTO, Usuario solicitante) {
        if (!solicitante.isAdminFamilia()) {
            throw new AccessDeniedException("Apenas administradores da família podem enviar convites.");
        }

        if (solicitante.getFamilia() == null) {
            throw new BusinessException("Você precisa pertencer a uma família para enviar convites.");
        }

        if (conviteDTO == null || conviteDTO.destinatarioUsername() == null || conviteDTO.destinatarioUsername().isBlank()) {
            throw new BusinessException("Username do destinatário é obrigatório.");
        }

        if (conviteDTO.familiaId() != null && !Objects.equals(conviteDTO.familiaId(), solicitante.getFamilia().getId())) {
            throw new AccessDeniedException("Família do convite não corresponde à família do solicitante.");
        }

        Usuario destinatario = usuarioRepository.findByUsername(conviteDTO.destinatarioUsername());
        if (destinatario == null) {
            throw new NotFoundException("Usuário destinatário não encontrado.");
        }

        if (destinatario.equals(solicitante)) {
            throw new BusinessException("Não é possível convidar a si mesmo.");
        }

        if (destinatario.getFamilia() != null) {
            throw new BusinessException("Este usuário já pertence a uma família.");
        }

        if (conviteRepository.existsByDestinatarioAndFamiliaAndStatus(destinatario, solicitante.getFamilia(), StatusConvite.PENDENTE)) {
            throw new BusinessException("Já existe um convite pendente para este usuário nesta família.");
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
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));

        if (!convite.getDestinatario().equals(usuario)) {
            throw new AccessDeniedException("Você não tem permissão para responder a este convite.");
        }

        if (usuario.getFamilia() != null) {
            throw new BusinessException("Você já pertence a uma família.");
        }

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessException("Este convite não está mais pendente.");
        }
        usuario.setFamilia(convite.getFamilia());

        convite.setStatus(StatusConvite.ACEITO);

        usuarioRepository.save(usuario);
        conviteRepository.save(convite);
        usuarioAuthCacheService.evictPorUsuario(usuario);
        return new ConviteResponseDTO(convite);
    }

    @Transactional
    public ConviteResponseDTO recusarConvite(Long conviteId, Usuario usuario) {
        Convite convite = conviteRepository.findById(conviteId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));

        if (!usuario.equals(convite.getDestinatario())) {
            throw new AccessDeniedException("Você não tem permissão para responder a este convite.");
        }
        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessException("Este convite não está mais pendente.");
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
