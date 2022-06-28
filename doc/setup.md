## Prerequisites

1. Open JDK 14 or up

2. Maven 3

## Source Tree

1. Get source tree : git clone https://github.com/yinghu/gamecluster.git

2. API project : `gamecluster/modules/protocol`

3. Platform project : `gamecluster/platform`

    `src/main/bin` -- tarantula.sh and tarantula.bat

    `src/main/config` -- addtional configuations

    `src/main/deploy` -- role base admin application deployment descriptor

    `src/main/resource` -- cluster and system configurations

    `src/main/java` -- main java source

    `src/main/web` -- web pages

    `src/test` -- unit test source

## Build

1. API project build (`gamecluster/modules/protocol`) 
    mvn clean install (the dependency of all other modules and platform)
    artifact : target/gec-protocol-1.0.jar

2. Platform project build (`gamecluster/platform`)
    mvn clean (if it is the first build to install local key-value store lib setup)
    mvn clean install
    artifact : target/gec-platform-2.0-bib.tar.gz

## Config

1. create external configs folder : /etc/tarantula ( sudo mkdir /etc/tarantula )

2. copy src/main/config/tarantula.properties to /etc/tarantula

    ```
    tarantula.data.bucket.node=ND01 
    #tarantula.data.store.dir=/mnt/tds
    #tarantula.service.deployment.dir=/mnt/deploy
    tarantula.cluster.name=tarantula-dev
    tarantula.auth.context=localhost
    ```

    If running without sudo permission on Unix-like OS, edit 

     ```
     tarantula.data.store.dir=[your/mnt/tds]
     tarantula.service.deployment.dir=[your/mnt/deploy]
     ```

3. copy src/main/config/push-service-settings.json to /etc/taratula

    ```
    {
        "sessionTimeout" : 30000,
        "sessionPoolSize": 100,
        "receiverTimeout": 1000,
        "IP": "10.0.0.192"
    }
    ```

    Edit:

        ```
        "IP": "host box ip address"
        ```

4. copy src/main/config/host.list to /etc/tarantula

    `localhost`

    Edit:
        `host box ip address`
## Run
cd to `target/release/gec-platform-2.0-bin/gec-platform-2.0/bin`
run tarantula.bat on Windows 
run tatantula.sh on unix-like OS 

### Run on local box

### Run on docker container

To run with docker rather than directly on your machine, make sure you have [Docker](https://www.docker.com/products/docker-desktop/) and docker-compose (included with docker-desktop) installed.

1. Ensure `platform/src/main/conf/tarantula.properties` has these properties set
```
tarantula.data.store.dir=/tarantula/data
tarantula.service.deployment.dir=/tarantula/deploy
```
2. Simply run `docker-compose up -d` and navigate to `localhost:8090`

If you make a code change, the image will need to get rebuilt, and can be done by running `docker-compose up --build -d`

## Test web
open index page `localhost:8090`
click signin icon to popup login page with user/pwd pair root/root
dashboard page includes player/acount/developer/sudo tabs 