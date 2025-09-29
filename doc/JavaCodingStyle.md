# Java Coding Style Guidelines

These guidelines keep the DemoRABC codebase consistent and easy to maintain. They extend the core [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with Spring Boot conventions used in this project.

## 1. Source File Structure
- Use UTF-8 encoding and Unix line endings.
- Keep one top-level class per file; records are fine, but avoid multiple public types.
- Order members: static fields, static methods, instance fields, constructors, public methods, protected, package-private, private helpers.
- Place Lombok annotations directly above the declaration they modify and prefer `@RequiredArgsConstructor` over `@Autowired` on fields.

## 2. Naming Conventions
- Classes: `PascalCase` (`ActivityLogController`).
- Methods and variables: `camelCase` (`logActivity`, `pageSize`).
- Constants: `UPPER_SNAKE_CASE` and mark them `static final`.
- Spring beans: let Spring infer bean names from class names; avoid explicit `@Component("name")` unless necessary.
- DTOs/records: suffix with `Dto` and keep them immutable.
- Repository methods: use descriptive names that clearly indicate what is being queried:
  - Good: `findActivityLogsByUsernameAndDateRange`, `countLoginFailuresSince`
  - Avoid: `findByFilters`, `getData`, `search`

## 3. Formatting Basics
- Indent with four spaces; never use tabs.
- Limit lines to 110 characters (extended from Google's 100 to accommodate longer Spring annotations).
  - This deviation is acceptable for this project to reduce line wrapping in annotation-heavy code.
- Always use braces, even for single-line `if` statements.
- Align chained builder calls one per line:
  ```java
  ActivityLog log = ActivityLog.builder()
      .username(username)
      .actionType(actionType)
      .build();
  ```

## 4. Imports and Dependencies
- Use explicit imports; never wildcard except for static inner classes in tests when it improves readability.
- Keep import blocks grouped: Java, javax/jakarta, third-party, `org.springframework`, then project packages. Alphabetize within each group.
- Prefer constructor injection for Spring components (Lombok can generate constructors).

## 5. Nullability and Optionals
- **Repository methods:** Always return `Optional<T>` for single-value lookups that may not find results:
  ```java
  Optional<User> findByUsername(String username);
  ```
- **Service methods:** Return `Optional<T>` when a not-found result is a valid outcome, not an error.
- **DTOs and entity fields:** Never use `Optional` as a field type; use `null` for absent values.
- **Controller parameters:** Guard against `null` from HTTP requests:
  ```java
  // Clean null/empty strings in service layer before passing to repository
  String cleanUsername = (username != null && username.trim().isEmpty()) ? null : username;
  ```
- Prefer validation annotations (`@NotNull`, `@NotBlank`) over manual null checks in controllers.

## 6. Logging and Exceptions
- Use SLF4J (`log.info`, `log.warn`, `log.error`).
- Log contextual data, not stack traces, unless you are handling an unexpected exception.
- **Exception handling strategy:**
  - Use unchecked exceptions (RuntimeException) for business logic errors
  - Catch specific exceptions, not generic `Exception`
  - Example:
    ```java
    try {
        activityLogService.logActivity(...);
    } catch (Exception e) {
        log.error("Failed to log activity for user {}: {}", username, e.getMessage());
        // Don't fail the main operation due to logging failure
    }
    ```
- Wrap low-level exceptions in domain-specific exceptions when rethrowing.
- Do not swallow exceptions silently; at minimum, log them with enough detail to diagnose issues.

## 7. Spring MVC and REST Patterns
- **Thin controllers:** Delegate all business logic to services. Controllers should only:
  - Map HTTP requests to method calls
  - Validate input
  - Return appropriate responses
- **Shared model attributes:** Use `@ControllerAdvice` with `@ModelAttribute` to centralize repeated data:
  ```java
  @ControllerAdvice
  public class GlobalControllerAdvice {
      @Autowired
      private FunctionService functionService;
      
      @ModelAttribute("groups")
      public List<FunctionGroup> addFunctionGroups(Principal principal) {
          return functionService.getGroupsForUser(principal.getName());
      }
      
      @ModelAttribute("functions")
      public List<Function> addFunctions(Principal principal) {
          return functionService.getFunctionsForUser(principal.getName());
      }
  }
  ```
- Return DTOs instead of entities; keep entity knowledge in the persistence layer.
- Validate inputs using Bean Validation annotations (`@Valid`, `@NotBlank`); handle validation failures with `@ExceptionHandler` in `@ControllerAdvice`.

## 8. Transactions and Persistence
- Mark read-only service methods with `@Transactional(readOnly = true)` to signal intent.
- Repository methods should describe what they fetch (`findByUsernameAndActionType`, `countActiveUsersInDateRange`).
- **Native query documentation:** Always document column mapping when using native SQL:
  ```java
  @Query(value = "SELECT id, username, action_type, created_at, ip_address, user_agent " +
                 "FROM activity_logs WHERE username = :username " +
                 "ORDER BY created_at DESC",
         nativeQuery = true)
  // Returns: [0]=id, [1]=username, [2]=action_type, [3]=created_at, [4]=ip_address, [5]=user_agent
  List<Object[]> findRawActivityLogsByUsername(@Param("username") String username);
  ```
- **Type casting in native queries:** When PostgreSQL cannot infer types, use explicit casts:
  ```java
  "((CAST(:startDate AS timestamp)) IS NULL OR a.created_at >= :startDate)"
  ```
- Avoid direct `EntityManager` usage in controllers; keep it in repositories or dedicated services.

## 9. Collection and Stream Usage
- Prefer `List` over `Set` unless duplicates must be prevented.
- When using streams, keep lambda bodies short. Extract complex logic to private methods.
- Convert to immutable collections before exposing outside the service layer (e.g., `List.copyOf`).

## 10. Testing Conventions
- Name tests `<ClassName>Test` or `<Feature>IT` for integration tests.
- Follow Arrange-Act-Assert structure; keep each test focused on one behavior.
- Use `@DataJpaTest` for repository specs and `@SpringBootTest` only when the full context is required.

## 11. Documentation
- Add Javadoc to public components that are part of the application API (controllers, service interfaces).
- Use short comments to explain non-obvious decisions; avoid narrating obvious code.
- Document workarounds for database-specific issues:
  ```java
  // PostgreSQL requires explicit type casting when comparing null timestamps
  // See: https://github.com/pgjdbc/pgjdbc/issues/1234
  ```

## 12. Security Best Practices
- **SQL injection prevention:**
  - Always use parameterized queries with `@Query` and `@Param`
  - Never concatenate user input into SQL strings
- **Sensitive data handling:**
  - Filter passwords and tokens before logging:
    ```java
    private boolean isSensitiveParameter(String paramName) {
        return paramName.toLowerCase().contains("password") ||
               paramName.toLowerCase().contains("token");
    }
    ```
- **Input validation:** Validate and sanitize all external input at controller boundaries

## 13. API Design (Future Consideration)
- When exposing REST APIs, version endpoints (`/api/v1/...`)
- Use proper HTTP status codes (200, 201, 400, 404, 500)
- Return consistent error response format across all endpoints

## 14. Git Hygiene
- Keep commits focused on one logical change.
- Run formatting and tests before committing.
- Update documentation/configuration alongside the code it describes.

Adhering to these rules minimizes merge conflicts, clarifies intent, and keeps the DemoRABC service maintainable as it grows.