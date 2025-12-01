package com.intiwasi.platform.iam.infrastructure.authorization.sfs.configuration;

import com.intiwasi.platform.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.intiwasi.platform.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.intiwasi.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value; // Importación necesaria
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web Security Configuration.
 * <p>
 * This class is responsible for configuring the web security.
 * It enables the method security and configures the security filter chain.
 * It includes the authentication manager, the authentication provider, the password encoder and the authentication entry point.
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final BearerTokenService tokenService;
    private final BCryptHashingService hashingService;
    private final AuthenticationEntryPoint unauthorizedRequestHandler;

    // INYECCIÓN DE VARIABLES DE ENTORNO PARA CORS Y SWAGGER
    @Value("${CORS_ALLOWED_ORIGINS:*}") // Origen permitido, usa '*' como fallback
    private String corsAllowedOrigins;
    
    @Value("${ENABLE_SWAGGER:true}") // Controla si Swagger está habilitado
    private boolean enableSwagger;
    
    public WebSecurityConfiguration(@Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService, BearerTokenService tokenService, BCryptHashingService hashingService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
    }

    @Bean
    public BearerAuthorizationRequestFilter authorizationRequestFilter() {
        return new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(hashingService);
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return hashingService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // --- CONFIGURACIÓN DE CORS OPTIMIZADA ---
        http.cors(configurer -> configurer.configurationSource(_ -> {
            var cors = new CorsConfiguration();
            
            // Si la variable de entorno es "*", permite todos. 
            // Si es una lista separada por comas, la parsea.
            if ("*".equals(corsAllowedOrigins)) {
                cors.setAllowedOrigins(List.of("*"));
            } else {
                List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                cors.setAllowedOrigins(origins);
            }
            
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
            cors.setAllowCredentials(true); // Necesario si usa cookies/sesiones (aunque aquí no)
            return cors;
        }));
        // ----------------------------------------

        http.csrf(csrfConfigurer -> csrfConfigurer.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedRequestHandler))
                .sessionManagement( customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // Configuración de rutas
        if (enableSwagger) {
            // Incluye rutas de Swagger si está habilitado
            http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(
                    "/api/v1/authentication/**", 
                    "/api/auth/authentication/**", 
                    "/api/v1/authentication/sign-in",
                    "/api/v1/authentication/sign-up",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**").permitAll()
                .anyRequest().authenticated());
        } else {
            // Solo incluye rutas de autenticación si Swagger está deshabilitado
            http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(
                    "/api/v1/authentication/**", 
                    "/api/auth/authentication/**", 
                    "/api/v1/authentication/sign-in",
                    "/api/v1/authentication/sign-up").permitAll()
                .anyRequest().authenticated());
        }


        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }
}
