#!/bin/sh


BUILD_DIR=$PWD/build
PRO_DIR=$PWD


mkdir -p $BUILD_DIR
cd build
qmake $PRO_DIR/UnoCard.pro "CONFIG+=debug" "CONFIG+=console"
make