# Data Extraction Service

Enterprise-grade Spring Boot REST API for metadata extraction, row counts, and mandatory paginated data extraction across PostgreSQL, MySQL, SQL Server, Oracle, Teradata, and MongoDB.

## Stack

- Java 11
- Spring Boot 2.7.x
- Maven
- Spring Web, JDBC, Data MongoDB, Validation, Actuator
- HikariCP
- Springdoc OpenAPI
- Logback
- JUnit 5 and Mockito

## Run

```bash
mvn spring-boot:run
```

The service starts on port `8080`.

## Configuration

Database connections are loaded from `src/main/resources/dbConfig.yml`. Multiple named connections are supported per database type.
Teradata support is implemented through standard JDBC. Add your licensed Teradata JDBC driver to your internal Maven repository or application runtime classpath before connecting to Teradata.

Pagination limits are configured in `src/main/resources/application.yml`:

```yaml
pagination:
  defaultLimit: 1000
  maxLimit: 10000
```

## APIs

- `GET /api/dataextract/databases`
- `GET /api/dataextract/{database}/tables`
- `GET /api/dataextract/{database}/{table}/metadata`
- `GET /api/dataextract/{database}/{table}/rowscount`
- `GET /api/dataextract/{database}/{table}/rows?offset=0&limit=1000`

Optional row query parameters:

- `columns`
- `sortBy`
- `sortOrder`

Full table extraction is intentionally unavailable. `offset` and `limit` are mandatory for row retrieval.

## Monitoring

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

## OpenAPI

- `/swagger-ui/index.html`
- `/v3/api-docs`

## Build Executable JAR

```bash
mvn clean package
java -jar target/data-extraction-service.jar
```
