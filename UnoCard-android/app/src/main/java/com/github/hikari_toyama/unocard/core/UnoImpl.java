////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

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

import android.content.Context;
import android.util.Log;

import com.github.hikari_toyama.unocard.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Uno Runtime Class (Singleton).
 */
class UnoImpl extends Uno {
    /**
     * Random number generator.
     */
    private static final Random rnd = new Random();

    /**
     * Tag name for Android Logcat.
     */
    private static final String TAG = "Uno";

    /**
     * Recent played cards (read-only version, provide for external accesses).
     */
    private final List<Card> recent_readOnly;

    /**
     * Colors of recent played cards
     * (read-only version, provide for external accesses).
     */
    private final List<Color> recentColors_readOnly;

    /**
     * Singleton, hide default constructor.
     *
     * @param context Pass a context object to let us get the card image
     *                resources stored in this application.
     */
    UnoImpl(Context context) {
        Mat[] br, dk;
        int i, loaded, total;

        // Preparations
        context = context.getApplicationContext();
        loaded = 0;
        total = 124;
        Log.i(TAG, "Loading... (0%)");

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
                new CardImpl(br[0], dk[0], RED, NUM0, "Red 0"),
                new CardImpl(br[1], dk[1], RED, NUM1, "Red 1"),
                new CardImpl(br[2], dk[2], RED, NUM2, "Red 2"),
                new CardImpl(br[3], dk[3], RED, NUM3, "Red 3"),
                new CardImpl(br[4], dk[4], RED, NUM4, "Red 4"),
                new CardImpl(br[5], dk[5], RED, NUM5, "Red 5"),
                new CardImpl(br[6], dk[6], RED, NUM6, "Red 6"),
                new CardImpl(br[7], dk[7], RED, NUM7, "Red 7"),
                new CardImpl(br[8], dk[8], RED, NUM8, "Red 8"),
                new CardImpl(br[9], dk[9], RED, NUM9, "Red 9"),
                new CardImpl(br[10], dk[10], RED, DRAW2, "Red +2"),
                new CardImpl(br[11], dk[11], RED, SKIP, "Red Skip"),
                new CardImpl(br[12], dk[12], RED, REV, "Red Reverse"),
                new CardImpl(br[13], dk[13], BLUE, NUM0, "Blue 0"),
                new CardImpl(br[14], dk[14], BLUE, NUM1, "Blue 1"),
                new CardImpl(br[15], dk[15], BLUE, NUM2, "Blue 2"),
                new CardImpl(br[16], dk[16], BLUE, NUM3, "Blue 3"),
                new CardImpl(br[17], dk[17], BLUE, NUM4, "Blue 4"),
                new CardImpl(br[18], dk[18], BLUE, NUM5, "Blue 5"),
                new CardImpl(br[19], dk[19], BLUE, NUM6, "Blue 6"),
                new CardImpl(br[20], dk[20], BLUE, NUM7, "Blue 7"),
                new CardImpl(br[21], dk[21], BLUE, NUM8, "Blue 8"),
                new CardImpl(br[22], dk[22], BLUE, NUM9, "Blue 9"),
                new CardImpl(br[23], dk[23], BLUE, DRAW2, "Blue +2"),
                new CardImpl(br[24], dk[24], BLUE, SKIP, "Blue Skip"),
                new CardImpl(br[25], dk[25], BLUE, REV, "Blue Reverse"),
                new CardImpl(br[26], dk[26], GREEN, NUM0, "Green 0"),
                new CardImpl(br[27], dk[27], GREEN, NUM1, "Green 1"),
                new CardImpl(br[28], dk[28], GREEN, NUM2, "Green 2"),
                new CardImpl(br[29], dk[29], GREEN, NUM3, "Green 3"),
                new CardImpl(br[30], dk[30], GREEN, NUM4, "Green 4"),
                new CardImpl(br[31], dk[31], GREEN, NUM5, "Green 5"),
                new CardImpl(br[32], dk[32], GREEN, NUM6, "Green 6"),
                new CardImpl(br[33], dk[33], GREEN, NUM7, "Green 7"),
                new CardImpl(br[34], dk[34], GREEN, NUM8, "Green 8"),
                new CardImpl(br[35], dk[35], GREEN, NUM9, "Green 9"),
                new CardImpl(br[36], dk[36], GREEN, DRAW2, "Green +2"),
                new CardImpl(br[37], dk[37], GREEN, SKIP, "Green Skip"),
                new CardImpl(br[38], dk[38], GREEN, REV, "Green Reverse"),
                new CardImpl(br[39], dk[39], YELLOW, NUM0, "Yellow 0"),
                new CardImpl(br[40], dk[40], YELLOW, NUM1, "Yellow 1"),
                new CardImpl(br[41], dk[41], YELLOW, NUM2, "Yellow 2"),
                new CardImpl(br[42], dk[42], YELLOW, NUM3, "Yellow 3"),
                new CardImpl(br[43], dk[43], YELLOW, NUM4, "Yellow 4"),
                new CardImpl(br[44], dk[44], YELLOW, NUM5, "Yellow 5"),
                new CardImpl(br[45], dk[45], YELLOW, NUM6, "Yellow 6"),
                new CardImpl(br[46], dk[46], YELLOW, NUM7, "Yellow 7"),
                new CardImpl(br[47], dk[47], YELLOW, NUM8, "Yellow 8"),
                new CardImpl(br[48], dk[48], YELLOW, NUM9, "Yellow 9"),
                new CardImpl(br[49], dk[49], YELLOW, DRAW2, "Yellow +2"),
                new CardImpl(br[50], dk[50], YELLOW, SKIP, "Yellow Skip"),
                new CardImpl(br[51], dk[51], YELLOW, REV, "Yellow Reverse"),
                new CardImpl(br[52], dk[52], NONE, WILD, "Wild"),
                new CardImpl(br[52], dk[52], NONE, WILD, "Wild"),
                new CardImpl(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new CardImpl(br[1], dk[1], RED, NUM1, "Red 1"),
                new CardImpl(br[2], dk[2], RED, NUM2, "Red 2"),
                new CardImpl(br[3], dk[3], RED, NUM3, "Red 3"),
                new CardImpl(br[4], dk[4], RED, NUM4, "Red 4"),
                new CardImpl(br[5], dk[5], RED, NUM5, "Red 5"),
                new CardImpl(br[6], dk[6], RED, NUM6, "Red 6"),
                new CardImpl(br[7], dk[7], RED, NUM7, "Red 7"),
                new CardImpl(br[8], dk[8], RED, NUM8, "Red 8"),
                new CardImpl(br[9], dk[9], RED, NUM9, "Red 9"),
                new CardImpl(br[10], dk[10], RED, DRAW2, "Red +2"),
                new CardImpl(br[11], dk[11], RED, SKIP, "Red Skip"),
                new CardImpl(br[12], dk[12], RED, REV, "Red Reverse"),
                new CardImpl(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new CardImpl(br[14], dk[14], BLUE, NUM1, "Blue 1"),
                new CardImpl(br[15], dk[15], BLUE, NUM2, "Blue 2"),
                new CardImpl(br[16], dk[16], BLUE, NUM3, "Blue 3"),
                new CardImpl(br[17], dk[17], BLUE, NUM4, "Blue 4"),
                new CardImpl(br[18], dk[18], BLUE, NUM5, "Blue 5"),
                new CardImpl(br[19], dk[19], BLUE, NUM6, "Blue 6"),
                new CardImpl(br[20], dk[20], BLUE, NUM7, "Blue 7"),
                new CardImpl(br[21], dk[21], BLUE, NUM8, "Blue 8"),
                new CardImpl(br[22], dk[22], BLUE, NUM9, "Blue 9"),
                new CardImpl(br[23], dk[23], BLUE, DRAW2, "Blue +2"),
                new CardImpl(br[24], dk[24], BLUE, SKIP, "Blue Skip"),
                new CardImpl(br[25], dk[25], BLUE, REV, "Blue Reverse"),
                new CardImpl(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new CardImpl(br[27], dk[27], GREEN, NUM1, "Green 1"),
                new CardImpl(br[28], dk[28], GREEN, NUM2, "Green 2"),
                new CardImpl(br[29], dk[29], GREEN, NUM3, "Green 3"),
                new CardImpl(br[30], dk[30], GREEN, NUM4, "Green 4"),
                new CardImpl(br[31], dk[31], GREEN, NUM5, "Green 5"),
                new CardImpl(br[32], dk[32], GREEN, NUM6, "Green 6"),
                new CardImpl(br[33], dk[33], GREEN, NUM7, "Green 7"),
                new CardImpl(br[34], dk[34], GREEN, NUM8, "Green 8"),
                new CardImpl(br[35], dk[35], GREEN, NUM9, "Green 9"),
                new CardImpl(br[36], dk[36], GREEN, DRAW2, "Green +2"),
                new CardImpl(br[37], dk[37], GREEN, SKIP, "Green Skip"),
                new CardImpl(br[38], dk[38], GREEN, REV, "Green Reverse"),
                new CardImpl(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"),
                new CardImpl(br[40], dk[40], YELLOW, NUM1, "Yellow 1"),
                new CardImpl(br[41], dk[41], YELLOW, NUM2, "Yellow 2"),
                new CardImpl(br[42], dk[42], YELLOW, NUM3, "Yellow 3"),
                new CardImpl(br[43], dk[43], YELLOW, NUM4, "Yellow 4"),
                new CardImpl(br[44], dk[44], YELLOW, NUM5, "Yellow 5"),
                new CardImpl(br[45], dk[45], YELLOW, NUM6, "Yellow 6"),
                new CardImpl(br[46], dk[46], YELLOW, NUM7, "Yellow 7"),
                new CardImpl(br[47], dk[47], YELLOW, NUM8, "Yellow 8"),
                new CardImpl(br[48], dk[48], YELLOW, NUM9, "Yellow 9"),
                new CardImpl(br[49], dk[49], YELLOW, DRAW2, "Yellow +2"),
                new CardImpl(br[50], dk[50], YELLOW, SKIP, "Yellow Skip"),
                new CardImpl(br[51], dk[51], YELLOW, REV, "Yellow Reverse"),
                new CardImpl(br[52], dk[52], NONE, WILD, "Wild"),
                new CardImpl(br[52], dk[52], NONE, WILD, "Wild")
        }; // table = new Card[]{}

        // Initialize other members
        players = 3;
        now = rnd.nextInt(4);
        difficulty = LV_EASY;
        direction = draw2StackCount = 0;
        sevenZeroRule = draw2StackRule = false;
        used = new ArrayList<>();
        deck = new ArrayList<>();
        recent = new ArrayList<>();
        recentColors = new ArrayList<>();
        recent_readOnly = Collections.unmodifiableList(recent);
        recentColors_readOnly = Collections.unmodifiableList(recentColors);
        player = new Player[]{
                new PlayerImpl(), // YOU
                new PlayerImpl(), // COM1
                new PlayerImpl(), // COM2
                new PlayerImpl()  // COM3
        }; // player = new Player[]{}
    } // UnoImpl(Context) (Class Constructor)

    /**
     * @return Card back image resource.
     */
    @Override
    public Mat getBackImage() {
        return backImage;
    } // getBackImage()

    /**
     * @param level   Pass LV_EASY or LV_HARD.
     * @param hiLight Pass true if you want to get a hi-lighted image,
     *                or false if you want to get a dark image.
     * @return Corresponding difficulty button image.
     */
    @Override
    public Mat getLevelImage(int level, boolean hiLight) {
        return level == LV_EASY ?
                /* level == LV_EASY */ (hiLight ? easyImage : easyImage_d) :
                /* level == LV_HARD */ (hiLight ? hardImage : hardImage_d);
    } // getLevelImage(int, boolean)

    /**
     * @return Background image resource in current direction.
     */
    @Override
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
     * When a player played a wild card and specified a following legal color,
     * get the corresponding color-filled image here, and show it in recent
     * card area.
     *
     * @param color The wild image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    @Override
    public Mat getColoredWildImage(Color color) {
        return wImage[color.ordinal()];
    } // getColoredWildImage(Color)

    /**
     * When a player played a wild +4 card and specified a following legal
     * color, get the corresponding color-filled image here, and show it in
     * recent card area.
     *
     * @param color The wild +4 image with which color filled you want to get.
     * @return Corresponding color-filled image.
     */
    @Override
    public Mat getColoredWildDraw4Image(Color color) {
        return w4Image[color.ordinal()];
    } // getColoredWildDraw4Image(Color)

    /**
     * @return Player in turn. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    @Override
    public int getNow() {
        return now;
    } // getNow()

    /**
     * Switch to next player's turn.
     *
     * @return Player in turn after switched. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    @Override
    public int switchNow() {
        return (now = getNext());
    } // switchNow()

    /**
     * @return Current player's next player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    @Override
    public int getNext() {
        int next = (now + direction) % 4;
        if (players == 3 && next == Player.COM2) {
            next = (next + direction) % 4;
        } // if (players == 3 && next == Player.COM2)

        return next;
    } // getNext()

    /**
     * @return Current player's opposite player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * NOTE: When only 3 players in game, getOppo() == getPrev().
     */
    @Override
    public int getOppo() {
        int oppo = (getNext() + direction) % 4;
        if (players == 3 && oppo == Player.COM2) {
            oppo = (oppo + direction) % 4;
        } // if (players == 3 && oppo == Player.COM2)

        return oppo;
    } // getOppo()

    /**
     * @return Current player's previous player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    @Override
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
    @Override
    public int getPlayers() {
        return players;
    } // getPlayers()

    /**
     * Set the amount of players in game.
     *
     * @param players Supports 3 and 4.
     */
    @Override
    public void setPlayers(int players) {
        if (players == 3 || players == 4) {
            this.players = players;
        } // if (players == 3 || players == 4)
    } // setPlayers(int)

    /**
     * Switch current action sequence.
     *
     * @return Switched action sequence. DIR_LEFT for clockwise,
     * or DIR_RIGHT for counter-clockwise.
     */
    @Override
    public int switchDirection() {
        return (direction = 4 - direction);
    } // switchDirection()

    /**
     * @param who Get which player's instance. Must be one of the following:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Specified player's instance.
     */
    @Override
    public Player getPlayer(int who) {
        return who < Player.YOU || who > Player.COM3 ? null : player[who];
    } // getPlayer(int)

    /**
     * @return Current difficulty (LV_EASY / LV_HARD).
     */
    @Override
    public int getDifficulty() {
        return difficulty;
    } // getDifficulty()

    /**
     * Set game difficulty.
     *
     * @param difficulty Pass target difficulty value.
     *                   Only LV_EASY and LV_HARD are available.
     */
    @Override
    public void setDifficulty(int difficulty) {
        if (difficulty == LV_EASY || difficulty == LV_HARD) {
            this.difficulty = difficulty;
        } // if (difficulty == LV_EASY || difficulty == LV_HARD)
    } // setDifficulty(int)

    /**
     * @return Whether the 7-0 rule is enabled. In 7-0 rule, when a seven card
     * is put down, the player must swap hand cards with another player
     * immediately. When a zero card is put down, everyone need to pass
     * the hand cards to the next player.
     */
    @Override
    public boolean isSevenZeroRule() {
        return sevenZeroRule;
    } // isSevenZeroRule()

    /**
     * @param enabled Enable/Disable the 7-0 rule.
     */
    @Override
    public void setSevenZeroRule(boolean enabled) {
        sevenZeroRule = enabled;
    } // setSevenZeroRule(boolean)

    /**
     * @return Can or cannot stack +2 cards. If can, when you put down a +2
     * card, the next player may transfer the punishment to its next
     * player by stacking another +2 card. Finally the first one who
     * does not stack a +2 card must draw all of the required cards.
     */
    @Override
    public boolean isDraw2StackRule() {
        return draw2StackRule;
    } // isDraw2StackRule()

    /**
     * @param enabled Enable/Disable the +2 stacking rule.
     */
    @Override
    public void setDraw2StackRule(boolean enabled) {
        draw2StackRule = enabled;
    } // setDraw2StackRule(boolean)

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
    @Override
    public int getDraw2StackCount() {
        return draw2StackCount;
    } // getDraw2StackCount()

    /**
     * Find a card instance in card table.
     *
     * @param color   Color of the card you want to get.
     * @param content Content of the card you want to get.
     * @return Corresponding card instance.
     */
    @Override
    public Card findCard(Color color, Content content) {
        int i;
        Card result;

        result = null;
        for (i = 0; i < 108; ++i) {
            if (table[i].color == color && table[i].content == content) {
                result = table[i];
                break;
            } // if (table[i].color == color && table[i].content == content)
        } // for (i = 0; i < 108; ++i)

        return result;
    } // findCard(Color, Content)

    /**
     * @return How many cards in deck (haven't been used yet).
     */
    @Override
    public int getDeckCount() {
        return deck.size();
    } // getDeckCount()

    /**
     * @return How many cards have been used.
     */
    @Override
    public int getUsedCount() {
        return used.size() + recent.size();
    } // getUsedCount()

    /**
     * @return Recent played cards.
     */
    @Override
    public List<Card> getRecent() {
        return recent_readOnly;
    } // getRecent()

    /**
     * @return Colors of recent played cards.
     */
    @Override
    public List<Color> getRecentColors() {
        return recentColors_readOnly;
    } // getRecentColors()

    /**
     * @return Color of the last played card.
     */
    @Override
    public Color lastColor() {
        return recentColors.get(recentColors.size() - 1);
    } // lastColor()

    /**
     * @return Color of the next-to-last played card.
     */
    @Override
    public Color next2lastColor() {
        return recentColors.get(recentColors.size() - 2);
    } // next2lastColor()

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    @Override
    public void start() {
        Card card;
        int i, size;

        // Reset direction
        direction = DIR_LEFT;

        // In +2 stack rule, reset the stack counter
        draw2StackCount = 0;

        // Clear card deck, used card deck, recent played cards,
        // everyone's hand cards, and everyone's strong/weak colors
        deck.clear();
        used.clear();
        recent.clear();
        recentColors.clear();
        for (i = Player.YOU; i <= Player.COM3; ++i) {
            player[i].handCards.clear();
            player[i].weakColor = NONE;
            player[i].strongColor = NONE;
        } // for (i = Player.YOU; i <= Player.COM3; ++i)

        // Generate a temporary sequenced card deck
        for (i = 0; i < 108; ++i) {
            deck.add(table[i]);
        } // for (i = 0; i < 108; ++i)

        // Shuffle cards
        size = deck.size();
        while (size > 0) {
            i = rnd.nextInt(size--);
            card = deck.get(i);
            deck.set(i, deck.get(size));
            deck.set(size, card);
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
        do {
            card = deck.get(deck.size() - 1);
            deck.remove(deck.size() - 1);
            if (card.isWild()) {
                // Start card cannot be a wild card, so return it
                // to the bottom of card deck and pick another card
                deck.add(0, card);
            } // if (card.isWild())
            else {
                // Any non-wild card can be start card
                // Start card determined
                recent.add(card);
                recentColors.add(card.color);
            } // else
        } while (recent.isEmpty());

        // In the case of (last winner = NORTH) & (game mode = 3 player mode)
        // Re-specify the dealer randomly
        if (players == 3 && now == Player.COM2) {
            now = (3 + rnd.nextInt(3)) % 4;
        } // if (players == 3 && now == Player.COM2)
    } // start()

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
    @Override
    public int draw(int who, boolean force) {
        Card card;
        List<Card> hand;
        int i, index, size;

        i = -1;
        if (who >= Player.YOU && who <= Player.COM3) {
            if (draw2StackCount > 0) {
                --draw2StackCount;
            } // if (draw2StackCount > 0)
            else if (!force) {
                // Draw a card by player itself, register weak color
                player[who].weakColor = lastColor();
                if (player[who].weakColor == player[who].strongColor) {
                    // Weak color cannot also be strong color
                    player[who].strongColor = NONE;
                } // if (player[who].weakColor == player[who].strongColor)
            } // else if (!force)

            hand = player[who].handCards;
            if (hand.size() < MAX_HOLD_CARDS) {
                // Draw a card from card deck, and put it to an appropriate position
                card = deck.get(deck.size() - 1);
                deck.remove(deck.size() - 1);
                for (i = 0; i < hand.size(); ++i) {
                    if (hand.get(i).order > card.order) {
                        // Found an appropriate position to insert the new card,
                        // which keeps the player's hand cards sequenced
                        break;
                    } // if (hand.get(i).order > card.order)
                } // for (i = 0; i < hand.size(); ++i)

                hand.add(i, card);
                player[who].recent = null;
                if (deck.isEmpty()) {
                    // Re-use the used cards when there are no more cards in deck
                    size = used.size();
                    while (size > 0) {
                        index = rnd.nextInt(size--);
                        deck.add(used.get(index));
                        used.remove(index);
                    } // while (size > 0)
                } // if (deck.isEmpty())
            } // if (hand.size() < MAX_HOLD_CARDS)
            else {
                // In +2 stack rule, if someone cannot draw all of the required
                // cards because of the max-hold-card limitation, force reset
                // the counter to zero.
                draw2StackCount = 0;
            } // else
        } // if (who >= Player.YOU && who <= Player.COM3)

        return i;
    } // draw(int, boolean)

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card Check which card's legality.
     * @return Whether the specified card is legal to play.
     */
    @Override
    public boolean isLegalToPlay(Card card) {
        boolean result;

        if (card == null || recent.isEmpty()) {
            // Null Pointer
            result = false;
        } // if (card == null || recent.isEmpty())
        else if (draw2StackCount > 0) {
            // When in +2 stacking procedure, only +2 cards are legal
            result = card.content == DRAW2;
        } // else if (draw2StackCount > 0)
        else if (card.isWild()) {
            // Wild cards: LEGAL
            result = true;
        } // else if (card.isWild())
        else {
            // Same content to previous: LEGAL
            // Same color to previous: LEGAL
            // Other cards: ILLEGAL
            result = card.content == recent.get(recent.size() - 1).content
                    || card.color == lastColor();
        } // else

        return result;
    } // isLegalToPlay(Card)

    /**
     * @return How many legal cards (the cards that can be played legally)
     * in now player's hand.
     */
    @Override
    public int legalCardsCount4NowPlayer() {
        int count = 0;

        for (Card card : player[now].handCards) {
            if (isLegalToPlay(card)) {
                ++count;
            } // if (isLegalToPlay(card))
        } // for (Card card : player[now].handCards)

        return count;
    } // legalCardsCount4NowPlayer()

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
    @Override
    public Card play(int who, int index, Color color) {
        int size;
        Card card;
        List<Card> hand;

        card = null;
        if (who >= Player.YOU && who <= Player.COM3) {
            hand = player[who].handCards;
            size = hand.size();
            if (index < size) {
                card = hand.get(index);
                hand.remove(index);
                if (card.isWild()) {
                    // When a wild card is played, register the specified
                    // following legal color as the player's strong color
                    player[who].strongColor = color;
                    player[who].strongCount = 1 + size / 3;
                    if (color == player[who].weakColor) {
                        // Strong color cannot also be weak color
                        player[who].weakColor = NONE;
                    } // if (color == player[who].weakColor)
                } // if (card.isWild())
                else if (card.color == player[who].strongColor) {
                    // Played a card that matches the registered
                    // strong color, strong counter counts down
                    --player[who].strongCount;
                    if (player[who].strongCount == 0) {
                        player[who].strongColor = NONE;
                    } // if (player[who].strongCount == 0)
                } // else if (card.color == player[who].strongColor)
                else if (player[who].strongCount > size - 1) {
                    // Correct the value of strong counter when necessary
                    player[who].strongCount = size - 1;
                } // else if (player[who].strongCount > size - 1)

                if (card.content == DRAW2 && draw2StackRule) {
                    draw2StackCount += 2;
                } // if (card.content == DRAW2 && draw2StackRule)

                player[who].recent = card;
                recent.add(card);
                recentColors.add(card.isWild() ? color : card.color);
                if (recent.size() > 5) {
                    used.add(recent.get(0));
                    recent.remove(0);
                    recentColors.remove(0);
                } // if (recent.size() > 5)

                if (hand.size() == 0) {
                    // Game over, change background image
                    direction = 0;
                } // if (hand.size() == 0)
            } // if (index < size)
        } // if (who >= Player.YOU && who <= Player.COM3)

        return card;
    } // play(int, int, Color)

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
    @Override
    public void swap(int a, int b) {
        Player store = player[a];
        player[a] = player[b];
        player[b] = store;
    } // swap(int, int)

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass the hand
     * cards to the next player.
     */
    @Override
    public void cycle() {
        int curr = now, next = getNext(), oppo = getOppo(), prev = getPrev();
        Player store = player[curr];
        player[curr] = player[prev];
        player[prev] = player[oppo];
        player[oppo] = player[next];
        player[next] = store;
    } // cycle()
} // UnoImpl Class

// E.O.F