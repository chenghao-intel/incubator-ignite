::  Licensed to the Apache Software Foundation (ASF) under one or more
::  contributor license agreements.  See the NOTICE file distributed with
::  this work for additional information regarding copyright ownership.
::  The ASF licenses this file to You under the Apache License, Version 2.0
::  (the "License"); you may not use this file except in compliance with
::  the License.  You may obtain a copy of the License at
::
::       http://www.apache.org/licenses/LICENSE-2.0
::
::  Unless required by applicable law or agreed to in writing, software
::  distributed under the License is distributed on an "AS IS" BASIS,
::  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
::  See the License for the specific language governing permissions and
::  limitations under the License.

:: Target class path resolver.
::
:: Can be used like:
::       call "%GRIDGAIN_HOME%\os\bin\include\target-classpath.bat"
:: in other scripts to set classpath using libs from target folder.
::
:: Will be excluded in release.

@echo off

for /D %%F in (%GRIDGAIN_HOME%\os\modules\*) do if not %%F == "%GRIDGAIN_HOME%\os\modules" call :includeToClassPath %%F

for /D %%F in (%GRIDGAIN_HOME%\modules\*) do if not %%F == "%GRIDGAIN_HOME%\modules" call :includeToClassPath %%F

goto :eof

:includeToClassPath
if exist "%1\target\" (
    if exist "%1\target\classes\" call :concat %1\target\classes

    if exist "%1\target\libs\" call :concat %1\target\libs\*
)
goto :eof

:concat
set GRIDGAIN_LIBS=%GRIDGAIN_LIBS%;%1
goto :eof
