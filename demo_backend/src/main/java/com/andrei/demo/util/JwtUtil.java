package com.andrei.demo.util;

import com.andrei.demo.model.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    private Date getCurrentDate() {
        return Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Person person) {
        return Jwts
                .builder()
                .subject(person.getEmail())
                .issuer("demo-spring-boot-backend")
                .issuedAt(getCurrentDate())
                .claims(Map.of(
                        "userId", person.getId(),
                        "role", "ADMIN"
                ))
                // the token will be expired in 10 hours
                .expiration(new Date(System.currentTimeMillis() + 1000* 60 * 60 *10))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean checkClaims(String token){
        Claims claims = getAllClaimsFromToken(token);

        // check issuer
        if (!"demo-spring-boot-backend".equals(claims.getIssuer())) {
            log.error("Invalid token issuer");
            return false;
        }

        // check expiration
        if (claims.getExpiration().before(getCurrentDate())) {
            log.error("Token has expired");
            return false;
        }

        // check iat
        if (claims.getIssuedAt() == null || claims.getIssuedAt().after(getCurrentDate())) {
            log.error("Token issued at date is invalid");
            return false;
        }
        // check claims
        if (claims.get("userId") == null || claims.get("role") == null) {
            log.error("Token claims are invalid: does not contain userId and role");
            return false;
        }
        log.info("Token is valid. User ID: {}, Role: {}",
                claims.get("userId"), claims.get("role"));
        return true;
    }

}
