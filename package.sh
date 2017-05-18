#!/usr/bin/env bash

BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

# resolve symlinks
while [ -h "$BASEDIR/$0" ]; do
    DIR=$(dirname -- "$BASEDIR/$0")
    SYM=$(readlink $BASEDIR/$0)
    BASEDIR=$(cd $DIR && cd $(dirname -- "$SYM") && pwd)
done
cd ${BASEDIR}

cd backend

if [ -z ${1} ] || [ -z ${2} ]; then
  echo "> Usage: ${0} <env> <version>"
  exit 1
fi

env=${1}
version=${2}

vault_file=$(mktemp)
pass "panopticon/aws/${env}/vault_password" >> ${vault_file}
secret_properties_file="application-${env}.properties"

trap "{ rm -f app.jar ${secret_properties_file} ${vault_file} ; exit 255; }" EXIT

echo "> Assembling files"
sh -c "sleep 5; rm -f ${vault_file}" &
ansible-vault decrypt "secretconfig/application-${env}.properties.encrypted" --vault-password-file ${vault_file} --output=${secret_properties_file}
if [ ! -f ${secret_properties_file} ]; then
    echo "Something went wrong with decrypting secret properties. File ${secret_properties_file} is missing. Can't deploy..."
    exit 1
fi

cp ~/.m2/repository/no/panopticon/panopticon-server/${version}/panopticon-server-${version}.jar ./app.jar

echo "> Packaging app"
zip -r app.zip app.jar .ebextensions Procfile ${secret_properties_file}
rm -f app.jar ${secret_properties_file}

