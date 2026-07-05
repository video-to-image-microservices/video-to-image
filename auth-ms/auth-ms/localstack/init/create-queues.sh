#!/bin/bash
set -e

create_queue_with_dlq() {
    local QUEUE_NAME=$1
    local DLQ_NAME="${QUEUE_NAME}-dlq"

    echo "Criando DLQ: $DLQ_NAME"

    DLQ_URL=$(awslocal sqs create-queue \
        --queue-name "$DLQ_NAME" \
        --query 'QueueUrl' \
        --output text)

    DLQ_ARN=$(awslocal sqs get-queue-attributes \
        --queue-url "$DLQ_URL" \
        --attribute-names QueueArn \
        --query 'Attributes.QueueArn' \
        --output text)

    echo "Criando fila: $QUEUE_NAME"

    QUEUE_URL=$(awslocal sqs create-queue \
        --queue-name "$QUEUE_NAME" \
        --attributes VisibilityTimeout=5 \
        --query 'QueueUrl' \
        --output text)

    echo "Configurando DLQ para $QUEUE_NAME"

    awslocal sqs set-queue-attributes \
        --queue-url "$QUEUE_URL" \
        --attributes "RedrivePolicy={\"deadLetterTargetArn\":\"$DLQ_ARN\",\"maxReceiveCount\":\"3\"}"
}

create_queue_with_dlq "user-created-queue"
create_queue_with_dlq "user-deleted-queue"

echo "Filas criadas com sucesso."