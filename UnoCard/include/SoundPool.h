////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __SOUND_POOL_H_494649FDFA62B3C015120BCB9BE17613__
#define __SOUND_POOL_H_494649FDFA62B3C015120BCB9BE17613__

#include <QObject>
#include <QThread>
#include <QSoundEffect>

/**
 * SoundPool Object. Play sounds in sub thread to get low latencies.
 */
class SoundPool : public QObject {
    Q_OBJECT

private:
    QThread thread;
    QSoundEffect sndUno;
    QSoundEffect sndWin;
    QSoundEffect sndLose;
    QSoundEffect sndDraw;
    QSoundEffect sndPlay;

public:
    static const int SND_UNO = 0;
    static const int SND_WIN = 1;
    static const int SND_LOSE = 2;
    static const int SND_DRAW = 3;
    static const int SND_PLAY = 4;
    SoundPool(QObject* parent = nullptr);
    ~SoundPool();

public slots:
    void play(int which);

signals:
    void playSndUno();
    void playSndWin();
    void playSndLose();
    void playSndDraw();
    void playSndPlay();
}; // SoundPool Class

#endif // __SOUND_POOL_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F