#!/bin/bash
# SAVES DOCKER ENV VARIABLES TO A FILE

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

main() {
  if [[ ! -e "/etc/jdvm-templates/base/docker-env" ]]; then
    printenv | grep -Ev "^(HOME|LANG|LOGNAME|LS_COLORS|MAIL|PATH|PWD|SHELL|SHLVL|TERM|USER|_)=" | grep -v "^SUDO_" | sed "s/^/export /" \
      | sudo tee "/etc/jdvm-templates/base/docker-env"
  fi
}

main
