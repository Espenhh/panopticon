#/bin/bash

GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo -e "${GREEN}Fetching your active Elastic Beanstalk environments...${NC}"
eb list
echo ""
echo -e "${GREEN}Type name of the environment you want to deploy to:${NC}"
read ENVIRONMENT
echo -e "${GREEN}Deploying backend to $ENVIRONMENT${NC}"

eb use "$ENVIRONMENT"

eb deploy