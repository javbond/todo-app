# Security Review Report -- Clarity (todo-app)

## Summary

| Field | Value |
|-------|-------|
| **Review Date** | 2026-03-10 |
| **Reviewed By** | Security Agent (Opus) |
| **Scope** | Sprint 1 scaffold (backend 86 Java files + frontend 30 TS files + infrastructure) |
| **Overall Risk Level** | Medium (scaffold phase -- no production exposure) |
| **Merge Recommendation** | DO NOT BLOCK. Fix SEC-001 through SEC-005 before Sprint 2 auth ships. |

---

## Findings Summary

| Severity | Count | Status |
|----------|-------|--------|
| Critical | 1 | Open (not exploitable yet -- deferred to Sprint 2) |
| High | 4 | Open (must fix before auth goes live) |
| Medium | 6 | Open (fix during Sprint 2-3) |
| Low | 4 | Open (backlog) |
| Info | 3 | Positive observations |

---

## Critical Findings

### SEC-001: Unsafe Redis Deserialization (RCE Vector)

| Field | Value |
|-------|-------|
| **Severity** | CRITICAL |
| **OWASP Category** | A08:2021 -- Data Integrity Failures |
| **Location** | `backend/src/main/java/com/clarity/shared/config/RedisConfig.java` |
| **Status** | Open -- not actively exploitable (cache/blacklist methods are no-ops) |

**Description:** `GenericJackson2JsonRedisSerializer` enables polymorphic deserialization, which is a known Remote Code Execution vector. Combined with unauthenticated Redis (SEC-003), this creates an attack chain.

**Impact:** If Redis is accessible and cache methods become active, an attacker could inject crafted payloads into Redis that execute arbitrary code on deserialization.

**Remediation:**
```java
// BAD: Polymorphic deserialization
template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

// GOOD: Use StringRedisSerializer or Jackson2JsonRedisSerializer with explicit type
template.setValueSerializer(new StringRedisSerializer());
// OR
ObjectMapper mapper = new ObjectMapper();
mapper.activateDefaultTyping(
    mapper.getPolymorphicTypeValidator(),
    ObjectMapper.DefaultTyping.NON_FINAL
);
template.setValueSerializer(new Jackson2JsonRedisSerializer<>(mapper, Object.class));
```

**Fix Sprint:** Sprint 2 (before cache/blacklist methods go live)

---

## High Findings

### SEC-002: Hardcoded Database Credentials in VCS

| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **OWASP Category** | A02:2021 -- Cryptographic Failures |
| **Location** | `backend/src/main/resources/application.yml` (dev profile), `docker-compose.yml` |

**Description:** Database password `clarity_dev` is hardcoded in both `application.yml` and `docker-compose.yml`, committed to version control.

**Impact:** Credential exposure if repo is public or breached. Dev credentials may be reused in other environments.

**Remediation:** Use environment variables or `.env` file (gitignored) for all credentials, even in dev.
```yaml
# application.yml dev profile
spring:
  datasource:
    password: ${DB_PASSWORD:clarity_dev}  # env var with fallback
```

### SEC-003: Unauthenticated Redis Exposed on Host

| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **OWASP Category** | A05:2021 -- Security Misconfiguration |
| **Location** | `docker-compose.yml` |

**Description:** Redis runs without `requirepass` and is bound to host port 6379.

**Remediation:**
```yaml
redis:
  image: redis:7-alpine
  command: redis-server --requirepass ${REDIS_PASSWORD:-redis_dev}
  ports:
    - "127.0.0.1:6379:6379"  # Bind to localhost only
```

### SEC-004: @EnableMethodSecurity Missing

| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **OWASP Category** | A01:2021 -- Broken Access Control |
| **Location** | `backend/src/main/java/com/clarity/auth/infrastructure/security/SecurityConfig.java` |

**Description:** `@EnableMethodSecurity` annotation is missing. When `@PreAuthorize` annotations are added in Sprint 2, they will be **silently ignored** without this annotation.

**Remediation:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ADD THIS
public class SecurityConfig { ... }
```

### SEC-005: Weak Password Policy

| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **OWASP Category** | A07:2021 -- Authentication Failures |
| **Location** | `backend/src/main/java/com/clarity/auth/api/dto/RegisterRequest.java`, `ResetPasswordRequest.java` |

**Description:** Password validation only enforces `@Size(min = 8)`. No complexity requirements (uppercase, digit, special char). Allows weak passwords like `aaaaaaaa`.

**Remediation:** Add `@Pattern` annotation:
```java
@Size(min = 8, max = 128)
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
         message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
private String password;
```

---

## Medium Findings

### SEC-006: Missing Security Headers Filter

| Field | Value |
|-------|-------|
| **Severity** | Medium |
| **OWASP Category** | A05:2021 -- Security Misconfiguration |
| **Location** | `SecurityConfig.java` |

**Description:** No security headers configured (CSP, X-Frame-Options, X-Content-Type-Options, HSTS).

**Remediation:** Add to SecurityConfig:
```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .frameOptions(frame -> frame.deny())
    .contentTypeOptions(Customizer.withDefaults())
);
```

### SEC-007: CSRF Disabled Globally

| Field | Value |
|-------|-------|
| **Severity** | Medium |
| **OWASP Category** | A01:2021 -- Broken Access Control |
| **Location** | `SecurityConfig.java` |

**Description:** CSRF is disabled. Acceptable for stateless JWT API, but should be explicitly documented as intentional.

### SEC-008: No Rate Limiting on Auth Endpoints

| Field | Value |
|-------|-------|
| **Severity** | Medium |
| **OWASP Category** | A04:2021 -- Insecure Design |
| **Location** | `AuthController.java` |

**Description:** No rate limiting configured. Auth endpoints are vulnerable to brute force when implemented.

**Fix Sprint:** Sprint 2 (with auth implementation)

### SEC-009: Account Lockout Logic Not Yet Implemented

| Field | Value |
|-------|-------|
| **Severity** | Medium |
| **OWASP Category** | A07:2021 -- Authentication Failures |
| **Location** | `User.java` domain model |

**Description:** Domain model has lockout fields but business logic is placeholder (TODO stubs).

**Fix Sprint:** Sprint 2

### SEC-010: GlobalExceptionHandler May Leak Internal Details

| Field | Value |
|-------|-------|
| **Severity** | Medium |
| **OWASP Category** | A05:2021 -- Security Misconfiguration |
| **Location** | `GlobalExceptionHandler.java` |

**Description:** Verify that exception handler does not include stack traces or internal class names in error responses sent to clients.

### SEC-011: Docker Compose PostgreSQL Without SSL

| Field | Value |
|-------|-------|
| **Severity** | Medium |
| **OWASP Category** | A02:2021 -- Cryptographic Failures |
| **Location** | `docker-compose.yml` |

**Description:** Dev database connection does not use SSL. Acceptable for local dev, must enforce in production.

---

## Low Findings

### SEC-012: Missing Structured Logging Configuration

| Field | Value |
|-------|-------|
| **Severity** | Low |
| **OWASP Category** | A09:2021 -- Logging & Monitoring Failures |

**Description:** No explicit logging configuration for security events (login attempts, authorization failures).

### SEC-013: No Dependency Vulnerability Scanning in Dev

| Field | Value |
|-------|-------|
| **Severity** | Low |
| **OWASP Category** | A06:2021 -- Vulnerable Components |

**Description:** OWASP Dependency-Check is in CI but not configured for local dev builds.

### SEC-014: Frontend Auth Guard Returns True

| Field | Value |
|-------|-------|
| **Severity** | Low |
| **OWASP Category** | A01:2021 -- Broken Access Control |
| **Location** | `frontend/src/app/core/auth/auth.guard.ts` |

**Description:** Auth guard always returns `true`. Expected for Sprint 1, must be implemented before auth ships.

### SEC-015: No Content-Security-Policy in Angular

| Field | Value |
|-------|-------|
| **Severity** | Low |
| **OWASP Category** | A05:2021 -- Security Misconfiguration |
| **Location** | `frontend/src/index.html` |

**Description:** No CSP meta tag in Angular's index.html. Should be added alongside backend CSP header.

---

## Positive Observations

| # | Observation |
|---|-------------|
| 1 | BCrypt with cost factor 12 correctly configured in SecurityConfig |
| 2 | All JPA queries use parameterized parameters -- zero injection risk |
| 3 | Bean Validation (`@Valid`) present on all controller request bodies |
| 4 | CORS not using wildcard (`*`) -- properly restricted |
| 5 | Stack traces disabled (`server.error.include-stacktrace: never`) |
| 6 | Actuator limited to `health` and `info` endpoints |
| 7 | Frontend stores tokens in memory, not localStorage |
| 8 | No Angular `bypassSecurityTrust*` calls found |
| 9 | Production profile uses environment variables for all secrets |
| 10 | TruffleHog and OWASP Dependency-Check configured in CI pipeline |

---

## Deferred Items (By Design -- Sprint 1 Scaffold)

These are NOT findings -- they are intentional placeholders to be implemented in future sprints:

| Item | Current State | Target Sprint |
|------|--------------|---------------|
| SecurityConfig permits all requests | `anyRequest().permitAll()` | Sprint 2 (auth) |
| JWT authentication filter | Skeleton class | Sprint 2 |
| JwtTokenProvider | Placeholder | Sprint 2 |
| RedisTokenBlacklistService | Placeholder | Sprint 2 |
| AuthApplicationService | TODO stubs | Sprint 2 |
| Controllers | TODO stubs | Sprint 2-3 |
| Frontend auth guard | Returns true | Sprint 2 |
| Frontend auth interceptor | Passes through | Sprint 2 |

---

## OWASP Compliance Matrix

| Category | Status | Notes |
|----------|--------|-------|
| A01: Broken Access Control | Partially Compliant | SecurityConfig permits all (deferred). Missing `@EnableMethodSecurity`. |
| A02: Cryptographic Failures | Partially Compliant | BCrypt correct. Hardcoded dev credentials. No SSL on dev DB. |
| A03: Injection | Compliant | All queries parameterized. Bean Validation on all inputs. |
| A04: Insecure Design | Partially Compliant | No rate limiting yet. Account lockout fields exist but logic deferred. |
| A05: Security Misconfiguration | Partially Compliant | Missing security headers. CSRF disabled (acceptable for JWT API). |
| A06: Vulnerable Components | Compliant | CI pipeline has dependency scanning. |
| A07: Authentication Failures | N/A (deferred) | Auth is skeleton. Password policy needs strengthening. |
| A08: Data Integrity Failures | Non-Compliant | Unsafe Redis deserialization (SEC-001). |
| A09: Logging & Monitoring | Partially Compliant | Basic logging exists. No security event logging configured. |
| A10: SSRF | N/A | No server-side URL fetching in codebase. |

---

## Priority Action Items

1. **Before Sprint 2 ships:** Fix SEC-001 (Redis serializer), SEC-004 (`@EnableMethodSecurity`), SEC-005 (password policy)
2. **During Sprint 2:** Implement auth with rate limiting (SEC-008), account lockout (SEC-009), security headers (SEC-006)
3. **Sprint 3:** Add security event logging (SEC-012), frontend CSP (SEC-015)
4. **Ongoing:** Keep dependencies updated, run `mvn dependency-check:check` locally

---

*Generated by SDLC Factory -- Phase 7: Security Review*
*Reviewed by: Security Agent (Opus)*
