# Week 6: Hibernate, Spring Data JPA & SonarQube

Weekly content focusing on ORM persistence, database repositories, and static code quality.

---

## Study Topics

### 1. Spring Data JPA & Hibernate
- Relationship between JPA and Hibernate
- Creating a Spring Boot project with Spring Data JPA and database configurations
- Entity Mapping: Annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`) and relationship mappings (`@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne`)
- Spring Data Repositories (JpaRepository, CrudRepository)
- Custom queries using method name derivations and `@Query` (JPQL/SQL)
- Pagination and Sorting using `Pageable` and `Sort`
- Entity Auditing (`@CreatedDate`, `@LastModifiedDate`, etc.)
- JPA Projections (interface-based and class-based)
- Configuring multiple data sources

### 2. Code Quality & SonarQube
- Static code analysis vs runtime testing
- SonarQube Architecture and Maven integration (`mvn clean verify sonar:sonar`)
- Interpreting analysis reports: Code smells, Cyclomatic Complexity, Duplications, Unit test coverage, Bugs & Vulnerabilities
- Enforcing and configuring Quality Gates

---

## Weekly Tasks
- Set up a database connection and configure Hibernate entities with annotations.
- Build repository interfaces with CRUD capabilities and custom JPQL queries.
- Add pagination and sorting support to repository search methods.
- Run a SonarQube scan locally over the project and evaluate the Quality Gate report.

