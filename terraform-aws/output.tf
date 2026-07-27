output "vpc_id" {
  value = aws_vpc.main.id
}

output "subnet_id" {
  value = aws_subnet.public.id
}

output "internet_gateway_id" {
  value = aws_internet_gateway.igw.id
}

output "auth_ms_subnet_id" {
  value = aws_subnet.auth_ms.id
}

output "auth_ms_documentdb_subnet_id" {
  value = aws_subnet.auth_ms_documentdb.id
}

output "auth_ms_documentdb_b_subnet_id" {
  value = aws_subnet.auth_ms_documentdb_b.id
}

output "auth_ms_elasticache_subnet_id" {
  value = aws_subnet.auth_ms_elasticache.id
}

output "auth_ms_elasticache_b_subnet_id" {
  value = aws_subnet.auth_ms_elasticache_b.id
}

output "nat_gateway_id" {
  value = aws_nat_gateway.main.id
}

output "alb_security_group_id" {
  value = aws_security_group.alb.id
}

output "auth_ms_security_group_id" {
  value = aws_security_group.auth_ms.id
}

output "auth_ms_documentdb_security_group_id" {
  value = aws_security_group.auth_ms_documentdb.id
}

output "auth_ms_elasticache_security_group_id" {
  value = aws_security_group.auth_ms_elasticache.id
}

output "auth_ms_mongo_private_ip" {
  value       = aws_instance.auth_ms_mongo[0].private_ip
  description = "Private IP of the dedicated MongoDB fallback required by AWS Free Plan."
}

output "auth_ms_mongo_uri" {
  value     = "mongodb://${aws_instance.auth_ms_mongo[0].private_ip}:27017/auth_ms"
  sensitive = true
}

output "auth_ms_redis_host" {
  value = aws_elasticache_cluster.auth_ms.cache_nodes[0].address
}

output "auth_ms_redis_port" {
  value = aws_elasticache_cluster.auth_ms.port
}

output "auth_ms_redis_uri" {
  value = "redis://${aws_elasticache_cluster.auth_ms.cache_nodes[0].address}:${aws_elasticache_cluster.auth_ms.port}"
}

output "user_created_queue_url" {
  value = aws_sqs_queue.user_created.url
}

output "user_deleted_queue_url" {
  value = aws_sqs_queue.user_deleted.url
}

output "video_status_queue_url" {
  value = aws_sqs_queue.video_status.url
}

output "process_queue_url" {
  value = aws_sqs_queue.process.url
}

output "auth_ms_launch_template_id" {
  value = aws_launch_template.auth_ms.id
}

output "auth_ms_asg_name" {
  value = aws_autoscaling_group.auth_ms.name
}

output "alb_dns_name" {
  value = aws_lb.main.dns_name
}

output "auth_ms_target_group_arn" {
  value = aws_lb_target_group.auth_ms.arn
}

output "documentdb_endpoint" {
  value       = null
  description = "Unavailable on AWS Free Plan; auth_ms_mongo_private_ip is used instead."
}

output "management_rds_endpoint" {
  value = aws_db_instance.management.address
}

output "management_redis_host" {
  value = aws_elasticache_cluster.management.cache_nodes[0].address
}

output "video_bucket_name" {
  value = aws_s3_bucket.videos.id
}

output "management_ms_asg_name" {
  value = aws_autoscaling_group.management_ms.name
}

output "worker_ms_asg_name" {
  value = aws_autoscaling_group.worker_ms.name
}

output "auth_base_url" {
  value = "http://${aws_lb.main.dns_name}/auth"
}

output "management_base_url" {
  value = "http://${aws_lb.main.dns_name}/management"
}

output "ecr_auth_ms_repository_url" {
  value = aws_ecr_repository.auth_ms.repository_url
}

output "ecr_management_ms_repository_url" {
  value = aws_ecr_repository.management_ms.repository_url
}

output "ecr_worker_ms_repository_url" {
  value = aws_ecr_repository.worker_ms.repository_url
}

output "github_actions_deploy_role_arn" {
  description = "Role assumida pelos workflows GitHub Actions via OIDC"
  value       = aws_iam_role.github_actions_deploy.arn
}
