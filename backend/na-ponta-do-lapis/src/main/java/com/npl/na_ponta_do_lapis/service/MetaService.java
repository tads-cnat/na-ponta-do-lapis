package com.npl.na_ponta_do_lapis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.service.factory.CalculadoraMetaFactory;
import com.npl.na_ponta_do_lapis.service.strategy.CalculadoraMetaStrategy;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.ContaIdNaoExisteException;
import com.npl.na_ponta_do_lapis.web.exception.MetaNaoEncontradaException;

@Service
public class MetaService {

    private final CalculadoraMetaFactory factory;
    private final MetaRepository metaRepository;
    private final ContaFinanceiraRepository contaFinanceiraRepository;

    public MetaService(MetaRepository metaRepository, CalculadoraMetaFactory factory,
                       ContaFinanceiraRepository contaFinanceiraRepository) {
        this.metaRepository = metaRepository;
        this.factory = factory;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
    }

    @Transactional(readOnly = true)
    public List<MetaResponseDTO> listarTodas(Usuario usuarioAutenticado) {
        return metaRepository.findByUsuarioId(usuarioAutenticado.getId())
                .stream()
                .map(this::montarResponseComProgresso)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetaResponseDTO buscarPorId(Long id, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(MetaNaoEncontradaException::new);

        if (!meta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new MetaNaoEncontradaException();
        }

        return montarResponseComProgresso(meta);
    }

    @Transactional
    public MetaResponseDTO criar(MetaDTO dto, Usuario usuarioAutenticado) {
        ContaFinanceira conta = null;
        if (dto.contaId() != null) {
            conta = contaFinanceiraRepository.findById(dto.contaId())
                    .orElseThrow(() -> new ContaIdNaoExisteException("Conta financeira não encontrada"));

            if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                throw new ContaIdNaoExisteException("Conta não pertence ao usuário autenticado");
            }
        }

        Meta novaMeta = dto.toEntity();
        novaMeta.setUsuario(usuarioAutenticado);
        novaMeta.setConta(conta);

        Meta metaSalva = metaRepository.save(novaMeta);
        return montarResponseComProgresso(metaSalva);
    }

    @Transactional
    public MetaResponseDTO atualizar(Long id, MetaDTO dto, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(MetaNaoEncontradaException::new);

        if (!meta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new MetaNaoEncontradaException();
        }

        ContaFinanceira conta = null;
        if (dto.contaId() != null) {
            conta = contaFinanceiraRepository.findById(dto.contaId())
                    .orElseThrow(() -> new ContaIdNaoExisteException("Conta financeira não encontrada"));

            if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                throw new ContaIdNaoExisteException("Conta não pertence ao usuário autenticado");
            }
        }

        meta.setNome(dto.nome());
        meta.setDescricao(dto.descricao());
        meta.setFotoUrl(dto.fotoUrl());
        meta.setValor(dto.valor());
        meta.setDataLimite(dto.dataLimite());
        meta.setTipoMeta(dto.tipoMeta());
        meta.setConta(conta);

        Meta atualizado = metaRepository.save(meta);
        return montarResponseComProgresso(atualizado);
    }

    @Transactional
    public void deletar(Long id, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(MetaNaoEncontradaException::new);

        if (!meta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new MetaNaoEncontradaException();
        }

        metaRepository.delete(meta);
    }

    private MetaResponseDTO montarResponseComProgresso(Meta meta) {
        CalculadoraMetaStrategy calculadora = factory.obterCalculadora(meta.getTipoMeta());
        double progresso = calculadora.calcularProgresso(meta.getValorAtual(), meta.getValor());
        return new MetaResponseDTO(meta, progresso);
    }
}