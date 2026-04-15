package dev.priyanshu.testdrivensecurity;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain supportSecurityFilterChain(HttpSecurity http) {
    http.securityMatcher("/support")
        .authorizeHttpRequests(authz -> authz.anyRequest().hasAuthority("SCOPE_support"))
        .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.httpBasic(withDefaults())
        //                .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(HttpMethod.GET, "/about")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/about")
                    .hasRole("ADMIN")
                    .requestMatchers("/submissions")
                    .hasRole("SPEAKER")
                    .anyRequest()
                    .authenticated());
    return http.build();
  }
}
