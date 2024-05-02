ECR_REPO=591022124130.dkr.ecr.us-west-2.amazonaws.com
AWS_PROFILE=ng-infra-shared-poweruser

aws sso login --profile $AWS_PROFILE
aws ecr get-login-password --region us-west-2 --profile $AWS_PROFILE | docker login --username AWS --password-stdin $ECR_REPO

VERSION=$(curl -X PUT -s -k https://versioning.pday.gg/semver/gamecluster-earth8/patch)-earth8
docker build --platform linux/amd64 -f Dockerfile-prod -t $ECR_REPO/gamecluster:$VERSION .

docker push $ECR_REPO/gamecluster:$VERSION

echo "Pushed Version $VERSION"