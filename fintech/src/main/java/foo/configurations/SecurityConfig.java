package foo.configurations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.security.SecureRandom;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    private final CsrfTokenRequestHandler spaCsrfTokenRequestHandler;

    private final OncePerRequestFilter csrfTokenFilter;

    public SecurityConfig(@Qualifier("customCsrfHandel") CsrfTokenRequestAttributeHandler spaCsrfTokenRequestHandler,
                          @Qualifier("csrfCookieFilter") OncePerRequestFilter csrfTokenFilter) {
        this.spaCsrfTokenRequestHandler = spaCsrfTokenRequestHandler;
        this.csrfTokenFilter = csrfTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
                )
                .addFilterAfter(csrfTokenFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authorize ->
                    authorize
                            .requestMatchers(antMatcher(HttpMethod.POST,"/registration/**")).permitAll()
                            .requestMatchers(antMatcher(HttpMethod.POST,"/registration")).permitAll()
                            .requestMatchers(antMatcher(HttpMethod.GET)).hasAnyRole("USER", "ADMIN")
                            .anyRequest().hasRole("ADMIN"))
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8, new SecureRandom());
    }

}
