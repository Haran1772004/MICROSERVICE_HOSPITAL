# Hospital Microservices

## Run with Docker Compose

Prerequisites: Docker Engine and the Docker Compose plugin. Compose builds the
three Java 17 Spring Boot services from the repository root, including their
shared `common` Maven module.

Create a local environment file if you do not already have one:

```sh
cp -n .env.example .env
```

Ensure `.env` defines `DB_PASSWORD`, `JWT_SECRET`, and `INTERNAL_API_SECRET`.
If you already have an `.env`, add any missing variables without overwriting
your existing values. Replace the template placeholders: use distinct random
values for `JWT_SECRET` and `INTERNAL_API_SECRET` (for example, generate each
with `openssl rand -hex 32`) and set a local database password. The `.env` file
is ignored by Git and excluded from Docker build contexts. These local
environment variables are for development only; `.env` is not production
secret management.

Build and start all services:

```sh
docker compose up --build -d
docker compose ps
```

Application ports on the host:

| Service | URL |
| --- | --- |
| Auth | <http://localhost:8081> |
| Hospital | <http://localhost:8082> |
| Appointment | <http://localhost:8083> |

PostgreSQL remains available on host ports `5432` (auth), `5433` (hospital),
and `5434` (appointment). Applications use Compose DNS names and container
port `5432` to reach their respective databases. Auth and appointment call
hospital at `http://hospital-service:8082`; hospital calls auth at
`http://auth-service:8081`.

Database data is stored in the named `auth-data`, `hospital-data`, and
`appointment-data` volumes, so it persists when containers are stopped or
recreated. `docker compose down` stops and removes containers but preserves
these volumes. Do not add `-v` if you want to retain the data.

Inspect startup and application logs with:

```sh
docker compose logs --tail=100
docker compose logs -f auth-service hospital-service appointment-service
```

The PostgreSQL containers have readiness health checks and each application
waits for its own database before starting. The project does not include Spring
Boot Actuator or an application health endpoint, so a running application
container is not by itself proof that the application is ready. Confirm startup
in its logs and exercise the relevant API. Check Compose configuration with
`docker compose config --quiet`.

Stop the stack without deleting database data:

```sh
docker compose down
```
