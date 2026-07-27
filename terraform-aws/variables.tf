variable "aws_region" {
  default = "us-east-1"
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  default = "10.0.1.0/24"
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