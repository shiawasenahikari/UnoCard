////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include <vector>
#include <Card.h>
#include <Color.h>
#include <Player.h>
#include <Content.h>

/**
 * @return This player's all hand cards.
 */
const std::vector<Card*>& Player::getHandCards() {
	return handCards;
} // getHandCards()

/**
 * When this player played a wild card, record the color specified, as this
 * player's dangerous color. The dangerous color will be remembered until
 * this player played a card matching that color. You can use this value to
 * defend this player's UNO dash.
 *
 * @return This player's dangerous color, or Color::NONE if no available
 *         dangerous color.
 */
Color Player::getDangerousColor() {
	return dangerousColor;
} // getDangerousColor()

/**
 * When this player draw a card in action, record the previous played card's
 * color, as this player's safe color. What this player did means that this
 * player probably do not have cards in that color. You can use this value
 * to defend this player's UNO dash.
 *
 * @return This player's safe color, or Color::NONE if no available safe
 *         color.
 */
Color Player::getSafeColor() {
	return safeColor;
} // getSafeColor()

/**
 * Evaluate which color is the best color for this player. In our evaluation
 * system, zero/reverse cards are worth 2 points, non-zero number cards are
 * worth 4 points, and skip/+2 cards are worth 5 points. Finally, the color
 * which contains the worthiest cards becomes the best color.
 *
 * @return This player's best color. Specially, when the player remains only
 *         wild cards, function will return a default value, Color::RED.
 */
Color Player::calcBestColor() {
	Color best = RED;
	int score[5] = { 0, 0, 0, 0, 0 };

	for (Card* card : handCards) {
		switch (card->content) {
		case REV:
		case NUM0:
			score[card->getRealColor()] += 2;
			break; // case REV, NUM0

		case SKIP:
		case DRAW2:
			score[card->getRealColor()] += 5;
			break; // case SKIP, DRAW2

		default:
			score[card->getRealColor()] += 4;
			break; // default
		} // switch (card->content)
	} // for (Card* card : handCards)

	// default to red, when only wild cards in hand,
	// function will return Color::RED
	if (score[BLUE] > score[best]) {
		best = BLUE;
	} // if (score[BLUE] > score[best]

	if (score[GREEN] > score[best]) {
		best = GREEN;
	} // if (score[GREEN] > score[best])

	if (score[YELLOW] > score[best]) {
		best = YELLOW;
	} // if (score[YELLOW] > score[best])

	return best;
} // calcBestColor()

/**
 * @return This player's recent played card, or nullptr if this player drew
 *         one or more cards in its previous action.
 */
Card* Player::getRecent() {
	return recent;
} // getRecent()

// E.O.F