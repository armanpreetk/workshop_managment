# Workshop Management - Dev DB Setup

This project is preconfigured to run against a local MySQL instance using Docker so every teammate can clone and run without using root.

## Quick start
1. Copy env template and adjust if needed:
   - Copy `.env.example` to `.env` (or keep the default values)
2. Start MySQL locally via Docker:
   - `docker compose up -d`
3. Run the application:
   - Windows PowerShell: `./mvnw.cmd spring-boot:run`

## Connection details (defaults)
- JDBC URL: `jdbc:mysql://localhost:3306/workshopdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true`
- Username: `workshop`
- Password: `password`

These values come from `.env` and `application.properties`. Change them by editing `.env` or setting environment variables.

## Security notes
- The app uses a non-root database user by default.
- Credentials in `.env` are for local development only. Do not commit real secrets.
- For shared/staging deployments, use a central DB host and secure credentials (VPN, TLS, limited GRANTs).