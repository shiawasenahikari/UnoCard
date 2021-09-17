////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <ctime>
#include <Uno.h>
#include <string>
#include <vector>
#include <Card.h>
#include <cstdlib>
#include <cstring>
#include <Color.h>
#include <iostream>
#include <Player.h>
#include <Content.h>
#include <opencv2/highgui.hpp>

static const char* BROKEN_IMAGE_RESOURCES_EXCEPTION =
"One or more image resources are broken. Re-install this application.";

/**
 * Singleton, hide default constructor.
 */
Uno::Uno() {
    cv::Mat br, dk;
    std::string path;
    int i, done, total;

    // Preparations
    done = 0;
    total = 124;
    std::cout << "Loading... (0%)" << std::endl;

    // Load background image resources
    bgWelcome = cv::imread("resource/bg_welcome.png");
    bgCounter = cv::imread("resource/bg_counter.png");
    bgClockwise = cv::imread("resource/bg_clockwise.png");
    if (bgWelcome.empty() ||
        bgWelcome.rows != 720 ||
        bgWelcome.cols != 1280 ||
        bgCounter.empty() ||
        bgCounter.rows != 720 ||
        bgCounter.cols != 1280 ||
        bgClockwise.empty() ||
        bgClockwise.rows != 720 ||
        bgClockwise.cols != 1280) {
        std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
        exit(1);
    } // if (bgWelcome.empty() || ...)
    else {
        done += 3;
        std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
    } // else

    // Load card back image resource
    backImage = cv::imread("resource/back.png");
    if (backImage.empty() ||
        backImage.rows != 181 ||
        backImage.cols != 121) {
        std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
        exit(1);
    } // if (backImage.empty() || ...)
    else {
        ++done;
        std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
    } // else

    // Load difficulty image resources
    easyImage = cv::imread("resource/lv_easy.png");
    hardImage = cv::imread("resource/lv_hard.png");
    easyImage_d = cv::imread("resource/lv_easy_dark.png");
    hardImage_d = cv::imread("resource/lv_hard_dark.png");
    if (easyImage.empty() ||
        easyImage.rows != 181 ||
        easyImage.cols != 121 ||
        hardImage.empty() ||
        hardImage.rows != 181 ||
        hardImage.cols != 121 ||
        easyImage_d.empty() ||
        easyImage_d.rows != 181 ||
        easyImage_d.cols != 121 ||
        hardImage_d.empty() ||
        hardImage_d.rows != 181 ||
        hardImage_d.cols != 121) {
        std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
        exit(1);
    } // if (easyImage.empty() || ...)
    else {
        done += 4;
        std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
    } // else

    // Generate number cards and action cards
    const char* color_short = "rbgy";
    const char* content_short = "0123456789+@$";
    const char* color_long[] = { "Red", "Blue", "Green", "Yellow" };
    const char* content_long[] = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "Draw 2", "Skip", "Reverse"
    }; // content_long[]

    for (i = 0; i < 52; ++i) {
        int a = i / 13, b = i % 13;
        path = "resource/front_";
        path = path + color_short[a] + content_short[b] + ".png";
        br = cv::imread(path);
        path = "resource/dark_";
        path = path + color_short[a] + content_short[b] + ".png";
        dk = cv::imread(path);
        if (br.empty() || br.rows != 181 || br.cols != 121 ||
            dk.empty() || dk.rows != 181 || dk.cols != 121) {
            std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
            exit(1);
        } // if (br.empty() || ...)
        else {
            std::string n = std::string(color_long[a]) + ' ' + content_long[b];
            const char* name = strdup(n.c_str());
            table.push_back(Card(br, dk, Color(a + 1), Content(b), name));
            if (b > 0) {
                // One zero-card in every color
                // Two other number/action cards in every color
                table.push_back(Card(br, dk, Color(a + 1), Content(b), name));
            } // if (b > 0)

            done += 2;
            std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
        } // else
    } // for (i = 0; i < 52; ++i)

    // Generate wild cards
    br = cv::imread("resource/front_kw.png");
    dk = cv::imread("resource/dark_kw.png");
    if (br.empty() || br.rows != 181 || br.cols != 121 ||
        dk.empty() || dk.rows != 181 || dk.cols != 121) {
        std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
        exit(1);
    } // if (br.empty() || ...)
    else {
        wildImage[0] = br;
        for (i = 0; i < 4; ++i) {
            table.push_back(Card(br, dk, NONE, WILD, "Wild"));
        } // for (i = 0; i < 4; ++i)

        done += 2;
        std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
    } // else

    // Generate wild +4 cards
    br = cv::imread("resource/front_kw+.png");
    dk = cv::imread("resource/dark_kw+.png");
    if (br.empty() || br.rows != 181 || br.cols != 121 ||
        dk.empty() || dk.rows != 181 || dk.cols != 121) {
        std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
        exit(1);
    } // if (br.empty() || ...)
    else {
        wildDraw4Image[0] = br;
        for (i = 0; i < 4; ++i) {
            table.push_back(Card(br, dk, NONE, WILD_DRAW4, "Wild +4"));
        } // for (i = 0; i < 4; ++i)

        done += 2;
        std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
    } // else

    // Load colored wild & wild +4 image resources
    for (i = 1; i < 5; ++i) {
        path = "resource/front_";
        path = path + color_short[i - 1] + "w.png";
        wildImage[i] = cv::imread(path);
        path = "resource/front_";
        path = path + color_short[i - 1] + "w+.png";
        wildDraw4Image[i] = cv::imread(path);
        if (wildImage[i].empty() ||
            wildImage[i].rows != 181 ||
            wildImage[i].cols != 121 ||
            wildDraw4Image[i].empty() ||
            wildDraw4Image[i].rows != 181 ||
            wildDraw4Image[i].cols != 121) {
            std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
            exit(1);
        } // if (wildImage[i].empty() || ...)
        else {
            done += 2;
            std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
        } // else
    } // for (i = 1; i < 5; ++i)

    // Generate a random seed based on the current time stamp
    srand((unsigned)time(nullptr));

    // Initialize other members
    players = 3;
    now = rand() % 4;
    difficulty = LV_EASY;
    draw2StackCount = direction = 0;
    draw2StackRule = sevenZeroRule = false;
} // Uno() (Class Constructor)

/**
 * @return Reference of our singleton.
 */
Uno* Uno::getInstance() {
    static Uno instance;
    return &instance;
} // getInstance()

/**
 * @return Card back image resource.
 */
const cv::Mat& Uno::getBackImage() {
    return backImage;
} // getBackImage()

/**
 * @param level   Pass LV_EASY or LV_HARD.
 * @param hiLight Pass true if you want to get a hi-lighted image,
 *                or false if you want to get a dark image.
 * @return Corresponding difficulty button image.
 */
const cv::Mat& Uno::getLevelImage(int level, bool hiLight) {
    return level == LV_EASY ?
        /* level == LV_EASY */ (hiLight ? easyImage : easyImage_d) :
        /* level == LV_HARD */ (hiLight ? hardImage : hardImage_d);
} // getLevelImage(int, bool)

/**
 * @return Background image resource in current direction.
 */
const cv::Mat& Uno::getBackground() {
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
const cv::Mat& Uno::getColoredWildImage(Color color) {
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
const cv::Mat& Uno::getColoredWildDraw4Image(Color color) {
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
 * Switch current action sequence.
 *
 * @return Switched action sequence. DIR_LEFT for clockwise,
 *         or DIR_RIGHT for counter-clockwise.
 */
int Uno::switchDirection() {
    return (direction = 4 - direction);
} // switchDirection()

/**
 * @param who Get which player's instance. Must be one of the following:
 *            Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @return Specified player's instance.
 */
Player* Uno::getPlayer(int who) {
    return who < Player::YOU || who > Player::COM3 ? nullptr : &player[who];
} // getPlayer(int)

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
 * Find a card instance in card table.
 *
 * @param color   Color of the card you want to get.
 * @param content Content of the card you want to get.
 * @return Corresponding card instance.
 */
Card* Uno::findCard(Color color, Content content) {
    int i;
    Card* result;

    result = nullptr;
    for (i = 0; i < 108; ++i) {
        if (table[i].color == color && table[i].content == content) {
            result = &table[i];
            break;
        } // if (table[i].color == color && table[i].content == content)
    } // for (i = 0; i < 108; ++i)

    return result;
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

    // Clear card deck, used card deck, recent played cards,
    // everyone's hand cards, and everyone's strong/weak colors
    deck.clear();
    used.clear();
    recent.clear();
    for (i = Player::YOU; i <= Player::COM3; ++i) {
        player[i].handCards.clear();
        player[i].weakColor = NONE;
        player[i].strongColor = NONE;
    } // for (i = Player::YOU; i <= Player::COM3; ++i)

    // Generate a temporary sequenced card deck
    for (i = 0; i < 108; ++i) {
        if (table[i].isWild()) {
            // reset the wild cards' colors
            table[i].color = NONE;
        } // if (table[i].isWild())

        deck.push_back(&table[i]);
    } // for (i = 0; i < 108; ++i)

    // Shuffle cards
    size = int(deck.size());
    while (size > 0) {
        i = rand() % size--;
        card = deck[i]; deck[i] = deck[size]; deck[size] = card;
    } // while (size > 0)

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
        } // else
    } while (recent.empty());

    // In the case of (last winner = NORTH) & (game mode = 3 player mode)
    // Re-specify the dealer randomly
    if (players == 3 && now == Player::COM2) {
        now = (3 + rand() % 3) % 4;
    } // if (players == 3 && now == Player::COM2)
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
    Card* picked;
    int i, index, size;
    std::vector<Card*>* hand;
    std::vector<Card*>::iterator it;

    i = -1;
    if (who >= Player::YOU && who <= Player::COM3) {
        if (draw2StackCount > 0) {
            --draw2StackCount;
        } // if (draw2StackCount > 0)
        else if (!force) {
            // Draw a card by player itself, register weak color
            player[who].weakColor = recent.back()->color;
            if (player[who].weakColor == player[who].strongColor) {
                // Weak color cannot also be strong color
                player[who].strongColor = NONE;
            } // if (player[who].weakColor == player[who].strongColor)
        } // else if (!force)

        hand = &(player[who].handCards);
        if (hand->size() < MAX_HOLD_CARDS) {
            // Draw a card from card deck, and put it to an appropriate position
            card = deck.back();
            deck.pop_back();
            for (i = 0, it = hand->begin(); it != hand->end(); ++i, ++it) {
                if ((*it)->order > card->order) {
                    // Found an appropriate position to insert the new card,
                    // which keeps the player's hand cards sequenced
                    break;
                } // if ((*it)->order > card->order)
            } // for (i = 0, it = hand->begin(); it != hand->end(); ++i, ++it)

            hand->insert(it, card);
            player[who].recent = nullptr;
            if (deck.empty()) {
                // Re-use the used cards when there are no more cards in deck
                size = int(used.size());
                while (size > 0) {
                    index = rand() % size;
                    picked = used.at(index);
                    if (picked->isWild()) {
                        // reset the used wild cards' colors
                        picked->color = NONE;
                    } // if (picked->isWild())

                    deck.push_back(picked);
                    used.erase(used.begin() + index);
                    --size;
                } // while (size > 0)
            } // if (deck.empty())
        } // if (hand->size() < MAX_HOLD_CARDS)
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
    bool result;
    Card* previous;

    if (card == nullptr || recent.empty()) {
        // Null Pointer
        result = false;
    } // if (card == nullptr || recent.empty())
    else if (draw2StackCount > 0) {
        // When in +2 stacking procedure, only +2 cards are legal
        result = card->content == DRAW2;
    } // else if (draw2StackCount > 0)
    else if (card->isWild()) {
        // Wild cards: LEGAL
        result = true;
    } // else if (card->isWild())
    else {
        // Same content to previous: LEGAL
        // Same color to previous: LEGAL
        // Other cards: ILLEGAL
        previous = recent.back();
        result = card->color == previous->color
            || card->content == previous->content;
    } // else

    return result;
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
            hand->erase(hand->begin() + index);
            if (card->isWild()) {
                // When a wild card is played, register the specified
                // following legal color as the player's strong color
                card->color = color;
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
            else if (player[who].strongCount > size - 1) {
                // Correct the value of strong counter when necessary
                player[who].strongCount = size - 1;
            } // else if (player[who].strongCount > size - 1)

            if (card->content == DRAW2 && draw2StackRule) {
                draw2StackCount += 2;
            } // if (card->content == DRAW2 && draw2StackRule)

            player[who].recent = card;
            recent.push_back(card);
            if (recent.size() > 5) {
                used.push_back(recent.front());
                recent.erase(recent.begin());
            } // if (recent.size() > 5)

            if (hand->size() == 0) {
                // Game over, change background image
                direction = 0;
            } // if (hand->size() == 0)
        } // if (index < size)
    } // if (who >= Player::YOU && who <= Player::COM3)

    return card;
} // play(int, int, Color)

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
} // cycle()

// E.O.F