////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
// COPYRIGHT HIKARI TOYAMA, 1992-2021. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import android.content.Context;

import org.opencv.core.Mat;

import java.util.List;

/**
 * Uno Runtime Class (Singleton).
 */
public abstract class Uno {
    /**
     * Easy level ID.
     */
    public static final int LV_EASY = 0;

    /**
     * Hard level ID.
     */
    public static final int LV_HARD = 1;

    /**
     * Direction value (clockwise).
     */
    public static final int DIR_LEFT = 1;

    /**
     * Direction value (counter-clockwise).
     */
    public static final int DIR_RIGHT = 3;

    /**
     * In this application, everyone can hold 14 cards at most.
     */
    public static final int MAX_HOLD_CARDS = 14;

    /**
     * Singleton.
     */
    private static volatile Uno INSTANCE;

    /**
     * Card table.
     */
    Card[] table;

    /**
     * Image resources for wild cards.
     */
    Mat[] wImage;

    /**
     * Image resources for wild +4 cards.
     */
    Mat[] w4Image;

    /**
     * Card back image resource.
     */
    Mat backImage;

    /**
     * Background image resource (for welcome screen).
     */
    Mat bgWelcome;

    /**
     * Background image resource (Direction: COUNTER CLOCKWISE).
     */
    Mat bgCounter;

    /**
     * Background image resource (Direction: CLOCKWISE).
     */
    Mat bgClockwise;

    /**
     * Difficulty button image resources (EASY).
     */
    Mat easyImage, easyImage_d;

    /**
     * Difficulty button image resources (HARD).
     */
    Mat hardImage, hardImage_d;

    /**
     * Player in turn. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    int now;

    /**
     * How many players in game. Supports 3 or 4.
     */
    int players;

    /**
     * Current action sequence (DIR_LEFT / DIR_RIGHT).
     */
    int direction;

    /**
     * Current difficulty (LV_EASY / LV_HARD).
     */
    int difficulty;

    /**
     * Game players.
     */
    Player[] player;

    /**
     * Card deck (ready to use).
     */
    List<Card> deck;

    /**
     * Used cards.
     */
    List<Card> used;

    /**
     * Recent played cards.
     */
    List<Card> recent;

    /**
     * In MainActivity Class, get Uno instance here.
     *
     * @param context Pass a context object to let us get the card image
     *                resources stored in this application.
     * @return Reference of our singleton.
     */
    public static Uno getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Uno.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UnoImpl(context);
                } // if (INSTANCE == null)
            } // synchronized (Uno.class)
        } // if (INSTANCE == null)

        return INSTANCE;
    } // getInstance()

    /**
     * @return Card back image resource.
     */
    public abstract Mat getBackImage();

    /**
     * @param level   Pass LV_EASY or LV_HARD.
     * @param hiLight Pass true if you want to get a hi-lighted image,
     *                or false if you want to get a dark image.
     * @return Corresponding difficulty button image.
     */
    public abstract Mat getLevelImage(int level, boolean hiLight);

    /**
     * @return Background image resource in current direction.
     */
    public abstract Mat getBackground();

    /**
     * When a player played a wild card and specified a following legal color,
     * get the corresponding color-filled image here, and show it in recent
     * card area.
     *
     * @param color The wild image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    public abstract Mat getColoredWildImage(Color color);

    /**
     * When a player played a wild +4 card and specified a following legal
     * color, get the corresponding color-filled image here, and show it in
     * recent card area.
     *
     * @param color The wild +4 image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    public abstract Mat getColoredWildDraw4Image(Color color);

    /**
     * @return Player in turn. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public abstract int getNow();

    /**
     * Switch to next player's turn.
     *
     * @return Player in turn after switched. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public abstract int switchNow();

    /**
     * @return Current player's next player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public abstract int getNext();

    /**
     * @return Current player's opposite player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * NOTE: When only 3 players in game, getOppo() == getPrev().
     */
    public abstract int getOppo();

    /**
     * @return Current player's previous player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public abstract int getPrev();

    /**
     * @return How many players in game (3 or 4).
     */
    public abstract int getPlayers();

    /**
     * Set the amount of players in game.
     *
     * @param players Supports 3 and 4.
     */
    public abstract void setPlayers(int players);

    /**
     * Switch current action sequence.
     *
     * @return Switched action sequence. DIR_LEFT for clockwise,
     * or DIR_RIGHT for counter-clockwise.
     */
    public abstract int switchDirection();

    /**
     * @param who Get which player's instance. Must be one of the following:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Specified player's instance.
     */
    public abstract Player getPlayer(int who);

    /**
     * @return Current difficulty (LV_EASY / LV_HARD).
     */
    public abstract int getDifficulty();

    /**
     * Set game difficulty.
     *
     * @param difficulty Pass target difficulty value.
     *                   Only LV_EASY and LV_HARD are available.
     */
    public abstract void setDifficulty(int difficulty);

    /**
     * Find a card instance in card table.
     *
     * @param color   Color of the card you want to get.
     * @param content Content of the card you want to get.
     * @return Corresponding card instance.
     */
    public abstract Card findCard(Color color, Content content);

    /**
     * @return How many cards in deck (haven't been used yet).
     */
    public abstract int getDeckCount();

    /**
     * @return How many cards have been used.
     */
    public abstract int getUsedCount();

    /**
     * @return Recent played cards.
     */
    public abstract List<Card> getRecent();

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    public abstract void start();

    /**
     * Call this function when someone needs to draw a card.
     * <p>
     * NOTE: Everyone can hold 14 cards at most in this program, so even if this
     * function is called, the specified player may not draw a card as a result.
     *
     * @param who   Who draws a card. Must be one of the following values:
     *              Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param force Pass true if the specified player is required to draw cards,
     *              i.e. previous player played a [+2] or [wild +4] to let this
     *              player draw cards. Or false if the specified player draws a
     *              card by itself in its action.
     * @return Reference of the drawn card, or null if the specified player
     * didn't draw a card because of the limitation.
     */
    public abstract Card draw(int who, boolean force);

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card Check which card's legality.
     * @return Whether the specified card is legal to play.
     */
    public abstract boolean isLegalToPlay(Card card);

    /**
     * @return How many legal cards (the cards that can be played legally)
     * in now player's hand.
     */
    public abstract int legalCardsCount4NowPlayer();

    /**
     * Call this function when someone needs to play a card. The played card
     * replaces the "previous played card", and the original "previous played
     * card" becomes a used card at the same time.
     * <p>
     * NOTE: Before calling this function, you must call isLegalToPlay(Card)
     * function at first to check whether the specified card is legal to play.
     * This function will play the card directly without checking the legality.
     *
     * @param who   Who plays a card. Must be one of the following values:
     *              Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param index Play which card. Pass the corresponding card's index of the
     *              specified player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     * @return Reference of the played card.
     */
    public abstract Card play(int who, int index, Color color);
} // Uno Class

// E.O.F