#!/usr/bin/env bash
# BUILDS WHOLE PROJECT

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
  -u                     Update versions
  -v                     Version check
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
      -u) updateVersions ;;
      -v) versionCheck ;;
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

updateVersions() {
  local mavenVersionIgnore=".*-M-?[0-9]+,.*-alpha-?[0-9]+,.*-beta-?[0-9]+"
  ./mvnw versions:update-properties \
    -pl . \
    -DgenerateBackupPoms=false \
    -Dmaven.version.ignore="${mavenVersionIgnore}"
  exit $?
}

versionCheck() {
  local mavenVersionIgnore=".*-M-?[0-9]+,.*-alpha-?[0-9]+,.*-beta-?[0-9]+"
  ./mvnw versions:display-property-updates \
    -pl . \
    -Dmaven.version.ignore="${mavenVersionIgnore}"
  exit $?
}

forceRemoveBuildxCache() {
  if [[ "${forceRemoveBuildxCache:-false}" == true ]] && docker volume ls -q | grep -q "jdvm"; then
    local volumeName
    volumeName="$(docker volume ls -q | grep "jdvm")"
    echo; step "Remove ${volumeName} volume"
    docker volume rm "${volumeName}"
    docker buildx prune --all --force
  fi
}

mvnCleanInstall() {
  step "Clean and Install"
  if [[ -n "${profile-}" ]]; then
    run ./mvnw clean install "${profile}" "${remainingOptions[@]}"
  else
    run ./mvnw clean install "${remainingOptions[@]}"
  fi
}

main "$@"
