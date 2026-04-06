package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.request.AuthenticationRequest;
import com.example.E_Learning_Platform.dto.request.LogoutRequest;
import com.example.E_Learning_Platform.dto.response.AuthenticationResponse;
import com.example.E_Learning_Platform.entity.InvalidatedToken;
import com.example.E_Learning_Platform.entity.RefreshToken;
import com.example.E_Learning_Platform.entity.RevokedSession;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.repository.InvalidatedTokenRepository;
import com.example.E_Learning_Platform.repository.RefreshTokenRepository;
import com.example.E_Learning_Platform.repository.RevokedSessionRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 3600;
    private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 360000;

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedSessionRepository revokedSessionRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @NonFinal
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    protected String SIGNER_KEY;

    private String generateToken(User user, long expirySeconds, String tokenType, String sessionId) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("minh-m80")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expirySeconds, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("tokenType", tokenType)
                .claim("sessionId", sessionId)
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));

        try {
            JWSSigner signer = new MACSigner(SIGNER_KEY.getBytes());
            jwsObject.sign(signer);
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        log.info("Login");

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String sessionId = UUID.randomUUID().toString();

        String accessToken = generateToken(user, ACCESS_TOKEN_EXPIRY_SECONDS, "ACCESS", sessionId);
        String refreshToken = generateToken(user, REFRESH_TOKEN_EXPIRY_SECONDS, "REFRESH", sessionId);

        saveRefreshToken(user.getEmail(), sessionId, refreshToken);

        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("tokenType");
        if (isRefresh && !"REFRESH".equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!isRefresh && !"ACCESS".equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String sessionId = signedJWT.getJWTClaimsSet().getStringClaim("sessionId");
        if (sessionId == null || revokedSessionRepository.existsById(sessionId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private SignedJWT verifyTokenForLogout(String token) throws ParseException, JOSEException {
        try {
            return verifyToken(token, false);
        } catch (AppException exception) {
            return verifyToken(token, true);
        }
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyTokenForLogout(request.getToken());

            String tokenId = signedJWT.getJWTClaimsSet().getJWTID();
            String sessionId = signedJWT.getJWTClaimsSet().getStringClaim("sessionId");
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            invalidateToken(tokenId, expiryTime);
            revokeSession(sessionId);

        } catch (AppException exception) {
            log.info("Token already expired or invalid");
        }
    }

    public AuthenticationResponse refreshToken(LogoutRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);

        String refreshJti = signedJWT.getJWTClaimsSet().getJWTID();
        String sessionId = signedJWT.getJWTClaimsSet().getStringClaim("sessionId");
        String email = signedJWT.getJWTClaimsSet().getSubject();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        validateRefreshTokenInStore(refreshJti, request.getToken());

        invalidateToken(refreshJti, expiryTime);
        revokeRefreshToken(refreshJti);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newAccessToken = generateToken(user, ACCESS_TOKEN_EXPIRY_SECONDS, "ACCESS", sessionId);
        String newRefreshToken = generateToken(user, REFRESH_TOKEN_EXPIRY_SECONDS, "REFRESH", sessionId);

        saveRefreshToken(email, sessionId, newRefreshToken);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }

    private void saveRefreshToken(String email, String sessionId, String refreshToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            String refreshJti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            String tokenHash = hashToken(refreshToken);

            RefreshToken entity = RefreshToken.builder()
                    .id(refreshJti)
                    .userEmail(email)
                    .sessionId(sessionId)
                    .tokenHash(tokenHash)
                    .expiryTime(expiryTime)
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(entity);

            long ttlSeconds = Math.max(
                    1,
                    (expiryTime.getTime() - System.currentTimeMillis()) / 1000
            );

            String redisKey = buildRefreshRedisKey(refreshJti);

            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    tokenHash,
                    Duration.ofSeconds(ttlSeconds)
            );
            String redisValue = stringRedisTemplate.opsForValue().get(redisKey);
            log.info("Saved refresh token to Redis successfully. key={}, valueExists={}",
                    redisKey, redisValue );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateRefreshTokenInStore(String refreshJti, String rawRefreshToken) {
        String redisKey = buildRefreshRedisKey(refreshJti);
        String storedHash = stringRedisTemplate.opsForValue().get(redisKey);

        log.info("Checking refresh token in Redis. key={}", redisKey);

        if (storedHash == null) {

            log.info("Redis miss for refresh token. Falling back to DB. refreshJti={}", refreshJti);
            RefreshToken refreshToken = refreshTokenRepository.findById(refreshJti)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            if (refreshToken.isRevoked() || refreshToken.getExpiryTime().before(new Date())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            storedHash = refreshToken.getTokenHash();
        }

        if (!storedHash.equals(hashToken(rawRefreshToken))) {
            log.info("Refresh token hash mismatch. refreshJti={}", refreshJti);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        log.info("Refresh token validated successfully. refreshJti={}", refreshJti);
    }

    private void revokeSession(String sessionId) {
        revokedSessionRepository.save(
                RevokedSession.builder()
                        .sessionId(sessionId)
                        .revokedAt(new Date())
                        .build()
        );

        refreshTokenRepository.findBySessionIdAndRevokedFalse(sessionId)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    stringRedisTemplate.delete(buildRefreshRedisKey(token.getId()));
                });
    }

    private void revokeRefreshToken(String refreshJti) {
        refreshTokenRepository.findById(refreshJti).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });

        stringRedisTemplate.delete(buildRefreshRedisKey(refreshJti));
    }

    private void invalidateToken(String tokenId, Date expiryTime) {
        invalidatedTokenRepository.save(
                InvalidatedToken.builder()
                        .id(tokenId)
                        .expiryTime(expiryTime)
                        .build()
        );
    }

    private String buildRefreshRedisKey(String refreshJti) {

        return "refresh:" + refreshJti;
    }

    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}
