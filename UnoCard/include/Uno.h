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
     * @return Reference of the drawn card, or nullptr if the specified player
     *         didn't draw a card because of the limitation.
     */
    Card* draw(int who, bool force);

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
}; // Uno Class

#endif // __UNO_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F