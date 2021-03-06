////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
// COPYRIGHT HIKARI TOYAMA, 1992-2021. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Store an Uno player's real-time information,
 * such as hand cards, and recent played card.
 */
public abstract class Player {
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
     * Hand cards.
     */
    List<Card> handCards;

    /**
     * Dangerous color.
     */
    Color dangerousColor = Color.NONE;

    /**
     * Safe color.
     */
    Color safeColor = Color.NONE;

    /**
     * Recent played card. If the player drew one or more cards in its last
     * action, this member will be null.
     */
    Card recent = null;

    /**
     * How many dangerous cards (cards in dangerous color) in hand. THIS IS AN
     * ESTIMATED VALUE, NOT A REAL VALUE! This value is estimated by player's
     * actions, such as which color this player selected when playing a wild
     * card, and how many dangerous cards are played after that wild card.
     */
    int dangerousCount = 0;

    /**
     * Default constructor.
     */
    Player() {
        handCards = new ArrayList<>();
    } // Player()

    /**
     * @return This player's all hand cards.
     */
    public abstract List<Card> getHandCards();

    /**
     * When this player played a wild card, record the color specified, as this
     * player's dangerous color. The dangerous color will be remembered until
     * this player played a number of card matching that color. You can use
     * this value to defend this player's UNO dash.
     *
     * @return This player's dangerous color, or Color.NONE if no available
     * dangerous color.
     */
    public abstract Color getDangerousColor();

    /**
     * When this player draw a card in action, record the previous played card's
     * color, as this player's safe color. What this player did means that this
     * player probably do not have cards in that color. You can use this value
     * to defend this player's UNO dash.
     *
     * @return This player's safe color, or Color.NONE if no available safe
     * color.
     */
    public abstract Color getSafeColor();

    /**
     * @return This player's recent played card, or null if this player drew
     * one or more cards in its previous action.
     */
    public abstract Card getRecent();
} // Player Abstract Class

// E.O.F