# ADR 0001: Modular monolith and Hexagonal Architecture

- **Status:** Accepted
- **Decision:** Build one Java 21+ Spring Boot deployment organized by business feature. Each feature uses domain, application, web, and infrastructure boundaries. Modules interact only via public application APIs or domain events; persistence entities never cross module boundaries. Verify boundaries with Spring Modulith.
- **Context:** The product has related capabilities and a small initial scope; separate services would add deployment and data-consistency cost without an established scaling need.
- **Consequences:** One deployable and relational database simplify local development and transactions. Modules remain independently evolvable only when boundaries are enforced. Extracting a module later requires explicit API/event and data-ownership design.
- **Alternatives:** Microservices (rejected for MVP operational overhead); flat layered monolith (rejected because feature ownership and boundaries become implicit).
