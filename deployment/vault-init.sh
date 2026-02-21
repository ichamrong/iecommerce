#!/bin/bash
set -e

echo "Waiting for HashiCorp Vault to start..."
while ! nc -z localhost 8200; do   
  sleep 1 
done

export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='root'

echo "Vault is up! Enabling Key-Value v2 secret engine..."
docker exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='root' iecommerce-vault vault secrets enable -path=secret kv-v2 || true

echo "Seeding baseline secrets for iecommerce..."
docker exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='root' iecommerce-vault vault kv put secret/iecommerce \
  postgres.username=admin \
  postgres.password=admin \
  keycloak.admin.password=admin

echo "Secrets seeded successfully!"
docker exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='root' iecommerce-vault vault kv get secret/iecommerce
