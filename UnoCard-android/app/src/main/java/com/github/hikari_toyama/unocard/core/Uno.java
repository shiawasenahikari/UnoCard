////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import android.content.Context;

import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
     * Random number generator.
     */
    public static final Random RNG = new Random();

    /**
     * Singleton.
     */
    private static volatile Uno INSTANCE;

    /**
     * Card map. table[i] stores the card instance of id number i.
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
     * Whether the force play rule is enabled.
     */
    boolean forcePlay;

    /**
     * Whether the 7-0 rule is enabled.
     */
    boolean sevenZeroRule;

    /**
     * Can or cannot stack +2 cards.
     */
    boolean draw2StackRule;

    /**
     * Only available in +2 stack rule. In this rule, when a +2 card is put
     * down, the next player may transfer the punishment to its next player
     * by stacking another +2 card. Finally the first one who does not stack
     * a +2 card must draw all of the required cards. This counter records
     * that how many required cards need to be drawn by the final player.
     * When this value is not zero, only +2 cards are legal to play.
     */
    int draw2StackCount;

    /**
     * This binary value shows that which cards are legal to play. When
     * 0x01L == ((legality >> i) & 0x01L), the card with id number i
     * is legal to play. When the recent-played-card queue changes,
     * this value will be updated automatically.
     */
    long legality;

    /**
     * Game players.
     */
    Player[] player;

    /**
     * Card deck (ready to use).
     */
    List<Card> deck = new ArrayList<>();

    /**
     * Used cards.
     */
    List<Card> used = new ArrayList<>();

    /**
     * Recent played cards.
     */
    List<Card> recent = new ArrayList<>();

    /**
     * Colors of recent played cards.
     */
    List<Color> recentColors = new ArrayList<>();

    /**
     * In MainActivity Class, get Uno instance here.
     *
     * @param context Pass a context object to let us get the card image
     *                resources stored in this application.
     * @return Reference of our singleton.
     */
    public static synchronized Uno getInstance(Context context) {
        if (INSTANCE == null) {
            try {
                INSTANCE = new UnoImpl(context);
            } // try
            catch (IOException e) {
                // Thrown if failed to load image resources, but won't happen
                // here, because our image resources are not in the external
                // storage, but packed in the application package
                throw new AssertionError(e);
            } // catch (IOException e)
        } // if (INSTANCE == null)

        return INSTANCE;
    } // getInstance(Context)

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
     * @param who Get which player's instance. Must be one of the following:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Specified player's instance.
     */
    public abstract Player getPlayer(int who);

    /**
     * @return this.player[this.getNow()].
     */
    public abstract Player getCurrPlayer();

    /**
     * @return this.player[this.getNext()].
     */
    public abstract Player getNextPlayer();

    /**
     * @return this.player[this.getOppo()].
     */
    public abstract Player getOppoPlayer();

    /**
     * @return this.player[this.getPrev()].
     */
    public abstract Player getPrevPlayer();

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
     * @return This value tells that what's the next step
     * after you drew a playable card in your action.
     * When force play is enabled, play the card immediately.
     * When force play is disabled, keep the card in your hand.
     */
    public abstract boolean isForcePlay();

    /**
     * @param enabled Enable/Disable the force play rule.
     */
    public abstract void setForcePlay(boolean enabled);

    /**
     * @return Whether the 7-0 rule is enabled. In 7-0 rule, when a seven card
     * is put down, the player must swap hand cards with another player
     * immediately. When a zero card is put down, everyone need to pass
     * the hand cards to the next player.
     */
    public abstract boolean isSevenZeroRule();

    /**
     * @param enabled Enable/Disable the 7-0 rule.
     */
    public abstract void setSevenZeroRule(boolean enabled);

    /**
     * @return Can or cannot stack +2 cards. If can, when you put down a +2
     * card, the next player may transfer the punishment to its next
     * player by stacking another +2 card. Finally the first one who
     * does not stack a +2 card must draw all of the required cards.
     */
    public abstract boolean isDraw2StackRule();

    /**
     * @param enabled Enable/Disable the +2 stacking rule.
     */
    public abstract void setDraw2StackRule(boolean enabled);

    /**
     * Only available in +2 stack rule. In this rule, when a +2 card is put
     * down, the next player may transfer the punishment to its next player
     * by stacking another +2 card. Finally the first one who does not stack
     * a +2 card must draw all of the required cards.
     *
     * @return This counter records that how many required cards need to be
     * drawn by the final player. When this value is not zero, only
     * +2 cards are legal to play.
     */
    public abstract int getDraw2StackCount();

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
     * @return Colors of recent played cards.
     */
    public abstract List<Color> getRecentColors();

    /**
     * @return Color of the last played card.
     */
    public abstract Color lastColor();

    /**
     * @return Color of the next-to-last played card.
     */
    public abstract Color next2lastColor();

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
     * @return Index of the drawn card in hand, or -1 if the specified player
     * didn't draw a card because of the limitation.
     */
    public abstract int draw(int who, boolean force);

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

    /**
     * In 7-0 rule, when someone put down a seven card, then the player must
     * swap hand cards with another player immediately.
     *
     * @param a Who put down the seven card. Must be one of the following:
     *          Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param b Exchange with whom. Must be one of the following:
     *          Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     *          Cannot exchange with yourself.
     */
    public abstract void swap(int a, int b);

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass the hand
     * cards to the next player.
     */
    public abstract void cycle();
} // Uno Class

// E.O.F