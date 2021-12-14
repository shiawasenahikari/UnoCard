################################################################################
##
## Uno Card Game 4 PC
## Author: Hikari Toyama
## Compile Environment: Qt 5 with Qt Creator
## COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
##
################################################################################

TEMPLATE = app
CONFIG += c++11
CONFIG -= app_bundle
DESTDIR = $$PWD
RC_FILE = UnoCard.rc
RC_ICONS = UnoCard.ico
QT += core gui multimedia widgets

HEADERS += \
    include/AI.h \
    include/Card.h \
    include/Color.h \
    include/Content.h \
    include/Player.h \
    include/SoundPool.h \
    include/Uno.h \
    include/main.h

SOURCES += \
    src/AI.cpp \
    src/Card.cpp \
    src/Player.cpp \
    src/SoundPool.cpp \
    src/Uno.cpp \
    src/main.cpp

FORMS += \
    src/main.ui

# E.O.F