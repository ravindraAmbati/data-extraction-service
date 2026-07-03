# Data Extraction Service

Enterprise Spring Boot ETL service for extracting and transforming metadata from multiple database platforms through a unified REST API. Load phase is planned for a later iteration.

## Tech Stack

- Java 11
- Spring Boot 2.7.x
- Maven
- Spring Web
- Spring JDBC
- Spring Data MongoDB
- IBM DB2 / AS400 JDBC via `net.sf.jt400:jt400`
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
- Database engines are optional at startup. Configure zero, one, or many connections in the active `dbConfig*.yml`.

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

Database connections are loaded from the profile-specific config location. Connections are not required for the application to start; APIs that target a missing database return `DATABASE_NOT_FOUND`.

- local: `classpath:dbConfig-local.yml`
- nonprod: `${DATAEXTRACT_DB_CONFIG}` or `classpath:dbConfig-nonprod.yml`
- prod: `${DATAEXTRACT_DB_CONFIG}` or `classpath:dbConfig-prod.yml`

The fallback shared example is `src/main/resources/dbConfig.yml`. The file may contain zero, one, or many named connections.

IBM DB2 / AS400 connection example:

```yaml
databases:
  - name: ibm_db2_as400
    type: IBM-DB2
    driver-class-name: com.ibm.as400.access.AS400JDBCDriver
    url: jdbc:as400:{host}
    username: {username}
    password: {password}
    domain-name: Source Data Dictionary
    community-name: TO-Source
    table-attributes: [Table Type, TableKind, Table Description, Column Count]
    column-attributes: [Column Name, Data Type, DataType Description, ColumnLength, Nullable]
    hikari:
      schema: {schema}
    tomcat:
      validation-query: SELECT 1
```

The `tomcat.validation-query` value is applied to HikariCP as `connectionTestQuery`.

Transformation metadata fields can be configured on every database entry:

- `metadata-query`: optional SQL query used instead of JDBC `DatabaseMetaData` for table metadata.
- `domain-name`: domain name used in transformed Domain and Asset identifiers. Whitespace is allowed.
- `community-name`: community name used in transformed identifiers. Whitespace is allowed.
- `table-attributes`: table-level attributes to include in transformed table assets.
- `column-attributes`: column-level attributes to include in transformed column assets.

Custom metadata query example:

```yaml
metadata-query: >
  SELECT column_name, data_type, is_nullable AS nullable
  FROM information_schema.columns
  WHERE table_name IN (?, ?)
  ORDER BY ordinal_position
```

The query should return column aliases compatible with `column_name`, `data_type`, and `nullable`. It receives two parameters: the requested table name and the simple table name without schema.

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
- `GET /api/dataextract/{database}/metadata`
- `GET /api/dataextract/{database}/{schema}/metadata`
- `GET /api/dataextract/{database}/{schema}/{table}/metadata`

Extract reads metadata from configured source databases and stores raw extract JSON in local storage.

Local extract storage:

```text
./{today-date}/{database-name}/extract/tables.json
./{today-date}/{database-name}/extract/{schema-name}/tables.json
./{today-date}/{database-name}/extract/{schema-name}/{table-name}.json
```

The output root defaults to the application working directory and can be changed with:

```yaml
dataextract:
  extractOutputRoot: .
  extractOutputFolder: extract
  transformOutputFolder: transform
```

The row count and paginated row APIs are soft-disabled for ETL extract mode and return HTTP `410 Gone`:

- `GET /api/dataextract/{database}/{table}/rowscount`
- `GET /api/dataextract/{database}/{table}/rows?offset=0&limit=1000`

## Data Transformation APIs

- `GET /api/transform/{database}/metadata`
- `GET /api/transform/{database}/{schema}/metadata`
- `GET /api/transform/{database}/{schema}/{table}/metadata`

Transform always calls Extract first, then reads raw extract JSON from local storage and writes Collibra-formatted JSON to local transform storage. Transform API responses return file paths only.

The Collibra-formatted JSON contains:

- one `Domain` resource
- one schema `Asset`
- one table `Asset`
- one column `Asset` per extracted column

Local transform storage:

```text
./{today-date}/{database-name}/transform/metadata.json
./{today-date}/{database-name}/transform/{schema-name}/metadata.json
./{today-date}/{database-name}/transform/{schema-name}/{table-name}.json
```

## Data Load APIs

- `POST /api/load/{database}/metadata`
- `POST /api/load/{database}/{schema}/metadata`
- `POST /api/load/{database}/{schema}/{table}/metadata`

Load always calls Transform first, and Transform calls Extract first. Load then reads the transformed Collibra-formatted JSON from local storage and posts it to Collibra as `multipart/form-data` with:

- `file`: actual JSON file content
- `filename`: JSON file name

Load responses include:

- Collibra target URL
- HTTP status and response body
- transformed JSON file used
- extract JSON files referred during the chain

Collibra API configuration is read from `application.yml` using nested YAML property syntax:

```yaml
collibra:
  host: ${COLLIBRA_HOST:localhost}
  port: ${COLLIBRA_PORT:443}
  api:
    path: ${COLLIBRA_API:/rest/2.0}
    endpoint: ${COLLIBRA_ENDPOINT:/import/json-job}
  username: ${COLLIBRA_USERNAME:}
  password: ${COLLIBRA_PASSWORD:}
  ssl: ${COLLIBRA_SSL:true}
```

The final target URL is built as:

```text
{http|https}://{host}:{port}/{api.path}/{api.endpoint}
```

## Clear Local Storage APIs

- `DELETE /api/clear/{database}/metadata`
- `DELETE /api/clear/{database}/{schema}/metadata`
- `DELETE /api/clear/{database}/{schema}/{table}/metadata`

Clear removes both Extract and Transform local storage for the requested scope.

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
