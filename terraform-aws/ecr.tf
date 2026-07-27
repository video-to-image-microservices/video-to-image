locals {
  ecr_registry = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

resource "aws_ecr_repository" "auth_ms" {
  name                 = "video-to-image/auth-ms"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }

  force_delete = true
}

resource "aws_ecr_repository" "management_ms" {
  name                 = "video-to-image/management-ms"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }

  force_delete = true
}

resource "aws_ecr_repository" "worker_ms" {
  name                 = "video-to-image/worker-ms"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }

  force_delete = true
}

