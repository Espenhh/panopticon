#/bin/bash

GREEN='\033[0;32m'
NC='\033[0m' # No Color




REGION="eu-central-1"

echo -e "${GREEN}Fetching your active buckets in region $REGION...${NC}"
aws s3 ls | grep panopticon
echo ""
echo -e "${GREEN}Type bucket name where you want to deploy panoopticon frontend:${NC}"
read BUCKET_NAME
echo -e "${GREEN}Deploying frontend to $BUCKET_NAME in $REGION${NC}"

npm run build

aws s3 sync ./dist/ "s3://$BUCKET_NAME" --region="$REGION" --delete
