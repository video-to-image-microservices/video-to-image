#!/bin/sh
set -eu

email="step5-$(date +%s)@example.com"
password="senha123"

json_get() {
  key="$1"
  sed -n "s/.*\"$key\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p"
}

for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/swagger-ui.html || true)
  if [ "$code" = "200" ] || [ "$code" = "302" ]; then
    break
  fi
  if [ "$i" = "60" ]; then
    printf "auth-ms not ready, last HTTP code=%s\n" "$code"
    exit 1
  fi
  sleep 1
done

create_body=$(printf '{"name":"Step Five","email":"%s","password":"%s"}' "$email" "$password")
create_resp=$(curl -s -X POST http://localhost:8082/users -H "Content-Type: application/json" -d "$create_body")
user_id=$(printf "%s" "$create_resp" | json_get id)
if [ -z "$user_id" ]; then
  printf "Create user failed: %s\n" "$create_resp"
  exit 1
fi

login_body=$(printf '{"email":"%s","password":"%s"}' "$email" "$password")
login_resp=$(curl -s -X POST http://localhost:8082/auth/login -H "Content-Type: application/json" -d "$login_body")
token=$(printf "%s" "$login_resp" | json_get token)
if [ -z "$token" ]; then
  printf "Login failed: %s\n" "$login_resp"
  exit 1
fi

response_file="/tmp/auth-step5-response.json"
http_code=$(curl -s -o "$response_file" -w "%{http_code}" \
  -H "Authorization: Bearer $token" \
  "http://localhost:8082/users/$user_id")

printf "GET /users/%s HTTP %s\n" "$user_id" "$http_code"
cat "$response_file"
printf "\n"

if [ "$http_code" != "200" ]; then
  exit 1
fi
