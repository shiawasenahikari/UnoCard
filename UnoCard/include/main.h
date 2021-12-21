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

#include <QFont>
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
    int x1, y1, x2, y2;
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
    int sScore;
    int sStatus;
    int sWinner;
    QFont sFont;
    Ui::Main* ui;
    int sHideFlag;
    QImage sScreen;
    bool sAIRunning;
    int sSelectedIdx;
    QPainter* sPainter;
    bool sAdjustOptions;
    SoundPool* sSoundPool;
    QMediaPlayer* sMediaPlay;
    QMediaPlaylist* sMediaList;

    // Functions
    void cycle();
    void easyAI();
    void hardAI();
    void sevenZeroAI();
    void onChallenge();
    void swapWith(int whom);
    void setStatus(int status);
    void threadWait(int millis);
    void play(int index, Color color = NONE);
    void draw(int count = 1, bool force = false);
    void refreshScreen(const QString& message = "");
    void animate(int layerCount, AnimateLayer layer[]);

protected:
    // Implemented Listeners
    void closeEvent(QCloseEvent* event);
    void paintEvent(QPaintEvent* event);
    void mousePressEvent(QMouseEvent* event);

public:
    // Constructor & Destructor
    Main(int argc, char* argv[], QWidget* parent = nullptr);
    ~Main();
}; // Main Class

#endif // __MAIN_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F