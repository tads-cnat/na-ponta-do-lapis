package com.npl.na_ponta_do_lapis.security.jwt;

import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    public static final String JWT_AUTHORIZATION = "Authorization";
    public static final String JWT_BEARER = "Bearer ";

    private final SecretKey KEY;
    private final long EXPIRE_DAYS;
    private final long EXPIRE_HOURS;
    private final long EXPIRE_MINUTES;
    private final long EXPIRERATION_REFRESH_TOKEN_DAYS;
    private final UsuarioRepository usuarioRepository;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expire-days:1}") long expireDays,
            @Value("${jwt.expire-hours:1}") long expireHours,
            @Value("${jwt.expire-minutes:5}") long expireMinutes,
            @Value("${jwt.expiration-refresh-days:7}") long expirationRefreshTokenDays,
            UsuarioRepository usuarioRepository) {
        this.KEY = Keys.hmacShaKeyFor(secret.getBytes());
        this.EXPIRE_DAYS = expireDays;
        this.EXPIRE_HOURS = expireHours;
        this.EXPIRE_MINUTES = expireMinutes;
        this.EXPIRERATION_REFRESH_TOKEN_DAYS = expirationRefreshTokenDays;
        this.usuarioRepository = usuarioRepository;
    }

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

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public boolean validateToken(String token) {
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
    }

    public Date EXPIRATION_REFRESH_TOKEN(Date start) {
        LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = dateTime.plusDays(EXPIRERATION_REFRESH_TOKEN_DAYS);
        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    }

}
