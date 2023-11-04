package foo.configurations;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Supplier;

@Configuration
@Slf4j
public class CsrfConfiguration {

    @Bean(name = "xorCsrfTokenRequestHandler")
    public CsrfTokenRequestHandler getDelegateHandler() {
        return new XorCsrfTokenRequestAttributeHandler();
    }

    @Bean("customCsrfHandel")
    public CsrfTokenRequestAttributeHandler getSpaCsrfTokenRequestHandler(){
        CsrfTokenRequestHandler delegate = getDelegateHandler();

        return new CsrfTokenRequestAttributeHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
                delegate.handle(request, response, csrfToken);
            }

            @Override
            public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
                if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                    return super.resolveCsrfTokenValue(request, csrfToken);
                }
                return delegate.resolveCsrfTokenValue(request, csrfToken);
            }
        };
    }

    @Bean(name = "csrfCookieFilter")
    public OncePerRequestFilter getCsrfCookieFilter() {

        return new OncePerRequestFilter(){
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
                csrfToken.getToken();

                filterChain.doFilter(request, response);
            }
        };
    }
}
