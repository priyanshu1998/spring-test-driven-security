## Two Ways to Inject a Mock User in Spring Security Tests

Both `testGreetingUser` and `testWithMockUser` are testing the same secured endpoint — `GET /greeting` — and both expect an `HTTP 200 OK` response with a personalised greeting. The `/greeting` endpoint itself simply reads the authenticated principal's username:

```java
@GetMapping("/greeting")
public String greeting(@AuthenticationPrincipal(expression = "username") String username) {
    return "Hello, " + username + "!";
}
```

Because `SecurityConfig` marks every unmatched request as `.anyRequest().authenticated()`, hitting this endpoint without credentials produces a `401 Unauthorized`. The two tests solve the "how do I supply a user?" problem in fundamentally different ways.

---

### `testGreetingUser` — Request-level post-processor

```java
this.mockMvcTester.get()
        .uri("/greeting")
        .with(user("Ria"))   // <-- applied per-request
```

`.with(user("Ria"))` is a **MockMvc request post-processor** imported from Spring Security's test support. It injects a mock `Authentication` object directly into the `SecurityContext` at the moment the request is dispatched, and clears it immediately afterwards. This approach is entirely programmatic and lives alongside the request builder chain, which makes it very flexible — you can vary the username, roles, or other attributes on a per-call basis without touching the test method signature.

---

### `testWithMockUser` — Annotation-based approach

```java
@Test
@WithMockUser(value = "Gia")
void testWithMockUser() {
    this.mockMvcTester.get().uri("/greeting")   // no .with(...) needed
```

`@WithMockUser` is a **test-class-level annotation** processed by Spring's `TestExecutionListener` infrastructure. Before the test method body even runs, it populates the `SecurityContext` with a mock user named `"Gia"`. Because the context is already populated, the request chain needs no `.with(...)` call at all.

---

### Key differences at a glance

| | `testGreetingUser` | `testWithMockUser` |
|---|---|---|
| **Mechanism** | Request post-processor (`.with(user(...))`) | Annotation (`@WithMockUser`) |
| **When context is set** | At request dispatch time | Before the test method runs |
| **Where the username lives** | Inside the request builder chain | In the annotation attribute |
| **Flexibility** | Easy to pass different users per request | Best for a single user per test method |

---

### The bigger picture

These tests sit inside the full Spring Security filter chain because `MockMvcTester` is built with `.apply(springSecurity())` in `@BeforeEach`:

```java
mockMvcTester = MockMvcTester.from(context,
        builder -> builder.apply(springSecurity()).build());
```

This means every request actually passes through `SecurityFilterChain`, making the tests realistic. The companion test `testUnauthorizedUser` confirms this by sending a request with *no* credentials and asserting `401 Unauthorized` — so the two mock-user tests are specifically proving that when a valid identity *is* present the endpoint returns the correct, personalised greeting.

Together, the three `/greeting` tests form a triangle: unauthorized access is blocked, a user injected at request time gets their name reflected back, and a user injected via annotation also gets their name reflected back — covering both common ways you'll authenticate users in Spring Security test code.

