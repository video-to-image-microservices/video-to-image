resource "aws_docdb_subnet_group" "auth_ms" {
  name = "auth-ms-documentdb-subnet-group"
  subnet_ids = [
    aws_subnet.auth_ms_documentdb.id,
    aws_subnet.auth_ms_documentdb_b.id,
  ]

  tags = {
    Name = "auth-ms-documentdb-subnet-group"
  }
}

resource "aws_docdb_cluster_parameter_group" "auth_ms" {
  family      = "docdb5.0"
  name        = "auth-ms-documentdb-parameters"
  description = "DocumentDB parameters for auth-ms"

  parameter {
    name  = "tls"
    value = "disabled"
  }
}

resource "aws_docdb_cluster" "auth_ms" {
  count = 0

  cluster_identifier              = "auth-ms-documentdb"
  engine                          = "docdb"
  engine_version                  = "5.0.0"
  master_username                 = var.documentdb_username
  master_password                 = var.documentdb_password
  db_subnet_group_name            = aws_docdb_subnet_group.auth_ms.name
  db_cluster_parameter_group_name = aws_docdb_cluster_parameter_group.auth_ms.name
  vpc_security_group_ids          = [aws_security_group.auth_ms_documentdb.id]
  backup_retention_period         = 1
  preferred_backup_window         = "03:00-04:00"
  skip_final_snapshot             = true
  storage_encrypted               = true

  tags = {
    Name = "auth-ms-documentdb"
  }
}

resource "aws_docdb_cluster_instance" "auth_ms" {
  count = 0

  identifier         = "auth-ms-documentdb-${count.index + 1}"
  cluster_identifier = aws_docdb_cluster.auth_ms[0].id
  instance_class     = var.documentdb_instance_class

  tags = {
    Name = "auth-ms-documentdb-${count.index + 1}"
  }
}
