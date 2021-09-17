////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __UNO_H_494649FDFA62B3C015120BCB9BE17613__
#define __UNO_H_494649FDFA62B3C015120BCB9BE17613__

#include <vector>
#include <Card.h>
#include <Color.h>
#include <Player.h>
#include <Content.h>
#include <opencv2/core.hpp>

/**
 * Uno Runtime Class (Singleton).
 */
class Uno {
private:
    /**
     * Card back image resource.
     */
    cv::Mat backImage;

    /**
     * Background image resource (for welcome screen).
     */
    cv::Mat bgWelcome;

    /**
     * Background image resource (Direction: COUNTER CLOCKWISE).
     */
    cv::Mat bgCounter;

    /**
     * Background image resource (Direction: CLOCKWISE).
     */
    cv::Mat bgClockwise;

    /**
     * Image resources for wild cards.
     */
    cv::Mat wildImage[5];

    /**
     * Image resources for wild +4 cards.
     */
    cv::Mat wildDraw4Image[5];

    /**
     * Difficulty button image resources (EASY).
     */
    cv::Mat easyImage, easyImage_d;

    /**
     * Difficulty button image resources (HARD).
     */
    cv::Mat hardImage, hardImage_d;

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
     * Game players.
     */
    Player player[4];

    /**
     * Card deck (ready to use).
     */
    std::vector<Card*> deck;

    /**
     * Used cards.
     */
    std::vector<Card*> used;

    /**
     * Card table.
     */
    std::vector<Card> table;

    /**
     * Recent played cards.
     */
    std::vector<Card*> recent;

    /**
     * Singleton, hide default constructor.
     */
    Uno();

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
     * In this application, everyone can hold 14 cards at most.
     */
    static const int MAX_HOLD_CARDS = 14;

    /**
     * @return Reference of our singleton.
     */
    static Uno* getInstance();

    /**
     * @return Card back image resource.
     */
    const cv::Mat& getBackImage();

    /**
     * @param level   Pass LV_EASY or LV_HARD.
     * @param hiLight Pass true if you want to get a hi-lighted image,
     *                or false if you want to get a dark image.
     * @return Corresponding difficulty button image.
     */
    const cv::Mat& getLevelImage(int level, bool hiLight);

    /**
     * @return Background image resource in current direction.
     */
    const cv::Mat& getBackground();

    /**
     * When a player played a wild card and specified a following legal color,
     * get the corresponding color-filled image here, and show it in recent
     * card area.
     *
     * @param color The wild image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    const cv::Mat& getColoredWildImage(Color color);

    /**
     * When a player played a wild +4 card and specified a following legal
     * color, get the corresponding color-filled image here, and show it in
     * recent card area.
     *
     * @param color The wild +4 image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    const cv::Mat& getColoredWildDraw4Image(Color color);

    /**
     * @return Player in turn. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    int getNow();

    /**
     * Switch to next player's turn.
     *
     * @return Player in turn after switched. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    int switchNow();

    /**
     * @return Current player's next player. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    int getNext();

    /**
     * @return Current player's opposite player. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     *         NOTE: When only 3 players in game, getOppo() == getPrev().
     */
    int getOppo();

    /**
     * @return Current player's previous player. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    int getPrev();

    /**
     * @return How many players in game (3 or 4).
     */
    int getPlayers();

    /**
     * Set the amount of players in game.
     *
     * @param players Supports 3 and 4.
     */
    void setPlayers(int players);

    /**
     * Switch current action sequence.
     *
     * @return Switched action sequence. DIR_LEFT for clockwise,
     *         or DIR_RIGHT for counter-clockwise.
     */
    int switchDirection();

    /**
     * @param who Get which player's instance. Must be one of the following:
     *            Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @return Specified player's instance.
     */
    Player* getPlayer(int who);

    /**
     * @return Current difficulty (LV_EASY / LV_HARD).
     */
    int getDifficulty();

    /**
     * Set game difficulty.
     *
     * @param difficulty Pass target difficulty value.
     *                   Only LV_EASY and LV_HARD are available.
     */
    void setDifficulty(int difficulty);

    /**
     * @return Whether the 7-0 rule is enabled. In 7-0 rule, when a seven card
     *         is put down, the player must swap hand cards with another player
     *         immediately. When a zero card is put down, everyone need to pass
     *         the hand cards to the next player.
     */
    bool isSevenZeroRule();

    /**
     * @param enabled Enable/Disable the 7-0 rule.
     */
    void setSevenZeroRule(bool enabled);

    /**
     * @return Can or cannot stack +2 cards. If can, when you put down a +2
     *         card, the next player may transfer the punishment to its next
     *         player by stacking another +2 card. Finally the first one who
     *         does not stack a +2 card must draw all of the required cards.
     */
    bool isDraw2StackRule();

    /**
     * @param enabled Enable/Disable the +2 stacking rule.
     */
    void setDraw2StackRule(bool enabled);

    /**
     * Find a card instance in card table.
     *
     * @param color   Color of the card you want to get.
     * @param content Content of the card you want to get.
     * @return Corresponding card instance.
     */
    Card* findCard(Color color, Content content);

    /**
     * @return How many cards in deck (haven't been used yet).
     */
    int getDeckCount();

    /**
     * @return How many cards have been used.
     */
    int getUsedCount();

    /**
     * @return Recent played cards.
     */
    const std::vector<Card*>& getRecent();

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    void start();

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
    int draw(int who, bool force);

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card Check which card's legality.
     * @return Whether the specified card is legal to play.
     */
    bool isLegalToPlay(Card* card);

    /**
     * @return How many legal cards (the cards that can be played legally)
     *         in now player's hand.
     */
    int legalCardsCount4NowPlayer();

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
    Card* play(int who, int index, Color color);

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
    void swap(int a, int b);

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass the hand
     * cards to the next player.
     */
    void cycle();
}; // Uno Class

#endif // __UNO_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F