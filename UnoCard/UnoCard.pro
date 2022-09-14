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
    include/i18n.h \
    main.h

SOURCES += \
    main.cpp

FORMS += \
    main.ui

msvc {
    QMAKE_CFLAGS += /utf-8
    QMAKE_CXXFLAGS += /utf-8
} # msvc

# E.O.F