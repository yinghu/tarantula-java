## Prerequisites
1. Open JDK 14 or up
2. Maven 3
## Source Tree
1. Get source tree : git clone https://github.com/yinghu/gamecluster.git
2. API project : gamecluster/modules/protocol
3. Platform project : gamecluster/platform
    src/main/bin -- tarantula.sh and tarantula.bat
    src/main/config -- addtional configuations 
    src/main/deploy -- role base admin application deployment descriptor
    src/main/resource -- cluster and system configurations
    src/main/java -- main java source 
    src/main/web -- web pages
    src/test -- unit test source
## Build
1. API project build (gamecluster/modules/protocol) 
    mvn clean install (the dependency of all other modules and platform)
    artifact : target/gec-protocol-1.0.jar
2. Platform project build (gamecluster/platform)
    mvn clean (if it is the first build to install local key-value store lib setup)
    mvn clean install
    artifact : target/gec-platform-2.0-bib.tar.gz
## Config
1. external configs folder : /etc/tarantula
## Run
## Test