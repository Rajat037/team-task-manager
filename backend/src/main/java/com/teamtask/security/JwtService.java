package com.teamtask.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtask.model.Role;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String email, Role role) {
        long expiresAt = Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond();
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of("sub", email, "role", role.name(), "exp", expiresAt);
        String unsigned = base64Json(header) + "." + base64Json(payload);
        return unsigned + "." + sign(unsigned);
    }

    public String getSubject(String token) {
        Map<String, Object> payload = payload(token);
        Object expiration = payload.get("exp");
        if (expiration instanceof Number number && number.longValue() < Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("Token expired");
        }
        return payload.get("sub").toString();
    }

    public boolean isValid(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        return sign(parts[0] + "." + parts[1]).equals(parts[2]);
    }

    private Map<String, Object> payload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (!isValid(token)) {
                throw new IllegalArgumentException("Invalid token");
            }
            byte[] json = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token", ex);
        }
    }

    private String base64Json(Map<String, Object> value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not encode JWT", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign JWT", ex);
        }
    }
}
