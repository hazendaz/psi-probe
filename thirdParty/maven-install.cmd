@REM
@REM Licensed under the GPL License. You may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
@REM
@REM THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
@REM WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
@REM PURPOSE.
@REM

@echo off
pushd "%~dp0"
call mvn install:install-file -Dfile=ojdbc7-12.1.0.2.jar -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0.2 -Dpackaging=jar
call mvn install:install-file -Dfile=ucp-12.1.0.2.jar -DgroupId=com.oracle -DartifactId=ucp -Dversion=12.1.0.2 -Dpackaging=jar
popd
