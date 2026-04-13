# `@TestConfiguration` vs `@Configuration` in Spring Boot

## Overview

This document captures the behavior and design rationale behind `@TestConfiguration`
and how it differs from `@Configuration` when used with `@SpringBootTest`.

---

## How `@Configuration` Is Picked Up (Auto-detected ✅)

`@Configuration` is meta-annotated with `@Component`, so Spring's standard
`@ComponentScan` (triggered by `@SpringBootApplication`) picks it up automatically.

```
@SpringBootApplication
  → @ComponentScan
    → finds @Component / @Configuration ✅
```

---

## Why `@TestConfiguration` Is NOT Auto-detected (❌)

Spring Boot adds a **`TypeExcludeFilter`** to every `@ComponentScan`:

```java
// Inside @SpringBootApplication → @ComponentScan
excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class) }
```

This filter delegates to all `TypeExcludeFilter` beans registered in the context.
During testing, Spring Boot registers a **`TestTypeExcludeFilter`** which explicitly
excludes any class annotated with `@TestComponent` — and `@TestConfiguration` is
meta-annotated with `@TestComponent`:

```
@TestConfiguration
  → @TestComponent
    → @Component (BUT blocked by TestTypeExcludeFilter ❌)
```

---

## Why Was It Designed This Way?

This is **intentional protection**. If a top-level `@TestConfiguration` were
auto-scanned, it would bleed into **every** `@SpringBootTest` across the project,
not just the tests you intended. This leads to:

- Unintended bean overrides in unrelated tests
- Broken test isolation / flaky tests

By requiring explicit opt-in, you control exactly **which tests** get the
extra configuration.

---

## Correct Ways to Use `@TestConfiguration`

### Option 1 — Nested Static Class (auto-applied to that test class only)

No `@Import` needed. The nested class is automatically detected by `@SpringBootTest`.

```java
@SpringBootTest
class MyTests {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public UserDetailsService users() {
            // define test users...
        }
    }
}
```

### Option 2 — Top-level Class with Explicit `@Import` (recommended for reuse)

Define the configuration in a separate file and explicitly opt in per test class.

```java
// src/test/java/.../UserDetailsManagerConfiguration.java
@TestConfiguration
public class UserDetailsManagerConfiguration {

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user").password("password").roles("ATTENDEE").build();

        UserDetails speaker = User.withDefaultPasswordEncoder()
                .username("speaker").password("password").roles("SPEAKER").build();

        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin").password("password").roles("ADMIN").build();

        return new InMemoryUserDetailsManager(user, speaker, admin);
    }
}
```

```java
// src/test/java/.../MyTests.java
@SpringBootTest
@Import(UserDetailsManagerConfiguration.class)  // ← explicit opt-in
class MyTests {
    // ...
}
```

---

## Quick Reference Table

| Annotation | Auto-detected by `@ComponentScan`? | Reason |
|---|---|---|
| `@Configuration` | ✅ Yes | Just a `@Component`, no exclusion filter |
| `@TestConfiguration` | ❌ No | `TestTypeExcludeFilter` blocks `@TestComponent` classes |

---

## Bean Name Lookup in Tests

When verifying a bean exists in the application context, use the **`@Bean` method
name** (not the class name), since `@TestConfiguration` registers beans by their
method names:

```java
// UserDetailsManagerConfiguration.java defines: public UserDetailsService users() { ... }
// So the bean name is "users"

@Test
void context() {
    assertNotNull(context.getBean("users"));
}
```

---

## References

- [Spring Boot Testing Docs — Test Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.spring-boot-applications.detecting-configuration)
- [`TypeExcludeFilter` JavaDoc](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/context/TypeExcludeFilter.html)
- [`@TestConfiguration` JavaDoc](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/context/TestConfiguration.html)
