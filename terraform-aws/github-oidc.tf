resource "aws_iam_openid_connect_provider" "github_actions" {
  url = "https://token.actions.githubusercontent.com"

  client_id_list = [
    "sts.amazonaws.com",
  ]

  tags = {
    Name = "github-actions"
  }
}

data "aws_iam_policy_document" "github_actions_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github_actions.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:video-to-image-microservices/*"]
    }
  }
}

resource "aws_iam_role" "github_actions_deploy" {
  name                 = "github-actions-video-to-image-deploy"
  assume_role_policy   = data.aws_iam_policy_document.github_actions_assume_role.json
  max_session_duration = 3600

  tags = {
    Name = "github-actions-video-to-image-deploy"
  }
}

data "aws_iam_policy_document" "github_actions_deploy" {
  statement {
    sid       = "EcrLogin"
    effect    = "Allow"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid    = "PublishServiceImages"
    effect = "Allow"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart",
    ]
    resources = [
      aws_ecr_repository.auth_ms.arn,
      aws_ecr_repository.management_ms.arn,
      aws_ecr_repository.worker_ms.arn,
    ]
  }

  statement {
    sid    = "ReadAutoScalingState"
    effect = "Allow"
    actions = [
      "autoscaling:DescribeAutoScalingGroups",
      "autoscaling:DescribeInstanceRefreshes",
    ]
    resources = ["*"]
  }

  statement {
    sid    = "DeployWithInstanceRefresh"
    effect = "Allow"
    actions = [
      "autoscaling:CancelInstanceRefresh",
      "autoscaling:StartInstanceRefresh",
    ]
    resources = [
      aws_autoscaling_group.auth_ms.arn,
      aws_autoscaling_group.management_ms.arn,
      aws_autoscaling_group.worker_ms.arn,
    ]
  }
}

resource "aws_iam_role_policy" "github_actions_deploy" {
  name   = "publish-ecr-and-refresh-asg"
  role   = aws_iam_role.github_actions_deploy.id
  policy = data.aws_iam_policy_document.github_actions_deploy.json
}

data "aws_iam_policy_document" "github_actions_worker_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github_actions.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:video-to-image-microservices@297548236/worker-ms@1313148069:ref:refs/heads/main"]
    }
  }
}

resource "aws_iam_role" "github_actions_worker_deploy" {
  name                 = "github-actions-worker-ms-deploy"
  assume_role_policy   = data.aws_iam_policy_document.github_actions_worker_assume_role.json
  max_session_duration = 3600

  tags = {
    Name = "github-actions-worker-ms-deploy"
  }
}

resource "aws_iam_role_policy" "github_actions_worker_deploy" {
  name   = "publish-ecr-and-refresh-asg"
  role   = aws_iam_role.github_actions_worker_deploy.id
  policy = data.aws_iam_policy_document.github_actions_deploy.json
}
