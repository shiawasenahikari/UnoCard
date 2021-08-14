////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __SOUND_H_494649FDFA62B3C015120BCB9BE17613__
#define __SOUND_H_494649FDFA62B3C015120BCB9BE17613__

#include <QObject>
#include <QThread>
#include <QSoundEffect>

/**
 * Sound Object. Play sounds in sub thread to get low latencies.
 */
class Sound : public QObject {
    Q_OBJECT

private:
    QThread thread;
    QSoundEffect snd;

public:
    Sound(const char* fileName, QObject* parent = nullptr);
    ~Sound();

public slots:
    void play();

signals:
    void doPlay();
}; // Sound Class

#endif // __SOUND_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F