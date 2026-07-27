# Video to Image Microservices

Sistema distribuído para receber vídeos, extrair frames de forma assíncrona e
disponibilizar as imagens em um arquivo ZIP.

## Repositórios

- [video-to-image](https://github.com/video-to-image-microservices/video-to-image):
  infraestrutura, ambiente local, monitoramento e documentação
- [auth-ms](https://github.com/video-to-image-microservices/auth-ms):
  usuários, autenticação JWT, MongoDB e eventos de usuário
- [management-ms](https://github.com/video-to-image-microservices/management-ms):
  upload, status, download, PostgreSQL, Redis, S3 e publicação de jobs
- [worker-ms](https://github.com/video-to-image-microservices/worker-ms):
  consumo da fila, FFmpeg, geração do ZIP e publicação do status

## Funcionalidades

- autenticação e gerenciamento de usuários;
- upload de vídeos;
- processamento assíncrono e simultâneo;
- extração de frames com FFmpeg;
- geração e download de ZIP;
- consulta individual e listagem do status;
- persistência de falhas com status `FAILED`;
- cache, filas com DLQ e escalabilidade horizontal.

> O aviso proativo por e-mail/push em caso de falha ainda não está
> implementado. A falha fica disponível na consulta de status e no
> monitoramento da DLQ.

## Arquitetura

A solução usa arquitetura hexagonal nos serviços e comunicação orientada a
eventos:

1. o usuário autentica no `auth-ms`;
2. o `management-ms` grava o vídeo no S3 e publica um job no SQS;
3. o `worker-ms` consome o job, extrai os frames e envia o ZIP ao S3;
4. o worker publica o novo status;
5. o `management-ms` persiste o status e disponibiliza o download.

Na AWS, um Application Load Balancer roteia `/auth/*` e `/management/*`.
Cada microsserviço usa um Auto Scaling Group dedicado. O worker escala pela
profundidade da fila.

![Arquitetura AWS](https://github.com/user-attachments/assets/398e5283-c837-494c-bee8-7c6ffb03b07f)

Detalhes da infraestrutura: [terraform-aws/README.md](terraform-aws/README.md).

## Tecnologias

- Java, Kotlin, Spring Boot e FFmpeg
- PostgreSQL no Amazon RDS
- Redis no Amazon ElastiCache
- MongoDB em EC2 privada no plano Free Tier
- Amazon S3, SQS, ECR, EC2, ALB, Auto Scaling e CloudWatch
- Docker, Terraform e GitHub Actions com OIDC
- Prometheus e Grafana no ambiente local

O Amazon DocumentDB não é usado na implantação atual porque a conta Free Plan
o bloqueia.

## Executar localmente

```bash
docker compose -f local-infra/docker-compose.yml up -d --build
```

Com monitoramento:

```bash
docker compose \
  -f local-infra/docker-compose.yml \
  -f local-infra/docker-compose.monitoring.yml \
  up -d --build
```

## Testes

Cada repositório de serviço executa `mvn verify` no CI. O worker inclui teste
de dois jobs simultâneos e cenários de falha.

Teste concorrente completo na AWS:

```bash
BASE_URL=http://SEU_ALB \
VIDEO_FILE=/caminho/video.mp4 \
./scripts/e2e-concurrent.sh
```

O script envia dois vídeos ao mesmo tempo, espera os dois processamentos,
baixa os ZIPs e verifica seu conteúdo.

## Monitoramento

- [Guia de monitoramento](docs/monitoramento.md)
- Dashboard local provisionado no Grafana
- Dashboard CloudWatch criado por Terraform
- Alarmes para indisponibilidade das APIs e DLQ de processamento

## Entrega

O [guia do entregável](docs/entregavel.md) contém a matriz de requisitos do
Hackathon, evidências, comandos de validação, checklist final e roteiro para o
vídeo de até 10 minutos.
