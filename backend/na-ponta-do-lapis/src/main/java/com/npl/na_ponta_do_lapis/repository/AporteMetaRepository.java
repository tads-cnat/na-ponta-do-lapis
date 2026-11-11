package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.AporteMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AporteMetaRepository extends JpaRepository<AporteMeta, Long> {
    List<AporteMeta> findByMetaIdOrderByDataAporteDesc(Long metaId);
<<<<<<< HEAD
<<<<<<< HEAD

    long countByUsuarioId(Long usuarioId);
=======
>>>>>>> 5fa35d6 (feat(metas): consolida 3 tipos essenciais, limpa tipo legado e implementa notificacoes e aportes)
=======

    long countByUsuarioId(Long usuarioId);
>>>>>>> 4b9c67c (feat(gamificacao): implementar sistema de conquistas, niveis de saude financeira, xp e aba no perfil)
}

