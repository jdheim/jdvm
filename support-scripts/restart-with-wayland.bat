@REM RESTARTS CONTAINER ON WINDOWS USING WAYLAND WITH X11 FALLBACK

@REM
@REM © 2024-2025 JDHeim
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off
setlocal

set IMAGE_VERSION=latest-ea
set IMAGE_NAME=jdvm
set IMAGE_NAMESPACE=jdheim
set CONTAINER_NAME=%IMAGE_NAME%

echo Stopping %CONTAINER_NAME%...
docker container stop "%CONTAINER_NAME%" > nul 2>&1

echo Removing %CONTAINER_NAME%...
docker container rm "%CONTAINER_NAME%" > nul 2>&1

echo Checking for a newer image %IMAGE_NAMESPACE%/%IMAGE_NAME%:%IMAGE_VERSION%...
docker image pull "%IMAGE_NAMESPACE%/%IMAGE_NAME%:%IMAGE_VERSION%"

echo Starting %IMAGE_NAMESPACE%/%IMAGE_NAME%:%IMAGE_VERSION%...
docker container run --privileged -d ^
    --name "%CONTAINER_NAME%" ^
    --hostname "%CONTAINER_NAME%" ^
    -p 80:80 -p 443:443 ^
    --mount source=projects,target=/home/dev/projects ^
    --mount source=maven,target=/home/dev/.m2/repository ^
    --mount source=home,target=/home/dev ^
    --mount source=docker,target=/var/lib/docker ^
    --mount type=bind,source="%USERPROFILE%/shared",target=/mnt/shared ^
    --mount type=bind,source=/mnt/host/wslg,target=/mnt/wslg ^
    --env PULSE_SERVER="unix:/mnt/wslg/PulseServer" ^
    --env DISPLAY=":0" ^
    --env WAYLAND_DISPLAY="wayland-0" ^
    --ipc host ^
    --shm-size 2g ^
    "%IMAGE_NAMESPACE%/%IMAGE_NAME%:%IMAGE_VERSION%" > nul

echo Cleaning dangling images...
docker image prune -f > nul

echo SUCCESS! Happy coding! ;)
pause
endlocal
