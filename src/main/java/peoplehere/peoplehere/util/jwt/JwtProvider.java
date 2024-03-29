package peoplehere.peoplehere.util.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import peoplehere.peoplehere.common.exception.jwt.bad_request.JwtUnsupportedTokenException;
import peoplehere.peoplehere.common.exception.jwt.unauthorized.JwtMalformedTokenException;
import peoplehere.peoplehere.common.exception.jwt.unauthorized.JwtInvalidTokenException;
import peoplehere.peoplehere.common.exception.jwt.unauthorized.JwtExpiredTokenException;
import peoplehere.peoplehere.service.UserDetailsServiceImpl;

import java.security.Key;
import java.util.Date;

import static peoplehere.peoplehere.common.response.status.BaseExceptionResponseStatus.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

    private final UserDetailsServiceImpl userDetailsService;

    @Value("${secret.jwt-secret-key}")
    private String jwtSecretKey;

    @Value("${secret.jwt-expired-in}")
    private long jwtExpiredIn;

    @Value("${secret.jwt-refresh-secret-key}")
    private String jwtRefreshSecretKey;

    @Value("${secret.jwt-refresh-expired-in}")
    private long jwtRefreshExpiredIn;

    public String createAccessToken(String email) {
        return createToken(email, jwtExpiredIn, jwtSecretKey);
    }

    public String createRefreshToken(String email) {
        return createToken(email, jwtRefreshExpiredIn, jwtRefreshSecretKey);
    }

    private String createToken(String email, long expirationTime, String secretKey) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .setSubject(String.valueOf(email))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        log.info("생성된 토큰: {}", token);
        return token;
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey).build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredTokenException(JWT_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtUnsupportedTokenException(JWT_TOKEN_UNSUPPORTED);
        } catch (MalformedJwtException e) {
            throw new JwtMalformedTokenException(JWT_TOKEN_MALFORMED);
        } catch (IllegalArgumentException | JwtException e) {
            throw new JwtInvalidTokenException(JWT_INVALID_TOKEN);
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey).build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.error("토큰 만료 오류: {}", e.getMessage());
            throw new JwtExpiredTokenException(JWT_TOKEN_EXPIRED);
        } catch (JwtException e) {
            log.error("토큰 유효성 검사 오류: {}", e.getMessage());
            throw new JwtInvalidTokenException(JWT_INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String getEmailFromToken(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token);

        Claims claims = jws.getBody();
        String subject = claims.getSubject();
        if (subject != null && subject.contains("@")) {
            return subject;
        } else {
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

}
