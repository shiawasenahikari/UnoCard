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
     * Safe color.
     */
    Color safeColor = NONE;

    /**
     * Dangerous color.
     */
    Color dangerousColor = NONE;

    /**
     * How many dangerous cards (cards in dangerous color) in hand. THIS IS AN
     * ESTIMATED VALUE, NOT A REAL VALUE! This value is estimated by player's
     * actions, such as which color this player selected when playing a wild
     * card, and how many dangerous cards are played after that wild card.
     */
    int dangerousCount = 0;

    /**
     * Hand cards.
     */
    List<Card> handCards = new ArrayList<>();

    /**
     * Hand cards (read only version, provide for external accesses).
     */
    List<Card> handCards_readOnly = Collections.unmodifiableList(handCards);

    /**
     * Default constructor.
     */
    Player() {
        // NOP (Only change accessibility to package-private)
    } // Player() (Class Constructor)

    /**
     * @return This player's all hand cards.
     */
    public List<Card> getHandCards() {
        return handCards_readOnly;
    } // getHandCards()

    /**
     * When this player draw a card in action, record the previous played card's
     * color, as this player's safe color. What this player did means that this
     * player probably do not have cards in that color. You can use this value
     * to defend this player's UNO dash.
     *
     * @return This player's safe color, or
     * Color.NONE if no available safe color.
     */
    public Color getSafeColor() {
        return safeColor;
    } // getSafeColor()

    /**
     * When this player played a wild card, record the color specified, as this
     * player's dangerous color. The dangerous color will be remembered until
     * this player played a card matching that color. You can use this value to
     * defend this player's UNO dash.
     *
     * @return This player's dangerous color, or
     * Color.NONE if no available dangerous color.
     */
    public Color getDangerousColor() {
        return dangerousColor;
    } // getDangerousColor()

    /**
     * Evaluate which color is the best color for this player. In our evaluation
     * system, zero/reverse cards are worth 2 points, non-zero number cards are
     * worth 4 points, and skip/+2 cards are worth 5 points. Finally, the color
     * which contains the worthiest cards becomes the best color.
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
            switch (card.content) {
                case REV:
                case NUM0:
                    score[card.color.ordinal()] += 2;
                    break; // case REV, NUM0

                case SKIP:
                case DRAW2:
                    score[card.color.ordinal()] += 5;
                    break; // case SKIP, DRAW2

                default:
                    score[card.color.ordinal()] += 4;
                    break; // default
            } // switch (card.content)
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