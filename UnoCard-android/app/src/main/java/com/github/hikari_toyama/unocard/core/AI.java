////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import android.content.Context;

/**
 * AI Strategies.
 */
public abstract class AI {
    /**
     * In MainActivity Class, get AI instance here.
     */
    public static AI getInstance(Context context) {
        return new AIImpl(context);
    } // getInstance()

    /**
     * AI strategies of determining if it's necessary to challenge previous
     * player's [wild +4] card's legality.
     *
     * @param challenger Who challenges. Must be one of the following values:
     *                   Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return True if it's necessary to make a challenge.
     */
    public abstract boolean needToChallenge(int challenger);

    /**
     * AI Strategies (Difficulty: EASY). Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass null. If drew a card from deck, then
     *                  you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let us pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    public abstract int easyAI_bestCardIndex4NowPlayer(Card drawnCard,
                                                       Color[] outColor);

    /**
     * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
     * and calculate which is the best card to play.
     *
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass null. If drew a card from deck, then
     *                  you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let us pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    public abstract int hardAI_bestCardIndex4NowPlayer(Card drawnCard,
                                                       Color[] outColor);
} // AI Abstract Class

// E.O.F