////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __UNO_H_494649FDFA62B3C015120BCB9BE17613__
#define __UNO_H_494649FDFA62B3C015120BCB9BE17613__

#include <map>
#include <ctime>
#include <QChar>
#include <QFile>
#include <QRect>
#include <QBrush>
#include <QColor>
#include <QDebug>
#include <QImage>
#include <vector>
#include <cstdlib>
#include <QBitmap>
#include <QString>
#include <QPainter>
#include <QIODevice>
#include "include/Card.h"
#include "include/Color.h"
#include "include/Player.h"
#include "include/Content.h"

#define BROKEN_IMAGE_RESOURCES_EXCEPTION \
"One or more image resources are broken. Re-install this app."

/**
 * RecentInfo Class.
 */
class RecentInfo {
public:
    Card* card = nullptr;
    Color color = NONE;
}; // RecentInfo Class

/**
 * Uno Runtime Class (Singleton).
 */
class Uno {
private:
    /**
     * Card back image resource.
     */
    QImage backImage;

    /**
     * Background image resource (for welcome screen).
     */
    QImage bgWelcome;

    /**
     * Background image resource (Direction: COUNTER CLOCKWISE).
     */
    QImage bgCounter;

    /**
     * Background image resource (Direction: CLOCKWISE).
     */
    QImage bgClockwise;

    /**
     * Image resources for wild cards.
     */
    QImage wildImage[5];

    /**
     * Image resources for wild +4 cards.
     */
    QImage wildDraw4Image[5];

    /**
     * Difficulty button image resources (EASY).
     */
    QImage easyImage, easyImage_d;

    /**
     * Difficulty button image resources (HARD).
     */
    QImage hardImage, hardImage_d;

    /**
     * 2vs2 button image resources.
     */
    QImage light2vs2, dark2vs2;

    /**
     * Custom font image resources.
     */
    QImage fontR, fontB, fontG, fontW, fontY;

    /**
     * Player in turn. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    int now;

    /**
     * How many players in game. Supports 3 or 4.
     */
    int players;

    /**
     * Current action sequence (DIR_LEFT / DIR_RIGHT).
     */
    int direction;

    /**
     * Current difficulty (LV_EASY / LV_HARD).
     */
    int difficulty;

    /**
     * Whether the 2vs2 rule is enabled.
     */
    bool _2vs2;

    /**
     * Whether the force play rule is enabled.
     */
    bool forcePlay;

    /**
     * Whether the 7-0 rule is enabled.
     */
    bool sevenZeroRule;

    /**
     * Can or cannot stack +2 cards.
     */
    bool draw2StackRule;

    /**
     * Only available in +2 stack rule. In this rule, when a +2 card is put
     * down, the next player may transfer the punishment to its next player
     * by stacking another +2 card. Finally the first one who does not stack
     * a +2 card must draw all of the required cards. This counter records
     * that how many required cards need to be drawn by the final player.
     * When this value is not zero, only +2 cards are legal to play.
     */
    int draw2StackCount;

    /**
     * How many initial cards for everyone in each game.
     * This value can be set to 5~20. Default to 7.
     */
    int initialCards;

    /**
     * This binary value shows that which cards are legal to play. When
     * 0x01LL == ((legality >> i) & 0x01LL), the card with id number i
     * is legal to play. When the recent-played-card queue changes,
     * this value will be updated automatically.
     */
    long long legality;

    /**
     * Game players.
     */
    Player player[4];

    /**
     * Access colorAnalysis[A] to get how many cards in color A are used.
     * Access contentAnalysis[B] to get how many cards in content B are used.
     */
    int colorAnalysis[5], contentAnalysis[15];

    /**
     * Card deck (ready to use).
     */
    std::vector<Card*> deck;

    /**
     * Used cards.
     */
    std::vector<Card*> used;

    /**
     * Card map. table[i] stores the card instance of id number i.
     */
    std::vector<Card> table;

    /**
     * Recent played cards.
     */
    RecentInfo recent[4];

    /**
     * Record the replay data.
     */
    QString replay;

    /**
     * Our custom font's character-to-position map.
     */
    std::map<QChar, int> charMap;

    /**
     * Singleton, hide default constructor.
     */
    inline Uno(unsigned seed) {
        QImage font;
        QPainter pt;
        QBitmap mask;
        int i, done, total;
        QString A[] = { "k", "r", "b", "g", "y" };
        QString B[] = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "+", "$", "@", "w", "w+"
        }; // B[]
        QString hanZi = QString()
            + "一上下东为乐人仍"
            + "从令以传余你保再"
            + "准出击分则到剩功"
            + "加北南发变叠可合"
            + "向否和回堆备多失"
            + "始定家将已度开张"
            + "戏成或战所打托择"
            + "指挑换接摸改效数"
            + "新方无时是最有来"
            + "标次欢法游点牌留"
            + "的目管红给绿置色"
            + "蓝被西规认设败跳"
            + "过迎选重难音颜黄";

        // Preparations
        done = 0;
        total = 127;
        qDebug("Loading... (0%%)");

        // Load background image resources
        bgWelcome = load("resource/bg_welcome.png", 1600, 900);
        bgCounter = load("resource/bg_counter.png", 1600, 900);
        bgClockwise = load("resource/bg_clockwise.png", 1600, 900);
        done += 3;
        qDebug("Loading... (%d%%)", 100 * done / total);

        // Load card back image resource
        backImage = load("resource/back.png", 121, 181);
        ++done;
        qDebug("Loading... (%d%%)", 100 * done / total);

        // Load difficulty image resources
        easyImage = load("resource/lv_easy.png", 121, 181);
        hardImage = load("resource/lv_hard.png", 121, 181);
        easyImage_d = load("resource/lv_easy_dark.png", 121, 181);
        hardImage_d = load("resource/lv_hard_dark.png", 121, 181);
        done += 4;
        qDebug("Loading... (%d%%)", 100 * done / total);

        // Load 2vs2 image resources
        dark2vs2 = load("resource/dark_2vs2.png", 121, 181);
        light2vs2 = load("resource/front_2vs2.png", 121, 181);

        // Generate 54 types of cards
        for (i = 0; i < 54; ++i) {
            Color a = i < 52 ? Color(i / 13 + 1) : Color(0);
            Content b = i < 52 ? Content(i % 13) : Content(i - 39);
            QImage x = load("resource/front_" + A[a] + B[b] + ".png", 121, 181);
            QImage y = load("resource/dark_" + A[a] + B[b] + ".png", 121, 181);

            done += 2;
            table.push_back(Card(x, y, a, b));
            qDebug("Loading... (%d%%)", 100 * done / total);
        } // for (i = 0; i < 54; ++i)

        // Load colored wild & wild +4 image resources
        wildImage[0] = table[39 + WILD].image;
        wildDraw4Image[0] = table[39 + WILD_DRAW4].image;
        for (i = 1; i < 5; ++i) {
            wildImage[i] = load(
                "resource/front_" + A[i] + B[WILD] + ".png", 121, 181
            ); // wildImage[i] = load(QString&, int, int)
            wildDraw4Image[i] = load(
                "resource/front_" + A[i] + B[WILD_DRAW4] + ".png", 121, 181
            ); // wildDraw4Image[i] = load(QString&, int, int)
            done += 2;
            qDebug("Loading... (%d%%)", 100 * done / total);
        } // for (i = 1; i < 5; ++i)

        // Load font images
        fontW = load("resource/font_w.png");
        ++done;
        qDebug("Loading... (%d%%)", 100 * done / total);
        font = fontW.convertToFormat(QImage::Format_RGBA8888);
        mask = QBitmap::fromImage(fontW.createAlphaMask());
        pt.begin(&font);
        pt.setClipRegion(mask);
        pt.setBrush(QBrush(QColor(0xFF, 0x77, 0x77)));
        pt.drawRect(0, 0, fontW.width(), fontW.height());
        fontR = font.copy();
        pt.setBrush(QBrush(QColor(0x77, 0x77, 0xFF)));
        pt.drawRect(0, 0, fontW.width(), fontW.height());
        fontB = font.copy();
        pt.setBrush(QBrush(QColor(0x77, 0xCC, 0x77)));
        pt.drawRect(0, 0, fontW.width(), fontW.height());
        fontG = font.copy();
        pt.setBrush(QBrush(QColor(0xFF, 0xCC, 0x11)));
        pt.drawRect(0, 0, fontW.width(), fontW.height());
        fontY = font.copy();
        pt.end();
        for (i = 0x20; i <= 0x7f; ++i) {
            int r = (i >> 4) - 2, c = i & 0x0f;
            charMap.insert({QChar(i), (r << 4) | c});
        } // for (i = 0x20; i <= 0x7f; ++i)

        for (i = 0; i < hanZi.length(); ++i) {
            int r = (i >> 3) + 6, c = i & 0x07;
            charMap.insert({hanZi[i], (r << 4) | c});
        } // for (i = 0; i < hanZi.length(); ++i)

        // Generate a random seed based on the current time stamp
        if (seed == 0U) {
            seed = unsigned(time(nullptr));
        } // if (seed == 0U)

        qDebug("Random seed is %d", seed);
        srand(seed);

        // Initialize other members
        players = 3;
        legality = 0;
        initialCards = 7;
        now = rand() % 4;
        forcePlay = true;
        difficulty = LV_EASY;
        draw2StackCount = direction = 0;
        _2vs2 = draw2StackRule = sevenZeroRule = false;
    } // Uno(unsigned) (Class Constructor)

    /**
     * Fake C++ Macro
     * #define MASK_I_TO_END(i) (0xffffffffU << (i))
     */
    inline static unsigned MASK_I_TO_END(int i) {
        return 0xffffffffU << i;
    } // MASK_I_TO_END(int)

    /**
     * Fake C++ Macro
     * #define MASK_BEGIN_TO_I(i) (~(0xffffffffU << (i)))
     */
    inline static unsigned MASK_BEGIN_TO_I(int i) {
        return ~(0xffffffffU << i);
    } // MASK_BEGIN_TO_I(int)

    /**
     * Fake C++ Macro
     * #define MAKE_PUBLIC(u, p) do {                            \
     * Player* pl = (u)->getPlayer(p);                           \
     * std::sort(pl->handCards.begin(), pl->handCards.end());    \
     * pl->open = MASK_BEGIN_TO_I(pl->getHandSize());            \
     * } while (false)
     */
    static void MAKE_PUBLIC(Uno* u, int p) {
        Player* pl = u->getPlayer(p);
        std::sort(pl->handCards.begin(), pl->handCards.end());
        pl->open = MASK_BEGIN_TO_I(pl->getHandSize());
    } // MAKE_PUBLIC(Uno*, int)

    /**
     * Convert int[] array to string.
     */
    inline static QString array2string(int arr[], int size) {
        QString s('[');

        if (size > 0) {
            s += QString::number(arr[0]);
            for (int i = 1; i < size; ++i) {
                s += ", ";
                s += QString::number(arr[i]);
            } // for (int i = 1; i < size; ++i)
        } // if (size > 0)

        return s += ']';
    } // array2string(int[], int)

    /**
     * Load a image file, and resize to the specified size.
     *
     * @param fileName Load which image file.
     * @param width    Resize to how many pixels of width.
     * @param height   Resize to how many pixels of height.
     * @return Instance of the loaded image.
     */
    inline static QImage load(const QString& fileName,
                              int width = 0, int height = 0) {
        QImage img;

        if (!img.load(fileName)) {
            qDebug(BROKEN_IMAGE_RESOURCES_EXCEPTION);
            exit(1);
        } // if (!img.load(fileName))

        if ((width | height) != 0) {
            if (img.width() != width || img.height() != height) {
                img = img.scaled(width, height);
            } // if (img.width() != width || img.height() != height)
        } // if ((width | height) != 0)

        return img;
    } // load(const QString&, int, int)

public:
    /**
     * Easy level ID.
     */
    static const int LV_EASY = 0;

    /**
     * Hard level ID.
     */
    static const int LV_HARD = 1;

    /**
     * Direction value (clockwise).
     */
    static const int DIR_LEFT = 1;

    /**
     * Direction value (counter-clockwise).
     */
    static const int DIR_RIGHT = 3;

    /**
     * In this application, everyone can hold 26 cards at most.
     */
    static const int MAX_HOLD_CARDS = 26;

    /**
     * @return Reference of our singleton.
     */
    inline static Uno* getInstance(unsigned seed = 0U) {
        static Uno instance(seed);
        return &instance;
    } // getInstance(unsigned)

    /**
     * @return Card back image resource.
     */
    inline const QImage& getBackImage() {
        return backImage;
    } // getBackImage()

    /**
     * @param level   Pass LV_EASY or LV_HARD.
     * @param hiLight Pass true if you want to get a hi-lighted image,
     *                or false if you want to get a dark image.
     * @return Corresponding difficulty button image.
     */
    inline const QImage& getLevelImage(int level, bool hiLight) {
        return level == LV_EASY ?
            /* level == LV_EASY */ (hiLight ? easyImage : easyImage_d) :
            /* level == LV_HARD */ (hiLight ? hardImage : hardImage_d);
    } // getLevelImage(int, bool)

    /**
     * @return 2vs2 image resource.
     */
    inline const QImage& get2vs2Image() {
        return _2vs2 ? light2vs2 : dark2vs2;
    } // get2vs2Image()

    /**
     * @return Background image resource in current direction.
     */
    inline const QImage& getBackground() {
        return direction == DIR_LEFT
            ? bgClockwise
            : direction == DIR_RIGHT
            ? bgCounter
            : bgWelcome;
    } // getBackground()

    /**
     * When a player played a wild card and specified a following legal color,
     * get the corresponding color-filled image here, and show it in recent
     * card area.
     *
     * @param color The wild image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    inline const QImage& getColoredWildImage(Color color) {
        return wildImage[color];
    } // getColoredWildImage(Color)

    /**
     * When a player played a wild +4 card and specified a following legal
     * color, get the corresponding color-filled image here, and show it in
     * recent card area.
     *
     * @param color The wild +4 image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    inline const QImage& getColoredWildDraw4Image(Color color) {
        return wildDraw4Image[color];
    } // getColoredWildDraw4Image(Color)

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
    inline int getTextWidth(const QString& text) {
        int width = 0;

        for (int i = 0, n = text.length(); i < n; ++i) {
            if ('[' == text[i] && i + 2 < n && text[i + 2] == ']') {
                i += 2;
            } // if ('[' == text[i] && i + 2 < n && text[i + 2] == ']')
            else {
                auto iter = charMap.find(text[i]);
                int position = iter != charMap.end() ? iter->second : 0x1f;
                int r = position >> 4;
                width += r < 6 ? 17 : 33;
            } // else
        } // for (int i = 0, n = text.length(); i < n; ++i)

        return width;
    } // getTextWidth(const QString&)

    /**
     * Put text on image, using our custom font.
     * Unknown characters will be replaced with the question mark '?'.
     * <p>
     * SPECIAL: In the text string, you can use color marks ([R], [B],
     * [G], [W] and [Y]) to control the color of the remaining text.
     *
     * @param painter Use which painter.
     * @param text    Put which text.
     * @param x       Put on where (x coordinate).
     * @param y       Put on where (y coordinate).
     */
    inline void putText(QPainter* painter, const QString& text, int x, int y) {
        QImage* font = &fontW;

        y -= 36;
        for (int i = 0, n = text.length(); i < n; ++i) {
            if ('[' == text[i] && i + 2 < n && text[i + 2] == ']') {
                ++i;
                if (text[i] == 'R') font = &fontR;
                if (text[i] == 'B') font = &fontB;
                if (text[i] == 'G') font = &fontG;
                if (text[i] == 'W') font = &fontW;
                if (text[i] == 'Y') font = &fontY;
                ++i;
            } // if ('[' == text[i] && i + 2 < n && text[i + 2] == ']')
            else {
                auto iter = charMap.find(text[i]);
                int position = iter != charMap.end() ? iter->second : 0x1f;
                int r = position >> 4;
                int c = position & 0x0f;
                int w = r < 6 ? 17 : 33;
                painter->drawImage(
                    /* target */ QRect(x, y, w, 48),
                    /* image  */ *font,
                    /* source */ QRect(w * c, 48 * r, w, 48)
                ); // painter->drawImage()
                x += w;
            } // else
        } // for (int i = 0, n = text.length(); i < n; ++i)
    } // putText(QPainter*, const QString&, int, int)

    /**
     * @return Player in turn. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    inline int getNow() {
        return now;
    } // getNow()

    /**
     * Switch to next player's turn.
     *
     * @return Player in turn after switched. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    inline int switchNow() {
        return (now = getNext());
    } // switchNow()

    /**
     * @return Current player's next player. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    inline int getNext() {
        int next = (now + direction) % 4;
        if (players == 3 && next == Player::COM2) {
            next = (next + direction) % 4;
        } // if (players == 3 && next == Player::COM2)

        return next;
    } // getNext()

    /**
     * @return Current player's opposite player. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     *         NOTE: When only 3 players in game, getOppo() == getPrev().
     */
    inline int getOppo() {
        int oppo = (getNext() + direction) % 4;
        if (players == 3 && oppo == Player::COM2) {
            oppo = (oppo + direction) % 4;
        } // if (players == 3 && oppo == Player::COM2)

        return oppo;
    } // getOppo()

    /**
     * @return Current player's previous player. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    inline int getPrev() {
        int prev = (4 + now - direction) % 4;
        if (players == 3 && prev == Player::COM2) {
            prev = (4 + prev - direction) % 4;
        } // if (players == 3 && prev == Player::COM2)

        return prev;
    } // getPrev()

    /**
     * @param who Get which player's instance. Must be one of the following:
     *            Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @return Specified player's instance.
     */
    inline Player* getPlayer(int who) {
        return who < Player::YOU || who > Player::COM3 ? nullptr : &player[who];
    } // getPlayer(int)

    /**
     * @return &this->player[this->getNow()].
     */
    inline Player* getCurrPlayer() {
        return &player[getNow()];
    } // getCurrPlayer()

    /**
     * @return &this->player[this->getNext()].
     */
    inline Player* getNextPlayer() {
        return &player[getNext()];
    } // getNextPlayer()

    /**
     * @return &this->player[this->getOppo()].
     */
    inline Player* getOppoPlayer() {
        return &player[getOppo()];
    } // getOppoPlayer()

    /**
     * @return &this->player[this->getPrev()].
     */
    inline Player* getPrevPlayer() {
        return &player[getPrev()];
    } // getPrevPlayer()

    /**
     * @return How many players in game (3 or 4).
     */
    inline int getPlayers() {
        return players;
    } // getPlayers()

    /**
     * Set the amount of players in game.
     *
     * @param players Supports 3 and 4.
     */
    inline void setPlayers(int players) {
        if (players == 3 || players == 4) {
            _2vs2 = false;
            this->players = players;
        } // if (players == 3 || players == 4)
    } // setPlayers(int)

    /**
     * Switch current action sequence. The value of [direction] will be
     * switched between DIR_LEFT and DIR_RIGHT.
     */
    inline void switchDirection() {
        direction = 4 - direction;
    } // switchDirection()

    /**
     * @return Current difficulty (LV_EASY / LV_HARD).
     */
    inline int getDifficulty() {
        return difficulty;
    } // getDifficulty()

    /**
     * Set game difficulty.
     *
     * @param difficulty Pass target difficulty value.
     *                   Only LV_EASY and LV_HARD are available.
     */
    inline void setDifficulty(int difficulty) {
        if (difficulty == LV_EASY || difficulty == LV_HARD) {
            this->difficulty = difficulty;
        } // if (difficulty == LV_EASY || difficulty == LV_HARD)
    } // setDifficulty(int)

    /**
     * @return Whether the 2vs2 rule is enabled. In 2vs2 mode, you win the
     *         game either you or the player sitting on your opposite
     *         position played all of the hand cards.
     */
    inline bool is2vs2() {
        return _2vs2;
    } // is2vs2()

    /**
     * @param enabled Enable/Disable the 2vs2 rule.
     */
    inline void set2vs2(bool enabled) {
        _2vs2 = enabled;
        if (enabled) {
            players = 4;
            sevenZeroRule = false;
        } // if (enabled)
    } // set2vs2(bool)

    /**
     * @return This value tells that what's the next step
     *         after you drew a playable card in your action.
     *         When force play is enabled, play the card immediately.
     *         When force play is disabled, keep the card in your hand.
     */
    inline bool isForcePlay() {
        return forcePlay;
    } // isForcePlay()

    /**
     * @param enabled Enable/Disable the force play rule.
     */
    inline void setForcePlay(bool enabled) {
        forcePlay = enabled;
    } // setForcePlay(bool)

    /**
     * @return Whether the 7-0 rule is enabled. In 7-0 rule, when a seven card
     *         is put down, the player must swap hand cards with another player
     *         immediately. When a zero card is put down, everyone need to pass
     *         the hand cards to the next player.
     */
    inline bool isSevenZeroRule() {
        return sevenZeroRule;
    } // isSevenZeroRule()

    /**
     * @param enabled Enable/Disable the 7-0 rule.
     */
    inline void setSevenZeroRule(bool enabled) {
        _2vs2 &= !(sevenZeroRule = enabled);
    } // setSevenZeroRule(bool)

    /**
     * @return Can or cannot stack +2 cards. If can, when you put down a +2
     *         card, the next player may transfer the punishment to its next
     *         player by stacking another +2 card. Finally the first one who
     *         does not stack a +2 card must draw all of the required cards.
     */
    inline bool isDraw2StackRule() {
        return draw2StackRule;
    } // isDraw2StackRule()

    /**
     * @param enabled Enable/Disable the +2 stacking rule.
     */
    inline void setDraw2StackRule(bool enabled) {
        draw2StackRule = enabled;
    } // setDraw2StackRule(bool)

    /**
     * Only available in +2 stack rule. In this rule, when a +2 card is put
     * down, the next player may transfer the punishment to its next player
     * by stacking another +2 card. Finally the first one who does not stack
     * a +2 card must draw all of the required cards.
     *
     * @return This counter records that how many required cards need to be
     *         drawn by the final player. When this value is not zero, only
     *         +2 cards are legal to play.
     */
    inline int getDraw2StackCount() {
        return draw2StackCount;
    } // getDraw2StackCount()

    /**
     * @return How many initial cards for everyone in each game.
     */
    inline int getInitialCards() {
        return initialCards;
    } // getInitialCards()

    /**
     * Add a initial card for everyone.
     * Initial cards can be increased to 20 at most.
     */
    inline void increaseInitialCards() {
        if (++initialCards > 20) {
            initialCards = 20;
        } // if (++initialCards > 20)
    } // increaseInitialCards()

    /**
     * Remove a initial card for everyone.
     * Initial cards can be decreased to 5 at least.
     */
    inline void decreaseInitialCards() {
        if (--initialCards < 5) {
            initialCards = 5;
        } // if (--initialCards < 5)
    } // decreaseInitialCards()

    /**
     * Find a card instance in card table.
     *
     * @param color   Color of the card you want to get.
     * @param content Content of the card you want to get.
     * @return Corresponding card instance.
     */
    inline Card* findCard(Color color, Content content) {
        return color == NONE && content == WILD
            ? &table[39 + WILD]
            : color == NONE && content == WILD_DRAW4
            ? &table[39 + WILD_DRAW4]
            : color != NONE && content != WILD && content != WILD_DRAW4
            ? &table[13 * (color - 1) + content]
            : nullptr;
    } // findCard(Color, Content)

    /**
     * @return How many cards in deck (haven't been used yet).
     */
    inline int getDeckCount() {
        return int(deck.size());
    } // getDeckCount()

    /**
     * @return How many cards have been used.
     */
    inline int getUsedCount() {
        int count = int(used.size());

        for (int i = 0; i < 4; ++i) {
            count += recent[i].card != nullptr ? 1 : 0;
        } // for (int i = 0; i < 4; ++i)

        return count;
    } // getUsedCount()

    /**
     * @return Recent played cards.
     * @deprecated Call getRecentInfo() instead.
     */
    [[deprecated]]
    inline const std::vector<Card*>& getRecent() {
        static std::vector<Card*> ret;

        ret.clear();
        for (int i = 0; i < 4; ++i) {
            if (recent[i].card != nullptr) {
                ret.push_back(recent[i].card);
            } // if (recent[i].card != nullptr)
        } // for (int i = 0; i < 4; ++i)

        return ret;
    } // getRecent()

    /**
     * @return Colors of recent played cards.
     * @deprecated Call getRecentInfo() instead.
     */
    [[deprecated]]
    inline const std::vector<Color>& getRecentColors() {
        static std::vector<Color> ret;

        ret.clear();
        for (int i = 0; i < 4; ++i) {
            if (recent[i].card != nullptr) {
                ret.push_back(recent[i].color);
            } // if (recent[i].card != nullptr)
        } // for (int i = 0; i < 4; ++i)

        return ret;
    } // getRecentColors()

    /**
     * @return Info of recent played cards. An array of RecentInfo objects
     *         will be returned. Access getRecentInfo()[3] for the info of
     *         the last played card, getRecentInfo()[2] for the info of the
     *         next-to-last played card, etc.
     */
    inline const RecentInfo* getRecentInfo() {
        return recent;
    } // getRecentInfo()

    /**
     * @return Color of the last played card.
     */
    inline Color lastColor() {
        return recent[3].color;
    } // lastColor()

    /**
     * @return Color of the next-to-last played card.
     */
    inline Color next2lastColor() {
        return recent[2].color;
    } // next2lastColor()

    /**
     * Access colorAnalysis[A] to get how many cards in color A are used.
     *
     * @param A Provide the parameter A.
     * @return Value of colorAnalysis[A].
     */
    inline int getColorAnalysis(Color A) {
        return colorAnalysis[A];
    } // getColorAnalysis(Color)

    /**
     * Access contentAnalysis[B] to get how many cards in content B are used.
     *
     * @param B Provide the parameter B.
     * @return Value of contentAnalysis[B].
     */
    inline int getContentAnalysis(Content B) {
        return contentAnalysis[B];
    } // getContentAnalysis(Content)

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    inline void start() {
        int i;
        Card* card;

        // Reset direction
        direction = DIR_LEFT;

        // In +2 stack rule, reset the stack counter
        draw2StackCount = 0;

        // Clear the analysis data
        memset(colorAnalysis, 0, 5 * sizeof(int));
        memset(contentAnalysis, 0, 15 * sizeof(int));

        // Clear card deck, used card deck, recent played cards,
        // everyone's hand cards, and everyone's strong/weak colors
        deck.clear();
        used.clear();
        for (i = 0; i < 4; ++i) {
            recent[i].card = nullptr;
            recent[i].color = NONE;
            player[i].open = 0x00;
            player[i].handCards.clear();
            player[i].weakColor = NONE;
            player[i].strongColor = NONE;
        } // for (i = 0; i < 4; ++i)

        // Generate a temporary sequenced card deck
        for (i = 0; i < 54; ++i) {
            card = &table[i];
            switch (card->content) {
            case WILD:
            case WILD_DRAW4:
                deck.push_back(card);
                deck.push_back(card);
                // fall through

            default:
                deck.push_back(card);
                // fall through

            case NUM0:
                deck.push_back(card);
            } // switch (card->content)
        } // for (i = 0; i < 54; ++i)

        // Shuffle cards
        std::random_shuffle(deck.begin(), deck.end());

        // Determine a start card as the previous played card
        do {
            card = deck.back();
            deck.pop_back();
            if (card->isWild()) {
                // Start card cannot be a wild card, so return it
                // to the bottom of card deck and pick another card
                deck.insert(deck.begin(), card);
            } // if (card->isWild())
            else {
                // Any non-wild card can be start card
                // Start card determined
                recent[3].card = card;
                ++colorAnalysis[card->color];
                ++contentAnalysis[card->content];
                recent[3].color = card->color;
            } // else
        } while (recent[3].card == nullptr);

        // Write log
        qDebug("Game starts with %s", qPrintable(card->name));
        replay.clear();
        replay += "ST,", replay += QString::number(_2vs2 ? 1 : 0);
        replay += ",", replay += QString::number(players);
        replay += ",", replay += QString::number(card->id);

        // Let everyone draw initial cards
        for (i = 0; i < initialCards; ++i) {
            draw(Player::YOU,  /* force */ true);
            draw(Player::COM1, /* force */ true);
            if (players == 4) draw(Player::COM2, /* force */ true);
            draw(Player::COM3, /* force */ true);
        } // for (i = 0; i < initialCards; ++i)

        // In the case of (last winner = NORTH) & (game mode = 3 player mode)
        // Re-specify the dealer randomly
        if (players == 3 && now == Player::COM2) {
            now = (3 + rand() % 3) % 4;
        } // if (players == 3 && now == Player::COM2)
    } // start()

    /**
     * Call this function when someone needs to draw a card.
     * <p>
     * NOTE: Everyone can hold 26 cards at most in this program, so even if this
     * function is called, the specified player may not draw a card as a result.
     *
     * @param who   Who draws a card. Must be one of the following values:
     *              Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @param force Pass true if the specified player is required to draw cards,
     *              i.e. previous player played a [+2] or [wild +4] to let this
     *              player draw cards. Or false if the specified player draws a
     *              card by itself in its action.
     * @return Index of the drawn card in hand, or -1 if the specified player
     *         didn't draw a card because of the limitation.
     */
    inline int draw(int who, bool force) {
        int i = -1;

        if (who >= Player::YOU && who <= Player::COM3) {
            auto& hand = player[who].handCards;
            if (draw2StackCount > 0) {
                --draw2StackCount;
            } // if (draw2StackCount > 0)
            else if (!force) {
                // Draw a card by player itself, register weak color
                player[who].weakColor = lastColor();
                if (player[who].weakColor == player[who].strongColor) {
                    // Weak color cannot also be strong color
                    player[who].strongColor = NONE;
                } // if (player[who].weakColor == player[who].strongColor)
            } // else if (!force)

            if (hand.size() < MAX_HOLD_CARDS) {
                // Draw a card from card deck, and put it to an appropriate position
                Card* card = deck.back();
                qDebug("Player %d draw a card", who);
                deck.pop_back();
                if (who == Player::YOU) {
                    auto j = std::upper_bound(hand.begin(), hand.end(), card);
                    i = int(j - hand.begin());
                    hand.insert(j, card);
                    player[who].open = (player[who].open << 1) | 0x01;
                } // if (who == Player::YOU)
                else {
                    i = int(hand.size());
                    hand.push_back(card);
                } // else

                player[who].recent = nullptr;
                replay += ";DR,", replay += QString::number(who);
                replay += ",", replay += QString::number(card->id);
                if (deck.empty()) {
                    // Re-use the used cards when there are no more cards in deck
                    qDebug("Re-use the used cards");
                    for (int j = int(used.size()); --j >= 0; ) {
                        deck.push_back(used[j]);
                        --colorAnalysis[used[j]->color];
                        --contentAnalysis[used[j]->content];
                        used.erase(used.begin() + j);
                    } // for (int j = int(used.size()); --j >= 0; )

                    std::random_shuffle(deck.begin(), deck.end());
                } // if (deck.empty())
            } // if (hand.size() < MAX_HOLD_CARDS)
            else {
                // In +2 stack rule, if someone cannot draw all of the required
                // cards because of the max-hold-card limitation, force reset
                // the counter to zero.
                draw2StackCount = 0;
                replay += ";DF,", replay += QString::number(who);
            } // else

            if (draw2StackCount == 0) {
                // Update the legality binary when necessary
                Card* card = recent[3].card;
                legality = card->isWild()
                    ? 0x30000000000000LL
                    | (0x1fffLL << 13 * (lastColor() - 1))
                    : 0x30000000000000LL
                    | (0x1fffLL << 13 * (lastColor() - 1))
                    | (0x8004002001LL << card->content);
            } // if (draw2StackCount == 0)
        } // if (who >= Player::YOU && who <= Player::COM3)

        return i;
    } // draw(int, bool)

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card Check which card's legality.
     * @return Whether the specified card is legal to play.
     */
    inline bool isLegalToPlay(Card* card) {
        return ((legality >> card->id) & 0x01LL) == 0x01LL;
    } // isLegalToPlay(Card*)

    /**
     * @return How many legal cards (the cards that can be played legally)
     *         in now player's hand.
     */
    inline int legalCardsCount4NowPlayer() {
        int count = 0;

        for (Card* card : player[now].handCards) {
            if (isLegalToPlay(card)) {
                ++count;
            } // if (isLegalToPlay(card))
        } // for (Card* card : player[now].handCards)

        return count;
    } // legalCardsCount4NowPlayer()

    /**
     * Call this function when someone needs to play a card. The played card
     * replaces the "previous played card", and the original "previous played
     * card" becomes a used card at the same time.
     * <p>
     * NOTE: Before calling this function, you must call isLegalToPlay(Card*)
     * function at first to check whether the specified card is legal to play.
     * This function will play the card directly without checking the legality.
     *
     * @param who   Who plays a card. Must be one of the following values:
     *              Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @param index Play which card. Pass the corresponding card's index of the
     *              specified player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     * @return Reference of the played card.
     */
    inline Card* play(int who, int index, Color color) {
        Card* card = nullptr;

        if (who >= Player::YOU && who <= Player::COM3) {
            auto& hand = player[who].handCards;
            int size = int(hand.size());
            if (index < size) {
                if ((card = hand[index])->isWild()) {
                    const char* name = qPrintable(Card::A(color) + card->name);
                    qDebug("Player %d played %s", who, name);
                } // if ((card = hand[index])->isWild())
                else {
                    color = card->color;
                    qDebug("Player %d played %s", who, qPrintable(card->name));
                } // else

                hand.erase(hand.begin() + index);
                if (card->isWild()) {
                    // When a wild card is played, register the specified
                    // following legal color as the player's strong color
                    player[who].strongColor = color;
                    player[who].strongCount = 1 + size / 3;
                    if (color == player[who].weakColor) {
                        // Strong color cannot also be weak color
                        player[who].weakColor = NONE;
                    } // if (color == player[who].weakColor)
                } // if (card->isWild())
                else if (card->color == player[who].strongColor) {
                    // Played a card that matches the registered
                    // strong color, strong counter counts down
                    --player[who].strongCount;
                    if (player[who].strongCount == 0) {
                        player[who].strongColor = NONE;
                    } // if (player[who].strongCount == 0)
                } // else if (card->color == player[who].strongColor)
                else if (player[who].strongCount >= size) {
                    // Correct the value of strong counter when necessary
                    player[who].strongCount = size - 1;
                } // else if (player[who].strongCount >= size)

                if (card->content == DRAW2 && draw2StackRule) {
                    draw2StackCount += 2;
                } // if (card->content == DRAW2 && draw2StackRule)

                player[who].open = who == Player::YOU
                    ? (player[who].open >> 1)
                    : (player[who].open & MASK_BEGIN_TO_I(index))
                    | (player[who].open & MASK_I_TO_END(index + 1)) >> 1;
                player[who].recent = card;
                for (int i = 0; i < 4; ++i) {
                    if (i > 0) {
                        recent[i - 1] = recent[i];
                    } // if (i > 0)
                    else if (recent[i].card != nullptr) {
                        used.push_back(recent[i].card);
                    } // else if (recent[i].card != nullptr)
                } // for (int i = 0; i < 4; ++i)

                recent[3].card = card;
                recent[3].color = color;
                ++colorAnalysis[card->color];
                ++contentAnalysis[card->content];
                qDebug("colorAnalysis & contentAnalysis:");
                qDebug("%s", qPrintable(array2string(colorAnalysis, 5)));
                qDebug("%s", qPrintable(array2string(contentAnalysis, 15)));
                replay += ";PL,", replay += QString::number(who);
                replay += ",", replay += QString::number(card->id);
                replay += ",", replay += QString::number(color);

                // Update the legality binary
                legality = draw2StackCount > 0
                    ? (0x8004002001LL << DRAW2)
                    : card->isWild()
                    ? 0x30000000000000LL
                    | (0x1fffLL << 13 * (lastColor() - 1))
                    : 0x30000000000000LL
                    | (0x1fffLL << 13 * (lastColor() - 1))
                    | (0x8004002001LL << card->content);
                if (size == 1) {
                    // Game over, change background & show everyone's hand cards
                    direction = 0;
                    for (int i = Player::COM1; i <= Player::COM3; ++i) {
                        MAKE_PUBLIC(this, i);
                    } // for (int i = Player::COM1; i <= Player::COM3; ++i)

                    qDebug("======= WINNER IS PLAYER %d =======", who);
                } // if (size == 1)
            } // if (index < size)
        } // if (who >= Player::YOU && who <= Player::COM3)

        return card;
    } // play(int, int, Color)

    /**
     * When you think your previous player used a [wild +4] card illegally,
     * i.e. it holds at least one card matching the next-to-last color,
     * call this function to make a challenge.
     *
     * @param whom Challenge whom. Must be one of the following:
     *             Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @return Tell the challenge result, true if challenge success,
     *         or false if challenge failure.
     */
    inline bool challenge(int whom) {
        bool result = false;

        if (whom >= Player::YOU && whom <= Player::COM3) {
            if (whom != Player::YOU) {
                MAKE_PUBLIC(this, whom);
            } // if (whom != Player::YOU)

            for (Card* card : player[whom].handCards) {
                if (card->color == next2lastColor()) {
                    result = true;
                    break;
                } // if (card->color == next2lastColor())
            } // for (Card* card : player[whom].handCards)
        } // if (whom >= Player::YOU && whom <= Player::COM3)

        qDebug("Player %d is challenged. Result = %d", whom, result);
        replay += ";CH,", replay += QString::number(whom);
        return result;
    } // challenge(int)

    /**
     * In 7-0 rule, when someone put down a seven card, then the player must
     * swap hand cards with another player immediately.
     *
     * @param a Who put down the seven card. Must be one of the following:
     *          Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @param b Exchange with whom. Must be one of the following:
     *          Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     *          Cannot exchange with yourself.
     */
    inline void swap(int a, int b) {
        Player store = player[a];
        player[a] = player[b];
        player[b] = store;
        if (a == Player::YOU || b == Player::YOU) {
            MAKE_PUBLIC(this, Player::YOU);
        } // if (a == Player::YOU || b == Player::YOU)

        qDebug("Player %d swapped hand cards with Player %d", a, b);
        replay += ";SW,", replay += QString::number(a);
        replay += ",", replay += QString::number(b);
    } // swap(int, int)

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass the hand
     * cards to the next player.
     */
    inline void cycle() {
        int curr = now, next = getNext(), oppo = getOppo(), prev = getPrev();
        Player store = player[curr];
        player[curr] = player[prev];
        player[prev] = player[oppo];
        player[oppo] = player[next];
        player[next] = store;
        MAKE_PUBLIC(this, Player::YOU);
        qDebug("Everyone passed hand cards to the next player");
        replay += ";CY";
    } // cycle()

    /**
     * Save current game as a new replay file.
     *
     * @return Name of the saved file, or an empty string if save failed.
     */
    inline QString save() {
        QString name;
        QFile f(QString::number(unsigned(time(nullptr))) + ".sav");

        if (f.open(QIODevice::WriteOnly)) {
            f.write(replay.toLatin1());
            f.close();
            name = f.fileName();
        } // if (f.open(QIODevice::WriteOnly))

        return name;
    } // save()
}; // Uno Class

#endif // __UNO_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F