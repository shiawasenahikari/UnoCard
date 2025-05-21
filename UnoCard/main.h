////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __MAIN_H_494649FDFA62B3C015120BCB9BE17613__
#define __MAIN_H_494649FDFA62B3C015120BCB9BE17613__

#include <QImage>
#include <QString>
#include <QWidget>
#include <QPainter>
#include <QCloseEvent>
#include <QMouseEvent>
#include <QPaintEvent>
#include <QMediaPlayer>
#include <QMediaPlaylist>
#include "include/SoundPool.h"
#include "include/Color.h"
#include "include/i18n.h"
#include "include/Uno.h"
#include "include/AI.h"

/**
 * Ui Namespace.
 */
namespace Ui {
    class Main;
} // Ui Namespace

/**
 * AnimateLayer Class.
 */
class AnimateLayer {
public:
    QImage elem;
    int startLeft, startTop, endLeft, endTop;
}; // AnimateLayer Class

/**
 * Game UI.
 */
class Main : public QWidget {
    Q_OBJECT

private:
    // Global Variables
    AI* sAI;
    Uno* sUno;
    I18N* i18n;
    bool sAuto;
    int sStatus;
    int sWinner;
    Ui::Main* ui;
    int sHideFlag;
    int sSpeed = 1;
    QImage sScreen;
    bool sGameSaved;
    int sSelectedIdx;
    int sScore, sDiff;
    QImage sBackup[4];
    QPainter* sPainter;
    bool sAdjustOptions;
    SoundPool* sSoundPool;
    AnimateLayer sLayer[4];
    QPainter* sBkPainter[4];
    QMediaPlayer* sMediaPlay;
    QMediaPlaylist* sMediaList;

    // Functions
    void cycle();
    void onChallenge();
    void swapWith(int whom);
    void setStatus(int status);
    void threadWait(int millis);
    int getTextWidth(const QString& text);
    void play(int index, Color color = NONE);
    void draw(int count = 1, bool force = false);
    void putText(const QString& text, int x, int y);
    void loadReplay(const QString& replayName = "");
    void animate(int layerCount, AnimateLayer layer[]);
    void refreshScreen(const QString& message = "", int area = 0xff);

signals:
    void signal_requestAI();

private slots:
    void requestAI();

protected:
    // Implemented Listeners
    void closeEvent(QCloseEvent* event);
    void paintEvent(QPaintEvent* event);
    void mousePressEvent(QMouseEvent* event);

public:
    // Constructor
    Main(int argc, char* argv[], QWidget* parent = nullptr);
}; // Main Class

#endif // __MAIN_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F