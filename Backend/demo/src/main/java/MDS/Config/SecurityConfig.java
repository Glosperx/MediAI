package MDS.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.MediaType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Dezactiveaza protectia CSRF
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Lista de URL-uri publice care nu necesita autentificare
                        .requestMatchers(
                                "/",
                                "/custom-login",
                                "/register",
                                "/verify",
                                "/css/**",
                                "/api/auth/register",
                                "/api/auth/activate",
                                "/api/auth/create-admin/**"  // Adaugat /** pentru a prinde toate sub-path-urile
                        ).permitAll()
                        // Configurarea accesului bazat pe roluri
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/consultations/**").hasAnyAuthority("DOCTOR", "ADMIN")
                        .requestMatchers("/predict/**","/patient/history").hasAnyAuthority("PACIENT", "ADMIN")
                        // Toate celelalte cereri necesita autentificare
                        .anyRequest().authenticated()
                )
                // Configurarea procesului de login
                .formLogin(form -> form
                        .loginPage("/custom-login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")

                        // Gestionarea redirectionarilor dupa login in functie de rol
                        .successHandler((request, response, authentication) -> {
                            // Preia rolul userului
                            String role = authentication.getAuthorities().stream()
                                    .findFirst()
                                    .map(GrantedAuthority::getAuthority)
                                    .orElse("");

                            switch (role) {
                                case "ADMIN":
                                    response.sendRedirect("/consultations");
//                                    response.sendRedirect("/admin-dashboard");
                                    break;
                                case "DOCTOR":
                                    response.sendRedirect("/consultations");
                                    break;
                                case "PACIENT":
                                    response.sendRedirect("/predict");
                                    break;
                                default:
                                    response.sendRedirect("/custom-login?error=Invalid role");
                            }
                        })
                        .failureUrl("/custom-login?error=Invalid credentials")
                        .permitAll()
                )

                // Configurarea procesului de logout

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/custom-login")
//                        .logoutSuccessUrl("/custom-login?logout")
                                .invalidateHttpSession(true)
                                // Sterge cookie ul de sesiune la logout
                                .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Exceptii
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Tratare speciala pentru endpoint-ul de creare admin
                            if (request.getRequestURI().contains("/api/auth/create-admin")) {
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.setStatus(401);
                                response.getWriter().write("{\"error\": \"Unauthorized\"}");
                            } else {
                                // Redirectionare catre login pentru alte cereri neautorizate
                                response.sendRedirect("/custom-login");
                            }
                        })
                );

        return http.build();
    }

    // Criptarea parolelor
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//}