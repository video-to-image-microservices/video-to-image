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
  value = aws_instance.auth_ms_mongo.private_ip
}

output "auth_ms_mongo_uri" {
  value = "mongodb://${aws_instance.auth_ms_mongo.private_ip}:27017/auth_ms"
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