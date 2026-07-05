# Video to Image Microservices

<h1>Arquitetura</h1>
<h2>Diagrama Generalista</h2>
<h3>Arquitetura Back-end</h3>
<img width="3918" height="2608" alt="Diagrama-generalista-backend" src="https://github.com/user-attachments/assets/398e5283-c837-494c-bee8-7c6ffb03b07f" />

Sistema distribuído para processamento assíncrono de vídeos, projetado para receber uploads de vídeos, processá-los em paralelo e disponibilizar as imagens extraídas em um arquivo ZIP para download.

A aplicação foi desenvolvida com uma arquitetura baseada em microsserviços e comunicação orientada a eventos, priorizando escalabilidade, alta disponibilidade, resiliência e desacoplamento entre os componentes.

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

A solução segue princípios de **Clean Architecture**, **Domain-Driven Design (DDD)** e arquitetura orientada a eventos, permitindo que os serviços evoluam de forma independente e escalem horizontalmente conforme a demanda.

## Tecnologias

* Java (Spring Boot)
* PostgreSQL
* Redis
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



