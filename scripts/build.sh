#!/bin/bash
# BUILDS WHOLE PROJECT

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

[[ -f "$(dirname "${BASH_SOURCE[0]}")/common/functions.sh" ]] && . "$(dirname "${BASH_SOURCE[0]}")/common/functions.sh"

readonly CURRENT_VERSIONS="test/jdvm-testcontainers/target/versions.md"

usage() {
  cat << EOF
Usage: $(basename "$0") [OPTION]...

Builds whole project

OPTIONS:
  -i                     Build image and remove unused images
  -r                     Remove buildx cache
  -t                     Run tests
  -d                     Dry-run JReleaser release
EOF
  exit 1
}

main() {
  cd ..
  readOptions "$@"
  scripts/common/updateVersion.sh "$(getProjectVersion)"
  scripts/common/updateCopyright.sh
  scripts/common/regenerateMvnWrapper.sh
  mvnCleanInstall
  forceRemoveBuildxCache
}

readOptions() {
  while [[ "$#" -gt 0 ]]; do
    case "${1}" in
      -i) profile+="build-image," ;;
      -r) profile+="remove-buildx-cache," ;;
      -t) profile+="integration-tests,prepare-release," ;;
      -d) dryRunJReleaserRelease ;;
      -h|--help) usage ;;
      *) remainingOptions+=("${1}") ;;
    esac
    shift
  done
  if [[ -n "${profile-}" ]]; then
    profile="-P${profile%,}"
    if [[ "${profile}" == *"remove-buildx-cache"* && "${profile}" != *"build-image"* ]]; then
      forceRemoveBuildxCache=true
    fi
  fi
}

dryRunJReleaserRelease() {
  step "Dry-run JReleaser release"
  if [[ ! -f "${CURRENT_VERSIONS}" ]]; then
    echo -e "${ERROR} The ${CURRENT_VERSIONS} is missing. Run VersionsTest#versionsOutput to generate it"
    exit 1
  fi
  if [[ -z "${GITHUB_TOKEN-}" ]]; then
    echo -e "${ERROR} The GITHUB_TOKEN env variable is not set"
    exit 1
  fi
  printf "## Changelog\n\n{{changelogChanges}}{{changelogContributors}}%s" "$(cat test/jdvm-testcontainers/target/versions.md)" > "test/jdvm-testcontainers/target/changelog.tpl"
  JRELEASER_GITHUB_TOKEN=${GITHUB_TOKEN-} run jreleaser release --dry-run --output-directory=target
  exit $?
}

forceRemoveBuildxCache() {
  if [[ "${forceRemoveBuildxCache:-false}" == true ]] && docker volume ls -q | grep -q "jdvm"; then
    local volumeName
    volumeName="$(docker volume ls -q | grep "jdvm")"
    echo; step "Remove ${volumeName} volume"
    docker volume rm "${volumeName}"
  fi
}

mvnCleanInstall() {
  step "Clean and Install"
  if [[ -n "${profile-}" ]]; then
    run mvn clean install "${profile}" "${remainingOptions[@]}"
  else
    run mvn clean install "${remainingOptions[@]}"
  fi
}

main "$@"
