### Run on docker container

To run with docker rather than directly on your machine, make sure you have [Docker](https://www.docker.com/products/docker-desktop/) and docker-compose (included with docker-desktop) installed.

Simply run `docker-compose up -d` and navigate to `localhost:8090`

The login is user: `dev/root` pass: `root`

If you make a code change, the image will need to get rebuilt, and can be done by running 
`docker-compose up --build -d`


### Running Without Docker Compose

If you have an image built and would like to run it manually it's required that the container is run with a specific name ending in a number. EG: `docker run --rm --name platform-01 [image id]` This is currenly a WIP scaling solution for kubernetes and will likely be removed as a requirement soon. 