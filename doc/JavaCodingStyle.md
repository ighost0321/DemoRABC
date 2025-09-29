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

## 3. Formatting Basics
- Indent with four spaces; never use tabs.
- Limit lines to 110 characters; wrap fluent calls and long arguments onto new lines with an extra indent.
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
- Favor `Optional<T>` for repository lookups returning a single value. Do not return `Optional` in DTOs or entity fields.
- Guard against `null` from external sources (HTTP parameters, native queries).
- Avoid `@Nullable` parameters in controllers; prefer validation annotations or default values.

## 6. Logging and Exceptions
- Use SLF4J (`log.info`, `log.warn`, `log.error`).
- Log contextual data, not stack traces, unless you are handling an unexpected exception.
- Wrap low-level exceptions in domain-specific exceptions when rethrowing.
- Do not swallow exceptions silently; at minimum, log them with enough detail to diagnose issues.

## 7. Spring MVC and REST Patterns
- Controllers should be thin: delegate to services and avoid embedding SQL or data conversions.
- Return DTOs instead of entities; keep entity knowledge in the persistence layer.
- Validate inputs using Bean Validation annotations (`@Valid`, `@NotBlank`); handle validation failures with `@ControllerAdvice` if necessary.
- Centralize repeated attributes (sidebar data, pagination sizes) in services or helper components.

## 8. Transactions and Persistence
- Mark read-only service methods with `@Transactional(readOnly = true)` to signal intent.
- Repository methods should describe what they fetch (`findByKeyword`, `countByFilters`).
- For native queries, map results carefully and document column order in comments.
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

## 12. Git Hygiene
- Keep commits focused on one logical change.
- Run formatting and tests before committing.
- Update documentation/configuration alongside the code it describes.

Adhering to these rules minimizes merge conflicts, clarifies intent, and keeps the DemoRABC service maintainable as it grows.
