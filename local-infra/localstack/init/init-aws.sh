#!/bin/sh
set -e

create_queue() {
  QUEUE_NAME="$1"

  awslocal sqs create-queue \
    --queue-name "$QUEUE_NAME" \
    --query QueueUrl \
    --output text
}

create_queue "user-created-queue"
create_queue "user-deleted-queue"
create_queue "video-status-queue"
create_queue "process-queue"

awslocal s3 mb s3://videos-bucket || true

echo "LocalStack resources ready."
