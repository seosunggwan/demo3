package com.example.backend.common.config.security;

import com.example.backend.security.customhandler.CustomFormSuccessHandler;
import com.example.backend.security.customhandler.CustomLogoutFilter;
import com.example.backend.security.customhandler.CustomOAuth2SuccessHandler;
import com.example.backend.security.jwt.JWTFilter;
import com.example.backend.security.jwt.JWTUtil;
import com.example.backend.security.repository.RefreshRepository;
import com.example.backend.security.service.RefreshTokenService;
import com.example.backend.security.service.form.CustomUserDetailsService;
import com.example.backend.security.service.oauth2.CustomOAuth2UserService;
import com.example.backend.security.service.oauth2.OAuthUserEntityToUserEntityService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshRepository refreshRepository;
    private final CustomUserDetailsService customUserDetailsService; // ✅ 사용자 정보 조회 서비스 추가
    private final OAuthUserEntityToUserEntityService oAuthUserEntityToUserEntityService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // AppConfig에서 주입받기

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder); // 주입받은 빈 사용
        return new ProviderManager(provider);
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("exception = " + exception);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized: " + exception.getMessage());
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable());

        http
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // ✅ 로그인 API 설정
                        .usernameParameter("email") // ✅ username 대신 email 사용
                        .passwordParameter("password")
                        .successHandler(new CustomFormSuccessHandler(jwtUtil, refreshTokenService))
                        .failureHandler(authenticationFailureHandler())
                        .permitAll());

        http
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userinfo -> userinfo.userService(customOAuth2UserService))
                        .successHandler(new CustomOAuth2SuccessHandler(jwtUtil, refreshTokenService, oAuthUserEntityToUserEntityService))
                        .failureHandler(authenticationFailureHandler())
                        .permitAll());

        http
                .logout(auth -> auth
                        .logoutUrl("/auth/logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("refresh_token")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logged out successfully");
                        })
                );

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie", "access_token"));
                    return configuration;
                }));

        http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/", 
                        "/login", 
                        "/join",
                        "/oauth2-jwt-header", 
                        "/connect/**",
                        "/topic/**",
                        "/app/**",
                        "/ws/**",
                        "/publish/**",
                        "/health",
                        "/api/items/image",
                        "/api/auth/refresh"
                    ).permitAll()
                    .requestMatchers("/admin").hasAuthority("ADMIN")
                    .anyRequest().authenticated()
                );
            

        http
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized Access");
                        }));

        http    
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);


        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // ✅ JWT 기반 인증을 위해 STATELESS 모드 설정

        return http.build();
    }
}
