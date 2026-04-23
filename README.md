common-utils
===============

This module contains shared utilities to reuse across microservices:

- `ObjectMapper` — interface for object mapping
- `ReflectionObjectMapper` — reflection-based implementation of ObjectMapper
- `ObjectMapperUtils` — utility class for batch mapping and JSON formatting
- `GlobalExceptionHandler` — basic controller advice for Spring Boot
- `LoggerAspect` — AOP entry/exit hooks for controllers

Usage
1. Add `common-utils` as a dependency in your microservice `pom.xml` (install to your local repo or include as a module).
2. Inject `ObjectMapper` to convert DTOs/entities: `objectMapper.map(src, Target.class)`.
3. Copy or extend the `GlobalExceptionHandler` for project-specific error handling.
4. The `LoggerAspect` will automatically log controller method calls if AOP is enabled.
