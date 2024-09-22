@REM SAVES DOCKER IMAGE FROM WITHIN THE CONTAINER AND LOADS IT INTO LOCAL REGISTRY ON WINDOWS

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

echo Save %IMAGE_NAMESPACE%/%IMAGE_NAME%:%IMAGE_VERSION% to /tmp/%IMAGE_NAME%.tar inside %CONTAINER_NAME% container...
docker exec "%CONTAINER_NAME%" docker save "%IMAGE_NAMESPACE%/%IMAGE_NAME%:%IMAGE_VERSION%" -o "/tmp/%IMAGE_NAME%.tar" > nul

echo Copy /tmp/%IMAGE_NAME%.tar inside %CONTAINER_NAME% container to %TEMP%\%IMAGE_NAME%.tar...
docker cp "%CONTAINER_NAME%:/tmp/%IMAGE_NAME%.tar" "%TEMP%\%IMAGE_NAME%.tar" > nul

echo Remove /tmp/%IMAGE_NAME%.tar inside %CONTAINER_NAME% container...
docker exec "%CONTAINER_NAME%" rm "/tmp/%IMAGE_NAME%.tar"

echo Load %TEMP%\%IMAGE_NAME%.tar...
docker load -i "%TEMP%\%IMAGE_NAME%.tar" > nul

echo Remove %TEMP%\%IMAGE_NAME%.tar...
del "%TEMP%\%IMAGE_NAME%.tar" > nul

echo DONE!
pause
endlocal
