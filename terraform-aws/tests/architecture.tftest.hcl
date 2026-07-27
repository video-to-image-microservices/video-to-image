mock_provider "aws" {
  mock_data "aws_caller_identity" {
    defaults = {
      account_id = "123456789012"
    }
  }

  mock_data "aws_ami" {
    defaults = {
      id = "ami-0123456789abcdef0"
    }
  }
}

run "dedicated_architecture" {
  command = plan

  assert {
    condition     = aws_autoscaling_group.worker_ms.max_size == var.worker_ms_max_size
    error_message = "worker-ms ASG must use the configured maximum size."
  }

  assert {
    condition     = aws_sqs_queue.process.visibility_timeout_seconds == 3600
    error_message = "The process queue visibility timeout must cover long video jobs."
  }

  assert {
    condition     = aws_lb_target_group.auth_ms.health_check[0].path == "/auth/actuator/health"
    error_message = "auth-ms target group health check is incorrect."
  }

  assert {
    condition     = aws_lb_target_group.management_ms.health_check[0].path == "/management/actuator/health"
    error_message = "management-ms target group health check is incorrect."
  }

  assert {
    condition     = aws_db_instance.management.publicly_accessible == false
    error_message = "The management database must remain in private subnets."
  }

  assert {
    condition     = aws_instance.auth_ms_mongo[0].instance_type == "t3.micro"
    error_message = "The Free Plan fallback MongoDB instance must remain small."
  }

  assert {
    condition     = aws_cloudwatch_dashboard.operations.dashboard_name == "${var.project_name}-operations"
    error_message = "The operational CloudWatch dashboard must be provisioned."
  }

  assert {
    condition     = aws_cloudwatch_metric_alarm.process_dlq_not_empty.threshold == 1
    error_message = "A single message in the processing DLQ must trigger an alarm."
  }

  assert {
    condition     = aws_cloudwatch_metric_alarm.auth_ms_unhealthy.treat_missing_data == "breaching"
    error_message = "Missing auth-ms health metrics must be treated as an outage."
  }
}
