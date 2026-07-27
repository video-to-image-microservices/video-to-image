terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "terraform-vpc"
  }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "public-subnet"
  }
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_b_cidr
  availability_zone       = "${var.aws_region}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "public-subnet-b"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "internet-gateway"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "public-route-table"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}

resource "aws_subnet" "auth_ms" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.auth_ms_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "auth-ms-subnet"
  }
}

resource "aws_subnet" "auth_ms_documentdb" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.auth_ms_documentdb_subnet_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "auth-ms-documentdb-subnet"
  }
}

resource "aws_subnet" "auth_ms_documentdb_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.auth_ms_documentdb_b_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "auth-ms-documentdb-subnet-b"
  }
}

resource "aws_subnet" "auth_ms_elasticache" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.auth_ms_elasticache_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "auth-ms-elasticache-subnet"
  }
}

resource "aws_subnet" "auth_ms_elasticache_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.auth_ms_elasticache_b_subnet_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "auth-ms-elasticache-subnet-b"
  }
}

resource "aws_eip" "nat" {
  domain = "vpc"

  tags = {
    Name = "nat-eip"
  }

  depends_on = [
    aws_internet_gateway.igw
  ]
}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public.id

  tags = {
    Name = "nat-gateway"
  }

  depends_on = [
    aws_internet_gateway.igw
  ]
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  tags = {
    Name = "private-route-table"
  }
}

resource "aws_route_table_association" "auth_ms" {
  subnet_id      = aws_subnet.auth_ms.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "auth_ms_documentdb" {
  subnet_id      = aws_subnet.auth_ms_documentdb.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "auth_ms_documentdb_b" {
  subnet_id      = aws_subnet.auth_ms_documentdb_b.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "auth_ms_elasticache" {
  subnet_id      = aws_subnet.auth_ms_elasticache.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "auth_ms_elasticache_b" {
  subnet_id      = aws_subnet.auth_ms_elasticache_b.id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "alb" {
  name        = "alb-sg"
  description = "ALB security group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "alb-sg"
  }
}

resource "aws_security_group" "auth_ms" {
  name        = "auth-ms-sg"
  description = "Security group for auth-ms instances"
  vpc_id      = aws_vpc.main.id

  # aceita trafego apenas do ALB
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "auth-ms-sg"
  }
}

resource "aws_security_group" "auth_ms_documentdb" {
  name        = "auth-ms-documentdb-sg"
  description = "Security group for auth-ms MongoDB on EC2"
  vpc_id      = aws_vpc.main.id

  # aceita trafego apenas das instâncias de auth-ms
  ingress {
    from_port       = 27017
    to_port         = 27017
    protocol        = "tcp"
    security_groups = [aws_security_group.auth_ms.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "auth-ms-documentdb-sg"
  }
}

resource "aws_security_group" "auth_ms_elasticache" {
  name        = "auth-ms-elasticache-sg"
  description = "Security group for auth-ms ElastiCache Redis"
  vpc_id      = aws_vpc.main.id

  # aceita trafego apenas das instâncias de auth-ms
  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.auth_ms.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "auth-ms-elasticache-sg"
  }
}

resource "aws_elasticache_subnet_group" "auth_ms" {
  name = "auth-ms-elasticache-subnet-group"
  subnet_ids = [
    aws_subnet.auth_ms_elasticache.id,
    aws_subnet.auth_ms_elasticache_b.id,
  ]

  tags = {
    Name = "auth-ms-elasticache-subnet-group"
  }
}

resource "aws_elasticache_cluster" "auth_ms" {
  cluster_id           = "auth-ms-redis"
  engine               = "redis"
  engine_version       = "7.1"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.auth_ms.name
  security_group_ids   = [aws_security_group.auth_ms_elasticache.id]

  tags = {
    Name = "auth-ms-redis"
  }
}

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_iam_role" "auth_ms_mongo" {
  name = "auth-ms-mongo-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "auth-ms-mongo-role"
  }
}

resource "aws_iam_role_policy_attachment" "auth_ms_mongo_ssm" {
  role       = aws_iam_role.auth_ms_mongo.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "auth_ms_mongo" {
  name = "auth-ms-mongo-profile"
  role = aws_iam_role.auth_ms_mongo.name
}

resource "aws_instance" "auth_ms_mongo" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = "t3.micro"
  subnet_id              = aws_subnet.auth_ms_documentdb.id
  vpc_security_group_ids = [aws_security_group.auth_ms_documentdb.id]
  iam_instance_profile   = aws_iam_instance_profile.auth_ms_mongo.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = <<-EOF
#!/bin/bash
set -euo pipefail

cat > /etc/yum.repos.d/mongodb-org-7.0.repo <<'REPO'
[mongodb-org-7.0]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/amazon/2023/mongodb-org/7.0/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://pgp.mongodb.com/server-7.0.asc
REPO

# Instala só o server (evita estouro de disco com tools/mongosh)
dnf install -y mongodb-org-server
sed -i 's/bindIp: 127.0.0.1/bindIp: 0.0.0.0/' /etc/mongod.conf
systemctl enable mongod
systemctl start mongod
EOF

  user_data_replace_on_change = true

  tags = {
    Name = "auth-ms-mongo"
  }
}

resource "aws_sqs_queue" "user_created_dlq" {
  name = "user-created-queue-dlq"
}

resource "aws_sqs_queue" "user_created" {
  name                       = "user-created-queue"
  visibility_timeout_seconds = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.user_created_dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "user_deleted_dlq" {
  name = "user-deleted-queue-dlq"
}

resource "aws_sqs_queue" "user_deleted" {
  name                       = "user-deleted-queue"
  visibility_timeout_seconds = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.user_deleted_dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "video_status_dlq" {
  name = "video-status-queue-dlq"
}

resource "aws_sqs_queue" "video_status" {
  name                       = "video-status-queue"
  visibility_timeout_seconds = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.video_status_dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "process_dlq" {
  name = "process-queue-dlq"
}

resource "aws_sqs_queue" "process" {
  name                       = "process-queue"
  visibility_timeout_seconds = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.process_dlq.arn
    maxReceiveCount     = 3
  })
}

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_iam_role" "auth_ms" {
  name = "auth-ms-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "auth-ms-ec2-role"
  }
}

resource "aws_iam_role_policy_attachment" "auth_ms_ssm" {
  role       = aws_iam_role.auth_ms.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "auth_ms_sqs" {
  name = "auth-ms-sqs-publish"
  role = aws_iam_role.auth_ms.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:GetQueueUrl",
          "sqs:GetQueueAttributes"
        ]
        Resource = [
          aws_sqs_queue.user_created.arn,
          aws_sqs_queue.user_deleted.arn
        ]
      }
    ]
  })
}

resource "aws_iam_instance_profile" "auth_ms" {
  name = "auth-ms-ec2-profile"
  role = aws_iam_role.auth_ms.name
}

resource "aws_launch_template" "auth_ms" {
  name_prefix   = "auth-ms-"
  image_id      = data.aws_ami.ubuntu.id
  instance_type = "t3.micro"

  vpc_security_group_ids = [aws_security_group.auth_ms.id]

  iam_instance_profile {
    name = aws_iam_instance_profile.auth_ms.name
  }

  # Hop limit 2 para o container Docker ler o instance profile (IMDSv2)
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  user_data = base64encode(<<-EOF
#!/bin/bash
set -euo pipefail
apt-get update -y
apt-get install -y docker.io
systemctl enable docker
systemctl start docker

docker pull caiqueluci0/auth-ms:1.0

docker rm -f api 2>/dev/null || true
docker run -d --restart always --name api -p 8080:8082 \
  -e MONGODB_URI=mongodb://${aws_instance.auth_ms_mongo.private_ip}:27017/auth_ms \
  -e SPRING_MONGODB_URI=mongodb://${aws_instance.auth_ms_mongo.private_ip}:27017/auth_ms \
  -e REDIS_HOST=${aws_elasticache_cluster.auth_ms.cache_nodes[0].address} \
  -e SPRING_DATA_REDIS_HOST=${aws_elasticache_cluster.auth_ms.cache_nodes[0].address} \
  -e REDIS_PORT=6379 \
  -e SPRING_DATA_REDIS_PORT=6379 \
  -e AWS_REGION=${var.aws_region} \
  -e SPRING_CLOUD_AWS_REGION_STATIC=${var.aws_region} \
  caiqueluci0/auth-ms:1.0
EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "auth-ms"
    }
  }
}

resource "aws_autoscaling_group" "auth_ms" {
  name                      = "auth-ms-asg"
  desired_capacity          = 1
  min_size                  = 1
  max_size                  = 5
  vpc_zone_identifier       = [aws_subnet.auth_ms.id]
  target_group_arns         = [aws_lb_target_group.auth_ms.arn]
  health_check_type         = "EC2"
  health_check_grace_period = 300

  launch_template {
    id      = aws_launch_template.auth_ms.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "auth-ms"
    propagate_at_launch = true
  }
}

resource "aws_lb" "main" {
  name               = "main-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = [aws_subnet.public.id, aws_subnet.public_b.id]

  tags = {
    Name = "main-alb"
  }
}

resource "aws_lb_target_group" "auth_ms" {
  name     = "auth-ms-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    enabled             = true
    path                = "/auth/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  tags = {
    Name = "auth-ms-tg"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

resource "aws_lb_listener_rule" "auth_ms" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.auth_ms.arn
  }

  condition {
    path_pattern {
      values = ["/auth", "/auth/*"]
    }
  }
}