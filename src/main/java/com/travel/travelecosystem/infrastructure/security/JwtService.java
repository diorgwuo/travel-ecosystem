package com.travel.travelecosystem.infrastructure.security;

import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${spring.security.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${spring.security.jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object v = claims.get(CLAIM_USER_ID);
            if (v instanceof Number n) {
                return n.longValue();
            }
            if (v != null) {
                return Long.parseLong(v.toString());
            }
            return null;
        });
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserEntity user) {
        Map<String, Object> claims = baseClaims(user);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(claims, user.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(UserEntity user) {
        Map<String, Object> claims = baseClaims(user);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(claims, user.getUsername(), refreshExpiration);
    }

    /**
     * @deprecated use {@link #generateAccessToken(UserEntity)}
     */
    @Deprecated
    public String generateToken(UserDetails userDetails) {
        if (userDetails instanceof UserEntity user) {
            return generateAccessToken(user);
        }
        return buildToken(new HashMap<>(), userDetails.getUsername(), jwtExpiration);
    }

    /**
     * @deprecated use {@link #generateAccessToken(UserEntity)} with explicit claims if needed
     */
    @Deprecated
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        if (userDetails instanceof UserEntity user) {
            Map<String, Object> claims = baseClaims(user);
            claims.putAll(extraClaims);
            claims.putIfAbsent(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
            return buildToken(claims, user.getUsername(), jwtExpiration);
        }
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    private static Map<String, Object> baseClaims(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getId());
        claims.put("role", user.getRole().name());
        return claims;
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isAccessToken(String token) {
        String type = extractClaim(token, c -> c.get(CLAIM_TOKEN_TYPE, String.class));
        return type == null || TOKEN_TYPE_ACCESS.equals(type);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
