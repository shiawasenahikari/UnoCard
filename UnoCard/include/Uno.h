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

#include <ctime>
#include <QFile>
#include <QDebug>
#include <QImage>
#include <vector>
#include <cstdlib>
#include <QString>
#include <QIODevice>
#include <QStringList>
#include "include/Card.h"
#include "include/Color.h"
#include "include/Player.h"
#include "include/Content.h"

#define BROKEN_IMAGE_RESOURCES_EXCEPTION \
"One or more image resources are broken. Re-install this app."

typedef const char* String;

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
     * 0: When you draw a playable card, you must keep it in hand.
     * 1: When you draw a playable card, choose to play it or not.
     * 2: When you draw a playable card, you must play it.
     */
    int forcePlayRule;

    /**
     * Whether the 7-0 rule is enabled.
     */
    bool sevenZeroRule;

    /**
     * 0: No cards can be stacked.
     * 1: Only +2 cards can be stacked.
     * 2: +2 & +4 cards can be stacked.
     */
    int stackRule;

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
     * Your loaded replay.
     */
    QStringList loaded;

    /**
     * Iterator of your loaded replay.
     */
    QStringList::Iterator it;

    /**
     * Singleton, hide default constructor.
     */
    inline Uno(unsigned seed) {
        int i, done, total;
        QString A[] = { "k", "r", "b", "g", "y" };
        QString B[] = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "+", "$", "@", "w", "w+"
        }; // B[]

        // Preparations
        done = 0;
        total = 120;
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
        forcePlayRule = 1;
        difficulty = LV_EASY;
        _2vs2 = sevenZeroRule = false;
        stackRule = draw2StackCount = direction = 0;
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
     * @deprecated This image resource has been deprecated.
     */
    [[deprecated]]
    inline const QImage& getLevelImage(int level, bool hiLight) {
        return level == LV_EASY
            ? (hiLight
            ? findCard(GREEN, NUM1)->image
            : findCard(GREEN, NUM1)->darkImg)
            : (hiLight
            ? findCard(RED, NUM2)->image
            : findCard(RED, NUM2)->darkImg);
    } // getLevelImage(int, bool)

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
     * @param whom Get whose hand cards.
     * @return Same as this->getPlayer(whom)->getHandCards().
     */
    inline const std::vector<Card*>& getHandCardsOf(int whom) {
        return getPlayer(whom)->getHandCards();
    } // getHandCardsOf(int)

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
            _2vs2 = sevenZeroRule = false;
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
     *         0: When you draw a playable card, you must keep it in hand.
     *         1: When you draw a playable card, choose to play it or not.
     *         2: When you draw a playable card, you must play it.
     */
    inline int getForcePlayRule() {
        return forcePlayRule;
    } // getForcePlayRule()

    /**
     * @param rule 0: When you draw a playable card, you must keep it in hand.
     *             1: When you draw a playable card, choose to play it or not.
     *             2: When you draw a playable card, you must play it.
     */
    inline void setForcePlayRule(int rule) {
        if (0 <= rule && rule <= 2) {
            forcePlayRule = rule;
        } // if (0 <= rule && rule <= 2)
    } // setForcePlayRule(int)

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
        sevenZeroRule = enabled;
        if (enabled) {
            players = 4;
            _2vs2 = false;
        } // if (enabled)
    } // setSevenZeroRule(bool)

    /**
     * Can or cannot stack +2/+4 cards. If can, when you put down a +2/+4
     * card, the next player may transfer the punishment to its next player
     * by stacking another +2/+4 card. Finally the first one who does not
     * stack a +2/+4 card must draw all of the required cards.
     *
     * @return 0: No cards can be stacked.
     *         1: Only +2 cards can be stacked.
     *         2: +2 & +4 cards can be stacked.
     */
    inline int getStackRule() {
        return stackRule;
    } // getStackRule()

    /**
     * @param rule 0: No cards can be stacked.
     *             1: Only +2 cards can be stacked.
     *             2: +2 & +4 cards can be stacked.
     */
    inline void setStackRule(int rule) {
        if (0 <= rule && rule <= 2) {
            stackRule = rule;
        } // if (0 <= rule && rule <= 2)
    } // setStackRule(int)

    /**
     * @return Current game mode. Must be one of the following:
     *         1: 7-0, 2: 2vs2, 3: 3P, 4: 4P.
     */
    inline int getGameMode() {
        return sevenZeroRule ? 1 : _2vs2 ? 2 : players;
    } // getGameMode()

    /**
     * @param gameMode Set to which game mode. Must be one of the following:
     *                 1: 7-0, 2: 2vs2, 3: 3P, 4: 4P.
     */
    inline void setGameMode(int gameMode) {
        if (gameMode == 1) {
            setSevenZeroRule(true);
        } // if (gameMode == 1)
        else if (gameMode == 2) {
            set2vs2(true);
        } // else if (gameMode == 2)
        else {
            setPlayers(gameMode);
        } // else
    } // setGameMode(int)

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
     * Find a card instance in card table by card ID (0 ~ 53).
     *
     * @param id ID of the card you want to get.
     * @return Corresponding card instance.
     */
    inline Card* findCardById(int id) {
        return 0 <= id && id <= 53 ? &table[id] : nullptr;
    } // findCardById(int)

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
        qDebug("Game starts with %s", card->name);
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
        Card* card;
        int i = -1, j;

        if (Player::YOU <= who && who <= Player::COM3) {
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
                card = deck.back();
                qDebug("Player %d draw a card", who);
                deck.pop_back();
                if (who == Player::YOU) {
                    i = std::upper_bound(hand.begin(), hand.end(), card)
                        - hand.begin();
                    hand.insert(hand.begin() + i, card);
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
                    for (j = int(used.size()); --j >= 0; ) {
                        deck.push_back(used[j]);
                        --colorAnalysis[used[j]->color];
                        --contentAnalysis[used[j]->content];
                        used.erase(used.begin() + j);
                    } // for (j = int(used.size()); --j >= 0; )

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
                card = recent[3].card;
                legality = card->isWild()
                    ? 0x30000000000000LL
                    | (0x1fffLL << 13 * (lastColor() - 1))
                    : 0x30000000000000LL
                    | (0x1fffLL << 13 * (lastColor() - 1))
                    | (0x8004002001LL << card->content);
            } // if (draw2StackCount == 0)
        } // if (Player::YOU <= who && who <= Player::COM3)

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
        int i, size;
        Card* card = nullptr;

        if (Player::YOU <= who && who <= Player::COM3) {
            auto& hand = player[who].handCards;

            size = int(hand.size());
            if (index < size) {
                if ((card = hand[index])->isWild()) {
                    static String p[] = { "", "[R]", "[B]", "[G]", "[Y]" };
                    qDebug("Player %d played %s%s", who, p[color], card->name);
                } // if ((card = hand[index])->isWild())
                else {
                    color = card->color;
                    qDebug("Player %d played %s", who, card->name);
                } // else

                hand.erase(hand.begin() + index);
                if (card->isWild()) {
                    // When a wild card is played, register the specified
                    // following legal color as the player's strong color
                    if (stackRule != 2 || card->content != WILD_DRAW4) {
                        // In +2/+4 stack rule, Wild +4 cards will lose their
                        // "change color" ability, and cannot be challenged.
                        // So when someone played a Wild +4 card in this rule,
                        // strong color will not be registered.
                        player[who].strongColor = color;
                        player[who].strongCount = 1 + size / 3;
                        if (color == player[who].weakColor) {
                            // Strong color cannot also be weak color
                            player[who].weakColor = NONE;
                        } // if (color == player[who].weakColor)
                    } // if (stackRule != 2 || card->content != WILD_DRAW4)
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

                if (card->content == DRAW2 && stackRule != 0) {
                    draw2StackCount += 2;
                } // if (card->content == DRAW2 && stackRule != 0)
                else if (card->content == WILD_DRAW4 && stackRule == 2) {
                    draw2StackCount += 4;
                } // else if (card->content == WILD_DRAW4 && stackRule == 2)

                player[who].open = who == Player::YOU
                    ? (player[who].open >> 1)
                    : (player[who].open & MASK_BEGIN_TO_I(index))
                    | (player[who].open & MASK_I_TO_END(index + 1)) >> 1;
                player[who].recent = card;
                for (i = 0; i < 4; ++i) {
                    if (i > 0) {
                        recent[i - 1] = recent[i];
                    } // if (i > 0)
                    else if (recent[i].card != nullptr) {
                        used.push_back(recent[i].card);
                    } // else if (recent[i].card != nullptr)
                } // for (i = 0; i < 4; ++i)

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
                if (draw2StackCount < 1) {
                    legality = card->isWild()
                        ? 0x30000000000000LL
                        | (0x1fffLL << 13 * (lastColor() - 1))
                        : 0x30000000000000LL
                        | (0x1fffLL << 13 * (lastColor() - 1))
                        | (0x8004002001LL << card->content);
                } // if (draw2StackCount < 1)
                else if (stackRule == 1) {
                    legality = 0x8004002001LL << DRAW2;
                } // else if (stackRule == 1)
                else {
                    legality = (card->isWild()
                        ? 0x1LL << DRAW2 << 13 * (lastColor() - 1)
                        : 0x8004002001LL << DRAW2)
                        | 0x20000000000000LL;
                } // else

                if (size == 1) {
                    // Game over, change background & show everyone's hand cards
                    direction = 0;
                    for (i = Player::COM1; i <= Player::COM3; ++i) {
                        MAKE_PUBLIC(this, i);
                    } // for (i = Player::COM1; i <= Player::COM3; ++i)

                    qDebug("======= WINNER IS PLAYER %d =======", who);
                } // if (size == 1)
            } // if (index < size)
        } // if (Player::YOU <= who && who <= Player::COM3)

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

        if (Player::YOU <= whom && whom <= Player::COM3) {
            if (whom != Player::YOU) {
                MAKE_PUBLIC(this, whom);
            } // if (whom != Player::YOU)

            for (Card* card : player[whom].handCards) {
                if (card->color == next2lastColor()) {
                    result = true;
                    break;
                } // if (card->color == next2lastColor())
            } // for (Card* card : player[whom].handCards)
        } // if (Player::YOU <= whom && whom <= Player::COM3)

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

    /**
     * Load a existed replay file.
     *
     * @param replayName Provide the file name of your replay.
     * @return True if load success.
     */
    inline bool loadReplay(const QString& replayName) {
        int i;
        QFile f(replayName);
        bool ok = f.open(QIODevice::ReadOnly);
        auto checkParam = [](const QString& val, int lo, int hi) {
            int iVal = val.toInt();
            return lo <= iVal && iVal <= hi;
        }; // checkParam(const QString&, int, int)

        if (ok) {
            loaded = QString(f.readAll()).split(';');
            f.close();
        } // if (ok)

        for (i = 0; ok && i < loaded.size(); ++i) {
            QStringList x = loaded[i].split(',');

            if (x.size() == 0 ||
                (i == 0 && x[0] != "ST") ||
                (i != 0 && x[0] == "ST")) {
                // Empty command is forbidden
                // ST command can only appear once at the beginning
                ok = false;
            } // if (x.size() == 0 || ...)
            else if (x[0] == "ST") {
                // ST: Start a new game
                // Command format: ST,a,b,c
                // a = 1 if in 2vs2 mode, otherwise 0
                // b = players in game [3, 4]
                // c = start card's id [0, 51]
                ok = x.size() > 3
                    && checkParam(x[1], 0, 1)
                    && checkParam(x[2], 3, 4)
                    && checkParam(x[3], 0, 51);
            } // else if (x[0] == "ST")
            else if (x[0] == "DR") {
                // DR: Draw a card from deck
                // Command format: DR,a,b
                // a = who drew a card [0, 3]
                // b = drawn card's id [0, 53]
                ok = x.size() > 2
                    && checkParam(x[1], 0, 3)
                    && checkParam(x[2], 0, 53);
            } // else if (x[0] == "DR")
            else if (x[0] == "PL") {
                // PL: Play a card
                // Command format: PL,a,b,c
                // a = who played a card [0, 3]
                // b = played card's id [0, 53]
                // c = the following legal color [0, 4]
                ok = x.size() > 3
                    && checkParam(x[1], 0, 3)
                    && checkParam(x[2], 0, 53)
                    && checkParam(x[3], 0, 4);
            } // else if (x[0] == "PL")
            else if (x[0] == "DF" || x[0] == "CH") {
                // DF: Draw but failure
                // Command format: DF,a
                // a = who drew but failure [0, 3]
                // CH: Make a challenge
                // Command format: CH,a
                // a = challenged to whom [0, 3]
                ok = x.size() > 1
                    && checkParam(x[1], 0, 3);
            } // else if (x[0] == "DF" || x[0] == "CH")
            else if (x[0] == "SW") {
                // SW: Swap hand cards between player A and B
                // Command format: SW,a,b
                // a = player A's id [0, 3]
                // b = player B's id [0, 3]
                ok = x.size() > 2
                    && checkParam(x[1], 0, 3)
                    && checkParam(x[2], 0, 3)
                    && x[1] != x[2];
            } // else if (x[0] == "SW")
            else if (x[0] != "CY") {
                // CY: Cycle, everyone pass hand cards to the next
                // Command format: CY
                // Other commands are all unknown commands
                ok = false;
            } // else if (x[0] != "CY")
        } // for (i = 0; ok && i < loaded.size(); ++i)

        it = ok ? loaded.begin() : (loaded.clear(), loaded).begin();
        return ok;
    } // loadReplay(const QString&)

    /**
     * Go to next step of your replay.
     * This method will do nothing when we reached the end of your replay.
     *
     * @param out This is an out parameter. Pass an int array (length>=3)
     *            in order to let us pass the return values by assigning
     *            out[0~2]. For a command "COM,a,b,c", out[0] will become
     *            the value of a, out[1] will become the value of b, and
     *            out[2] will become the value of c.
     * @return Name of current command, e.g. "ST". When out == nullptr,
     *         or end of replay reached, return an empty string.
     */
    inline QString forwardReplay(int out[]) {
        QString s;
        Card* card;
        int a, b, c, i;

        if (out == nullptr) {
            throw "out == nullptr";
        } // if (out == nullptr)

        if (loaded.size() > 0 && it != loaded.end()) {
            QStringList x = (it++)->split(',');

            a = out[0] = x.size() > 1 ? x[1].toInt() : 0;
            b = out[1] = x.size() > 2 ? x[2].toInt() : 0;
            c = out[2] = x.size() > 3 ? x[3].toInt() : 0;
            if ((s = x[0]) == "ST") {
                // ST: Start a new game
                // Command format: ST,a,b,c
                // a = 1 if in 2vs2 mode, otherwise 0
                // b = players in game [3, 4]
                // c = start card's id [0, 51]
                setPlayers(b);
                set2vs2(a != 0);
                card = &table[c];
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

                recent[3].card = card;
                recent[3].color = card->color;
                direction = card->content == REV ? DIR_RIGHT : DIR_LEFT;
            } // if ((s = x[0]) == "ST")
            else if (s == "DR") {
                // DR: Draw a card from deck
                // Command format: DR,a,b
                // a = who drew a card [0, 3]
                // b = drawn card's id [0, 53]
                auto& hand = player[a].handCards;

                card = &table[b];
                i = std::lower_bound(hand.begin(), hand.end(), card)
                    - hand.begin();
                hand.insert(hand.begin() + i, card);
                player[a].open = (player[a].open << 1) | 0x01;
                now = a;
            } // else if (s == "DR")
            else if (s == "PL") {
                // PL: Play a card
                // Command format: PL,a,b,c
                // a = who played a card [0, 3]
                // b = played card's id [0, 53]
                // c = the following legal color [0, 4]
                auto& hand = player[a].handCards;

                card = &table[b];
                i = std::lower_bound(hand.begin(), hand.end(), card)
                    - hand.begin();
                if (i < player[a].getHandSize() && hand[i] == card) {
                    hand.erase(hand.begin() + i);
                    player[a].open = player[a].open >> 1;
                    for (i = 0; i < 3; ++i) {
                        recent[i] = recent[i + 1];
                    } // for (i = 0; i < 3; ++i)

                    recent[3].card = card;
                    recent[3].color = Color(c);
                } // if (i < player[a].getHandSize() && hand[i] == card)

                now = a;
                if (card->content == REV) {
                    switchDirection();
                } // if (card->content == REV)
            } // else if (s == "PL")
            else if (s == "DF") {
                // DF: Draw but failure
                // Command format: DF,a
                // a = who drew but failure [0, 3]
                now = a;
            } // else if (s == "DF")
            else if (s == "SW") {
                // SW: Swap hand cards between player A and B
                // Command format: SW,a,b
                // a = player A's id [0, 3]
                // b = player B's id [0, 3]
                Player store = player[a];
                player[a] = player[b];
                player[b] = store;
            } // else if (s == "SW")
            else if (s == "CY") {
                // CY: Cycle, everyone pass hand cards to the next
                // Command format: CY
                int curr = now, next = getNext();
                int oppo = getOppo(), prev = getPrev();
                Player store = player[curr];
                player[curr] = player[prev];
                player[prev] = player[oppo];
                player[oppo] = player[next];
                player[next] = store;
            } // else if (s == "CY")
        } // if (loaded.size() > 0 && it != loaded.end())
        else {
            direction = 0;
        } // else

        return s;
    } // forwardReplay(int[])
}; // Uno Class

#endif // __UNO_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F