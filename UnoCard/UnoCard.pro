################################################################################
##
## Uno Card Game
## Author: Hikari Toyama
## Compile Environment: Qt 5 with Qt Creator
## COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
##
################################################################################

TEMPLATE = app
CONFIG += console c++11
CONFIG -= app_bundle
DESTDIR = $$PWD
RC_FILE = UnoCard.rc
RC_ICONS = UnoCard.ico
QT += multimedia widgets

HEADERS += \
    include/AI.h \
    include/Card.h \
    include/Color.h \
    include/Content.h \
    include/Player.h \
    include/Sound.h \
    include/Uno.h

SOURCES += \
    src/AI.cpp \
    src/Card.cpp \
    src/Player.cpp \
    src/Sound.cpp \
    src/Uno.cpp \
    src/main.cpp

macx {
    DEPENDPATH += /opt/homebrew/include $$PWD/include
    INCLUDEPATH += /opt/homebrew/include $$PWD/include
    LIBS += \
        -L/opt/homebrew/lib -lopencv_core \
        -L/opt/homebrew/lib -lopencv_highgui \
        -L/opt/homebrew/lib -lopencv_imgproc \
        -L/opt/homebrew/lib -lopencv_imgcodecs
} # end of macx

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