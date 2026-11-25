package com.npl.na_ponta_do_lapis.service.admin;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provisiona contas ADMIN_SITE a partir da variável de ambiente APP_ADMIN_EMAILS.
 * Por segurança, a promoção automática exige que o usuário tenha emailVerificado = true.
 */
@Service
public class AdminBootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAuthCacheService usuarioAuthCacheService;
    private final Set<String> adminEmails;

    public AdminBootstrapService(
            UsuarioRepository usuarioRepository,
            UsuarioAuthCacheService usuarioAuthCacheService,
            @Value("${app.admin.emails:}") String adminEmailsRaw
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
        if (adminEmailsRaw == null || adminEmailsRaw.isBlank()) {
            this.adminEmails = Collections.emptySet();
        } else {
            this.adminEmails = Arrays.stream(adminEmailsRaw.split(","))
                    .map(s -> s.strip().toLowerCase(Locale.ROOT))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmails.isEmpty()) {
            return;
        }
        for (String email : adminEmails) {
            usuarioRepository.findByEmailIgnoreCase(email).ifPresentOrElse(
                    usuario -> {
                        if (promoverSeElegivel(usuario)) {
                            log.info("Bootstrap ADMIN_SITE: usuário '{}' promovido para ADMIN_SITE.", email);
                        } else if (!usuario.isEmailVerificado()) {
                            log.info("Bootstrap ADMIN_SITE: usuário '{}' ainda não verificou o e-mail; promoção adiada até verificação.", email);
                        }
                    },
                    () -> log.info("Bootstrap ADMIN_SITE: e-mail '{}' ainda não cadastrado; será promovido automaticamente após cadastro e confirmação de e-mail.", email)
            );
        }
    }

    public boolean isEmailBootstrapAdmin(String email) {
        if (email == null || email.isBlank() || adminEmails.isEmpty()) {
            return false;
        }
        return adminEmails.contains(email.strip().toLowerCase(Locale.ROOT));
    }

    @Transactional
    public boolean promoverSeElegivel(Usuario usuario) {
        if (usuario == null || !isEmailBootstrapAdmin(usuario.getEmail())) {
            return false;
        }
        if (!usuario.isEmailVerificado()) {
            return false;
        }
        if (usuario.getPapel() == Papel.ADMIN_SITE) {
            return false;
        }
        usuario.setPapel(Papel.ADMIN_SITE);
        usuarioRepository.save(usuario);
        if (usuarioAuthCacheService != null) {
            usuarioAuthCacheService.evictPorUsuario(usuario);
        }
        return true;
    }
}
