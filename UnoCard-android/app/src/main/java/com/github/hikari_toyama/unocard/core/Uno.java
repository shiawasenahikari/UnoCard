////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import android.content.Context;
import android.util.Log;

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
     * Tag name for Android Logcat.
     */
    private static final String TAG = "Uno";

    /**
     * Singleton.
     */
    private static volatile Uno INSTANCE;

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
     * Background image resource (for welcome screen).
     */
    private Mat bgWelcome;

    /**
     * Background image resource (Direction: COUTNER CLOCKWISE).
     */
    private Mat bgCounter;

    /**
     * Background image resource (Direction: CLOCKWISE).
     */
    private Mat bgClockwise;

    /**
     * Difficulty button image resources (EASY).
     */
    private Mat easyImage, easyImage_d;

    /**
     * Difficulty button image resources (HARD).
     */
    private Mat hardImage, hardImage_d;

    /**
     * Player in turn. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    private int now;

    /**
     * How many players in game. Supports 3 or 4.
     */
    private int players;

    /**
     * Current action sequence (DIR_LEFT / DIR_RIGHT).
     */
    private int direction;

    /**
     * Current difficulty (LV_EASY / LV_HARD).
     */
    private int difficulty;

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
     * Recent played cards (read-only version, provide for external accesses).
     */
    private List<Card> recent_readOnly;

    /**
     * Singleton, hide default constructor.
     *
     * @param context Pass a context object to let we get the card image
     *                resources stored in this application.
     */
    private Uno(Context context) {
        Mat[] br, dk;
        int i, loaded, total;

        // Preparations
        context = context.getApplicationContext();
        loaded = 0;
        total = 124;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        try {
            // Load background image resources
            bgWelcome = Utils.loadResource(context, R.raw.bg_welcome);
            bgCounter = Utils.loadResource(context, R.raw.bg_counter);
            bgClockwise = Utils.loadResource(context, R.raw.bg_clockwise);
            Imgproc.cvtColor(bgWelcome, bgWelcome, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(bgCounter, bgCounter, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(bgClockwise, bgClockwise, Imgproc.COLOR_BGR2RGB);
            loaded += 3;
            Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

            // Load card back image resource
            backImage = Utils.loadResource(context, R.raw.back);
            Imgproc.cvtColor(backImage, backImage, Imgproc.COLOR_BGR2RGB);
            ++loaded;
            Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

            // Load difficulty image resources
            easyImage = Utils.loadResource(context, R.raw.lv_easy);
            hardImage = Utils.loadResource(context, R.raw.lv_hard);
            easyImage_d = Utils.loadResource(context, R.raw.lv_easy_dark);
            hardImage_d = Utils.loadResource(context, R.raw.lv_hard_dark);
            Imgproc.cvtColor(easyImage, easyImage, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(hardImage, hardImage, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(easyImage_d, easyImage_d, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(hardImage_d, hardImage_d, Imgproc.COLOR_BGR2RGB);
            loaded += 4;
            Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

            // Load cards' front image resources
            br = new Mat[]{
                    Utils.loadResource(context, R.raw.front_r0),
                    Utils.loadResource(context, R.raw.front_r1),
                    Utils.loadResource(context, R.raw.front_r2),
                    Utils.loadResource(context, R.raw.front_r3),
                    Utils.loadResource(context, R.raw.front_r4),
                    Utils.loadResource(context, R.raw.front_r5),
                    Utils.loadResource(context, R.raw.front_r6),
                    Utils.loadResource(context, R.raw.front_r7),
                    Utils.loadResource(context, R.raw.front_r8),
                    Utils.loadResource(context, R.raw.front_r9),
                    Utils.loadResource(context, R.raw.front_rd2),
                    Utils.loadResource(context, R.raw.front_rs),
                    Utils.loadResource(context, R.raw.front_rr),
                    Utils.loadResource(context, R.raw.front_b0),
                    Utils.loadResource(context, R.raw.front_b1),
                    Utils.loadResource(context, R.raw.front_b2),
                    Utils.loadResource(context, R.raw.front_b3),
                    Utils.loadResource(context, R.raw.front_b4),
                    Utils.loadResource(context, R.raw.front_b5),
                    Utils.loadResource(context, R.raw.front_b6),
                    Utils.loadResource(context, R.raw.front_b7),
                    Utils.loadResource(context, R.raw.front_b8),
                    Utils.loadResource(context, R.raw.front_b9),
                    Utils.loadResource(context, R.raw.front_bd2),
                    Utils.loadResource(context, R.raw.front_bs),
                    Utils.loadResource(context, R.raw.front_br),
                    Utils.loadResource(context, R.raw.front_g0),
                    Utils.loadResource(context, R.raw.front_g1),
                    Utils.loadResource(context, R.raw.front_g2),
                    Utils.loadResource(context, R.raw.front_g3),
                    Utils.loadResource(context, R.raw.front_g4),
                    Utils.loadResource(context, R.raw.front_g5),
                    Utils.loadResource(context, R.raw.front_g6),
                    Utils.loadResource(context, R.raw.front_g7),
                    Utils.loadResource(context, R.raw.front_g8),
                    Utils.loadResource(context, R.raw.front_g9),
                    Utils.loadResource(context, R.raw.front_gd2),
                    Utils.loadResource(context, R.raw.front_gs),
                    Utils.loadResource(context, R.raw.front_gr),
                    Utils.loadResource(context, R.raw.front_y0),
                    Utils.loadResource(context, R.raw.front_y1),
                    Utils.loadResource(context, R.raw.front_y2),
                    Utils.loadResource(context, R.raw.front_y3),
                    Utils.loadResource(context, R.raw.front_y4),
                    Utils.loadResource(context, R.raw.front_y5),
                    Utils.loadResource(context, R.raw.front_y6),
                    Utils.loadResource(context, R.raw.front_y7),
                    Utils.loadResource(context, R.raw.front_y8),
                    Utils.loadResource(context, R.raw.front_y9),
                    Utils.loadResource(context, R.raw.front_yd2),
                    Utils.loadResource(context, R.raw.front_ys),
                    Utils.loadResource(context, R.raw.front_yr),
                    Utils.loadResource(context, R.raw.front_kw),
                    Utils.loadResource(context, R.raw.front_kw4)
            }; // br = new Mat[]{}
            dk = new Mat[]{
                    Utils.loadResource(context, R.raw.dark_r0),
                    Utils.loadResource(context, R.raw.dark_r1),
                    Utils.loadResource(context, R.raw.dark_r2),
                    Utils.loadResource(context, R.raw.dark_r3),
                    Utils.loadResource(context, R.raw.dark_r4),
                    Utils.loadResource(context, R.raw.dark_r5),
                    Utils.loadResource(context, R.raw.dark_r6),
                    Utils.loadResource(context, R.raw.dark_r7),
                    Utils.loadResource(context, R.raw.dark_r8),
                    Utils.loadResource(context, R.raw.dark_r9),
                    Utils.loadResource(context, R.raw.dark_rd2),
                    Utils.loadResource(context, R.raw.dark_rs),
                    Utils.loadResource(context, R.raw.dark_rr),
                    Utils.loadResource(context, R.raw.dark_b0),
                    Utils.loadResource(context, R.raw.dark_b1),
                    Utils.loadResource(context, R.raw.dark_b2),
                    Utils.loadResource(context, R.raw.dark_b3),
                    Utils.loadResource(context, R.raw.dark_b4),
                    Utils.loadResource(context, R.raw.dark_b5),
                    Utils.loadResource(context, R.raw.dark_b6),
                    Utils.loadResource(context, R.raw.dark_b7),
                    Utils.loadResource(context, R.raw.dark_b8),
                    Utils.loadResource(context, R.raw.dark_b9),
                    Utils.loadResource(context, R.raw.dark_bd2),
                    Utils.loadResource(context, R.raw.dark_bs),
                    Utils.loadResource(context, R.raw.dark_br),
                    Utils.loadResource(context, R.raw.dark_g0),
                    Utils.loadResource(context, R.raw.dark_g1),
                    Utils.loadResource(context, R.raw.dark_g2),
                    Utils.loadResource(context, R.raw.dark_g3),
                    Utils.loadResource(context, R.raw.dark_g4),
                    Utils.loadResource(context, R.raw.dark_g5),
                    Utils.loadResource(context, R.raw.dark_g6),
                    Utils.loadResource(context, R.raw.dark_g7),
                    Utils.loadResource(context, R.raw.dark_g8),
                    Utils.loadResource(context, R.raw.dark_g9),
                    Utils.loadResource(context, R.raw.dark_gd2),
                    Utils.loadResource(context, R.raw.dark_gs),
                    Utils.loadResource(context, R.raw.dark_gr),
                    Utils.loadResource(context, R.raw.dark_y0),
                    Utils.loadResource(context, R.raw.dark_y1),
                    Utils.loadResource(context, R.raw.dark_y2),
                    Utils.loadResource(context, R.raw.dark_y3),
                    Utils.loadResource(context, R.raw.dark_y4),
                    Utils.loadResource(context, R.raw.dark_y5),
                    Utils.loadResource(context, R.raw.dark_y6),
                    Utils.loadResource(context, R.raw.dark_y7),
                    Utils.loadResource(context, R.raw.dark_y8),
                    Utils.loadResource(context, R.raw.dark_y9),
                    Utils.loadResource(context, R.raw.dark_yd2),
                    Utils.loadResource(context, R.raw.dark_ys),
                    Utils.loadResource(context, R.raw.dark_yr),
                    Utils.loadResource(context, R.raw.dark_kw),
                    Utils.loadResource(context, R.raw.dark_kw4)
            }; // dk = new Mat[]{}
            for (i = 0; i < 54; ++i) {
                Imgproc.cvtColor(br[i], br[i], Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(dk[i], dk[i], Imgproc.COLOR_BGR2RGB);
                loaded += 2;
                Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");
            } // for (i = 0; i < 54; ++i)

            // Load wild & wild +4 image resources
            wImage = new Mat[]{
                    br[52],
                    Utils.loadResource(context, R.raw.front_rw),
                    Utils.loadResource(context, R.raw.front_bw),
                    Utils.loadResource(context, R.raw.front_gw),
                    Utils.loadResource(context, R.raw.front_yw)
            }; // wImage = new Mat[]{}
            w4Image = new Mat[]{
                    br[53],
                    Utils.loadResource(context, R.raw.front_rw4),
                    Utils.loadResource(context, R.raw.front_bw4),
                    Utils.loadResource(context, R.raw.front_gw4),
                    Utils.loadResource(context, R.raw.front_yw4)
            }; // w4Image = new Mat[]{}
            for (i = 1; i < 5; ++i) {
                Imgproc.cvtColor(wImage[i], wImage[i], Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(w4Image[i], w4Image[i], Imgproc.COLOR_BGR2RGB);
                loaded += 2;
                Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");
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

        // Initialize other members
        players = 3;
        direction = 0;
        now = Player.YOU;
        difficulty = LV_EASY;
        used = new ArrayList<>();
        deck = new LinkedList<>();
        recent = new ArrayList<>();
        recent_readOnly = Collections.unmodifiableList(recent);
        player = new Player[]{
                new Player(), // YOU
                new Player(), // COM1
                new Player(), // COM2
                new Player()  // COM3
        }; // player = new Player[]{}

        // Generate a random seed based on the current time stamp
        rand = new Random();
    } // Uno() (Class Constructor)

    /**
     * @param context Pass a context object to let we get the card image
     *                resources stored in this application.
     * @return Reference of our singleton.
     */
    public static Uno getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Uno.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Uno(context);
                } // if (INSTANCE == null)
            } // synchronized (Uno.class)
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
     * @param level   Pass LV_EASY or LV_HARD.
     * @param hiLight Pass true if you want to get a hi-lighted image,
     *                or false if you want to get a dark image.
     * @return Corresponding difficulty button image.
     */
    public Mat getLevelImage(int level, boolean hiLight) {
        return level == LV_EASY ?
                /* level == LV_EASY */ (hiLight ? easyImage : easyImage_d) :
                /* level == LV_HARD */ (hiLight ? hardImage : hardImage_d);
    } // getLevelImage()

    /**
     * @return Background image resource in current direction.
     */
    public Mat getBackground() {
        switch (direction) {
            case DIR_LEFT:
                return bgClockwise; // case DIR_LEFT

            case DIR_RIGHT:
                return bgCounter; // case DIR_RIGHT

            default:
                return bgWelcome; // default
        } // switch (direction)
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
     * @return Player in turn. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    public int getNow() {
        return now;
    } // getNow()

    /**
     * Switch to next player's turn.
     *
     * @return Player in turn after switched. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    public int switchNow() {
        now = getNext();
        return now;
    } // switchNow()

    /**
     * @return Current player's next player. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    public int getNext() {
        int next = (now + direction) % 4;
        if (players == 3 && next == Player.COM2) {
            next = (next + direction) % 4;
        } // if (players == 3 && next == Player.COM2)

        return next;
    } // getNext()

    /**
     * @return Current player's opposite player. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * NOTE: When only 3 players in game, getOppo() == getPrev().
     */
    public int getOppo() {
        int oppo = (getNext() + direction) % 4;
        if (players == 3 && oppo == Player.COM2) {
            oppo = (oppo + direction) % 4;
        } // if (players == 3 && oppo == Player.COM2)

        return oppo;
    } // getOppo()

    /**
     * @return Current player's previous player. Must be one of the following:
     * Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    public int getPrev() {
        int prev = (4 + now - direction) % 4;
        if (players == 3 && prev == Player.COM2) {
            prev = (4 + prev - direction) % 4;
        } // if (players == 3 && prev == Player.COM2)

        return prev;
    } // getPrev()

    /**
     * @return How many players in game (3 or 4).
     */
    public int getPlayers() {
        return players;
    } // getPlayers()

    /**
     * Set the amount of players in game.
     *
     * @param players Supports 3 and 4.
     */
    public void setPlayers(int players) {
        if (players == 3 || players == 4) {
            this.players = players;
        } // if (players == 3 || players == 4)
    } // setPlayers()

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
     * @return Current difficulty (LV_EASY / LV_HARD).
     */
    public int getDifficulty() {
        return difficulty;
    } // getDifficulty()

    /**
     * Set game difficulty.
     *
     * @param difficulty Pass target difficulty value.
     *                   Only LV_EASY and LV_HARD are available.
     */
    public void setDifficulty(int difficulty) {
        if (difficulty == LV_EASY || difficulty == LV_HARD) {
            this.difficulty = difficulty;
        } // if (difficulty == LV_EASY || difficulty == LV_HARD)
    } // setDifficulty()

    /**
     * @param who Get which player's instance. Must be one of the following:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Specified player's instance.
     */
    public Player getPlayer(int who) {
        return player[who];
    } // getPlayer()

    /**
     * Find a card instance in card table.
     *
     * @param color   Color of the card you want to get.
     * @param content Content of the card you want to get.
     * @return Corresponding card instance.
     */
    public Card findCard(Color color, Content content) {
        Card result;

        result = null;
        for (Card card : table) {
            if (card.color == color && card.content == content) {
                result = card;
                break;
            } // if (card.color == color && card.content == content)
        } // for (Card card : table)

        return result;
    } // findCard()

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
        return recent_readOnly;
    } // getRecent()

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    public void start() {
        Card card;
        int i, index, size;
        List<Card> allCards;

        // Clear card deck, used card deck, recent played cards,
        // everyone's hand cards, and everyone's safe/dangerous colors
        deck.clear();
        used.clear();
        recent.clear();
        for (i = Player.YOU; i <= Player.COM3; ++i) {
            player[i].handCards.clear();
            player[i].safeColor = NONE;
            player[i].dangerousColor = NONE;
        } // for (i = Player.YOU; i <= Player.COM3; ++i)

        // Reset direction
        direction = DIR_LEFT;

        // Generate a temporary sequenced card deck
        allCards = new ArrayList<>();
        for (i = 0; i < 108; ++i) {
            if (table[i].isWild()) {
                // reset the wild cards' colors
                table[i].color = NONE;
            } // if (table[i].isWild())

            allCards.add(table[i]);
        } // for (i = 0; i < 108; ++i)

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
        if (players == 3) {
            for (i = 0; i < 7; ++i) {
                draw(Player.YOU,  /* force */ true);
                draw(Player.COM1, /* force */ true);
                draw(Player.COM3, /* force */ true);
            } // for (i = 0; i < 7; ++i)
        } // if (players == 3)
        else {
            for (i = 0; i < 7; ++i) {
                draw(Player.YOU,  /* force */ true);
                draw(Player.COM1, /* force */ true);
                draw(Player.COM2, /* force */ true);
                draw(Player.COM3, /* force */ true);
            } // for (i = 0; i < 7; ++i)
        } // else

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
     * @param who   Who draws a card. Must be one of the following values:
     *              Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param force Pass true if the specified player is required to draw cards,
     *              i.e. previous player played a [+2] or [wild +4] to let this
     *              player draw cards. Or false if the specified player draws a
     *              card by itself in its action.
     * @return Reference of the drawn card, or null if the specified player
     * didn't draw a card because of the limit.
     */
    public Card draw(int who, boolean force) {
        Card card, picked;
        int i, index, size;
        List<Card> handCards;

        card = null;
        if (!force) {
            // Draw a card by player itself, register safe color
            player[who].safeColor = recent.get(recent.size() - 1).color;
            if (player[who].safeColor == player[who].dangerousColor) {
                // Safe color cannot also be dangerous color
                player[who].dangerousColor = NONE;
            } // if (player[who].safeColor == player[who].dangerousColor)
        } // if (!force)

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
                    picked = used.get(index);
                    if (picked.isWild()) {
                        // reset the used wild cards' colors
                        picked.color = NONE;
                    } // if (picked.isWild())

                    deck.add(picked);
                    used.remove(index);
                    --size;
                } // while (size > 0)
            } // if (deck.isEmpty())
        } // if (handCards.size() < MAX_HOLD_CARDS)

        return card;
    } // draw()

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card Check which card's legality.
     * @return Whether the specified card is legal to play.
     */
    public boolean isLegalToPlay(Card card) {
        Card previous;
        boolean result;

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
                result = card.color == previous.color;
            } // else
        } // else

        return result;
    } // isLegalToPlay()

    /**
     * Call this method when someone needs to play a card. The played card
     * replaces the "previous played card", and the original "previous played
     * card" becomes a used card at the same time.
     * <p>
     * NOTE: Before calling this method, you must call isLegalToPlay(Card*)
     * method at first to check whether the specified card is legal to play.
     * This method will play the card directly without checking the legality.
     *
     * @param who   Who plays a card. Must be one of the following values:
     *              Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param index Play which card. Pass the corresponding card's index of the
     *              specified player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     * @return Reference of the played card.
     */
    public Card play(int who, int index, Color color) {
        int size;
        Card card;
        List<Card> handCards;

        card = null;
        handCards = player[who].handCards;
        size = handCards.size();
        if (index < size) {
            card = handCards.get(index);
            handCards.remove(index);
            if (card.isWild()) {
                // When a wild card is played, register the specified
                // following legal color as the player's dangerous color
                card.color = color;
                player[who].dangerousColor = color;
                player[who].dangerousCount = 1 + size / 3;
                if (color == player[who].safeColor) {
                    // Dangerous color cannot also be safe color
                    player[who].safeColor = NONE;
                } // if (color == player[who].safeColor)
            } // if（card.isWild())
            else if (card.color == player[who].dangerousColor) {
                // Played a card that matches the registered
                // dangerous color, dangerous counter counts down
                --player[who].dangerousCount;
                if (player[who].dangerousCount == 0) {
                    player[who].dangerousColor = NONE;
                } // if (player[who].dangerousCount == 0)
            } // else if (card.color == player[who].dangerousColor)
            else if (player[who].dangerousCount > size - 1) {
                // Correct the value of dangerous counter when necessary
                player[who].dangerousCount = size - 1;
            } // else if (player[who].dangerousCount > size - 1)

            player[who].recent = card;
            recent.add(card);
            if (recent.size() > 5) {
                used.add(recent.get(0));
                recent.remove(0);
            } // if (recent.size() > 5)

            if (handCards.size() == 0) {
                // Game over, change background image
                direction = 0;
            } // if (handCards.size() == 0)
        } // if (index < size)

        return card;
    } // play()
} // Uno Class

// E.O.F