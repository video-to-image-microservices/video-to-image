# Infraestrutura AWS dedicada

Esta pasta implementa a arquitetura distribuida do diagrama do projeto:

- ALB publico com roteamento por path;
- `auth-ms` em Auto Scaling Group dedicado;
- `management-ms` em Auto Scaling Group dedicado;
- `worker-ms` em Auto Scaling Group dedicado;
- MongoDB dedicado em EC2 privado e ElastiCache exclusivo do `auth-ms`;
- Amazon RDS for PostgreSQL e ElastiCache exclusivos do `management-ms`;
- S3 para videos e ZIPs;
- SQS com DLQ para eventos de usuario, processamento e status;
- VPC Endpoint de S3 e endpoint privado de SQS;
- NAT Gateway para bootstrap e pull das imagens privadas;
- scaling por CPU para APIs e por profundidade da fila para workers.

## Fluxo do ALB

| Path | Destino |
| --- | --- |
| `/auth` e `/auth/*` | Target Group do `auth-ms` |
| `/management` e `/management/*` | Target Group do `management-ms` |

O Terraform configura `SERVER_SERVLET_CONTEXT_PATH=/management` no container do
management para que o prefixo do ALB seja entendido pela aplicacao.

## Auto Scaling

- `auth-ms`: target tracking em 60% de CPU, entre 1 e 5 instancias por padrao.
- `management-ms`: target tracking em 60% de CPU, entre 1 e 5 instancias.
- `worker-ms`: adiciona uma instancia quando ha mensagem visivel em
  `process-queue` e remove uma instancia quando a fila fica vazia por 5 minutos.
- Todos os ASGs usam instance refresh quando o Launch Template muda.
- As APIs usam Amazon Linux 2023, com AWS CLI preinstalado, para instalar
  somente Docker e reduzir o tempo de bootstrap.

O worker usa `c7i-flex.large` por padrao: e um tipo x86 elegivel no AWS Free
Plan desta conta e oferece CPU suficiente para o FFmpeg. Os limites e tipos de
instancia podem ser alterados no `terraform.tfvars`.

## Publicar as imagens

O Terraform cria tres repositorios privados no ECR. Crie primeiro apenas os
repositorios:

```bash
terraform apply \
  -target=aws_ecr_repository.auth_ms \
  -target=aws_ecr_repository.management_ms \
  -target=aws_ecr_repository.worker_ms

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
REGISTRY="${ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com"
aws ecr get-login-password --region us-east-1 |
  docker login --username AWS --password-stdin "$REGISTRY"

docker build -t "$REGISTRY/video-to-image/auth-ms:latest" ../../auth-ms/auth-ms
docker build -t "$REGISTRY/video-to-image/management-ms:latest" ../../management-ms/management_ms
docker build -t "$REGISTRY/video-to-image/worker-ms:latest" ../../worker-ms/worker_ms

docker push "$REGISTRY/video-to-image/auth-ms:latest"
docker push "$REGISTRY/video-to-image/management-ms:latest"
docker push "$REGISTRY/video-to-image/worker-ms:latest"
```

## Provisionar

```bash
terraform init
terraform fmt -check
terraform validate
terraform plan -out=tfplan
terraform apply tfplan
```

Ao finalizar:

```bash
terraform output auth_base_url
terraform output management_base_url
terraform output video_bucket_name
```

## CI/CD dos microsservicos

O Terraform cria o provedor OIDC do GitHub e duas roles:

- `github-actions-video-to-image-deploy` para os repositorios da organizacao;
- `github-actions-worker-ms-deploy`, vinculada ao arquivo de workflow do
  `worker-ms`.

Cada repositorio possui `.github/workflows/ci-cd-aws.yml`. Pull requests
executam apenas `mvn verify`. Um push em `main`, depois dos testes:

1. assume a role AWS via OIDC;
2. publica no ECR as tags do SHA e `latest`;
3. inicia um instance refresh do ASG do servico;
4. aguarda o refresh terminar ou falha por timeout.

Nao e necessario cadastrar `AWS_ACCESS_KEY_ID` ou
`AWS_SECRET_ACCESS_KEY` no GitHub.

## Verificar Auto Scaling do worker

```bash
aws cloudwatch describe-alarms \
  --alarm-names worker-ms-process-queue-high worker-ms-process-queue-empty

aws autoscaling describe-auto-scaling-groups \
  --auto-scaling-group-names worker-ms-asg

aws sqs get-queue-attributes \
  --queue-url "$(terraform output -raw process_queue_url)" \
  --attribute-names ApproximateNumberOfMessages
```

## Observacoes

- Esta topologia cria recursos cobraveis, incluindo NAT Gateway, ALB,
  RDS, ElastiCache, endpoints privados e instancias EC2.
- A conta esta no AWS Free Plan, que bloqueia DocumentDB. Por isso o auth usa
  uma instancia MongoDB dedicada na subnet privada. Ao atualizar o plano da
  conta, o bloco DocumentDB pode ser reativado em `auth-data.tf`.
- O state existente deve ser migrado para um backend remoto antes de trabalho
  em equipe.
- `terraform destroy` remove inclusive o bucket e seus objetos porque
  `force_destroy` esta habilitado.
