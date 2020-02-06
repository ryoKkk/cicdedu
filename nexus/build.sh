#!/bin/bash
docker build -t foo/nexus3 .
docker run -d --rm --name nexus3 -p 8081:8081 foo/nexus3