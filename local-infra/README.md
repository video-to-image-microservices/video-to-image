# video-to-image microservices

Stack local integrada para testar `auth-ms`, `management-ms` e `worker-ms` juntos com Docker, LocalStack, Postman, MongoDB, Redis e Postgres.

## Servicos

| Servico | Porta | Papel |
| --- | --- | --- |
| auth-ms | 8082 | Cadastro, login JWT e eventos `user-created` / `user-deleted` |
| management-ms | 8080 | Upload, status e download de videos com JWT do auth-ms |
| worker-ms | — | Consome `process-queue`, extrai frames com ffmpeg, grava ZIP no S3 e publica status em `video-status-queue` |
| localstack | 4566 | SQS e S3 locais |
| mongo | 27017 | Banco do auth-ms |
| postgres | 5432 | Banco do management-ms |
| auth-redis | 6379 | Cache exclusivo do auth-ms |
| management-redis | 6380 | Cache exclusivo do management-ms |

## Subir tudo

```bash
docker compose up --build
```

Para rodar em background:

```bash
docker compose up -d --build
```

Se estiver no WSL e aparecer `The command 'docker' could not be found in this WSL 2 distro`, habilite a integracao da sua distro em Docker Desktop > Settings > Resources > WSL Integration, ou use o Docker instalado dentro do WSL com:

```bash
sudo docker compose up --build
```

Depois de alteracoes em `application.properties` ou `Dockerfile`, use `--build` para recriar a imagem da aplicacao.

## Fluxo de teste no Postman

Importe estes arquivos:

- `docs/postman/video-to-image-local.postman_environment.json`
- `docs/postman/video-to-image-integrated.postman_collection.json`

Rode as requests nesta ordem:

1. `Auth MS - Create User`
2. `Auth MS - Login`
3. `Management MS - Upload Video`
4. Aguarde o `worker-ms` processar o video (consome `process-queue`, gera o ZIP no S3 e publica em `video-status-queue`)
5. `Management MS - Download ZIP`

O cadastro salva `user_id`; o login salva `jwt_token`. O `auth-ms` publica o evento `user-created`, e o `management-ms` consome esse evento para liberar uploads desse usuario.

O `worker-ms` consome `process-queue`, extrai frames com ffmpeg, grava o ZIP no S3 e publica o status em `video-status-queue`. O `management-ms` consome esse evento para atualizar o status do video no Postgres e liberar o download.

## URLs uteis

- Auth: http://localhost:8082
- Management: http://localhost:8080
- LocalStack: http://localhost:4566
- Auth Swagger: http://localhost:8082/auth/swagger-ui.html
