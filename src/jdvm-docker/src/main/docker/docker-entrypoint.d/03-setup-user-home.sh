#!/usr/bin/env bash
# SETUPS USER HOME

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
  setupApps
  setupFirefox
  setupGit
  symlinkWithBackup "Setup Gnome Settings" "/etc/jdvm-templates/base/dconf/user" "/home/${USER}/.config/dconf/user"
  setupGo
  symlinkWithBackup "Setup Kitty" "/etc/jdvm-config/kitty.conf" "/home/${USER}/.config/kitty/kitty.conf"
  setupKubectlKrew
  setupLaunchee
  setupMaven
  setupNpm
  setupNvm
  setupProjects
  setupRust
  setupSdkMan
  symlinkWithBackup "Setup Tealdeer" "/etc/jdvm-config/tealdeer-config.toml" "/home/${USER}/.config/tealdeer/config.toml"
  symlinkWithBackup "Setup XDG User Dirs" "/etc/xdg/user-dirs.defaults" "/home/${USER}/.config/user-dirs.dirs"
  setupYarn
}

setupApps() {
  if [[ ! -e "/home/${USER}/apps" ]]; then
    echo -e "${INFO} Setup Apps..."
    mkdir -v --parents "/home/${USER}/apps"
  fi
}

setupFirefox() {
  cleanupBrokenSymlinks "/home/${USER}/.mozilla"
  local profileDir
  profileDir="$(printf '%s\n' "/home/${USER}/.mozilla/firefox"/*.dev)"
  if notExistsOrNotSymlink "${profileDir}/user.js"; then
    echo -e "${INFO} Setup Firefox..."
    if [[ ! -e "${profileDir}" ]]; then
      firefox --headless -CreateProfile "${USER}" > /dev/null 2>&1
      profileDir="$(printf '%s\n' "/home/${USER}/.mozilla/firefox"/*.dev)"
      echo -e "${INFO} Created $(basename "${profileDir}") Firefox profile"
    fi
    backup "${profileDir}/user.js"
    ln -v --symbolic --force "/etc/jdvm-config/betterfox-user.js" "${profileDir}/user.js"
  else
    local currentSha savedSha
    currentSha=$(sha256sum "$(readlink -f "${profileDir}/user.js")" | awk '{print $1}')
    if [[ -f "${profileDir}/.userjs.sha256" ]]; then
        savedSha="$(cat "${profileDir}/.userjs.sha256")"
    fi
    if [[ "${currentSha}" != "${savedSha-}" ]]; then
        echo -e "${INFO} Detected change in Firefox user.js - removing prefs.js to reload settings"
        echo "${currentSha}" > "${profileDir}/.userjs.sha256"
        if [[ -f "${profileDir}/prefs.js" ]]; then
          rm -v "${profileDir}/prefs.js"
        fi
    fi
  fi
}

setupGit() {
  if [[ ! -e "/home/${USER}/.gitconfig" || -z "$(git config --global "init.defaultBranch")" ]]; then
    echo -e "${INFO} Setup Git: set init.defaultBranch=main..."
    git config --global "init.defaultBranch" "main"
  fi
  if [[ ! -e "/home/${USER}/.gitconfig" || -z "$(git config --global "core.autocrlf")" ]]; then
    echo -e "${INFO} Setup Git: set core.autocrlf=input..."
    git config --global "core.autocrlf" "input"
  fi
}

setupGo() {
  if [[ ! -e "/home/${USER}/.config/go/telemetry/mode" ]]; then
    echo -e "${INFO} Setup Go..."
    if [[ ! -e "/home/${USER}/.config/go/telemetry" ]]; then
      mkdir -v --parents "/home/${USER}/.config/go/telemetry"
    fi
    echo "off" | tee "/home/${USER}/.config/go/telemetry/mode" > /dev/null
    echo "Telemetry turned off"
  fi
}

setupKubectlKrew() {
  cleanupBrokenSymlinks "/home/${USER}/.krew"
  symlinkWithBackup "Setup Kubectl Krew: index" "/opt/krew/index" "/home/${USER}/.krew/index"
  shopt -s nullglob
  for dir in "/opt/krew"/*; do
    local fileName target
    fileName="$(basename "${dir}")"
    target="/home/${USER}/.krew/${fileName}"
    if [[ ! -e "${target}" ]]; then
      echo -e "${INFO} Setup Kubectl Krew: ${fileName}..."
      mkdir -v --parents "${target}"
    fi
  done
  shopt -u nullglob
}

setupLaunchee() {
  if [[ ! -e "/home/${USER}/.config/launchee" ]]; then
    echo -e "${INFO} Setup Launchee..."
    mkdir -v --parents "/home/${USER}/.config/launchee"
  fi
  if [[ ! -e "/home/${USER}/.config/launchee/launchee.yml" ]]; then
    echo -e "${INFO} Setup Launchee Config..."
    printf "%s\n%s\n%s\n" \
      "# USER CONFIGURATION FILE." \
      "# USE THIS FILE TO OVERRIDE OR EXTEND THE DEFAULT SETTINGS MANAGED BY THE CONTAINER." \
      "# HOW TO ADD NEW SHORTCUTS TO LAUNCHEE? CHECK /etc/launchee/launchee.yml"\
      > "/home/${USER}/.config/launchee/launchee.yml"
  fi
}

setupMaven() {
  if [[ ! -e "/home/${USER}/.m2/repository" ]]; then
    echo -e "${INFO} Setup Maven..."
    mkdir -v --parents "/home/${USER}/.m2/repository"
  fi
}

setupNpm() {
  if [[ ! -e "/home/${USER}/.npmrc" ]]; then
    echo -e "${INFO} Setup Npm..."
    echo "ignore-scripts=true" | tee "/home/${USER}/.npmrc" > /dev/null
    echo "Ignored scripts"
  else
    if ! grep -q "ignore-scripts=" "/home/${USER}/.npmrc"; then
      echo -e "${INFO} Setup Npm: ignore scripts..."
      echo "ignore-scripts=true" | tee -a "/home/${USER}/.npmrc" > /dev/null
    fi
  fi
}

setupNvm() {
  cleanupBrokenSymlinks "/home/${USER}/.nvm"
  if [[ ! -e "/home/${USER}/.nvm" ]]; then
    echo -e "${INFO} Setup Nvm..."
    mkdir -v --parents "/home/${USER}/.nvm"
  fi
  for file in "/opt/nvm"/*; do
    local fileName target
    fileName="$(basename "${file}")"
    target="/home/${USER}/.nvm/${fileName}"
    if notExistsOrNotSymlink "${target}"; then
      echo -e "${INFO} Setup Nvm: ${fileName}..."
      backup "${target}"
      ln -v --symbolic --force "${file}" "${target}"
    fi
  done
}

setupProjects() {
  if [[ ! -e "/home/${USER}/projects" ]]; then
    echo -e "${INFO} Setup Projects..."
    mkdir -v --parents "/home/${USER}/projects"
  fi
}

setupRust() {
  cleanupBrokenSymlinks "/home/${USER}/.rustup"
  symlinkWithBackup "Setup Rust: settings.toml" "/opt/rustup/settings.toml" "/home/${USER}/.rustup/settings.toml"
  shopt -s nullglob
  for file in "/opt/rustup"/*; do
    local fileName target
    fileName="$(basename "${file}")"
    target="/home/${USER}/.rustup/${fileName}"
    [[ "${fileName}" == "settings.toml" ]] && continue
    if [[ ! -e "${target}" ]]; then
      echo -e "${INFO} Setup Rust: ${fileName}..."
      mkdir -v --parents "${target}"
    fi
  done
  for file in "/opt/rustup/toolchains"/*; do
    local fileName target
    fileName="$(basename "${file}")"
    target="/home/${USER}/.rustup/toolchains/${fileName}"
    if notExistsOrNotSymlink "${target}"; then
      echo -e "${INFO} Setup Rust: ${fileName}..."
      backup "${target}"
      ln -v --symbolic --force "${file}" "${target}"
    fi
  done
  for file in "/opt/rustup/update-hashes"/*; do
    local fileName target
    fileName="$(basename "${file}")"
    target="/home/${USER}/.rustup/update-hashes/${fileName}"
    if notExistsOrNotSymlink "${target}"; then
      echo -e "${INFO} Setup Rust: ${fileName}..."
      backup "${target}"
      ln -v --symbolic --force "${file}" "${target}"
    fi
  done
  shopt -u nullglob
  cleanupBrokenSymlinks "/home/${USER}/.cargo"
  symlinkWithBackup "Setup Cargo: env" "/opt/cargo/env" "/home/${USER}/.cargo/env"
  shopt -s nullglob
  for file in "/opt/cargo"/*; do
    local fileName target
    fileName="$(basename "${file}")"
    target="/home/${USER}/.cargo/${fileName}"
    [[ "${fileName}" == "env" ]] && continue
    if [[ ! -e "${target}" ]]; then
      echo -e "${INFO} Setup Cargo: ${fileName}..."
      mkdir -v --parents "${target}"
    fi
  done
  symlinkWithBackup "Setup Cargo: rustup" "/opt/cargo/bin/rustup" "/home/${USER}/.cargo/bin/rustup"
  shopt -u nullglob
}

setupSdkMan() {
  cleanupBrokenSymlinks "/home/${USER}/.sdkman"
  if [[ ! -e "/home/${USER}/.sdkman/candidates" ]]; then
    echo -e "${INFO} Setup SdkMan..."
    mkdir -v --parents "/home/${USER}/.sdkman/candidates"
  fi
  shopt -s nullglob
  for dir in "/opt/sdkman"/*; do
    local fileName target
    fileName="$(basename "${dir}")"
    [[ "${fileName}" == "candidates" ]] && continue
    target="/home/${USER}/.sdkman/${fileName}"
    if notExistsOrNotSymlink "${target}"; then
      echo -e "${INFO} Setup SdkMan: ${fileName}..."
      backup "${target}"
      ln -v --symbolic --force "${dir}" "${target}"
    fi
  done
  shopt -u nullglob
}

setupYarn() {
  if [[ ! -e "/home/${USER}/.yarnrc.yml" ]]; then
    echo -e "${INFO} Setup Yarn..."
    echo "enableTelemetry: false" | tee "/home/${USER}/.yarnrc.yml" > /dev/null
    echo "Telemetry turned off"
    echo "enableScripts: false" | tee -a "/home/${USER}/.yarnrc.yml" > /dev/null
    echo "Disabled scripts"
  else
    if ! grep -q "enableTelemetry:" "/home/${USER}/.yarnrc.yml"; then
      echo -e "${INFO} Setup Yarn: telemetry turn off..."
      echo "enableTelemetry: false" | tee -a "/home/${USER}/.yarnrc.yml" > /dev/null
    fi
    if ! grep -q "enableScripts:" "/home/${USER}/.yarnrc.yml"; then
      echo -e "${INFO} Setup Yarn: disable scripts..."
      echo "enableScripts: false" | tee -a "/home/${USER}/.yarnrc.yml" > /dev/null
    fi
  fi
}

cleanupBrokenSymlinks() {
  local path="${1}"
  if [[ -e "${path}" ]]; then
    echo -e "${INFO} Clean Broken Symlinks in ${path}..."
    find "${path}" -xtype l -exec rm -v {} +
  fi
}

symlinkWithBackup() {
  local message="${1}"
  local source=${2}
  local target="${3}"
  if notExistsOrNotSymlink "${target}"; then
    echo -e "${INFO} ${message}..."
    local targetDir
    targetDir="$(dirname "${target}")"
    if [[ ! -e "${targetDir}" ]]; then
      mkdir -v --parents "${targetDir}"
    fi
    backup "${target}"
    ln -v --symbolic --force "${source}" "${target}"
  fi
}

notExistsOrNotSymlink() {
  local symlink="${1}"
  [[ ! -e "${symlink}" || ! -L "${symlink}" ]]
}

backup() {
  local path="${1}"
  if [[ ! -e "${path}" ]]; then
    return 0
  fi

  local backupPath="${path}.bak"
  if [[ -f "${path}" ]]; then
    cp -v "${path}" "${backupPath}"
  elif [[ -d "${path}" ]]; then
    rm -rf "${backupPath}"
    cp -r -v "${path}" "${backupPath}"
    rm -rf "${path}"
  fi
}

main
