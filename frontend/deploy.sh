#/bin/bash

npm run build

aws s3 sync ./dist/ s3://panopticon-prod --region=eu-central-1 --delete