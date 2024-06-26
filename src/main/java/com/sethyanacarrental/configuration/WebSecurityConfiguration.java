package com.sethyanacarrental.configuration;


import com.sethyanacarrental.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                authorizeRequests()
                .antMatchers("/").permitAll() //antMatchers: what is the service pattern
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/image/**").permitAll()
                .antMatchers("/vendor/**").permitAll()
                .antMatchers("/login/**").permitAll()
                .antMatchers("/forgotpassword").permitAll()

                .antMatchers("/customer/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST", "DRIVER") //**-any service after customer would work(findAll,crud) // role name match according to db
                .antMatchers("/vehicle/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST")
                .antMatchers("/package/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST")
                .antMatchers("/driver/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST")
                .antMatchers("/driverPortal/**").hasAnyAuthority("ADMIN", "MANAGER", "DRIVER")
                .antMatchers("/cd_reservation/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST", "DRIVER")
                .antMatchers("/sd_reservation/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST")
                .antMatchers("/customer_payment/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST", "DRIVER")
                .antMatchers("/report/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST")
                .antMatchers("/notification/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST")

                .antMatchers("/user/**", "/employee/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST", "DRIVER")
                .antMatchers("/mainwindow").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST", "DRIVER")
                .antMatchers("/privilege/**").hasAnyAuthority("ADMIN", "MANAGER", "RECEPTIONIST", "DRIVER").anyRequest().authenticated()
                .and().csrf().disable().formLogin()
                .loginPage("/login")
                .failureHandler((request, response, exception) -> {
                    System.out.println(exception.getMessage());
                    System.out.println(response.getStatus());
                    String redirectUrl = "";
                    if (exception.getMessage() == "User is disabled") {
                        redirectUrl = request.getContextPath() + "/login?error=notactive";
                    } else if (exception.getMessage() == "Bad credentials") {
                        redirectUrl = request.getContextPath() + "/login?error=detailserr";
                    } else if (exception.getMessage() == null) {
                        redirectUrl = request.getContextPath() + "/login?error=detailserr";
                    }
                    response.sendRedirect(redirectUrl);
                })
                .defaultSuccessUrl("/mainwindow", true)
                .usernameParameter("username")
                .passwordParameter("password")
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login").and().exceptionHandling().accessDeniedPage("/access-denied").and()
                .sessionManagement()
                .invalidSessionUrl("/login")
                .sessionFixation()
                .changeSessionId()
                .maximumSessions(6)
                .expiredUrl("/login").maxSessionsPreventsLogin(true);
        http.headers()
                .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }
/*    @Bean
    public ViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setPrefix("/resources/**");
        bean.setSuffix(".html");
        return bean;
    }*/
}
