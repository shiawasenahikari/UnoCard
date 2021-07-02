////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
// COPYRIGHT HIKARI TOYAMA, 1992-2021. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <vector>
#include <Card.h>
#include <Color.h>
#include <Player.h>

/**
 * @return This player's all hand cards.
 */
const std::vector<Card*>& Player::getHandCards() {
	return handCards;
} // getHandCards()

/**
 * When this player played a wild card, record the color specified, as this
 * player's dangerous color. The dangerous color will be remembered until
 * this player played a number of card matching that color. You can use
 * this value to defend this player's UNO dash.
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
 * @return This player's recent played card, or nullptr if this player drew
 *         one or more cards in its previous action.
 */
Card* Player::getRecent() {
	return recent;
} // getRecent()

// E.O.F