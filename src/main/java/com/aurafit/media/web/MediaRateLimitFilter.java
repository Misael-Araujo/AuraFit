package com.aurafit.media.web;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
public class MediaRateLimitFilter extends OncePerRequestFilter {

  private static final int CAPACITY = 10;

  private final Cache<String, Bucket> buckets;
  private final ObjectMapper objectMapper;

  public MediaRateLimitFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.buckets =
        Caffeine.newBuilder().maximumSize(100_000).expireAfterAccess(1, TimeUnit.HOURS).build();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api/v1/media")
        || !"POST".equals(request.getMethod());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String clientAddress = request.getRemoteAddr();
    Bucket bucket =
        buckets.get(
            clientAddress,
            key ->
                Bucket.builder()
                    .addLimit(
                        Bandwidth.classic(
                            CAPACITY, Refill.intervally(CAPACITY, Duration.ofMinutes(1))))
                    .build());

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
      return;
    }

    response.setStatus(429);
    response.setContentType("application/problem+json");
    response.setHeader("Retry-After", "60");
    objectMapper.writeValue(
        response.getOutputStream(),
        Map.of(
            "type",
            "urn:aurafit:problem:rate_limit_exceeded",
            "title",
            "Too Many Requests",
            "status",
            429,
            "detail",
            "Upload request limit exceeded.",
            "code",
            "rate_limit_exceeded",
            "instance",
            request.getRequestURI()));
  }
}
