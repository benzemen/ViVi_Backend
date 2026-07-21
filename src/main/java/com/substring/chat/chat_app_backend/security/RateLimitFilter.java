package com.substring.chat.chat_app_backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j token-bucket algorithm.
 *
 * <p>Applies two tiers of rate limiting:</p>
 * <ul>
 *   <li><b>Per-IP</b>: Applied to unauthenticated requests (e.g., login attempts,
 *       public endpoints). Prevents brute-force and DDoS from a single source.</li>
 *   <li><b>Per-User</b>: Applied to authenticated requests. Prevents a single user
 *       from overwhelming the system (e.g., message spam).</li>
 * </ul>
 *
 * <p>When the rate limit is exceeded, the filter returns {@code 429 Too Many Requests}
 * with a {@code Retry-After} header.</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.auth-requests-per-minute:100}")
    private int authRequestsPerMinute;

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Bucket bucket;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // Authenticated user — per-user rate limit (higher limit)
            String username = auth.getName();
            bucket = userBuckets.computeIfAbsent(username, k -> createBucket(authRequestsPerMinute));
        } else {
            // Anonymous — per-IP rate limit (lower limit)
            String clientIp = getClientIp(request);
            bucket = ipBuckets.computeIfAbsent(clientIp, k -> createBucket(requestsPerMinute));
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                    "\"message\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private Bucket createBucket(int capacityPerMinute) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(capacityPerMinute, Duration.ofMinutes(1)))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
