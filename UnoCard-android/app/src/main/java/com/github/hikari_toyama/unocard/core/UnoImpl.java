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
import static com.github.hikari_toyama.unocard.core.Content.WILD;
import static com.github.hikari_toyama.unocard.core.Content.WILD_DRAW4;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.github.hikari_toyama.unocard.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Uno Runtime Class (Singleton).
 */
class UnoImpl extends Uno {
    /**
     * Tag name for Android Logcat.
     */
    private static final String TAG = "Uno";

    /**
     * Our custom font's character-to-position map.
     */
    private static final Map<Character, Integer> CHAR_MAP = new TreeMap<>();

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
     * Recent played cards (read-only version, provide for external accesses).
     */
    private final List<Card> recent_readOnly
            = Collections.unmodifiableList(recent);

    /**
     * Colors of recent played cards
     * (read-only version, provide for external accesses).
     */
    private final List<Color> recentColors_readOnly
            = Collections.unmodifiableList(recentColors);

    /**
     * Singleton, hide default constructor.
     *
     * @param context Pass a context object to let us get the card image
     *                resources stored in this application.
     * @throws IOException Thrown if failed to load image resources.
     */
    UnoImpl(Context context) throws IOException {
        Mat[] br, dk;
        int i, loaded, total;

        // Preparations
        context = context.getApplicationContext();
        loaded = 0;
        total = 128;
        Log.i(TAG, "Loading... (0%)");

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
                Utils.loadResource(context, R.raw.front_rr),
                Utils.loadResource(context, R.raw.front_rs),
                Utils.loadResource(context, R.raw.front_rd2),
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
                Utils.loadResource(context, R.raw.front_br),
                Utils.loadResource(context, R.raw.front_bs),
                Utils.loadResource(context, R.raw.front_bd2),
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
                Utils.loadResource(context, R.raw.front_gr),
                Utils.loadResource(context, R.raw.front_gs),
                Utils.loadResource(context, R.raw.front_gd2),
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
                Utils.loadResource(context, R.raw.front_yr),
                Utils.loadResource(context, R.raw.front_ys),
                Utils.loadResource(context, R.raw.front_yd2),
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
                Utils.loadResource(context, R.raw.dark_rr),
                Utils.loadResource(context, R.raw.dark_rs),
                Utils.loadResource(context, R.raw.dark_rd2),
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
                Utils.loadResource(context, R.raw.dark_br),
                Utils.loadResource(context, R.raw.dark_bs),
                Utils.loadResource(context, R.raw.dark_bd2),
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
                Utils.loadResource(context, R.raw.dark_gr),
                Utils.loadResource(context, R.raw.dark_gs),
                Utils.loadResource(context, R.raw.dark_gd2),
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
                Utils.loadResource(context, R.raw.dark_yr),
                Utils.loadResource(context, R.raw.dark_ys),
                Utils.loadResource(context, R.raw.dark_yd2),
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

        // Load font images
        fontR = Utils.loadResource(context, R.raw.font_r);
        fontG = Utils.loadResource(context, R.raw.font_g);
        fontW = Utils.loadResource(context, R.raw.font_w);
        fontY = Utils.loadResource(context, R.raw.font_y);
        Imgproc.cvtColor(fontR, fontR, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(fontG, fontG, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(fontW, fontW, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(fontY, fontY, Imgproc.COLOR_BGR2RGB);
        loaded += 4;
        Log.i(TAG, "Loading... (" + 100 * loaded / total + "%)");

        // Generate 54 types of cards
        table = new Card[54];
        for (i = 0; i < 54; ++i) {
            table[i] = new CardImpl(
                    /* image   */ br[i],
                    /* darkImg */ dk[i],
                    /* color   */ Color.values()[i < 52 ? i / 13 + 1 : 0],
                    /* content */ Content.values()[i < 52 ? i % 13 : i - 39]
            ); // new CardImpl(Mat, Mat, Color, Content)
        } // for (i = 0; i < 54; ++i)

        // Initialize other members
        players = 3;
        legality = 0;
        now = Uno.RNG.nextInt(4);
        forcePlay = true;
        difficulty = LV_EASY;
        direction = draw2StackCount = 0;
        sevenZeroRule = draw2StackRule = false;
        colorAnalysis = new int[Color.values().length];
        contentAnalysis = new int[Content.values().length];
        player = new Player[]{
                new PlayerImpl(), // YOU
                new PlayerImpl(), // COM1
                new PlayerImpl(), // COM2
                new PlayerImpl()  // COM3
        }; // player = new Player[]{}
    } // UnoImpl(Context) (Class Constructor)

    /**
     * Fake C++ Macro
     * #define MASK_I_TO_END(i) (0xffffffffU << (i))
     */
    private static int MASK_I_TO_END(int i) {
        return 0xffffffff << i;
    } // MASK_I_TO_END(int)

    /**
     * Fake C++ Macro
     * #define MASK_BEGIN_TO_I(i) (~(0xffffffffU << (i)))
     */
    private static int MASK_BEGIN_TO_I(int i) {
        return ~(0xffffffff << i);
    } // MASK_BEGIN_TO_I(int)

    /**
     * Fake C++ Macro
     * #define MASK_ALL(u, p) MASK_BEGIN_TO_I((u)->getPlayer(p)->getHandSize())
     */
    private static int MASK_ALL(Uno u, int p) {
        return MASK_BEGIN_TO_I(u.getPlayer(p).getHandSize());
    } // MASK_ALL(Uno, int)

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
     * Measure the text width, using our custom font.
     *
     * @param text Measure which text's width.
     * @return Width of the provided text (unit: pixels).
     */
    @Override
    public int getTextWidth(String text) {
        int r, width;
        Integer position;

        width = 0;
        for (char ch : text.toCharArray()) {
            position = CHAR_MAP.get(ch);
            if (position == null) position = 0x1f;
            r = position >>> 4;
            width += r < 6 ? 17 : 33;
        } // for (char ch : text.toCharArray())

        return width;
    } // getTextWidth(String)

    /**
     * Put text on image, using our custom font.
     * Unknown characters will be replaced with the question mark '?'.
     *
     * @param m     Put on which image.
     * @param text  Put which text.
     * @param x     Put on where (x coordinate).
     * @param y     Put on where (y coordinate).
     * @param color Text color. Must be one of the following:
     *              Color.RED, Color.GREEN, Color.YELLOW, null (as white).
     */
    @Override
    public void putText(Mat m, String text, int x, int y, Color color) {
        int r, c, width;
        Rect roi1, roi2;
        Integer position;
        Mat font, cimg, mask;

        y -= 36;
        font = color == Color.RED ? fontR
                : color == Color.GREEN ? fontG
                : color == Color.YELLOW ? fontY : fontW;
        for (char ch : text.toCharArray()) {
            position = CHAR_MAP.get(ch);
            if (position == null) position = 0x1f;
            r = position >>> 4;
            c = position & 0x0f;
            width = r < 6 ? 17 : 33;
            roi1 = new Rect(width * c, 48 * r, width, 48);
            cimg = new Mat(font, roi1);
            mask = new Mat(fontW, roi1);
            roi2 = new Rect(x, y, width, 48);
            cimg.copyTo(new Mat(m, roi2), mask);
            x += width;
        } // for (char ch : text.toCharArray())
    } // putText(Mat, String, int, int, Color)

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
     * @param who Get which player's instance. Must be one of the following:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Specified player's instance.
     */
    @Override
    public Player getPlayer(int who) {
        return who < Player.YOU || who > Player.COM3 ? null : player[who];
    } // getPlayer(int)

    /**
     * @return this.player[this.getNow()].
     */
    @Override
    public Player getCurrPlayer() {
        return player[getNow()];
    } // getCurrPlayer()

    /**
     * @return this.player[this.getNext()].
     */
    @Override
    public Player getNextPlayer() {
        return player[getNext()];
    } // getNextPlayer()

    /**
     * @return this.player[this.getOppo()].
     */
    @Override
    public Player getOppoPlayer() {
        return player[getOppo()];
    } // getOppoPlayer()

    /**
     * @return this.player[this.getPrev()].
     */
    @Override
    public Player getPrevPlayer() {
        return player[getPrev()];
    } // getPrevPlayer()

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
     * Switch current action sequence. The value of [direction] will be
     * switched between DIR_LEFT and DIR_RIGHT.
     */
    @Override
    public void switchDirection() {
        direction = 4 - direction;
    } // switchDirection()

    /**
     * /**
     *
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
     * @return This value tells that what's the next step
     * after you drew a playable card in your action.
     * When force play is enabled, play the card immediately.
     * When force play is disabled, keep the card in your hand.
     */
    @Override
    public boolean isForcePlay() {
        return forcePlay;
    } // isForcePlay()

    /**
     * @param enabled Enable/Disable the force play rule.
     */
    @Override
    public void setForcePlay(boolean enabled) {
        forcePlay = enabled;
    } // setForcePlay(boolean)

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
        return color == NONE && content == WILD
                ? table[39 + WILD.ordinal()]
                : color == NONE && content == WILD_DRAW4
                ? table[39 + WILD_DRAW4.ordinal()]
                : color != NONE && content != WILD && content != WILD_DRAW4
                ? table[13 * (color.ordinal() - 1) + content.ordinal()]
                : null;
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
     * Access colorAnalysis[A] to get how many cards in color A are used.
     *
     * @param A Provide the parameter A.
     * @return Value of colorAnalysis[A].
     */
    @Override
    public int getColorAnalysis(Color A) {
        return colorAnalysis[A.ordinal()];
    } // getColorAnalysis(Color)

    /**
     * Access contentAnalysis[B] to get how many cards in content B are used.
     *
     * @param B Provide the parameter B.
     * @return Value of contentAnalysis[B].
     */
    @Override
    public int getContentAnalysis(Content B) {
        return contentAnalysis[B.ordinal()];
    } // getContentAnalysis(Content)

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

        // Clear the analysis data
        Arrays.fill(colorAnalysis, 0);
        Arrays.fill(contentAnalysis, 0);

        // Clear card deck, used card deck, recent played cards,
        // everyone's hand cards, and everyone's strong/weak colors
        deck.clear();
        used.clear();
        recent.clear();
        recentColors.clear();
        for (i = Player.YOU; i <= Player.COM3; ++i) {
            player[i].open = 0x00;
            player[i].handCards.clear();
            player[i].weakColor = NONE;
            player[i].strongColor = NONE;
        } // for (i = Player.YOU; i <= Player.COM3; ++i)

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
        size = deck.size();
        while (size > 0) {
            i = Uno.RNG.nextInt(size--);
            card = deck.get(i);
            deck.set(i, deck.get(size));
            deck.set(size, card);
        } // while (size > 0)

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
                ++colorAnalysis[card.color.ordinal()];
                ++contentAnalysis[card.content.ordinal()];
                recentColors.add(card.color);
            } // else
        } while (recent.isEmpty());

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

        // Update the legality binary
        legality = 0x30000000000000L
                | (0x1fffL << 13 * (card.color.ordinal() - 1))
                | (0x8004002001L << card.content.ordinal());

        // In the case of (last winner = NORTH) & (game mode = 3 player mode)
        // Re-specify the dealer randomly
        if (players == 3 && now == Player.COM2) {
            now = (3 + Uno.RNG.nextInt(3)) % 4;
        } // if (players == 3 && now == Player.COM2)

        // Write log
        Log.i(TAG, "Game starts with " + card.name);
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
                Log.i(TAG, "Player " + who + " draw a card");
                card = deck.get(deck.size() - 1);
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
                if (deck.isEmpty()) {
                    // Re-use the used cards when there are no more cards in deck
                    Log.i(TAG, "Re-use the used cards");
                    size = used.size();
                    while (size > 0) {
                        index = Uno.RNG.nextInt(size--);
                        --contentAnalysis[used.get(index).content.ordinal()];
                        --colorAnalysis[used.get(index).color.ordinal()];
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

            if (draw2StackCount == 0) {
                // Update the legality binary when necessary
                card = recent.get(recent.size() - 1);
                legality = card.isWild()
                        ? 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        : 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        | (0x8004002001L << card.content.ordinal());
            } // if (draw2StackCount == 0)
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
        return ((legality >> card.id) & 0x01L) == 0x01L;
    } // isLegalToPlay(Card)

    /**
     * @return How many legal cards (the cards that can be played legally)
     * in now player's hand.
     */
    @Override
    public int legalCardsCount4NowPlayer() {
        int count = 0;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            for (Card card : player[now].handCards) {
                if (isLegalToPlay(card)) {
                    ++count;
                } // if (isLegalToPlay(card))
            } // for (Card card : player[now].handCards)
        } // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        else {
            count += player[now].handCards.stream()
                    .filter(this::isLegalToPlay).count();
        } // else

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
                Log.i(TAG, "Player " + who + " played " + card.name);
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
                recent.add(card);
                ++colorAnalysis[card.color.ordinal()];
                ++contentAnalysis[card.content.ordinal()];
                printAnalysisData();
                recentColors.add(card.isWild() ? color : card.color);
                if (recent.size() > 5) {
                    used.add(recent.get(0));
                    recent.remove(0);
                    recentColors.remove(0);
                } // if (recent.size() > 5)

                // Update the legality binary
                legality = draw2StackCount > 0
                        ? (0x8004002001L << DRAW2.ordinal())
                        : card.isWild()
                        ? 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        : 0x30000000000000L
                        | (0x1fffL << 13 * (lastColor().ordinal() - 1))
                        | (0x8004002001L << card.content.ordinal());
                if (hand.size() == 0) {
                    // Game over, change background & show everyone's hand cards
                    direction = 0;
                    for (int i = Player.COM1; i <= Player.COM3; ++i) {
                        player[i].sort();
                        player[i].open = MASK_ALL(this, i);
                    } // for (int i = Player.COM1; i <= Player.COM3; ++i)

                    Log.i(TAG, "======= WINNER IS PLAYER " + who + " =======");
                } // if (hand.size() == 0)
            } // if (index < size)
        } // if (who >= Player.YOU && who <= Player.COM3)

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
    @Override
    public boolean challenge(int whom) {
        boolean result = false;

        if (whom >= Player.YOU && whom <= Player.COM3) {
            if (whom != Player.YOU) {
                player[whom].sort();
                player[whom].open = MASK_ALL(this, whom);
            } // if (whom != Player.YOU)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                for (Card card : player[whom].handCards) {
                    if (card.color == next2lastColor()) {
                        result = true;
                        break;
                    } // if (card.color == next2lastColor())
                } // for (Card card : player[whom].handCards)
            } // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            else {
                result = player[whom].handCards.stream()
                        .anyMatch(card -> card.color == next2lastColor());
            } // else
        } // if (whom >= Player.YOU && whom <= Player.COM3)

        Log.i(TAG, "Player " + whom + " is challenged. Result = " + result);
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
    @Override
    public void swap(int a, int b) {
        Player store = player[a];
        player[a] = player[b];
        player[b] = store;
        if (a == Player.YOU || b == Player.YOU) {
            player[Player.YOU].sort();
            player[Player.YOU].open = MASK_ALL(this, Player.YOU);
        } // if (a == Player.YOU || b == Player.YOU)

        Log.i(TAG, "Player " + a + " swapped hand cards with Player " + b);
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
        player[Player.YOU].sort();
        player[Player.YOU].open = MASK_ALL(this, Player.YOU);
        Log.i(TAG, "Everyone passed hand cards to the next player");
    } // cycle()

    /**
     * Print the content of the colorAnalysis array and the contentAnalysis
     * array.
     */
    @Override
    public void printAnalysisData() {
        Log.i(TAG, "colorAnalysis = " + Arrays.toString(colorAnalysis));
        Log.i(TAG, "contentAnalysis = " + Arrays.toString(contentAnalysis));
    } // printAnalysisData()
} // UnoImpl Class

// E.O.F