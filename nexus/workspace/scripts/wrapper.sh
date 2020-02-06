#!/bin/bash

nohup ${NEXUS_HOME}/init.groovy.d/provision.sh &
# from: https://github.com/sonatype/docker-nexus3/blob/master/Dockerfile
${SONATYPE_DIR}/start-nexus-repository-manager.sh