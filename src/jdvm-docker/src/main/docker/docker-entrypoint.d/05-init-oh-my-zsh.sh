#!/bin/bash
# INITIALIZES OH MY ZSH

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

readonly INFO="\e[1;34m>\e[0m"

main() {
  createCustomConfigurationDir
  linkGitStatus
  fixOhMyZshInIntelliJIdea
}

createCustomConfigurationDir() {
  if [[ ! -e "/home/${USER}/.oh-my-zsh/custom" ]]; then
    echo -e "${INFO} Create custom configuration dir..."
    mkdir -v --parents "/home/${USER}/.oh-my-zsh/custom"
  fi
}

linkGitStatus() {
  if [[ ! -e "/home/${USER}/.cache/gitstatus/gitstatusd-linux-x86_64" ]]; then
    echo -e "${INFO} Link Git Status..."
    if [[ ! -e "/home/${USER}/.cache/gitstatus" ]]; then
      mkdir -v --parents "/home/${USER}/.cache/gitstatus"
    fi
    ln -v --symbolic --force "/opt/oh-my-zsh/themes/powerlevel10k/gitstatus/gitstatusd-linux-x86_64" "/home/${USER}/.cache/gitstatus/gitstatusd-linux-x86_64"
  fi
}

fixOhMyZshInIntelliJIdea() {
  local intellijIdeaFontsDir="/home/${USER}/apps/intellij-idea/jbr/lib/fonts"
  local fontsDir="/usr/share/fonts/truetype"
  if [[ -e "${intellijIdeaFontsDir}" ]]; then
    if [[ ! -e "${intellijIdeaFontsDir}/MesloLGS-NF-Regular.ttf" ]]; then
      echo -e "${INFO} Fix OhMyZsh in IntelliJ IDEA: MesloLGS-NF-Regular.ttf..."
      ln -v --symbolic --force "${fontsDir}/meslo/MesloLGS-NF-Regular.ttf" "${intellijIdeaFontsDir}/MesloLGS-NF-Regular.ttf"
    fi
    if [[ ! -e "${intellijIdeaFontsDir}/MesloLGS-NF-Bold.ttf" ]]; then
      echo -e "${INFO} Fix OhMyZsh in IntelliJ IDEA: MesloLGS-NF-Bold.ttf..."
      ln -v --symbolic --force "${fontsDir}/meslo/MesloLGS-NF-Bold.ttf" "${intellijIdeaFontsDir}/MesloLGS-NF-Bold.ttf"
    fi
    if [[ ! -e "${intellijIdeaFontsDir}/MesloLGS-NF-Italic.ttf" ]]; then
      echo -e "${INFO} Fix OhMyZsh in IntelliJ IDEA: MesloLGS-NF-Italic.ttf..."
      ln -v --symbolic --force "${fontsDir}/meslo/MesloLGS-NF-Italic.ttf" "${intellijIdeaFontsDir}/MesloLGS-NF-Italic.ttf"
    fi
    if [[ ! -e "${intellijIdeaFontsDir}/MesloLGS-NF-Bold-Italic.ttf" ]]; then
      echo -e "${INFO} Fix OhMyZsh in IntelliJ IDEA: MesloLGS-NF-Bold-Italic.ttf..."
      ln -v --symbolic --force "${fontsDir}/meslo/MesloLGS-NF-Bold-Italic.ttf" "${intellijIdeaFontsDir}/MesloLGS-NF-Bold-Italic.ttf"
    fi
  fi
}

main
