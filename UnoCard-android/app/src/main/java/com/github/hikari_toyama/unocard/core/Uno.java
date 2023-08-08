////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import static com.github.hikari_toyama.unocard.core.Color.NONE;
import static com.github.hikari_toyama.unocard.core.Content.DRAW2;
import static com.github.hikari_toyama.unocard.core.Content.REV;
import static com.github.hikari_toyama.unocard.core.Content.WILD;
import static com.github.hikari_toyama.unocard.core.Content.WILD_DRAW4;

import android.content.Context;
import android.util.Log;

import com.github.hikari_toyama.unocard.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Uno Runtime Class (Singleton).
 */
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
     * In this application, everyone can hold 26 cards at most.
     */
    public static final int MAX_HOLD_CARDS = 26;

    /**
     * Random number generator.
     */
    static final Random RNG = new Random();

    /**
     * Tag name for Android Logcat.
     */
    static final String TAG = "Uno";

    /**
     * Our custom font's character-to-position map.
     */
    static final Map<Character, Integer> CHAR_MAP = new TreeMap<>();

    static {
        char[] hanZi = new char[]{
                '一', '上', '下', '东', '为', '乐', '人', '仍',
                '从', '令', '以', '传', '余', '你', '保', '再',
                '准', '出', '击', '分', '则', '到', '剩', '功',
                '加', '北', '南', '发', '变', '叠', '可', '合',
                '向', '否', '和', '回', '堆', '备', '多', '失',
                '始', '定', '家', '将', '已', '度', '开', '张',
                '戏', '成', '或', '战', '所', '打', '托', '择',
                '指', '挑', '换', '接', '摸', '改', '效', '数',
                '新', '方', '无', '时', '是', '最', '有', '来',
                '标', '次', '欢', '法', '游', '点', '牌', '留',
                '的', '目', '管', '红', '给', '绿', '置', '色',
                '蓝', '被', '西', '规', '认', '设', '败', '跳',
                '过', '迎', '选', '重', '难', '音', '颜', '黄'
        }; // new char[]{}

        for (int i = 0x20; i <= 0x7f; ++i) {
            int r = (i >>> 4) - 2, c = i & 0x0f;
            CHAR_MAP.put((char) i, (r << 4) | c);
        } // for (int i = 0x20; i <= 0x7f; ++i)

        for (int i = 0; i < hanZi.length; ++i) {
            int r = (i >>> 3) + 6, c = i & 0x07;
            CHAR_MAP.put(hanZi[i], (r << 4) | c);
        } // for (int i = 0; i < hanZi.length; ++i)
    } // static

    /**
     * Card deck (ready to use).
     */
    final List<Card> deck = new ArrayList<>();

    /**
     * Used cards.
     */
    final List<Card> used = new ArrayList<>();

    /**
     * Recent played cards.
     */
    RecentInfo[] recent;

    /**
     * Recent played cards (read-only version, provide for external accesses).
     */
    RecentInfo[] constRecent;

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
     * 2vs2 button image resources.
     */
    Mat light2vs2, dark2vs2;

    /**
     * Font image.
     */
    Mat font;

    /**
     * Color blocks. Used for drawing characters.
     */
    Mat[] blk_r, blk_b, blk_g, blk_y, blk_w;

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
     * Whether the 2vs2 rule is enabled.
     */
    boolean _2vs2;

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
     * How many initial cards for everyone in each game.
     * This value can be set to 5~20. Default to 7.
     */
    int initialCards;

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
     * Access colorAnalysis[A] to get how many cards in color A are used.
     * Access contentAnalysis[B] to get how many cards in content B are used.
     */
    int[] colorAnalysis, contentAnalysis;

    /**
     * Record the replay data.
     */
    StringBuilder replay;

    /**
     * Iterator of your loaded replay.
     */
    Iterator<String> it;

    /**
     * The directory that stores your replay save files.
     */
    File replayDir;

    /**
     * Singleton, hide default constructor.
     *
     * @param c Pass a context object to let us get the card image
     *          resources stored in this application.
     * @throws IOException Thrown if failed to load image resources.
     */
    Uno(Context c) throws IOException {
        Mat[] br, dk;
        int i, loaded, total;
        Scalar rgb_red, rgb_blue, rgb_green, rgb_white, rgb_yellow;

        // Preparations
        loaded = 0;
        total = 127;
        Log.i(TAG, "Loading... (0%)");

        // Load background image resources
        bgWelcome = Utils.loadResource(c, R.raw.bg_welcome);
        bgCounter = Utils.loadResource(c, R.raw.bg_counter);
        bgClockwise = Utils.loadResource(c, R.raw.bg_clockwise);
        Imgproc.cvtColor(bgWelcome, bgWelcome, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(bgCounter, bgCounter, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(bgClockwise, bgClockwise, Imgproc.COLOR_BGR2RGB);
        loaded += 3;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        // Load card back image resource
        backImage = Utils.loadResource(c, R.raw.back);
        Imgproc.cvtColor(backImage, backImage, Imgproc.COLOR_BGR2RGB);
        ++loaded;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        // Load difficulty image resources
        easyImage = Utils.loadResource(c, R.raw.lv_easy);
        hardImage = Utils.loadResource(c, R.raw.lv_hard);
        easyImage_d = Utils.loadResource(c, R.raw.lv_easy_dark);
        hardImage_d = Utils.loadResource(c, R.raw.lv_hard_dark);
        Imgproc.cvtColor(easyImage, easyImage, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(hardImage, hardImage, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(easyImage_d, easyImage_d, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(hardImage_d, hardImage_d, Imgproc.COLOR_BGR2RGB);
        loaded += 4;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        // Load 2vs2 image resources
        dark2vs2 = Utils.loadResource(c, R.raw.dark_2vs2);
        light2vs2 = Utils.loadResource(c, R.raw.front_2vs2);
        Imgproc.cvtColor(dark2vs2, dark2vs2, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(light2vs2, light2vs2, Imgproc.COLOR_BGR2RGB);
        loaded += 2;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        // Load cards' front image resources
        br = new Mat[]{
                Utils.loadResource(c, R.raw.front_r0),
                Utils.loadResource(c, R.raw.front_r1),
                Utils.loadResource(c, R.raw.front_r2),
                Utils.loadResource(c, R.raw.front_r3),
                Utils.loadResource(c, R.raw.front_r4),
                Utils.loadResource(c, R.raw.front_r5),
                Utils.loadResource(c, R.raw.front_r6),
                Utils.loadResource(c, R.raw.front_r7),
                Utils.loadResource(c, R.raw.front_r8),
                Utils.loadResource(c, R.raw.front_r9),
                Utils.loadResource(c, R.raw.front_rd2),
                Utils.loadResource(c, R.raw.front_rr),
                Utils.loadResource(c, R.raw.front_rs),
                Utils.loadResource(c, R.raw.front_b0),
                Utils.loadResource(c, R.raw.front_b1),
                Utils.loadResource(c, R.raw.front_b2),
                Utils.loadResource(c, R.raw.front_b3),
                Utils.loadResource(c, R.raw.front_b4),
                Utils.loadResource(c, R.raw.front_b5),
                Utils.loadResource(c, R.raw.front_b6),
                Utils.loadResource(c, R.raw.front_b7),
                Utils.loadResource(c, R.raw.front_b8),
                Utils.loadResource(c, R.raw.front_b9),
                Utils.loadResource(c, R.raw.front_bd2),
                Utils.loadResource(c, R.raw.front_br),
                Utils.loadResource(c, R.raw.front_bs),
                Utils.loadResource(c, R.raw.front_g0),
                Utils.loadResource(c, R.raw.front_g1),
                Utils.loadResource(c, R.raw.front_g2),
                Utils.loadResource(c, R.raw.front_g3),
                Utils.loadResource(c, R.raw.front_g4),
                Utils.loadResource(c, R.raw.front_g5),
                Utils.loadResource(c, R.raw.front_g6),
                Utils.loadResource(c, R.raw.front_g7),
                Utils.loadResource(c, R.raw.front_g8),
                Utils.loadResource(c, R.raw.front_g9),
                Utils.loadResource(c, R.raw.front_gd2),
                Utils.loadResource(c, R.raw.front_gr),
                Utils.loadResource(c, R.raw.front_gs),
                Utils.loadResource(c, R.raw.front_y0),
                Utils.loadResource(c, R.raw.front_y1),
                Utils.loadResource(c, R.raw.front_y2),
                Utils.loadResource(c, R.raw.front_y3),
                Utils.loadResource(c, R.raw.front_y4),
                Utils.loadResource(c, R.raw.front_y5),
                Utils.loadResource(c, R.raw.front_y6),
                Utils.loadResource(c, R.raw.front_y7),
                Utils.loadResource(c, R.raw.front_y8),
                Utils.loadResource(c, R.raw.front_y9),
                Utils.loadResource(c, R.raw.front_yd2),
                Utils.loadResource(c, R.raw.front_yr),
                Utils.loadResource(c, R.raw.front_ys),
                Utils.loadResource(c, R.raw.front_kw),
                Utils.loadResource(c, R.raw.front_kw4)
        }; // br = new Mat[]{}
        dk = new Mat[]{
                Utils.loadResource(c, R.raw.dark_r0),
                Utils.loadResource(c, R.raw.dark_r1),
                Utils.loadResource(c, R.raw.dark_r2),
                Utils.loadResource(c, R.raw.dark_r3),
                Utils.loadResource(c, R.raw.dark_r4),
                Utils.loadResource(c, R.raw.dark_r5),
                Utils.loadResource(c, R.raw.dark_r6),
                Utils.loadResource(c, R.raw.dark_r7),
                Utils.loadResource(c, R.raw.dark_r8),
                Utils.loadResource(c, R.raw.dark_r9),
                Utils.loadResource(c, R.raw.dark_rd2),
                Utils.loadResource(c, R.raw.dark_rr),
                Utils.loadResource(c, R.raw.dark_rs),
                Utils.loadResource(c, R.raw.dark_b0),
                Utils.loadResource(c, R.raw.dark_b1),
                Utils.loadResource(c, R.raw.dark_b2),
                Utils.loadResource(c, R.raw.dark_b3),
                Utils.loadResource(c, R.raw.dark_b4),
                Utils.loadResource(c, R.raw.dark_b5),
                Utils.loadResource(c, R.raw.dark_b6),
                Utils.loadResource(c, R.raw.dark_b7),
                Utils.loadResource(c, R.raw.dark_b8),
                Utils.loadResource(c, R.raw.dark_b9),
                Utils.loadResource(c, R.raw.dark_bd2),
                Utils.loadResource(c, R.raw.dark_br),
                Utils.loadResource(c, R.raw.dark_bs),
                Utils.loadResource(c, R.raw.dark_g0),
                Utils.loadResource(c, R.raw.dark_g1),
                Utils.loadResource(c, R.raw.dark_g2),
                Utils.loadResource(c, R.raw.dark_g3),
                Utils.loadResource(c, R.raw.dark_g4),
                Utils.loadResource(c, R.raw.dark_g5),
                Utils.loadResource(c, R.raw.dark_g6),
                Utils.loadResource(c, R.raw.dark_g7),
                Utils.loadResource(c, R.raw.dark_g8),
                Utils.loadResource(c, R.raw.dark_g9),
                Utils.loadResource(c, R.raw.dark_gd2),
                Utils.loadResource(c, R.raw.dark_gr),
                Utils.loadResource(c, R.raw.dark_gs),
                Utils.loadResource(c, R.raw.dark_y0),
                Utils.loadResource(c, R.raw.dark_y1),
                Utils.loadResource(c, R.raw.dark_y2),
                Utils.loadResource(c, R.raw.dark_y3),
                Utils.loadResource(c, R.raw.dark_y4),
                Utils.loadResource(c, R.raw.dark_y5),
                Utils.loadResource(c, R.raw.dark_y6),
                Utils.loadResource(c, R.raw.dark_y7),
                Utils.loadResource(c, R.raw.dark_y8),
                Utils.loadResource(c, R.raw.dark_y9),
                Utils.loadResource(c, R.raw.dark_yd2),
                Utils.loadResource(c, R.raw.dark_yr),
                Utils.loadResource(c, R.raw.dark_ys),
                Utils.loadResource(c, R.raw.dark_kw),
                Utils.loadResource(c, R.raw.dark_kw4)
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
                Utils.loadResource(c, R.raw.front_rw),
                Utils.loadResource(c, R.raw.front_bw),
                Utils.loadResource(c, R.raw.front_gw),
                Utils.loadResource(c, R.raw.front_yw)
        }; // wImage = new Mat[]{}
        w4Image = new Mat[]{
                br[53],
                Utils.loadResource(c, R.raw.front_rw4),
                Utils.loadResource(c, R.raw.front_bw4),
                Utils.loadResource(c, R.raw.front_gw4),
                Utils.loadResource(c, R.raw.front_yw4)
        }; // w4Image = new Mat[]{}
        for (i = 1; i < 5; ++i) {
            Imgproc.cvtColor(wImage[i], wImage[i], Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(w4Image[i], w4Image[i], Imgproc.COLOR_BGR2RGB);
            loaded += 2;
            Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");
        } // for (i = 1; i < 5; ++i)

        // Load font image
        font = Utils.loadResource(c, R.raw.font_w);
        Imgproc.cvtColor(font, font, Imgproc.COLOR_BGR2RGB);
        ++loaded;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        // Generate color blocks
        rgb_red = new Scalar(0xFF, 0x77, 0x77);
        rgb_blue = new Scalar(0x77, 0x77, 0xFF);
        rgb_green = new Scalar(0x77, 0xCC, 0x77);
        rgb_white = new Scalar(0xCC, 0xCC, 0xCC);
        rgb_yellow = new Scalar(0xFF, 0xCC, 0x11);
        blk_r = new Mat[]{
                new Mat(48, 17, CvType.CV_8UC3, rgb_red),    // for ASCII
                new Mat(48, 33, CvType.CV_8UC3, rgb_red)     // for CJK
        }; // blk_r = new Mat[]{}
        blk_b = new Mat[]{
                new Mat(48, 17, CvType.CV_8UC3, rgb_blue),   // for ASCII
                new Mat(48, 33, CvType.CV_8UC3, rgb_blue)    // for CJK
        }; // blk_b = new Mat[]{}
        blk_g = new Mat[]{
                new Mat(48, 17, CvType.CV_8UC3, rgb_green),  // for ASCII
                new Mat(48, 33, CvType.CV_8UC3, rgb_green)   // for CJK
        }; // blk_g = new Mat[]{}
        blk_w = new Mat[]{
                new Mat(48, 17, CvType.CV_8UC3, rgb_white),  // for ASCII
                new Mat(48, 33, CvType.CV_8UC3, rgb_white)   // for CJK
        }; // blk_w = new Mat[]{}
        blk_y = new Mat[]{
                new Mat(48, 17, CvType.CV_8UC3, rgb_yellow), // for ASCII
                new Mat(48, 33, CvType.CV_8UC3, rgb_yellow)  // for CJK
        }; // blk_y = new Mat[]{}

        // Generate 54 types of cards
        table = new Card[54];
        for (i = 0; i < 54; ++i) {
            table[i] = new Card(
                    /* image   */ br[i],
                    /* darkImg */ dk[i],
                    /* color   */ Color.values()[i < 52 ? i / 13 + 1 : 0],
                    /* content */ Content.values()[i < 52 ? i % 13 : i - 39]
            ); // new Card(Mat, Mat, Color, Content)
        } // for (i = 0; i < 54; ++i)

        // Initialize other members
        players = 3;
        legality = 0;
        initialCards = 7;
        now = Uno.RNG.nextInt(4);
        forcePlay = true;
        difficulty = LV_EASY;
        replay = new StringBuilder();
        direction = draw2StackCount = 0;
        replayDir = c.getExternalFilesDir("replay");
        _2vs2 = sevenZeroRule = draw2StackRule = false;
        colorAnalysis = new int[Color.values().length];
        contentAnalysis = new int[Content.values().length];
        player = new Player[]{
                new Player(), // YOU
                new Player(), // COM1
                new Player(), // COM2
                new Player()  // COM3
        }; // player = new Player[]{}
        recent = new RecentInfo[]{
                new RecentInfo(),
                new RecentInfo(),
                new RecentInfo(),
                new RecentInfo()
        }; // recent = new RecentInfo[]
        constRecent = new RecentInfo[]{
                new RecentInfo(),
                new RecentInfo(),
                new RecentInfo(),
                new RecentInfo()
        }; // constRecent = new RecentInfo[]{}
    } // Uno(Context) (Class Constructor)

    /**
     * Fake C++ Macro
     * #define MASK_I_TO_END(i) (0xffffffffU << (i))
     */
    static int MASK_I_TO_END(int i) {
        return 0xffffffff << i;
    } // MASK_I_TO_END(int)

    /**
     * Fake C++ Macro
     * #define MASK_BEGIN_TO_I(i) (~(0xffffffffU << (i)))
     */
    static int MASK_BEGIN_TO_I(int i) {
        return ~(0xffffffff << i);
    } // MASK_BEGIN_TO_I(int)

    /**
     * Fake C++ Macro
     * #define MAKE_PUBLIC(u, p) do {                            \
     * Player* pl = (u)->getPlayer(p);                           \
     * std::sort(pl->handCards.begin(), pl->handCards.end());    \
     * pl->open = MASK_BEGIN_TO_I(pl->getHandSize());            \
     * } while (false)
     */
    static void MAKE_PUBLIC(Uno u, int p) {
        Player pl = u.getPlayer(p);
        Collections.sort(pl.handCards);
        pl.open = MASK_BEGIN_TO_I(pl.getHandSize());
    } // MAKE_PUBLIC(Uno, int)

    /**
     * In MainActivity Class, get Uno instance here.
     *
     * @param c Pass a context object to let us get the card image
     *          resources stored in this application.
     * @return Reference of our singleton.
     */
    public static Uno getInstance(Context c) {
        try {
            return new Uno(c.getApplicationContext());
        } // try
        catch (IOException e) {
            throw new AssertionError(e);
        } // catch (IOException e)
    } // getInstance(Context)

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
    } // getLevelImage(int, boolean)

    /**
     * @return 2vs2 image resource.
     */
    public Mat get2vs2Image() {
        return _2vs2 ? light2vs2 : dark2vs2;
    } // get2vs2Image()

    /**
     * @return Background image resource in current direction.
     */
    public Mat getBackground() {
        return direction == DIR_LEFT
                ? bgClockwise
                : direction == DIR_RIGHT
                ? bgCounter
                : bgWelcome;
    } // getBackground()

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
    } // getColoredWildImage(Color)

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
    } // getColoredWildDraw4Image(Color)

    /**
     * Measure the text width, using our custom font.
     * <p>
     * SPECIAL: In the text string, you can use color marks ([R], [B],
     * [G], [W] and [Y]) to control the color of the remaining text.
     * COLOR MARKS SHOULD NOT BE TREATED AS PRINTABLE CHARACTERS.
     *
     * @param text Measure which text's width.
     * @return Width of the provided text (unit: pixels).
     */
    public int getTextWidth(String text) {
        char[] txt;
        Integer position;
        int i, n, r, width;

        txt = text.toCharArray();
        for (i = width = 0, n = txt.length; i < n; ++i) {
            if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']') {
                i += 2;
            } // if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']')
            else {
                position = CHAR_MAP.get(txt[i]);
                if (position == null) position = 0x1f;
                r = position >>> 4;
                width += r < 6 ? 17 : 33;
            } // else
        } // for (i = width = 0, n = txt.length; i < n; ++i)

        return width;
    } // getTextWidth(String)

    /**
     * @see Uno#getTextWidth(String)
     * @deprecated Call getTextWidth(text) instead.
     */
    @Deprecated
    public int getFormatTextWidth(String text) {
        return getTextWidth(text);
    } // getFormatTextWidth(String)

    /**
     * Put text on image, using our custom font.
     * Unknown characters will be replaced with the question mark '?'.
     * <p>
     * SPECIAL: In the text string, you can use color marks ([R], [B],
     * [G], [W] and [Y]) to control the color of the remaining text.
     *
     * @param m    Put on which image.
     * @param text Put which text.
     * @param x    Put on where (x coordinate).
     * @param y    Put on where (y coordinate).
     */
    public void putText(Mat m, String text, int x, int y) {
        Mat mask;
        Mat[] blk;
        char[] txt;
        Integer position;
        int i, n, r, c, w;

        y -= 36;
        blk = blk_w;
        txt = text.toCharArray();
        for (i = 0, n = txt.length; i < n; ++i) {
            if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']') {
                ++i;
                if (txt[i] == 'R') blk = blk_r;
                if (txt[i] == 'B') blk = blk_b;
                if (txt[i] == 'G') blk = blk_g;
                if (txt[i] == 'W') blk = blk_w;
                if (txt[i] == 'Y') blk = blk_y;
                ++i;
            } // if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']')
            else {
                position = CHAR_MAP.get(txt[i]);
                if (position == null) position = 0x1f;
                r = position >>> 4;
                c = position & 0x0f;
                w = r < 6 ? 17 : 33;
                mask = font.submat(48 * r, 48 + 48 * r, w * c, w + w * c);
                blk[r < 6 ? 0 : 1].copyTo(m.submat(y, y + 48, x, x + w), mask);
                x += w;
            } // else
        } // for (i = 0, n = txt.length; i < n; ++i)
    } // putText(Mat, String, int, int)

    /**
     * @see Uno#putText(Mat, String, int, int)
     * @deprecated Call putText(m, text, x, y) instead.
     */
    @Deprecated
    public void putText(Mat m, String text, int x, int y, Color color) {
        if (color == Color.RED) text = "[R]" + text;
        if (color == Color.BLUE) text = "[B]" + text;
        if (color == Color.GREEN) text = "[G]" + text;
        if (color == Color.YELLOW) text = "[Y]" + text;
        putText(m, text, x, y);
    } // putText(Mat, String, int, int, Color)

    /**
     * @see Uno#putText(Mat, String, int, int)
     * @deprecated Call putText(m, text, x, y) instead.
     */
    @Deprecated
    public void putFormatText(Mat m, String text, int x, int y) {
        putText(m, text, x, y);
    } // putFormatText(Mat, String, int, int)

    /**
     * @return Player in turn. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public int getNow() {
        return now;
    } // getNow()

    /**
     * Switch to next player's turn.
     *
     * @return Player in turn after switched. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public int switchNow() {
        return (now = getNext());
    } // switchNow()

    /**
     * @return Current player's next player. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
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
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
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
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    public int getPrev() {
        int prev = (4 + now - direction) % 4;

        if (players == 3 && prev == Player.COM2) {
            prev = (4 + prev - direction) % 4;
        } // if (players == 3 && prev == Player.COM2)

        return prev;
    } // getPrev()

    /**
     * @param who Get which player's instance. Must be one of the following:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Specified player's instance.
     */
    public Player getPlayer(int who) {
        return who < Player.YOU || who > Player.COM3 ? null : player[who];
    } // getPlayer(int)

    /**
     * @return this.player[this.getNow()].
     */
    public Player getCurrPlayer() {
        return player[getNow()];
    } // getCurrPlayer()

    /**
     * @return this.player[this.getNext()].
     */
    public Player getNextPlayer() {
        return player[getNext()];
    } // getNextPlayer()

    /**
     * @return this.player[this.getOppo()].
     */
    public Player getOppoPlayer() {
        return player[getOppo()];
    } // getOppoPlayer()

    /**
     * @return this.player[this.getPrev()].
     */
    public Player getPrevPlayer() {
        return player[getPrev()];
    } // getPrevPlayer()

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
            _2vs2 = false;
            this.players = players;
        } // if (players == 3 || players == 4)
    } // setPlayers(int)

    /**
     * Switch current action sequence. The value of [direction] will be
     * switched between DIR_LEFT and DIR_RIGHT.
     */
    public void switchDirection() {
        direction = 4 - direction;
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
    } // setDifficulty(int)

    /**
     * @return Whether the 2vs2 rule is enabled. In 2vs2 mode, you win the
     * game either you or the player sitting on your opposite
     * position played all of the hand cards.
     */
    public boolean is2vs2() {
        return _2vs2;
    } // is2vs2()

    /**
     * @param enabled Enable/Disable the 2vs2 rule.
     */
    public void set2vs2(boolean enabled) {
        _2vs2 = enabled;
        if (enabled) {
            players = 4;
            sevenZeroRule = false;
        } // if (enabled)
    } // set2vs2(boolean)

    /**
     * @return This value tells that what's the next step
     * after you drew a playable card in your action.
     * When force play is enabled, play the card immediately.
     * When force play is disabled, keep the card in your hand.
     */
    public boolean isForcePlay() {
        return forcePlay;
    } // isForcePlay()

    /**
     * @param enabled Enable/Disable the force play rule.
     */
    public void setForcePlay(boolean enabled) {
        forcePlay = enabled;
    } // setForcePlay(boolean)

    /**
     * @return Whether the 7-0 rule is enabled. In 7-0 rule, when a seven card
     * is put down, the player must swap hand cards with another player
     * immediately. When a zero card is put down, everyone need to pass
     * the hand cards to the next player.
     */
    public boolean isSevenZeroRule() {
        return sevenZeroRule;
    } // isSevenZeroRule()

    /**
     * @param enabled Enable/Disable the 7-0 rule.
     */
    public void setSevenZeroRule(boolean enabled) {
        _2vs2 &= !(sevenZeroRule = enabled);
    } // setSevenZeroRule(boolean)

    /**
     * @return Can or cannot stack +2 cards. If can, when you put down a +2
     * card, the next player may transfer the punishment to its next
     * player by stacking another +2 card. Finally the first one who
     * does not stack a +2 card must draw all of the required cards.
     */
    public boolean isDraw2StackRule() {
        return draw2StackRule;
    } // isDraw2StackRule()

    /**
     * @param enabled Enable/Disable the +2 stacking rule.
     */
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
    public int getDraw2StackCount() {
        return draw2StackCount;
    } // getDraw2StackCount()

    /**
     * @return How many initial cards for everyone in each game.
     */
    public int getInitialCards() {
        return initialCards;
    } // getInitialCards()

    /**
     * Add a initial card for everyone.
     * Initial cards can be increased to 20 at most.
     */
    public void increaseInitialCards() {
        if (++initialCards > 20) {
            initialCards = 20;
        } // if (++initialCards > 20)
    } // increaseInitialCards()

    /**
     * Remove a initial card for everyone.
     * Initial cards can be decreased to 5 at least.
     */
    public void decreaseInitialCards() {
        if (--initialCards < 5) {
            initialCards = 5;
        } // if (--initialCards < 5)
    } // decreaseInitialCards()

    /**
     * Find a card instance in card table.
     *
     * @param color   Color of the card you want to get.
     * @param content Content of the card you want to get.
     * @return Corresponding card instance.
     */
    public Card findCard(Color color, Content content) {
        return color == NONE && content == WILD
                ? table[39 + WILD.ordinal()]
                : color == NONE && content == WILD_DRAW4
                ? table[39 + WILD_DRAW4.ordinal()]
                : color != NONE && content != WILD && content != WILD_DRAW4
                ? table[13 * (color.ordinal() - 1) + content.ordinal()]
                : null;
    } // findCard(Color, Content)

    /**
     * Find a card instance in card table by card ID (0 ~ 53).
     *
     * @param id ID of the card you want to get.
     * @return Corresponding card instance.
     */
    public Card findCardById(int id) {
        return 0 <= id && id <= 53 ? table[id] : null;
    } // findCardById(int)

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
        int count = used.size();

        for (int i = 0; i < 4; ++i) {
            count += recent[i].card != null ? 1 : 0;
        } // for (int i = 0; i < 4; ++i)

        return count;
    } // getUsedCount()

    /**
     * @return Recent played cards.
     * @deprecated Call getRecentInfo() instead.
     */
    @Deprecated
    public List<Card> getRecent() {
        List<Card> ret = new ArrayList<>();

        for (int i = 0; i < 4; ++i) {
            if (recent[i].card != null) {
                ret.add(recent[i].card);
            } // if (recent[i].card != null)
        } // for (int i = 0; i < 4; ++i)

        return ret;
    } // getRecent()

    /**
     * @return Colors of recent played cards.
     * @deprecated Call getRecentInfo() instead.
     */
    @Deprecated
    public List<Color> getRecentColors() {
        List<Color> ret = new ArrayList<>();

        for (int i = 0; i < 4; ++i) {
            if (recent[i].card != null) {
                ret.add(recent[i].color);
            } // if (recent[i].card != null)
        } // for (int i = 0; i < 4; ++i)

        return ret;
    } // getRecentColors()

    /**
     * @return Info of recent played cards. An array of RecentInfo objects
     * will be returned. Access getRecentInfo()[3] for the info of
     * the last played card, getRecentInfo()[2] for the info of the
     * next-to-last played card, etc.
     */
    public RecentInfo[] getRecentInfo() {
        for (int i = 0; i < 4; ++i) {
            constRecent[i].card = recent[i].card;
            constRecent[i].color = recent[i].color;
        } // for (int i = 0; i < 4; ++i)

        return constRecent;
    } // getRecentInfo()

    /**
     * @return Color of the last played card.
     */
    public Color lastColor() {
        return recent[3].color;
    } // lastColor()

    /**
     * @return Color of the next-to-last played card.
     */
    public Color next2lastColor() {
        return recent[2].color;
    } // next2lastColor()

    /**
     * Access colorAnalysis[A] to get how many cards in color A are used.
     *
     * @param A Provide the parameter A.
     * @return Value of colorAnalysis[A].
     */
    public int getColorAnalysis(Color A) {
        return colorAnalysis[A.ordinal()];
    } // getColorAnalysis(Color)

    /**
     * Access contentAnalysis[B] to get how many cards in content B are used.
     *
     * @param B Provide the parameter B.
     * @return Value of contentAnalysis[B].
     */
    public int getContentAnalysis(Content B) {
        return contentAnalysis[B.ordinal()];
    } // getContentAnalysis(Content)

    /**
     * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
     * then determine our start card.
     */
    public void start() {
        int i;
        Card card;

        // Reset direction
        direction = DIR_LEFT;

        // In +2 stack rule, reset the stack counter
        draw2StackCount = 0;

        // Clear the analysis data
        Arrays.fill(colorAnalysis, 0);
        Arrays.fill(contentAnalysis, 0);

        // Clear card deck, used card deck, recent played cards,
        // everyone's hand cards, and everyone's strong/weak colors
        deck.clear();
        used.clear();
        for (i = 0; i < 4; ++i) {
            recent[i].card = null;
            recent[i].color = NONE;
            player[i].open = 0x00;
            player[i].handCards.clear();
            player[i].weakColor = NONE;
            player[i].strongColor = NONE;
        } // for (i = 0; i < 4; ++i)

        // Generate a temporary sequenced card deck
        for (i = 0; i < 54; ++i) {
            card = table[i];
            switch (card.content) {
                case WILD:
                case WILD_DRAW4:
                    deck.add(card);
                    deck.add(card);
                    // fall through

                default:
                    deck.add(card);
                    // fall through

                case NUM0:
                    deck.add(card);
            } // switch (card.content)
        } // for (i = 0; i < 54; ++i)

        // Shuffle cards
        Collections.shuffle(deck, Uno.RNG);

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
                recent[3].card = card;
                ++colorAnalysis[card.color.ordinal()];
                ++contentAnalysis[card.content.ordinal()];
                recent[3].color = card.color;
            } // else
        } while (recent[3].card == null);

        // Write log
        Log.i(TAG, "Game starts with " + card.name);
        replay.setLength(0);
        replay.append("ST,").append(_2vs2 ? 1 : 0);
        replay.append(",").append(players);
        replay.append(",").append(card.id);

        // Let everyone draw initial cards
        for (i = 0; i < initialCards; ++i) {
            draw(Player.YOU,  /* force */ true);
            draw(Player.COM1, /* force */ true);
            if (players == 4) draw(Player.COM2, /* force */ true);
            draw(Player.COM3, /* force */ true);
        } // for (i = 0; i < initialCards; ++i)

        // In the case of (last winner = NORTH) & (game mode = 3 player mode)
        // Re-specify the dealer randomly
        if (players == 3 && now == Player.COM2) {
            now = (3 + Uno.RNG.nextInt(3)) % 4;
        } // if (players == 3 && now == Player.COM2)
    } // start()

    /**
     * Call this function when someone needs to draw a card.
     * <p>
     * NOTE: Everyone can hold 26 cards at most in this program, so even if this
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
    public int draw(int who, boolean force) {
        Card card;
        int i = -1, j;

        if (Player.YOU <= who && who <= Player.COM3) {
            List<Card> hand = player[who].handCards;

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

            if (hand.size() < MAX_HOLD_CARDS) {
                // Draw a card from card deck, and put it to an appropriate position
                card = deck.get(deck.size() - 1);
                Log.i(TAG, "Player " + who + " draw a card");
                deck.remove(deck.size() - 1);
                if (who == Player.YOU) {
                    i = Collections.binarySearch(hand, card);
                    if (i < 0) i = ~i;
                    hand.add(i, card);
                    player[who].open = (player[who].open << 1) | 0x01;
                } // if (who == Player.YOU)
                else {
                    i = hand.size();
                    hand.add(card);
                } // else

                player[who].recent = null;
                replay.append(";DR,").append(who);
                replay.append(",").append(card.id);
                if (deck.isEmpty()) {
                    // Re-use the used cards when there are no more cards in deck
                    Log.i(TAG, "Re-use the used cards");
                    for (j = used.size(); --j >= 0; ) {
                        --contentAnalysis[used.get(j).content.ordinal()];
                        --colorAnalysis[used.get(j).color.ordinal()];
                        deck.add(used.get(j));
                        used.remove(j);
                    } // for (j = used.size(); --j >= 0; )

                    Collections.shuffle(deck, Uno.RNG);
                } // if (deck.isEmpty())
            } // if (hand.size() < MAX_HOLD_CARDS)
            else {
                // In +2 stack rule, if someone cannot draw all of the required
                // cards because of the max-hold-card limitation, force reset
                // the counter to zero.
                draw2StackCount = 0;
                replay.append(";DF,").append(who);
            } // else

            if (draw2StackCount == 0) {
                // Update the legality binary when necessary
                card = recent[3].card;
                legality = card.isWild()
                        ? 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        : 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        | (0x8004002001L << card.content.ordinal());
            } // if (draw2StackCount == 0)
        } // if (Player.YOU <= who && who <= Player.COM3)

        return i;
    } // draw(int, boolean)

    /**
     * Check whether the specified card is legal to play. It's legal only when
     * it's wild, or it has the same color/content to the previous played card.
     *
     * @param card Check which card's legality.
     * @return Whether the specified card is legal to play.
     */
    public boolean isLegalToPlay(Card card) {
        return ((legality >> card.id) & 0x01L) == 0x01L;
    } // isLegalToPlay(Card)

    /**
     * @return How many legal cards (the cards that can be played legally)
     * in now player's hand.
     */
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
    public Card play(int who, int index, Color color) {
        int i, size;
        Card card = null;

        if (Player.YOU <= who && who <= Player.COM3) {
            List<Card> hand = player[who].handCards;

            size = hand.size();
            if (index < size) {
                if ((card = hand.get(index)).isWild()) {
                    String name = Card.A[color.ordinal()] + card.name;
                    Log.i(TAG, "Player " + who + " played " + name);
                } // if ((card = hand.get(index)).isWild())
                else {
                    color = card.color;
                    Log.i(TAG, "Player " + who + " played " + card.name);
                } // else

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
                else if (player[who].strongCount >= size) {
                    // Correct the value of strong counter when necessary
                    player[who].strongCount = size - 1;
                } // else if (player[who].strongCount >= size)

                if (card.content == DRAW2 && draw2StackRule) {
                    draw2StackCount += 2;
                } // if (card.content == DRAW2 && draw2StackRule)

                player[who].open = who == Player.YOU
                        ? (player[who].open >> 1)
                        : (player[who].open & MASK_BEGIN_TO_I(index))
                        | (player[who].open & MASK_I_TO_END(index + 1)) >> 1;
                player[who].recent = card;
                for (i = 0; i < 4; ++i) {
                    if (i > 0) {
                        recent[i - 1].card = recent[i].card;
                        recent[i - 1].color = recent[i].color;
                    } // if (i > 0)
                    else if (recent[i].card != null) {
                        used.add(recent[i].card);
                    } // else if (recent[i].card != null)
                } // for (i = 0; i < 4; ++i)

                recent[3].card = card;
                recent[3].color = color;
                ++colorAnalysis[card.color.ordinal()];
                ++contentAnalysis[card.content.ordinal()];
                Log.i(TAG, "colorAnalysis & contentAnalysis:");
                Log.i(TAG, Arrays.toString(colorAnalysis));
                Log.i(TAG, Arrays.toString(contentAnalysis));
                replay.append(";PL,").append(who);
                replay.append(",").append(card.id);
                replay.append(",").append(color.ordinal());

                // Update the legality binary
                legality = draw2StackCount > 0
                        ? (0x8004002001L << DRAW2.ordinal())
                        : card.isWild()
                        ? 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        : 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        | (0x8004002001L << card.content.ordinal());
                if (size == 1) {
                    // Game over, change background & show everyone's hand cards
                    direction = 0;
                    for (i = Player.COM1; i <= Player.COM3; ++i) {
                        MAKE_PUBLIC(this, i);
                    } // for (i = Player.COM1; i <= Player.COM3; ++i)

                    Log.i(TAG, "======= WINNER IS PLAYER " + who + " =======");
                } // if (size == 1)
            } // if (index < size)
        } // if (Player.YOU <= who && who <= Player.COM3)

        return card;
    } // play(int, int, Color)

    /**
     * When you think your previous player used a [wild +4] card illegally,
     * i.e. it holds at least one card matching the next-to-last color,
     * call this method to make a challenge.
     *
     * @param whom Challenge whom. Must be one of the following:
     *             Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Tell the challenge result, true if challenge success,
     * or false if challenge failure.
     */
    public boolean challenge(int whom) {
        boolean result = false;

        if (Player.YOU <= whom && whom <= Player.COM3) {
            if (whom != Player.YOU) {
                MAKE_PUBLIC(this, whom);
            } // if (whom != Player.YOU)

            for (Card card : player[whom].handCards) {
                if (card.color == next2lastColor()) {
                    result = true;
                    break;
                } // if (card.color == next2lastColor())
            } // for (Card card : player[whom].handCards)
        } // if (Player.YOU <= whom && whom <= Player.COM3)

        Log.i(TAG, "Player " + whom + " is challenged. Result = " + result);
        replay.append(";CH,").append(whom);
        return result;
    } // challenge(int)

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
    public void swap(int a, int b) {
        Player store = player[a];
        player[a] = player[b];
        player[b] = store;
        if (a == Player.YOU || b == Player.YOU) {
            MAKE_PUBLIC(this, Player.YOU);
        } // if (a == Player.YOU || b == Player.YOU)

        Log.i(TAG, "Player " + a + " swapped hand cards with Player " + b);
        replay.append(";SW,").append(a);
        replay.append(",").append(b);
    } // swap(int, int)

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass the hand
     * cards to the next player.
     */
    public void cycle() {
        int curr = now, next = getNext(), oppo = getOppo(), prev = getPrev();
        Player store = player[curr];
        player[curr] = player[prev];
        player[prev] = player[oppo];
        player[oppo] = player[next];
        player[next] = store;
        MAKE_PUBLIC(this, Player.YOU);
        Log.i(TAG, "Everyone passed hand cards to the next player");
        replay.append(";CY");
    } // cycle()

    /**
     * Save current game as a new replay file.
     *
     * @return Name of the saved file, or an empty string if save failed.
     */
    public String save() {
        File f;
        Writer w;
        String name = "";

        try {
            f = new File(replayDir, System.currentTimeMillis() + ".sav");
            w = new FileWriter(f);
            w.write(replay.toString());
            w.close();
            name = f.getName();
        } // try
        catch (IOException ignore) {
        } // catch (IOException ignore)

        return name;
    } // save()

    /**
     * @param val Provide a value (in string format, 10 based).
     * @param lo  Provide a lower bound.
     * @param hi  Provide a upper bound.
     * @return True if lo <= val <= hi.
     */
    boolean checkParam(String val, int lo, int hi) {
        int iVal;

        try {
            iVal = Integer.parseInt(val);
        } // try
        catch (NumberFormatException e) {
            iVal = 0;
        } // catch (NumberFormatException e)

        return lo <= iVal && iVal <= hi;
    } // checkParam(String, int, int)

    /**
     * Load a existed replay file.
     *
     * @param replayName Provide the file name of your replay.
     * @return True if load success.
     */
    public boolean loadReplay(String replayName) {
        int i;
        File f;
        boolean ok;
        FileReader r;
        StringBuilder sb;
        String[] loaded = null;

        f = new File(replayDir, replayName);
        ok = f.canRead();
        if (ok) try {
            r = new FileReader(f);
            sb = new StringBuilder();
            for (i = r.read(); i >= 0; i = r.read()) {
                sb.append((char) i);
            } // for (i = r.read(); i >= 0; i = r.read())

            loaded = sb.toString().split(";");
            r.close();
        } // if (ok) try
        catch (IOException e) {
            ok = false;
        } // catch (IOException e)

        for (i = 0; ok && i < loaded.length; ++i) {
            String[] x = loaded[i].split(",");

            if (x.length == 0 ||
                    (i == 0 && !x[0].equals("ST")) ||
                    (i != 0 && x[0].equals("ST"))) {
                // Empty command is forbidden
                // ST command can only appear once at the beginning
                ok = false;
            } // if (x.length == 0 || ...)
            else if (x[0].equals("ST")) {
                // ST: Start a new game
                // Command format: ST,a,b,c
                // a = 1 if in 2vs2 mode, otherwise 0
                // b = players in game [3, 4]
                // c = start card's id [0, 51]
                ok = x.length > 3
                        && checkParam(x[1], 0, 1)
                        && checkParam(x[2], 3, 4)
                        && checkParam(x[3], 0, 51);
            } // else if (x[0].equals("ST"))
            else if (x[0].equals("DR")) {
                // DR: Draw a card from deck
                // Command format: DR,a,b
                // a = who drew a card [0, 3]
                // b = drawn card's id [0, 53]
                ok = x.length > 2
                        && checkParam(x[1], 0, 3)
                        && checkParam(x[2], 0, 53);
            } // else if (x[0].equals("DR"))
            else if (x[0].equals("PL")) {
                // PL: Play a card
                // Command format: PL,a,b,c
                // a = who played a card [0, 3]
                // b = played card's id [0, 53]
                // c = the following legal color [0, 4]
                ok = x.length > 3
                        && checkParam(x[1], 0, 3)
                        && checkParam(x[2], 0, 53)
                        && checkParam(x[3], 0, 4);
            } // else if (x[0].equals("PL"))
            else if (x[0].equals("DF") || x[0].equals("CH")) {
                // DF: Draw but failure
                // Command format: DF,a
                // a = who drew but failure [0, 3]
                // CH: Make a challenge
                // Command format: CH,a
                // a = challenged to whom [0, 3]
                ok = x.length > 1
                        && checkParam(x[1], 0, 3);
            } // else if (x[0].equals("DF") || x[0].equals("CH"))
            else if (x[0].equals("SW")) {
                // SW: Swap hand cards between player A and B
                // Command format: SW,a,b
                // a = player A's id [0, 3]
                // b = player B's id [0, 3]
                ok = x.length > 2
                        && checkParam(x[1], 0, 3)
                        && checkParam(x[2], 0, 3)
                        && !x[1].equals(x[2]);
            } // else if (x[0].equals("SW"))
            else if (!x[0].equals("CY")) {
                // CY: Cycle, everyone pass hand cards to the next
                // Command format: CY
                // Other commands are all unknown commands
                ok = false;
            } // else if (!x[0].equals("CY"))
        } // for (i = 0; ok && i < loaded.length; ++i)

        it = ok ? Arrays.asList(loaded).iterator() : null;
        return ok;
    } // loadReplay(String)

    /**
     * Go to next step of your replay.
     * This method will do nothing when we reached the end of your replay.
     *
     * @param out This is a out parameter. Pass an int array (length>=3)
     *            in order to let us pass the return values by assigning
     *            out[0~2]. For a command "COM,a,b,c", out[0] will become
     *            the value of a, out[1] will become the value of b, and
     *            out[2] will become the value of c.
     * @return Name of current command, e.g. "ST".
     * When end of replay reached, return an empty string.
     */
    public String forwardReplay(int[] out) {
        Card card;
        String s = "";
        int a, b, c, i;

        if (out == null || out.length < 3) {
            throw new IllegalArgumentException("out == null || out.length < 3");
        } // if (out == null || out.length < 3)

        if (it != null && it.hasNext()) {
            String[] x = it.next().split(",");

            a = out[0] = x.length > 1 ? Integer.parseInt(x[1]) : 0;
            b = out[1] = x.length > 2 ? Integer.parseInt(x[2]) : 0;
            c = out[2] = x.length > 3 ? Integer.parseInt(x[3]) : 0;
            if ((s = x[0]).equals("ST")) {
                // ST: Start a new game
                // Command format: ST,a,b,c
                // a = 1 if in 2vs2 mode, otherwise 0
                // b = players in game [3, 4]
                // c = start card's id [0, 51]
                setPlayers(b);
                set2vs2(a != 0);
                card = table[c];
                deck.clear();
                used.clear();
                for (i = 0; i < 4; ++i) {
                    recent[i].card = null;
                    recent[i].color = NONE;
                    player[i].open = 0x00;
                    player[i].handCards.clear();
                    player[i].weakColor = NONE;
                    player[i].strongColor = NONE;
                } // for (i = 0; i < 4; ++i)

                recent[3].card = card;
                recent[3].color = card.color;
                direction = card.content == REV ? DIR_RIGHT : DIR_LEFT;
            } // if ((s = x[0]).equals("ST"))
            else if (s.equals("DR")) {
                // DR: Draw a card from deck
                // Command format: DR,a,b
                // a = who drew a card [0, 3]
                // b = drawn card's id [0, 53]
                List<Card> hand = player[a].handCards;

                card = table[b];
                i = Collections.binarySearch(hand, card);
                hand.add(i < 0 ? ~i : i, card);
                player[a].open = (player[a].open << 1) | 0x01;
                now = a;
            } // else if (s.equals("DR"))
            else if (s.equals("PL")) {
                // PL: Play a card
                // Command format: PL,a,b,c
                // a = who played a card [0, 3]
                // b = played card's id [0, 53]
                // c = the following legal color [0, 4]
                List<Card> hand = player[a].handCards;

                card = table[b];
                i = Collections.binarySearch(hand, card);
                if (i >= 0) {
                    hand.remove(i);
                    player[a].open = player[a].open >> 1;
                    for (i = 1; i < 4; ++i) {
                        recent[i - 1].card = recent[i].card;
                        recent[i - 1].color = recent[i].color;
                    } // for (i = 1; i < 4; ++i)

                    recent[3].card = card;
                    recent[3].color = Color.values()[c];
                } // if (i >= 0)

                now = a;
                if (card.content == REV) {
                    switchDirection();
                } // if (card.content == REV)
            } // else if (s.equals("PL"))
            else if (s.equals("DF")) {
                // DF: Draw but failure
                // Command format: DF,a
                // a = who drew but failure [0, 3]
                now = a;
            } // else if (s.equals("DF"))
            else if (s.equals("SW")) {
                // SW: Swap hand cards between player A and B
                // Command format: SW,a,b
                // a = player A's id [0, 3]
                // b = player B's id [0, 3]
                Player store = player[a];
                player[a] = player[b];
                player[b] = store;
            } // else if (s.equals("SW"))
            else if (s.equals("CY")) {
                // CY: Cycle, everyone pass hand cards to the next
                // Command format: CY
                int curr = now, next = getNext();
                int oppo = getOppo(), prev = getPrev();
                Player store = player[curr];
                player[curr] = player[prev];
                player[prev] = player[oppo];
                player[oppo] = player[next];
                player[next] = store;
            } // else if (s.equals("CY"))
        } // if (it != null && it.hasNext())
        else {
            direction = 0;
        } // else

        return s;
    } // forwardReplay(int[])

    /**
     * RecentInfo Inner Class.
     */
    public static class RecentInfo {
        public Card card = null;
        public Color color = NONE;
    } // RecentInfo Inner Class
} // Uno Class

// E.O.F