#!/bin/bash
### BEGIN INIT INFO
# Provides: tarantula
# Required-Start:
# Required-Stop:
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Start tarantula service on bootup
# Description: Enable OBM scheduler service provided by daemon.
### END INIT INFO
start(){
	echo "Starting Tarantula Distribution System  ..."
	mount /dev/sdb /mnt
	cd /root/release/bin
	./tarantula.sh &
}
stop(){
	echo "Stopping Tarantula Distribution System  ..."
	cd /root/release/bin
    kill -TERM `cat tarantula.pid`
}
case $1 in 
        start)
                start
                ;;
        stop)
                stop
                ;;
        *)
                echo "Usage tarantula.sh start|stop"
esac
exit 0