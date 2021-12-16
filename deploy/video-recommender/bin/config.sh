#!/bin/bash
# -*- coding:utf-8 -*-

#######################
# Template config
# DO NOT modify
PATH=".:$PATH"
TBINDIR="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TBASEDIR=$(dirname $TBINDIR)
TBASENAME=$(basename $TBASEDIR)

#######################
# programe name
# Replace s
PROG="$JAVA_HOME/bin/java"

PROGNAME="video-recommender"
PROGBASE=${PROGNAME%%.*}

hostip=`hostname -i|cut -d' ' -f2`
memTotal=`free -g|sed -n 2p|awk '{print $2}'`
memHeap=$((memTotal*75/100))
memDirect=$((memTotal*5/100))

#######################
# runtime options -XX:SurvivorRatio=8 -Xmn15G
OPTS="-Xmx${memHeap}g
-Xms${memHeap}g
-XX:MaxDirectMemorySize=${memDirect}G
-Djdk.nio.maxCachedBufferSize=262144
-XX:MaxGCPauseMillis=150
-XX:InitiatingHeapOccupancyPercent=50
-XX:+UseG1GC
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=17066
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.port=9997
-Djava.rmi.server.hostname=$hostip
-XX:+PrintGCDetails
-Xloggc:../logs/gc.log
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-XX:-OmitStackTraceInFastThrow
-DSW_AGENT_NAME=video-recommender
-DSW_AGENT_COLLECTOR_BACKEND_SERVICES=10.42.15.139:80
-DSW_AGENT_SPAN_LIMIT=2000
-DSW_AGENT_SAMPLE=1
-javaagent:../skywalking/skywalking-agent.jar
-Dfile.encoding=UTF8
-cp video-recommender-1.0-SNAPSHOT.jar com.td.recommend.video.api.dubbo.DubboService -port 8088 -dubboPort 8089"

#######################
#
DAEMONIZE=yes

#######################
# pidfile absolute path
# Default:
#	${TBASEDIR}/var/${PROGBASE}.pid
PIDFILE=${PIDFILE:-${TBASEDIR}/var/${TBASENAME}.pid}

#######################
# nohup stdout file
# set STDOUT=/dev/null if no need
# Default:
#	${TBASEDIR}/logs/${PROGBASE}.stdout.log
#STDOUT=${STDOUT:-${TBASEDIR}/logs/${PROGBASE}.stdout.log.`date +%Y%m%d%H%M%S`}
STDOUT=/dev/null
#######################
# ulimit -c
# Default:
#	0
DAEMON_COREFILE_LIMIT=${DAEMON_COREFILE_LIMIT:-0}

TIMEWAIT=${TIMEWAIT:-10}

#######################
# use nohup to daemonize
# if PROG daemonized by itself, set START_COMMAND="${PROG} ${OPTS} > /dev/null 2>&1"
if [ ${DAEMONIZE} == 'yes' -o ${DAEMONIZE} == 'YES' -o ${DAEMONIZE} == 1 ];then
  START_COMMAND="${TBINDIR}/daemonize.sh"
else
  START_COMMAND="${PROG} ${OPTS}"
fi

#######################
# monit config
MONIT_NAME="video-recommender"
