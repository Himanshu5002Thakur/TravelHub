package com.travel.config;

import com.travel.entity.User;
import com.travel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;  // Auto-wire your repo

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
public UserDetailsService userDetailsService() {
    return email -> {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail()) // Sets email as the principal name
            .password(user.getPassword())
            .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    };
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // Publicly accessible pages
            .requestMatchers("/", "/login", "/signup", "/home", "/about", "/contact", "/css/**", "/js/**", "/images/**").permitAll()
            // Everything else (Booking, My Bookings, etc.) requires Login
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/home", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout=true")
            .permitAll()
        );
    return http.build();
}
}