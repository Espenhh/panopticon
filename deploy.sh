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

envs=$(eb list | sed 's/^\* //')

if [[ ${1} != panopticon-* ]]; then
  echo "Usage: ${0} panopticon-<environment>"
  echo
  echo "Available environments:"
  echo "${envs}"
  exit 1
elif [ $(echo "${envs}" | grep "^${1}$" -c) -eq 0 ]; then
  echo "Environment not recognized: '${1}'. Use one of the following:"
  echo
  echo "${envs}"
  exit 1
fi

local_version=$( grep -E "<version>[0-9]+(\.[0-9]+).*(SNAPSHOT)?</version>" pom.xml -m1 2> /dev/null | sed 's/.*<version>\(.*\)<\/version>/\1/' )

version_suggestion="[${local_version}] "
read -p "Version? ${version_suggestion}" version
[ -z ${version} ] && version="${local_version}"

beanstalk_env=${1}
env=$(echo ${1} | sed s/panopticon-//g)

vault_file=$(mktemp)
pass "panopticon/aws/${env}/vault_password" >> ${vault_file}

sh -c "sleep 5; rm -f ${vault_file}" &

echo "> Assembling files"
secret_properties_file="application-${env}.properties"

ansible-vault decrypt "secretconfig/application-${env}.properties.encrypted" --vault-password-file ${vault_file} --output=${secret_properties_file}
if [ ! -f ${secret_properties_file} ]; then
    echo "Something went wrong with decrypting secret properties. File ${secret_properties_file} is missing. Can't deploy..."
    exit 1
fi

cp ~/.m2/repository/no/panopticon/panopticon-server/${version}/panopticon-server-${version}.jar ./app.jar

echo "> Packaging app"
zip -r app.zip app.jar .ebextensions Procfile ${secret_properties_file}
rm -f app.jar ${secret_properties_file}

echo "> Starting deploy"
eb deploy "${beanstalk_env}"

echo "> Deleting temporary files"
rm -f app.zip

echo "Deploy complete.'"
exit 0
trap "{ rm -f app.zip app.jar ${secret_properties_file} ; rm -f ${vault_file} }" EXIT
