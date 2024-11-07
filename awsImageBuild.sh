set -e

ECR_REPO=591022124130.dkr.ecr.us-west-2.amazonaws.com
AWS_PROFILE=ng-infra-shared-poweruser

aws sso login --profile $AWS_PROFILE
aws ecr get-login-password --region us-west-2 --profile $AWS_PROFILE | docker login --username AWS --password-stdin $ECR_REPO

SHA=$(git rev-parse --short HEAD)
SEMVER=$(curl -X PUT -s -k https://versioning.pday.gg/semver/gamecluster-earth8/patch)

DOCKER_VERSION=$SEMVER-earth8-$SHA
docker build --platform linux/amd64 -f Dockerfile-prod -t $ECR_REPO/gamecluster:$DOCKER_VERSION .

docker push $ECR_REPO/gamecluster:$DOCKER_VERSION

git tag $SEMVER
git push --tags

echo "Pushed Version $SEMVER"
