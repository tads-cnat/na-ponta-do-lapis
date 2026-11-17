package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheService;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioUpdateDTO;
import com.npl.na_ponta_do_lapis.web.exception.BusinessException;
import com.npl.na_ponta_do_lapis.web.exception.UsuarioIdNaoExisteException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioAuthCacheService usuarioAuthCacheService;

    @PersistenceContext
    private EntityManager entityManager;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            UsuarioAuthCacheService usuarioAuthCacheService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
    }

    public List<Usuario> listarUsuarios(){
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(
                        () -> new UsuarioIdNaoExisteException("Usuário de ID " + id + " não existe!")
                );
    }

    public Usuario buscarUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario buscarUsuarioLogado(){
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        return buscarUsuarioPorEmail(email);
    }

    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    
    @Transactional
    public Usuario atualizarUsuarioLogado(UsuarioUpdateDTO dto) {
        Usuario usuario = buscarUsuarioLogado();
        String emailAnterior = usuario.getEmail();
        String usernameAnterior = usuario.getUsername();

        if (dto.nome() != null && !dto.nome().trim().isEmpty()) {
            usuario.setNome(dto.nome().trim());
        }

        if (dto.email() != null && !dto.email().trim().isEmpty()) {
            usuario.setEmail(dto.email().trim());
        }

        if (dto.senha() != null && !dto.senha().trim().isEmpty()) {
            if (dto.senhaAtual() == null || dto.senhaAtual().trim().isEmpty()
                    || !passwordEncoder.matches(dto.senhaAtual(), usuario.getPassword())) {
                throw new BusinessException("A senha atual está incorreta.");
            }

            String senhaCriptografada = passwordEncoder.encode(dto.senha());
            usuario.setSenha(senhaCriptografada);
        }

        if (dto.moedaPadrao() != null) {
            usuario.setMoedaPadrao(dto.moedaPadrao());
        }

        Usuario salvo = usuarioRepository.save(usuario);
        usuarioAuthCacheService.evict(emailAnterior, usernameAnterior, salvo.getEmail(), salvo.getUsername());
        return salvo;
    }

    @Transactional
    public void excluirUsuario(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + id + " não existe!"));
        excluirUsuarioEDependencias(usuario);
    }

    /**
     * Exclui definitivamente a conta do usuário autenticado e todos os seus dados pessoais/financeiros (LGPD Art. 18, VI).
     */
    @Transactional
    public void excluirContaLogada() {
        Usuario usuario = buscarUsuarioLogado();
        excluirUsuarioEDependencias(usuario);
    }

    private void excluirUsuarioEDependencias(Usuario usuario) {
        Long uid = usuario.getId();
        Long familiaId = usuario.getFamilia() != null ? usuario.getFamilia().getId() : null;

        if (entityManager != null) {
            entityManager.createQuery(
                    "DELETE FROM AporteMeta a WHERE a.usuario.id = :uid OR a.meta.usuario.id = :uid OR a.contaOrigem.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM Transacao t WHERE t.contaFinanceira.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM Meta m WHERE m.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM ContaFinanceira c WHERE c.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM ChaveIaUsuario c WHERE c.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM UsuarioPluggyIntegracao p WHERE p.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM UsuarioConquista uc WHERE uc.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM Notificacao n WHERE n.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM Convite c WHERE c.destinatario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM Marcador m WHERE m.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
            entityManager.createQuery("DELETE FROM Post p WHERE p.usuario.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();

            if (familiaId != null) {
                usuario.setFamilia(null);
                usuarioRepository.saveAndFlush(usuario);
                Long restantes = entityManager.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE u.familia.id = :fid", Long.class)
                        .setParameter("fid", familiaId)
                        .getSingleResult();
                if (restantes != null && restantes == 0L) {
                    entityManager.createQuery("DELETE FROM Convite c WHERE c.familia.id = :fid")
                            .setParameter("fid", familiaId)
                            .executeUpdate();
                    entityManager.createQuery("DELETE FROM Familia f WHERE f.id = :fid")
                            .setParameter("fid", familiaId)
                            .executeUpdate();
                }
            }

            entityManager.createQuery("DELETE FROM Usuario u WHERE u.id = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();
        } else {
            usuarioRepository.delete(usuario);
        }

        usuarioAuthCacheService.evictPorUsuario(usuario);
    }

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Usuário de email " + email + " não encontrado!")
        );
    }

    @Transactional
    public void tornarAdminFamilia(Long id){}

//    public Usuario buscarUsuarioAutenticado(Principal principal) {
//        return buscarUsuarioAutenticado(principal, null);
//    }

    public Usuario buscarUsuarioAutenticado(Principal principal, String usernameFallback) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            Usuario usuarioAutenticado = usuarioRepository.findByUsername(principal.getName());
            if (usuarioAutenticado == null) {
                throw new RuntimeException("Usuário autenticado não encontrado");
            }
            return usuarioAutenticado;
        }

        // Temporario: permite operar sem autenticacao formal enquanto o fluxo de login nao existe.
        if (usernameFallback == null || usernameFallback.isBlank()) {
            throw new RuntimeException("Usuário não autenticado. Informe o parâmetro 'username' temporariamente.");
        }

        Usuario usuarioFallback = usuarioRepository.findByUsername(usernameFallback);
        if (usuarioFallback == null) {
            throw new RuntimeException("Usuário informado não encontrado");
        }
        return usuarioFallback;
    }
}
