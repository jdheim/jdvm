<p align="center">
    <img src="docs/images/logo.png" width="150" alt="JDVM"/>
</p>

<p align="center">
  <a href="src/jdvm-docker/src/main/docker/Dockerfile" rel="noreferrer">
      <img src="https://img.shields.io/badge/Dockerfile-1350CC?logo=docker&logoColor=white" alt="Dockerfile"/>
  </a>
  <a href="https://hub.docker.com/r/jdheim/jdvm" rel="noreferrer">
      <img src="https://img.shields.io/docker/pulls/jdheim/jdvm?label=Docker%20Pulls&logo=docker&logoColor=white&color=1350CC&cacheSeconds=21600" alt="Docker Pulls"/>
  </a>
  <a href="https://github.com/jdheim/jdvm/releases" rel="noreferrer">
      <img src="https://img.shields.io/github/v/release/jdheim/jdvm?label=Latest%20Release&logo=github&logoColor=white&include_prereleases" alt="Latest Release"/>
  </a>
  <a href="LICENSE" rel="noreferrer">
      <img src="https://img.shields.io/github/license/jdheim/jdvm?label=License&logo=googledocs&logoColor=white" alt="License"/>
  </a>
  <br>
  <a href="https://github.com/jdheim/jdvm/actions/workflows/main-ea.yml" rel="noreferrer">
      <img src="https://img.shields.io/github/actions/workflow/status/jdheim/jdvm/main-ea.yml?label=Build,%20Test%20and%20Push%20Image&logo=github&logoColor=white" alt="Build, Test and Push Image"/>
  </a>
  <a href="https://github.com/jdheim/jdvm/actions/workflows/attestation-verification-ea.yml" rel="noreferrer">
      <img src="https://img.shields.io/github/actions/workflow/status/jdheim/jdvm/attestation-verification-ea.yml?label=Attestation%20Verification&logo=github&logoColor=white" alt="Attestation Verification"/>
  </a>
</p>

## 💻 JDVM: One VM to Code Them All

> _Less configuration, more creation_

> [!CAUTION]
> 🛠️ This repository is under active development.
>
> Only Early Access (EA) builds are provided at this stage.

The **JDVM** is a **powerful development environment** designed specifically **for Java Developers working on Windows**
or **Linux**. This Virtual Machine is based on [Ubuntu](https://hub.docker.com/_/ubuntu), providing a familiar Linux
environment that integrates seamlessly with Windows through Docker Desktop or Linux through Docker Engine.

The main advantage of the JDVM is its **time-saving** nature. Developers no longer need to spend valuable time manually
setting up their development environment or installing the necessary tools. Everything you need - such as OpenJDK (via
Eclipse Temurin), IDE, build tools, Version Control System, Docker, Kubernetes and so on - is **pre-configured and ready
to go right out of the box**.

With the JDVM, developers can **focus on writing code and delivering high-quality software**, instead of worrying about
installation issues or environment configuration. It's an ideal solution for anyone looking for a streamlined,
efficient, and hassle-free Java development setup.

It requires **2** dependencies on Windows:

* [**WSL version 2**](https://learn.microsoft.com/en-us/windows/wsl/install) (Windows Subsystem for Linux) - lets you
  install a Linux distribution and use Linux applications, utilities, and Bash command-line tools directly on Windows,
  unmodified, without the overhead of a traditional virtual machine or dualboot setup. Check quickly if you have it with
  command: `wsl --version` in CMD/PowerShell
* [**Docker Desktop**](https://docs.docker.com/desktop/install/windows-install/) - lets you build, share, and run
  containerized applications. It provides a straightforward GUI (Graphical User Interface) that lets you manage your
  containers, applications, and images directly from your machine

It requires only **1** dependency on Linux:

* [**Docker Engine**](https://docs.docker.com/engine/install/) - lets you build, share, and run containerized
  applications. It does not provide any GUI, management is performed via CLI

JDVM image can be pulled from DockerHub: https://hub.docker.com/r/jdheim/jdvm

Container from the image can be started with a batch script (more in the next section):

![Restart batch script](docs/images/restart.bat.gif)

After the container is started, you'll see the dock with the applications having a graphical interface:

![Launchee](docs/images/launchee.gif)

For example, IntelliJ IDEA (not installed by default, you can install it manually):

![IntelliJ IDEA](docs/images/intellij-idea.gif)

All applications having graphical interface (even the ones not included in the dock, e.g. `engrampa` archive manager)
open as a separate window which you can move freely on your Windows / Linux:

![Windows Taskbar](docs/images/windows-taskbar.gif)

## 🛠️ First Setup and Start

### 1. Installation

Please follow the linked official documentation and install all required dependencies mentioned above.

### 2. Setup (in case of Windows)

You can place a `.wslconfig` file in `C:\Users\%USERNAME%` where you can configure the amount of memory or the number of
logical processors you want to assign to Docker Desktop.

> [!WARNING]
> You need to restart WSL2 service with command: `wsl --shutdown`, and then Docker Desktop to see the changes

Example:

```
[wsl2]
memory=10GB
processors=4

[experimental]
autoMemoryReclaim=dropcache
```

Full documentation: https://learn.microsoft.com/en-us/windows/wsl/wsl-config#wslconfig

### 3. Start

1. Start Docker Desktop (in case of Windows).
2. After that start JDVM container.
3. Enjoy!

See an example batch script which starts or restarts JDVM: [restart.bat](support-scripts/restart.bat) or bash
script: [restart.sh](support-scripts/restart.sh). Change the value of `IMAGE_VERSION` variable if there is a newer
version available.

Batch scripts can be executed on Windows in CMD/PowerShell or with a double mouse click. Bash scripts can be executed on
Linux in the terminal.

### 4. Quit

#### Windows

Quit Docker Desktop to quit JDVM.

#### Linux

Run in terminal:

```bash
docker container stop jdvm; docker container rm jdvm
```

## 💾 Persisting changes

By default, Docker containers do not persist changes. You'll need to
use [volumes](https://docs.docker.com/storage/volumes/) to enable persistence.

In [restart.bat](support-scripts/restart.bat)/[restart-with-wayland.bat](support-scripts/restart-with-wayland.bat)/[restart.sh](support-scripts/restart.sh)
I defined **6** volumes:

* `projects` under `/home/dev/projects` - a place where you can start your projects
* `maven` under `/home/dev/.m2/repository` - local Maven repository where artifacts are stored
* `home` under `/home/dev` - home directory of JDVM user
* `docker` under `/var/lib/docker` - a place where Docker stores the data (e.g. downloaded images)
* `/mnt/shared` - a shared place between JDVM and `C:\Users\%USERNAME%\shared` on Windows or
  `/home/${USER}/shared` on Linux
* `/mnt/wslg` a shared socket bindings required to display applications and enable audio output from JDVM on Windows or
  Linux
    * On Linux `/tmp/.X11-unix` for display and `/run/user/${UID}/pulse/native` for audio needs to be mounted explicitly
      instead

> [!WARNING]
> For the best experience, I recommend using X11. Wayland is supported, but still experimental and may be less stable.

## 🚀 How to add a shortcut to the app launcher ([Launchee](https://github.com/jdheim/launchee))?

You can add a shortcut to the app launcher by editing `/home/dev/.config/launchee/launchee.yml` file. Here is the
example:

```yaml
# USER CONFIGURATION FILE.
# USE THIS FILE TO OVERRIDE OR EXTEND THE DEFAULT SETTINGS MANAGED BY THE CONTAINER.
# HOW TO ADD NEW SHORTCUTS TO LAUNCHEE? CHECK /etc/launchee/launchee.yml

shortcuts:
  - name: "IntelliJ IDEA"
    icon: "/home/dev/apps/intellij-idea/bin/idea.svg"
    command: "idea"
```

IntelliJ IDEA, by default, uses X11. If you want to make it use Wayland, set:

```yaml
    command: "idea -Dawt.toolkit.name=WLToolkit"
```

The configuration managed by the container is located in `/etc/launchee/launchee.yml`.

Additional examples can be found in [Launchee](https://github.com/jdheim/launchee/tree/main/src/test/config)
repository.

> [!WARNING]
> Better Documentation is coming soon!

## 🔄 How to update Docker Desktop/WSL?

### Update Docker Desktop

1. Go to Docker Desktop settings
2. Click on "Software updates"
3. Click on "Check for updates"
4. Install updates

### Update WSL

1. Quit Docker Desktop
2. Open PowerShell
3. Check version: `wsl --version`
4. Close WSL: `wsl --shutdown`
5. Update WSL: `wsl --update`
    * Optionally, you can update WSL to a pre-release version (less stable): `wsl --update --pre-release`

## 🐛 Known Issues

### Wayland: App icons do not load (all show the [Tux](https://en.wikipedia.org/wiki/Tux_(mascot)) icon)

If you use [restart-with-wayland.bat](support-scripts/restart-with-wayland.bat), you may notice that all applications
using wayland display the Tux icon instead of their proper icons.

The fix is to:

1. Download this repository as a ZIP (`Code` -> `Download ZIP`)
2. Unpack the contents of the [wayland-icon-fix](wayland-icon-fix) folder to `\\wsl$\docker-desktop\usr\share` using
   Windows Explorer:
    * [wayland-icon-fix/applications](wayland-icon-fix/applications) having `*.desktop` files go to
      `\\wsl$\docker-desktop\usr\share\applications`
    * [wayland-icon-fix/icons/apps](wayland-icon-fix/icons/apps) having `*.png` icons go to
      `\\wsl$\docker-desktop\usr\share\icons\apps`
3. Restart Docker Desktop to see the changes

If you install your own application, you'll need to create a matching `*.desktop` file:

1. In a terminal run `tail "/mnt/wslg/weston.log" -f | grep "appId:"`
2. Launch your application and check the `appId` shown.
3. The `*.desktop` file created in `applications` directory must have the exact same filename as the `appId`.
4. The `StartupWMClass` field inside the `*.desktop` file must also match the `appId`.
5. Place your application's icon into the `icons/apps` directory
6. Restart Docker Desktop to see the changes

Minimal `jetbrains-idea.desktop` file example:

```ini
[Desktop Entry]
Name=IntelliJ IDEA
Exec=idea
Type=Application
Icon=/usr/share/icons/apps/jetbrains-idea.png
StartupWMClass=jetbrains-idea
```

> [!WARNING]
> After Docker Desktop update the above needs to be reapplied again, so make a backup of these files.

### Wayland: App GUI freezes after Windows sleep/resume

After resuming Windows from sleep, GUI applications may appear frozen. To fix this, right-click the application in the
taskbar and select **Close**.

**Launchee** will automatically restart after a few seconds.

This issue is specific to **GTK on Wayland** (it does not occur on X11). For more details,
see: [KWin Wiki: Restarting](https://invent.kde.org/plasma/kwin/-/wikis/Restarting)

### VHDX disk grows over time

This issue has been resolved in Docker Desktop 4.34. Just quit Docker Desktop in order to reclaim disk space.

<details>
  <summary>For Docker Desktop older than 4.34 click here</summary>
  <br>
  Deleting data in Docker Desktop (e.g. images, volumes) does not cause the size
  of <a href="https://en.wikipedia.org/wiki/VHD_(file_format)">VHDX (Virtual Hard Disk)</a> to decrease. Once the VHDX grows it will
  remain that size, or grow larger as the amount of data increases.

You can check the size of your Docker Desktop VHDX file under path: `%LOCALAPPDATA%\Docker\wsl\disk` (`%LOCALAPPDATA%`
is Windows environment variable which resolves to `C:\Users\%USERNAME%\AppData\Local`).

If you want to recover some of the disk space on Windows that is being consumed by the VHDX, you can shrink the VHDX.

See example batch script which automates the shrinking of VHDX
via [diskpart](https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/diskpart) Windows
utility: [shrink-vhdx.bat](deprecated/scripts/shrink-vhdx.bat). Change the value of `vhdxFile` and `vhdxPath` variables
if the name/path of your VHDX is different.

:warning: **WARNING: Shut down Docker Desktop before running the script!**

![Docker Desktop Quit](docs/images/docker-desktop-quit.png)

Before:

![Shrink VHDX - Before](docs/images/shrink-vhdx-before.png)

Shrinking:

![Shrink VHDX](docs/images/shrink-vhdx.bat.png)

After:

![Shrink VHDX - After](docs/images/shrink-vhdx-after.png)
</details>

### Firefox can crash if shared memory size is too low

To prevent crashes from happening when running Firefox inside JDVM, the size of the shared memory located at
`/dev/shm` must be increased. The issue is documented [here](https://bugzilla.mozilla.org/show_bug.cgi?id=1338771#c10).

By default, the size in Docker containers is **64MB**, which is not enough. It is recommended to use a size of **2GB**.
This value is arbitrary, but known to work well. Setting the size of `/dev/shm` can be done by adding the
`--shm-size 2g` parameter to the `docker run` command. It is already added
to [restart.bat](support-scripts/restart.bat)/[restart.sh](support-scripts/restart.sh).

### Missing `overlay2` Storage Driver

To check the Storage Driver inside JDVM run command: `docker system info --format "{{.Driver}}"`. If it is not
`overlay2`, it means you're missing `--mount source=docker,target=/var/lib/docker` parameter in `docker run`
command. It is already added to [restart.bat](support-scripts/restart.bat)/[restart.sh](support-scripts/restart.sh).

Thanks to this, Docker can use `overlay2` as a Storage Driver, otherwise it falls back to `vfs`. Using `vfs` may cause
issues when creating a k3s kubernetes cluster with [k3d](https://k3d.io).

## 📜 Useful Scripts

On JDVM there are some [useful scripts](src/jdvm-docker/src/main/docker/scripts) which you can use (more coming soon!).

<details>
  <summary>Click here to see an example</summary>
  <br>
  <p>If you want to check versions installed (see
  also <a href="https://github.com/jdheim/jdvm/releases">Releases</a> tab), type:</p>
  <img src="docs/images/jdvm-versions.gif" alt="Versions" width="550px">
</details>

## 📚 Useful Docs

* [Accessing application in a Windows browser](docs/accessing-application-in-a-windows-browser.md)
* [Certificate Setup](docs/certificate.md)
* [Verifying Provenance and SBOM Attestations](docs/provenance-and-sbom.md)

## 🏗️ How to build image locally?

Clone or download as a ZIP this repository.

Install the latest JDK on Windows / Linux: https://adoptium.net/temurin/releases

Windows: Use [Maven wrapper](https://maven.apache.org/wrapper/): [mvnw.cmd](mvnw.cmd) and type in CMD/PowerShell:

```shell
.\mvnw.cmd clean install -Pbuild-image
```

Linux: Use [Maven wrapper](https://maven.apache.org/wrapper/): [mvnw](mvnw) and type in Terminal:

```shell
./mvnw clean install -Pbuild-image
```

If you want to remove builder cache after the image is built, use `-Pbuild-image,remove-buildx-cache` Maven profiles or
open Docker Desktop, go to Volumes tab and remove `buildx_buildkit_jdvm-builder0_state` volume manually. You can also
execute command:

```shell
docker volume rm "buildx_buildkit_jdvm-builder0_state"
```

Older releases are removed from DockerHub. If you want to use an older release for whatever reason, you need to build it
yourself. In the [Releases](https://github.com/jdheim/jdvm/releases) tab you can find zip with the source code.

## 💖 Support

Hey there! If you enjoy my work and would like to support me, consider buying me a coffee! :slightly_smiling_face: Your
contributions help me keep creating, and I truly appreciate every bit of support you offer.

<p>
  <a href="https://www.buymeacoffee.com/jdheim" rel="noreferrer">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy me a Coffee" style="height: 40px !important;width: 160px !important;" >
  </a>
</p>

Also, please consider giving this project a ⭐ on GitHub. This kind of support helps promote the project and lets others
know that it's worth checking out.

Thank you for being amazing!

## ©️ License

© 2024-2025 JDHeim

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for full license terms.

This repository defines a [Dockerfile](src/jdvm-docker/src/main/docker/Dockerfile) that builds a development environment
based on [Ubuntu](https://hub.docker.com/_/ubuntu) and includes third-party open-source components.

As with all Docker images, the resulting Docker image includes software licensed under various open-source licenses,
including but not limited to:

* Apache License 2.0 (e.g. Apache Maven)
* MIT License (e.g. Git LFS, supporting CLI tools)
* Mozilla Public License 2.0 (e.g. Firefox browser)
* GNU General Public License v2 with Classpath Exception (e.g. Eclipse Temurin JDK)
* GNU General Public License v2 (e.g. Git)
* GNU General Public License v3 (e.g. Kitty terminal)

These components are included in binary form and retain their original license terms. Consult the respective project
websites for full licensing details.

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies
with any relevant licenses for all software contained within.

---

Java® is a registered trademark of Oracle and/or its affiliates.  
Docker® is a registered trademark of Docker, Inc.  
JDVM is an independent open-source project and is not affiliated with or endorsed by Oracle or Docker.
