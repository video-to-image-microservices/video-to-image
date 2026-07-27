resource "aws_security_group" "worker_ms" {
  name        = "worker-ms-sg"
  description = "Outbound-only security group for worker-ms"
  vpc_id      = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "worker-ms-sg"
  }
}

resource "aws_iam_role" "worker_ms" {
  name = "worker-ms-ec2-role"

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

resource "aws_iam_role_policy_attachment" "worker_ms_ssm" {
  role       = aws_iam_role.worker_ms.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "worker_ms_ecr" {
  role       = aws_iam_role.worker_ms.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy" "worker_ms" {
  name = "worker-ms-s3-sqs"
  role = aws_iam_role.worker_ms.id

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
          aws_sqs_queue.process.arn,
          aws_sqs_queue.video_status.arn,
        ]
      },
    ]
  })
}

resource "aws_iam_instance_profile" "worker_ms" {
  name = "worker-ms-ec2-profile"
  role = aws_iam_role.worker_ms.name
}

resource "aws_launch_template" "worker_ms" {
  name_prefix   = "worker-ms-"
  image_id      = data.aws_ami.ubuntu.id
  instance_type = var.worker_ms_instance_type

  vpc_security_group_ids = [aws_security_group.worker_ms.id]

  iam_instance_profile {
    name = aws_iam_instance_profile.worker_ms.name
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  block_device_mappings {
    device_name = "/dev/sda1"

    ebs {
      volume_size           = 30
      volume_type           = "gp3"
      delete_on_termination = true
      encrypted             = true
    }
  }

  user_data = base64encode(<<-EOF
#!/bin/bash
set -euo pipefail
apt-get update -y
apt-get install -y awscli docker.io
systemctl enable --now docker

aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${local.ecr_registry}
docker pull ${aws_ecr_repository.worker_ms.repository_url}:latest
docker rm -f worker-ms 2>/dev/null || true
docker run -d --restart always --name worker-ms \
  -e AWS_REGION=${var.aws_region} \
  -e SPRING_CLOUD_AWS_REGION_STATIC=${var.aws_region} \
  -e APP_SQS_PROCESS_QUEUE=${aws_sqs_queue.process.name} \
  -e APP_SQS_STATUS_QUEUE=${aws_sqs_queue.video_status.name} \
  -e APP_PROCESSING_FPS=1 \
  ${aws_ecr_repository.worker_ms.repository_url}:latest
EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "worker-ms"
    }
  }
}

resource "aws_autoscaling_group" "worker_ms" {
  name                      = "worker-ms-asg"
  desired_capacity          = var.worker_ms_desired_capacity
  min_size                  = var.worker_ms_min_size
  max_size                  = var.worker_ms_max_size
  vpc_zone_identifier       = [aws_subnet.worker_ms.id, aws_subnet.worker_ms_b.id]
  health_check_type         = "EC2"
  health_check_grace_period = 300

  launch_template {
    id      = aws_launch_template.worker_ms.id
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
    value               = "worker-ms"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_policy" "worker_scale_out" {
  name                      = "worker-ms-sqs-scale-out"
  autoscaling_group_name    = aws_autoscaling_group.worker_ms.name
  policy_type               = "StepScaling"
  adjustment_type           = "ChangeInCapacity"
  estimated_instance_warmup = 300

  step_adjustment {
    metric_interval_lower_bound = 0
    scaling_adjustment          = 1
  }
}

resource "aws_autoscaling_policy" "worker_scale_in" {
  name                   = "worker-ms-sqs-scale-in"
  autoscaling_group_name = aws_autoscaling_group.worker_ms.name
  policy_type            = "StepScaling"
  adjustment_type        = "ChangeInCapacity"

  step_adjustment {
    metric_interval_upper_bound = 0
    scaling_adjustment          = -1
  }
}

resource "aws_cloudwatch_metric_alarm" "worker_queue_high" {
  alarm_name          = "worker-ms-process-queue-high"
  alarm_description   = "Adds a worker while process messages are waiting"
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 1
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_autoscaling_policy.worker_scale_out.arn]

  dimensions = {
    QueueName = aws_sqs_queue.process.name
  }
}

resource "aws_cloudwatch_metric_alarm" "worker_queue_empty" {
  alarm_name          = "worker-ms-process-queue-empty"
  alarm_description   = "Removes an idle worker after the queue stays empty"
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 5
  datapoints_to_alarm = 5
  threshold           = 0
  comparison_operator = "LessThanOrEqualToThreshold"
  treat_missing_data  = "breaching"
  alarm_actions       = [aws_autoscaling_policy.worker_scale_in.arn]

  dimensions = {
    QueueName = aws_sqs_queue.process.name
  }
}
