package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Notificacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;
import com.npl.na_ponta_do_lapis.repository.NotificacaoRepository;
import com.npl.na_ponta_do_lapis.web.dto.NotificacaoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarUltimasPorUsuario(Long usuarioId) {
        return notificacaoRepository.findTop20ByUsuarioIdOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(NotificacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidasPorUsuario(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLida(Long notificacaoId, Long usuarioId) {
        notificacaoRepository.findById(notificacaoId).ifPresent(n -> {
            if (n.getUsuario().getId().equals(usuarioId)) {
                n.setLida(true);
                notificacaoRepository.save(n);
            }
        });
    }

    @Transactional
    public void marcarTodasComoLidas(Long usuarioId) {
        notificacaoRepository.marcarTodasComoLidasPorUsuario(usuarioId);
    }

    @Transactional
    public void excluir(Long notificacaoId, Long usuarioId) {
        notificacaoRepository.findById(notificacaoId).ifPresent(n -> {
            if (n.getUsuario().getId().equals(usuarioId)) {
                notificacaoRepository.delete(n);
            }
        });
    }

    @Transactional
    public Notificacao criarNotificacao(Usuario usuario, String titulo, String mensagem, TipoNotificacao tipo, String link) {
        Notificacao notificacao = new Notificacao(usuario, titulo, mensagem, tipo, link);
        return notificacaoRepository.save(notificacao);
    }
}

