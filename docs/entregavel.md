# Guia do entregável

Este documento relaciona os requisitos do Hackathon da Fase 5 com evidências
existentes no código.

## Repositórios

- [Solução e infraestrutura](https://github.com/video-to-image-microservices/video-to-image)
- [Autenticação](https://github.com/video-to-image-microservices/auth-ms)
- [Gerenciamento](https://github.com/video-to-image-microservices/management-ms)
- [Worker](https://github.com/video-to-image-microservices/worker-ms)

## Matriz de requisitos

| Requisito | Implementação/evidência | Situação |
| --- | --- | --- |
| Autenticação | JWT, BCrypt e endpoints de usuário no `auth-ms` | Pronto |
| Upload de vídeo | `POST /management/videos`, persistência no S3 | Pronto |
| Processamento assíncrono | SQS `process-queue` e `worker-ms` com FFmpeg | Pronto |
| Vários vídeos simultâneos | jobs isolados por UUID, ASG do worker e teste concorrente | Pronto |
| Sem perda de solicitações | SQS, visibility timeout, tentativas e DLQs | Pronto |
| Consulta de status | endpoints individual e de listagem | Pronto |
| Download das imagens | geração e download do ZIP | Pronto |
| Persistência | MongoDB, PostgreSQL/RDS e S3 | Pronto |
| Escalabilidade | ASGs dedicados; worker escala pela profundidade da fila | Pronto |
| Testes | testes unitários/integração nos serviços e E2E concorrente | Pronto |
| CI/CD | GitHub Actions testa, publica no ECR e atualiza os ASGs | Pronto |
| Monitoramento | Prometheus/Grafana local e CloudWatch na AWS | Pronto |
| Aviso proativo ao usuário em caso de erro | falha é persistida como `FAILED`, mas não há envio de e-mail/push | Pendente |

## Como demonstrar

### Testes automatizados dos serviços

Em cada repositório de serviço:

```bash
./mvnw verify
```

O worker possui cenários de sucesso, falha de download, falha do FFmpeg,
sanitização de nome e execução simultânea de dois jobs.

### Teste ponta a ponta concorrente na AWS

Use um vídeo curto e válido:

```bash
BASE_URL=http://SEU_ALB \
VIDEO_FILE=/caminho/video.mp4 \
./scripts/e2e-concurrent.sh
```

O teste cadastra um usuário, autentica, envia dois vídeos ao mesmo tempo,
aguarda ambos terminarem, baixa os ZIPs, valida seu conteúdo e confirma a
listagem dos status.

### Infraestrutura

```bash
cd terraform-aws
terraform fmt -check -recursive
terraform validate
terraform test
terraform plan
```

Os arquivos Terraform são os scripts de criação dos recursos solicitados.

## Roteiro sugerido para o vídeo de até 10 minutos

1. **0:00–1:00:** problema, objetivo e integrantes.
2. **1:00–2:30:** diagrama, responsabilidades dos três microsserviços.
3. **2:30–4:00:** fluxo S3 → SQS → worker → ZIP → status.
4. **4:00–5:30:** Terraform, ASGs e escalabilidade por fila.
5. **5:30–7:30:** demonstração do E2E com dois vídeos.
6. **7:30–8:30:** testes e pipelines do GitHub Actions.
7. **8:30–9:30:** dashboard, alarmes e DLQs.
8. **9:30–10:00:** limitações, custos e conclusão.

## Antes da entrega

- Executar o E2E na URL que será apresentada.
- Capturar uma execução verde de cada pipeline.
- Abrir o dashboard CloudWatch e confirmar os três alarmes.
- Gravar o vídeo com duração máxima de 10 minutos.
- Entregar os quatro links de repositório e o link do vídeo.
- Informar honestamente a pendência de notificação, ou implementá-la antes da
  submissão final.
