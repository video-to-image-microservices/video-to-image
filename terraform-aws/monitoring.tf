locals {
  monitoring_dashboard_name = "${var.project_name}-operations"
  monitoring_queue_names = {
    process          = "process-queue"
    process_dlq      = "process-queue-dlq"
    user_created_dlq = "user-created-queue-dlq"
    user_deleted_dlq = "user-deleted-queue-dlq"
    video_status_dlq = "video-status-queue-dlq"
  }
  monitoring_asg_names = {
    auth       = "auth-ms-asg"
    management = "management-ms-asg"
    worker     = "worker-ms-asg"
  }
}

data "aws_lb" "monitoring" {
  name = var.monitoring_alb_name
}

data "aws_lb_target_group" "auth_ms_monitoring" {
  name = var.monitoring_auth_target_group_name
}

data "aws_lb_target_group" "management_ms_monitoring" {
  name = var.monitoring_management_target_group_name
}

resource "aws_cloudwatch_metric_alarm" "auth_ms_unhealthy" {
  alarm_name          = "${var.project_name}-auth-ms-no-healthy-targets"
  alarm_description   = "O auth-ms ficou sem targets saudaveis no ALB."
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "HealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Minimum"
  threshold           = 1
  treat_missing_data  = "breaching"

  dimensions = {
    LoadBalancer = data.aws_lb.monitoring.arn_suffix
    TargetGroup  = data.aws_lb_target_group.auth_ms_monitoring.arn_suffix
  }
}

resource "aws_cloudwatch_metric_alarm" "management_ms_unhealthy" {
  alarm_name          = "${var.project_name}-management-ms-no-healthy-targets"
  alarm_description   = "O management-ms ficou sem targets saudaveis no ALB."
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "HealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Minimum"
  threshold           = 1
  treat_missing_data  = "breaching"

  dimensions = {
    LoadBalancer = data.aws_lb.monitoring.arn_suffix
    TargetGroup  = data.aws_lb_target_group.management_ms_monitoring.arn_suffix
  }
}

resource "aws_cloudwatch_metric_alarm" "process_dlq_not_empty" {
  alarm_name          = "${var.project_name}-process-dlq-not-empty"
  alarm_description   = "Existe processamento de video na dead-letter queue."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 60
  statistic           = "Maximum"
  threshold           = 1
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = local.monitoring_queue_names.process_dlq
  }
}

resource "aws_cloudwatch_dashboard" "operations" {
  dashboard_name = local.monitoring_dashboard_name

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "Targets saudaveis no ALB"
          region = var.aws_region
          stat   = "Minimum"
          period = 60
          metrics = [
            [
              "AWS/ApplicationELB",
              "HealthyHostCount",
              "LoadBalancer",
              data.aws_lb.monitoring.arn_suffix,
              "TargetGroup",
              data.aws_lb_target_group.auth_ms_monitoring.arn_suffix,
              { label = "auth-ms" }
            ],
            [
              "AWS/ApplicationELB",
              "HealthyHostCount",
              "LoadBalancer",
              data.aws_lb.monitoring.arn_suffix,
              "TargetGroup",
              data.aws_lb_target_group.management_ms_monitoring.arn_suffix,
              { label = "management-ms" }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "Trafego e erros do ALB"
          region = var.aws_region
          period = 60
          metrics = [
            [
              "AWS/ApplicationELB",
              "RequestCount",
              "LoadBalancer",
              data.aws_lb.monitoring.arn_suffix,
              { stat = "Sum", label = "Requisicoes" }
            ],
            [
              ".",
              "HTTPCode_Target_5XX_Count",
              ".",
              ".",
              { stat = "Sum", label = "HTTP 5xx" }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Fila de processamento"
          region = var.aws_region
          period = 60
          metrics = [
            [
              "AWS/SQS",
              "ApproximateNumberOfMessagesVisible",
              "QueueName",
              local.monitoring_queue_names.process,
              { label = "Aguardando" }
            ],
            [
              ".",
              "ApproximateNumberOfMessagesNotVisible",
              ".",
              ".",
              { label = "Em processamento" }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Mensagens nas DLQs"
          region = var.aws_region
          stat   = "Maximum"
          period = 60
          metrics = [
            ["AWS/SQS", "ApproximateNumberOfMessagesVisible", "QueueName", local.monitoring_queue_names.user_created_dlq, { label = "user-created" }],
            [".", ".", ".", local.monitoring_queue_names.user_deleted_dlq, { label = "user-deleted" }],
            [".", ".", ".", local.monitoring_queue_names.video_status_dlq, { label = "video-status" }],
            [".", ".", ".", local.monitoring_queue_names.process_dlq, { label = "process" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 24
        height = 6
        properties = {
          title  = "Instancias desejadas dos Auto Scaling Groups"
          region = var.aws_region
          period = 60
          metrics = [
            ["AWS/AutoScaling", "GroupDesiredCapacity", "AutoScalingGroupName", local.monitoring_asg_names.auth, { label = "auth-ms" }],
            [".", ".", ".", local.monitoring_asg_names.management, { label = "management-ms" }],
            [".", ".", ".", local.monitoring_asg_names.worker, { label = "worker-ms" }]
          ]
        }
      }
    ]
  })
}

output "cloudwatch_dashboard_name" {
  description = "Dashboard operacional nativo e de baixo custo."
  value       = aws_cloudwatch_dashboard.operations.dashboard_name
}

output "critical_alarm_names" {
  description = "Alarmes que devem permanecer em estado OK."
  value = [
    aws_cloudwatch_metric_alarm.auth_ms_unhealthy.alarm_name,
    aws_cloudwatch_metric_alarm.management_ms_unhealthy.alarm_name,
    aws_cloudwatch_metric_alarm.process_dlq_not_empty.alarm_name,
  ]
}
