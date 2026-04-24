package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.repository.ConviteRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.dto.ConviteResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ConviteResponseDTO enviarConvite(String username, Usuario solicitante) {
        if (solicitante.getPapel() != Papel.ADMIN_FAMILIA) {
            throw new RuntimeException("Apenas admins podem enviar convite");
        }

        if (solicitante.getFamilia() == null) {
            throw new RuntimeException("Admin sem família?");
        }

        Usuario destinatario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

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

        usuario.setFamilia(convite.getFamilia());

        convite.setStatus(StatusConvite.ACEITO);

        usuarioRepository.save(usuario);
        conviteRepository.save(convite);
        return new ConviteResponseDTO(convite);
    }

    @Transactional
    public ConviteResponseDTO recusarConvite(Long conviteId, Usuario usuario) {
        Convite convite = conviteRepository.findById(conviteId)
                .orElseThrow( );
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
