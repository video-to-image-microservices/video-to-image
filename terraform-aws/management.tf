resource "aws_security_group" "management_ms" {
  name        = "management-ms-sg"
  description = "Traffic from the ALB to management-ms"
  vpc_id      = aws_vpc.main.id

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
    Name = "management-ms-sg"
  }
}

resource "aws_security_group" "management_rds" {
  name        = "management-rds-sg"
  description = "PostgreSQL access from management-ms"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.management_ms.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "management-rds-sg"
  }
}

resource "aws_security_group" "management_redis" {
  name        = "management-redis-sg"
  description = "Redis access from management-ms"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.management_ms.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "management-redis-sg"
  }
}

resource "aws_db_subnet_group" "management" {
  name = "management-rds-subnet-group"
  subnet_ids = [
    aws_subnet.management_rds.id,
    aws_subnet.management_rds_b.id,
  ]

  tags = {
    Name = "management-rds-subnet-group"
  }
}

resource "aws_db_instance" "management" {
  identifier                 = "management-postgres"
  engine                     = "postgres"
  engine_version             = "16"
  instance_class             = var.rds_instance_class
  allocated_storage          = 20
  max_allocated_storage      = 100
  storage_type               = "gp3"
  storage_encrypted          = true
  db_name                    = var.rds_database_name
  username                   = var.rds_username
  password                   = var.rds_password
  db_subnet_group_name       = aws_db_subnet_group.management.name
  vpc_security_group_ids     = [aws_security_group.management_rds.id]
  publicly_accessible        = false
  multi_az                   = false
  backup_retention_period    = 1
  deletion_protection        = false
  skip_final_snapshot        = true
  auto_minor_version_upgrade = true

  tags = {
    Name = "management-postgres"
  }
}

resource "aws_elasticache_subnet_group" "management" {
  name = "management-redis-subnet-group"
  subnet_ids = [
    aws_subnet.management_redis.id,
    aws_subnet.management_redis_b.id,
  ]

  tags = {
    Name = "management-redis-subnet-group"
  }
}

resource "aws_elasticache_cluster" "management" {
  cluster_id           = "management-redis"
  engine               = "redis"
  engine_version       = "7.1"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.management.name
  security_group_ids   = [aws_security_group.management_redis.id]

  tags = {
    Name = "management-redis"
  }
}

resource "aws_iam_role" "management_ms" {
  name = "management-ms-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "management_ms_ssm" {
  role       = aws_iam_role.management_ms.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "management_ms_ecr" {
  role       = aws_iam_role.management_ms.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy" "management_ms" {
  name = "management-ms-s3-sqs"
  role = aws_iam_role.management_ms.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
        ]
        Resource = "${aws_s3_bucket.videos.arn}/*"
      },
      {
        Effect   = "Allow"
        Action   = ["s3:ListBucket"]
        Resource = aws_s3_bucket.videos.arn
      },
      {
        Effect = "Allow"
        Action = [
          "sqs:GetQueueUrl",
          "sqs:GetQueueAttributes",
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:ChangeMessageVisibility",
        ]
        Resource = [
          aws_sqs_queue.user_created.arn,
          aws_sqs_queue.user_deleted.arn,
          aws_sqs_queue.video_status.arn,
          aws_sqs_queue.process.arn,
        ]
      },
    ]
  })
}

resource "aws_iam_instance_profile" "management_ms" {
  name = "management-ms-ec2-profile"
  role = aws_iam_role.management_ms.name
}

resource "aws_launch_template" "management_ms" {
  name_prefix   = "management-ms-"
  image_id      = data.aws_ami.amazon_linux.id
  instance_type = var.management_ms_instance_type

  vpc_security_group_ids = [aws_security_group.management_ms.id]

  iam_instance_profile {
    name = aws_iam_instance_profile.management_ms.name
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  user_data = base64encode(<<-EOF
#!/bin/bash
set -euo pipefail
dnf install -y docker
systemctl enable --now docker

aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${local.ecr_registry}
docker pull ${aws_ecr_repository.management_ms.repository_url}:latest
docker rm -f management-ms 2>/dev/null || true
docker run -d --restart always --name management-ms -p 8080:8080 \
  -e SERVER_SERVLET_CONTEXT_PATH=/management \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://${aws_db_instance.management.address}:5432/${var.rds_database_name} \
  -e SPRING_DATASOURCE_USERNAME=${var.rds_username} \
  -e 'SPRING_DATASOURCE_PASSWORD=${var.rds_password}' \
  -e REDIS_HOST=${aws_elasticache_cluster.management.cache_nodes[0].address} \
  -e REDIS_PORT=6379 \
  -e JWT_SECRET=${var.jwt_secret} \
  -e AWS_REGION=${var.aws_region} \
  -e SPRING_CLOUD_AWS_REGION_STATIC=${var.aws_region} \
  -e APP_SQS_USER_CREATED_QUEUE=${aws_sqs_queue.user_created.name} \
  -e APP_SQS_USER_DELETED_QUEUE=${aws_sqs_queue.user_deleted.name} \
  -e APP_SQS_STATUS_QUEUE=${aws_sqs_queue.video_status.name} \
  -e APP_SQS_PROCESS_QUEUE=${aws_sqs_queue.process.name} \
  -e APP_S3_VIDEO_BUCKET=${aws_s3_bucket.videos.id} \
  ${aws_ecr_repository.management_ms.repository_url}:latest
EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "management-ms"
    }
  }
}

resource "aws_lb_target_group" "management_ms" {
  name     = "management-ms-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    enabled             = true
    path                = "/management/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  tags = {
    Name = "management-ms-tg"
  }
}

resource "aws_autoscaling_group" "management_ms" {
  name                      = "management-ms-asg"
  desired_capacity          = var.management_ms_desired_capacity
  min_size                  = var.management_ms_min_size
  max_size                  = var.management_ms_max_size
  vpc_zone_identifier       = [aws_subnet.management_ms.id, aws_subnet.management_ms_b.id]
  target_group_arns         = [aws_lb_target_group.management_ms.arn]
  health_check_type         = "ELB"
  health_check_grace_period = 3600

  launch_template {
    id      = aws_launch_template.management_ms.id
    version = "$Latest"
  }

  instance_refresh {
    strategy = "Rolling"

    preferences {
      min_healthy_percentage = 50
    }
  }

  tag {
    key                 = "Name"
    value               = "management-ms"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_policy" "management_ms_cpu" {
  name                   = "management-ms-cpu-target"
  autoscaling_group_name = aws_autoscaling_group.management_ms.name
  policy_type            = "TargetTrackingScaling"

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }

    target_value     = 60
    disable_scale_in = false
  }
}

resource "aws_lb_listener_rule" "management_ms" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 20

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.management_ms.arn
  }

  condition {
    path_pattern {
      values = ["/management", "/management/*"]
    }
  }
}
