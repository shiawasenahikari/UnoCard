////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import android.content.Context;

import com.github.hikari_toyama.unocard.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.github.hikari_toyama.unocard.core.Color.BLUE;
import static com.github.hikari_toyama.unocard.core.Color.GREEN;
import static com.github.hikari_toyama.unocard.core.Color.NONE;
import static com.github.hikari_toyama.unocard.core.Color.RED;
import static com.github.hikari_toyama.unocard.core.Color.YELLOW;
import static com.github.hikari_toyama.unocard.core.Content.DRAW2;
import static com.github.hikari_toyama.unocard.core.Content.NUM0;
import static com.github.hikari_toyama.unocard.core.Content.NUM1;
import static com.github.hikari_toyama.unocard.core.Content.NUM2;
import static com.github.hikari_toyama.unocard.core.Content.NUM3;
import static com.github.hikari_toyama.unocard.core.Content.NUM4;
import static com.github.hikari_toyama.unocard.core.Content.NUM5;
import static com.github.hikari_toyama.unocard.core.Content.NUM6;
import static com.github.hikari_toyama.unocard.core.Content.NUM7;
import static com.github.hikari_toyama.unocard.core.Content.NUM8;
import static com.github.hikari_toyama.unocard.core.Content.NUM9;
import static com.github.hikari_toyama.unocard.core.Content.REV;
import static com.github.hikari_toyama.unocard.core.Content.SKIP;
import static com.github.hikari_toyama.unocard.core.Content.WILD;
import static com.github.hikari_toyama.unocard.core.Content.WILD_DRAW4;

/**
 * Uno Runtime Class (Singleton).
 */
@SuppressWarnings("ALL")
public class Uno {
    public static final int DIR_LEFT = 1;
    public static final int DIR_RIGHT = 3;
    public static final int PLAYER_YOU = 0;
    public static final int PLAYER_COM1 = 1;
    public static final int PLAYER_COM2 = 2;
    public static final int PLAYER_COM3 = 3;
    public static final int MAX_HOLD_CARDS = 15;

    /**
     * Singleton.
     */
    private static Uno INSTANCE;

    /**
     * Random number generator.
     */
    private Random rand;

    /**
     * Card table.
     */
    private Card[] table;

    /**
     * Image resources for wild cards.
     */
    private Mat[] wImage;

    /**
     * Image resources for wild +4 cards.
     */
    private Mat[] w4Image;

    /**
     * Card back image resource.
     */
    private Mat backImage;

    /**
     * Difficulty button image resource (EASY).
     */
    private Mat easyImage;

    /**
     * Difficulty button image resource (HARD).
     */
    private Mat hardImage;

    /**
     * Background image resource (Direction: COUTNER CLOCKWISE).
     */
    private Mat bgCounter;

    /**
     * Background image resource (Direction: CLOCKWISE).
     */
    private Mat bgClockwise;

    /**
     * Current action sequence (DIR_LEFT / DIR_RIGHT).
     */
    private int direction = DIR_LEFT;

    /**
     * Game players.
     */
    private Player[] player;

    /**
     * Card deck (ready to use).
     */
    private List<Card> deck;

    /**
     * Used cards.
     */
    private List<Card> used;

    /**
     * Recent played cards.
     */
    private List<Card> recent;

    /**
     * Singleton, hide default constructor.
     *
     * @param context Pass a context object to let we get the card image
     *                resources stored in this application.
     */
    private Uno(Context context) {
        int i;
        Mat[] br, dk;
        Context appContext;

        try {
            // Load background image resources
            appContext = context.getApplicationContext();
            bgCounter = Utils.loadResource(appContext, R.raw.bg_counter);
            bgClockwise = Utils.loadResource(appContext, R.raw.bg_clockwise);
            Imgproc.cvtColor(bgCounter, bgCounter, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(bgClockwise, bgClockwise, Imgproc.COLOR_BGR2RGB);

            // Load card back image resource
            backImage = Utils.loadResource(appContext, R.raw.back);
            Imgproc.cvtColor(backImage, backImage, Imgproc.COLOR_BGR2RGB);

            // Load difficulty image resources
            easyImage = Utils.loadResource(appContext, R.raw.lv_easy);
            hardImage = Utils.loadResource(appContext, R.raw.lv_hard);
            Imgproc.cvtColor(easyImage, easyImage, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(hardImage, hardImage, Imgproc.COLOR_BGR2RGB);

            // Load cards' front image resources
            br = new Mat[]{
                    Utils.loadResource(appContext, R.raw.front_r0),
                    Utils.loadResource(appContext, R.raw.front_r1),
                    Utils.loadResource(appContext, R.raw.front_r2),
                    Utils.loadResource(appContext, R.raw.front_r3),
                    Utils.loadResource(appContext, R.raw.front_r4),
                    Utils.loadResource(appContext, R.raw.front_r5),
                    Utils.loadResource(appContext, R.raw.front_r6),
                    Utils.loadResource(appContext, R.raw.front_r7),
                    Utils.loadResource(appContext, R.raw.front_r8),
                    Utils.loadResource(appContext, R.raw.front_r9),
                    Utils.loadResource(appContext, R.raw.front_rd2),
                    Utils.loadResource(appContext, R.raw.front_rs),
                    Utils.loadResource(appContext, R.raw.front_rr),
                    Utils.loadResource(appContext, R.raw.front_b0),
                    Utils.loadResource(appContext, R.raw.front_b1),
                    Utils.loadResource(appContext, R.raw.front_b2),
                    Utils.loadResource(appContext, R.raw.front_b3),
                    Utils.loadResource(appContext, R.raw.front_b4),
                    Utils.loadResource(appContext, R.raw.front_b5),
                    Utils.loadResource(appContext, R.raw.front_b6),
                    Utils.loadResource(appContext, R.raw.front_b7),
                    Utils.loadResource(appContext, R.raw.front_b8),
                    Utils.loadResource(appContext, R.raw.front_b9),
                    Utils.loadResource(appContext, R.raw.front_bd2),
                    Utils.loadResource(appContext, R.raw.front_bs),
                    Utils.loadResource(appContext, R.raw.front_br),
                    Utils.loadResource(appContext, R.raw.front_g0),
                    Utils.loadResource(appContext, R.raw.front_g1),
                    Utils.loadResource(appContext, R.raw.front_g2),
                    Utils.loadResource(appContext, R.raw.front_g3),
                    Utils.loadResource(appContext, R.raw.front_g4),
                    Utils.loadResource(appContext, R.raw.front_g5),
                    Utils.loadResource(appContext, R.raw.front_g6),
                    Utils.loadResource(appContext, R.raw.front_g7),
                    Utils.loadResource(appContext, R.raw.front_g8),
                    Utils.loadResource(appContext, R.raw.front_g9),
                    Utils.loadResource(appContext, R.raw.front_gd2),
                    Utils.loadResource(appContext, R.raw.front_gs),
                    Utils.loadResource(appContext, R.raw.front_gr),
                    Utils.loadResource(appContext, R.raw.front_y0),
                    Utils.loadResource(appContext, R.raw.front_y1),
                    Utils.loadResource(appContext, R.raw.front_y2),
                    Utils.loadResource(appContext, R.raw.front_y3),
                    Utils.loadResource(appContext, R.raw.front_y4),
                    Utils.loadResource(appContext, R.raw.front_y5),
                    Utils.loadResource(appContext, R.raw.front_y6),
                    Utils.loadResource(appContext, R.raw.front_y7),
                    Utils.loadResource(appContext, R.raw.front_y8),
                    Utils.loadResource(appContext, R.raw.front_y9),
                    Utils.loadResource(appContext, R.raw.front_yd2),
                    Utils.loadResource(appContext, R.raw.front_ys),
                    Utils.loadResource(appContext, R.raw.front_yr),
                    Utils.loadResource(appContext, R.raw.front_kw),
                    Utils.loadResource(appContext, R.raw.front_kw4)
            }; // br = new Mat[]{}
            dk = new Mat[]{
                    Utils.loadResource(appContext, R.raw.dark_r0),
                    Utils.loadResource(appContext, R.raw.dark_r1),
                    Utils.loadResource(appContext, R.raw.dark_r2),
                    Utils.loadResource(appContext, R.raw.dark_r3),
                    Utils.loadResource(appContext, R.raw.dark_r4),
                    Utils.loadResource(appContext, R.raw.dark_r5),
                    Utils.loadResource(appContext, R.raw.dark_r6),
                    Utils.loadResource(appContext, R.raw.dark_r7),
                    Utils.loadResource(appContext, R.raw.dark_r8),
                    Utils.loadResource(appContext, R.raw.dark_r9),
                    Utils.loadResource(appContext, R.raw.dark_rd2),
                    Utils.loadResource(appContext, R.raw.dark_rs),
                    Utils.loadResource(appContext, R.raw.dark_rr),
                    Utils.loadResource(appContext, R.raw.dark_b0),
                    Utils.loadResource(appContext, R.raw.dark_b1),
                    Utils.loadResource(appContext, R.raw.dark_b2),
                    Utils.loadResource(appContext, R.raw.dark_b3),
                    Utils.loadResource(appContext, R.raw.dark_b4),
                    Utils.loadResource(appContext, R.raw.dark_b5),
                    Utils.loadResource(appContext, R.raw.dark_b6),
                    Utils.loadResource(appContext, R.raw.dark_b7),
                    Utils.loadResource(appContext, R.raw.dark_b8),
                    Utils.loadResource(appContext, R.raw.dark_b9),
                    Utils.loadResource(appContext, R.raw.dark_bd2),
                    Utils.loadResource(appContext, R.raw.dark_bs),
                    Utils.loadResource(appContext, R.raw.dark_br),
                    Utils.loadResource(appContext, R.raw.dark_g0),
                    Utils.loadResource(appContext, R.raw.dark_g1),
                    Utils.loadResource(appContext, R.raw.dark_g2),
                    Utils.loadResource(appContext, R.raw.dark_g3),
                    Utils.loadResource(appContext, R.raw.dark_g4),
                    Utils.loadResource(appContext, R.raw.dark_g5),
                    Utils.loadResource(appContext, R.raw.dark_g6),
                    Utils.loadResource(appContext, R.raw.dark_g7),
                    Utils.loadResource(appContext, R.raw.dark_g8),
                    Utils.loadResource(appContext, R.raw.dark_g9),
                    Utils.loadResource(appContext, R.raw.dark_gd2),
                    Utils.loadResource(appContext, R.raw.dark_gs),
                    Utils.loadResource(appContext, R.raw.dark_gr),
                    Utils.loadResource(appContext, R.raw.dark_y0),
                    Utils.loadResource(appContext, R.raw.dark_y1),
                    Utils.loadResource(appContext, R.raw.dark_y2),
                    Utils.loadResource(appContext, R.raw.dark_y3),
                    Utils.loadResource(appContext, R.raw.dark_y4),
                    Utils.loadResource(appContext, R.raw.dark_y5),
                    Utils.loadResource(appContext, R.raw.dark_y6),
                    Utils.loadResource(appContext, R.raw.dark_y7),
                    Utils.loadResource(appContext, R.raw.dark_y8),
                    Utils.loadResource(appContext, R.raw.dark_y9),
                    Utils.loadResource(appContext, R.raw.dark_yd2),
                    Utils.loadResource(appContext, R.raw.dark_ys),
                    Utils.loadResource(appContext, R.raw.dark_yr),
                    Utils.loadResource(appContext, R.raw.dark_kw),
                    Utils.loadResource(appContext, R.raw.dark_kw4)
            }; // dk = new Mat[]{}
            for (i = 0; i < 54; ++i) {
                Imgproc.cvtColor(br[i], br[i], Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(dk[i], dk[i], Imgproc.COLOR_BGR2RGB);
            } // for (i = 0; i < 54; ++i)

            // Load wild & wild +4 image resources
            wImage = new Mat[]{
                    br[52],
                    Utils.loadResource(appContext, R.raw.front_rw),
                    Utils.loadResource(appContext, R.raw.front_bw),
                    Utils.loadResource(appContext, R.raw.front_gw),
                    Utils.loadResource(appContext, R.raw.front_yw)
            }; // wImage = new Mat[]{}
            w4Image = new Mat[]{
                    br[53],
                    Utils.loadResource(appContext, R.raw.front_rw4),
                    Utils.loadResource(appContext, R.raw.front_bw4),
                    Utils.loadResource(appContext, R.raw.front_gw4),
                    Utils.loadResource(appContext, R.raw.front_yw4)
            }; // w4Image = new Mat[]{}
            for (i = 1; i < 5; ++i) {
                Imgproc.cvtColor(wImage[i], wImage[i], Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(w4Image[i], w4Image[i], Imgproc.COLOR_BGR2RGB);
            } // for (i = 1; i < 5; ++i)
        } // try
        catch (IOException e) {
            // Thrown if error occurred when loading image resources, but won't
            // happen here, because our image resources are not in the external
            // storage, but packed in the application package
            throw new AssertionError(e);
        } // catch (IOException e)

        // Generate card table
        table = new Card[]{
                new Card(br[0], dk[0], RED, NUM0, "Red 0"),
                new Card(br[1], dk[1], RED, NUM1, "Red 1"),
                new Card(br[2], dk[2], RED, NUM2, "Red 2"),
                new Card(br[3], dk[3], RED, NUM3, "Red 3"),
                new Card(br[4], dk[4], RED, NUM4, "Red 4"),
                new Card(br[5], dk[5], RED, NUM5, "Red 5"),
                new Card(br[6], dk[6], RED, NUM6, "Red 6"),
                new Card(br[7], dk[7], RED, NUM7, "Red 7"),
                new Card(br[8], dk[8], RED, NUM8, "Red 8"),
                new Card(br[9], dk[9], RED, NUM9, "Red 9"),
                new Card(br[10], dk[10], RED, DRAW2, "Red +2"),
                new Card(br[11], dk[11], RED, SKIP, "Red Skip"),
                new Card(br[12], dk[12], RED, REV, "Red Reverse"),
                new Card(br[13], dk[13], BLUE, NUM0, "Blue 0"),
                new Card(br[14], dk[14], BLUE, NUM1, "Blue 1"),
                new Card(br[15], dk[15], BLUE, NUM2, "Blue 2"),
                new Card(br[16], dk[16], BLUE, NUM3, "Blue 3"),
                new Card(br[17], dk[17], BLUE, NUM4, "Blue 4"),
                new Card(br[18], dk[18], BLUE, NUM5, "Blue 5"),
                new Card(br[19], dk[19], BLUE, NUM6, "Blue 6"),
                new Card(br[20], dk[20], BLUE, NUM7, "Blue 7"),
                new Card(br[21], dk[21], BLUE, NUM8, "Blue 8"),
                new Card(br[22], dk[22], BLUE, NUM9, "Blue 9"),
                new Card(br[23], dk[23], BLUE, DRAW2, "Blue +2"),
                new Card(br[24], dk[24], BLUE, SKIP, "Blue Skip"),
                new Card(br[25], dk[25], BLUE, REV, "Blue Reverse"),
                new Card(br[26], dk[26], GREEN, NUM0, "Green 0"),
                new Card(br[27], dk[27], GREEN, NUM1, "Green 1"),
                new Card(br[28], dk[28], GREEN, NUM2, "Green 2"),
                new Card(br[29], dk[29], GREEN, NUM3, "Green 3"),
                new Card(br[30], dk[30], GREEN, NUM4, "Green 4"),
                new Card(br[31], dk[31], GREEN, NUM5, "Green 5"),
                new Card(br[32], dk[32], GREEN, NUM6, "Green 6"),
                new Card(br[33], dk[33], GREEN, NUM7, "Green 7"),
                new Card(br[34], dk[34], GREEN, NUM8, "Green 8"),
                new Card(br[35], dk[35], GREEN, NUM9, "Green 9"),
                new Card(br[36], dk[36], GREEN, DRAW2, "Green +2"),
                new Card(br[37], dk[37], GREEN, SKIP, "Green Skip"),
                new Card(br[38], dk[38], GREEN, REV, "Green Reverse"),
                new Card(br[39], dk[39], YELLOW, NUM0, "Yellow 0"),
                new Card(br[40], dk[40], YELLOW, NUM1, "Yellow 1"),
                new Card(br[41], dk[41], YELLOW, NUM2, "Yellow 2"),
                new Card(br[42], dk[42], YELLOW, NUM3, "Yellow 3"),
                new Card(br[43], dk[43], YELLOW, NUM4, "Yellow 4"),
                new Card(br[44], dk[44], YELLOW, NUM5, "Yellow 5"),
                new Card(br[45], dk[45], YELLOW, NUM6, "Yellow 6"),
                new Card(br[46], dk[46], YELLOW, NUM7, "Yellow 7"),
                new Card(br[47], dk[47], YELLOW, NUM8, "Yellow 8"),
                new Card(br[48], dk[48], YELLOW, NUM9, "Yellow 9"),
                new Card(br[49], dk[49], YELLOW, DRAW2, "Yellow +2"),
                new Card(br[50], dk[50], YELLOW, SKIP, "Yellow Skip"),
                new Card(br[51], dk[51], YELLOW, REV, "Yellow Reverse"),
                new Card(br[52], dk[52], NONE, WILD, "Wild"),
                new Card(br[52], dk[52], NONE, WILD, "Wild"),
                new Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new Card(br[1], dk[1], RED, NUM1, "Red 1"),
                new Card(br[2], dk[2], RED, NUM2, "Red 2"),
                new Card(br[3], dk[3], RED, NUM3, "Red 3"),
                new Card(br[4], dk[4], RED, NUM4, "Red 4"),
                new Card(br[5], dk[5], RED, NUM5, "Red 5"),
                new Card(br[6], dk[6], RED, NUM6, "Red 6"),
                new Card(br[7], dk[7], RED, NUM7, "Red 7"),
                new Card(br[8], dk[8], RED, NUM8, "Red 8"),
                new Card(br[9], dk[9], RED, NUM9, "Red 9"),
                new Card(br[10], dk[10], RED, DRAW2, "Red +2"),
                new Card(br[11], dk[11], RED, SKIP, "Red Skip"),
                new Card(br[12], dk[12], RED, REV, "Red Reverse"),
                new Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new Card(br[14], dk[14], BLUE, NUM1, "Blue 1"),
                new Card(br[15], dk[15], BLUE, NUM2, "Blue 2"),
                new Card(br[16], dk[16], BLUE, NUM3, "Blue 3"),
                new Card(br[17], dk[17], BLUE, NUM4, "Blue 4"),
                new Card(br[18], dk[18], BLUE, NUM5, "Blue 5"),
                new Card(br[19], dk[19], BLUE, NUM6, "Blue 6"),
                new Card(br[20], dk[20], BLUE, NUM7, "Blue 7"),
                new Card(br[21], dk[21], BLUE, NUM8, "Blue 8"),
                new Card(br[22], dk[22], BLUE, NUM9, "Blue 9"),
                new Card(br[23], dk[23], BLUE, DRAW2, "Blue +2"),
                new Card(br[24], dk[24], BLUE, SKIP, "Blue Skip"),
                new Card(br[25], dk[25], BLUE, REV, "Blue Reverse"),
                new Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new Card(br[27], dk[27], GREEN, NUM1, "Green 1"),
                new Card(br[28], dk[28], GREEN, NUM2, "Green 2"),
                new Card(br[29], dk[29], GREEN, NUM3, "Green 3"),
                new Card(br[30], dk[30], GREEN, NUM4, "Green 4"),
                new Card(br[31], dk[31], GREEN, NUM5, "Green 5"),
                new Card(br[32], dk[32], GREEN, NUM6, "Green 6"),
                new Card(br[33], dk[33], GREEN, NUM7, "Green 7"),
                new Card(br[34], dk[34], GREEN, NUM8, "Green 8"),
                new Card(br[35], dk[35], GREEN, NUM9, "Green 9"),
                new Card(br[36], dk[36], GREEN, DRAW2, "Green +2"),
                new Card(br[37], dk[37], GREEN, SKIP, "Green Skip"),
                new Card(br[38], dk[38], GREEN, REV, "Green Reverse"),
                new Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new Card(br[40], dk[40], YELLOW, NUM1, "Yellow 1"),
                new Card(br[41], dk[41], YELLOW, NUM2, "Yellow 2"),
                new Card(br[42], dk[42], YELLOW, NUM3, "Yellow 3"),
                new Card(br[43], dk[43], YELLOW, NUM4, "Yellow 4"),
                new Card(br[44], dk[44], YELLOW, NUM5, "Yellow 5"),
                new Card(br[45], dk[45], YELLOW, NUM6, "Yellow 6"),
                new Card(br[46], dk[46], YELLOW, NUM7, "Yellow 7"),
                new Card(br[47], dk[47], YELLOW, NUM8, "Yellow 8"),
                new Card(br[48], dk[48], YELLOW, NUM9, "Yellow 9"),
                new Card(br[49], dk[49], YELLOW, DRAW2, "Yellow +2"),
                new Card(br[50], dk[50], YELLOW, SKIP, "Yellow Skip"),
                new Card(br[51], dk[51], YELLOW, REV, "Yellow Reverse"),
                new Card(br[52], dk[52], NONE, WILD, "Wild"),
                new Card(br[52], dk[52], NONE, WILD, "Wild")
        }; // table = new Card[]{}

        // Initialize containers
        used = new ArrayList<>();
        deck = new LinkedList<>();
        recent = new ArrayList<>();
        player = new Player[4];
        player[0] = new Player(); // PLAYER_YOU
        player[1] = new Player(); // PLAYER_COM1
        player[2] = new Player(); // PLAYER_COM2
        player[3] = new Player(); // PLAYER_COM3

        // Generate a random seed based on the current time stamp
        rand = new Random();
    } // Uno() (Class Constructor)

    /**
     * @param context Pass a context object to let we get the card image
     *                resources stored in this application.
     * @return Reference of our singleton.
     */
    public static synchronized Uno getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Uno(context);
        } // if (INSTANCE == null)

        return INSTANCE;
    } // getInstance()

    /**
     * @return Card back image resource.
     */
    public Mat getBackImage() {
        return backImage;
    } // getBackImage()

    /**
     * @return Difficulty button image resource (EASY).
     */
    public Mat getEasyImage() {
        return easyImage;
    } // getEasyImage()

    /**
     * @return Difficulty button image resource (HARD).
     */
    public Mat getHardImage() {
        return hardImage;
    } // getHardImage()

    /**
     * @return Background image resource in current direction.
     */
    public Mat getBackground() {
        return (direction == DIR_RIGHT ? bgCounter : bgClockwise);
    } // getBackground()

    /**
     * @return Background image's width (Unit: px).
     */
    public int getBgWidth() {
        return bgClockwise.cols();
    } // getBgWidth()

    /**
     * @return Background image's height (Unit: px).
     */
    public int getBgHeight() {
        return bgClockwise.rows();
    } // getBgHeight()

    /**
     * When a player played a wild card and specified a following legal color,
     * get the corresponding color-filled image here, and show it in recent
     * card area.
     *
     * @param color The wild image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    public Mat getColoredWildImage(Color color) {
        return wImage[color.ordinal()];
    } // getColoredWildImage()

    /**
     * When a player played a wild +4 card and specified a following legal
     * color, get the corresponding color-filled image here, and show it in
     * recent card area.
     *
     * @param color The wild +4 image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    public Mat getColoredWildDraw4Image(Color color) {
        return w4Image[color.ordinal()];
    } // getColoredWildDraw4Image()

    /**
     * Get current action sequence. You can get the next player by calculating
     * (now + this.getDirection()) % 4, or the previous player by calculating
     * (now + 4 - this.getDirection()) % 4.
     *
     * @return Current action sequence. DIR_LEFT for clockwise,
     * or DIR_RIGHT for counter-clockwise.
     */
    public int getDirection() {
        return direction;
    } // getDirection()

    /**
     * Switch current action sequence.
     *
     * @return Switched action sequence. DIR_LEFT for clockwise,
     * or DIR_RIGHT for counter-clockwise.
     */
    public int switchDirection() {
        direction = 4 - direction;
        return direction;
    } // switchDirection()

    /**
     * @param who Get which player's instance. Must be one of the following
     *            values: PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
     * @return Specified player's instance.
     */
    public Player getPlayer(int who) {
        return player[who];
    } // getPlayer()

    /**
     * @return How many cards in deck (haven't been used yet).
     */
    public int getDeckCount() {
        return deck.size();
    } // getDeckCount()

    /**
     * @return How many cards have been used.
     */
    public int getUsedCount() {
        return used.size() + recent.size();
    } // getUsedCount()

    /**
     * @return Recent played cards.
     */
    public List<Card> getRecent() {
        return Collections.unmodifiableList(recent);
    } // getRecent()

    /**
     * @param whom Get whose hand cards. Must be one of the following values:
     *             PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
     * @return Specified player's all hand cards.
     * @deprecated Use getPlayer(whom).getHandCards() instead.
     */
    @Deprecated
    public List<Card> getHandCardsOf(int whom) {
        return getPlayer(whom).getHandCards();
    } // getHandCardsOf()

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    public void start() {
        Card card;
        int i, index, size;
        List<Card> allCards;

        // Clear card deck, used card deck, recent played cards,
        // and everyone's hand cards
        deck.clear();
        used.clear();
        recent.clear();
        player[0].handCards.clear();
        player[1].handCards.clear();
        player[2].handCards.clear();
        player[3].handCards.clear();

        // Reset direction
        direction = DIR_LEFT;

        // Generate a temporary sequenced card deck
        allCards = new ArrayList<>();
        Collections.addAll(allCards, table);

        // Keep picking a card from the temporary card deck randomly,
        // until all cards are picked to the real card deck (shuffle cards)
        size = allCards.size();
        while (size > 0) {
            index = rand.nextInt(size);
            deck.add(allCards.get(index));
            allCards.remove(index);
            --size;
        } // while (size > 0)

        // Let everyone draw 7 cards
        for (i = 0; i < 28; ++i) {
            draw(i % 4);
        } // for (i = 0; i < 28; ++i)

        // Determine a start card as the previous played card
        index = deck.size() - 1;
        do {
            card = deck.get(index);
            deck.remove(index);
            if (card.isWild()) {
                // Start card cannot be a wild card, so return it
                // to the bottom of card deck and pick another card
                deck.add(0, card);
            } // if (card.isWild())
            else {
                // Any non-wild card can be start card
                // Start card determined
                recent.add(card);
            } // else
        } while (recent.isEmpty());
    } // start()

    /**
     * Call this method when someone needs to draw a card.
     * <p>
     * NOTE: Everyone can hold 15 cards at most in this program, so even if this
     * method is called, the specified player may not draw a card as a result.
     *
     * @param who Who draws a card. Must be one of the following values:
     *            PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
     * @return Reference of the drawn card, or null if the specified player
     * didn't draw a card because of the limit.
     */
    public Card draw(int who) {
        Card card;
        int i, index, size;
        List<Card> handCards;

        card = null;
        handCards = player[who].handCards;
        if (handCards.size() < MAX_HOLD_CARDS) {
            // Draw a card from card deck, and put it to an appropriate position
            index = deck.size() - 1;
            card = deck.get(index);
            deck.remove(index);
            size = handCards.size();
            for (i = 0; i < size; ++i) {
                if (handCards.get(i).compareTo(card) > 0) {
                    // Found an appropriate position to insert the new card,
                    // which keeps the player's hand cards sequenced
                    break;
                } // if (handCards.get(i).compareTo(card) > 0)
            } // for (i = 0; i < size; ++i)

            handCards.add(i, card);
            player[who].recent = null;
            if (deck.isEmpty()) {
                // Re-use the used cards when there are no more cards in deck
                size = used.size();
                while (size > 0) {
                    index = rand.nextInt(size);
                    deck.add(used.get(index));
                    used.remove(index);
                    --size;
                } // while (size > 0)
            } // if (deck.isEmpty())
        } // if (handCards.size() < MAX_HOLD_CARDS)

        return card;
    } // draw()

    /**
     * Evaluate which color is the best color for the specified player. In our
     * evaluation system, zero cards are worth 2 points, non-zero number cards
     * are worth 4 points, and action cards are worth 5 points. Finally, the
     * color which contains the worthiest cards becomes the best color.
     *
     * @param whom Evaluate whose best color. Must be one of the following
     *             values: PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
     * @return The best color for the specified player now. Specially, when an
     * illegal [whom] parameter was passed in, or the specified. player remains
     * only wild cards, method will return a default value, which is Color.RED.
     */
    public Color bestColorFor(int whom) {
        Color best;
        int[] score;

        best = RED;
        score = new int[]{0, 0, 0, 0, 0};
        for (Card card : player[whom].handCards) {
            if (card.isZero()) {
                score[card.color.ordinal()] += 2;
            } // if (card.isZero())
            else if (card.isAction()) {
                score[card.color.ordinal()] += 5;
            } // else if (card.isAction())
            else {
                score[card.color.ordinal()] += 4;
            } // else
        } // for (Card card : player[whom].handCards)

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
    } // bestColorFor()

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card The card you want to check whether it's legal to play.
     * @return Whether the specified card is legal to play.
     */
    public boolean isLegalToPlay(Card card) {
        Card previous;
        boolean result;
        Color prevColor;

        if (card == null || recent.isEmpty()) {
            // Null Pointer
            result = false;
        } // if (card == null || recent.isEmpty())
        else if (card.isWild()) {
            // Wild cards: LEGAL
            result = true;
        } // else if (card.isWild())
        else {
            // Same content to previous: LEGAL
            // Same color to previous: LEGAL
            // Other cards: ILLEGAL
            previous = recent.get(recent.size() - 1);
            if (card.content == previous.content) {
                result = true;
            } // if (card.content == previous.content)
            else {
                if (previous.isWild()) {
                    prevColor = previous.wildColor;
                } // if (previous.isWild())
                else {
                    prevColor = previous.color;
                } // else

                result = (card.color == prevColor);
            } // else
        } // else

        return result;
    } // isLegalToPlay()

    /**
     * Call this method when someone needs to play a card. The played card
     * replaces the "previous played card", and the original "previous played
     * card" becomes a used card at the same time.
     * <p>
     * NOTE: Before calling this method, you must call isLegalToPlay(Card)
     * method at first to check whether the specified card is legal to play.
     * This method will play the card directly without checking the legality.
     *
     * @param who   Who plays a card. Must be one of the following values:
     *              PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
     * @param index Play which card. Pass the corresponding card's index of the
     *              specified player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     * @return Reference of the played card.
     */
    public Card play(int who, int index, Color color) {
        Card card;
        List<Card> handCards;

        card = null;
        handCards = player[who].handCards;
        if (index < handCards.size()) {
            card = handCards.get(index);
            handCards.remove(index);
            if (card.isWild()) {
                card.wildColor = color;
            } // if（card.isWild())

            player[who].recent = card;
            recent.add(card);
            if (recent.size() > 5) {
                used.add(recent.get(0));
                recent.remove(0);
            } // if (recent.size() > 5)
        } // if (index < handCards.size())

        return card;
    } // play()
} // Uno Class

// E.O.F