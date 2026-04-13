package dev.priyanshu.testdrivensecurity;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class UserDetailsManagerConfiguration {

  @Bean
  public UserDetailsService users() {
    UserDetails user =
        User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("ATTENDEE")
            .build();

    UserDetails speaker =
        User.withDefaultPasswordEncoder()
            .username("speaker")
            .password("password")
            .roles("SPEAKER")
            .build();

    UserDetails admin =
        User.withDefaultPasswordEncoder()
            .username("admin")
            .password("password")
            .roles("ADMIN")
            .build();

    return new InMemoryUserDetailsManager(user, speaker, admin);
  }
}
