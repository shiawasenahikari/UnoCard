////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
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
private:
    /**
     * Uno runtime.
     */
    Uno* uno = Uno::getInstance();

public:
    /**
     * Evaluate which color is the best for current player. In our evaluation
     * system, zero cards / reverse cards are worth 2 points, non-zero number
     * cards are worth 4 points, and skip / draw two cards are worth 5 points.
     * Finally, the color which contains the worthiest cards becomes the best
     * color.
     *
     * @return Current player's best color.
     */
    Color calcBestColor4NowPlayer();

    /**
     * In 7-0 rule, when a seven card is put down, the player must swap hand
     * cards with another player immediately. This API returns that swapping
     * with whom is the best answer for current player.
     *
     * @return Current player swaps with whom. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    int bestSwapTarget4NowPlayer();

    /**
     * AI strategies of determining if it's necessary to challenge previous
     * player's [wild +4] card's legality.
     *
     * @return True if it's necessary to make a challenge.
     */
    bool needToChallenge();

    /**
     * AI Strategies (Difficulty: EASY). Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param outColor This is a out parameter. Pass a Color array (length>=1)
     *                 in order to let us pass the return value by assigning
     *                 outColor[0]. When the best card to play becomes a wild
     *                 card, outColor[0] will become the following legal color
     *                 to change. When the best card to play becomes an action
     *                 or a number card, outColor[0] will become the player's
     *                 best color.
     * @return Index of the best card to play, in current player's hand.
     *         Or a negative number that means no appropriate card to play.
     */
    int easyAI_bestCardIndex4NowPlayer(Color outColor[]);

    /**
     * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
     * and calculate which is the best card to play.
     *
     * @param outColor This is a out parameter. Pass a Color array (length>=1)
     *                 in order to let us pass the return value by assigning
     *                 outColor[0]. When the best card to play becomes a wild
     *                 card, outColor[0] will become the following legal color
     *                 to change. When the best card to play becomes an action
     *                 or a number card, outColor[0] will become the player's
     *                 best color.
     * @return Index of the best card to play, in current player's hand.
     *         Or a negative number that means no appropriate card to play.
     */
    int hardAI_bestCardIndex4NowPlayer(Color outColor[]);

    /**
     * AI Strategies in 7-0 special rule. Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param outColor This is a out parameter. Pass a Color array (length>=1)
     *                 in order to let us pass the return value by assigning
     *                 outColor[0]. When the best card to play becomes a wild
     *                 card, outColor[0] will become the following legal color
     *                 to change. When the best card to play becomes an action
     *                 or a number card, outColor[0] will become the player's
     *                 best color.
     * @return Index of the best card to play, in current player's hand.
     *         Or a negative number that means no appropriate card to play.
     */
    int sevenZeroAI_bestCardIndex4NowPlayer(Color outColor[]);
}; // AI Class

#endif // __AI_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F