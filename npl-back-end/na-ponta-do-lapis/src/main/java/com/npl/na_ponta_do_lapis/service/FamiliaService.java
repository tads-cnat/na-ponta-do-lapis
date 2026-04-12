package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.repository.FamiliaRepository;
import com.npl.na_ponta_do_lapis.web.Controller.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.Controller.dto.FamiliaResponseDTO;
import com.npl.na_ponta_do_lapis.web.Controller.dto.UsuarioResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamiliaService {
    private final FamiliaRepository familiaRepository;

    public FamiliaService( FamiliaRepository familiaRepository){this.familiaRepository = familiaRepository;}

    @Transactional
    public FamiliaResponseDTO criarFamilia(FamiliaDTO familiaDTO) {
        Familia familia = familiaDTO.toEntity();
        familiaRepository.save(familia);
        return new FamiliaResponseDTO(familia);
    }

    @Transactional
    public void excluirFamilia(long id){
        familiaRepository.deleteById(id);
    }

    @Transactional
    public FamiliaResponseDTO editarFamilia(long id, FamiliaDTO familiaDTO){
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Família de ID " + id + " não existe!"));
        if (familiaDTO.nome() != null){
            if (familiaDTO.nome().isBlank()) {
                throw new RuntimeException("Nome da família não pode ser vazio.");
            }
            familia.setNome(familiaDTO.nome());
        }
        if (familiaDTO.foto() != null){
            familia.setFotoFamilia(familiaDTO.foto());
        }
        familiaRepository.save(familia);
        return new FamiliaResponseDTO(familia);
    }

    //@Transactional
    //public UsuarioResponseDTO adicionarUsuarioNaFamilia(){}

    //@Transactional
    //public UsuarioResponseDTO removerUsuarioDaFamilia(){}

    //public List<UsuarioResponseDTO> listarUsuariosDaFamilia(){}

    //@Transactional
    //public UsuarioResponseDTO tornarAdminFamilia(){}

    public List<FamiliaResponseDTO> listarFamilias(){
        return familiaRepository.findAll()
                .stream()
                .map(familia -> new FamiliaResponseDTO(familia))
                .toList();
    }

    public FamiliaResponseDTO buscarFamiliaPorID(long id){
        return familiaRepository.findById(id)
                .map(familia -> new FamiliaResponseDTO(familia))
                .orElseThrow(() -> new RuntimeException("Família de ID " + id + " não existe!"));
    }

}
