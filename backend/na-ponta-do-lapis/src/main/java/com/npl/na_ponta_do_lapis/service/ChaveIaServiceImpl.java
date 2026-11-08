package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.ChaveIaUsuarioRepository;
import com.npl.na_ponta_do_lapis.service.crypto.CryptoService;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChaveIaServiceImpl implements ChaveIaService {

    private final ChaveIaUsuarioRepository repository;
    private final CryptoService cryptoService;

    public ChaveIaServiceImpl(ChaveIaUsuarioRepository repository, CryptoService cryptoService) {
        this.repository = repository;
        this.cryptoService = cryptoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChaveIaResponseDTO> listarChaves(Usuario usuario) {
        return repository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId())
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChaveIaResponseDTO cadastrarChave(Usuario usuario, ChaveIaRequestDTO dto) {
        boolean deveSerPadrao = dto.padrao();

        // Se for a primeira chave do usuário, torna padrão automaticamente
        List<ChaveIaUsuario> chavesExistentes = repository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId());
        if (chavesExistentes.isEmpty()) {
            deveSerPadrao = true;
        }

        if (deveSerPadrao) {
            desmarcarChavesPadrao(usuario.getId());
        }

        String chaveCifrada = cryptoService.encrypt(dto.apiKey().trim());
        ChaveIaUsuario chave = new ChaveIaUsuario(
                usuario,
                dto.apelido().trim(),
                dto.provedor(),
                chaveCifrada,
                deveSerPadrao
        );

        ChaveIaUsuario salva = repository.save(chave);
        return converterParaResponse(salva);
    }

    @Override
    @Transactional
    public void definirComoPadrao(Usuario usuario, Long chaveId) {
        ChaveIaUsuario chave = repository.findByIdAndUsuarioId(chaveId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chave de IA não encontrada"));

        desmarcarChavesPadrao(usuario.getId());
        chave.setPadrao(true);
        repository.save(chave);
    }

    @Override
    @Transactional
    public void deletarChave(Usuario usuario, Long chaveId) {
        ChaveIaUsuario chave = repository.findByIdAndUsuarioId(chaveId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chave de IA não encontrada"));

        boolean eraPadrao = chave.isPadrao();
        repository.delete(chave);

        // Se deletou a chave padrão e ainda restam chaves, elege a mais recente como padrão
        if (eraPadrao) {
            List<ChaveIaUsuario> restantes = repository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId());
            if (!restantes.isEmpty()) {
                ChaveIaUsuario novaPadrao = restantes.get(0);
                novaPadrao.setPadrao(true);
                repository.save(novaPadrao);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChaveIaUsuario obterEntidade(Usuario usuario, Long chaveId) {
        return repository.findByIdAndUsuarioId(chaveId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chave de IA não encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public String obterChaveDecifrada(Usuario usuario, Long chaveId) {
        ChaveIaUsuario chave = repository.findByIdAndUsuarioId(chaveId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chave de IA não encontrada"));

        return cryptoService.decrypt(chave.getChaveCriptografada());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChaveIaUsuario> obterChavePadrao(Usuario usuario) {
        return repository.findByUsuarioIdAndPadraoTrue(usuario.getId());
    }

    private void desmarcarChavesPadrao(Long usuarioId) {
        repository.findByUsuarioIdAndPadraoTrue(usuarioId).ifPresent(c -> {
            c.setPadrao(false);
            repository.save(c);
        });
    }

    private ChaveIaResponseDTO converterParaResponse(ChaveIaUsuario c) {
        String chaveDecifrada = cryptoService.decrypt(c.getChaveCriptografada());
        String mascarada = cryptoService.mask(chaveDecifrada);

        return new ChaveIaResponseDTO(
                c.getId(),
                c.getApelido(),
                c.getProvedor(),
                c.getProvedor().getNomeExibicao(),
                mascarada,
                c.isPadrao(),
                c.getProvedor().isSuportaPdf(),
                c.getCriadoEm()
        );
    }
}
