@REM WARNING: SHUT DOWN DOCKER DESKTOP BEFORE RUNNING THIS SCRIPT
@REM SHRINKS DOCKER DESKTOP VHDX DISK WHICH GROWS OVER TIME
@REM YOU CAN CHECK THE SIZE UNDER PATH: %LOCALAPPDATA%\Docker\wsl\disk

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
set vhdxFile=docker_data.vhdx
set vhdxPath=Docker\wsl\disk

echo Shutting down WSL...
wsl --shutdown

echo Creating a temporary file with diskpart commands...
set diskpartCommands=%TEMP%\diskpart_commands.txt
echo select vdisk file="%%LOCALAPPDATA%%\%vhdxPath%\%vhdxFile%" > %diskpartCommands%
echo compact vdisk >> %diskpartCommands%
echo exit >> %diskpartCommands%

echo Shrinking %vhdxFile% with diskpart...
diskpart /s %diskpartCommands%

echo Deleting a temporary file with diskpart commands...
del %diskpartCommands%

echo DONE!
pause
endlocal
