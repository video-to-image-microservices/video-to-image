variable "aws_region" {
  default = "us-east-1"
}

variable "project_name" {
  description = "Prefixo usado nos recursos operacionais e de monitoramento."
  type        = string
  default     = "video-to-image"
}

variable "monitoring_alb_name" {
  description = "Nome do ALB existente que fornece as métricas operacionais."
  type        = string
  default     = "main-alb"
}

variable "monitoring_auth_target_group_name" {
  description = "Nome do target group do auth-ms."
  type        = string
  default     = "auth-ms-tg"
}

variable "monitoring_management_target_group_name" {
  description = "Nome do target group do management-ms."
  type        = string
  default     = "management-ms-tg"
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  default = "10.0.1.0/24"
}

variable "public_subnet_b_cidr" {
  default = "10.0.7.0/24"
}

variable "auth_ms_subnet_cidr" {
  default = "10.0.2.0/24"
}

variable "auth_ms_documentdb_subnet_cidr" {
  default = "10.0.3.0/24"
}

variable "auth_ms_documentdb_b_subnet_cidr" {
  default = "10.0.5.0/24"
}

variable "auth_ms_elasticache_subnet_cidr" {
  default = "10.0.4.0/24"
}

variable "auth_ms_elasticache_b_subnet_cidr" {
  default = "10.0.6.0/24"
}

variable "auth_ms_subnet_b_cidr" {
  default = "10.0.8.0/24"
}

variable "management_ms_subnet_cidr" {
  default = "10.0.9.0/24"
}

variable "management_ms_subnet_b_cidr" {
  default = "10.0.10.0/24"
}

variable "worker_ms_subnet_cidr" {
  default = "10.0.11.0/24"
}

variable "worker_ms_subnet_b_cidr" {
  default = "10.0.12.0/24"
}

variable "management_rds_subnet_cidr" {
  default = "10.0.13.0/24"
}

variable "management_rds_subnet_b_cidr" {
  default = "10.0.14.0/24"
}

variable "management_redis_subnet_cidr" {
  default = "10.0.15.0/24"
}

variable "management_redis_subnet_b_cidr" {
  default = "10.0.16.0/24"
}

variable "auth_ms_instance_type" {
  default = "t3.micro"
}

variable "management_ms_instance_type" {
  default = "t3.micro"
}

variable "worker_ms_instance_type" {
  description = "x86 instance type allowed by the account plan and suitable for FFmpeg."
  default     = "c7i-flex.large"
}

variable "auth_ms_image" {
  default = "caiqueluci0/auth-ms:1.0"
}

variable "management_ms_image" {
  default = "caiqueluci0/management-ms:1.0"
}

variable "worker_ms_image" {
  default = "caiqueluci0/worker-ms:1.0"
}

variable "auth_ms_min_size" {
  default = 1
}

variable "auth_ms_desired_capacity" {
  default = 1
}

variable "auth_ms_max_size" {
  default = 5
}

variable "management_ms_min_size" {
  default = 1
}

variable "management_ms_desired_capacity" {
  default = 1
}

variable "management_ms_max_size" {
  default = 5
}

variable "worker_ms_min_size" {
  default = 1
}

variable "worker_ms_desired_capacity" {
  default = 1
}

variable "worker_ms_max_size" {
  default = 10
}

variable "documentdb_instance_class" {
  default = "db.t3.medium"
}

variable "documentdb_username" {
  default = "authadmin"
}

variable "documentdb_password" {
  sensitive = true
  default   = "AuthMongo123!"
}

variable "rds_instance_class" {
  default = "db.t3.micro"
}

variable "rds_database_name" {
  default = "management_ms"
}

variable "rds_username" {
  default = "usr"
}

variable "rds_password" {
  sensitive = true
  default   = "Management123!"
}

variable "jwt_secret" {
  sensitive = true
  default   = "QWluZGEgcXVlIGV1IGFuZGFzc2UgcGVsbyB2YWxlIGRhIHNvbWJyYSBkYSBtb3J0ZSwgbsOjbyB0ZW1lcmlhIG1hbCBhbGd1bSwgcG9ycXVlIHR1IGVzdMOhcyBjb21pZ287IGEgdHVhIHZhcmEgZSBvIHRldSBjYWphZG8gbWUgY29uc29sYW0uU2FsbW9zIDIzOjQ="
}

variable "video_bucket_name" {
  description = "Optional globally unique S3 bucket name. Null derives one from the AWS account and region."
  type        = string
  default     = null
  nullable    = true
}
