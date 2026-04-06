# Server Docker usage

This project’s `:server` module is a Ktor (Netty) webservice.

## Build the image
From the repo root:

```sh
docker build -f server/Dockerfile -t allaboard-server:local .
```

## Run the container (recommended: env vars at runtime)
Use Docker’s `--env-file` to inject environment variables from `server/.env` **at runtime**.

```sh
docker run --rm -p 8080:8080 \
  --env-file server/.env \
  allaboard-server:local
```

### Notes
- **Do not bake secrets into the image.** Keeping secrets outside the image is safer and works for CI/CD.
- The server binds to `0.0.0.0` and uses port `8080` by default.
- `SupabaseConfig` first checks real environment variables, then falls back to loading a `.env` file for local dev.

## Smoke test

```sh
curl http://localhost:8080/
```
