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
 * AI Strategies (Difficulty: EASY).
 *
 * @param whom      Analyze whose hand cards. Must be one of the following:
 *                  Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @param drawnCard When the specified player drew a card in its turn just
 *                  now, pass the drawn card. If not, pass nullptr. If drew
 *                  a card from deck, then you can play only the drawn card,
 *                  but not the other cards in your hand, immediately.
 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
 *                  in order to let we pass the return value by assigning
 *                  outColor[0]. When the best card to play becomes a wild
 *                  card, outColor[0] will become the following legal color
 *                  to change. When the best card to play becomes an action
 *                  or a number card, outColor[0] will become the player's
 *                  best color.
 * @return Index of the best card to play, in the specified player's hand.
 *         Or a negative number that means no appropriate card to play.
 */
int easyAI_bestCardIndexFor(int whom, Card* drawnCard, Color outColor[]);

/**
 * AI Strategies (Difficulty: HARD).
 *
 * @param whom      Analyze whose hand cards. Must be one of the following:
 *                  Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @param drawnCard When the specified player drew a card in its turn just
 *                  now, pass the drawn card. If not, pass nullptr. If drew
 *                  a card from deck, then you can play only the drawn card,
 *                  but not the other cards in your hand, immediately.
 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
 *                  in order to let we pass the return value by assigning
 *                  outColor[0]. When the best card to play becomes a wild
 *                  card, outColor[0] will become the following legal color
 *                  to change. When the best card to play becomes an action
 *                  or a number card, outColor[0] will become the player's
 *                  best color.
 * @return Index of the best card to play, in the specified player's hand.
 *         Or a negative number that means no appropriate card to play.
 */
int hardAI_bestCardIndexFor(int whom, Card* drawnCard, Color outColor[]);

#endif // __AI_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F