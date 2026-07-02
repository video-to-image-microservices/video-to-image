# Guia de teste local

## O projeto cumpre os requisitos?

Parcialmente.

| Requisito | Status | Observacao |
| --- | --- | --- |
| Processar mais de um video ao mesmo tempo | Parcial | O upload publica mensagens na `process-queue`, permitindo processamento paralelo por workers consumidores. O worker que extrai imagens/gera zip ainda nao esta neste microservico. |
| Em picos, nao perder requisicao | Parcial | SQS + Postgres + S3 ajudam a nao perder uploads ja aceitos. Para producao faltam DLQ, retry policy, idempotencia mais forte e observabilidade. |
| Sistema protegido por usuario e senha | Parcial | Este microservico valida JWT. Usuario/senha ficam no `auth-ms`. Precisa usar o mesmo `JWT_SECRET` do auth-ms. |
| Listagem de status dos videos de um usuario | Sim | `GET /videos/status` lista os videos do usuario autenticado. |
| Enviar video e baixar zip | Sim no management-ms | O upload existe, publica evento de processamento com `outputZipKey`, e o download busca o `.zip` gerado no S3. O worker que extrai imagens e grava o zip no S3 fica fora deste microservico. |

## Subir ambiente

```powershell
cd C:\Users\krs\Desktop\tech\management-ms\management_ms
docker compose up --build
```

O compose sobe:

- `postgres` em `localhost:5432`
- `localstack` em `localhost:4566`
- `management-ms` em `localhost:8080`

O LocalStack cria automaticamente:

- `user-created-queue`
- `user-deleted-queue`
- `video-status-queue`
- `process-queue`
- bucket `videos-bucket`

## Importar Postman

Importe estes dois arquivos:

- `docs/postman/management-ms.postman_collection.json`
- `docs/postman/management-ms.postman_environment.json`

Selecione o environment `management-ms local`.

## Fluxo de teste no Postman

1. Gere ou obtenha um JWT do `auth-ms`.
   - O token precisa ter o id do usuario em `userId`, `id` ou `sub`.
   - O valor precisa ser igual a variavel `user_id` do Postman.
   - Cole o token na variavel `jwt_token`.

2. Rode `LocalStack SQS - Simular User Queue`.
   - Isso envia `{"userId":"{{user_id}}"}` para a `user-created-queue`.
   - O management-ms consome e grava o usuario localmente.

3. Rode `Videos - Upload`.
   - Selecione um `.mp4` no campo `file`.
   - Resposta esperada: `202 Accepted`.
   - O Postman salva `video_process_id` e `file_name`.

4. Rode `LocalStack SQS - Ver Process Queue`.
   - Deve aparecer uma mensagem com `videoProcessId`, `userId`, `bucket`, `storageKey`, `originalFileName` e `contentType`.

5. Rode `Videos - Listar Status do Usuario`.
   - Deve retornar uma lista com o video em status `RECEIVED`.

6. Rode `LocalStack S3 - Upload ZIP Gerado Fake`.
   - Isso simula o worker gravando o `.zip` gerado no S3.

7. Rode `LocalStack SQS - Simular Status Queue PROCESSED`.
   - Isso simula o processador de video avisando que terminou e informando a chave do zip.

8. Rode `Videos - Status por Nome do Arquivo`.
   - Deve retornar status `PROCESSED`.

9. Rode `Videos - Download`.
   - Deve baixar o `.zip` salvo no S3, nao o video original.

## Formato das mensagens

User queue:

```json
{
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

Delete user queue:

```json
{
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

Process queue produzida pelo upload:

```json
{
  "videoProcessId": "uuid",
  "userId": "uuid",
  "bucket": "videos-bucket",
  "storageKey": "uuid/uuid/sample.mp4",
  "outputZipKey": "uuid/uuid/generated/sample.zip",
  "originalFileName": "sample.mp4",
  "contentType": "video/mp4"
}
```

Status queue:

```json
{
  "videoProcessId": "uuid",
  "status": "PROCESSED",
  "zipStorageKey": "uuid/uuid/generated/sample.zip",
  "zipFileName": "sample.zip"
}
```

Status aceitos:

- `RECEIVED`
- `PROCESSING`
- `PROCESSED`
- `FAILED`
