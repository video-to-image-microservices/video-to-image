#!/bin/sh
set -e

awslocal sqs create-queue --queue-name user-created-queue
awslocal sqs create-queue --queue-name user-deleted-queue
awslocal sqs create-queue --queue-name video-status-queue
awslocal sqs create-queue --queue-name process-queue
awslocal s3 mb s3://videos-bucket
