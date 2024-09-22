#!/bin/bash
# RESTARTS CONTAINER ON LINUX USING X11

#
# © 2024-2025 JDHeim
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -o errexit  # ABORT ON NON-ZERO EXIT STATUS
set -o nounset  # TREAT UNSET VARIABLES AS AN ERROR AND EXIT
set -o pipefail # DON'T HIDE ERRORS WITHIN PIPES

readonly IMAGE_VERSION="latest-ea"
readonly IMAGE_NAME="jdvm"
readonly IMAGE_NAMESPACE="jdheim"
readonly CONTAINER_NAME="${IMAGE_NAME}"

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"
readonly INFO="[\e[1;34mINFO\e[0m]"
readonly ERROR="[\e[1;31mERROR\e[0m]"
readonly SUCCESS="[\e[1;32mSUCCESS\e[0m]"

main() {
  step "Stop and remove"
  if ! isExecInstalled "docker"; then
    echo -e "${ERROR} The \"docker\" command is not installed. Please install it: https://docs.docker.com/engine/install/"
    exit 1
  fi
  echo -e "${INFO} Stopping ${CONTAINER_NAME}..."
  docker container stop "${CONTAINER_NAME}" >/dev/null 2>&1 || true

  echo -e "${INFO} Removing ${CONTAINER_NAME}..."
  docker container rm "${CONTAINER_NAME}" >/dev/null 2>&1 || true

  step "Start"

  if [[ ! -d "${HOME}/shared" ]]; then
    echo -e "${INFO} Creating shared directory: ${HOME}/shared..."
    mkdir "${HOME}/shared"
  fi

  echo -e "${INFO} Checking for a newer image ${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION} available..."
  docker image pull "${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION}" || true

  if ! isExecInstalled "xhost"; then
    echo -e "${ERROR} The \"xhost\" command is not installed. Please install it with your package manager"
    exit 1
  fi
  # TODO Check /tmp/.X11-unix is not empty
  echo -e "${INFO} Allow connections to X11: adding non-network local connections to access control list..."
  xhost +local:docker >/dev/null

  echo -e "${INFO} Starting ${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION}..."
  docker container run --privileged -d \
    --name "${CONTAINER_NAME}" \
    --hostname "${CONTAINER_NAME}" \
    -p 80:80 -p 443:443 \
    --mount source=projects,target=/home/dev/projects \
    --mount source=maven,target=/home/dev/.m2/repository \
    --mount source=home,target=/home/dev \
    --mount source=docker,target=/var/lib/docker \
    --mount type=bind,source="${HOME}/shared",target=/mnt/shared \
    --mount type=bind,source=/tmp/.X11-unix,target=/tmp/.X11-unix \
    --mount type=bind,source=/run/user/${UID}/pulse/native,target=/tmp/PulseServer \
    --env DISPLAY="${DISPLAY}" \
    --env PULSE_SERVER="unix:/tmp/PulseServer" \
    --ipc host \
    --shm-size 2g \
    "${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_VERSION}" >/dev/null

  echo -e "${INFO} Cleaning dangling images..."
  docker image prune -f >/dev/null

  echo -e "${SUCCESS} Happy coding! ;)"
}

step() {
    local message="${1}"
    echo -e "${STEP} ${LINE} ${message} ${LINE}"
}

isExecInstalled() {
  local executable="${1}"
  command -v "${executable}" >/dev/null
}

main
