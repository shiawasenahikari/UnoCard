////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <QPen>
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
#include <QFileDialog>
#include <QMouseEvent>
#include <QPaintEvent>
#include <QApplication>
#include <QMediaPlayer>
#include <QFontDatabase>
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
static const int STAT_ASK_KEEP_PLAY = 0x8888;
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
    int dw[64] = { 0 };
    char header[9] = { 0 };
    QFile reader("UnoCard.stat");

    // Preparations
    QFontDatabase::addApplicationFont("resource/noto.ttc");
    if (strstr(argv[0], "zh") != nullptr) {
        sFont.setFamily("Noto Sans Mono CJK SC");
        sFont.setPixelSize(34);
        i18n = new I18N_zh_CN;
    } // if (strstr(argv[0], "zh") != nullptr)
    else if (strstr(argv[0], "ja") != nullptr) {
        sFont.setFamily("Noto Sans Mono CJK JP");
        sFont.setPixelSize(34);
        i18n = new I18N_ja_JP;
    } // else if (strstr(argv[0], "ja") != nullptr)
    else {
        sFont.setFamily("Noto Sans Mono CJK JP");
        sFont.setPixelSize(34);
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
                if (dw[0] > 9999)
                    dw[0] = 9999;
                else if (dw[0] < -999)
                    dw[0] = -999;
                sScore = dw[0];
                sUno->setPlayers(dw[1]);
                sUno->setDifficulty(dw[2]);
                sUno->setForcePlayRule(dw[3]);
                sUno->setSevenZeroRule(dw[4] != 0);
                sUno->setStackRule(dw[5]);
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
                if (dw[0] > 9999)
                    dw[0] = 9999;
                else if (dw[0] < -999)
                    dw[0] = -999;
                sScore = dw[0];
                sUno->setPlayers(dw[1]);
                sUno->setDifficulty(dw[2]);
                sUno->setForcePlayRule(dw[3]);
                sUno->setSevenZeroRule(dw[4] != 0);
                sUno->setStackRule(dw[5]);
                sSoundPool->setEnabled(dw[6] != 0);
                sMediaPlay->setVolume(dw[7]);
                sUno->set2vs2(dw[9] != 0);
                sSpeed = dw[10] < 2 ? 1 : dw[10] > 2 ? 3 : 2;
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
    sWinner = Player::YOU;
    sAdjustOptions = false;
    connect(this, SIGNAL(signal_requestAI()), this, SLOT(requestAI()));
    for (i = 0; i <= 3; ++i) {
        sBackup[i] = QImage(1600, 900, QImage::Format_RGB888);
        sBkPainter[i] = new QPainter(&sBackup[i]);
    } // for (i = 0; i <= 3; ++i)

    sScreen = QImage(1600, 900, QImage::Format_RGB888);
    sPainter = new QPainter(&sScreen);
    sPainter->setPen(Qt::NoPen);
    sPainter->setFont(sFont);
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
} // requestAI()

/**
 * Let our UI wait the number of specified milli seconds.
 *
 * @param millis How many milli seconds to wait.
 */
void Main::threadWait(int millis) {
    static QEventLoop loop;

    QTimer::singleShot(millis / sSpeed, &loop, SLOT(quit()));
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
            refreshScreen(i18n->info_skipped(sUno->getNow()), 0x00);
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
            emit signal_requestAI();
        } // if (sAuto)
        else if (sAdjustOptions) {
            refreshScreen();
        } // else if (sAdjustOptions)
        else if (sUno->legalCardsCount4NowPlayer() == 0) {
            draw();
        } // else if (sUno->legalCardsCount4NowPlayer() == 0)
        else if (sUno->getHandCardsOf(Player::YOU).size() == 1) {
            play(0);
        } // else if (sUno->getHandCardsOf(Player::YOU).size() == 1)
        else if (sSelectedIdx < 0) {
            int c = sUno->getDraw2StackCount();

            refreshScreen(c == 0
                ? i18n->info_yourTurn()
                : sUno->getStackRule() != 2
                ? i18n->info_yourTurn_stackDraw2(c)
                : i18n->info_yourTurn_stackDraw2(c, 2));
        } // else if (sSelectedIdx < 0)
        else {
            auto hand = sUno->getHandCardsOf(Player::YOU);
            Card* card = hand[sSelectedIdx];

            refreshScreen(sUno->isLegalToPlay(card)
                ? i18n->info_clickAgainToPlay(card->name)
                : i18n->info_cannotPlay(card->name));
        } // else
        break; // case Player::YOU

    case STAT_WILD_COLOR:
        // Need to specify the following legal color after played a
        // wild card. Draw color sectors in the center of screen
        refreshScreen(i18n->ask_color(), 0x21);
        break; // case STAT_WILD_COLOR

    case STAT_DOUBT_WILD4:
        if (sUno->getNext() == Player::YOU && !sAuto) {
            // Challenge or not is decided by you
            refreshScreen(i18n->ask_challenge(sUno->next2lastColor()), 0x00);
        } // if (sUno->getNext() == Player::YOU && !sAuto)
        else if (sAI->needToChallenge()) {
            // Challenge or not is decided by AI
            onChallenge();
        } // else if (sAI->needToChallenge())
        else {
            sUno->switchNow();
            draw(4, /* force */ true);
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
            refreshScreen(i18n->ask_target(), 0x00);
        } // else
        break; // case STAT_SEVEN_TARGET

    case STAT_ASK_KEEP_PLAY:
        if (sAuto) {
            play(sSelectedIdx, sAI->calcBestColor4NowPlayer());
        } // if (sAuto)
        else {
            refreshScreen(i18n->ask_keep_play(), 0x01);
        } // else
        break; // case STAT_ASK_KEEP_PLAY

    case Player::COM1:
    case Player::COM2:
    case Player::COM3:
        // AI players' turn
        emit signal_requestAI();
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
 * Measure the text width, using our custom font.
 * <p>
 * SPECIAL: In the text string, you can use color marks ([R], [B],
 * [G], [W] and [Y]) to control the color of the remaining text.
 * COLOR MARKS SHOULD NOT BE TREATED AS PRINTABLE CHARACTERS.
 *
 * @param text Measure which text's width.
 * @return Width of the provided text (unit: pixels).
 */
int Main::getTextWidth(const QString& text) {
    int width = 0;

    for (int i = 0, n = text.length(); i < n; ++i) {
        if ('[' == text[i] && i + 2 < n && text[i + 2] == ']') {
            i += 2;
        } // if ('[' == text[i] && i + 2 < n && text[i + 2] == ']')
        else {
            width += ' ' <= text[i] && text[i] <= '~' ? 17 : 33;
        } // else
    } // for (int i = 0, n = text.length(); i < n; ++i)

    return width;
} // getTextWidth(const QString&)

/**
 * Put text on image, using our custom font.
 * <p>
 * SPECIAL: In the text string, you can use color marks ([R], [B],
 * [G], [W] and [Y]) to control the color of the remaining text.
 *
 * @param text Put which text.
 * @param x    Put on where (x coordinate).
 * @param y    Put on where (y coordinate).
 */
void Main::putText(const QString& text, int x, int y) {
    static const QPen pen_red(QColor(0xFF, 0x77, 0x77));
    static const QPen pen_blue(QColor(0x77, 0x77, 0xFF));
    static const QPen pen_green(QColor(0x77, 0xCC, 0x77));
    static const QPen pen_white(QColor(0xCC, 0xCC, 0xCC));
    static const QPen pen_yellow(QColor(0xFF, 0xCC, 0x11));

    sPainter->setPen(pen_white);
    for (int i = 0, n = text.length(); i < n; ++i) {
        if ('[' == text[i] && i + 2 < n && text[i + 2] == ']') {
            ++i;
            if (text[i] == 'R')
                sPainter->setPen(pen_red);
            if (text[i] == 'B')
                sPainter->setPen(pen_blue);
            if (text[i] == 'G')
                sPainter->setPen(pen_green);
            if (text[i] == 'W')
                sPainter->setPen(pen_white);
            if (text[i] == 'Y')
                sPainter->setPen(pen_yellow);
            ++i;
        } // if ('[' == text[i] && i + 2 < n && text[i + 2] == ']')
        else {
            sPainter->drawText(x, y, text[i]);
            x += ' ' <= text[i] && text[i] <= '~' ? 17 : 33;
        } // else
    } // for (int i = 0, n = text.length(); i < n; ++i)

    sPainter->setPen(Qt::NoPen);
} // putText(const QString&, int, int)

/**
 * Refresh the screen display. The content of global variable [sScreen]
 * will be changed after calling this function.
 *
 * @param message Extra message to show.
 * @param area    Refresh which area. Use | to select multiple areas.
 *                0x01 = Your hand card area
 *                0x02 = WEST's hand card area
 *                0x04 = NORTH's hand card area
 *                0x08 = EAST's hand card area
 *                0x10 = (Reversed bit, not used yet)
 *                0x20 = Left-bottom & Right-bottom corner
 *                0x40 = Center deck area
 *                0x80 = Recent card area
 *                0xff = Full screen (default)
 */
void Main::refreshScreen(const QString& message, int area) {
    static bool active;
    static QImage image;
    static QString info;
    static const RecentInfo* recent;
    static int i, x, remain, size, status, used, width;

    // Lock the value of global variable [sStatus]
    status = sStatus;

    // Clear
    if ((area &= 0xff) == 0xff) {
        sPainter->drawImage(0, 0, sUno->getBackground());
    } // if ((area &= 0xff) == 0xff)
    else {
        image = sUno->getBackground();
        sPainter->drawImage(200, 584, image, 200, 584, 1200, 48);
    } // else

    // Message area
    width = getTextWidth(message);
    putText(message, 800 - width / 2, 620);

    // Left-bottom & Right-bottom corner
    if ((area & 0x20) != 0x00) {
        if (area != 0xff) {
            image = sUno->getBackground();
            sPainter->drawImage(20, 844, image, 20, 844, 170, 48);
            image = sUno->getBackground();
            sPainter->drawImage(1427, 844, image, 1427, 844, 153, 48);
        } // if (area != 0xff)

        // Left-bottom corner: <OPTIONS> button
        // Shows only when game is not in process
        if (status == Player::YOU ||
            status == STAT_WELCOME ||
            status == STAT_GAME_OVER) {
            active = sAdjustOptions;
            putText(i18n->btn_settings(active), 20, 880);
        } // if (status == Player::YOU || ...)

        // Right-bottom corner: <AUTO> / <LOAD> / <SAVE>
        if (!sAdjustOptions) {
            if (status == Player::YOU && !sAuto) {
                width = getTextWidth(i18n->btn_auto());
                putText(i18n->btn_auto(), 1580 - width, 880);
            } // if (status == Player::YOU && !sAuto)
            else if (status == STAT_WELCOME) {
                width = getTextWidth(i18n->btn_load());
                putText(i18n->btn_load(), 1580 - width, 880);
            } // else if (status == STAT_WELCOME)
            else if (status == STAT_GAME_OVER && !sGameSaved) {
                width = getTextWidth(i18n->btn_save());
                putText(i18n->btn_save(), 1580 - width, 880);
            } // else if (status == STAT_GAME_OVER && !sGameSaved)
        } // if (!sAdjustOptions)
    } // if ((area & 0x20) != 0x00)

    if (sAdjustOptions) {
        // Show special screen when configuring game options
        // BGM switch
        putText(i18n->label_bgm(), 60, 160);
        image = sMediaPlay->volume() > 0
            ? sUno->findCard(RED, SKIP)->darkImg
            : sUno->findCard(RED, SKIP)->image;
        sPainter->drawImage(150, 60, image);
        image = sMediaPlay->volume() > 0
            ? sUno->findCard(GREEN, REV)->image
            : sUno->findCard(GREEN, REV)->darkImg;
        sPainter->drawImage(330, 60, image);

        // Sound effect switch
        putText(i18n->label_snd(), 60, 350);
        image = sSoundPool->isEnabled()
            ? sUno->findCard(RED, SKIP)->darkImg
            : sUno->findCard(RED, SKIP)->image;
        sPainter->drawImage(150, 250, image);
        image = sSoundPool->isEnabled()
            ? sUno->findCard(GREEN, REV)->image
            : sUno->findCard(GREEN, REV)->darkImg;
        sPainter->drawImage(330, 250, image);

        // Speed
        putText(i18n->label_speed(), 780, 350);
        image = sSpeed < 2
            ? sUno->findCard(GREEN, NUM1)->image
            : sUno->findCard(GREEN, NUM1)->darkImg;
        sPainter->drawImage(930, 250, image);
        image = sSpeed == 2
            ? sUno->findCard(YELLOW, NUM2)->image
            : sUno->findCard(YELLOW, NUM2)->darkImg;
        sPainter->drawImage(1110, 250, image);
        image = sSpeed > 2
            ? sUno->findCard(BLUE, NUM3)->image
            : sUno->findCard(BLUE, NUM3)->darkImg;
        sPainter->drawImage(1290, 250, image);

        if (status != Player::YOU) {
            // [Level] option: easy / hard
            putText(i18n->label_level(), 780, 160);
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

            // Rule settings
            // Initial cards
            putText(i18n->label_initialCards(), 60, 670);
            width = sUno->getInitialCards();
            info = QString::number(width / 10) + QString::number(width % 10);
            putText("<-", 896, 670);
            putText(info, 1127, 670);
            putText("+>", 1358, 670);

            // Game Mode
            putText(i18n->label_gameMode(), 60, 720);
            active = sUno->getPlayers() == 3;
            putText(i18n->btn_3p(active), 896, 720);
            active = sUno->getPlayers() == 4
                && !sUno->isSevenZeroRule()
                && !sUno->is2vs2();
            putText(i18n->btn_4p(active), 1028, 720);
            active = sUno->isSevenZeroRule();
            putText(i18n->btn_7_0(active), 1159, 720);
            active = sUno->is2vs2();
            putText(i18n->btn_2vs2(active), 1290, 720);

            // Force play switch
            i = sUno->getForcePlayRule();
            putText(i18n->label_forcePlay(), 60, 770);
            putText(i18n->btn_keep(i == 0), 896, 770);
            putText(i18n->btn_ask(i == 1), 1110, 770);
            putText(i18n->btn_play(i == 2), 1290, 770);

            // stacking
            i = sUno->getStackRule();
            putText(i18n->label_draw2Stack(), 60, 820);
            putText(i18n->btn_none(i == 0), 896, 820);
            putText(i18n->btn_d2(i == 1), 1110, 820);
            putText(i18n->btn_d4(i == 2), 1290, 820);
        } // if (status != Player::YOU)
    } // if (sAdjustOptions)
    else if (status == STAT_WELCOME) {
        // For welcome screen, show the start button and your score
        image = sUno->getBackImage();
        sPainter->drawImage(740, 360, image);
        width = getTextWidth(i18n->label_score());
        putText(i18n->label_score(), 500 - width, 800);
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
        if ((area & 0x40) != 0x00) {
            if (area != 0xff) {
                image = sUno->getBackground();
                sPainter->drawImage(270, 270, image, 270, 270, 271, 271);
            } // if (area != 0xff)

            image = sUno->getBackImage();
            sPainter->drawImage(338, 360, image);
        } // if ((area & 0x40) != 0x00)

        if ((area & 0x80) != 0x00) {
            recent = sUno->getRecentInfo();
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
        } // if ((area & 0x80) != 0x00)

        // Left-top corner: remain / used
        if (area != 0xff) {
            image = sUno->getBackground();
            sPainter->drawImage(20, 6, image, 20, 6, 170, 48);
        } // if (area != 0xff)

        remain = sUno->getDeckCount();
        used = sUno->getUsedCount();
        info = i18n->label_remain_used(remain, used);
        putText(info, 20, 42);

        // Right-top corner: lacks
        if (area != 0xff) {
            image = sUno->getBackground();
            sPainter->drawImage(1427, 6, image, 1410, 6, 153, 48);
        } // if (area != 0xff)

        info = i18n->label_lacks(
            sUno->getPlayer(Player::COM2)->getWeakColor(),
            sUno->getPlayer(Player::COM3)->getWeakColor(),
            sUno->getPlayer(Player::COM1)->getWeakColor(),
            sUno->getPlayer(Player::YOU)->getWeakColor());
        width = getTextWidth(info);
        putText(info, 1580 - width, 42);

        // Left-center: Hand cards of Player West (COM1)
        if ((area & 0x02) != 0x00) {
            if (area != 0xff) {
                image = sUno->getBackground();
                sPainter->drawImage(20, 96, image, 20, 96, 165, 709);
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && sWinner == Player::COM1) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 80 - width / 2, 461);
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
                    width = getTextWidth("UNO");
                    putText("[Y]UNO", 80 - width / 2, 584);
                } // if (size == 1)
            } // else if (((sHideFlag >> 1) & 0x01) == 0x00)
        } // if ((area & 0x02) != 0x00)

        // Top-center: Hand cards of Player North (COM2)
        if ((area & 0x04) != 0x00) {
            if (area != 0xff) {
                image = sUno->getBackground();
                sPainter->drawImage(190, 20, image, 190, 20, 1221, 181);
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && sWinner == Player::COM2) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 800 - width / 2, 121);
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
                    width = getTextWidth("UNO");
                    putText("[Y]UNO", 720 - width, 121);
                } // if (size == 1)
            } // else if (((sHideFlag >> 2) & 0x01) == 0x00)
        } // if ((area & 0x04) != 0x00)

        // Right-center: Hand cards of Player East (COM3)
        if ((area & 0x08) != 0x00) {
            if (area != 0xff) {
                image = sUno->getBackground();
                sPainter->drawImage(1416, 96, image, 1416, 96, 165, 709);
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && sWinner == Player::COM3) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 1520 - width / 2, 461);
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
                    width = getTextWidth("UNO");
                    putText("[Y]UNO", 1520 - width / 2, 584);
                } // if (size == 1)
            } // else if (((sHideFlag >> 3) & 0x01) == 0x00)
        } // if ((area & 0x08) != 0x00)

        // Bottom: Your hand cards
        if ((area & 0x01) != 0x00) {
            if (area != 0xff) {
                image = sUno->getBackground();
                sPainter->drawImage(190, 680, image, 190, 680, 1221, 201);
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && sWinner == Player::YOU) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 800 - width / 2, 801);
            } // if (status == STAT_GAME_OVER && sWinner == Player::YOU)
            else if ((sHideFlag & 0x01) == 0x00) {
                // Show your all hand cards
                auto hand = sUno->getHandCardsOf(Player::YOU);

                size = int(hand.size());
                width = 44 * size + 76;
                for (i = 0; i < size; ++i) {
                    image = status == STAT_GAME_OVER
                        || (status == Player::YOU && sUno->isLegalToPlay(hand[i]))
                        || (status == STAT_ASK_KEEP_PLAY && i == sSelectedIdx)
                        ? hand[i]->image
                        : hand[i]->darkImg;
                    sPainter->drawImage(
                        /* x     */ 800 - width / 2 + 44 * i,
                        /* y     */ i == sSelectedIdx ? 680 : 700,
                        /* image */ image
                    ); // drawImage(int, int, QImage&)
                } // for (i = 0; i < size; ++i)

                if (size == 1) {
                    // Show "UNO" warning when only one card in hand
                    putText("[Y]UNO", 880, 801);
                } // if (size == 1)
            } // else if ((sHideFlag & 0x01) == 0x00)
        } // if ((area & 0x01) != 0x00)

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
        case STAT_ASK_KEEP_PLAY:
            // Ask whether you want to challenge your previous player
            // Draw YES button
            sPainter->setBrush(BRUSH_GREEN);
            sPainter->drawPie(270, 270, 271, 271, 0, 180 * 16);
            width = getTextWidth(i18n->label_yes());
            putText(i18n->label_yes(), 405 - width / 2, 358);

            // Draw NO button
            sPainter->setBrush(BRUSH_RED);
            sPainter->drawPie(270, 270, 271, 271, 0, -180 * 16);
            width = getTextWidth(i18n->label_no());
            putText(i18n->label_no(), 405 - width / 2, 472);
            break; // case STAT_DOUBT_WILD4, STAT_ASK_KEEP_PLAY

        case STAT_SEVEN_TARGET:
            // Ask the target you want to swap hand cards with
            // Draw west sector (red)
            sPainter->setBrush(BRUSH_RED);
            sPainter->drawPie(270, 270, 271, 271, -90 * 16, -120 * 16);
            width = getTextWidth("W");
            putText("W", 338 - width / 2, 440);

            // Draw east sector (green)
            sPainter->setBrush(BRUSH_GREEN);
            sPainter->drawPie(270, 270, 271, 271, -90 * 16, 120 * 16);
            width = getTextWidth("E");
            putText("E", 472 - width / 2, 440);

            // Draw north sector (yellow)
            sPainter->setBrush(BRUSH_YELLOW);
            sPainter->drawPie(270, 270, 271, 271, 150 * 16, -120 * 16);
            width = getTextWidth("N");
            putText("N", 405 - width / 2, 360);
            break; // case STAT_SEVEN_TARGET

        default:
            break; // default
        } // switch (status)
    } // else

    // Show screen
    update();
} // refreshScreen(const QString&, int)

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
    refreshScreen(i18n->info_0_rotate(), 0x0f);
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
    refreshScreen(i18n->info_0_rotate(), 0x0f);
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
    int curr, flag;

    setStatus(STAT_IDLE);
    curr = sUno->getNow();
    sHideFlag = (1 << curr) | (1 << whom);
    flag = (1 << curr) | (1 << whom) | (curr == Player::YOU ? 0x40 : 0x00);
    refreshScreen(i18n->info_7_swap(curr, whom), flag);
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
    refreshScreen(i18n->info_7_swap(curr, whom), flag & ~0x40);
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
    int c, now, size, width, next, flag;

    setStatus(STAT_IDLE); // block mouse click events when idle
    now = sUno->getNow();
    size = sUno->getCurrPlayer()->getHandSize();
    if (sUno->getStackRule() == 2 && sUno->getCurrPlayer()
        ->getHandCards()[index]->content == WILD_DRAW4) {
        color = sUno->lastColor();
    } // if (sUno->getStackRule() == 2 && ...)

    card = sUno->play(now, index, color);
    sSelectedIdx = -1;
    sSoundPool->play(SoundPool::SND_PLAY);
    if (size == 2) {
        sSoundPool->play(SoundPool::SND_UNO);
    } // if (size == 2)

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
            flag = now == Player::YOU ? 0xe1 : (1 << now) | 0x80;
            switch (card->content) {
            case DRAW2:
                next = sUno->switchNow();
                if (sUno->getStackRule() != 0) {
                    c = sUno->getDraw2StackCount();
                    refreshScreen(i18n->act_playDraw2(now, next, c), flag);
                    threadWait(1500);
                    setStatus(next);
                } // if (sUno->getStackRule() != 0)
                else {
                    refreshScreen(i18n->act_playDraw2(now, next, 2), flag);
                    threadWait(1500);
                    draw(2, /* force */ true);
                } // else
                break; // case DRAW2

            case SKIP:
                next = sUno->switchNow();
                refreshScreen(i18n->act_playSkip(now, next), flag);
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
                refreshScreen(i18n->act_playWild(now, color), flag);
                threadWait(1500);
                setStatus(sUno->switchNow());
                break; // case WILD

            case WILD_DRAW4:
                if (sUno->getStackRule() == 2) {
                    next = sUno->switchNow();
                    c = sUno->getDraw2StackCount();
                    refreshScreen(i18n->act_playDraw2(now, next, c), flag);
                    threadWait(1500);
                    setStatus(next);
                } // if (sUno->getStackRule() == 2)
                else {
                    next = sUno->getNext();
                    refreshScreen(i18n->act_playWildDraw4(now, next), flag);
                    threadWait(1500);
                    setStatus(STAT_DOUBT_WILD4);
                } // else
                break; // case WILD_DRAW4

            case NUM7:
                if (sUno->isSevenZeroRule()) {
                    refreshScreen(i18n->act_playCard(now, card->name), flag);
                    threadWait(750);
                    setStatus(STAT_SEVEN_TARGET);
                    break; // case NUM7
                } // if (sUno->isSevenZeroRule())
                // else fall through

            case NUM0:
                if (sUno->isSevenZeroRule()) {
                    refreshScreen(i18n->act_playCard(now, card->name), flag);
                    threadWait(750);
                    cycle();
                    break; // case NUM0
                } // if (sUno->isSevenZeroRule())
                // else fall through

            default:
                refreshScreen(i18n->act_playCard(now, card->name), flag);
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
    int i, index, c, now, size, width, flag;

    setStatus(STAT_IDLE); // block mouse click events when idle
    c = sUno->getDraw2StackCount();
    if (c > 0) {
        count = c;
        force = true;
    } // if (c > 0)

    index = -1;
    drawn = nullptr;
    now = sUno->getNow();
    flag = now == Player::YOU ? 0xff : 1 << now;
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
            refreshScreen(message, flag);
            threadWait(300);
        } // if (index >= 0)
        else {
            message = i18n->info_cannotDraw(now, Uno::MAX_HOLD_CARDS);
            refreshScreen(message, flag);
            break;
        } // else
    } // for (i = 0; i < count; ++i)

    threadWait(750);
    if (count == 1 &&
        drawn != nullptr &&
        sUno->getForcePlayRule() != 0 &&
        sUno->isLegalToPlay(drawn)) {
        // Player drew one card by itself, the drawn card
        // can be played immediately if it's legal to play
        if (sAuto || now != Player::YOU) {
            play(index, sAI->calcBestColor4NowPlayer());
        } // if (sAuto || now != Player::YOU)
        else if (sUno->getForcePlayRule() == 1) {
            // Store index value as global value. This value
            // will be used after the wild color determined.
            sSelectedIdx = index;
            setStatus(STAT_ASK_KEEP_PLAY);
        } // else if (sUno->getForcePlayRule() == 1)
        else if (!drawn->isWild()) {
            // Force play a non-wild card
            play(index, drawn->color);
        } // else if (!drawn->isWild())
        else if (sUno->getStackRule() == 2 &&
            drawn->content == WILD_DRAW4) {
            // Force play a Wild +4 card, but do not change the next color
            play(index, sUno->lastColor());
        } // else if (sUno->getStackRule() == 2 && ...)
        else {
            // Force play a Wild / Wild +4 card, and change the next color
            sSelectedIdx = index;
            setStatus(STAT_WILD_COLOR);
        } // else
    } // if (count == 1 && ...)
    else {
        refreshScreen(i18n->act_pass(now), 0x00);
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
    refreshScreen(i18n->info_challenge(challenger, now,
        sUno->next2lastColor()), (1 << now) | 0x40);
    threadWait(1500);
    if (challengeSuccess) {
        // Challenge success, who played [wild +4] draws 4 cards
        refreshScreen(i18n->info_challengeSuccess(now), 0x00);
        threadWait(1500);
        draw(4, /* force */ true);
    } // if (challengeSuccess)
    else {
        // Challenge failure, challenger draws 6 cards
        refreshScreen(i18n->info_challengeFailure(challenger), 0x00);
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
                else if (930 <= x && x <= 1050) {
                    // Speed = 1
                    sSpeed = 1;
                    setStatus(sStatus);
                } // else if (930 <= x && x <= 1050)
                else if (1110 <= x && x <= 1230) {
                    // Speed = 2
                    sSpeed = 2;
                    setStatus(sStatus);
                } // else if (1110 <= x && x <= 1230)
                else if (1290 <= x && x <= 1410) {
                    // Speed = 3
                    sSpeed = 3;
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1410)
            } // else if (270 <= y && y <= 450)
            else if (649 <= y && y <= 670 && sStatus != Player::YOU) {
                if (896 <= x && x <= 929) {
                    // Decrease initial cards
                    sUno->decreaseInitialCards();
                    setStatus(sStatus);
                } // if (896 <= x && x <= 929)
                else if (1358 <= x && x <= 1391) {
                    // Increase initial cards
                    sUno->increaseInitialCards();
                    setStatus(sStatus);
                } // else if (1358 <= x && x <= 1391)
            } // else if (649 <= y && y <= 670 && sStatus != Player::YOU)
            else if (699 <= y && y <= 720 && sStatus != Player::YOU) {
                if (896 <= x && x <= 963) {
                    // Game mode, <3P> button
                    sUno->setPlayers(3);
                    setStatus(sStatus);
                } // if (896 <= x && x <= 963)
                else if (1028 <= x && x <= 1095) {
                    // Game mode, <4P> button
                    sUno->setPlayers(4);
                    setStatus(sStatus);
                } // else if (1028 <= x && x <= 1095)
                else if (1159 <= x && x <= 1243) {
                    // Game mode, <7-0> button
                    sUno->setSevenZeroRule(true);
                    setStatus(sStatus);
                } // else if (1159 <= x && x <= 1243)
                else if (1290 <= x && x <= 1391) {
                    // Game mode, <2vs2> button
                    sUno->set2vs2(true);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1391)
            } // else if (699 <= y && y <= 720 && sStatus != Player::YOU)
            else if (749 <= y && y <= 770 && sStatus != Player::YOU) {
                if (896 <= x && x <= 997) {
                    // Force play, <KEEP> button
                    sUno->setForcePlayRule(0);
                    setStatus(sStatus);
                } // if (896 <= x && x <= 997)
                else if (1110 <= x && x <= 1194) {
                    // Force play, <ASK> button
                    sUno->setForcePlayRule(1);
                    setStatus(sStatus);
                } // else if (1110 <= x && x <= 1194)
                else if (1290 <= x && x <= 1391) {
                    // Force play, <PLAY> button
                    sUno->setForcePlayRule(2);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1391)
            } // else if (749 <= y && y <= 770 && sStatus != Player::YOU)
            else if (799 <= y && y <= 820 && sStatus != Player::YOU) {
                if (896 <= x && x <= 997) {
                    // stacking, <NONE> button
                    sUno->setStackRule(0);
                    setStatus(sStatus);
                } // if (896 <= x && x <= 997)
                else if (1110 <= x && x <= 1177) {
                    // stacking, <+2> button
                    sUno->setStackRule(1);
                    setStatus(sStatus);
                } // else if (1110 <= x && x <= 1177)
                else if (1290 <= x && x <= 1391) {
                    // stacking, <+2+4> button
                    sUno->setStackRule(2);
                    setStatus(sStatus);
                } // else if (1290 <= x && x <= 1391)
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
            if (sStatus == Player::YOU && !sAuto) {
                sAuto = true;
                setStatus(sStatus);
            } // if (sStatus == Player::YOU && !sAuto)
            else if (sStatus == STAT_WELCOME) {
                // [UPDATE] When in welcome screen, change to <LOAD> button
                QString replayName = QFileDialog::getOpenFileName(
                    /* parent  */ this,
                    /* caption */ "Open...",
                    /* dir     */ QApplication::applicationDirPath(),
                    /* filter  */ "Uno replay files (*.sav)"
                ); // getOpenFileName()

                if (!replayName.isNull()) {
                    loadReplay(replayName);
                } // if (!replayName.isNull())
            } // else if (sStatus == STAT_WELCOME)
            else if (sStatus == STAT_GAME_OVER && !sGameSaved) {
                // [UPDATE] When game over, change to <SAVE> button
                QString replayName = sUno->save();

                sGameSaved = !replayName.isEmpty();
                refreshScreen(i18n->info_save(replayName.toLatin1()), 0x20);
            } // else if (sStatus == STAT_GAME_OVER && !sGameSaved)
        } // else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580)
        else switch (sStatus) {
        case STAT_WELCOME:
            if (360 <= y && y <= 540 && 740 <= x && x <= 860) {
                // UNO button, start a new game
                sGameSaved = false;
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
                auto hand = sUno->getHandCardsOf(Player::YOU);
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
                        if (!card->isWild() || size < 2) {
                            play(index);
                        } // if (!card->isWild() || size < 2)
                        else if (sUno->getStackRule() != 2 ||
                            card->content != WILD_DRAW4) {
                            setStatus(STAT_WILD_COLOR);
                        } // else if (sUno->getStackRule() != 2 || ...)
                        else {
                            play(index, sUno->lastColor());
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

        case STAT_ASK_KEEP_PLAY:
            if (310 < x && x < 500 && 405 < y && y < 500) {
                // No button, keep the drawn card
                sSelectedIdx = -1;
                setStatus(STAT_IDLE);
                refreshScreen(i18n->act_pass(Player::YOU));
                threadWait(750);
                setStatus(sUno->switchNow());
            } // if (310 < x && x < 500 && 405 < y && y < 500)
            else if (310 < x && x < 500 && 310 < y && y < 405) {
                // YES button, play the drawn card
                auto hand = sUno->getHandCardsOf(Player::YOU);
                Card* card = hand[sSelectedIdx];

                if (!card->isWild() || hand.size() < 2) {
                    play(sSelectedIdx, card->color);
                } // if (!card->isWild() || hand.size() < 2)
                else if (sUno->getStackRule() != 2 ||
                    card->content != WILD_DRAW4) {
                    setStatus(STAT_WILD_COLOR);
                } // else if (sUno->getStackRule() != 2 || ...)
                else {
                    play(sSelectedIdx, sUno->lastColor());
                } // else
            } // else if (310 < x && x < 500 && 310 < y && y < 405)
            else if (700 <= y && y <= 880) {
                auto hand = sUno->getHandCardsOf(Player::YOU);
                int size = int(hand.size());
                int width = 44 * size + 76;
                int startX = 800 - width / 2;
                Card* card = hand[sSelectedIdx];
                int index = qMin((x - startX) / 44, size - 1);

                if (!(startX <= x && x <= startX + width)) {
                    break; // case STAT_ASK_KEEP_PLAY
                } // if (!(startX <= x && x <= startX + width))

                if (index != sSelectedIdx) {
                    break; // case STAT_ASK_KEEP_PLAY
                } // if (index != sSelectedIdx)

                if (!card->isWild() || hand.size() < 2) {
                    play(sSelectedIdx, card->color);
                } // if (!card->isWild() || hand.size() < 2)
                else if (sUno->getStackRule() != 2 ||
                    card->content != WILD_DRAW4) {
                    setStatus(STAT_WILD_COLOR);
                } // else if (sUno->getStackRule() != 2 || ...)
                else {
                    play(sSelectedIdx, sUno->lastColor());
                } // else
            } // else if (700 <= y && y <= 880)
            break; // case STAT_ASK_KEEP_PLAY

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
                sGameSaved = false;
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
 * Load a existed replay file.
 *
 * @param replayName Provide the file name of your replay.
 */
void Main::loadReplay(const QString& replayName) {
    if (sUno->loadReplay(replayName)) {
        static int x[] = { 740, 160, 740, 1320 };
        static int y[] = { 670, 360, 50, 360 };
        int params[] = { 0, 0, 0 };

        setStatus(STAT_IDLE);
        while (true) {
            Card* card;
            QString cmd;
            int a, b, c, d, i, size, width;

            if ((cmd = sUno->forwardReplay(params)) == "ST") {
                refreshScreen(i18n->info_ready());
                threadWait(1000);
            } // if ((cmd = sUno->forwardReplay(params)) == "ST")
            else if (cmd == "DR") {
                auto h = sUno->getHandCardsOf(a = params[0]);

                card = sUno->findCardById(params[1]);
                i = std::lower_bound(h.begin(), h.end(), card) - h.begin();
                size = int(h.size());
                sLayer[0].startLeft = 338;
                sLayer[0].startTop = 360;
                sLayer[0].elem = card->image;
                if (a == Player::COM1) {
                    width = 44 * qMin(size, 13) + 136;
                    sLayer[0].endLeft = 20 + i / 13 * 44;
                    sLayer[0].endTop = 450 - width / 2 + i % 13 * 44;
                } // if (a == Player::COM1)
                else if (a == Player::COM2) {
                    width = 44 * size + 76;
                    sLayer[0].endLeft = 800 - width / 2 + 44 * i;
                    sLayer[0].endTop = 20;
                } // else if (a == Player::COM2)
                else if (a == Player::COM3) {
                    width = 44 * qMin(size, 13) + 136;
                    sLayer[0].endLeft = 1460 - i / 13 * 44;
                    sLayer[0].endTop = 450 - width / 2 + i % 13 * 44;
                } // else if (a == Player::COM3)
                else {
                    width = 44 * size + 76;
                    sLayer[0].endLeft = 800 - width / 2 + 44 * i;
                    sLayer[0].endTop = 700;
                } // else

                // Animation
                sSoundPool->play(SoundPool::SND_DRAW);
                animate(1, sLayer);
                refreshScreen(i18n->act_drawCard(a, card->name));
                threadWait(300);
            } // else if (cmd == "DR")
            else if (cmd == "PL") {
                auto h = sUno->getHandCardsOf(a = params[0]);

                card = sUno->findCardById(params[1]);
                i = std::lower_bound(h.begin(), h.end(), card) - h.begin();
                size = int(h.size()) + 1;
                threadWait(750);
                sSoundPool->play(SoundPool::SND_PLAY);
                sLayer[0].elem = card->image;
                if (a == Player::COM1) {
                    width = 44 * qMin(size, 13) + 136;
                    sLayer[0].startLeft = 160 + i / 13 * 44;
                    sLayer[0].startTop = 450 - width / 2 + i % 13 * 44;
                } // if (a == Player::COM1)
                else if (a == Player::COM2) {
                    width = 44 * size + 76;
                    sLayer[0].startLeft = 800 - width / 2 + 44 * i;
                    sLayer[0].startTop = 50;
                } // else if (a == Player::COM2)
                else if (a == Player::COM3) {
                    width = 44 * qMin(size, 13) + 136;
                    sLayer[0].startLeft = 1320 - i / 13 * 44;
                    sLayer[0].startTop = 450 - width / 2 + i % 13 * 44;
                } // else if (a == Player::COM3)
                else {
                    width = 44 * size + 76;
                    sLayer[0].startLeft = 800 - width / 2 + 44 * i;
                    sLayer[0].startTop = 680;
                } // else

                // Animation
                sLayer[0].endLeft = 1118;
                sLayer[0].endTop = 360;
                animate(1, sLayer);
                if (size == 2)
                    sSoundPool->play(SoundPool::SND_UNO);
                refreshScreen(i18n->act_playCard(a, card->name));
                threadWait(750);
            } // else if (cmd == "PL")
            else if (cmd == "DF") {
                a = params[0];
                refreshScreen(i18n->info_cannotDraw(a, Uno::MAX_HOLD_CARDS));
                threadWait(750);
            } // else if (cmd == "DF")
            else if (cmd == "CH") {
                a = params[0];
                threadWait(750);
                if (sUno->challenge(a)) {
                    refreshScreen(i18n->info_challengeSuccess(a));
                } // if (sUno->challenge(a))
                else {
                    b = sUno->getNext();
                    refreshScreen(i18n->info_challengeFailure(b));
                } // else

                threadWait(750);
            } // else if (cmd == "CH")
            else if (cmd == "SW") {
                a = params[0];
                b = params[1];

                // Animation
                sHideFlag = (1 << a) | (1 << b);
                refreshScreen(i18n->info_7_swap(a, b));
                sLayer[0].elem = sLayer[1].elem = sUno->getBackImage();
                sLayer[0].startLeft = x[a];
                sLayer[0].startTop = y[a];
                sLayer[0].endLeft = x[b];
                sLayer[0].endTop = y[b];
                sLayer[1].startLeft = x[b];
                sLayer[1].startTop = y[b];
                sLayer[1].endLeft = x[a];
                sLayer[1].endTop = y[a];
                animate(2, sLayer);
                sHideFlag = 0x00;
                refreshScreen(i18n->info_7_swap(a, b));
                threadWait(750);
            } // else if (cmd == "SW")
            else if (cmd == "CY") {
                // Animation
                sHideFlag = 0x0f;
                refreshScreen(i18n->info_0_rotate());
                a = sUno->getNow();
                b = sUno->getNext();
                c = sUno->getOppo();
                d = sUno->getPrev();
                sLayer[0].elem = sUno->getBackImage();
                sLayer[0].startLeft = x[a];
                sLayer[0].startTop = y[a];
                sLayer[0].endLeft = x[b];
                sLayer[0].endTop = y[b];
                sLayer[1].elem = sUno->getBackImage();
                sLayer[1].startLeft = x[b];
                sLayer[1].startTop = y[b];
                sLayer[1].endLeft = x[c];
                sLayer[1].endTop = y[c];
                if (sUno->getPlayers() == 3) {
                    sLayer[2].elem = sUno->getBackImage();
                    sLayer[2].startLeft = x[c];
                    sLayer[2].startTop = y[c];
                    sLayer[2].endLeft = x[a];
                    sLayer[2].endTop = y[a];
                    animate(3, sLayer);
                } // if (sUno->getPlayers() == 3)
                else {
                    sLayer[2].elem = sUno->getBackImage();
                    sLayer[2].startLeft = x[c];
                    sLayer[2].startTop = y[c];
                    sLayer[2].endLeft = x[d];
                    sLayer[2].endTop = y[d];
                    sLayer[3].elem = sUno->getBackImage();
                    sLayer[3].startLeft = x[d];
                    sLayer[3].startTop = y[d];
                    sLayer[3].endLeft = x[a];
                    sLayer[3].endTop = y[a];
                    animate(4, sLayer);
                } // else

                sHideFlag = 0x00;
                refreshScreen(i18n->info_0_rotate());
                threadWait(750);
            } // else if (cmd == "CY")
            else {
                break;
            } // else
        } // while (true)

        if ((sUno->is2vs2() && sUno->getHandCardsOf(Player::COM2).empty()) ||
            sUno->getHandCardsOf(Player::YOU).empty()) {
            sSoundPool->play(SoundPool::SND_WIN);
        } // if ((sUno->is2vs2() && ...)
        else {
            sSoundPool->play(SoundPool::SND_LOSE);
        } // else

        threadWait(3000);
        setStatus(STAT_WELCOME);
    } // if (sUno->loadReplay(replayName))
    else {
        refreshScreen("[Y]Failed to load " + replayName);
    } // else
} // loadReplay(const QString&)

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
            /* dw[3] = force play switch  */ sUno->getForcePlayRule(),
            /* dw[4] = seven zero switch  */ sUno->isSevenZeroRule() ? 1 : 0,
            /* dw[5] = stacking switch    */ sUno->getStackRule(),
            /* dw[6] = snd switch         */ sSoundPool->isEnabled() ? 1 : 0,
            /* dw[7] = bgm switch         */ sMediaPlay->volume(),
            /* dw[8] = initial cards      */ sUno->getInitialCards(),
            /* dw[9] = 2vs2 switch        */ sUno->is2vs2() ? 1 : 0,
            /* dw[10] = speed             */ sSpeed
        }; // dw[]

        // Fill unused bits with 1
        memset(dw + 11, -1, (63 - 11) * sizeof(int));

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