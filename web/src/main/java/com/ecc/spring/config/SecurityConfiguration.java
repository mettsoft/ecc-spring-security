package com.ecc.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecc.spring.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/users/**").hasRole("ADMIN")
                .antMatchers("/persons/create").hasRole("CREATE_PERSON")
                .antMatchers("/persons/upload").hasRole("CREATE_PERSON")
                .antMatchers("/persons/update").hasRole("UPDATE_PERSON")
                .regexMatchers("/persons\\?.*id=.*").hasRole("UPDATE_PERSON")
                .antMatchers("/persons/delete").hasRole("DELETE_PERSON")
                .antMatchers("/roles/create").hasRole("CREATE_ROLE")
                .antMatchers("/roles/update").hasRole("UPDATE_ROLE")
                .regexMatchers("/roles\\?.*id=.*").hasRole("UPDATE_ROLE")
                .antMatchers("/roles/delete").hasRole("DELETE_ROLE")
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}