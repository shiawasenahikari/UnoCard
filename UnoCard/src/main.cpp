////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <AI.h>
#include <Uno.h>
#include <string>
#include <vector>
#include <Card.h>
#include <cstdlib>
#include <cstring>
#include <fstream>
#include <Color.h>
#include <Player.h>
#include <Content.h>
#include <QFileInfo>
#include <SoundPool.h>
#include <QApplication>
#include <QMediaPlayer>
#include <QMediaPlaylist>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>

// Constants
static const int STAT_IDLE = 0x1111;
static const int STAT_WELCOME = 0x2222;
static const int STAT_NEW_GAME = 0x3333;
static const int STAT_GAME_OVER = 0x4444;
static const int STAT_WILD_COLOR = 0x5555;
static const int STAT_SEVEN_TARGET = 0x7777;
static const cv::Scalar RGB_RED = CV_RGB(0xFF, 0x55, 0x55);
static const cv::Scalar RGB_BLUE = CV_RGB(0x55, 0x55, 0xFF);
static const cv::Scalar RGB_GREEN = CV_RGB(0x55, 0xAA, 0x55);
static const cv::Scalar RGB_WHITE = CV_RGB(0xCC, 0xCC, 0xCC);
static const cv::Scalar RGB_YELLOW = CV_RGB(0xFF, 0xAA, 0x11);
static const std::string NAME[] = { "YOU", "WEST", "NORTH", "EAST" };
static const enum cv::HersheyFonts FONT_SANS = cv::FONT_HERSHEY_DUPLEX;
static const std::string CL[] = { "", "RED", "BLUE", "GREEN", "YELLOW" };
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

// Global Variables
static AI sAI;
static Uno* sUno;
static bool sAuto;
static int sScore;
static int sStatus;
static int sWinner;
static int sDrawCount;
static bool sAIRunning;
static cv::Mat sScreen;
static Card* sDrawnCard;
static bool sImmPlayAsk;
static bool sChallenged;
static bool sChallengeAsk;
static bool sAdjustOptions;
static Card* sSelectedCard;
static SoundPool* sSoundPool;
static QMediaPlayer* sMediaPlay;
static QMediaPlaylist* sMediaList;

// Functions
static void easyAI();
static void hardAI();
static void swapWith(int whom);
static void onStatusChanged(int status);
static void play(int index, Color color = NONE);
static void draw(int count = 1, bool force = false);
static void refreshScreen(const std::string& message);
static void onChallengeChance(bool challenged = true);
static void animate(cv::Mat elem, int x1, int y1, int x2, int y2);
static void onMouse(int event, int x, int y, int flags, void* param);

/**
 * Defines the entry point for the console application.
 */
int main(int argc, char* argv[]) {
    int len, dw[8], hash;
    std::ifstream reader;
    QApplication a(argc, argv);
    char header[9] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    // Preparations
    sUno = Uno::getInstance();
    sSoundPool = new SoundPool;
    sMediaPlay = new QMediaPlayer;
    sMediaList = new QMediaPlaylist;
    sMediaList->setPlaybackMode(QMediaPlaylist::Loop);
    sMediaList->addMedia(QUrl::fromLocalFile(
        QFileInfo("resource/bgm.mp3").absoluteFilePath()));
    sMediaPlay->setPlaylist(sMediaList);
    sMediaPlay->setVolume(50);
    reader = std::ifstream("UnoCard.stat", std::ios::in | std::ios::binary);
    if (!reader.fail()) {
        // Using statistics data in UnoCard.stat file.
        reader.seekg(0, std::ios::end);
        len = int(reader.tellg());
        reader.seekg(0, std::ios::beg);
        if (len == 8 + 8 * sizeof(int)) {
            reader.read(header, 8);
            reader.read((char*)dw, 8 * sizeof(int));
            for (hash = 0, len = 0; len < 7; ++len) {
                hash = 31 * hash + dw[len];
            } // for (hash = 0, len = 0; len < 7; ++len)

            if (strcmp(header, FILE_HEADER) == 0 && hash == dw[7]) {
                // File verification success
                if (dw[0] > 9999) dw[0] = 9999;
                else if (dw[0] < -999) dw[0] = -999;
                sScore = dw[0];
                sUno->setPlayers(dw[1]);
                sUno->setDifficulty(dw[2]);
                sUno->setSevenZeroRule(dw[3] != 0);
                sUno->setDraw2StackRule(dw[4] != 0);
                sSoundPool->setEnabled(dw[5] != 0);
                sMediaPlay->setVolume(dw[6]);
            } // if (strcmp(header, FILE_HEADER) == 0 && hash == dw[7])
        } // if (len == 8 + 8 * sizeof(int))

        reader.close();
    } // if (!reader.fail())

    sWinner = Player::YOU;
    sStatus = STAT_WELCOME;
    sScreen = sUno->getBackground().clone();
    cv::namedWindow("Uno");
    onStatusChanged(sStatus);
    sMediaPlay->play();
    cv::setMouseCallback("Uno", onMouse, nullptr);
    for (;;) {
        cv::waitKey(0); // prevent from blocking main thread
    } // for (;;)
} // main(int, char*[])

/**
 * AI Strategies (Difficulty: EASY).
 */
static void easyAI() {
    int idxBest, now;
    Color bestColor[1];

    sAIRunning = true;
    while (sStatus == Player::COM1
        || sStatus == Player::COM2
        || sStatus == Player::COM3
        || sStatus == Player::YOU && sAuto) {
        if (sChallengeAsk) {
            onChallengeChance(sAI.needToChallenge(sStatus));
        } // if (sChallengeAsk)
        else {
            now = sStatus;
            sStatus = STAT_IDLE; // block mouse click events when idle
            idxBest = sAI.easyAI_bestCardIndex4NowPlayer(
                /* drawnCard */ sImmPlayAsk ? sDrawnCard : nullptr,
                /* outColor  */ bestColor
            ); // idxBest = sAI.easyAI_bestCardIndex4NowPlayer()

            if (idxBest >= 0) {
                // Found an appropriate card to play
                sImmPlayAsk = false;
                play(idxBest, bestColor[0]);
            } // if (idxBest >= 0)
            else {
                // No appropriate cards to play, or no card is legal to play
                if (sImmPlayAsk) {
                    sImmPlayAsk = false;
                    refreshScreen(NAME[now] + ": Pass");
                    cv::waitKey(750);
                    sStatus = sUno->switchNow();
                    onStatusChanged(sStatus);
                } // if (sImmPlayAsk)
                else {
                    draw();
                } // else
            } // else
        } // else
    } // while (sStatus == Player::COM1 || ...)

    sAIRunning = false;
} // easyAI()

/**
 * AI Strategies (Difficulty: HARD).
 */
static void hardAI() {
    int idxBest, now;
    Color bestColor[1];

    sAIRunning = true;
    while (sStatus == Player::COM1
        || sStatus == Player::COM2
        || sStatus == Player::COM3
        || sStatus == Player::YOU && sAuto) {
        if (sChallengeAsk) {
            onChallengeChance(sAI.needToChallenge(sStatus));
        } // if (sChallengeAsk)
        else {
            now = sStatus;
            sStatus = STAT_IDLE; // block mouse click events when idle
            idxBest = sAI.hardAI_bestCardIndex4NowPlayer(
                /* drawnCard */ sImmPlayAsk ? sDrawnCard : nullptr,
                /* outColor  */ bestColor
            ); // idxBest = sAI.hardAI_bestCardIndex4NowPlayer()

            if (idxBest >= 0) {
                // Found an appropriate card to play
                sImmPlayAsk = false;
                play(idxBest, bestColor[0]);
            } // if (idxBest >= 0)
            else {
                // No appropriate cards to play, or no card is legal to play
                if (sImmPlayAsk) {
                    sImmPlayAsk = false;
                    refreshScreen(NAME[now] + ": Pass");
                    cv::waitKey(750);
                    sStatus = sUno->switchNow();
                    onStatusChanged(sStatus);
                } // if (sImmPlayAsk)
                else {
                    draw();
                } // else
            } // else
        } // else
    } // while (sStatus == Player::COM1 || ...)

    sAIRunning = false;
} // hardAI()

/**
 * Triggered when the value of global value [sStatus] changed.
 *
 * @param status New status value.
 */
static void onStatusChanged(int status) {
    cv::Rect rect;
    cv::Size axes;
    Card* next2last;
    cv::Point center;
    std::string message;
    std::vector<Card*> recent;

    switch (status) {
    case STAT_WELCOME:
        refreshScreen(sAdjustOptions ? "SPECIAL RULES" :
            "WELCOME TO UNO CARD GAME, CLICK UNO TO START");
        break; // case STAT_WELCOME

    case STAT_NEW_GAME:
        // New game
        sUno->start();
        sSelectedCard = nullptr;
        refreshScreen("GET READY");
        cv::waitKey(2000);
        switch (sUno->getRecent().at(0)->content) {
        case DRAW2:
            // If starting with a [+2], let dealer draw 2 cards.
            draw(2, /* force */ true);
            break; // case DRAW2

        case SKIP:
            // If starting with a [skip], skip dealer's turn.
            refreshScreen(NAME[sUno->getNow()] + ": Skipped");
            cv::waitKey(1500);
            sStatus = sUno->switchNow();
            onStatusChanged(sStatus);
            break; // case SKIP

        case REV:
            // If starting with a [reverse], change the action
            // sequence to COUNTER CLOCKWISE.
            sUno->switchDirection();
            refreshScreen("Direction changed");
            cv::waitKey(1500);
            sStatus = sUno->getNow();
            onStatusChanged(sStatus);
            break; // case REV

        default:
            // Otherwise, go to dealer's turn.
            sStatus = sUno->getNow();
            onStatusChanged(sStatus);
            break; // default
        } // switch (sUno->getRecent().back()->content)
        break; // case STAT_NEW_GAME

    case Player::YOU:
        // Your turn, select a hand card to play, or draw a card
        if (sAuto) {
            if (!sAIRunning) {
                if (sUno->getDifficulty() == Uno::LV_EASY) {
                    easyAI();
                } // if (sUno->getDifficulty() == Uno::LV_EASY)
                else {
                    hardAI();
                } // else
            } // if (!sAIRunning)
        } // if (sAuto)
        else if (sImmPlayAsk) {
            sSelectedCard = sDrawnCard;
            message = "^ Play " + std::string(sDrawnCard->name) + "?";
            refreshScreen(message);
            rect = cv::Rect(338, 270, 121, 181);
            sUno->getBackground()(rect).copyTo(sScreen(rect));
            center = cv::Point(405, 315);
            axes = cv::Size(135, 135);

            // Draw YES button
            cv::ellipse(
                /* img        */ sScreen,
                /* center     */ center,
                /* axes       */ axes,
                /* angle      */ 0,
                /* startAngle */ 0,
                /* endAngle   */ -180,
                /* color      */ RGB_GREEN,
                /* thickness  */ -1,
                /* lineType   */ cv::LINE_AA
            ); // cv::ellipse()
            cv::putText(
                /* img       */ sScreen,
                /* text      */ "YES",
                /* org       */ cv::Point(346, 295),
                /* fontFace  */ FONT_SANS,
                /* fontScale */ 2.0,
                /* color     */ RGB_WHITE,
                /* thickness */ 2
            ); // cv::putText()

            // Draw NO button
            cv::ellipse(
                /* img        */ sScreen,
                /* center     */ center,
                /* axes       */ axes,
                /* angle      */ 0,
                /* startAngle */ 0,
                /* endAngle   */ 180,
                /* color      */ RGB_RED,
                /* thickness  */ -1,
                /* lineType   */ cv::LINE_AA
            ); // cv::ellipse()
            cv::putText(
                /* img       */ sScreen,
                /* text      */ "NO",
                /* org       */ cv::Point(360, 378),
                /* fontFace  */ FONT_SANS,
                /* fontScale */ 2.0,
                /* color     */ RGB_WHITE,
                /* thickness */ 2
            ); // cv::putText()

            // Show screen
            imshow("Uno", sScreen);
        } // else if (sImmPlayAsk)
        else if (sChallengeAsk) {
            recent = sUno->getRecent();
            next2last = recent.at(recent.size() - 2);
            message = "^ Do you think "
                + NAME[sUno->getNow()] + " still has "
                + CL[next2last->getRealColor()] + "?";
            refreshScreen(message);
            rect = cv::Rect(338, 270, 121, 181);
            sUno->getBackground()(rect).copyTo(sScreen(rect));
            center = cv::Point(405, 315);
            axes = cv::Size(135, 135);

            // Draw YES button
            cv::ellipse(
                /* img        */ sScreen,
                /* center     */ center,
                /* axes       */ axes,
                /* angle      */ 0,
                /* startAngle */ 0,
                /* endAngle   */ -180,
                /* color      */ RGB_GREEN,
                /* thickness  */ -1,
                /* lineType   */ cv::LINE_AA
            ); // cv::ellipse()
            cv::putText(
                /* img       */ sScreen,
                /* text      */ "YES",
                /* org       */ cv::Point(346, 295),
                /* fontFace  */ FONT_SANS,
                /* fontScale */ 2.0,
                /* color     */ RGB_WHITE,
                /* thickness */ 2
            ); // cv::putText()

            // Draw NO button
            cv::ellipse(
                /* img        */ sScreen,
                /* center     */ center,
                /* axes       */ axes,
                /* angle      */ 0,
                /* startAngle */ 0,
                /* endAngle   */ 180,
                /* color      */ RGB_RED,
                /* thickness  */ -1,
                /* lineType   */ cv::LINE_AA
            ); // cv::ellipse()
            cv::putText(
                /* img       */ sScreen,
                /* text      */ "NO",
                /* org       */ cv::Point(360, 378),
                /* fontFace  */ FONT_SANS,
                /* fontScale */ 2.0,
                /* color     */ RGB_WHITE,
                /* thickness */ 2
            ); // cv::putText()

            // Show screen
            imshow("Uno", sScreen);
        } // else if (sChallengeAsk)
        else if (sUno->legalCardsCount4NowPlayer() == 0) {
            if (sDrawCount == 0) {
                message = "No card can be played... Draw a card from deck";
            } // if (sDrawCount == 0)
            else {
                message = "No +2 card to stack... Draw "
                    + std::to_string(sDrawCount) + " cards from deck";
            } // else

            refreshScreen(message);
        } // else if (sUno->legalCardsCount4NowPlayer() == 0)
        else if (sSelectedCard == nullptr) {
            if (sDrawCount == 0) {
                message = "Select a card to play, or draw a card from deck";
            } // if (sDrawCount == 0)
            else {
                message = "Stack a +2 card, or draw "
                    + std::to_string(sDrawCount) + " cards from deck";
            } // else

            refreshScreen(message);
        } // else if (sSelectedCard == nullptr)
        else {
            refreshScreen("Click again to play");
        } // else
        break; // case Player::YOU

    case STAT_WILD_COLOR:
        // Need to specify the following legal color after played a
        // wild card. Draw color sectors in the center of screen
        refreshScreen("^ Specify the following legal color");
        rect = cv::Rect(338, 270, 121, 181);
        sUno->getBackground()(rect).copyTo(sScreen(rect));
        center = cv::Point(405, 315);
        axes = cv::Size(135, 135);

        // Draw blue sector
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ 0,
            /* startAngle */ 0,
            /* endAngle   */ -90,
            /* color      */ RGB_BLUE,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()

        // Draw green sector
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ 0,
            /* startAngle */ 0,
            /* endAngle   */ 90,
            /* color      */ RGB_GREEN,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()

        // Draw red sector
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ 180,
            /* startAngle */ 0,
            /* endAngle   */ 90,
            /* color      */ RGB_RED,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()

        // Draw yellow sector
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ 180,
            /* startAngle */ 0,
            /* endAngle   */ -90,
            /* color      */ RGB_YELLOW,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()

        // Show screen
        imshow("Uno", sScreen);
        break; // case STAT_WILD_COLOR

    case STAT_SEVEN_TARGET:
        // In 7-0 rule, when someone put down a seven card, the player
        // must swap hand cards with another player immediately.
        if (sAuto || sUno->getNow() != Player::YOU) {
            // Seven-card is played by AI. Select target automatically.
            swapWith(sAI.bestSwapTarget4NowPlayer());
            break; // case STAT_SEVEN_TARGET
        } // if (sAuto || sUno->getNow() != Player::YOU)

        // Seven-card is played by you. Select target manually.
        refreshScreen("^ Specify the target to swap hand cards with");
        rect = cv::Rect(338, 270, 121, 181);
        sUno->getBackground()(rect).copyTo(sScreen(rect));
        center = cv::Point(405, 315);
        axes = cv::Size(135, 135);

        // Draw west sector (red)
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ 90,
            /* startAngle */ 0,
            /* endAngle   */ 120,
            /* color      */ RGB_RED,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()
        cv::putText(
            /* img       */ sScreen,
            /* text      */ "WEST",
            /* org       */ cv::Point(300, 350),
            /* fontFace  */ FONT_SANS,
            /* fontScale */ 1.0,
            /* color     */ RGB_WHITE,
            /* thickness */ 2
        ); // cv::putText()

        // Draw east sector (green)
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ 90,
            /* startAngle */ 0,
            /* endAngle   */ -120,
            /* color      */ RGB_GREEN,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()
        cv::putText(
            /* img       */ sScreen,
            /* text      */ "EAST",
            /* org       */ cv::Point(430, 350),
            /* fontFace  */ FONT_SANS,
            /* fontScale */ 1.0,
            /* color     */ RGB_WHITE,
            /* thickness */ 2
        ); // cv::putText()

        // Draw north sector (yellow)
        cv::ellipse(
            /* img        */ sScreen,
            /* center     */ center,
            /* axes       */ axes,
            /* angle      */ -150,
            /* startAngle */ 0,
            /* endAngle   */ 120,
            /* color      */ RGB_YELLOW,
            /* thickness  */ -1,
            /* lineType   */ cv::LINE_AA
        ); // cv::ellipse()
        cv::putText(
            /* img       */ sScreen,
            /* text      */ "NORTH",
            /* org       */ cv::Point(350, 270),
            /* fontFace  */ FONT_SANS,
            /* fontScale */ 1.0,
            /* color     */ RGB_WHITE,
            /* thickness */ 2
        ); // cv::putText()

        // Show screen
        imshow("Uno", sScreen);
        break; // case STAT_SEVEN_TARGET

    case Player::COM1:
    case Player::COM2:
    case Player::COM3:
        // AI players' turn
        if (!sAIRunning) {
            if (sUno->getDifficulty() == Uno::LV_EASY) {
                easyAI();
            } // if (sUno->getDifficulty() == Uno::LV_EASY)
            else {
                hardAI();
            } // else
        } // if (!sAIRunning)
        break; // case Player::COM1, Player::COM2, Player::COM3

    case STAT_GAME_OVER:
        // Game over
        if (sAdjustOptions) {
            message = "SPECIAL RULES";
        } // if (sAdjustOptions)
        else {
            message = "Your score is " + std::to_string(sScore)
                + ". Click the card deck to restart";
        } // else

        refreshScreen(message);
        break; // case STAT_GAME_OVER

    default:
        break; // default
    } // switch (status)
} // onStatusChanged(int)

/**
 * Refresh the screen display. The content of global variable [sScreen]
 * will be changed after calling this function.
 *
 * @param message Extra message to show.
 */
static void refreshScreen(const std::string& message) {
    cv::Rect roi;
    cv::Mat image;
    cv::Point point;
    std::string info;
    bool beChallenged;
    std::vector<Card*> hand;
    int i, remain, size, status, used, width;

    // Lock the value of global variable [sStatus]
    status = sStatus;

    // Clear
    sUno->getBackground().copyTo(sScreen);

    // Message area
    width = cv::getTextSize(message, FONT_SANS, 1.0, 1, nullptr).width;
    point = cv::Point(640 - width / 2, 480);
    cv::putText(sScreen, message, point, FONT_SANS, 1.0, RGB_WHITE);

    // Right-top corner: <QUIT> button
    point.x = 1140;
    point.y = 42;
    cv::putText(sScreen, "<QUIT>", point, FONT_SANS, 1.0, RGB_WHITE);

    // Right-bottom corner: <AUTO> button
    point.x = 1130;
    point.y = 700;
    cv::putText(
        /* img       */ sScreen,
        /* text      */ "<AUTO>",
        /* org       */ point,
        /* fontFace  */ FONT_SANS,
        /* fontScale */ 1.0,
        /* color     */ sAuto ? RGB_YELLOW : RGB_WHITE
    ); // cv::putText()

    // Left-bottom corner: <OPTIONS> button
    // Shows only when game is not in process
    if (status == STAT_WELCOME || status == STAT_GAME_OVER) {
        point.x = 20;
        point.y = 700;
        cv::putText(
            /* img       */ sScreen,
            /* text      */ "<OPTIONS>",
            /* org       */ point,
            /* fontFace  */ FONT_SANS,
            /* fontScale */ 1.0,
            /* color     */ sAdjustOptions ? RGB_YELLOW : RGB_WHITE
        ); // cv::putText()
    } // if (status == STAT_WELCOME || status == STAT_GAME_OVER)

    if (sAdjustOptions) {
        // Show special screen when configuring game options
        // BGM switch
        point.x = 60;
        point.y = 160;
        cv::putText(sScreen, "BGM", point, FONT_SANS, 1.0, RGB_WHITE);
        image = sMediaPlay->volume() > 0 ?
            sUno->findCard(RED, SKIP)->darkImg:
            sUno->findCard(RED, SKIP)->image;
        roi = cv::Rect(150, 60, 121, 181);
        image.copyTo(sScreen(roi), image);
        image = sMediaPlay->volume() > 0 ?
            sUno->findCard(GREEN, REV)->image:
            sUno->findCard(GREEN, REV)->darkImg;
        roi.x = 330;
        image.copyTo(sScreen(roi), image);

        // Sound effect switch
        point.x = 60;
        point.y = 350;
        cv::putText(sScreen, "SND", point, FONT_SANS, 1.0, RGB_WHITE);
        image = sSoundPool->isEnabled() ?
            sUno->findCard(RED, SKIP)->darkImg:
            sUno->findCard(RED, SKIP)->image;
        roi.x = 150;
        roi.y = 250;
        image.copyTo(sScreen(roi), image);
        image = sSoundPool->isEnabled() ?
            sUno->findCard(GREEN, REV)->image:
            sUno->findCard(GREEN, REV)->darkImg;
        roi.x = 330;
        image.copyTo(sScreen(roi), image);

        // [Level] option: easy / hard
        point.x = 640;
        point.y = 160;
        cv::putText(sScreen, "LEVEL", point, FONT_SANS, 1.0, RGB_WHITE);
        image = sUno->getLevelImage(
            /* level   */ Uno::LV_EASY,
            /* hiLight */ sUno->getDifficulty() == Uno::LV_EASY
        ); // image = sUno->getLevelImage()
        roi.x = 790;
        roi.y = 60;
        image.copyTo(sScreen(roi), image);
        image = sUno->getLevelImage(
            /* level   */ Uno::LV_HARD,
            /* hiLight */ sUno->getDifficulty() == Uno::LV_HARD
        ); // image = sUno->getLevelImage()
        roi.x = 970;
        image.copyTo(sScreen(roi), image);

        // [Players] option: 3 / 4
        point.x = 640;
        point.y = 350;
        cv::putText(sScreen, "PLAYERS", point, FONT_SANS, 1.0, RGB_WHITE);
        image = sUno->getPlayers() == 3 ?
            sUno->findCard(GREEN, NUM3)->image :
            sUno->findCard(GREEN, NUM3)->darkImg;
        roi.x = 790;
        roi.y = 250;
        image.copyTo(sScreen(roi), image);
        image = sUno->getPlayers() == 4 ?
            sUno->findCard(YELLOW, NUM4)->image :
            sUno->findCard(YELLOW, NUM4)->darkImg;
        roi.x = 970;
        image.copyTo(sScreen(roi), image);

        // Special rules
        // 7-0 Rule
        image = sUno->isSevenZeroRule() ?
            sUno->findCard(RED, NUM7)->image :
            sUno->findCard(RED, NUM7)->darkImg;
        roi.x = 240;
        roi.y = 520;
        image.copyTo(sScreen(roi), image);
        image = sUno->isSevenZeroRule() ?
            sUno->findCard(YELLOW, REV)->image :
            sUno->findCard(YELLOW, REV)->darkImg;
        roi.x += 45;
        image.copyTo(sScreen(roi), image);
        image = sUno->isSevenZeroRule() ?
            sUno->findCard(GREEN, NUM0)->image :
            sUno->findCard(GREEN, NUM0)->darkImg;
        roi.x += 45;
        image.copyTo(sScreen(roi), image);

        // +2 Stack Rule
        image = sUno->findCard(RED, DRAW2)->image;
        roi.x = 790;
        image.copyTo(sScreen(roi), image);
        image = sUno->isDraw2StackRule() ?
            sUno->findCard(BLUE, DRAW2)->image :
            sUno->findCard(BLUE, DRAW2)->darkImg;
        roi.x += 45;
        image.copyTo(sScreen(roi), image);
        image = sUno->isDraw2StackRule() ?
            sUno->findCard(GREEN, DRAW2)->image :
            sUno->findCard(GREEN, DRAW2)->darkImg;
        roi.x += 45;
        image.copyTo(sScreen(roi), image);
        image = sUno->isDraw2StackRule() ?
            sUno->findCard(YELLOW, DRAW2)->image :
            sUno->findCard(YELLOW, DRAW2)->darkImg;
        roi.x += 45;
        image.copyTo(sScreen(roi), image);
    } // if (sAdjustOptions)
    else if (status == STAT_WELCOME) {
        // For welcome screen, show the start button and your score
        image = sUno->getBackImage();
        roi = cv::Rect(580, 270, 121, 181);
        image.copyTo(sScreen(roi), image);
        point.x = 240;
        point.y = 620;
        cv::putText(sScreen, "SCORE", point, FONT_SANS, 1.0, RGB_WHITE);
        if (sScore < 0) {
            image = sUno->getColoredWildImage(NONE);
        } // if (sScore < 0)
        else {
            i = sScore / 1000;
            image = sUno->findCard(RED, Content(i))->image;
        } // else

        roi.x = 360;
        roi.y = 520;
        image.copyTo(sScreen(roi), image);
        i = abs(sScore / 100 % 10);
        image = sUno->findCard(BLUE, Content(i))->image;
        roi.x += 140;
        image.copyTo(sScreen(roi), image);
        i = abs(sScore / 10 % 10);
        image = sUno->findCard(GREEN, Content(i))->image;
        roi.x += 140;
        image.copyTo(sScreen(roi), image);
        i = abs(sScore % 10);
        image = sUno->findCard(YELLOW, Content(i))->image;
        roi.x += 140;
        image.copyTo(sScreen(roi), image);
    } // else if (status == STAT_WELCOME)
    else {
        // Center: card deck & recent played card
        image = sUno->getBackImage();
        roi = cv::Rect(338, 270, 121, 181);
        image.copyTo(sScreen(roi), image);
        hand = sUno->getRecent();
        size = int(hand.size());
        width = 45 * size + 75;
        roi.x = 792 - width / 2;
        roi.y = 270;
        for (Card* recent : hand) {
            if (recent->content == WILD) {
                image = sUno->getColoredWildImage(recent->getRealColor());
            } // if (recent->content == WILD)
            else if (recent->content == WILD_DRAW4) {
                image = sUno->getColoredWildDraw4Image(recent->getRealColor());
            } // else if (recent->content == WILD_DRAW4)
            else {
                image = recent->image;
            } // else

            image.copyTo(sScreen(roi), image);
            roi.x += 45;
        } // for (Card* recent : hand)

        // Left-top corner: remain / used
        point.x = 20;
        point.y = 42;
        remain = sUno->getDeckCount();
        used = sUno->getUsedCount();
        info = "Remain/Used: "
            + std::to_string(remain) + "/" + std::to_string(used);
        cv::putText(sScreen, info, point, FONT_SANS, 1.0, RGB_WHITE);

        // Left-center: Hand cards of Player West (COM1)
        if (status == STAT_GAME_OVER && sWinner == Player::COM1) {
            // Played all hand cards, it's winner
            point.x = 51;
            point.y = 461;
            cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (status == STAT_GAME_OVER && sWinner == Player::COM1)
        else {
            hand = sUno->getPlayer(Player::COM1)->getHandCards();
            size = int(hand.size());
            roi.x = 20;
            roi.y = 290 - 20 * size;
            beChallenged = sChallenged && sUno->getNow() == Player::COM1;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained cards to everyone
                // when being challenged or game over
                for (Card* card : hand) {
                    image = card->image;
                    image.copyTo(sScreen(roi), image);
                    roi.y += 40;
                } // for (Card* card : hand)
            } // if (beChallenged || status == STAT_GAME_OVER)
            else {
                // Only show card backs in game process
                image = sUno->getBackImage();
                for (i = 0; i < size; ++i) {
                    image.copyTo(sScreen(roi), image);
                    roi.y += 40;
                } // for (i = 0; i < size; ++i)
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 47;
                point.y = 494;
                cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Top-center: Hand cards of Player North (COM2)
        if (status == STAT_GAME_OVER && sWinner == Player::COM2) {
            // Played all hand cards, it's winner
            point.x = 611;
            point.y = 121;
            cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (status == STAT_GAME_OVER && sWinner == Player::COM2)
        else {
            hand = sUno->getPlayer(Player::COM2)->getHandCards();
            size = int(hand.size());
            roi.x = (1205 - 45 * size) / 2;
            roi.y = 20;
            beChallenged = sChallenged && sUno->getNow() == Player::COM2;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained cards to everyone
                // when being challenged or game over
                for (Card* card : hand) {
                    image = card->image;
                    image.copyTo(sScreen(roi), image);
                    roi.x += 45;
                } // for (Card* card : hand)
            } // if (beChallenged || status == STAT_GAME_OVER)
            else {
                // Only show card backs in game process
                image = sUno->getBackImage();
                for (i = 0; i < size; ++i) {
                    image.copyTo(sScreen(roi), image);
                    roi.x += 45;
                } // for (i = 0; i < size; ++i)
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 500;
                point.y = 121;
                cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Right-center: Hand cards of Player East (COM3)
        if (status == STAT_GAME_OVER && sWinner == Player::COM3) {
            // Played all hand cards, it's winner
            point.x = 1170;
            point.y = 461;
            cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (status == STAT_GAME_OVER && sWinner == Player::COM3)
        else {
            hand = sUno->getPlayer(Player::COM3)->getHandCards();
            size = int(hand.size());
            roi.x = 1140;
            roi.y = 290 - 20 * size;
            beChallenged = sChallenged && sUno->getNow() == Player::COM3;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained cards to everyone
                // when being challenged or game over
                for (Card* card : hand) {
                    image = card->image;
                    image.copyTo(sScreen(roi), image);
                    roi.y += 40;
                } // for (Card* card : hand)
            } // if (beChallenged || status == STAT_GAME_OVER)
            else {
                // Only show card backs in game process
                image = sUno->getBackImage();
                for (i = 0; i < size; ++i) {
                    image.copyTo(sScreen(roi), image);
                    roi.y += 40;
                } // for (i = 0; i < size; ++i)
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 1166;
                point.y = 494;
                cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Bottom: Your hand cards
        if (status == STAT_GAME_OVER && sWinner == Player::YOU) {
            // Played all hand cards, it's winner
            point.x = 611;
            point.y = 621;
            cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (status == STAT_GAME_OVER && sWinner == Player::YOU)
        else {
            // Show your all hand cards
            hand = sUno->getPlayer(Player::YOU)->getHandCards();
            size = int(hand.size());
            roi.x = (1205 - 45 * size) / 2;
            for (Card* card : hand) {
                switch (status) {
                case Player::YOU:
                    if (sImmPlayAsk) {
                        if (card == sDrawnCard) {
                            image = card->image;
                            roi.y = 490;
                        } // if (card == sDrawnCard)
                        else {
                            image = card->darkImg;
                            roi.y = 520;
                        } // else
                    } // if (sImmPlayAsk)
                    else if (sChallengeAsk || sChallenged) {
                        image = card->darkImg;
                        roi.y = 520;
                    } // else if (sChallengeAsk || sChallenged)
                    else {
                        image = sUno->isLegalToPlay(card) ?
                            card->image :
                            card->darkImg;
                        roi.y = card == sSelectedCard ? 490 : 520;
                    } // else
                    break; // case Player::YOU

                case STAT_WILD_COLOR:
                    image = card->darkImg;
                    roi.y = card == sSelectedCard ? 490 : 520;
                    break; // case STAT_WILD_COLOR

                case STAT_GAME_OVER:
                    image = card->image;
                    roi.y = 520;
                    break; // case STAT_GAME_OVER

                default:
                    image = card->darkImg;
                    roi.y = 520;
                    break; // default
                } // switch (status)

                image.copyTo(sScreen(roi), image);
                roi.x += 45;
            } // for (Card* card : hand)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 720;
                point.y = 621;
                cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else
    } // else

    // Show screen
    imshow("Uno", sScreen);
} // refreshScreen(const std::string&)

/**
 * The player in action swap hand cards with another player.
 *
 * @param whom Swap with whom. Must be one of the following:
 *             Player::YOU, Player::COM1, Player::COM2, Player::COM3
 */
void swapWith(int whom) {
    int now = sUno->getNow();
    sStatus = STAT_IDLE;
    sUno->swap(now, whom);
    refreshScreen(NAME[now] + " swapped hands with " + NAME[whom]);
    cv::waitKey(1500);
    sStatus = sUno->switchNow();
    onStatusChanged(sStatus);
} // swapWith(int)

/**
 * The player in action play a card.
 *
 * @param index Play which card. Pass the corresponding card's index of the
 *              player's hand cards.
 * @param color Optional, available when the card to play is a wild card.
 *              Pass the specified following legal color.
 */
static void play(int index, Color color) {
    Card* card;
    cv::Rect roi;
    cv::Mat image;
    std::string message;
    int x1, y1, x2, now, size, recentSize, next;

    sStatus = STAT_IDLE; // block mouse click events when idle
    now = sUno->getNow();
    size = sUno->getPlayer(now)->getHandSize();
    card = sUno->play(now, index, color);
    sSelectedCard = nullptr;
    sSoundPool->play(SoundPool::SND_PLAY);
    if (card != nullptr) {
        image = card->image;
        switch (now) {
        case Player::COM1:
            x1 = 160;
            y1 = 290 - 20 * size + 40 * index;
            break; // case Player::COM1

        case Player::COM2:
            x1 = (1205 - 45 * size + 90 * index) / 2;
            y1 = 50;
            break; // case Player::COM2

        case Player::COM3:
            x1 = 1000;
            y1 = 290 - 20 * size + 40 * index;
            break; // case Player::COM3

        default:
            x1 = (1205 - 45 * size + 90 * index) / 2;
            y1 = 490;
            break; // default
        } // switch (now)

        recentSize = int(sUno->getRecent().size());
        x2 = (45 * recentSize + 1419) / 2;
        animate(image, x1, y1, x2, 270);
        if (size == 1) {
            // The player in action becomes winner when it played the
            // final card in its hand successfully
            if (now == Player::YOU) {
                sScore += sUno->getPlayer(Player::COM1)->getHandScore()
                    + sUno->getPlayer(Player::COM2)->getHandScore()
                    + sUno->getPlayer(Player::COM3)->getHandScore();
                if (sScore > 9999) sScore = 9999;
                sSoundPool->play(SoundPool::SND_WIN);
            } // if (now == Player::YOU)
            else {
                sScore -= sUno->getPlayer(Player::YOU)->getHandScore();
                if (sScore < -999) sScore = -999;
                sSoundPool->play(SoundPool::SND_LOSE);
            } // else

            sWinner = now;
            sStatus = STAT_GAME_OVER;
            onStatusChanged(sStatus);
        } // if (size == 1)
        else {
            // When the played card is an action card or a wild card,
            // do the necessary things according to the game rule
            if (size == 2) {
                sSoundPool->play(SoundPool::SND_UNO);
            } // if (size == 2)

            message = NAME[now];
            switch (card->content) {
            case DRAW2:
                next = sUno->switchNow();
                if (sUno->isDraw2StackRule()) {
                    sDrawCount += 2;
                    message += ": Let " + NAME[next] + " draw ";
                    message += std::to_string(sDrawCount) + " cards";
                    refreshScreen(message);
                    cv::waitKey(1500);
                    sStatus = next;
                    onStatusChanged(sStatus);
                } // if (sUno->isDraw2StackRule())
                else {
                    message += ": Let " + NAME[next] + " draw 2 cards";
                    refreshScreen(message);
                    cv::waitKey(1500);
                    draw(2, /* force */ true);
                } // else
                break; // case DRAW2

            case SKIP:
                next = sUno->switchNow();
                if (next == Player::YOU) {
                    message += ": Skip your turn";
                } // if (next == Player::YOU)
                else {
                    message += ": Skip " + NAME[next] + "'s turn";
                } // else

                refreshScreen(message);
                cv::waitKey(1500);
                sStatus = sUno->switchNow();
                onStatusChanged(sStatus);
                break; // case SKIP

            case REV:
                if (sUno->switchDirection() == Uno::DIR_LEFT) {
                    message += ": Change direction to CLOCKWISE";
                } // if (sUno->switchDirection() == Uno::DIR_LEFT)
                else {
                    message += ": Change direction to COUNTER CLOCKWISE";
                } // else

                refreshScreen(message);
                cv::waitKey(1500);
                sStatus = sUno->switchNow();
                onStatusChanged(sStatus);
                break; // case REV

            case WILD:
                message += ": Change the following legal color";
                refreshScreen(message);
                cv::waitKey(1500);
                sStatus = sUno->switchNow();
                onStatusChanged(sStatus);
                break; // case WILD

            case WILD_DRAW4:
                next = sUno->getNext();
                message += ": Let " + NAME[next] + " draw 4 cards";
                refreshScreen(message);
                cv::waitKey(1500);
                sStatus = next;
                sChallengeAsk = true;
                onStatusChanged(sStatus);
                break; // case WILD_DRAW4

            case NUM7:
                if (sUno->isSevenZeroRule()) {
                    message += ": " + std::string(card->name);
                    refreshScreen(message);
                    cv::waitKey(750);
                    sStatus = STAT_SEVEN_TARGET;
                    onStatusChanged(sStatus);
                    break; // case NUM7
                } // if (sUno->isSevenZeroRule())
                // else fall through

            case NUM0:
                if (sUno->isSevenZeroRule()) {
                    message += ": " + std::string(card->name);
                    refreshScreen(message);
                    cv::waitKey(750);
                    sUno->cycle();
                    refreshScreen("Hand cards transferred to next");
                    cv::waitKey(1500);
                    sStatus = sUno->switchNow();
                    onStatusChanged(sStatus);
                    break; // case NUM0
                } // if (sUno->isSevenZeroRule())
                // else fall through

            default:
                message += ": " + std::string(card->name);
                refreshScreen(message);
                cv::waitKey(1500);
                sStatus = sUno->switchNow();
                onStatusChanged(sStatus);
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
static void draw(int count, bool force) {
    cv::Mat image;
    std::string message;
    int i, index, now, size, x2, y2;

    if (sDrawCount > 0) {
        count = sDrawCount;
        sDrawCount = 0;
    } // if (sDrawCount > 0)

    sStatus = STAT_IDLE; // block mouse click events when idle
    now = sUno->getNow();
    sSelectedCard = nullptr;
    for (i = 0; i < count; ++i) {
        index = sUno->draw(now, force);
        if (index >= 0) {
            sDrawnCard = sUno->getPlayer(now)->getHandCards().at(index);
            size = sUno->getPlayer(now)->getHandSize();
            switch (now) {
            case Player::COM1:
                image = sUno->getBackImage();
                x2 = 20;
                y2 = 290 - 20 * size + 40 * index;
                if (count == 1) {
                    message = NAME[now] + ": Draw a card";
                } // if (count == 1)
                else {
                    message = NAME[now] + ": Draw "
                        + std::to_string(count) + " cards";
                } // else
                break; // case Player::COM1

            case Player::COM2:
                image = sUno->getBackImage();
                x2 = (1205 - 45 * size + 90 * index) / 2;
                y2 = 20;
                if (count == 1) {
                    message = NAME[now] + ": Draw a card";
                } // if (count == 1)
                else {
                    message = NAME[now] + ": Draw "
                        + std::to_string(count) + " cards";
                } // else
                break; // case Player::COM2

            case Player::COM3:
                image = sUno->getBackImage();
                x2 = 1140;
                y2 = 290 - 20 * size + 40 * index;
                if (count == 1) {
                    message = NAME[now] + ": Draw a card";
                } // if (count == 1)
                else {
                    message = NAME[now] + ": Draw "
                        + std::to_string(count) + " cards";
                } // else
                break; // case Player::COM3

            default:
                image = sDrawnCard->image;
                x2 = (1205 - 45 * size + 90 * index) / 2;
                y2 = 520;
                message = NAME[now] + ": Draw " + sDrawnCard->name;
                break; // default
            } // switch (now)

            sSoundPool->play(SoundPool::SND_DRAW);
            animate(image, 338, 270, x2, y2);
            refreshScreen(message);
            cv::waitKey(300);
        } // if (index >= 0)
        else {
            message = NAME[now]
                + " cannot hold more than "
                + std::to_string(Uno::MAX_HOLD_CARDS) + " cards";
            refreshScreen(message);
            break;
        } // else
    } // for (i = 0; i < count; ++i)

    cv::waitKey(750);
    if (count == 1 &&
        sDrawnCard != nullptr &&
        sUno->isLegalToPlay(sDrawnCard)) {
        // Player drew one card by itself, the drawn card
        // can be played immediately if it's legal to play
        sStatus = now;
        sImmPlayAsk = true;
        onStatusChanged(sStatus);
    } // if (count == 1 && ...)
    else {
        refreshScreen(NAME[now] + ": Pass");
        cv::waitKey(750);
        sStatus = sUno->switchNow();
        onStatusChanged(sStatus);
    } // else
} // draw(int, bool)

/**
 * Triggered on challenge chance. When a player played a [wild +4], the next
 * player can challenge its legality. Only when you have no cards that match
 * the previous played card's color, you can play a [wild +4].
 * Next player does not challenge: next player draw 4 cards;
 * Challenge success: current player draw 4 cards;
 * Challenge failure: next player draw 6 cards.
 *
 * @param challenged Whether the next player (challenger) challenged current
 *                   player(be challenged)'s [wild +4].
 */
static void onChallengeChance(bool challenged) {
    Card* next2last;
    bool draw4IsLegal;
    std::string message;
    int curr, challenger;
    Color colorBeforeDraw4;
    std::vector<Card*> recent;

    sStatus = STAT_IDLE; // block mouse click events when idle
    sChallenged = challenged;
    sChallengeAsk = false;
    if (challenged) {
        curr = sUno->getNow();
        challenger = sUno->getNext();
        recent = sUno->getRecent();
        next2last = recent.at(recent.size() - 2);
        colorBeforeDraw4 = next2last->getRealColor();
        if (curr == Player::YOU) {
            message = NAME[challenger]
                + " doubted that you still have "
                + CL[colorBeforeDraw4];
        } // if (curr == Player::YOU)
        else {
            message = NAME[challenger]
                + " doubted that " + NAME[curr]
                + " still has " + CL[colorBeforeDraw4];
        } // else

        refreshScreen(message);
        cv::waitKey(1500);
        draw4IsLegal = true;
        for (Card* card : sUno->getPlayer(curr)->getHandCards()) {
            if (card->getRealColor() == colorBeforeDraw4) {
                // Found a card that matches the next-to-last recent
                // played card's color, [wild +4] is illegally used
                draw4IsLegal = false;
                break;
            } // if (card->getRealColor() == colorBeforeDraw4)
        } // for (Card* card : sUno->getPlayer(curr)->getHandCards())

        if (draw4IsLegal) {
            // Challenge failure, challenger draws 6 cards
            if (challenger == Player::YOU) {
                message = "Challenge failure, you draw 6 cards";
            } // if (challenger == Player::YOU)
            else {
                message = "Challenge failure, "
                    + NAME[challenger] + " draws 6 cards";
            } // else

            refreshScreen(message);
            cv::waitKey(1500);
            sChallenged = false;
            sUno->switchNow();
            draw(6, /* force */ true);
        } // if (draw4IsLegal)
        else {
            // Challenge success, who played [wild +4] draws 4 cards
            if (curr == Player::YOU) {
                message = "Challenge success, you draw 4 cards";
            } // if (curr == Player::YOU)
            else {
                message = "Challenge success, "
                    + NAME[curr] + " draws 4 cards";
            } // else

            refreshScreen(message);
            cv::waitKey(1500);
            sChallenged = false;
            draw(4, /* force */ true);
        } // else
    } // if (challenged)
    else {
        sUno->switchNow();
        draw(4, /* force */ true);
    } // else
} // onChallengeChance(bool)

/**
 * Do uniform motion for an object from somewhere to somewhere.
 * NOTE: This function does not draw the last frame. After animation,
 * you need to call refreshScreen() function to draw the last frame.
 *
 * @param elem Move which object.
 * @param x1   The object's start X coordinate.
 * @param y1   The object's start Y coordinate.
 * @param x2   The object's end X coordinate.
 * @param y2   The object's end Y coordinate.
 */
static void animate(cv::Mat elem, int x1, int y1, int x2, int y2) {
    int i;
    cv::Rect roi;
    cv::Mat canvas;

    roi = cv::Rect(x1, y1, elem.cols, elem.rows);
    canvas = sScreen.clone();
    elem.copyTo(canvas(roi), elem);
    imshow("Uno", canvas);
    cv::waitKey(30);
    for (i = 1; i < 5; ++i) {
        sScreen(roi).copyTo(canvas(roi));
        roi.x = x1 + (x2 - x1) * i / 5;
        roi.y = y1 + (y2 - y1) * i / 5;
        elem.copyTo(canvas(roi), elem);
        imshow("Uno", canvas);
        cv::waitKey(30);
    } // for (i = 1; i < 5; ++i)
} // animate(cv::Mat, cv::Point, cv::Point)

/**
 * Mouse event callback, used by OpenCV GUI windows. When a GUI window
 * registered this function as its mouse callback, once a mouse event
 * occurred in that window, this function will be called.
 *
 * @param event Which mouse event occurred, e.g. EVENT_LBUTTONDOWN
 * @param x     Mouse pointer's x-coordinate position when event occurred
 * @param y     Mouse pointer's y-coordinate position when event occurred
 * @param flags [UNUSED IN THIS CALLBACK]
 * @param param [UNUSED IN THIS CALLBACK]
 */
static void onMouse(int event, int x, int y, int /*flags*/, void* /*param*/) {
    static int dw[8];
    static Card* card;
    static std::ofstream writer;
    static std::vector<Card*> hand;
    static int i, index, size, width, startX;

    if (event == cv::EVENT_LBUTTONDOWN || event == cv::EVENT_LBUTTONDBLCLK) {
        // Only response to left-click events, and ignore the others
        if (sAdjustOptions) {
            // Do special behaviors when configuring game options
            if (y >= 21 && y <= 42) {
                if (x >= 1140 && x <= 1260) {
                    // <QUIT> button
                    // Leave options page
                    sAdjustOptions = false;
                    onStatusChanged(sStatus);
                } // if (x >= 1140 && x <= 1260)
            } // if (y >= 21 && y <= 42)
            else if (y >= 60 && y <= 240) {
                if (x >= 150 && x <= 270) {
                    // BGM OFF button
                    sMediaPlay->setVolume(0);
                    onStatusChanged(sStatus);
                } // if (x >= 150 && x <= 270)
                else if (x >= 330 && x <= 450) {
                    // BGM ON button
                    sMediaPlay->setVolume(50);
                    onStatusChanged(sStatus);
                } // else if (x >= 330 && x <= 450)
                else if (x >= 790 && x <= 910) {
                    // Easy AI Level
                    sUno->setDifficulty(Uno::LV_EASY);
                    onStatusChanged(sStatus);
                } // else if (x >= 790 && x <= 910)
                else if (x >= 970 && x <= 1090) {
                    // Hard AI Level
                    sUno->setDifficulty(Uno::LV_HARD);
                    onStatusChanged(sStatus);
                } // else if (x >= 970 && x <= 1090)
            } // else if (y >= 60 && y <= 240)
            else if (y >= 270 && y <= 450) {
                if (x >= 150 && x <= 270) {
                    // SND OFF button
                    sSoundPool->setEnabled(false);
                    onStatusChanged(sStatus);
                } // if (x >= 150 && x <= 270)
                else if (x >= 330 && x <= 450) {
                    // SND ON button
                    sSoundPool->setEnabled(true);
                    sSoundPool->play(SoundPool::SND_PLAY);
                    onStatusChanged(sStatus);
                } // else if (x >= 330 && x <= 450)
                else if (x >= 790 && x <= 910) {
                    // 3 players
                    sUno->setPlayers(3);
                    onStatusChanged(sStatus);
                } // else if (x >= 790 && x <= 910)
                else if (x >= 970 && x <= 1090) {
                    // 4 players
                    sUno->setPlayers(4);
                    onStatusChanged(sStatus);
                } // else if (x >= 970 && x <= 1090)
            } // else if (y >= 270 && y <= 450)
            else if (y >= 520 && y <= 700) {
                if (x >= 240 && x <= 450) {
                    // 7-0 rule
                    sUno->setSevenZeroRule(!sUno->isSevenZeroRule());
                    onStatusChanged(sStatus);
                } // if (x >= 240 && x <= 450)
                else if (x >= 790 && x <= 1045) {
                    // +2 stacking rule
                    sUno->setDraw2StackRule(!sUno->isDraw2StackRule());
                    onStatusChanged(sStatus);
                } // else if (x >= 790 && x <= 1045)
                else if (y >= 679) {
                    if (x >= 20 && x <= 200) {
                        // <OPTIONS> button
                        // Leave options page
                        sAdjustOptions = false;
                        onStatusChanged(sStatus);
                    } // if (x >= 20 && x <= 200)
                    else if (x >= 1140 && x <= 1260) {
                        // <AUTO> button
                        sAuto = !sAuto;
                        onStatusChanged(sStatus);
                    } // else if (x >= 1140 && x <= 1260)
                } // else if (y >= 679)
            } // else if (y >= 520 && y <= 700)
        } // if (sAdjustOptions)
        else if (y >= 21 && y <= 42 && x >= 1140 && x <= 1260) {
            // <QUIT> button
            // Store statistics data to UnoCard.stat file
            writer = std::ofstream(
                /* filename */ "UnoCard.stat",
                /*   mode   */ std::ios::out | std::ios::binary
            ); // std::ofstream()

            if (!writer.fail()) {
                // Store statistics data to file
                dw[0] = sScore;
                dw[1] = sUno->getPlayers();
                dw[2] = sUno->getDifficulty();
                dw[3] = sUno->isSevenZeroRule() ? 1 : 0;
                dw[4] = sUno->isDraw2StackRule() ? 1 : 0;
                dw[5] = sSoundPool->isEnabled() ? 1 : 0;
                dw[6] = sMediaPlay->volume();
                for (dw[7] = 0, i = 0; i < 7; ++i) {
                    dw[7] = 31 * dw[7] + dw[i];
                } // for (dw[7] = 0, i = 0; i < 7; ++i)

                writer.write(FILE_HEADER, 8);
                writer.write((char*)dw, 8 * sizeof(int));
                writer.close();
            } // if (!writer.fail())

            cv::destroyAllWindows();
            delete sMediaList;
            delete sMediaPlay;
            delete sSoundPool;
            exit(0);
        } // else if (y >= 21 && y <= 42 && x >= 1140 && x <= 1260)
        else if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260) {
            // <AUTO> button
            // In player's action, automatically play or draw cards by AI
            sAuto = !sAuto;
            switch (sStatus) {
            case STAT_WILD_COLOR:
                sStatus = Player::YOU;
                // fall through

            case Player::YOU:
            case STAT_SEVEN_TARGET:
                onStatusChanged(sStatus);
                break; // case Player::YOU, STAT_SEVEN_TARGET

            default:
                cv::putText(
                    /* img       */ sScreen,
                    /* text      */ "<AUTO>",
                    /* org       */ cv::Point(1130, 700),
                    /* fontFace  */ FONT_SANS,
                    /* fontScale */ 1.0,
                    /* color     */ sAuto ? RGB_YELLOW : RGB_WHITE
                ); // cv::putText()

                imshow("Uno", sScreen);
                break; // default
            } // switch (sStatus)
        } // else if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260)
        else switch (sStatus) {
        case STAT_WELCOME:
            if (y >= 270 && y <= 450) {
                if (x >= 580 && x <= 700) {
                    // UNO button, start a new game
                    sStatus = STAT_NEW_GAME;
                    onStatusChanged(sStatus);
                } // if (x >= 580 && x <= 700)
            } // if (y >= 270 && y <= 450)
            else if (y >= 679 && y <= 700) {
                if (x >= 20 && x <= 200) {
                    // <OPTIONS> button
                    sAdjustOptions = true;
                    onStatusChanged(sStatus);
                } // if (x >= 20 && x <= 200)
            } // else if (y >= 679 && y <= 700)
            break; // case STAT_WELCOME

        case Player::YOU:
            if (sAuto) {
                // Do operations automatically by AI strategies
                break; // case Player::YOU
            } // if (sAuto)
            else if (sImmPlayAsk) {
                // Asking if you want to play the drawn card immediately
                if (y >= 520 && y <= 700) {
                    hand = sUno->getPlayer(Player::YOU)->getHandCards();
                    size = int(hand.size());
                    width = 45 * size + 75;
                    startX = 640 - width / 2;
                    if (x >= startX && x <= startX + width) {
                        // Hand card area
                        // Calculate which card clicked by the X-coordinate
                        index = (x - startX) / 45;
                        if (index >= size) {
                            index = size - 1;
                        } // if (index >= size)

                        // If clicked the drawn card, play it
                        card = hand.at(index);
                        if (card == sDrawnCard) {
                            sImmPlayAsk = false;
                            if (card->isWild() && size > 1) {
                                sStatus = STAT_WILD_COLOR;
                                onStatusChanged(sStatus);
                            } // if (card->isWild() && size > 1)
                            else {
                                play(index);
                            } // else
                        } // if (card == sDrawnCard)
                    } // if (x >= startX && x <= startX + width)
                } // if (y >= 520 && y <= 700)
                else if (x > 310 && x < 500) {
                    if (y > 220 && y < 315) {
                        // YES button, play the drawn card
                        hand = sUno->getPlayer(sStatus)->getHandCards();
                        size = int(hand.size());
                        for (index = 0; index < size; ++index) {
                            card = hand.at(index);
                            if (card == sDrawnCard) {
                                sImmPlayAsk = false;
                                if (card->isWild() && size > 1) {
                                    sStatus = STAT_WILD_COLOR;
                                    onStatusChanged(sStatus);
                                } // if (card->isWild() && size > 1)
                                else {
                                    play(index);
                                } // else
                                break;
                            } // if (card == sDrawnCard)
                        } // for (index = 0; index < size; ++index)
                    } // if (y > 220 && y < 315)
                    else if (y > 315 && y < 410) {
                        // NO button, go to next player's round
                        sStatus = STAT_IDLE; // block mouse events when idle
                        sImmPlayAsk = false;
                        refreshScreen(NAME[Player::YOU] + ": Pass");
                        cv::waitKey(750);
                        sStatus = sUno->switchNow();
                        onStatusChanged(sStatus);
                    } // else if (y > 315 && y < 410)
                } // else if (x > 310 && x < 500)
            } // else if (sImmPlayAsk)
            else if (sChallengeAsk) {
                // Asking if you want to challenge your previous player
                if (x > 310 && x < 500) {
                    if (y > 220 && y < 315) {
                        // YES button, challenge wild +4
                        onChallengeChance(true);
                    } // if (y > 220 && y < 315)
                    else if (y > 315 && y < 410) {
                        // NO button, do not challenge wild +4
                        onChallengeChance(false);
                    } // else if (y > 315 && y < 410)
                } // if (x > 310 && x < 500)
            } // else if (sChallengeAsk)
            else if (y >= 520 && y <= 700) {
                hand = sUno->getPlayer(Player::YOU)->getHandCards();
                size = int(hand.size());
                width = 45 * size + 75;
                startX = 640 - width / 2;
                if (x >= startX && x <= startX + width) {
                    // Hand card area
                    // Calculate which card clicked by the X-coordinate
                    index = (x - startX) / 45;
                    if (index >= size) {
                        index = size - 1;
                    } // if (index >= size)

                    // Try to play it
                    card = hand.at(index);
                    if (card != sSelectedCard) {
                        sSelectedCard = card;
                        onStatusChanged(sStatus);
                    } // if (card != sSelectedCard)
                    else if (card->isWild() && size > 1) {
                        sStatus = STAT_WILD_COLOR;
                        onStatusChanged(sStatus);
                    } // else if (card->isWild() && size > 1)
                    else if (sUno->isLegalToPlay(card)) {
                        play(index);
                    } // else if (sUno->isLegalToPlay(card))
                    else {
                        refreshScreen("Cannot play " + std::string(card->name));
                    } // else
                } // if (x >= startX && x <= startX + width)
                else {
                    // Blank area, cancel your selection
                    sSelectedCard = nullptr;
                    onStatusChanged(sStatus);
                } // else
            } // else if (y >= 520 && y <= 700)
            else if (y >= 270 && y <= 450 && x >= 338 && x <= 458) {
                // Card deck area, draw a card
                draw();
            } // else if (y >= 270 && y <= 450 && x >= 338 && x <= 458)
            break; // case Player::YOU

        case STAT_WILD_COLOR:
            if (y > 220 && y < 315) {
                if (x > 310 && x < 405) {
                    // Red sector
                    sStatus = Player::YOU;
                    play(index, RED);
                } // if (x > 310 && x < 405)
                else if (x > 405 && x < 500) {
                    // Blue sector
                    sStatus = Player::YOU;
                    play(index, BLUE);
                } // else if (x > 405 && x < 500)
            } // if (y > 220 && y < 315)
            else if (y > 315 && y < 410) {
                if (x > 310 && x < 405) {
                    // Yellow sector
                    sStatus = Player::YOU;
                    play(index, YELLOW);
                } // if (x > 310 && x < 405)
                else if (x > 405 && x < 500) {
                    // Green sector
                    sStatus = Player::YOU;
                    play(index, GREEN);
                } // else if (x > 405 && x < 500)
            } // else if (y > 315 && y < 410)
            break; // case STAT_WILD_COLOR

        case STAT_SEVEN_TARGET:
            if (y > 198 && y < 276 && sUno->getPlayers() == 4) {
                if (x > 338 && x < 472) {
                    // North sector
                    swapWith(Player::COM2);
                } // if (x > 338 && x < 472)
            } // if (y > 198 && y < 276 && sUno->getPlayers() == 4)
            else if (y > 315 && y < 410) {
                if (x > 310 && x < 405) {
                    // West sector
                    swapWith(Player::COM1);
                } // if (x > 310 && x < 405)
                else if (x > 405 && x < 500) {
                    // East sector
                    swapWith(Player::COM3);
                } // else if (x > 405 && x < 500)
            } // else if (y > 315 && y < 410)
            break; // case STAT_SEVEN_TARGET

        case STAT_GAME_OVER:
            if (y >= 270 && y <= 450) {
                if (x >= 338 && x <= 458) {
                    // Card deck area, start a new game
                    sStatus = STAT_NEW_GAME;
                    onStatusChanged(sStatus);
                } // if (x >= 338 && x <= 458)
            } // if (y >= 270 && y <= 450)
            else if (y >= 679 && y <= 700) {
                if (x >= 20 && x <= 200) {
                    // <OPTIONS> button
                    sAdjustOptions = true;
                    onStatusChanged(sStatus);
                } // if (x >= 20 && x <= 200)
            } // else if (y >= 679 && y <= 700)
            break; // case STAT_GAME_OVER

        default:
            break; // default
        } // else switch (sStatus)
    } // if (event == cv::EVENT_LBUTTONDOWN || event == cv::EVENT_LBUTTONDBLCLK)
} // onMouse(int * 4, void*)

// E.O.F