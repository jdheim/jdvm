#!/usr/bin/env bash
# FIXES USER VOLUME

#
# © 2024-2025 JDHeim.com
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

readonly INFO="\e[1;34m>\e[0m"

main() {
  fixUserVolumePermissions "/home/${JDVM_USER}/projects"
  fixUserVolumePermissions "/home/${JDVM_USER}/.m2"
  fixUserVolumePermissions "/home/${JDVM_USER}"
}

fixUserVolumePermissions() {
  local path="${1}"
  if [[ -d "${path}" ]] && [[ "$(stat -c %u "${path}")" != "$(id -u "${JDVM_USER}")" || "$(stat -c %g "${path}")" != "$(id -g "${JDVM_USER}")" ]]; then
    echo -e "${INFO} Fix User Volume Permissions for ${path}..."
    chown --recursive "$(id -u "${JDVM_USER}"):$(id -g "${JDVM_USER}")" "${path}"
  fi
}

main
