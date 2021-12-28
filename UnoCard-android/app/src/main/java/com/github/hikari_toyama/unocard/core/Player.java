////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
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
    List<Card> handCards = new ArrayList<>();

    /**
     * Strong color.
     */
    Color strongColor = Color.NONE;

    /**
     * Weak color.
     */
    Color weakColor = Color.NONE;

    /**
     * Recent played card. If the player drew one or more cards in its last
     * action, this member will be null.
     */
    Card recent = null;

    /**
     * How many dangerous cards (cards in strong color) in hand. THIS IS AN
     * ESTIMATED VALUE, NOT A REAL VALUE! This value is estimated by player's
     * actions, such as which color this player selected when playing a wild
     * card, and how many dangerous cards are played after that wild card.
     */
    int strongCount = 0;

    /**
     * This binary values shows the visibility of your cards. The card of
     * handCards.at(i) is known by you when 0x01 == ((open >> i) & 0x01).
     */
    int open = 0x00000000;

    /**
     * Default constructor.
     */
    Player() {
        // NOP (only change accessibility to package-private)
    } // Player() (Class Constructor)

    /**
     * @return This player's all hand cards.
     */
    public abstract List<Card> getHandCards();

    /**
     * Calculate the total score of this player's hand cards. According to the
     * official rule, Wild Cards are worth 50 points, Action Cards are worth 20
     * points, and Number Cards are worth points that equals to the number.
     *
     * @return Score of this player's hand cards.
     */
    public abstract int getHandScore();

    /**
     * @return How many cards in this player's hand.
     */
    public abstract int getHandSize();

    /**
     * When this player played a wild card, record the color specified, as this
     * player's strong color. The strong color will be remembered until this
     * player played a number of card matching that color. You can use this
     * value to defend this player's UNO dash.
     *
     * @return This player's strong color, or Color.NONE if no available
     * strong color.
     */
    public abstract Color getStrongColor();

    /**
     * When this player draw a card in action, record the previous played card's
     * color, as this player's weak color. What this player did means that this
     * player probably do not have cards in that color. You can use this value
     * to defend this player's UNO dash.
     *
     * @return This player's weak color, or Color.NONE if no available weak
     * color.
     */
    public abstract Color getWeakColor();

    /**
     * @return This player's recent played card, or null if this player drew
     * one or more cards in its previous action.
     */
    public abstract Card getRecent();

    /**
     * @deprecated Call this.isOpen(-1).
     */
    @Deprecated
    public boolean isOpen() {
        return isOpen(-1);
    } // isOpen()

    /**
     * Check whether this player's hand cards are known by you, i.e. the unique
     * non-AI player. In 7-0 rule, when a seven or zero card is put down, and
     * your hand cards are transferred to someone else (for example, A), then
     * A's all hand cards are known by you.
     *
     * @param index Index of the card to check (0 ~ this.handCards.size() - 1).
     *              If you pass -1, check all hand cards.
     * @return Whether this player's specified card is known by you. If index is
     * -1, this method will return true only when ALL OF THIS PLAYER'S
     * HAND CARDS are known by you.
     */
    public abstract boolean isOpen(int index);

    /**
     * Call this method to rearrange this player's hand cards.
     * The cards with same color will be arranged together.
     */
    public abstract void sort();
} // Player Abstract Class

// E.O.F