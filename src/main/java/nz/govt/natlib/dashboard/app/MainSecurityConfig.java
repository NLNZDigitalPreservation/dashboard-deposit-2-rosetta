package nz.govt.natlib.dashboard.app;


import nz.govt.natlib.dashboard.common.auth.HttpAccessManagementFilter;
import nz.govt.natlib.dashboard.common.auth.Sessions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class MainSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private Sessions sessions;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/").anonymous()
//                .antMatchers("/index.html").anonymous()
//                .antMatchers(RequestPathConstants.PATH_ROOT_RESTFUL + "/**").anonymous()
//                .anyRequest().permitAll()
//                .and()
//                .logout()
//                .invalidateHttpSession(true)
//                .logoutSuccessUrl(RequestPathConstants.PATH_USER_LOGOUT);
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
        http.addFilterBefore(httpAccessManagementFilter(), AnonymousAuthenticationFilter.class);
        http.headers().frameOptions().sameOrigin();
    }

//    @Bean
//    public Sessions sessions() {
//        Sessions bean = new Sessions();
//        return bean;
//    }

    @Bean
    public HttpAccessManagementFilter httpAccessManagementFilter() {
        HttpAccessManagementFilter bean = new HttpAccessManagementFilter();
        bean.setSessions(sessions);
        return bean;
    }

//    @Bean
//    public LdapAuthenticationClient authClient() {
//        return new LdapAuthenticationClient();
//    }
}
