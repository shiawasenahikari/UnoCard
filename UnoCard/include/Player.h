////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __PLAYER_H_494649FDFA62B3C015120BCB9BE17613__
#define __PLAYER_H_494649FDFA62B3C015120BCB9BE17613__

#include <vector>
#include <Card.h>
#include <Color.h>

/**
 * Store an Uno player's real-time information,
 * such as hand cards, and recent played card.
 */
class Player {
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
	 * When this player played a wild card, record the color specified, as this
	 * player's dangerous color. The dangerous color will be remembered until
	 * this player played a card matching that color. You can use this value to
	 * defend this player's UNO dash.
	 *
	 * @return This player's dangerous color, or Color::NONE if no available
	 *         dangerous color.
	 */
	Color getDangerousColor();

	/**
	 * When this player draw a card in action, record the previous played card's
	 * color, as this player's safe color. What this player did means that this
	 * player probably do not have cards in that color. You can use this value
	 * to defend this player's UNO dash.
	 *
	 * @return This player's safe color, or Color::NONE if no available safe
	 *         color.
	 */
	Color getSafeColor();

	/**
	 * Evaluate which color is the best color for this player. In our evaluation
	 * system, zero cards are worth 2 points, non-zero number cards are worth 4
	 * points, and action cards are worth 5 points. Finally, the color which
	 * contains the worthiest cards becomes the best color.
	 *
	 * @return This player's best color. Specially, when the player remains only
	 *         wild cards, function will return a default value, Color::RED.
	 * @deprecated Call AI::calcBestColor4NowPlayer() instead.
	 */
	[[deprecated]]
	Color calcBestColor();

	/**
	 * @return This player's recent played card, or nullptr if this player drew
	 *         one or more cards in its previous action.
	 */
	Card* getRecent();

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
	 * Dangerous color.
	 */
	Color dangerousColor = NONE;

	/**
	 * Safe color.
	 */
	Color safeColor = NONE;

	/**
	 * Recent played card. If the player drew one or more cards in its last
	 * action, this member will be nullptr.
	 */
	Card* recent = nullptr;

	/**
	 * How many dangerous cards (cards in dangerous color) in hand. THIS IS AN
	 * ESTIMATED VALUE, NOT A REAL VALUE! This value is estimated by player's
	 * actions, such as which color this player selected when playing a wild
	 * card, and how many dangerous cards are played after that wild card.
	 */
	int dangerousCount = 0;

	/**
	 * Grant Uno class to access our constructors (to create Player instances)
	 * and our private fields (to change players' real-time information).
	 */
	friend class Uno;
}; // Player Class

#endif // __PLAYER_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F