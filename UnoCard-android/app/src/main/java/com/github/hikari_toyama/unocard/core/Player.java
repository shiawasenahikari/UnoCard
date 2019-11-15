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

/**
 * Store a Uno player's real-time information,
 * such as hand cards, and recent played card.
 */
public class Player {
    /**
     * Hand cards.
     */
    List<Card> handCards = new ArrayList<>();

    /**
     * Recent played card. If the player drew one or more cards in its last
     * action, this member will be null.
     */
    Card recent = null;

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
        return Collections.unmodifiableList(handCards);
    } // getHandCards()

    /**
     * @return This player's recent played card, or null if this player drew one
     * or more cards in its previous action.
     */
    public Card getRecent() {
        return recent;
    } // getRecent()
} // Player Class

// E.O.F