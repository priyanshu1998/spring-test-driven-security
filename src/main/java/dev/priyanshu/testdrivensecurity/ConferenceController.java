package dev.priyanshu.testdrivensecurity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConferenceController {

  private String aboutConference = "Join us online September 1–2!";

  @GetMapping("/about")
  public String getAbout() {
    return this.aboutConference;
  }

  @PostMapping("/about")
  public void updateAbout(@RequestBody String updateAbout) {
    this.aboutConference = updateAbout;
  }

  @GetMapping("/greeting")
  public String greeting(@AuthenticationPrincipal(expression = "username") String username) {
    return "Hello, " + username + "!";
  }

  @GetMapping("/support")
  public String console() {
    return "This page should be accessible by support folk only";
  }
}
