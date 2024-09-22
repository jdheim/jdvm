#!/bin/bash
# COMPARES LATEST VERSIONS FROM GITHUB WITH CURRENT VERSIONS

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
set -o pipefail # DON'T HIDE ERRORS WITHIN PIPES

readonly STEP="[\e[1;96mSTEP\e[0m]"
readonly LINE="\e[1;96m-----\e[0m"
readonly INFO="[\e[1;34mINFO\e[0m]"
readonly ERROR="[\e[1;31mERROR\e[0m]"

readonly CURRENT_VERSIONS="target/versions.md"
readonly LATEST_VERSIONS="target/versions-old.md"
readonly LATEST_RELEASES_URL="https://api.github.com/repos/jdheim/jdvm/releases/latest"

usage() {
  cat << EOF
Usage: $(basename "$0")

Compares latest versions from GitHub with current versions
EOF
  exit 1
}

main() {
  readOptions "$@"
  if [[ "${EXEC_MAVEN_PLUGIN}" != "true" ]]; then
    cd ../../..
  fi
  validateCurrentVersionsFile
  fetchLatestVersionsFileFromGitHub
  compareVersionsFiles
}

step() {
    local message="${1}"
    echo -e "${STEP} ${LINE} ${message} ${LINE}"
}

readOptions() {
  while getopts ":h" option; do
    case "${option}" in
      h|?) usage ;;
    esac
  done
}

validateCurrentVersionsFile() {
  if [[ ! -f "${CURRENT_VERSIONS}" ]]; then
    echo -e "${ERROR} The ${CURRENT_VERSIONS} is missing. Run VersionsTest#versionsOutput to generate it"
    exit 1
  fi
}

fetchLatestVersionsFileFromGitHub() {
  step "Fetch latest versions from GitHub"
  local jsonResponse="$(curl -s "${LATEST_RELEASES_URL}")"
  local body="$(echo "${jsonResponse}" | jq -r .body | tr -d '\r' | sed -e '/## Versions/,$!d' -e 's/\(\*\*.*\*\*\).*[⬆️️🆕]/\1/')"
  echo -e "${INFO} Save to ${LATEST_VERSIONS}"
  echo "${body}" > "${LATEST_VERSIONS}"
}

compareVersionsFiles() {
  step "Compare latest versions from GitHub with current versions"
  while IFS= read -r diffLine; do
    if [[ "${diffLine}" == *"  |"* || "${diffLine}" == *"  >"* ]]; then
      updateCurrentVersionsFile
    fi
  done < <(diff --side-by-side --suppress-common-lines --width=300 "${LATEST_VERSIONS}" "${CURRENT_VERSIONS}" | grep "  [|>]")
  if [[ -z "${updateLogs}" ]]; then
    echo -e "${INFO} Versions are the same"
  else
    printf "%b" "${updateLogs}"
  fi
}

updateCurrentVersionsFile() {
  local newEmoji="🆕"
  local updateEmoji="⬆️"
  local dependencyName="$(echo "${diffLine}" | sed "s/.*[|>].*- \[\([^]]*\)\].*/\1/")"
  local latestVersion="$(grep "\[${dependencyName}\]" "${LATEST_VERSIONS}" | sed "s/^- [^*]*\*\*\([^*]*\)\*\*.*/\1/")"
  local currentVersion="$(grep "\[${dependencyName}\]" "${CURRENT_VERSIONS}" | sed "s/^- [^*]*\*\*\([^*]*\)\*\*.*/\1/")"
  if [[ -z "${latestVersion}" && -n "${currentVersion}" ]]; then
    processUpdateCurrentVersionsFile "Adding" " ${newEmoji}"
  elif [[ -n "${latestVersion}" && -n "${currentVersion}" && "${latestVersion}" != "${currentVersion}" ]]; then
    processUpdateCurrentVersionsFile "Updating" " ${updateEmoji}"
  elif [[ -n "${latestVersion}" && -n "${currentVersion}" && "${latestVersion}" == "${currentVersion}" ]]; then
    processUpdateCurrentVersionsFile "Refreshing"
  fi
}

processUpdateCurrentVersionsFile() {
  local operation="${1}"
  local emoji="${2}"
  updateLogs+="$(echo "${INFO} ${operation} ${dependencyName}\n")"
  sed -i "s/\(\[${dependencyName}\].*\*\*\).*/\1${emoji}/" "${CURRENT_VERSIONS}"
}

main "$@"
