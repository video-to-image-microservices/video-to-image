#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://main-alb-2076985125.us-east-1.elb.amazonaws.com}"
VIDEO_FILE="${VIDEO_FILE:-}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-900}"

for command in curl jq unzip; do
  command -v "$command" >/dev/null || {
    echo "Dependência ausente: $command" >&2
    exit 1
  }
done

if [[ -z "$VIDEO_FILE" || ! -f "$VIDEO_FILE" ]]; then
  echo "Uso: VIDEO_FILE=/caminho/video.mp4 BASE_URL=http://seu-alb ./scripts/e2e-concurrent.sh" >&2
  exit 1
fi

tmp_dir="$(mktemp -d)"
email="e2e-$(date +%s)-$RANDOM@example.com"
password="Teste123!"
user_id=""
token=""

cleanup() {
  if [[ -n "$user_id" ]]; then
    curl -fsS -X DELETE "$BASE_URL/auth/users/$user_id" \
      -H "Authorization: Bearer $token" >/dev/null 2>&1 || true
  fi
  rm -rf "$tmp_dir"
}
trap cleanup EXIT

request_json() {
  local method="$1"
  local url="$2"
  local body="$3"
  curl -fsS -X "$method" "$url" -H "Content-Type: application/json" -d "$body"
}

echo "1/5 Criando usuário e autenticando"
create_response="$(request_json POST "$BASE_URL/auth/users" \
  "$(jq -nc --arg name "Teste E2E concorrente" --arg email "$email" --arg password "$password" \
    '{name: $name, email: $email, password: $password}')" )"
user_id="$(jq -er '.id' <<<"$create_response")"

login_response="$(request_json POST "$BASE_URL/auth/login" \
  "$(jq -nc --arg email "$email" --arg password "$password" \
    '{email: $email, password: $password}')" )"
token="$(jq -er '.token' <<<"$login_response")"

# O cadastro chega ao management-ms por SQS; aguarda a consistência eventual.
sleep 5

extension="${VIDEO_FILE##*.}"
first_file="$tmp_dir/e2e-a-$RANDOM.$extension"
second_file="$tmp_dir/e2e-b-$RANDOM.$extension"
cp "$VIDEO_FILE" "$first_file"
cp "$VIDEO_FILE" "$second_file"

upload() {
  local file="$1"
  local output="$2"
  local code attempt
  for attempt in {1..12}; do
    code="$(curl -sS -o "$output" -w "%{http_code}" \
      -X POST "$BASE_URL/management/videos" \
      -H "Authorization: Bearer $token" \
      -F "file=@$file")"
    [[ "$code" == "202" ]] && return 0
    sleep 5
  done
  echo "Upload de $(basename "$file") retornou HTTP $code: $(cat "$output")" >&2
  return 1
}

echo "2/5 Enviando dois vídeos simultaneamente"
upload "$first_file" "$tmp_dir/upload-a.json" &
first_pid=$!
upload "$second_file" "$tmp_dir/upload-b.json" &
second_pid=$!
wait "$first_pid"
wait "$second_pid"

first_name="$(jq -er '.fileName' "$tmp_dir/upload-a.json")"
second_name="$(jq -er '.fileName' "$tmp_dir/upload-b.json")"
first_id="$(jq -er '.videoProcessId' "$tmp_dir/upload-a.json")"
second_id="$(jq -er '.videoProcessId' "$tmp_dir/upload-b.json")"
[[ "$first_id" != "$second_id" ]] || {
  echo "Os uploads receberam o mesmo identificador." >&2
  exit 1
}

wait_until_processed() {
  local file_name="$1"
  local deadline=$((SECONDS + TIMEOUT_SECONDS))
  local response status

  while (( SECONDS < deadline )); do
    response="$(curl -fsS \
      -H "Authorization: Bearer $token" \
      "$BASE_URL/management/videos/$file_name/status")"
    status="$(jq -er '.status' <<<"$response")"
    case "$status" in
      PROCESSED) return 0 ;;
      FAILED)
        echo "Processamento de $file_name terminou com FAILED: $response" >&2
        return 1
        ;;
    esac
    sleep 5
  done

  echo "Timeout aguardando $file_name" >&2
  return 1
}

echo "3/5 Aguardando os dois workers/processamentos"
wait_until_processed "$first_name" &
first_poll_pid=$!
wait_until_processed "$second_name" &
second_poll_pid=$!
wait "$first_poll_pid"
wait "$second_poll_pid"

echo "4/5 Baixando e validando os ZIPs"
for entry in "a:$first_name" "b:$second_name"; do
  label="${entry%%:*}"
  file_name="${entry#*:}"
  zip_file="$tmp_dir/result-$label.zip"
  curl -fsS -L \
    -H "Authorization: Bearer $token" \
    "$BASE_URL/management/videos/$file_name/download" \
    -o "$zip_file"
  unzip -tq "$zip_file"
  [[ "$(unzip -Z1 "$zip_file" | wc -l)" -gt 0 ]]
done

echo "5/5 Validando listagem de status"
status_list="$(curl -fsS \
  -H "Authorization: Bearer $token" \
  "$BASE_URL/management/videos/status")"
jq -e --arg first "$first_id" --arg second "$second_id" \
  '([.[].videoProcessId] | index($first)) != null and
   ([.[].videoProcessId] | index($second)) != null' <<<"$status_list" >/dev/null

echo "E2E concorrente OK: $first_id e $second_id foram processados e baixados."
