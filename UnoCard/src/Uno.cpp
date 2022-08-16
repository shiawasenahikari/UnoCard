////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <ctime>
#include <QDebug>
#include <QImage>
#include <vector>
#include <cstdlib>
#include <QString>
#include "include/Uno.h"
#include "include/Card.h"
#include "include/Color.h"
#include "include/Player.h"
#include "include/Content.h"

#define MASK_I_TO_END(i) (0xffffffffU << (i))
#define MASK_BEGIN_TO_I(i) (~(0xffffffffU << (i)))
#define MASK_ALL(u, p) MASK_BEGIN_TO_I((u)->getPlayer(p)->getHandSize())

static const QString A[] = {
    "k", "r", "b", "g", "y"
}; // A[]

static const QString B[] = {
    "0", "1", "2", "3", "4",
    "5", "6", "7", "8", "9",
    "+", "@", "$", "w", "w+"
}; // B[]

static const char* BROKEN_IMAGE_RESOURCES_EXCEPTION =
"One or more image resources are broken. Re-install this application.";

/**
 * Singleton, hide default constructor.
 */
Uno::Uno(unsigned seed) {
    QImage br, dk;
    int i, done, total;

    // Preparations
    done = 0;
    total = 124;
    qDebug("Loading... (0%)");

    // Load background image resources
    if (bgWelcome.load("resource/bg_welcome.png") &&
        bgWelcome.width() == 1280 && bgWelcome.height() == 720 &&
        bgCounter.load("resource/bg_counter.png") &&
        bgCounter.width() == 1280 && bgCounter.height() == 720 &&
        bgClockwise.load("resource/bg_clockwise.png") &&
        bgClockwise.width() == 1280 && bgClockwise.height() == 720) {
        done += 3;
        qDebug("Loading... (%d%%)", 100 * done / total);
    } // if (bgWelcome.load("resource/bg_welcome.png") && ...)
    else {
        qDebug(BROKEN_IMAGE_RESOURCES_EXCEPTION);
        exit(1);
    } // else

    // Load card back image resource
    if (backImage.load("resource/back.png") &&
        backImage.width() == 121 && backImage.height() == 181) {
        ++done;
        qDebug("Loading... (%d%%)", 100 * done / total);
    } // if (backImage.load("resource/back.png") && ...)
    else {
        qDebug(BROKEN_IMAGE_RESOURCES_EXCEPTION);
        exit(1);
    } // else

    // Load difficulty image resources
    if (easyImage.load("resource/lv_easy.png") &&
        easyImage.width() == 121 && easyImage.height() == 181 &&
        hardImage.load("resource/lv_hard.png") &&
        hardImage.width() == 121 && hardImage.height() == 181 &&
        easyImage_d.load("resource/lv_easy_dark.png") &&
        easyImage_d.width() == 121 && easyImage_d.height() == 181 &&
        hardImage_d.load("resource/lv_hard_dark.png") &&
        hardImage_d.width() == 121 && hardImage_d.height() == 181) {
        done += 4;
        qDebug("Loading... (%d%%)", 100 * done / total);
    } // if (easyImage.load("resource/lv_easy.png") && ...)
    else {
        qDebug(BROKEN_IMAGE_RESOURCES_EXCEPTION);
        exit(1);
    } // else

    // Generate 54 types of cards
    for (i = 0; i < 54; ++i) {
        Color a = i < 52 ? Color(i / 13 + 1) : Color(0);
        Content b = i < 52 ? Content(i % 13) : Content(i - 39);
        if (br.load("resource/front_" + A[a] + B[b] + ".png") &&
            br.width() == 121 && br.height() == 181 &&
            dk.load("resource/dark_" + A[a] + B[b] + ".png") &&
            dk.width() == 121 && dk.height() == 181) {
            done += 2;
            table.push_back(Card(br, dk, a, b));
            qDebug("Loading... (%d%%)", 100 * done / total);
        } // if (br.load("resource/front_" + A[a] + B[b] + ".png") && ...)
        else {
            qDebug(BROKEN_IMAGE_RESOURCES_EXCEPTION);
            exit(1);
        } // else
    } // for (i = 0; i < 54; ++i)

    // Load colored wild & wild +4 image resources
    wildImage[0] = table.at(39 + WILD).image;
    wildDraw4Image[0] = table.at(39 + WILD_DRAW4).image;
    for (i = 1; i < 5; ++i) {
        if (br.load("resource/front_" + A[i] + B[WILD] + ".png") &&
            br.width() == 121 && br.height() == 181 &&
            dk.load("resource/front_" + A[i] + B[WILD_DRAW4] + ".png") &&
            dk.width() == 121 && dk.height() == 181) {
            done += 2;
            wildImage[i] = br;
            wildDraw4Image[i] = dk;
            qDebug("Loading... (%d%%)", 100 * done / total);
        } // if (br.load("resource/front_" + A[i] + B[WILD] + ".png" && ...)
        else {
            qDebug(BROKEN_IMAGE_RESOURCES_EXCEPTION);
            exit(1);
        } // else
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
    now = rand() % 4;
    forcePlay = true;
    difficulty = LV_EASY;
    draw2StackCount = direction = 0;
    draw2StackRule = sevenZeroRule = false;
} // Uno(unsigned) (Class Constructor)

/**
 * @return Reference of our singleton.
 */
Uno* Uno::getInstance(unsigned seed) {
    static Uno instance(seed);
    return &instance;
} // getInstance(unsigned)

/**
 * @return Card back image resource.
 */
const QImage& Uno::getBackImage() {
    return backImage;
} // getBackImage()

/**
 * @param level   Pass LV_EASY or LV_HARD.
 * @param hiLight Pass true if you want to get a hi-lighted image,
 *                or false if you want to get a dark image.
 * @return Corresponding difficulty button image.
 */
const QImage& Uno::getLevelImage(int level, bool hiLight) {
    return level == LV_EASY ?
        /* level == LV_EASY */ (hiLight ? easyImage : easyImage_d) :
        /* level == LV_HARD */ (hiLight ? hardImage : hardImage_d);
} // getLevelImage(int, bool)

/**
 * @return Background image resource in current direction.
 */
const QImage& Uno::getBackground() {
    switch (direction) {
    case DIR_LEFT:
        return bgClockwise; // case DIR_LEFT

    case DIR_RIGHT:
        return bgCounter; // case DIR_RIGHT

    default:
        return bgWelcome; // default
    } // switch (direction)
} // getBackground()

/**
 * When a player played a wild card and specified a following legal color,
 * get the corresponding color-filled image here, and show it in recent
 * card area.
 *
 * @param color The wild image with which color filled you want to get.
 * @return Corresponding color-filled image.
 */
const QImage& Uno::getColoredWildImage(Color color) {
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
const QImage& Uno::getColoredWildDraw4Image(Color color) {
    return wildDraw4Image[color];
} // getColoredWildDraw4Image(Color)

/**
 * @return Player in turn. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::getNow() {
    return now;
} // getNow()

/**
 * Switch to next player's turn.
 *
 * @return Player in turn after switched. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::switchNow() {
    return (now = getNext());
} // switchNow()

/**
 * @return Current player's next player. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::getNext() {
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
int Uno::getOppo() {
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
int Uno::getPrev() {
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
Player* Uno::getPlayer(int who) {
    return who < Player::YOU || who > Player::COM3 ? nullptr : &player[who];
} // getPlayer(int)

/**
 * @return &this->player[this->getNow()].
 */
Player* Uno::getCurrPlayer() {
    return &player[getNow()];
} // getCurrPlayer()

/**
 * @return &this->player[this->getNext()].
 */
Player* Uno::getNextPlayer() {
    return &player[getNext()];
} // getNextPlayer()

/**
 * @return &this->player[this->getOppo()].
 */
Player* Uno::getOppoPlayer() {
    return &player[getOppo()];
} // getOppoPlayer()

/**
 * @return &this->player[this->getPrev()].
 */
Player* Uno::getPrevPlayer() {
    return &player[getPrev()];
} // getPrevPlayer()

/**
 * @return How many players in game (3 or 4).
 */
int Uno::getPlayers() {
    return players;
} // getPlayers()

/**
 * Set the amount of players in game.
 *
 * @param players Supports 3 and 4.
 */
void Uno::setPlayers(int players) {
    if (players == 3 || players == 4) {
        this->players = players;
    } // if (players == 3 || players == 4)
} // setPlayers(int)

/**
 * Switch current action sequence. The value of [direction] will be
 * switched between DIR_LEFT and DIR_RIGHT.
 */
void Uno::switchDirection() {
    direction = 4 - direction;
} // switchDirection()

/**
 * @return Current difficulty (LV_EASY / LV_HARD).
 */
int Uno::getDifficulty() {
    return difficulty;
} // getDifficulty()

/**
 * Set game difficulty.
 *
 * @param difficulty Pass target difficulty value.
 *                   Only LV_EASY and LV_HARD are available.
 */
void Uno::setDifficulty(int difficulty) {
    if (difficulty == LV_EASY || difficulty == LV_HARD) {
        this->difficulty = difficulty;
    } // if (difficulty == LV_EASY || difficulty == LV_HARD)
} // setDifficulty(int)

/**
 * @return This value tells that what's the next step
 *         after you drew a playable card in your action.
 *         When force play is enabled, play the card immediately.
 *         When force play is disabled, keep the card in your hand.
 */
bool Uno::isForcePlay() {
    return forcePlay;
} // isForcePlay()

/**
 * @param enabled Enable/Disable the force play rule.
 */
void Uno::setForcePlay(bool enabled) {
    forcePlay = enabled;
} // setForcePlay(bool)

/**
 * @return Whether the 7-0 rule is enabled. In 7-0 rule, when a seven card
 *         is put down, the player must swap hand cards with another player
 *         immediately. When a zero card is put down, everyone need to pass
 *         the hand cards to the next player.
 */
bool Uno::isSevenZeroRule() {
    return sevenZeroRule;
} // isSevenZeroRule()

/**
 * @param enabled Enable/Disable the 7-0 rule.
 */
void Uno::setSevenZeroRule(bool enabled) {
    sevenZeroRule = enabled;
} // setSevenZeroRule(bool)

/**
 * @return Can or cannot stack +2 cards. If can, when you put down a +2
 *         card, the next player may transfer the punishment to its next
 *         player by stacking another +2 card. Finally the first one who
 *         does not stack a +2 card must draw all of the required cards.
 */
bool Uno::isDraw2StackRule() {
    return draw2StackRule;
} // isDraw2StackRule()

/**
 * @param enabled Enable/Disable the +2 stacking rule.
 */
void Uno::setDraw2StackRule(bool enabled) {
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
int Uno::getDraw2StackCount() {
    return draw2StackCount;
} // getDraw2StackCount()

/**
 * Find a card instance in card table.
 *
 * @param color   Color of the card you want to get.
 * @param content Content of the card you want to get.
 * @return Corresponding card instance.
 */
Card* Uno::findCard(Color color, Content content) {
    return color == NONE && content == WILD
        ? &table.at(39 + WILD)
        : color == NONE && content == WILD_DRAW4
        ? &table.at(39 + WILD_DRAW4)
        : color != NONE && content != WILD && content != WILD_DRAW4
        ? &table.at(13 * (color - 1) + content)
        : nullptr;
} // findCard(Color, Content)

/**
 * @return How many cards in deck (haven't been used yet).
 */
int Uno::getDeckCount() {
    return int(deck.size());
} // getDeckCount()

/**
 * @return How many cards have been used.
 */
int Uno::getUsedCount() {
    return int((used.size() + recent.size()));
} // getUsedCount()

/**
 * @return Recent played cards.
 */
const std::vector<Card*>& Uno::getRecent() {
    return recent;
} // getRecent()

/**
 * @return Colors of recent played cards.
 */
const std::vector<Color>& Uno::getRecentColors() {
    return recentColors;
} // getRecentColors()

/**
 * @return Color of the last played card.
 */
Color Uno::lastColor() {
    return recentColors.back();
} // lastColor()

/**
 * @return Color of the next-to-last played card.
 */
Color Uno::next2lastColor() {
    return recentColors.at(recentColors.size() - 2);
} // next2lastColor()

/**
 * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
 * then determine our start card.
 */
void Uno::start() {
    Card* card;
    int i, size;

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
    recent.clear();
    recentColors.clear();
    for (i = Player::YOU; i <= Player::COM3; ++i) {
        player[i].open = 0x00;
        player[i].handCards.clear();
        player[i].weakColor = NONE;
        player[i].strongColor = NONE;
    } // for (i = Player::YOU; i <= Player::COM3; ++i)

    // Generate a temporary sequenced card deck
    for (i = 0; i < 54; ++i) {
        card = &table.at(i);
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
    size = int(deck.size());
    while (size > 0) {
        i = rand() % size--;
        card = deck[i]; deck[i] = deck[size]; deck[size] = card;
    } // while (size > 0)

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
            recent.push_back(card);
            ++colorAnalysis[card->color];
            ++contentAnalysis[card->content];
            recentColors.push_back(card->color);
        } // else
    } while (recent.empty());

    // Let everyone draw 7 cards
    if (players == 3) {
        for (i = 0; i < 7; ++i) {
            draw(Player::YOU,  /* force */ true);
            draw(Player::COM1, /* force */ true);
            draw(Player::COM3, /* force */ true);
        } // for (i = 0; i < 7; ++i)
    } // if (players == 3)
    else {
        for (i = 0; i < 7; ++i) {
            draw(Player::YOU,  /* force */ true);
            draw(Player::COM1, /* force */ true);
            draw(Player::COM2, /* force */ true);
            draw(Player::COM3, /* force */ true);
        } // for (i = 0; i < 7; ++i)
    } // else

    // Update the legality binary
    legality = 0x30000000000000LL
        | (0x1fffLL << 13 * (card->color - 1))
        | (0x8004002001LL << card->content);

    // In the case of (last winner = NORTH) & (game mode = 3 player mode)
    // Re-specify the dealer randomly
    if (players == 3 && now == Player::COM2) {
        now = (3 + rand() % 3) % 4;
    } // if (players == 3 && now == Player::COM2)

    // Write log
    qDebug("Game starts with %s", qPrintable(card->name));
} // start()

/**
 * Call this function when someone needs to draw a card.
 * <p>
 * NOTE: Everyone can hold 14 cards at most in this program, so even if this
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
int Uno::draw(int who, bool force) {
    Card* card;
    int i, index, size;
    std::vector<Card*>* hand;

    i = -1;
    if (who >= Player::YOU && who <= Player::COM3) {
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

        hand = &(player[who].handCards);
        if (hand->size() < MAX_HOLD_CARDS) {
            // Draw a card from card deck, and put it to an appropriate position
            qDebug("Player %d draw a card", who);
            card = deck.back();
            deck.pop_back();
            if (who == Player::YOU) {
                auto it = std::upper_bound(hand->begin(), hand->end(), card);
                i = int(it - hand->begin());
                hand->insert(it, card);
                player[who].open = (player[who].open << 1) | 0x01;
            } // if (who == Player::YOU)
            else {
                i = int(hand->size());
                hand->push_back(card);
            } // else

            player[who].recent = nullptr;
            if (deck.empty()) {
                // Re-use the used cards when there are no more cards in deck
                qDebug("Re-use the used cards");
                size = int(used.size());
                while (size > 0) {
                    index = rand() % size--;
                    deck.push_back(used.at(index));
                    --colorAnalysis[used.at(index)->color];
                    --contentAnalysis[used.at(index)->content];
                    used.erase(used.begin() + index);
                } // while (size > 0)
            } // if (deck.empty())
        } // if (hand->size() < MAX_HOLD_CARDS)
        else {
            // In +2 stack rule, if someone cannot draw all of the required
            // cards because of the max-hold-card limitation, force reset
            // the counter to zero.
            draw2StackCount = 0;
        } // else

        if (draw2StackCount == 0) {
            // Update the legality binary when necessary
            card = recent.back();
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
bool Uno::isLegalToPlay(Card* card) {
    return ((legality >> card->id) & 0x01LL) == 0x01LL;
} // isLegalToPlay(Card*)

/**
 * @return How many legal cards (the cards that can be played legally)
 *         in now player's hand.
 */
int Uno::legalCardsCount4NowPlayer() {
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
Card* Uno::play(int who, int index, Color color) {
    int size;
    Card* card;
    std::vector<Card*>* hand;

    card = nullptr;
    if (who >= Player::YOU && who <= Player::COM3) {
        hand = &(player[who].handCards);
        size = int(hand->size());
        if (index < size) {
            card = hand->at(index);
            qDebug("Player %d played %s", who, qPrintable(card->name));
            hand->erase(hand->begin() + index);
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
            recent.push_back(card);
            ++colorAnalysis[card->color];
            ++contentAnalysis[card->content];
            printAnalysisData();
            recentColors.push_back(card->isWild() ? color : card->color);
            if (recent.size() > 5) {
                used.push_back(recent.front());
                recent.erase(recent.begin());
                recentColors.erase(recentColors.begin());
            } // if (recent.size() > 5)

            // Update the legality binary
            legality = draw2StackCount > 0
                ? (0x8004002001LL << DRAW2)
                : card->isWild()
                ? 0x30000000000000LL
                | (0x1fffLL << 13 * (lastColor() - 1))
                : 0x30000000000000LL
                | (0x1fffLL << 13 * (lastColor() - 1))
                | (0x8004002001LL << card->content);
            if (hand->size() == 0) {
                // Game over, change background & show everyone's hand cards
                direction = 0;
                for (int i = Player::COM1; i <= Player::COM3; ++i) {
                    player[i].sort();
                    player[i].open = MASK_ALL(this, i);
                } // for (int i = Player::COM1; i <= Player::COM3; ++i)

                qDebug("======= WINNER IS PLAYER %d =======", who);
            } // if (hand->size() == 0)
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
bool Uno::challenge(int whom) {
    bool result = false;

    if (whom >= Player::YOU && whom <= Player::COM3) {
        if (whom != Player::YOU) {
            player[whom].sort();
            player[whom].open = MASK_ALL(this, whom);
        } // if (whom != Player::YOU)

        for (Card* card : player[whom].handCards) {
            if (card->color == next2lastColor()) {
                result = true;
                break;
            } // if (card->color == next2lastColor())
        } // for (Card* card : player[whom].handCards)
    } // if (whom >= Player::YOU && whom <= Player::COM3)

    qDebug("Player %d is challenged. Result = %d", whom, result);
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
void Uno::swap(int a, int b) {
    Player store = player[a];
    player[a] = player[b];
    player[b] = store;
    if (a == Player::YOU || b == Player::YOU) {
        player[Player::YOU].sort();
        player[Player::YOU].open = MASK_ALL(this, Player::YOU);
    } // if (a == Player::YOU || b == Player::YOU)

    qDebug("Player %d swapped hand cards with Player %d", a, b);
} // swap(int, int)

/**
 * In 7-0 rule, when a zero card is put down, everyone need to pass the hand
 * cards to the next player.
 */
void Uno::cycle() {
    int curr = now, next = getNext(), oppo = getOppo(), prev = getPrev();
    Player store = player[curr];
    player[curr] = player[prev];
    player[prev] = player[oppo];
    player[oppo] = player[next];
    player[next] = store;
    player[Player::YOU].sort();
    player[Player::YOU].open = MASK_ALL(this, Player::YOU);
    qDebug("Everyone passed hand cards to the next player");
} // cycle()

/**
 * Print the content of the colorAnalysis array and the contentAnalysis
 * array.
 */
void Uno::printAnalysisData() {
    int i;
    QString s(QString::number(colorAnalysis[0]));
    QString t(QString::number(contentAnalysis[0]));

    for (i = 1; i < 5; ++i) {
        s = s + ", " + QString::number(colorAnalysis[i]);
    } // for (i = 1; i < 5; ++i)

    for (i = 1; i < 15; ++i) {
        t = t + ", " + QString::number(contentAnalysis[i]);
    } // for (i = 1; i < 15; ++i)

    qDebug("colorAnalysis = [%s]", qPrintable(s));
    qDebug("contentAnalysis = [%s]", qPrintable(t));
} // printAnalysisData()

// E.O.F