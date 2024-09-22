#!/bin/bash
# STOPS CONTAINER

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

[[ -f "$(dirname "${BASH_SOURCE[0]}")/start.sh" ]] && . "$(dirname "${BASH_SOURCE[0]}")/start.sh"

usage() {
  cat << EOF
Usage: $(basename "$0")

Stops container
EOF
  exit 1
}

main() {
  cd ..
  readOptions "$@"
  enrichContainerData
  stop
  remove
}

stop() {
  local runningContainers restartingContainers
  runningContainers="$(docker container ls --filter "name=^jdvm-" --filter "status=running" --format "{{.ID}}")"
  restartingContainers="$(docker container ls --filter "name=^jdvm-" --filter "status=restarting" --format "{{.ID}}")"
  if [[ -n "${runningContainers}" || -n "${restartingContainers}" ]]; then
    step "Stop ${imageName}:${imageVersion}"
    local containersToStop="${runningContainers} ${restartingContainers}"
    run docker container stop ${containersToStop}
    sleep 0.2
  fi
}

remove() {
  local containersToRemove
  containersToRemove="$(docker container ls --filter "status=exited" --format "{{.ID}}")"
  if [[ -n "${containersToRemove}" ]]; then
    step "Remove ${imageName}:${imageVersion}"
    run docker container rm ${containersToRemove}
  fi
}

main "$@"
