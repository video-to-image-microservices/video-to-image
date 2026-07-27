data "aws_caller_identity" "current" {}

locals {
  video_bucket_name = coalesce(
    var.video_bucket_name,
    "video-to-image-${data.aws_caller_identity.current.account_id}-${var.aws_region}"
  )
}

resource "aws_s3_bucket" "videos" {
  bucket        = local.video_bucket_name
  force_destroy = true

  tags = {
    Name = "video-to-image-storage"
  }
}

resource "aws_s3_bucket_public_access_block" "videos" {
  bucket = aws_s3_bucket.videos.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "videos" {
  bucket = aws_s3_bucket.videos.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "videos" {
  bucket = aws_s3_bucket.videos.id

  rule {
    id     = "abort-incomplete-uploads"
    status = "Enabled"

    filter {}

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}
