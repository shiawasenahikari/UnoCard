@echo off
title qmake and nmake build prompt
set MINGW_PATH=c:\Qt\Qt5.12.12\Tools\mingw730_64
set QT_DIR=c:\Qt\Qt5.12.12\5.12.12\mingw73_64
set BUILD_DIR=%cd%\build
set PRO_DIR=%cd%
set PATH=%MINGW_PATH%\bin;%QT_DIR%\bin;%PATH%


if not exist %BUILD_DIR% (
    md %BUILD_DIR%
)

cd build
qmake.exe %PRO_DIR%\UnoCard.pro -spec win32-g++ "CONFIG+=release"
if exist %BUILD_DIR%\release\UnoCard.exe del %BUILD_DIR%\release\UnoCard.exe
@REM c:\Qt\Qt5.12.12\Tools\QtCreator\bin\jom.exe -j4
%MINGW_PATH%\bin\mingw32-make -f Makefile.Release