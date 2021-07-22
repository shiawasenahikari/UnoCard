################################################################################
##
## Uno Card Game
## Author: Hikari Toyama
## Compile Environment: Visual Studio 2015, Windows 10 x64
## COPYRIGHT HIKARI TOYAMA, 1992-2021. ALL RIGHTS RESERVED.
##
################################################################################

TEMPLATE = app
CONFIG += console c++11
CONFIG -= app_bundle
CONFIG -= qt
DESTDIR = $$PWD
RC_FILE = UnoCard.rc
RC_ICONS = UnoCard.ico

HEADERS += \
    include/AI.h \
    include/Card.h \
    include/Color.h \
    include/Content.h \
    include/Player.h \
    include/Uno.h

SOURCES += \
    src/AI.cpp \
    src/Card.cpp \
    src/Player.cpp \
    src/Uno.cpp \
    src/main.cpp

unix:!macx {
    DEPENDPATH += /usr/local/include $$PWD/include
    INCLUDEPATH += /usr/local/include $$PWD/include
    LIBS += -lopencv_core -lopencv_highgui -lopencv_imgproc -lopencv_imgcodecs
} # end of unix:!macx

win32-msvc2015 {
    DEPENDPATH += $$PWD/include
    INCLUDEPATH += $$PWD/include
    LIBS += $$PWD/lib/opencv_world411.lib
} # end of win32-msvc2015

# E.O.F