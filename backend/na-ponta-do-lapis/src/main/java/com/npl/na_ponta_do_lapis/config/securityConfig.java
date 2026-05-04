package com.npl.na_ponta_do_lapis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class securityConfig {


    
    @Bean
    public PasswordEncoder passwordEncoder(){
       return new BCryptPasswordEncoder();
    }
}
