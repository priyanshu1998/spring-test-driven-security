package dev.priyanshu.testdrivensecurity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(UserDetailsManagerConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestDrivenSecurityApplicationTests {

  @Autowired WebApplicationContext context;

  MockMvcTester mockMvcTester;

  @BeforeEach
  public void setup() {
    mockMvcTester = MockMvcTester.from(context, builder -> builder.apply(springSecurity()).build());
  }

  @Test
  void testConferenceInfo() {
    Assertions.assertThat(this.mockMvcTester.get().uri("/about"))
        .hasStatus(HttpStatus.OK)
        .bodyText()
        .contains("Join us online September 1–2!");
  }

  @Test
  void testGreetingUser() {
    Assertions.assertThat(this.mockMvcTester.get().uri("/greeting").with(user("Ria")))
        .hasStatus(HttpStatus.OK)
        .bodyText()
        .contains("Hello, Ria!");
  }

  @Test
  @WithMockUser(value = "Gia")
  void testWithMockUser() {
    Assertions.assertThat(this.mockMvcTester.get().uri("/greeting"))
        .hasStatus(HttpStatus.OK)
        .bodyText()
        .contains("Hello, Gia!");
  }

  @Test
  void testUnauthorizedUser() {
    Assertions.assertThat(this.mockMvcTester.get().uri("/greeting"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void updateAboutWhenUserIsAdmin() {
    Assertions.assertThat(
            this.mockMvcTester
                .post()
                .uri("/about")
                .contentType(MediaType.TEXT_PLAIN)
                .content("this is the best conference")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void updateAboutWhenUserIsAdmin403() {
    Assertions.assertThat(
            this.mockMvcTester
                .post()
                .uri("/about")
                .contentType(MediaType.TEXT_PLAIN)
                .content("this is the best conference")
                .with(user("admin").roles("ADMIN")))
        .hasStatus(HttpStatus.FORBIDDEN);
  }

  @Test
  void updateAboutWhenUnauthenticatedUserReturn401() {
    Assertions.assertThat(
            this.mockMvcTester
                .post()
                .uri("/about")
                .contentType(MediaType.TEXT_PLAIN)
                .content("I will change the agenda!!!")
                .with(csrf()))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void postAboutWithoutCsrfThenReturns403() {
    Assertions.assertThat(this.mockMvcTester.post().uri("/about")).hasStatus(HttpStatus.FORBIDDEN);
  }
}
