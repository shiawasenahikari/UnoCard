////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __AI_H_494649FDFA62B3C015120BCB9BE17613__
#define __AI_H_494649FDFA62B3C015120BCB9BE17613__

#include <Uno.h>
#include <Card.h>
#include <Color.h>

/**
 * AI Strategies.
 */
class AI {
public:
	/**
	 * AI strategies of determining if it's necessary to challenge previous
	 * player's [wild +4] card's legality.
	 *
	 * @param challenger Who challenges. Must be one of the following values:
	 *                   Player::YOU, Player::COM1, Player::COM2, Player::COM3.
	 * @return Whether the challenger needs to make a challenge.
	 */
	bool needToChallenge(int challenger);

	/**
	 * AI Strategies (Difficulty: EASY). Analyze current player's hand cards,
	 * and calculate which is the best card to play out.
	 *
	 * @param drawnCard When current player drew a card just now, pass the drawn
	 *                  card. If not, pass nullptr. If drew a card from deck,
	 *                  then you can play only the drawn card, but not the other
	 *                  cards in your hand, immediately.
	 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
	 *                  in order to let we pass the return value by assigning
	 *                  outColor[0]. When the best card to play becomes a wild
	 *                  card, outColor[0] will become the following legal color
	 *                  to change. When the best card to play becomes an action
	 *                  or a number card, outColor[0] will become the player's
	 *                  best color.
	 * @return Index of the best card to play, in current player's hand.
	 *         Or a negative number that means no appropriate card to play.
	 */
	int easyAI_bestCardIndex4NowPlayer(Card* drawnCard, Color outColor[]);

	/**
	 * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
	 * and calculate which is the best card to play.
	 *
	 * @param drawnCard When current player drew a card just now, pass the drawn
	 *                  card. If not, pass nullptr. If drew a card from deck,
	 *                  then you can play only the drawn card, but not the other
	 *                  cards in your hand, immediately.
	 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
	 *                  in order to let we pass the return value by assigning
	 *                  outColor[0]. When the best card to play becomes a wild
	 *                  card, outColor[0] will become the following legal color
	 *                  to change. When the best card to play becomes an action
	 *                  or a number card, outColor[0] will become the player's
	 *                  best color.
	 * @return Index of the best card to play, in current player's hand.
	 *         Or a negative number that means no appropriate card to play.
	 */
	int hardAI_bestCardIndex4NowPlayer(Card* drawnCard, Color outColor[]);

private:
	/**
	 * Uno runtime.
	 */
	Uno* uno = Uno::getInstance();

	/**
	 * Evaluate which color is the best for current player. In our evaluation
	 * system, zero cards are worth 2 points, non-zero number cards are worth
	 * 4 points, and action cards are worth 5 points. Finally, the color which
	 * contains the worthiest cards becomes the best color.
	 *
	 * @return Current player's best color.
	 */
	Color calcBestColor4NowPlayer();
}; // AI Class

#endif // __AI_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F