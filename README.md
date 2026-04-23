common-utils
===============

This module contains shared utilities to reuse across microservices:

- `Mapper` — simple mapping helper using Jackson
- `Utilities` — small collection helpers
- `GlobalExceptionHandler` — basic controller advice
- `LoggerAspect` — AOP entry/exit hooks for controllers

Usage
1. Add `common-utils` as a dependency in your microservice `pom.xml` (install to your local repo or include as a module).
2. Call `Mapper.map(src, Target.class)` to convert DTOs/entities.
3. Copy or extend the `GlobalExceptionHandler` for project-specific error handling.
