# auth-ms

Microserviço de autenticação e gerenciamento de usuários do ecossistema **video-to-image**. Responsável por cadastro, login com JWT, CRUD de perfil e publicação de eventos de domínio via SQS.

O código da aplicação está no diretório [`auth-ms/`](auth-ms/).

## Sumário

- [Conceito: autenticação centralizada, autorização descentralizada](#conceito-autenticação-centralizada-autorização-descentralizada)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [API](#api)
- [Eventos SQS](#eventos-sqs)
- [Pré-requisitos](#pré-requisitos)
- [Rodando localmente](#rodando-localmente)
- [Docker](#docker)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Banco de dados e Liquibase](#banco-de-dados-e-liquibase)
- [Testes](#testes)
- [Swagger](#swagger)

---

## Conceito: autenticação centralizada, autorização descentralizada

Este serviço concentra **quem o usuário é** e **se as credenciais são válidas**. Os demais microserviços do ecossistema não implementam login nem emitem tokens — eles apenas **validam o JWT** recebido e decidem **o que aquele usuário pode fazer** em seu próprio domínio.

| Responsabilidade | Onde fica | Exemplo |
|------------------|-----------|---------|
| **Autenticação** (centralizada) | `auth-ms` | Login, emissão de JWT, cadastro, hash de senha |
| **Autorização** (descentralizada) | Cada microserviço consumidor | "Este usuário pode processar este vídeo?", "Pode acessar este bucket?" |

### O que o auth-ms faz

- Registra usuários (`POST /users`)
- Autentica e emite JWT HS256 (`POST /auth/login`)
- Garante que cada usuário só acessa **o próprio** recurso (`GET/PUT/DELETE /users/{id}` com validação de ownership)
- Publica eventos `user-created` e `user-deleted` para que outros serviços reajam sem acoplamento direto

### O que os consumidores fazem

1. Recebem o header `Authorization: Bearer <token>` do cliente
2. Validam assinatura e expiração do JWT (mesmo `JWT_SECRET` ou chave pública, conforme estratégia)
3. Extraem claims (`userId`, `email`, `name`) e aplicam **suas próprias regras de autorização**
---

## Stack

| Tecnologia | Versão / detalhe |
|------------|------------------|
| Java | 17 |
| Spring Boot | 4.1.0 |
| Spring Security | JWT stateless (HS256) |
| Spring Data JPA | Hibernate + PostgreSQL |
| Liquibase | Migrations versionadas |
| Spring Cloud AWS SQS | 4.0.2 |
| MapStruct | 1.6.3 |
| Springdoc OpenAPI | 2.8.5 |
| JJWT | 0.12.6 |

---

## Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)**. O núcleo (`core/`) não depende de Spring, JPA ou AWS; a infraestrutura (`infra/`) implementa os adaptadores.

```
auth-ms/src/main/java/video/to/image/auth_ms/
├── core/
│   ├── domain/           # Entidades, exceções de domínio
│   └── application/      # Ports (in/out) e use cases
└── infra/
    ├── adapters/inbound/web/     # Controllers, DTOs, JWT filter
    ├── adapters/outbound/        # JPA, BCrypt, JWT generator
    ├── broker/                   # Publicação SQS
    └── config/                   # Beans Spring, Security, properties
```

### Fluxo de uma requisição autenticada

```mermaid
sequenceDiagram
    participant C as Cliente
    participant F as JwtAuthenticationFilter
    participant SC as SecurityContext
    participant UC as UserController
    participant US as UserService
    participant UU as UserCrudUseCase

    C->>F: GET /users/{id} + Bearer token
    F->>F: Valida JWT e extrai userId
    F->>SC: Principal = userId
    UC->>US: get(id)
    US->>SC: getCurrentUserId()
    US->>UU: findById(requesterId, id)
    UU->>UU: assertOwnership(requesterId, id)
    UU-->>C: 200 ou 403 Forbidden
```

### Camadas e responsabilidades

| Camada | Pacote | Responsabilidade |
|--------|--------|------------------|
| Domain | `core.domain` | `User`, `AuthResult`, exceções |
| Application | `core.application` | `AuthenticateUseCase`, `UserCrudUseCase`, ports |
| Inbound | `infra.adapters.inbound.web` | REST API, segurança HTTP, DTOs |
| Outbound persistence | `infra.adapters.outbound.persistence` | JPA, repositórios |
| Outbound security | `infra.adapters.outbound.security` | BCrypt, geração JWT |
| Broker | `infra.broker` | `UserEventPublisher` → SQS |

---

## API

**Base URL:** `http://localhost:8082`

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|--------------|-----------|
| `POST` | `/auth/login` | Pública | Login e obtenção do JWT |
| `POST` | `/users` | Pública | Cadastro de usuário |
| `GET` | `/users/{id}` | JWT | Buscar perfil (apenas o próprio) |
| `PUT` | `/users/{id}` | JWT | Atualizar nome (apenas o próprio) |
| `DELETE` | `/users/{id}` | JWT | Excluir conta (apenas o próprio) |

### Claims do JWT

| Claim | Conteúdo |
|-------|----------|
| `sub` | UUID do usuário |
| `userId` | UUID do usuário |
| `email` | E-mail |
| `name` | Nome |
| `exp` | Expiração (padrão: 24h) |

### Exemplos com curl

**Cadastro:**

```bash
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@email.com","password":"senha123"}'
```

**Login:**

```bash
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","password":"senha123"}'
```

**Buscar perfil (substitua `{id}` e `{token}`):**

```bash
curl http://localhost:8082/users/{id} \
  -H "Authorization: Bearer {token}"
```

### Códigos de erro

| HTTP | Situação |
|------|----------|
| 400 | Validação de entrada |
| 401 | Credenciais inválidas ou token ausente/expirado |
| 403 | Tentativa de acessar recurso de outro usuário |
| 404 | Usuário não encontrado |
| 409 | E-mail já cadastrado |

---

## Eventos SQS

Quando um usuário é criado ou excluído, o serviço publica mensagens JSON para filas SQS. Outros microserviços consomem essas filas para provisionar ou remover recursos associados ao usuário.

| Evento | Fila | Payload | Quando |
|--------|------|---------|--------|
| User created | `user-created-queue` | `{ "userId": "<uuid>" }` | Após `POST /users` |
| User deleted | `user-deleted-queue` | `{ "userId": "<uuid>" }` | Após `DELETE /users/{id}` |

### LocalStack (desenvolvimento)

O `docker-compose` sobe o LocalStack com SQS na porta `4566`. Um script de init cria as filas automaticamente:

- [`auth-ms/localstack/init/create-queues.sh`](auth-ms/localstack/init/create-queues.sh)

**Verificar mensagens na fila:**

```bash
aws --endpoint-url=http://localhost:4566 sqs receive-message \
  --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/user-created-queue
```

### AWS (produção)

Em produção, remova ou não defina `spring.cloud.aws.sqs.endpoint`. O SDK usará o SQS real da AWS com as credenciais/IAM role do ambiente (ECS task role, EC2 instance profile, etc.).

Crie as filas na AWS (ex.: via Terraform/CloudFormation) com os mesmos nomes configurados em `app.sqs.*` ou sobrescreva via variáveis de ambiente.

| Propriedade | Local (LocalStack) | AWS |
|-------------|-------------------|-----|
| `spring.cloud.aws.sqs.endpoint` | `http://localhost:4566` | *(não definir)* |
| `spring.cloud.aws.region.static` | `us-east-1` | região do deploy |
| Credenciais | `test` / `test` | IAM role / credenciais do ambiente |

---

## Pré-requisitos

- **Java 17**
- **Maven** (ou use o wrapper `./mvnw` incluído no projeto)
- **Docker** e **Docker Compose**
- **AWS CLI** (opcional, para inspecionar filas no LocalStack)
- **PostgreSQL 14+** (local ou via Docker)

---

## Rodando localmente

Todos os comandos abaixo assumem o diretório `auth-ms/` (onde estão `pom.xml`, `Dockerfile` e `docker-compose.yml`):

```bash
cd auth-ms
```

### 1. Subir o PostgreSQL

O `docker-compose` do projeto **não inclui** Postgres — ele roda separadamente para flexibilidade entre desenvolvimento local e deploy na AWS (RDS).

**Opção A — container Docker dedicado (recomendado):**

```bash
docker run -d \
  --name auth-ms-postgres \
  -e POSTGRES_DB=auth_ms \
  -e POSTGRES_USER=usr \
  -e POSTGRES_PASSWORD=pwd \
  -p 5432:5432 \
  postgres:latest
```

**Parar / remover:**

```bash
docker stop auth-ms-postgres
docker rm auth-ms-postgres
```

**Opção B — recriar do zero (apaga dados):**

```bash
docker rm -f auth-ms-postgres 2>/dev/null
docker run -d \
  --name auth-ms-postgres \
  -e POSTGRES_DB=auth_ms \
  -e POSTGRES_USER=usr \
  -e POSTGRES_PASSWORD=pwd \
  -p 5432:5432 \
  postgres:latest
```

### 2. Subir o LocalStack (SQS)

```bash
docker compose up -d localstack
```

Aguarde o status `Ready` nos logs:

```bash
docker compose logs -f localstack
```

### 3. Rodar a aplicação (Maven)

```bash
./mvnw spring-boot:run
```

Na primeira execução, o **Liquibase** cria a tabela `tb_user` automaticamente.

A aplicação sobe em **http://localhost:8082**.

### 4. Fluxo completo de validação

```bash
# 1. Criar usuário
curl -s -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste","email":"teste@email.com","password":"senha123"}'

# 2. Login (copie o token da resposta)
curl -s -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@email.com","password":"senha123"}'

# 3. Verificar evento na fila
aws --endpoint-url=http://localhost:4566 sqs receive-message \
  --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/user-created-queue
```

---

## Docker

### Subir app + LocalStack (Postgres no host)

Com o Postgres rodando no host (porta `5432`), suba o restante da stack:

```bash
cd auth-ms
docker compose up --build
```

O container `auth-ms` conecta ao Postgres via `host.docker.internal` e ao LocalStack pelo hostname `localstack`.

**Customizar host do banco:**

```bash
DB_HOST=192.168.1.10 docker compose up --build
```

**Customizar JWT secret:**

```bash
JWT_SECRET=seu-secret-com-pelo-menos-32-caracteres docker compose up --build
```

> Defina `JWT_SECRET` no ambiente do host antes do `docker compose up`. O valor é lido pelo Spring via `jwt.secret=${JWT_SECRET:...}` em `application.properties`.

### Dockerfile

Build multi-stage em [`auth-ms/Dockerfile`](auth-ms/Dockerfile):

1. **Build:** `maven:3.9-eclipse-temurin-17` — resolve dependências e gera o JAR
2. **Runtime:** `eclipse-temurin:17-jre-alpine` — executa como usuário não-root

```bash
cd auth-ms
docker build -t auth-ms:local .
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/auth_ms \
  -e SPRING_DATASOURCE_USERNAME=usr \
  -e SPRING_DATASOURCE_PASSWORD=pwd \
  -e SPRING_CLOUD_AWS_SQS_ENDPOINT=http://host.docker.internal:4566 \
  --add-host=host.docker.internal:host-gateway \
  auth-ms:local
```

---

## Variáveis de ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_DATASOURCE_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/auth_ms` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `usr` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `pwd` |
| `JWT_SECRET` | Chave HMAC para assinar JWT | Valor default em `application.properties` |
| `JWT_EXPIRATION_MS` | Expiração do token (via `jwt.expiration-ms`) | `86400000` (24h) |
| `SPRING_CLOUD_AWS_SQS_ENDPOINT` | Endpoint SQS (LocalStack) | `http://localhost:4566` |
| `SPRING_LIQUIBASE_ENABLED` | Habilita migrations no startup | `true` |
| `DB_HOST` | Host do Postgres (apenas docker-compose) | `host.docker.internal` |

---

## Banco de dados e Liquibase

O schema é gerenciado pelo **Liquibase**, não pelo Hibernate (`ddl-auto=validate`).

```
auth-ms/src/main/resources/db/changelog/
├── master.xml      # Ponto de entrada — inclui changelogs por tabela
└── tb_user.xml     # Criação da tabela tb_user
```

### Tabela `tb_user`

| Coluna | Tipo | Constraints |
|--------|------|-------------|
| `id` | UUID | PK |
| `name` | VARCHAR(255) | nullable |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | NOT NULL (BCrypt) |

### Adicionar novas migrations

1. Crie `db/changelog/<nome>.xml` com um novo `changeSet`
2. Inclua em `master.xml`:

```xml
<include file="<nome>.xml" relativeToChangelogFile="true"/>
```

> Nunca altere changesets já aplicados. O Liquibase registra o histórico em `databasechangelog`.

### Liquibase em produção (ASG / múltiplas instâncias)

O Liquibase roda em todo startup da aplicação. Para ambientes com auto scaling, migrations já aplicadas são ignoradas (consulta rápida ao `databasechangelog`). Para deploys com schema novo em escala, considere rodar migrations uma única vez no CI/CD antes do deploy.

---

## Testes

```bash
cd auth-ms
./mvnw test
```

O teste de contexto (`AuthMsApplicationTests`) desabilita o SQS (`spring.cloud.aws.sqs.enabled=false`) e mocka o `UserEventPublisher`. Requer PostgreSQL acessível em `localhost:5432`.

---

## Swagger

Documentação interativa disponível em:

- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8082/v3/api-docs

---

## Estrutura do repositório

```
.
├── README.md                 # Este arquivo
└── auth-ms/                  # Projeto Spring Boot
    ├── Dockerfile
    ├── docker-compose.yml
    ├── pom.xml
    ├── mvnw
    ├── localstack/
    │   └── init/
    │       └── create-queues.sh
    └── src/
        ├── main/
        │   ├── java/...
        │   └── resources/
        │       ├── application.properties
        │       └── db/changelog/
        └── test/
```

---
