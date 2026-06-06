# Data Extraction Service

Enterprise Spring Boot service for extracting metadata, row counts, and paginated data from multiple database platforms through a unified REST API.

## Tech Stack

- Java 11
- Spring Boot 2.7.x
- Maven
- Spring Web
- Spring JDBC
- Spring Data MongoDB
- HikariCP
- Spring Boot Actuator
- Spring Validation
- Springdoc OpenAPI / Swagger
- Logback
- JUnit 5
- Mockito
- AES/GCM password encryption using JDK 11 crypto APIs

## Local Setup

Prerequisites:

- JDK 11 installed and available on `PATH`
- Maven installed and available on `PATH`
- Database engines reachable from this machine, or update `src/main/resources/dbConfig.yml` with reachable hosts

Build:

```bash
mvn clean package
```

Run locally:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Or use the generated scripts after packaging:

```bash
./scripts/unix/start.sh
./scripts/unix/stop.sh
./scripts/unix/restart.sh
```

Windows Command Prompt:

```cmd
scripts\windows\start.cmd
scripts\windows\stop.cmd
scripts\windows\restart.cmd
```

Windows PowerShell:

```powershell
.\scripts\windows\start.ps1
.\scripts\windows\stop.ps1
.\scripts\windows\restart.ps1
```

The default local URL is `http://localhost:8080`.

## Environment Configuration

Common configuration lives in `src/main/resources/application.yml`.

Environment-specific configuration files:

- `src/main/resources/application-local.yml`
- `src/main/resources/application-nonprod.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/dbConfig-local.yml`
- `src/main/resources/dbConfig-nonprod.yml`
- `src/main/resources/dbConfig-prod.yml`

Select an environment with:

```bash
java -jar target/data-extraction-service.jar --spring.profiles.active=local
java -jar target/data-extraction-service.jar --spring.profiles.active=nonprod
java -jar target/data-extraction-service.jar --spring.profiles.active=prod
```

Script profile selection:

```bash
APP_PROFILE=nonprod ./scripts/unix/start.sh
```

```cmd
set APP_PROFILE=nonprod
scripts\windows\start.cmd
```

```powershell
$env:APP_PROFILE = "nonprod"
.\scripts\windows\start.ps1
```

Recommended environment variables:

- `SERVER_PORT`: HTTP port for non-prod/prod
- `DATAEXTRACT_DB_CONFIG`: optional external database config location, such as `file:/opt/dataextract/dbConfig-prod.yml`
- `DATAEXTRACT_ENCRYPTION_KEY`: encryption key material
- `DATAEXTRACT_ENCRYPTION_SALT`: encryption salt
- `JAVA_OPTS`: JVM options, such as `-Xms512m -Xmx1024m`

Use different `DATAEXTRACT_ENCRYPTION_KEY` and `DATAEXTRACT_ENCRYPTION_SALT` values for local, non-prod, and prod. Values encrypted in one environment can only be decrypted with the same key and salt.

## Database Configuration

Database connections are loaded from the profile-specific config location:

- local: `classpath:dbConfig-local.yml`
- nonprod: `${DATAEXTRACT_DB_CONFIG}` or `classpath:dbConfig-nonprod.yml`
- prod: `${DATAEXTRACT_DB_CONFIG}` or `classpath:dbConfig-prod.yml`

The fallback shared example is `src/main/resources/dbConfig.yml`. Multiple named connections are supported per database type.

Example encrypted password:

```yaml
databases:
  - name: postgres_hr
    type: POSTGRES
    jdbcUrl: jdbc:postgresql://localhost:5432/hr
    username: postgres
    password: ENC(base64-cipher-text)
```

Plain text passwords still work, but encrypted values are recommended. JDBC passwords are decrypted automatically when a datasource is created.

Teradata support is implemented through standard JDBC. Add your licensed Teradata JDBC driver to your internal Maven repository or application runtime classpath before connecting to Teradata.

Pagination limits are configured in `application.yml`:

```yaml
pagination:
  defaultLimit: 1000
  maxLimit: 10000
```

## Password Encryption APIs

Encrypt:

```http
POST /encrypt
Content-Type: application/json

{
  "value": "password"
}
```

Response:

```json
{
  "value": "ENC(...)"
}
```

Decrypt:

```http
POST /decrypt
Content-Type: application/json

{
  "value": "ENC(...)"
}
```

Grouped aliases are also available at `POST /api/crypto/encrypt` and `POST /api/crypto/decrypt`.

Response:

```json
{
  "value": "password"
}
```

## Data Extraction APIs

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
java -jar target/data-extraction-service.jar --spring.profiles.active=local
```
