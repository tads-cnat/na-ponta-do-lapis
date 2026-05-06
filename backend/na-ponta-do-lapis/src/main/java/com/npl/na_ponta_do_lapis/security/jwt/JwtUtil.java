package com.npl.na_ponta_do_lapis.security.jwt;

import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    UsuarioRepository usuarioRepository;

    public JwtUtil(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private static final String SECRET =
            "c2d6bbaa4431dba6cd171db5c588f16612dd561811f0cb5aaba60809b1f2f5529dc5f335792bbd16891af5e5d32e540448eb39af3b613675c09656d7d76c0491";
    public static final long EXPIRE_DAYS = 1;
    public static final long EXPIRE_HOURS = 1;
    public static final long EXPIRE_MINUTES = 5;
    public static final long EXPIRERATION_REFRESH_TOKEN_DAYS = 7;
    public static final String JWT_AUTHORIZATION = "Authorization";
    public static final String JWT_BEARER = "Bearer ";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(UserDetails userDetails, Date expirationDate, String email) {
        List<String> papeis = userDetails.getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();

        Date limit = EXPIRATION_TOKEN(expirationDate);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("email", email)
                .claim("papeis", papeis)
                .expiration(limit)
                .signWith(KEY)
                .compact();
    }

    public static String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Date EXPIRATION_TOKEN(Date start) {
        LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = dateTime.plusDays(EXPIRE_DAYS).plusHours(EXPIRE_HOURS).plusMinutes(EXPIRE_MINUTES);
        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    };

    public Date EXPIRATION_REFRESH_TOKEN(Date start){
        LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = dateTime.plusDays(EXPIRERATION_REFRESH_TOKEN_DAYS);
        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    }

}
