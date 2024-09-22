#!/bin/bash
# UPDATES VERSION

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

[[ -f "$(dirname "${BASH_SOURCE[0]}")/functions.sh" ]] && . "$(dirname "${BASH_SOURCE[0]}")/functions.sh"

main() {
  local version="${1-}"
  step "Update version"
  if [[ -z "${version}" ]]; then
    echo -e "${ERROR} Version is missing"
    exit 1
  fi
  updatePropertyInXmlFile "pom.xml" "n=http://maven.apache.org/POM/4.0.0" "/n:project/n:version" "${version}"
  for module in $(getProjectModules); do
    updatePropertyInXmlFile "${module}/pom.xml" "n=http://maven.apache.org/POM/4.0.0" "/n:project/n:parent/n:version" "${version}"
  done
  if [[ "${isUpdated:-false}" == false ]]; then
    echo -e "${WARNING} Nothing to update"
  fi
}

main "$@"
