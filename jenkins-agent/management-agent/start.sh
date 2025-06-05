#!/bin/bash
set -e

curl -v -o /opt/jenkins/agent.jar ${url}jnlpJars/agent.jar
java -jar /opt/jenkins/agent.jar -url ${url} -name "management-slave" -secret ${secret}