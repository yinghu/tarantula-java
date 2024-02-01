### Build and push image to AWS

## Requirements
- Make sure you have [Docker](https://www.docker.com/products/docker-desktop/) and docker-compose (included with docker-desktop) installed.
- Install the [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) and set up a [profile](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for the correct account (talk to Harrison, Andrew, or Alec for this information)


## First time setup

In the `awsImageBuild.sh` file, change the `AWS_PROFILE` variable to the name of your profile defined above


## Push

NOTE: You must be connected to Twingate

Simply run `./awsImageBuild.sh` in the root of this repo. It will make sure you're aws cli is signed in, pull the next version number from the Perfect Day Versioning Service, build the image with docker, and push that image to the AWS Elastic Conatiner Registry