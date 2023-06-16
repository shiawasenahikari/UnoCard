////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <QUrl>
#include <QFile>
#include <QIcon>
#include <QRect>
#include <QBrush>
#include <QColor>
#include <QImage>
#include <QTimer>
#include <cstdlib>
#include <cstring>
#include <QString>
#include <QWidget>
#include <QPainter>
#include <QFileInfo>
#include <QIODevice>
#include <QEventLoop>
#include <QCloseEvent>
#include <QMouseEvent>
#include <QPaintEvent>
#include <QApplication>
#include <QMediaPlayer>
#include <QMediaPlaylist>
#include "include/SoundPool.h"
#include "include/Content.h"
#include "include/Player.h"
#include "include/Color.h"
#include "include/i18n.h"
#include "include/Card.h"
#include "include/Uno.h"
#include "include/AI.h"
#include "ui_main.h"
#include "main.h"

// Constants
static const int STAT_IDLE = 0x1111;
static const int STAT_WELCOME = 0x2222;
static const int STAT_NEW_GAME = 0x3333;
static const int STAT_GAME_OVER = 0x4444;
static const int STAT_WILD_COLOR = 0x5555;
static const int STAT_DOUBT_WILD4 = 0x6666;
static const int STAT_SEVEN_TARGET = 0x7777;
static const QBrush BRUSH_RED(QColor(0xFF, 0x55, 0x55));
static const QBrush BRUSH_BLUE(QColor(0x55, 0x55, 0xFF));
static const QBrush BRUSH_GREEN(QColor(0x55, 0xAA, 0x55));
static const QBrush BRUSH_YELLOW(QColor(0xFF, 0xAA, 0x11));
static const char FILE_HEADER[] = {
    (char)('U' + 'N'),
    (char)('O' + '@'),
    (char)('H' + 'i'),
    (char)('k' + 'a'),
    (char)('r' + 'i'),
    (char)('T' + 'o'),
    (char)('y' + 'a'),
    (char)('m' + 'a'), 0x00
}; // FILE_HEADER[]

/**
 * Triggered when application starts.
 */
Main::Main(int argc, char* argv[], QWidget* parent) : QWidget(parent) {
    int i, hash;
    QString bgmPath;
    int dw[64] = {0};
    char header[9] = {0};
    QFile reader("UnoCard.stat");

    // Preparations
    if (strstr(argv[0], "zh") != nullptr) {
        i18n = new I18N_zh_CN;
    } // if (strstr(argv[0], "zh") != nullptr)
    else {
        i18n = new I18N_en_US;
    } // else

    if (argc > 1) {
        unsigned seed = unsigned(atoi(argv[1]));
        sUno = Uno::getInstance(seed);
    } // if (argc > 1)
    else {
        sUno = Uno::getInstance();
    } // else

    sScore = 0;
    sAI = AI::getInstance();
    sSoundPool = new SoundPool;
    sMediaPlay = new QMediaPlayer;
    sMediaList = new QMediaPlaylist;
    bgmPath = QFileInfo("resource/bgm.mp3").absoluteFilePath();
    sMediaList->addMedia(QUrl::fromLocalFile(bgmPath));
    sMediaList->setPlaybackMode(QMediaPlaylist::Loop);
    sMediaPlay->setPlaylist(sMediaList);
    sMediaPlay->setVolume(50);
    if (reader.open(QIODevice::ReadOnly)) {
        // Using statistics data in UnoCard.stat file.
        i = int(reader.size());
        if (i == 8 + 9 * sizeof(int)) {
            // Old version
            reader.read(header, 8);
            reader.read((char*)dw, 9 * sizeof(int));
            for (hash = 0, i = 0; i < 8; ++i) {
                hash = 31 * hash + dw[i];
            } // for (hash = 0, i = 0; i < 8; ++i)

            if (strcmp(header, FILE_HEADER) == 0 && hash == dw[8]) {
                // File verification success
                if (dw[0] > 9999) dw[0] = 9999;
                else if (dw[0] < -999) dw[0] = -999;
                sScore = dw[0];
                sUno->setPlayers(dw[1]);
                sUno->setDifficulty(dw[2]);
                sUno->setForcePlay(dw[3] != 0);
                sUno->setSevenZeroRule(dw[4] != 0);
                sUno->setDraw2StackRule(dw[5] != 0);
                sSoundPool->setEnabled(dw[6] != 0);
                sMediaPlay->setVolume(dw[7]);
            } // if (strcmp(header, FILE_HEADER) == 0 && hash == dw[8])
        } // if (i == 8 + 9 * sizeof(int))
        else if (i == 8 + 64 * sizeof(int)) {
            // New version
            reader.read(header, 8);
            reader.read((char*)dw, 64 * sizeof(int));
            for (hash = 0, i = 0; i < 63; ++i) {
                hash = 31 * hash + dw[i];
            } // for (hash = 0, i = 0; i < 63; ++i)

            if (strcmp(header, FILE_HEADER) == 0 && hash == dw[63]) {
                // File verification success
                if (dw[0] > 9999) dw[0] = 9999;
                else if (dw[0] < -999) dw[0] = -999;
                sScore = dw[0];
                sUno->setPlayers(dw[1]);
                sUno->setDifficulty(dw[2]);
                sUno->setForcePlay(dw[3] != 0);
                sUno->setSevenZeroRule(dw[4] != 0);
                sUno->setDraw2StackRule(dw[5] != 0);
                sSoundPool->setEnabled(dw[6] != 0);
                sMediaPlay->setVolume(dw[7]);
                sUno->set2vs2(dw[9] != 0);
                if (5 <= dw[8] && dw[8] <= 20) {
                    while (sUno->getInitialCards() < dw[8]) {
                        sUno->increaseInitialCards();
                    } // while (sUno->getInitialCards() < dw[8])

                    while (sUno->getInitialCards() > dw[8]) {
                        sUno->decreaseInitialCards();
                    } // while (sUno->getInitialCards() > dw[8])
                } // if (5 <= dw[8] && dw[8] <= 20)
            } // if (strcmp(header, FILE_HEADER) == 0 && hash == dw[63])
        } // else if (i == 8 + 64 * sizeof(int))

        reader.close();
    } // if (reader.open(QIODevice::ReadOnly))

    sAuto = false;
    sHideFlag = 0x00;
    sSelectedIdx = -1;
    sAIRunning = false;
    sWinner = Player::YOU;
    sAdjustOptions = false;
    for (i = 0; i <= 3; ++i) {
        sBackup[i] = QImage(1600, 900, QImage::Format_RGB888);
        sBkPainter[i] = new QPainter(&sBackup[i]);
    } // for (i = 0; i <= 3; ++i)

    sScreen = QImage(1600, 900, QImage::Format_RGB888);
    sPainter = new QPainter(&sScreen);
    sPainter->setPen(Qt::NoPen);
    ui = new Ui::Main;
    ui->setupUi(this);
    sMediaPlay->play();
    setStatus(STAT_WELCOME);
} // Main(int, char*[], QWidget*) (Class Constructor)

/**
 * The unique AI entry point.
 */
void Main::requestAI() {
    int idxBest;
    Color bestColor[1];

    if (!sAIRunning) {
        sAIRunning = true;
        while (sStatus == Player::COM1
            || sStatus == Player::COM2
            || sStatus == Player::COM3
            || (sStatus == Player::YOU && sAuto)) {
            setStatus(STAT_IDLE); // block mouse click events when idle
            idxBest = sUno->getDifficulty() == Uno::LV_EASY
                ? sAI->easyAI_bestCardIndex4NowPlayer(bestColor)
                : sUno->isSevenZeroRule()
                ? sAI->sevenZeroAI_bestCardIndex4NowPlayer(bestColor)
                : sUno->is2vs2()
                ? sAI->teamAI_bestCardIndex4NowPlayer(bestColor)
                : sAI->hardAI_bestCardIndex4NowPlayer(bestColor);
            if (idxBest >= 0) {
                // Found an appropriate card to play
                play(idxBest, bestColor[0]);
            } // if (idxBest >= 0)
            else {
                // No appropriate cards to play, or no card to play
                draw();
            } // else
        } // while (sStatus == Player::COM1 || ...)

        sAIRunning = false;
    } // if (!sAIRunning)
} // requestAI()

/**
 * Let our UI wait the number of specified milli seconds.
 *
 * @param millis How many milli seconds to wait.
 */
void Main::threadWait(int millis) {
    static QEventLoop loop;

    QTimer::singleShot(millis, &loop, SLOT(quit()));
    loop.exec();
} // threadWait(int)

/**
 * Change the value of global variable [sStatus]
 * and do the following operations when necessary.
 *
 * @param status New status value.
 */
void Main::setStatus(int status) {
    switch (sStatus = status) {
    case STAT_WELCOME:
        if (sAdjustOptions) {
            refreshScreen(i18n->info_ruleSettings());
        } // if (sAdjustOptions)
        else {
            refreshScreen(i18n->info_welcome());
        } // else
        break; // case STAT_WELCOME

    case STAT_NEW_GAME:
        // New game
        sStatus = STAT_IDLE; // block mouse click events when idle

        // You will lose 200 points if you quit during the game
        sScore -= 200;
        sUno->start();
        sSelectedIdx = -1;
        refreshScreen(i18n->info_ready());
        threadWait(2000);
        switch (sUno->getRecentInfo()[3].card->content) {
        case DRAW2:
            // If starting with a [+2], let dealer draw 2 cards.
            draw(2, /* force */ true);
            break; // case DRAW2

        case SKIP:
            // If starting with a [skip], skip dealer's turn.
            refreshScreen(i18n->info_skipped(sUno->getNow()));
            threadWait(1500);
            setStatus(sUno->switchNow());
            break; // case SKIP

        case REV:
            // If starting with a [reverse], change the action
            // sequence to COUNTER CLOCKWISE.
            sUno->switchDirection();
            refreshScreen(i18n->info_dirChanged());
            threadWait(1500);
            setStatus(sUno->getNow());
            break; // case REV

        default:
            // Otherwise, go to dealer's turn.
            setStatus(sUno->getNow());
            break; // default
        } // switch (sUno->getRecentInfo()[3].card->content)
        break; // case STAT_NEW_GAME

    case Player::YOU:
        // Your turn, select a hand card to play, or draw a card
        if (sAuto) {
            requestAI();
        } // if (sAuto)
        else if (sUno->legalCardsCount4NowPlayer() == 0) {
            draw();
        } // else if (sUno->legalCardsCount4NowPlayer() == 0)
        else if (sAdjustOptions) {
            refreshScreen();
        } // else if (sAdjustOptions)
        else {
            auto hand = sUno->getPlayer(Player::YOU)->getHandCards();
            if (hand.size() == 1) {
                play(0);
            } // if (hand.size() == 1)
            else if (sSelectedIdx < 0) {
                int c = sUno->getDraw2StackCount();
                refreshScreen(c == 0
                    ? i18n->info_yourTurn()
                    : i18n->info_yourTurn_stackDraw2(c));
            } // else if (sSelectedIdx < 0)
            else {
                Card* card = hand[sSelectedIdx];
                refreshScreen(sUno->isLegalToPlay(card)
                    ? i18n->info_clickAgainToPlay(card->name)
                    : i18n->info_cannotPlay(card->name));
            } // else
        } // else
        break; // case Player::YOU

    case STAT_WILD_COLOR:
        // Need to specify the following legal color after played a
        // wild card. Draw color sectors in the center of screen
        refreshScreen(i18n->ask_color());
        break; // case STAT_WILD_COLOR

    case STAT_DOUBT_WILD4:
        if (sAuto || sUno->getNext() != Player::YOU) {
            // Challenge or not is decided by AI
            if (sAI->needToChallenge()) {
                onChallenge();
            } // if (sAI->needToChallenge())
            else {
                sUno->switchNow();
                draw(4, /* force */ true);
            } // else
        } // if (sAuto || sUno->getNext() != Player::YOU)
        else {
            // Challenge or not is decided by you
            refreshScreen(i18n->ask_challenge(sUno->next2lastColor()));
        } // else
        break; // case STAT_DOUBT_WILD4

    case STAT_SEVEN_TARGET:
        // In 7-0 rule, when someone put down a seven card, the player
        // must swap hand cards with another player immediately.
        if (sAuto || sUno->getNow() != Player::YOU) {
            // Seven-card is played by AI. Select target automatically.
            swapWith(sAI->calcBestSwapTarget4NowPlayer());
        } // if (sAuto || sUno->getNow() != Player::YOU)
        else {
            // Seven-card is played by you. Select target manually.
            refreshScreen(i18n->ask_target());
        } // else
        break; // case STAT_SEVEN_TARGET

    case Player::COM1:
    case Player::COM2:
    case Player::COM3:
        // AI players' turn
        requestAI();
        break; // case Player::COM1, Player::COM2, Player::COM3

    case STAT_GAME_OVER:
        // Game over
        if (sAdjustOptions) {
            refreshScreen(i18n->info_ruleSettings());
        } // if (sAdjustOptions)
        else {
            refreshScreen(i18n->info_gameOver(sScore, sDiff));
        } // else
        break; // case STAT_GAME_OVER

    default:
        break; // default
    } // switch (sStatus = status)
} // setStatus(int)

/**
 * Refresh the screen display. The content of global variable [sScreen]
 * will be changed after calling this function.
 *
 * @param message Extra message to show.
 */
void Main::refreshScreen(const QString& message) {
    static bool active;
    static QImage image;
    static QString info;
    static const RecentInfo* recent;
    static int i, x, remain, size, status, used, width;

    // Lock the value of global variable [sStatus]
    status = sStatus;

    // Clear
    sPainter->drawImage(0, 0, sUno->getBackground());

    // Message area
    width = sUno->getTextWidth(message);
    sUno->putText(sPainter, message, 800 - width / 2, 620);

    // Left-bottom corner: <OPTIONS> button
    // Shows only when game is not in process
    if (status == Player::YOU ||
        status == STAT_WELCOME ||
        status == STAT_GAME_OVER) {
        active = sAdjustOptions;
        sUno->putText(sPainter, i18n->btn_settings(active), 20, 880);
    } // if (status == Player::YOU || ...)

    // Right-bottom corner: <AUTO> button
    if (status == Player::YOU && !sAuto && !sAdjustOptions) {
        width = sUno->getTextWidth(i18n->btn_auto());
        sUno->putText(sPainter, i18n->btn_auto(), 1580 - width, 880);
    } // if (status == Player::YOU && !sAuto && !sAdjustOptions)

    if (sAdjustOptions) {
        // Show special screen when configuring game options
        // BGM switch
        sUno->putText(sPainter, i18n->label_bgm(), 60, 160);
        image = sMediaPlay->volume() > 0 ?
            sUno->findCard(RED, SKIP)->darkImg :
            sUno->findCard(RED, SKIP)->image;
        sPainter->drawImage(150, 60, image);
        image = sMediaPlay->volume() > 0 ?
            sUno->findCard(GREEN, REV)->image :
            sUno->findCard(GREEN, REV)->darkImg;
        sPainter->drawImage(330, 60, image);

        // Sound effect switch
        sUno->putText(sPainter, i18n->label_snd(), 60, 350);
        image = sSoundPool->isEnabled() ?
            sUno->findCard(RED, SKIP)->darkImg :
            sUno->findCard(RED, SKIP)->image;
        sPainter->drawImage(150, 250, image);
        image = sSoundPool->isEnabled() ?
            sUno->findCard(GREEN, REV)->image :
            sUno->findCard(GREEN, REV)->darkImg;
        sPainter->drawImage(330, 250, image);

        if (status != Player::YOU) {
            // [Level] option: easy / hard
            sUno->putText(sPainter, i18n->label_level(), 780, 160);
            image = sUno->getLevelImage(
                /* level   */ Uno::LV_EASY,
                /* hiLight */ sUno->getDifficulty() == Uno::LV_EASY
            ); // image = sUno->getLevelImage()
            sPainter->drawImage(930, 60, image);
            image = sUno->getLevelImage(
                /* level   */ Uno::LV_HARD,
                /* hiLight */ sUno->getDifficulty() == Uno::LV_HARD
            ); // image = sUno->getLevelImage()
            sPainter->drawImage(1110, 60, image);

            // [Players] option: 3 / 4 / 2vs2
            sUno->putText(sPainter, i18n->label_players(), 780, 350);
            image = sUno->getPlayers() == 3 ?
                sUno->findCard(GREEN, NUM3)->image :
                sUno->findCard(GREEN, NUM3)->darkImg;
            sPainter->drawImage(930, 250, image);
            image = sUno->getPlayers() == 4 && !sUno->is2vs2() ?
                sUno->findCard(YELLOW, NUM4)->image :
                sUno->findCard(YELLOW, NUM4)->darkImg;
            sPainter->drawImage(1110, 250, image);
            image = sUno->get2vs2Image();
            sPainter->drawImage(1290, 250, image);

            // Rule settings
            // Initial cards
            sUno->putText(sPainter, i18n->label_initialCards(), 60, 670);
            width = sUno->getInitialCards();
            info = QString::number(width / 10) + QString::number(width % 10);
            sUno->putText(sPainter, "<-", 1110, 670);
            sUno->putText(sPainter, info, 1234, 670);
            sUno->putText(sPainter, "+>", 1358, 670);

            // Force play switch
            active = sUno->isForcePlay();
            sUno->putText(sPainter, i18n->label_forcePlay(), 60, 720);
            sUno->putText(sPainter, i18n->btn_keep(!active), 1110, 720);
            sUno->putText(sPainter, i18n->btn_play(active), 1290, 720);

            // 7-0
            active = sUno->isSevenZeroRule();
            sUno->putText(sPainter, i18n->label_7_0(), 60, 770);
            sUno->putText(sPainter, i18n->btn_off(!active), 1110, 770);
            sUno->putText(sPainter, i18n->btn_on(active), 1290, 770);

            // +2 stack
            active = sUno->isDraw2StackRule();
            sUno->putText(sPainter, i18n->label_draw2Stack(), 60, 820);
            sUno->putText(sPainter, i18n->btn_off(!active), 1110, 820);
            sUno->putText(sPainter, i18n->btn_on(active), 1290, 820);
        } // if (status != Player::YOU)
    } // if (sAdjustOptions)
    else if (status == STAT_WELCOME) {
        // For welcome screen, show the start button and your score
        image = sUno->getBackImage();
        sPainter->drawImage(740, 360, image);
        width = sUno->getTextWidth(i18n->label_score());
        sUno->putText(sPainter, i18n->label_score(), 500 - width, 800);
        if (sScore < 0) {
            image = sUno->getColoredWildImage(NONE);
        } // if (sScore < 0)
        else {
            i = sScore / 1000;
            image = sUno->findCard(RED, Content(i))->image;
        } // else

        sPainter->drawImage(520, 700, image);
        i = abs(sScore / 100 % 10);
        image = sUno->findCard(BLUE, Content(i))->image;
        sPainter->drawImage(660, 700, image);
        i = abs(sScore / 10 % 10);
        image = sUno->findCard(GREEN, Content(i))->image;
        sPainter->drawImage(800, 700, image);
        i = abs(sScore % 10);
        image = sUno->findCard(YELLOW, Content(i))->image;
        sPainter->drawImage(940, 700, image);
    } // else if (status == STAT_WELCOME)
    else {
        // Center: card deck & recent played card
        recent = sUno->getRecentInfo();
        image = sUno->getBackImage();
        sPainter->drawImage(338, 360, image);
        for (i = 0, x = 986; i < 4; ++i, x += 44) {
            if (recent[i].card == nullptr) {
                continue;
            } // if (recent[i].card == nullptr)
            else if (recent[i].card->content == WILD) {
                image = sUno->getColoredWildImage(recent[i].color);
            } // else if (recent[i].card->content == WILD)
            else if (recent[i].card->content == WILD_DRAW4) {
                image = sUno->getColoredWildDraw4Image(recent[i].color);
            } // else if (recent[i].card->content == WILD_DRAW4)
            else {
                image = recent[i].card->image;
            } // else

            sPainter->drawImage(x, 360, image);
        } // for (i = 0, x = 986; i < 4; ++i, x += 44)

        // Left-top corner: remain / used
        remain = sUno->getDeckCount();
        used = sUno->getUsedCount();
        info = i18n->label_remain_used(remain, used);
        sUno->putText(sPainter, info, 20, 42);

        // Right-top corner: lacks
        if (sUno->getPlayer(Player::COM2)->getWeakColor() == RED)
            info = "LACK: [R]N";
        else if (sUno->getPlayer(Player::COM2)->getWeakColor() == BLUE)
            info = "LACK: [B]N";
        else if (sUno->getPlayer(Player::COM2)->getWeakColor() == GREEN)
            info = "LACK: [G]N";
        else if (sUno->getPlayer(Player::COM2)->getWeakColor() == YELLOW)
            info = "LACK: [Y]N";
        else
            info = "LACK: N";
        if (sUno->getPlayer(Player::COM3)->getWeakColor() == RED)
            info += "[R]E";
        else if (sUno->getPlayer(Player::COM3)->getWeakColor() == BLUE)
            info += "[B]E";
        else if (sUno->getPlayer(Player::COM3)->getWeakColor() == GREEN)
            info += "[G]E";
        else if (sUno->getPlayer(Player::COM3)->getWeakColor() == YELLOW)
            info += "[Y]E";
        else
            info += "[W]E";
        if (sUno->getPlayer(Player::COM1)->getWeakColor() == RED)
            info += "[R]W";
        else if (sUno->getPlayer(Player::COM1)->getWeakColor() == BLUE)
            info += "[B]W";
        else if (sUno->getPlayer(Player::COM1)->getWeakColor() == GREEN)
            info += "[G]W";
        else if (sUno->getPlayer(Player::COM1)->getWeakColor() == YELLOW)
            info += "[Y]W";
        else
            info += "[W]W";
        if (sUno->getPlayer(Player::YOU)->getWeakColor() == RED)
            info += "[R]S";
        else if (sUno->getPlayer(Player::YOU)->getWeakColor() == BLUE)
            info += "[B]S";
        else if (sUno->getPlayer(Player::YOU)->getWeakColor() == GREEN)
            info += "[G]S";
        else if (sUno->getPlayer(Player::YOU)->getWeakColor() == YELLOW)
            info += "[Y]S";
        else
            info += "[W]S";
        width = sUno->getTextWidth(info);
        sUno->putText(sPainter, info, 1580 - width, 42);

        // Left-center: Hand cards of Player West (COM1)
        if (status == STAT_GAME_OVER && sWinner == Player::COM1) {
            // Played all hand cards, it's winner
            width = sUno->getTextWidth("WIN");
            sUno->putText(sPainter, "[G]WIN", 80 - width / 2, 461);
        } // if (status == STAT_GAME_OVER && sWinner == Player::COM1)
        else if (((sHideFlag >> 1) & 0x01) == 0x00) {
            Player* p = sUno->getPlayer(Player::COM1);
            auto hand = p->getHandCards();
            size = int(hand.size());
            width = 44 * qMin(size, 13) + 136;
            for (i = 0; i < size; ++i) {
                image = p->isOpen(i) ? hand[i]->image : sUno->getBackImage();
                sPainter->drawImage(
                    /* x     */ 20 + i / 13 * 44,
                    /* y     */ 450 - width / 2 + i % 13 * 44,
                    /* image */ image
                ); // drawImage(int, int, QImage&)
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = sUno->getTextWidth("UNO");
                sUno->putText(sPainter, "[Y]UNO", 80 - width / 2, 584);
            } // if (size == 1)
        } // else if (((sHideFlag >> 1) & 0x01) == 0x00)

        // Top-center: Hand cards of Player North (COM2)
        if (status == STAT_GAME_OVER && sWinner == Player::COM2) {
            // Played all hand cards, it's winner
            width = sUno->getTextWidth("WIN");
            sUno->putText(sPainter, "[G]WIN", 800 - width / 2, 121);
        } // if (status == STAT_GAME_OVER && sWinner == Player::COM2)
        else if (((sHideFlag >> 2) & 0x01) == 0x00) {
            Player* p = sUno->getPlayer(Player::COM2);
            auto hand = p->getHandCards();
            size = int(hand.size());
            width = 44 * size + 76;
            for (i = 0; i < size; ++i) {
                image = p->isOpen(i) ? hand[i]->image : sUno->getBackImage();
                sPainter->drawImage(800 - width / 2 + 44 * i, 20, image);
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = sUno->getTextWidth("UNO");
                sUno->putText(sPainter, "[Y]UNO", 720 - width, 121);
            } // if (size == 1)
        } // else if (((sHideFlag >> 2) & 0x01) == 0x00)

        // Right-center: Hand cards of Player East (COM3)
        if (status == STAT_GAME_OVER && sWinner == Player::COM3) {
            // Played all hand cards, it's winner
            width = sUno->getTextWidth("WIN");
            sUno->putText(sPainter, "[G]WIN", 1520 - width / 2, 461);
        } // if (status == STAT_GAME_OVER && sWinner == Player::COM3)
        else if (((sHideFlag >> 3) & 0x01) == 0x00) {
            Player* p = sUno->getPlayer(Player::COM3);
            auto hand = p->getHandCards();
            size = int(hand.size());
            width = 44 * qMin(size, 13) + 136;
            for (i = 13; i < size; ++i) {
                image = p->isOpen(i) ? hand[i]->image : sUno->getBackImage();
                sPainter->drawImage(
                    /* x     */ 1416,
                    /* y     */ 450 - width / 2 + (i - 13) * 44,
                    /* image */ image
                ); // drawImage(int, int, QImage&)
            } // for (i = 13; i < size; ++i)

            for (i = 0; i < 13 && i < size; ++i) {
                image = p->isOpen(i) ? hand[i]->image : sUno->getBackImage();
                sPainter->drawImage(
                    /* x     */ 1460,
                    /* y     */ 450 - width / 2 + i * 44,
                    /* image */ image
                ); // drawImage(int, int, QImage&)
            } // for (i = 0; i < 13 && i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = sUno->getTextWidth("UNO");
                sUno->putText(sPainter, "[Y]UNO", 1520 - width / 2, 584);
            } // if (size == 1)
        } // else if (((sHideFlag >> 3) & 0x01) == 0x00)

        // Bottom: Your hand cards
        if (status == STAT_GAME_OVER && sWinner == Player::YOU) {
            // Played all hand cards, it's winner
            width = sUno->getTextWidth("WIN");
            sUno->putText(sPainter, "[G]WIN", 800 - width / 2, 801);
        } // if (status == STAT_GAME_OVER && sWinner == Player::YOU)
        else if ((sHideFlag & 0x01) == 0x00) {
            // Show your all hand cards
            auto hand = sUno->getPlayer(Player::YOU)->getHandCards();
            size = int(hand.size());
            width = 44 * size + 76;
            for (i = 0; i < size; ++i) {
                Card* card = hand[i];
                image = status == STAT_GAME_OVER
                    || (status == Player::YOU
                        && sUno->isLegalToPlay(card))
                    ? card->image : card->darkImg;
                sPainter->drawImage(
                    /* x     */ 800 - width / 2 + 44 * i,
                    /* y     */ i == sSelectedIdx ? 680 : 700,
                    /* image */ image
                ); // drawImage(int, int, QImage&)
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                sUno->putText(sPainter, "[Y]UNO", 880, 801);
            } // if (size == 1)
        } // else if ((sHideFlag & 0x01) == 0x00)

        // Extra sectors in special status
        switch (status) {
        case STAT_WILD_COLOR:
            // Need to specify the following legal color after played a
            // wild card. Draw color sectors in the center of screen
            // Draw blue sector
            sPainter->setBrush(BRUSH_BLUE);
            sPainter->drawPie(270, 270, 271, 271, 0, 90 * 16);

            // Draw green sector
            sPainter->setBrush(BRUSH_GREEN);
            sPainter->drawPie(270, 270, 271, 271, 0, -90 * 16);

            // Draw red sector
            sPainter->setBrush(BRUSH_RED);
            sPainter->drawPie(270, 270, 271, 271, 180 * 16, -90 * 16);

            // Draw yellow sector
            sPainter->setBrush(BRUSH_YELLOW);
            sPainter->drawPie(270, 270, 271, 271, 180 * 16, 90 * 16);
            break; // case STAT_WILD_COLOR

        case STAT_DOUBT_WILD4:
            // Ask whether you want to challenge your previous player
            // Draw YES button
            sPainter->setBrush(BRUSH_GREEN);
            sPainter->drawPie(270, 270, 271, 271, 0, 180 * 16);
            width = sUno->getTextWidth(i18n->label_yes());
            sUno->putText(sPainter, i18n->label_yes(), 405 - width / 2, 358);

            // Draw NO button
            sPainter->setBrush(BRUSH_RED);
            sPainter->drawPie(270, 270, 271, 271, 0, -180 * 16);
            width = sUno->getTextWidth(i18n->label_no());
            sUno->putText(sPainter, i18n->label_no(), 405 - width / 2, 472);
            break; // case STAT_DOUBT_WILD4

        case STAT_SEVEN_TARGET:
            // Ask the target you want to swap hand cards with
            // Draw west sector (red)
            sPainter->setBrush(BRUSH_RED);
            sPainter->drawPie(270, 270, 271, 271, -90 * 16, -120 * 16);
            width = sUno->getTextWidth("W");
            sUno->putText(sPainter, "W", 338 - width / 2, 440);

            // Draw east sector (green)
            sPainter->setBrush(BRUSH_GREEN);
            sPainter->drawPie(270, 270, 271, 271, -90 * 16, 120 * 16);
            width = sUno->getTextWidth("E");
            sUno->putText(sPainter, "E", 472 - width / 2, 440);

            // Draw north sector (yellow)
            sPainter->setBrush(BRUSH_YELLOW);
            sPainter->drawPie(270, 270, 271, 271, 150 * 16, -120 * 16);
            width = sUno->getTextWidth("N");
            sUno->putText(sPainter, "N", 405 - width / 2, 360);
            break; // case STAT_SEVEN_TARGET

        default:
            break; // default
        } // switch (status)
    } // else

    // Show screen
    update();
} // refreshScreen(const QString&)

/**
 * Draw [sScreen] on the window. Called by system.
 */
void Main::paintEvent(QPaintEvent*) {
    static QPainter painter;
    static QRect roi(0, 0, 0, 0);

    roi.setWidth(this->width());
    roi.setHeight(this->height());
    painter.begin(this);
    painter.drawImage(roi, sScreen);
    painter.end();
} // paintEvent(QPaintEvent*)

/**
 * In 7-0 rule, when a zero card is put down, everyone need to pass
 * the hand cards to the next player.
 */
void Main::cycle() {
    static int x[] = { 740, 160, 740, 1320 };
    static int y[] = { 670, 360, 50, 360 };
    int curr, next, oppo, prev;

    setStatus(STAT_IDLE);
    sHideFlag = 0x0f;
    refreshScreen(i18n->info_0_rotate());
    curr = sUno->getNow();
    next = sUno->getNext();
    oppo = sUno->getOppo();
    prev = sUno->getPrev();
    sLayer[0].elem = sUno->getBackImage();
    sLayer[0].startLeft = x[curr];
    sLayer[0].startTop = y[curr];
    sLayer[0].endLeft = x[next];
    sLayer[0].endTop = y[next];
    sLayer[1].elem = sUno->getBackImage();
    sLayer[1].startLeft = x[next];
    sLayer[1].startTop = y[next];
    sLayer[1].endLeft = x[oppo];
    sLayer[1].endTop = y[oppo];
    if (sUno->getPlayers() == 3) {
        sLayer[2].elem = sUno->getBackImage();
        sLayer[2].startLeft = x[oppo];
        sLayer[2].startTop = y[oppo];
        sLayer[2].endLeft = x[curr];
        sLayer[2].endTop = y[curr];
        animate(3, sLayer);
    } // if (sUno->getPlayers() == 3)
    else {
        sLayer[2].elem = sUno->getBackImage();
        sLayer[2].startLeft = x[oppo];
        sLayer[2].startTop = y[oppo];
        sLayer[2].endLeft = x[prev];
        sLayer[2].endTop = y[prev];
        sLayer[3].elem = sUno->getBackImage();
        sLayer[3].startLeft = x[prev];
        sLayer[3].startTop = y[prev];
        sLayer[3].endLeft = x[curr];
        sLayer[3].endTop = y[curr];
        animate(4, sLayer);
    } // else

    sHideFlag = 0x00;
    sUno->cycle();
    refreshScreen(i18n->info_0_rotate());
    threadWait(1500);
    setStatus(sUno->switchNow());
} // cycle()

/**
 * The player in action swap hand cards with another player.
 *
 * @param whom Swap with whom. Must be one of the following:
 *             Player::YOU, Player::COM1, Player::COM2, Player::COM3
 */
void Main::swapWith(int whom) {
    static int x[] = { 740, 160, 740, 1320 };
    static int y[] = { 670, 360, 50, 360 };
    int curr;

    setStatus(STAT_IDLE);
    curr = sUno->getNow();
    sHideFlag = (1 << curr) | (1 << whom);
    refreshScreen(i18n->info_7_swap(curr, whom));
    sLayer[0].elem = sLayer[1].elem = sUno->getBackImage();
    sLayer[0].startLeft = x[curr];
    sLayer[0].startTop = y[curr];
    sLayer[0].endLeft = x[whom];
    sLayer[0].endTop = y[whom];
    sLayer[1].startLeft = x[whom];
    sLayer[1].startTop = y[whom];
    sLayer[1].endLeft = x[curr];
    sLayer[1].endTop = y[curr];
    animate(2, sLayer);
    sHideFlag = 0x00;
    sUno->swap(curr, whom);
    refreshScreen(i18n->info_7_swap(curr, whom));
    threadWait(1500);
    setStatus(sUno->switchNow());
} // swapWith(int)

/**
 * The player in action play a card.
 *
 * @param index Play which card. Pass the corresponding card's index of the
 *              player's hand cards.
 * @param color Optional, available when the card to play is a wild card.
 *              Pass the specified following legal color.
 */
void Main::play(int index, Color color) {
    Card* card;
    int c, now, size, width, next;

    setStatus(STAT_IDLE); // block mouse click events when idle
    now = sUno->getNow();
    size = sUno->getCurrPlayer()->getHandSize();
    card = sUno->play(now, index, color);
    sSelectedIdx = -1;
    sSoundPool->play(SoundPool::SND_PLAY);
    if (card != nullptr) {
        sLayer[0].elem = card->image;
        switch (now) {
        case Player::COM1:
            width = 44 * qMin(size, 13) + 136;
            sLayer[0].startLeft = 160 + index / 13 * 44;
            sLayer[0].startTop = 450 - width / 2 + index % 13 * 44;
            break; // case Player::COM1

        case Player::COM2:
            width = 44 * size + 76;
            sLayer[0].startLeft = 800 - width / 2 + 44 * index;
            sLayer[0].startTop = 50;
            break; // case Player::COM2

        case Player::COM3:
            width = 44 * qMin(size, 13) + 136;
            sLayer[0].startLeft = 1320 - index / 13 * 44;
            sLayer[0].startTop = 450 - width / 2 + index % 13 * 44;
            break; // case Player::COM3

        default:
            width = 44 * size + 76;
            sLayer[0].startLeft = 800 - width / 2 + 44 * index;
            sLayer[0].startTop = 680;
            break; // default
        } // switch (now)

        sLayer[0].endLeft = 1118;
        sLayer[0].endTop = 360;
        animate(1, sLayer);
        if (size == 1) {
            // The player in action becomes winner when it played the
            // final card in its hand successfully
            if (sUno->is2vs2()) {
                if (now == Player::YOU || now == Player::COM2) {
                    sDiff = 2 * (sUno->getPlayer(Player::COM1)->getHandScore()
                        + sUno->getPlayer(Player::COM3)->getHandScore());
                    sScore = qMin(9999, 200 + sScore + sDiff);
                    sSoundPool->play(SoundPool::SND_WIN);
                } // if (now == Player::YOU || now == Player::COM2)
                else {
                    sDiff = -2 * (sUno->getPlayer(Player::YOU)->getHandScore()
                        + sUno->getPlayer(Player::COM2)->getHandScore());
                    sScore = qMax(-999, 200 + sScore + sDiff);
                    sSoundPool->play(SoundPool::SND_LOSE);
                } // else
            } // if (sUno->is2vs2())
            else if (now == Player::YOU) {
                sDiff = sUno->getPlayer(Player::COM1)->getHandScore()
                    + sUno->getPlayer(Player::COM2)->getHandScore()
                    + sUno->getPlayer(Player::COM3)->getHandScore();
                sScore = qMin(9999, 200 + sScore + sDiff);
                sSoundPool->play(SoundPool::SND_WIN);
            } // else if (now == Player::YOU)
            else {
                sDiff = -sUno->getPlayer(Player::YOU)->getHandScore();
                sScore = qMax(-999, 200 + sScore + sDiff);
                sSoundPool->play(SoundPool::SND_LOSE);
            } // else

            sAuto = false; // Force disable the AUTO switch
            sWinner = now;
            setStatus(STAT_GAME_OVER);
        } // if (size == 1)
        else {
            // When the played card is an action card or a wild card,
            // do the necessary things according to the game rule
            if (size == 2) {
                sSoundPool->play(SoundPool::SND_UNO);
            } // if (size == 2)

            switch (card->content) {
            case DRAW2:
                next = sUno->switchNow();
                if (sUno->isDraw2StackRule()) {
                    c = sUno->getDraw2StackCount();
                    refreshScreen(i18n->act_playDraw2(now, next, c));
                    threadWait(1500);
                    setStatus(next);
                } // if (sUno->isDraw2StackRule())
                else {
                    refreshScreen(i18n->act_playDraw2(now, next, 2));
                    threadWait(1500);
                    draw(2, /* force */ true);
                } // else
                break; // case DRAW2

            case SKIP:
                next = sUno->switchNow();
                refreshScreen(i18n->act_playSkip(now, next));
                threadWait(1500);
                setStatus(sUno->switchNow());
                break; // case SKIP

            case REV:
                sUno->switchDirection();
                refreshScreen(i18n->act_playRev(now));
                threadWait(1500);
                setStatus(sUno->switchNow());
                break; // case REV

            case WILD:
                refreshScreen(i18n->act_playWild(now, color));
                threadWait(1500);
                setStatus(sUno->switchNow());
                break; // case WILD

            case WILD_DRAW4:
                next = sUno->getNext();
                refreshScreen(i18n->act_playWildDraw4(now, next));
                threadWait(1500);
                setStatus(STAT_DOUBT_WILD4);
                break; // case WILD_DRAW4

            case NUM7:
                if (sUno->isSevenZeroRule()) {
                    refreshScreen(i18n->act_playCard(now, card->name));
                    threadWait(750);
                    setStatus(STAT_SEVEN_TARGET);
                    break; // case NUM7
                } // if (sUno->isSevenZeroRule())
                // else fall through

            case NUM0:
                if (sUno->isSevenZeroRule()) {
                    refreshScreen(i18n->act_playCard(now, card->name));
                    threadWait(750);
                    cycle();
                    break; // case NUM0
                } // if (sUno->isSevenZeroRule())
                // else fall through

            default:
                refreshScreen(i18n->act_playCard(now, card->name));
                threadWait(1500);
                setStatus(sUno->switchNow());
                break; // default
            } // switch (card->content)
        } // else
    } // if (card != nullptr)
} // play(int, Color)

/**
 * The player in action draw one or more cards.
 *
 * @param count How many cards to draw.
 * @param force Pass true if the specified player is required to draw cards,
 *              i.e. previous player played a [+2] or [wild +4] to let this
 *              player draw cards. Or false if the specified player draws a
 *              card by itself in its action.
 */
void Main::draw(int count, bool force) {
    Card* drawn;
    QString message;
    int i, index, c, now, size, width;

    setStatus(STAT_IDLE); // block mouse click events when idle
    c = sUno->getDraw2StackCount();
    if (c > 0) {
        count = c;
        force = true;
    } // if (c > 0)

    index = -1;
    drawn = nullptr;
    now = sUno->getNow();
    sSelectedIdx = -1;
    for (i = 0; i < count; ++i) {
        index = sUno->draw(now, force);
        if (index >= 0) {
            drawn = sUno->getCurrPlayer()->getHandCards()[index];
            size = sUno->getCurrPlayer()->getHandSize();
            sLayer[0].startLeft = 338;
            sLayer[0].startTop = 360;
            switch (now) {
            case Player::COM1:
                width = 44 * qMin(size, 13) + 136;
                sLayer[0].elem = sUno->getBackImage();
                sLayer[0].endLeft = 20 + index / 13 * 44;
                sLayer[0].endTop = 450 - width / 2 + index % 13 * 44;
                message = i18n->act_drawCardCount(now, count);
                break; // case Player::COM1

            case Player::COM2:
                width = 44 * size + 76;
                sLayer[0].elem = sUno->getBackImage();
                sLayer[0].endLeft = 800 - width / 2 + 44 * index;
                sLayer[0].endTop = 20;
                message = i18n->act_drawCardCount(now, count);
                break; // case Player::COM2

            case Player::COM3:
                width = 44 * qMin(size, 13) + 136;
                sLayer[0].elem = sUno->getBackImage();
                sLayer[0].endLeft = 1460 - index / 13 * 44;
                sLayer[0].endTop = 450 - width / 2 + index % 13 * 44;
                message = i18n->act_drawCardCount(now, count);
                break; // case Player::COM3

            default:
                width = 44 * size + 76;
                sLayer[0].elem = drawn->image;
                sLayer[0].endLeft = 800 - width / 2 + 44 * index;
                sLayer[0].endTop = 700;
                message = i18n->act_drawCard(now, drawn->name);
                break; // default
            } // switch (now)

            sSoundPool->play(SoundPool::SND_DRAW);
            animate(1, sLayer);
            refreshScreen(message);
            threadWait(300);
        } // if (index >= 0)
        else {
            message = i18n->info_cannotDraw(now, Uno::MAX_HOLD_CARDS);
            refreshScreen(message);
            break;
        } // else
    } // for (i = 0; i < count; ++i)

    threadWait(750);
    if (count == 1 &&
        drawn != nullptr &&
        sUno->isForcePlay() &&
        sUno->isLegalToPlay(drawn)) {
        // Player drew one card by itself, the drawn card
        // can be played immediately if it's legal to play
        if (!drawn->isWild()) {
            play(index);
        } // if (!drawn->isWild())
        else if (sAuto || now != Player::YOU) {
            play(index, sAI->calcBestColor4NowPlayer());
        } // else if (sAuto || now != Player::YOU)
        else {
            // Store index value as global value. This value
            // will be used after the wild color determined.
            sSelectedIdx = index;
            setStatus(STAT_WILD_COLOR);
        } // else
    } // if (count == 1 && ...)
    else {
        refreshScreen(i18n->act_pass(now));
        threadWait(750);
        setStatus(sUno->switchNow());
    } // else
} // draw(int, bool)

/**
 * Do uniform motion for objects from somewhere to somewhere.
 * NOTE: This function does not draw the last frame. After animation,
 * you need to call refreshScreen() function to draw the last frame.
 *
 * @param layerCount Move how many objects at the same time.
 * @param layer      Describe your movements by AnimateLayer objects.
 *                   Specifying parameters in AnimateLayer object to
 *                   describe your expected movements, such as:
 *                   [elem]      Move which object.
 *                   [startLeft] The object's start X coordinate.
 *                   [startTop]  The object's start Y coordinate.
 *                   [endLeft]   The object's end X coordinate.
 *                   [endTop]    The object's end Y coordinate.
 */
void Main::animate(int layerCount, AnimateLayer layer[]) {
    int i, j;
    static QRect roi;

    for (i = 0; i < 10; ++i) {
        for (j = 0; j < layerCount; ++j) {
            AnimateLayer& l = layer[j];
            roi.setX(l.startLeft + (l.endLeft - l.startLeft) * i / 10);
            roi.setY(l.startTop + (l.endTop - l.startTop) * i / 10);
            roi.setWidth(l.elem.width());
            roi.setHeight(l.elem.height());
            sBkPainter[j]->drawImage(roi, sScreen, roi);
        } // for (j = 0; j < layerCount; ++j)

        for (j = 0; j < layerCount; ++j) {
            AnimateLayer& l = layer[j];
            sPainter->drawImage(
                /* x     */ l.startLeft + (l.endLeft - l.startLeft) * i / 10,
                /* y     */ l.startTop + (l.endTop - l.startTop) * i / 10,
                /* image */ l.elem
            ); // drawImage(int, int, QImage&)
        } // for (j = 0; j < layerCount; ++j)

        update();
        threadWait(15);
        for (j = 0; j < layerCount; ++j) {
            AnimateLayer& l = layer[j];
            roi.setX(l.startLeft + (l.endLeft - l.startLeft) * i / 10);
            roi.setY(l.startTop + (l.endTop - l.startTop) * i / 10);
            roi.setWidth(l.elem.width());
            roi.setHeight(l.elem.height());
            sPainter->drawImage(roi, sBackup[j], roi);
        } // for (j = 0; j < layerCount; ++j)
    } // for (i = 0; i < 10; ++i)
} // animate(int, AnimateLayer[])

/**
 * Triggered on challenge chance. When a player played a [wild +4], the next
 * player can challenge its legality. Only when you have no cards that match
 * the previous played card's color, you can play a [wild +4].
 * Next player does not challenge: next player draw 4 cards;
 * Challenge success: current player draw 4 cards;
 * Challenge failure: next player draw 6 cards.
 */
void Main::onChallenge() {
    int now, challenger;
    bool challengeSuccess;

    setStatus(STAT_IDLE); // block mouse click events when idle
    now = sUno->getNow();
    challenger = sUno->getNext();
    challengeSuccess = sUno->challenge(now);
    refreshScreen(i18n->info_challenge(
        challenger, now, sUno->next2lastColor()));
    threadWait(1500);
    if (challengeSuccess) {
        // Challenge success, who played [wild +4] draws 4 cards
        refreshScreen(i18n->info_challengeSuccess(now));
        threadWait(1500);
        draw(4, /* force */ true);
    } // if (challengeSuccess)
    else {
        // Challenge failure, challenger draws 6 cards
        refreshScreen(i18n->info_challengeFailure(challenger));
        threadWait(1500);
        sUno->switchNow();
        draw(6, /* force */ true);
    } // else
} // onChallenge()

/**
 * Triggered when a mouse press event occurred. Called by system.
 */
void Main::mousePressEvent(QMouseEvent* event) {
    if (event->button() == Qt::LeftButton) {
        // Only response to left-click events, and ignore the others
        int x = 1600 * event->x() / this->width();
        int y = 900 * event->y() / this->height();
        if (sAdjustOptions) {
            // Do special behaviors when configuring game options
            if (60 <= y && y <= 240) {
                if (150 <= x && x <= 270) {
                    // BGM OFF button
                    sMediaPlay->setVolume(0);
                    setStatus(sStatus);
                } // if (150 <= x && x <= 270)
                else if (330 <= x && x <= 450) {
                    // BGM ON button
                    sMediaPlay->setVolume(50);
                    setStatus(sStatus);
                } // else if (330 <= x && x <= 450)
                else if (930 <= x && x <= 1050 && sStatus != Player::YOU) {
                    // Easy AI Level
                    sUno->setDifficulty(Uno::LV_EASY);
                    setStatus(sStatus);
                } // else if (930 <= x && x <= 1050 && sStatus != Player::YOU)
                else if (1110 <= x && x <= 1230 && sStatus != Player::YOU) {
                    // Hard AI Level
                    sUno->setDifficulty(Uno::LV_HARD);
                    setStatus(sStatus);
                } // else if (1110 <= x && x <= 1230 && sStatus != Player::YOU)
            } // if (60 <= y && y <= 240)
            else if (270 <= y && y <= 450) {
                if (150 <= x && x <= 270) {
                    // SND OFF button
                    sSoundPool->setEnabled(false);
                    setStatus(sStatus);
                } // if (150 <= x && x <= 270)
                else if (330 <= x && x <= 450) {
                    // SND ON button
                    sSoundPool->setEnabled(true);
                    sSoundPool->play(SoundPool::SND_PLAY);
                    setStatus(sStatus);
                } // else if (330 <= x && x <= 450)
                else if (930 <= x && x <= 1050 && sStatus != Player::YOU) {
                    // 3 players
                    sUno->setPlayers(3);
                    setStatus(sStatus);
                } // else if (930 <= x && x <= 1050 && sStatus != Player::YOU)
                else if (1110 <= x && x <= 1230 && sStatus != Player::YOU) {
                    // 4 players
                    sUno->setPlayers(4);
                    setStatus(sStatus);
                } // else if (1110 <= x && x <= 1230 && sStatus != Player::YOU)
                else if (1290 <= x && x <= 1410 && sStatus != Player::YOU) {
                    // 2vs2
                    sUno->set2vs2(true);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1410 && sStatus != Player::YOU)
            } // else if (270 <= y && y <= 450)
            else if (649 <= y && y <= 670 && sStatus != Player::YOU) {
                if (1110 <= x && x <= 1143) {
                    // Decrease initial cards
                    sUno->decreaseInitialCards();
                    setStatus(sStatus);
                } // if (1110 <= x && x <= 1143)
                else if (1358 <= x && x <= 1391) {
                    // Increase initial cards
                    sUno->increaseInitialCards();
                    setStatus(sStatus);
                } // else if (1358 <= x && x <= 1391)
            } // else if (649 <= y && y <= 670 && sStatus != Player::YOU)
            else if (699 <= y && y <= 720 && sStatus != Player::YOU) {
                if (1110 <= x && x <= 1211) {
                    // Force play, <KEEP> button
                    sUno->setForcePlay(false);
                    setStatus(sStatus);
                } // if (1110 <= x && x <= 1211)
                else if (1290 <= x && x <= 1391) {
                    // Force play, <PLAY> button
                    sUno->setForcePlay(true);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1391)
            } // else if (699 <= y && y <= 720 && sStatus != Player::YOU)
            else if (749 <= y && y <= 770 && sStatus != Player::YOU) {
                if (1110 <= x && x <= 1194) {
                    // 7-0, <OFF> button
                    sUno->setSevenZeroRule(false);
                    setStatus(sStatus);
                } // if (1110 <= x && x <= 1194)
                else if (1290 <= x && x <= 1357) {
                    // 7-0, <ON> button
                    sUno->setSevenZeroRule(true);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1357)
            } // else if (749 <= y && y <= 770 && sStatus != Player::YOU)
            else if (799 <= y && y <= 820 && sStatus != Player::YOU) {
                if (1110 <= x && x <= 1194) {
                    // +2 stack, <OFF> button
                    sUno->setDraw2StackRule(false);
                    setStatus(sStatus);
                } // if (1110 <= x && x <= 1194)
                else if (1290 <= x && x <= 1357) {
                    // +2 stack, <ON> button
                    sUno->setDraw2StackRule(true);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1357)
            } // else if (799 <= y && y <= 820 && sStatus != Player::YOU)
            else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                // <OPTIONS> button
                // Leave options page
                sAdjustOptions = false;
                setStatus(sStatus);
            } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
        } // if (sAdjustOptions)
        else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580) {
            // <AUTO> button
            // In player's action, automatically play or draw cards by AI
            if (sStatus == Player::YOU) {
                sAuto = true;
                setStatus(sStatus);
            } // if (sStatus == Player::YOU)
        } // else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580)
        else switch (sStatus) {
        case STAT_WELCOME:
            if (360 <= y && y <= 540 && 740 <= x && x <= 860) {
                // UNO button, start a new game
                setStatus(STAT_NEW_GAME);
            } // if (360 <= y && y <= 540 && 740 <= x && x <= 860)
            else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                // <OPTIONS> button
                sAdjustOptions = true;
                setStatus(sStatus);
            } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
            break; // case STAT_WELCOME

        case Player::YOU:
            if (sAuto) {
                // Do operations automatically by AI strategies
                break; // case Player::YOU
            } // if (sAuto)
            else if (700 <= y && y <= 880) {
                Player* now = sUno->getPlayer(Player::YOU);
                auto hand = now->getHandCards();
                int size = int(hand.size());
                int width = 44 * size + 76;
                int startX = 800 - width / 2;
                if (startX <= x && x <= startX + width) {
                    // Hand card area
                    // Calculate which card clicked by the X-coordinate
                    int index = qMin((x - startX) / 44, size - 1);
                    Card* card = hand[index];

                    // Try to play it
                    if (index != sSelectedIdx) {
                        sSelectedIdx = index;
                        setStatus(sStatus);
                    } // if (index != sSelectedIdx)
                    else if (sUno->isLegalToPlay(card)) {
                        if (card->isWild() && size > 1) {
                            setStatus(STAT_WILD_COLOR);
                        } // if (card->isWild() && size > 1)
                        else {
                            play(index);
                        } // else
                    } // else if (sUno->isLegalToPlay(card))
                } // if (startX <= x && x <= startX + width)
                else if (y >= 859 && 20 <= x && x <= 200) {
                    // <OPTIONS> button
                    sAdjustOptions = true;
                    setStatus(sStatus);
                } // else if (y >= 859 && 20 <= x && x <= 200)
                else {
                    // Blank area, cancel your selection
                    sSelectedIdx = -1;
                    setStatus(sStatus);
                } // else
            } // else if (700 <= y && y <= 880)
            else if (360 <= y && y <= 540 && 338 <= x && x <= 458) {
                // Card deck area, draw a card
                draw();
            } // else if (360 <= y && y <= 540 && 338 <= x && x <= 458)
            break; // case Player::YOU

        case STAT_WILD_COLOR:
            if (310 < y && y < 405) {
                if (310 < x && x < 405) {
                    // Red sector
                    play(sSelectedIdx, RED);
                } // if (310 < x && x < 405)
                else if (405 < x && x < 500) {
                    // Blue sector
                    play(sSelectedIdx, BLUE);
                } // else if (405 < x && x < 500)
            } // if (310 < y && y < 405)
            else if (405 < y && y < 500) {
                if (310 < x && x < 405) {
                    // Yellow sector
                    play(sSelectedIdx, YELLOW);
                } // if (310 < x && x < 405)
                else if (405 < x && x < 500) {
                    // Green sector
                    play(sSelectedIdx, GREEN);
                } // else if (405 < x && x < 500)
            } // else if (405 < y && y < 500)
            break; // case STAT_WILD_COLOR

        case STAT_DOUBT_WILD4:
            // Asking if you want to challenge your previous player
            if (310 < x && x < 500) {
                if (310 < y && y < 405) {
                    // YES button, challenge wild +4
                    onChallenge();
                } // if (310 < y && y < 405)
                else if (405 < y && y < 500) {
                    // NO button, do not challenge wild +4
                    sUno->switchNow();
                    draw(4, /* force */ true);
                } // else if (405 < y && y < 500)
            } // if (310 < x && x < 500)
            break; // case STAT_DOUBT_WILD4

        case STAT_SEVEN_TARGET:
            if (288 < y && y < 366 && sUno->getPlayers() == 4) {
                if (338 < x && x < 472) {
                    // North sector
                    swapWith(Player::COM2);
                } // if (338 < x && x < 472)
            } // if (288 < y && y < 366 && sUno->getPlayers() == 4)
            else if (405 < y && y < 500) {
                if (310 < x && x < 405) {
                    // West sector
                    swapWith(Player::COM1);
                } // if (310 < x && x < 405)
                else if (405 < x && x < 500) {
                    // East sector
                    swapWith(Player::COM3);
                } // else if (405 < x && x < 500)
            } // else if (405 < y && y < 500)
            break; // case STAT_SEVEN_TARGET

        case STAT_GAME_OVER:
            if (360 <= y && y <= 540 && 338 <= x && x <= 458) {
                // Card deck area, start a new game
                setStatus(STAT_NEW_GAME);
            } // if (360 <= y && y <= 540 && 338 <= x && x <= 458)
            else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                // <OPTIONS> button
                sAdjustOptions = true;
                setStatus(sStatus);
            } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
            break; // case STAT_GAME_OVER

        default:
            break; // default
        } // else switch (sStatus)
    } // if (event->button() == Qt::LeftButton)
} // mousePressEvent(QMouseEvent*)

/**
 * Triggered when application finishes.
 */
void Main::closeEvent(QCloseEvent*) {
    QFile writer("UnoCard.stat");

    if (writer.open(QIODevice::WriteOnly)) {
        // Store statistics data to file
        int dw[64] = {
            /* dw[0] = your score         */ qMax(-999, sScore),
            /* dw[1] = players (3/4)      */ sUno->getPlayers(),
            /* dw[2] = difficulty (ez/hd) */ sUno->getDifficulty(),
            /* dw[3] = force play switch  */ sUno->isForcePlay() ? 1 : 0,
            /* dw[4] = seven zero switch  */ sUno->isSevenZeroRule() ? 1 : 0,
            /* dw[5] = +2 stack switch    */ sUno->isDraw2StackRule() ? 1 : 0,
            /* dw[6] = snd switch         */ sSoundPool->isEnabled() ? 1 : 0,
            /* dw[7] = bgm switch         */ sMediaPlay->volume(),
            /* dw[8] = initial cards      */ sUno->getInitialCards(),
            /* dw[9] = 2vs2 switch        */ sUno->is2vs2() ? 1 : 0
        }; // dw[]

        // Fill unused bits with 1
        memset(dw + 10, -1, (63 - 10) * sizeof(int));

        // Calculate hash
        for (int i = 0; i < 63; ++i) {
            dw[63] = 31 * dw[63] + dw[i];
        } // for (int i = 0; i < 63; ++i)

        writer.write(FILE_HEADER, 8);
        writer.write((char*)dw, 64 * sizeof(int));
        writer.close();
    } // if (writer.open(QIODevice::WriteOnly))

    exit(0);
} // closeEvent(QCloseEvent*)

/**
 * Defines the entry point for the console application.
 */
int main(int argc, char* argv[]) {
    QApplication app(argc, argv);
    Main window(argc, argv);

    app.setWindowIcon(QIcon("resource/icon_128x128.ico"));
    window.show();
    return app.exec();
} // main(int, char*[])

// E.O.F