resource "aws_autoscaling_policy" "auth_ms_cpu" {
  name                   = "auth-ms-cpu-target"
  autoscaling_group_name = aws_autoscaling_group.auth_ms.name
  policy_type            = "TargetTrackingScaling"

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }

    target_value     = 60
    disable_scale_in = false
  }
}
