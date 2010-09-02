@echo off

REM
REM @(#)jhsearch.bat	1.13 06/10/30
REM 
REM Copyright (c) 2006 Sun Microsystems, Inc.  All Rights Reserved.
REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
REM 
REM This code is free software; you can redistribute it and/or modify it
REM under the terms of the GNU General Public License version 2 only, as
REM published by the Free Software Foundation.  Sun designates this
REM particular file as subject to the "Classpath" exception as provided
REM by Sun in the LICENSE file that accompanied this code.
REM 
REM This code is distributed in the hope that it will be useful, but WITHOUT
REM ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
REM FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
REM version 2 for more details (a copy is included in the LICENSE file that
REM accompanied this code).
REM 
REM You should have received a copy of the GNU General Public License version
REM 2 along with this work; if not, write to the Free Software Foundation,
REM Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
REM 
REM Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
REM CA 95054 USA or visit www.sun.com if you need additional information or
REM have any questions.
REM


if not "%OS%"=="Windows_NT" goto win9xStart
:winNTStart
@setlocal

rem %~dp0 is name of current script under NT
set DEFAULT_JAVAHELP_HOME=%~dp0

rem : operator works similar to make : operator
set DEFAULT_JAVAHELP_HOME=%DEFAULT_JAVAHELP_HOME:\javahelp\bin\=%

if %JAVAHELP_HOME%a==a set JAVAHELP_HOME=%DEFAULT_JAVAHELP_HOME%
set DEFAULT_JAVAHELP_HOME=
goto doneStart

:win9xStart
:doneStart
rem This label provides a place for NT handling to skip to.

rem find JAVAHELP_HOME
if not "%JAVAHELP_HOME%"=="" goto runjhsearch

rem check for JavaHelp in Program Files on system drive
if not exist "%SystemDrive%\Program Files\jh" goto checkSystemDrive
set JAVAHELP_HOME=%SystemDrive%\Program Files\jh
goto checkJava

:checkSystemDrive
rem check for JavaHelp in root directory of system drive
if not exist "%SystemDrive%\jh" goto noJavaHelpHome
set JAVA_HOME=%SystemDrive%\jh
goto runjhsearch

:noJavaHelpHome
echo JAVAHELP_HOME is not set and JavaHelp could not be located. Please set JAVAHELP_HOME.
goto end

:runjhsearch
java -jar %JAVAHELP_HOME%\javahelp\bin\jhsearch.jar %1

if not "%OS%"=="Windows_NT" goto mainEnd
:winNTend
@endlocal

:mainEnd



