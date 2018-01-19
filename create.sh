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

if [[ ${1} != panopticon-* ]]; then
  echo "Usage: ${0} panopticon-<environment> [debug]"
  exit 1
fi

local_version=$( grep -E "<version>[0-9]+(\.[0-9]+).*(SNAPSHOT)?</version>" pom.xml -m1 2> /dev/null | sed 's/.*<version>\(.*\)<\/version>/\1/' )

version_suggestion="[${local_version}] "
read -p "Version? ${version_suggestion}" version
[ -z ${version} ] && version="${local_version}"

beanstalk_env=${1}
env=$(echo ${beanstalk_env} | sed s/panopticon-//g)

trap "{ rm -f app.zip ; exit 255; }" EXIT

../package.sh ${env} ${version}
if [ $? -ne 255 ]; then
  echo "> Package failed!"
  exit 1
fi

SPRING_PROFILES_ACTIVE=${env}
KEYNAME="panopticon-nsb"
instance_type="t2.micro"
platform="java-8"

echo "Variables:
---------
version: ${version}
platform: ${platform}
instance_type: ${instance_type}
keyname: ${KEYNAME}
envvars SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
"

if [ "${2}" == "debug" ]; then
  echo "Debug mode. Skipping creation."
  exit 0
fi

echo "Starting creation of ${beanstalk_env}:
-----------------------------------
"
envchain aws eb create ${beanstalk_env} \
--keyname ${KEYNAME} \
--vpc.elbpublic \
--platform "${platform}" \
--instance_type ${instance_type} \
--cname ${beanstalk_env} \
--envvars SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

exit 0
