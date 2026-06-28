#!/bin/bash
awslocal sqs create-queue --queue-name user-created-queue
awslocal sqs create-queue --queue-name user-deleted-queue
