#!/bin/bash
# RUNS INIT SCRIPTS IN /DOCKER-ENTRYPOINT.D/ AND THEN STARTS SYSTEMD

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

readonly DOCKER_ENTRYPOINT_DIR="/docker-entrypoint.d"

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"
readonly INFO="[\e[1;34mINFO\e[0m]"
readonly ERROR="[\e[1;31mERROR\e[0m]"
readonly SUCCESS="[\e[1;32mSUCCESS\e[0m]"

main() {
  init
  startSystemd
}

step() {
    local message="${1}"
    echo -e "${STEP} ${LINE} ${message} ${LINE}"
}

init() {
  step "Start initialization"
  if [[ -d "${DOCKER_ENTRYPOINT_DIR}" ]]; then
    for file in "${DOCKER_ENTRYPOINT_DIR}/"*.sh; do
      [[ "$(basename "${file}")" == "$(basename "${0}")" ]] && continue
      if [[ -x "${file}" ]]; then
        echo -e "${INFO} Running ${file}"
        if [[ "${file}" != *docker-env* ]]; then
          sudo -u "${JDVM_USER}" bash "${file}"
        else
          sudo -E -u "${JDVM_USER}" bash "${file}"
        fi
      else
        echo -e "${ERROR} ${file} is not executable"
        exit 1
      fi
    done
    echo -e "${SUCCESS} Initialization complete"
  fi
}

startSystemd() {
  step "Start systemd"
  echo -e "${INFO} Executing systemd"
  (
    until ! journalctl 2>&1 | grep -q "No journal files were found"; do
      sleep 0.5
    done
    sleep 0.5
    journalctl --follow --no-tail
  ) &
  echo -e "${SUCCESS} Happy coding! ;)"
  exec /usr/lib/systemd/systemd
}

main
