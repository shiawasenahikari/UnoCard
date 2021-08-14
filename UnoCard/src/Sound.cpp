////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <Sound.h>

Sound::Sound(const char* fileName, QObject* parent) :
    QObject(parent) {
    snd.setSource(QUrl::fromLocalFile(fileName));
    snd.moveToThread(&thread);
    connect(this, SIGNAL(doPlay()), &snd, SLOT(play()));
    thread.start();
} // Sound(QUrl, QObject*) (Class Constructor)

void Sound::play() {
    emit this->doPlay();
} // play()

Sound::~Sound() {
    if (thread.isRunning()) {
        thread.terminate();
        thread.deleteLater();
        thread.wait();
    } // if (thread.isRunning())
} // ~Sound() (Class Deconstructor)

// E.O.F