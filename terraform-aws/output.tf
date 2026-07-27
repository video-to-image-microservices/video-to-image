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

output "auth_ms_elasticache_subnet_id" {
  value = aws_subnet.auth_ms_elasticache.id
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