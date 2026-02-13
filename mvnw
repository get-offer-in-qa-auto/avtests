#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# Resolve project base directory
SCRIPT_DIR="$PWD"
while [ "$SCRIPT_DIR" != '/' ]; do
  if [ -d "$SCRIPT_DIR/.mvn" ]; then
    break
  fi
  SCRIPT_DIR="$(dirname "$SCRIPT_DIR")"
done

if [ ! -d "$SCRIPT_DIR/.mvn" ]; then
  echo "Could not find .mvn directory"
  exit 1
fi

WRAPPER_JAR="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.jar"

# Download wrapper jar if missing
if [ ! -r "$WRAPPER_JAR" ]; then
  if [ -n "$MVNW_VERBOSE" ]; then
    echo "Couldn't find maven-wrapper.jar, downloading it ..."
  fi
  jarUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  if [ -f "$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties" ]; then
    wrapperUrl=$(grep '^wrapperUrl=' "$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties" | cut -d'=' -f2)
    [ -n "$wrapperUrl" ] && jarUrl="$wrapperUrl"
  fi
  if command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$jarUrl"
  elif command -v curl >/dev/null 2>&1; then
    curl -sSL -o "$WRAPPER_JAR" "$jarUrl"
  else
    echo "Error: Could not download maven-wrapper.jar (wget or curl required)"
    exit 1
  fi
fi

export MAVEN_PROJECTBASEDIR="$SCRIPT_DIR"
cd "$SCRIPT_DIR"
exec "$JAVA_HOME/bin/java" ${MAVEN_OPTS} \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$SCRIPT_DIR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
