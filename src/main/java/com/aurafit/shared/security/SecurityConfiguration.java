package com.aurafit.shared.security;

import com.aurafit.shared.config.AuraFitProperties;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      SecurityProblemHandler securityProblemHandler)
      throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(
                        "/actuator/health/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/me")
                    .authenticated()
                    .requestMatchers("/api/v1/me/**")
                    .authenticated()
                    .requestMatchers("/api/v1/media/**")
                    .authenticated()
                    .requestMatchers("/api/v1/consents/**")
                    .authenticated()
                    .requestMatchers("/api/v1/reports/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(securityProblemHandler)
                    .accessDeniedHandler(securityProblemHandler))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .authenticationEntryPoint(securityProblemHandler)
                    .accessDeniedHandler(securityProblemHandler)
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .headers(
            headers ->
                headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions(frame -> frame.deny())
                    .contentSecurityPolicy(
                        policy ->
                            policy.policyDirectives("default-src 'none'; frame-ancestors 'none'")))
        .build();
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey jwtSecret) {
    return new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(jwtSecret));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey jwtSecret, AuraFitProperties properties) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(jwtSecret).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(
        JwtValidators.createDefaultWithIssuer(properties.security().jwt().issuer()));
    return decoder;
  }

  @Bean
  SecretKey jwtSecret(AuraFitProperties properties) {
    byte[] key = Base64.getDecoder().decode(properties.security().jwt().secret());
    if (key.length < 32) {
      throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes.");
    }
    return new SecretKeySpec(key, "HmacSHA256");
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName("roles");
    authoritiesConverter.setAuthorityPrefix("ROLE_");
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return converter;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.password.DelegatingPasswordEncoder(
        "bcrypt", java.util.Map.of("bcrypt", new BCryptPasswordEncoder(12)));
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(AuraFitProperties properties) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(properties.cors().allowedOrigins());
    configuration.setAllowedMethods(
        java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        java.util.List.of("Authorization", "Content-Type", "Idempotency-Key"));
    configuration.setExposedHeaders(java.util.List.of("Location", "ETag"));
    configuration.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
