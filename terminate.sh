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

env=${1}

envs=$(eb list | sed 's/^\* //' )

if [[ ${env} != panopticon-* ]]; then
  echo "Usage: ${0} panopticon-<env>"
  exit 1
elif [ $(echo "${envs}" | grep "^${1}$" -c) -eq 0 ]; then
  echo "Environment not recognized: '${1}'. Use one of the following:"
  echo
  echo "${envs}"
  exit 1
fi

echo "> Starting termination"
eb terminate "${env}"
