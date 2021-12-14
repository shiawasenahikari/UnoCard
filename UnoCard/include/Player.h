////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __PLAYER_H_494649FDFA62B3C015120BCB9BE17613__
#define __PLAYER_H_494649FDFA62B3C015120BCB9BE17613__

#include <vector>
#include "include/Card.h"
#include "include/Color.h"

/**
 * Store an Uno player's real-time information,
 * such as hand cards, and recent played card.
 */
class Player {
private:
    /**
     * Default constructor.
     */
    Player() = default;

    /**
     * Hand cards.
     */
    std::vector<Card*> handCards;

    /**
     * Strong color.
     */
    Color strongColor = NONE;

    /**
     * Weak color.
     */
    Color weakColor = NONE;

    /**
     * Recent played card. If the player drew one or more cards in its last
     * action, this member will be nullptr.
     */
    Card* recent = nullptr;

    /**
     * How many dangerous cards (cards in strong color) in hand. THIS IS AN
     * ESTIMATED VALUE, NOT A REAL VALUE! This value is estimated by player's
     * actions, such as which color this player selected when playing a wild
     * card, and how many dangerous cards are played after that wild card.
     */
    int strongCount = 0;

    /**
     * Whether this player's hand cards are known by you, i.e. the unique
     * non-AI player.
     */
    bool open = false;

    /**
     * Grant Uno class to access our constructors (to create Player instances)
     * and our private fields (to change players' real-time information).
     */
    friend class Uno;

public:
    /**
     * Your player ID.
     */
    static const int YOU = 0;

    /**
     * WEST's player ID.
     */
    static const int COM1 = 1;

    /**
     * NORTH's player ID.
     */
    static const int COM2 = 2;

    /**
     * EAST's player ID.
     */
    static const int COM3 = 3;

    /**
     * @return This player's all hand cards.
     */
    const std::vector<Card*>& getHandCards();

    /**
     * Calculate the total score of this player's hand cards. According to the
     * official rule, Wild Cards are worth 50 points, Action Cards are worth 20
     * points, and Number Cards are worth points that equals to the number.
     *
     * @return Score of this player's hand cards.
     */
    int getHandScore();

    /**
     * @return How many cards in this player's hand.
     */
    int getHandSize();

    /**
     * When this player played a wild card, record the color specified, as this
     * player's strong color. The strong color will be remembered until this
     * player played a number of card matching that color. You can use this
     * value to defend this player's UNO dash.
     *
     * @return This player's strong color, or Color::NONE if no available
     *         strong color.
     */
    Color getStrongColor();

    /**
     * When this player draw a card in action, record the previous played card's
     * color, as this player's weak color. What this player did means that this
     * player probably do not have cards in that color. You can use this value
     * to defend this player's UNO dash.
     *
     * @return This player's weak color, or Color::NONE if no available weak
     *         color.
     */
    Color getWeakColor();

    /**
     * @return This player's recent played card, or nullptr if this player drew
     *         one or more cards in its previous action.
     */
    Card* getRecent();

    /**
     * @return Whether this player's hand cards are known by you, i.e. the
     *         unique non-AI player. In 7-0 rule, when a seven or zero card is
     *         put down, and your hand cards are transferred to someone else
     *         (for example, A), then A's all hand cards are known by you.
     */
    bool isOpen();
}; // Player Class

#endif // __PLAYER_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F