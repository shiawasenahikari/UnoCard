////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.hikari_toyama.unocard.core.Color.BLUE;
import static com.github.hikari_toyama.unocard.core.Color.GREEN;
import static com.github.hikari_toyama.unocard.core.Color.NONE;
import static com.github.hikari_toyama.unocard.core.Color.RED;
import static com.github.hikari_toyama.unocard.core.Color.YELLOW;

/**
 * Store an Uno player's real-time information,
 * such as hand cards, and recent played card.
 */
@SuppressWarnings("ALL")
public class Player {
    /**
     * Your player ID.
     */
    public static final int YOU = 0;

    /**
     * WEST's player ID.
     */
    public static final int COM1 = 1;

    /**
     * NORTH's player ID.
     */
    public static final int COM2 = 2;

    /**
     * EAST's player ID.
     */
    public static final int COM3 = 3;

    /**
     * Recent played card. If the player drew one or more cards in its last
     * action, this member will be null.
     */
    Card recent = null;

    /**
     * Hand cards.
     */
    List<Card> handCards;

    /**
     * Hand cards (read only version, provide for external accesses).
     */
    List<Card> handCards_readOnly;

    /**
     * Default constructor.
     */
    Player() {
        handCards = new ArrayList<>();
        handCards_readOnly = Collections.unmodifiableList(handCards);
    } // Player() (Class Constructor)

    /**
     * @return This player's all hand cards.
     */
    public List<Card> getHandCards() {
        return handCards_readOnly;
    } // getHandCards()

    /**
     * Normally return Color.NONE, but when this player started an UNO dash
     * with a wild card, call this method to get which color was specified.
     *
     * @return This player's dangerous color.
     */
    public Color getDangerousColor() {
        Color dangerousColor;

        if (handCards.size() == 1 && recent.isWild()) {
            dangerousColor = recent.color;
        } // if (handCards.size() == 1 && recent.isWild())
        else {
            dangerousColor = NONE;
        } // else

        return dangerousColor;
    } // getDangerousColor()

    /**
     * Evaluate which color is the best color for this player. In our evaluation
     * system, zero cards are worth 2 points, non-zero number cards are worth 4
     * points, and action cards are worth 5 points. Finally, the color which
     * contains the worthiest cards becomes the best color.
     *
     * @return This player's best color. Specially, when the player remains only
     * wild cards, method will return a default value, Color.RED.
     */
    public Color calcBestColor() {
        Color best;
        int[] score;

        best = RED;
        score = new int[]{0, 0, 0, 0, 0};
        for (Card card : handCards) {
            if (card.isZero()) {
                score[card.color.ordinal()] += 2;
            } // if (card.isZero())
            else if (card.isAction()) {
                score[card.color.ordinal()] += 5;
            } // else if (card.isAction())
            else {
                score[card.color.ordinal()] += 4;
            } // else
        } // for (Card card : handCards)

        // default to red, when only wild cards in hand,
        // method will return Color.RED
        if (score[BLUE.ordinal()] > score[best.ordinal()]) {
            best = BLUE;
        } // if (score[BLUE.ordinal()] > score[best.ordinal()])

        if (score[GREEN.ordinal()] > score[best.ordinal()]) {
            best = GREEN;
        } // if (score[GREEN.ordinal()] > score[best.ordinal()])

        if (score[YELLOW.ordinal()] > score[best.ordinal()]) {
            best = YELLOW;
        } // if (score[YELLOW.ordinal()] > score[best.ordinal()])

        return best;
    } // calcBestColor()

    /**
     * @return This player's recent played card, or null if this player drew one
     * or more cards in its previous action.
     */
    public Card getRecent() {
        return recent;
    } // getRecent()
} // Player Class

// E.O.F