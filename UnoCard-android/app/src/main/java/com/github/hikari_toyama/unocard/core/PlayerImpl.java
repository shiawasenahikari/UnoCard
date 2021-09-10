////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import java.util.Collections;
import java.util.List;

/**
 * Store an Uno player's real-time information,
 * such as hand cards, and recent played card.
 */
class PlayerImpl extends Player {
    /**
     * Hand cards (read only version, provide for external accesses).
     */
    private final List<Card> handCards_readOnly;

    /**
     * Default constructor.
     */
    PlayerImpl() {
        handCards_readOnly = Collections.unmodifiableList(handCards);
    } // PlayerImpl() (Class Constructor)

    /**
     * @return This player's all hand cards.
     */
    @Override
    public List<Card> getHandCards() {
        return handCards_readOnly;
    } // getHandCards()

    /**
     * Calculate the total score of this player's hand cards. According to the
     * official rule, Wild Cards are worth 50 points, Action Cards are worth 20
     * points, and Number Cards are worth points that equals to the number.
     *
     * @return Score of this player's hand cards.
     */
    @Override
    public int getHandScore() {
        int score = 0;
        for (Card card : handCards) {
            switch (card.content) {
                case WILD:
                case WILD_DRAW4:
                    score += 50;
                    break; // case WILD, WILD_DRAW4

                case REV:
                case SKIP:
                case DRAW2:
                    score += 20;
                    break; // case REV, SKIP, DRAW2

                default: // Number Cards
                    score += card.content.ordinal();
                    break; // default
            } // switch (card.content)
        } // for (Card card : handCards)

        return score;
    } // getHandScore()

    /**
     * @return How many cards in this player's hand.
     */
    @Override
    public int getHandSize() {
        return handCards.size();
    } // getHandSize()

    /**
     * When this player played a wild card, record the color specified, as this
     * player's strong color. The strong color will be remembered until this
     * player played a number of card matching that color. You can use this
     * value to defend this player's UNO dash.
     *
     * @return This player's strong color, or Color.NONE if no available
     * strong color.
     */
    @Override
    public Color getStrongColor() {
        return strongColor;
    } // getStrongColor()

    /**
     * When this player draw a card in action, record the previous played card's
     * color, as this player's weak color. What this player did means that this
     * player probably do not have cards in that color. You can use this value
     * to defend this player's UNO dash.
     *
     * @return This player's weak color, or Color.NONE if no available weak
     * color.
     */
    @Override
    public Color getWeakColor() {
        return weakColor;
    } // getWeakColor()

    /**
     * @return This player's recent played card, or null if this player drew
     * one or more cards in its previous action.
     */
    @Override
    public Card getRecent() {
        return recent;
    } // getRecent()
} // PlayerImpl Class

// E.O.F