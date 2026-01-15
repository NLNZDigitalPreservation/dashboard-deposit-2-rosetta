package nz.govt.natlib.dashboard.app;


import nz.govt.natlib.dashboard.common.auth.HttpAccessManagementFilter;
import nz.govt.natlib.dashboard.common.auth.Sessions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class MainSecurityConfig {

    private final Sessions sessions;

    public MainSecurityConfig(Sessions sessions) {
        this.sessions = sessions;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                ).headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                ).addFilterBefore(
                        httpAccessManagementFilter(),
                        AnonymousAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public HttpAccessManagementFilter httpAccessManagementFilter() {
        HttpAccessManagementFilter filter = new HttpAccessManagementFilter();
        filter.setSessions(sessions);
        return filter;
    }
}
