#!/bin/bash
# REGENERATE MAVEN WRAPPER

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
  step "Regenerate Maven Wrapper"
  local oldValue newValue
  oldValue=$(grep "apache-maven" ".mvn/wrapper/maven-wrapper.properties" | sed "s|.*apache-maven/\(.*\)/apache-maven.*|\1|")
  newValue=$(grep "mvn.version=" "src/jdvm-resources/src/main/resources/versions.properties" | sed "s/.*=//")
  if [[ "${oldValue}" != "${newValue}" ]]; then
    mvn wrapper:wrapper -Dmaven="${newValue}"
    replaceHttpWithHttps
    isUpdated=true
  fi
  if [[ "${isUpdated:-false}" == false ]]; then
    echo -e "${WARNING} Nothing to update"
  fi
}

readOptions() {
  while getopts ":h" option; do
    case "${option}" in
      h|?) usage ;;
    esac
  done
  shift $((OPTIND - 1))
}

replaceHttpWithHttps() {
  for file in $(grep -Ril --exclude-dir={target,scripts} "http://www.apache.org"); do
    sed -i "s|http\(://www.apache.org\)|https\1|" "${file}"
  done
}

main "$@"
