package com.grimgate.grimgate_backend.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Security 설정
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final StringRedisTemplate redisTemplate;

    // 비밀번호 암호화에 사용할 BCrypt 인코더
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (STATELESS)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CORS는 WebConfig 설정 따름
                .cors(cors -> cors.configure(http))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register/member",
                                "/api/auth/register/manager",
                                "/api/auth/login/member",
                                "/api/auth/login/manager",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                "/api/auth/oauth/google",
                                "/api/auth/password/reset-request",
                                "/api/auth/password/reset",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")
                        .requestMatchers("/api/member/**").hasRole("MEMBER")
                        .anyRequest().authenticated()
                )

                // 인증/권한 예외 처리 핸들러 연결
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .formLogin(form -> form.disable())

                // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(new JwtFilter(jwtProvider, customUserDetailsService, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
