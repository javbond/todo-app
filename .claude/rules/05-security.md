# Security Rules

## OWASP Top 10 Prevention

### A01: Broken Access Control
- Deny by default — explicit role-based access on every endpoint
- Use Spring Security `@PreAuthorize("hasRole('ADMIN')")` annotations
- Validate object-level authorization (user can only access their own data)
- Disable directory listing, remove default credentials

### A02: Cryptographic Failures
- TLS 1.2+ for all data in transit
- AES-256 for data at rest
- BCrypt (cost factor 12+) for password hashing — never MD5/SHA1
- No secrets in code or config files — use environment variables or vault

### A03: Injection
- Use parameterized queries / JPA named parameters (never string concatenation)
- Validate and sanitize all user inputs at the boundary
- Use `@Valid` with Bean Validation on all request DTOs
- Escape output based on context (HTML, JS, URL, SQL)

### A04: Insecure Design
- Threat model during HLD phase
- Rate limiting on authentication and sensitive endpoints
- Business logic validation in domain layer (not just UI)

### A05: Security Misconfiguration
- Remove default Spring Boot actuator endpoints in production
- Disable stack traces in error responses (`server.error.include-stacktrace=never`)
- Configure CORS explicitly (no wildcard `*` in production)
- Security headers: CSP, X-Frame-Options, X-Content-Type-Options, HSTS

### A06: Vulnerable Components
- Dependabot enabled for automated dependency updates
- OWASP Dependency-Check in CI pipeline
- Pin dependency versions (no ranges)
- Regular npm audit / Maven dependency:tree review

### A07: Authentication Failures
- JWT tokens with short expiry (15 min access, 7 day refresh)
- Refresh token rotation — invalidate old refresh token on use
- Account lockout after 5 failed attempts (15 min cooldown)
- Multi-factor authentication for admin operations

### A08: Data Integrity Failures
- Verify signatures on JWTs (RS256 preferred over HS256)
- Validate all deserialized data (no blind object mapping)
- CI/CD pipeline integrity — no manual deployments

### A09: Logging & Monitoring
- Log all authentication events (login, logout, failed attempts)
- Log all authorization failures
- Never log sensitive data (passwords, tokens, PII)
- Structured logging with correlation IDs
- Alert on anomalous patterns (brute force, privilege escalation)

### A10: SSRF
- Validate and whitelist all URLs for server-side requests
- Block internal network ranges (169.254.x.x, 10.x.x.x, etc.)
- Use URL parsers, not regex, for URL validation

## Spring Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // JWT authentication filter
    // CORS configuration
    // CSRF protection (stateless = disabled, stateful = enabled)
    // Session management: STATELESS for API
    // Authorization rules per endpoint pattern
}
```

## Angular Security
- Sanitize user-generated content (Angular DomSanitizer)
- Use `HttpOnly`, `Secure`, `SameSite` flags on cookies
- Content Security Policy headers
- No sensitive data in localStorage (use HttpOnly cookies or memory)
- Angular's built-in XSS protection (template interpolation auto-escapes)

## API Security Checklist
- [ ] All endpoints require authentication (except health/public)
- [ ] Rate limiting configured (100 req/min per user)
- [ ] Input validation on all request bodies
- [ ] Response does not expose internal details (no stack traces, internal IDs)
- [ ] CORS configured for specific origins only
- [ ] Security headers set (CSP, HSTS, X-Frame-Options)
- [ ] JWT validation (signature, expiry, issuer, audience)
- [ ] Sensitive operations require re-authentication
