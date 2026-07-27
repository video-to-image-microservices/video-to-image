# Monitoramento

A solução usa duas camadas simples, compatíveis com a proposta de baixo custo:

- Prometheus e Grafana locais para métricas detalhadas das APIs;
- CloudWatch nativo na AWS para ALB, SQS, DLQs e Auto Scaling Groups.

Não são usados Amazon Managed Service for Prometheus nem Amazon Managed
Grafana.

## Ambiente local

Suba a aplicação e a camada de observabilidade:

```bash
docker compose \
  -f local-infra/docker-compose.yml \
  -f local-infra/docker-compose.monitoring.yml \
  up -d --build
```

Endereços:

- Prometheus: <http://localhost:9090>
- Grafana: <http://localhost:3000> (`admin` / `admin`)
- Targets: <http://localhost:9090/targets>

O dashboard `Video to Image - Microservices` é provisionado
automaticamente e mostra requisições, latência, CPU e heap das aplicações.

Validação rápida:

```bash
curl -fsS http://localhost:8082/actuator/prometheus >/dev/null
curl -fsS http://localhost:8080/actuator/prometheus >/dev/null
curl -fsS http://localhost:9090/-/ready
```

## AWS

O arquivo `terraform-aws/monitoring.tf` cria:

- dashboard `video-to-image-operations`;
- saúde dos target groups de `auth-ms` e `management-ms`;
- volume de requisições e respostas HTTP 5xx no ALB;
- mensagens aguardando e em processamento na fila principal;
- mensagens nas quatro DLQs;
- capacidade desejada dos três Auto Scaling Groups;
- alarmes para APIs sem target saudável e para a DLQ de processamento.

Aplicação:

```bash
cd terraform-aws
terraform init
terraform plan
terraform apply
```

Consulta pela AWS CLI:

```bash
aws cloudwatch get-dashboard \
  --dashboard-name "$(terraform output -raw cloudwatch_dashboard_name)"

aws cloudwatch describe-alarms \
  --alarm-names $(terraform output -json critical_alarm_names | jq -r '.[]')
```

Os alarmes ficam sem ação externa configurada para não exigir SNS, e-mail ou
serviços adicionais. A equipe deve verificar que permanecem em `OK`. Estado
`ALARM` na DLQ indica uma mensagem que esgotou as tentativas e precisa ser
inspecionada.

## Investigação básica

1. Verifique o dashboard e identifique o componente afetado.
2. Se houver mensagem na DLQ, consulte o corpo antes de redirecioná-la.
3. Consulte os logs do container na instância do ASG.
4. Confirme a saúde do target group ou a profundidade da fila.
5. Depois da correção, execute o teste ponta a ponta concorrente.
