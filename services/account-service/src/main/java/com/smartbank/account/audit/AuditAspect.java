package com.smartbank.account.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Base64;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        AuditLog entry = new AuditLog();
        entry.setAction(auditable.action());
        if (auditable.resourceType() != null && !auditable.resourceType().isBlank()) {
            entry.setResourceType(auditable.resourceType());
        }

        try {
            extractRequestContext(entry);
            extractAuthFromJwt(entry);
            extractParams(entry, joinPoint, auditable);

            Object result = joinPoint.proceed();

            entry.setSuccess(true);
            try {
                auditRepository.save(entry);
            } catch (Exception e) {
                log.warn("Failed to save audit log: {}", e.getMessage());
            }
            return result;

        } catch (Throwable t) {
            entry.setSuccess(false);
            entry.setErrorMessage(truncate(t.getMessage(), 500));
            try {
                auditRepository.save(entry);
            } catch (Exception e) {
                log.warn("Failed to save audit log: {}", e.getMessage());
            }
            throw t;
        }
    }

    private void extractRequestContext(AuditLog entry) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                entry.setIpAddress(clientIp(req));
                entry.setUserAgent(truncate(req.getHeader("User-Agent"), 500));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Extract user identity from the JWT passed by the API Gateway.
     * The gateway forwards the original Authorization header.
     * We decode the JWT payload (without verifying) to get username/role.
     */
    private void extractAuthFromJwt(AuditLog entry) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest req = attrs.getRequest();
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) return;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            var node = objectMapper.readTree(payloadJson);

            if (node.has("sub")) entry.setUsername(node.get("sub").asText());
            if (node.has("userId")) entry.setUserId(node.get("userId").asText());
        } catch (Exception e) {
            // ignore — user info is best-effort
        }
    }

    private void extractParams(AuditLog entry, ProceedingJoinPoint joinPoint, Auditable auditable) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] params = method.getParameters();

        try {
            if (!auditable.accountIdParam().isBlank()) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i].getName().equals(auditable.accountIdParam()) && args[i] != null) {
                        entry.setAccountId(UUID.fromString(args[i].toString()));
                        break;
                    }
                }
            }

            if (!auditable.resourceIdParam().isBlank()) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i].getName().equals(auditable.resourceIdParam()) && args[i] != null) {
                        entry.setResourceId(UUID.fromString(args[i].toString()));
                        break;
                    }
                }
            }

            if (entry.getAccountId() == null && entry.getResourceId() == null) {
                StringBuilder details = new StringBuilder();
                for (Object arg : args) {
                    if (arg == null) continue;
                    if (arg instanceof byte[] || arg instanceof java.io.InputStream) continue;
                    try {
                        details.append(objectMapper.writeValueAsString(arg)).append(" | ");
                    } catch (Exception e) {
                        // ignore non-serializable
                    }
                }
                String d = details.toString().trim();
                if (d.endsWith("|")) d = d.substring(0, d.length() - 1).trim();
                entry.setDetails(truncate(d, 4000));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private String clientIp(HttpServletRequest req) {
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        for (String h : headers) {
            String v = req.getHeader(h);
            if (v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v)) {
                return v.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}