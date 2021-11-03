#!/bin/bash

if [[ ! -z "${JCP_LICENSE_KEY}" ]]; then
  java ru.CryptoPro.JCP.tools.License -serial $JCP_LICENSE_KEY -store
fi

java ru.CryptoPro.JCP.tools.License

exec "$@"