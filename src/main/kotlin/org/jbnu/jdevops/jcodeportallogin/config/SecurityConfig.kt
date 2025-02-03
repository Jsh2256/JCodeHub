package org.jbnu.jdevops.jcodeportallogin.config

import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import jakarta.servlet.http.HttpServletResponse

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource {
                    val configuration = CorsConfiguration()
                    configuration.allowedOrigins = listOf("http://localhost:3000")
                    configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    configuration.allowedHeaders = listOf("*")
                    configuration.exposedHeaders = listOf("Authorization")
                    configuration.allowCredentials = true
                    configuration
                }
            }
            .csrf { csrf -> csrf.disable() }
//            .exceptionHandling { exceptionHandling ->
//                exceptionHandling.authenticationEntryPoint { request, response, _ ->
//                    response.status = HttpServletResponse.SC_UNAUTHORIZED
//                }
//            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/signup", "/api/auth/login/basic").permitAll()
                    .requestMatchers("/oauth2/**", "/login/oidc/success").permitAll()
                    .requestMatchers("/api/users/student", "/api/users/assistant", "/api/users/professor").hasAuthority("ADMIN")
                    .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .defaultSuccessUrl("/api/auth/login/oidc/success", true)
                    .authorizationEndpoint {
                        it.baseUri("/oauth2/authorization/keycloak")
                    }
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login/basic")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            }
            .sessionManagement { sessionManagement ->
                sessionManagement.sessionFixation { sessionFixation ->
                    sessionFixation.migrateSession()  // 세션 고정 보호
                }
                sessionManagement.maximumSessions(1)  // 동시 세션 1개로 제한
            }
            .addFilterBefore(jwtAuthenticationFilter, OAuth2LoginAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun redirectFilter(): CustomRedirectFilter {
        return CustomRedirectFilter()
    }

    @Bean
    fun httpSessionListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(se: HttpSessionEvent) {
                se.session.maxInactiveInterval = 3600  // 1시간 (3600초)
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}