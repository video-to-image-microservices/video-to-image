resource "aws_subnet" "auth_ms_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.auth_ms_subnet_b_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "auth-ms-subnet-b"
  }
}

resource "aws_subnet" "management_ms" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.management_ms_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "management-ms-subnet"
  }
}

resource "aws_subnet" "management_ms_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.management_ms_subnet_b_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "management-ms-subnet-b"
  }
}

resource "aws_subnet" "worker_ms" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.worker_ms_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "worker-ms-subnet"
  }
}

resource "aws_subnet" "worker_ms_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.worker_ms_subnet_b_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "worker-ms-subnet-b"
  }
}

resource "aws_subnet" "management_rds" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.management_rds_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "management-rds-subnet"
  }
}

resource "aws_subnet" "management_rds_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.management_rds_subnet_b_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "management-rds-subnet-b"
  }
}

resource "aws_subnet" "management_redis" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.management_redis_subnet_cidr
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "management-redis-subnet"
  }
}

resource "aws_subnet" "management_redis_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.management_redis_subnet_b_cidr
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "management-redis-subnet-b"
  }
}

locals {
  private_subnet_ids = {
    auth_ms_b          = aws_subnet.auth_ms_b.id
    management_ms      = aws_subnet.management_ms.id
    management_ms_b    = aws_subnet.management_ms_b.id
    worker_ms          = aws_subnet.worker_ms.id
    worker_ms_b        = aws_subnet.worker_ms_b.id
    management_rds     = aws_subnet.management_rds.id
    management_rds_b   = aws_subnet.management_rds_b.id
    management_redis   = aws_subnet.management_redis.id
    management_redis_b = aws_subnet.management_redis_b.id
  }
}

resource "aws_route_table_association" "service_private" {
  for_each = local.private_subnet_ids

  subnet_id      = each.value
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "vpc_endpoints" {
  name        = "aws-vpc-endpoints-sg"
  description = "HTTPS from application instances to AWS interface endpoints"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    security_groups = [
      aws_security_group.auth_ms.id,
      aws_security_group.management_ms.id,
      aws_security_group.worker_ms.id,
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "aws-vpc-endpoints-sg"
  }
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = aws_vpc.main.id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [aws_route_table.private.id]

  tags = {
    Name = "s3-gateway-endpoint"
  }
}

resource "aws_vpc_endpoint" "sqs" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.sqs"
  vpc_endpoint_type   = "Interface"
  private_dns_enabled = true
  subnet_ids          = [aws_subnet.auth_ms.id, aws_subnet.auth_ms_b.id]
  security_group_ids  = [aws_security_group.vpc_endpoints.id]

  tags = {
    Name = "sqs-interface-endpoint"
  }
}
