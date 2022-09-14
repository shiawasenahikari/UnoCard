////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __SOUND_POOL_H_494649FDFA62B3C015120BCB9BE17613__
#define __SOUND_POOL_H_494649FDFA62B3C015120BCB9BE17613__

#include <QUrl>
#include <QObject>
#include <QString>
#include <QThread>
#include <QSoundEffect>

/**
 * SoundPool Object. Play sounds in sub thread to get low latencies.
 */
class SoundPool : public QObject {
    Q_OBJECT

private:
    bool enabled;
    QThread thread;
    QSoundEffect* sndUno;
    QSoundEffect* sndWin;
    QSoundEffect* sndLose;
    QSoundEffect* sndDraw;
    QSoundEffect* sndPlay;

public:
    static const int SND_UNO = 0;
    static const int SND_WIN = 1;
    static const int SND_LOSE = 2;
    static const int SND_DRAW = 3;
    static const int SND_PLAY = 4;

    inline SoundPool(QObject* = nullptr) {
        sndUno = new QSoundEffect;
        sndWin = new QSoundEffect;
        sndLose = new QSoundEffect;
        sndDraw = new QSoundEffect;
        sndPlay = new QSoundEffect;
        sndUno->setSource(QUrl::fromLocalFile("resource/snd_uno.wav"));
        sndWin->setSource(QUrl::fromLocalFile("resource/snd_win.wav"));
        sndLose->setSource(QUrl::fromLocalFile("resource/snd_lose.wav"));
        sndDraw->setSource(QUrl::fromLocalFile("resource/snd_draw.wav"));
        sndPlay->setSource(QUrl::fromLocalFile("resource/snd_play.wav"));
        sndUno->moveToThread(&thread);
        sndWin->moveToThread(&thread);
        sndLose->moveToThread(&thread);
        sndDraw->moveToThread(&thread);
        sndPlay->moveToThread(&thread);
        connect(this, SIGNAL(playSndUno()), sndUno, SLOT(play()));
        connect(this, SIGNAL(playSndWin()), sndWin, SLOT(play()));
        connect(this, SIGNAL(playSndLose()), sndLose, SLOT(play()));
        connect(this, SIGNAL(playSndDraw()), sndDraw, SLOT(play()));
        connect(this, SIGNAL(playSndPlay()), sndPlay, SLOT(play()));
        enabled = true;
        thread.start();
    } // SoundPool(QObject*) (Class Constructor)

    inline void setEnabled(bool enabled) {
        this->enabled = enabled;
    } // setEnabled(bool)

    inline bool isEnabled() {
        return enabled;
    } // isEnabled()

signals:
    void playSndUno();
    void playSndWin();
    void playSndLose();
    void playSndDraw();
    void playSndPlay();

public slots:
    inline void play(int which) {
        if (enabled) {
            switch (which) {
            case SND_UNO:
                emit this->playSndUno();
                break; // case SND_UNO

            case SND_WIN:
                emit this->playSndWin();
                break; // case SND_WIN

            case SND_LOSE:
                emit this->playSndLose();
                break; // case SND_LOSE

            case SND_DRAW:
                emit this->playSndDraw();
                break; // case SND_DRAW

            case SND_PLAY:
                emit this->playSndPlay();
                break; // case SND_PLAY

            default:
                break; // default
            } // switch (which)
        } // if (enabled)
    } // play(int)
}; // SoundPool Class

#endif // __SOUND_POOL_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F