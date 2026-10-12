#!/usr/bin/bash
set -euo pipefail

ENV_NAME="${1:-dev}"
FLAG_REPLACE_CERTS="${2:-false}"
REPLACE_CONF_FILES="${3:-false}"
IDENTITY_CLIENT_ID="${4:-}"


SOURCE_TEMP_DIR="/home/dpsutl/temp"
DASHBOARD_DEST_DIR="/data/viridian/conf/dashboard"

move_if_needed() {
  local src_file="$1"
  local dest_file="$2"

  if [[ -f "$src_file" ]] && [[ "$REPLACE_CONF_FILES" == "true" || ! -f "$dest_file" ]]; then
    mkdir -p "$(dirname "$dest_file")"
    cp -f "$src_file" "$dest_file"
    echo "Copied $src_file -> $dest_file"
  elif [[ -f "$src_file" ]]; then
    echo "Skipped $src_file because destination already exists and REPLACE_CONF_FILES is false."
  else
    echo "Warning: source file not found: $src_file" >&2
  fi
}


if [[ ! -d "$DASHBOARD_DEST_DIR" ]]; then
  mkdir -p "$DASHBOARD_DEST_DIR"
  echo "Created directory: $DASHBOARD_DEST_DIR"
fi

move_if_needed "$SOURCE_TEMP_DIR/deployment/docker-compose.yml" "$DASHBOARD_DEST_DIR/docker-compose.yml"
move_if_needed "$SOURCE_TEMP_DIR/deployment/.env" "$DASHBOARD_DEST_DIR/.env"
move_if_needed "$SOURCE_TEMP_DIR/deployment/application.properties" "$DASHBOARD_DEST_DIR/application.properties"

RESOURCE_NAME="${HOSTNAME%%.*}"

az login --identity  --client-id "$IDENTITY_CLIENT_ID"

az acr login --name acrdiaanlnzndev


podman-compose -f "$DASHBOARD_DEST_DIR/docker-compose.yml" down

IMAGE="acrdiaanlnzndev.azurecr.io/deposit-dashboard"

podman images --filter "reference=${IMAGE}:*" \
  --format '{{.ID}}' \
  | sort -u \
  | xargs -r podman rmi -f >/dev/null 2>&1 || true

# podman rmi acrdiaanlnzndev.azurecr.io/deposit-dashboard:latest 2>/dev/null || true

# podman pull acrdiaanlnzndev.azurecr.io/deposit-dashboard:latest

podman-compose -f "$DASHBOARD_DEST_DIR/docker-compose.yml" --env-file "$DASHBOARD_DEST_DIR/.env" up -d


echo "Deployment completed successfully for environment: $ENV_NAME"
