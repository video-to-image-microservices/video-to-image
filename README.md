# Video to Image Microservices

Sistema distribuído para processamento assíncrono de vídeos, projetado para receber uploads de vídeos, processá-los em paralelo e disponibilizar as imagens extraídas em um arquivo ZIP para download.

A aplicação foi desenvolvida com uma arquitetura baseada em microsserviços e comunicação orientada a eventos, priorizando escalabilidade, alta disponibilidade, resiliência e desacoplamento entre os componentes.

<h4>Arquitetura Back-end</h4>
<img width="3918" height="2608" alt="Diagrama-generalista-backend" src="https://github.com/user-attachments/assets/398e5283-c837-494c-bee8-7c6ffb03b07f" />

## Principais funcionalidades

* Autenticação e gerenciamento de usuários.
* Upload de vídeos.
* Processamento assíncrono de múltiplos vídeos simultaneamente.
* Extração automática de frames.
* Geração e download de arquivos ZIP contendo as imagens extraídas.
* Consulta do status de processamento.
* Notificações em caso de falhas.
* Cache para otimização de desempenho.

## Arquitetura

A solução segue princípios de **Arquitetura Hexagonal** e arquitetura orientada a eventos, permitindo que os serviços evoluam de forma independente e escalem horizontalmente conforme a demanda.

## Tecnologias

* Java (Spring Boot)
* PostgreSQL (Amazon RDS)
* Redis (Amazon Elasticache for Redis)
* MongoDB (Amazon DocumentDB)
* Amazon SQS
* Docker
* GitHub Actions
* AWS (EC2, RDS, DocumentDB, ElastiCache for Redis, Application Load Balancer, Auto Scaling, S3, VPC, VPC endpoint, CloudWatch)

## Objetivos

* Escalabilidade horizontal.
* Alta disponibilidade.
* Processamento assíncrono.
* Resiliência a falhas.
* Baixo acoplamento entre serviços.
* Observabilidade e monitoramento.
* Pipeline de CI/CD automatizado.

## Modelagens de dados

### Auth-ms - MongoDB / DocumentDB
<img width="1175" height="500" alt="image" src="https://github.com/user-attachments/assets/01e189f2-f801-4373-b33a-a7447afe6628" />

#### `tb_user`
Armazena os dados dos usuários da plataforma.

**Características:**
- Senha armazenada utilizando hash BCrypt.
- Responsável pelas informações de autenticação e identificação dos usuários.

#### `mongockChangeLog`
Armazena o histórico de migrations executadas pelo Mongock.

**Características:**
- Controle de versões do banco de dados.
- Registro das migrations já aplicadas na coleção.


### Management-ms - PostgreSQL / Amazon RDS for PostgreSQL
<img width="1234" height="643" alt="image" src="https://github.com/user-attachments/assets/0de91e18-523d-493f-83fd-8c6b46299a52" />

#### `user`
Armazena o id consumido pela fila "user-created-queue"

**Características:**
- Responsável pela identificação dos usuários.
- Um usuário pode possuir muitos processamentos de vídeo (video_process)

#### `video_process`
Armazena dados referente aos pedidos de processamento de vídeo feitos por usuários

**Características:**
- Responsável pela identificação dos usuários.
- Um processo pertence a apenas um usuário.

