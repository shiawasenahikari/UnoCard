////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;
import androidx.fragment.app.DialogFragment;

import com.github.hikari_toyama.unocard.core.AI;
import com.github.hikari_toyama.unocard.core.Card;
import com.github.hikari_toyama.unocard.core.Color;
import com.github.hikari_toyama.unocard.core.Content;
import com.github.hikari_toyama.unocard.core.I18N;
import com.github.hikari_toyama.unocard.core.Player;
import com.github.hikari_toyama.unocard.core.Uno;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * AnimateLayer Class.
 */
class AnimateLayer {
    public Mat elem;
    public int startLeft, startTop, endLeft, endTop;
} // AnimateLayer Class

/**
 * Game UI.
 */
@SuppressWarnings("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity
        implements Handler.Callback, Runnable, View.OnTouchListener {
    private static final boolean OPENCV_INIT_SUCCESS = OpenCVLoader.initDebug();
    private static final Scalar RGB_YELLOW = new Scalar(0xFF, 0xAA, 0x11);
    private static final Scalar RGB_GREEN = new Scalar(0x55, 0xAA, 0x55);
    private static final Scalar RGB_BLUE = new Scalar(0x55, 0x55, 0xFF);
    private static final Scalar RGB_RED = new Scalar(0xFF, 0x55, 0x55);
    private static final int STAT_ASK_KEEP_PLAY = 0x8888;
    private static final int STAT_SEVEN_TARGET = 0x7777;
    private static final int STAT_DOUBT_WILD4 = 0x6666;
    private static final int STAT_WILD_COLOR = 0x5555;
    private static final int STAT_GAME_OVER = 0x4444;
    private static final int STAT_NEW_GAME = 0x3333;
    private static final int STAT_WELCOME = 0x2222;
    private static final int STAT_IDLE = 0x1111;
    private MediaPlayer mMediaPlayer;
    private boolean mAdjustOptions;
    private AnimateLayer[] mLayer;
    private SoundPool mSoundPool;
    private ImageView mImgScreen;
    private Handler mSubHandler;
    private Handler mUIHandler;
    private Color[] mBestColor;
    private boolean mGameSaved;
    private boolean mAIRunning;
    private int mScore, mDiff;
    private int mSelectedIdx;
    private boolean mAuto;
    private float mSndVol;
    private float mBgmVol;
    private Mat[] mBackup;
    private int mHideFlag;
    private int mStatus;
    private int mWinner;
    private Bitmap mBmp;
    private int sndPlay;
    private int sndDraw;
    private int sndLose;
    private int sndWin;
    private int sndUno;
    private int mSpeed;
    private Paint mPen;
    private I18N i18n;
    private Mat mScr;
    private Uno mUno;
    private AI mAI;

    /**
     * Activity initialization.
     */
    @Override
    @UiThread
    protected void onCreate(Bundle savedInstanceState) {
        int initialCards;
        SharedPreferences sp;
        DialogFragment dialog;

        // Preparations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUIHandler = new Handler(Looper.getMainLooper(), this);
        if (OPENCV_INIT_SUCCESS) {
            if (Locale.getDefault().toString().contains("zh")) {
                i18n = I18N.ZH_CN;
            } // if (Locale.getDefault().toString().contains("zh"))
            else if (Locale.getDefault().toString().contains("ja")) {
                i18n = I18N.JA_JP;
            } // else if (Locale.getDefault().toString().contains("ja"))
            else {
                i18n = I18N.EN_US;
            } // else

            sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
            mScore = sp.getInt("score", 0);
            mBgmVol = sp.getFloat("bgmVol", 0.5f);
            mUno = Uno.getInstance(this);
            mUno.setPlayers(sp.getInt("players", 3));
            mUno.setDifficulty(sp.getInt("difficulty", Uno.LV_EASY));
            mUno.setForcePlayRule(sp.getInt("forcePlayRule", 1));
            mUno.setSevenZeroRule(sp.getBoolean("sevenZero", false));
            mUno.setStackRule(sp.getInt("stackRule", 0));
            mUno.set2vs2(sp.getBoolean("2vs2", false));
            mSpeed = MathUtils.clamp(sp.getInt("speed", 1), 1, 3);
            initialCards = MathUtils.clamp(sp.getInt("initialCards", 7), 5, 20);
            while (mUno.getInitialCards() < initialCards) {
                mUno.increaseInitialCards();
            } // while (mUno.getInitialCards() < initialCards)

            while (mUno.getInitialCards() > initialCards) {
                mUno.decreaseInitialCards();
            } // while (mUno.getInitialCards() > initialCards)

            mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            sndUno = mSoundPool.load(this, R.raw.snd_uno, 1);
            sndWin = mSoundPool.load(this, R.raw.snd_win, 1);
            sndLose = mSoundPool.load(this, R.raw.snd_lose, 1);
            sndPlay = mSoundPool.load(this, R.raw.snd_play, 1);
            sndDraw = mSoundPool.load(this, R.raw.snd_draw, 1);
            mMediaPlayer = MediaPlayer.create(this, R.raw.bgm);
            mMediaPlayer.setVolume(mBgmVol, mBgmVol);
            mMediaPlayer.setLooping(true);
            mAI = new AI(mUno);
            mBestColor = new Color[1];
            mAdjustOptions = false;
            mWinner = Player.YOU;
            mAIRunning = false;
            mSelectedIdx = -1;
            mHideFlag = 0x00;
            mAuto = false;
            mScr = Mat.zeros(900, 1600, CvType.CV_8UC4);
            mLayer = new AnimateLayer[]{
                    new AnimateLayer(),
                    new AnimateLayer(),
                    new AnimateLayer(),
                    new AnimateLayer()
            }; // new AnimateLayer[4]
            mBackup = new Mat[]{
                    Mat.zeros(900, 1600, CvType.CV_8UC4),
                    Mat.zeros(900, 1600, CvType.CV_8UC4),
                    Mat.zeros(900, 1600, CvType.CV_8UC4),
                    Mat.zeros(900, 1600, CvType.CV_8UC4)
            }; // new Mat[]{}
            mPen = new Paint();
            mPen.setTextSize(34);
            mPen.setTypeface(Typeface.createFromAsset(getAssets(), "noto.ttc"));
            mBmp = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888);
            mImgScreen = findViewById(R.id.imgMainScreen);
            new Thread(this).start(); // -> run()
            mImgScreen.setOnTouchListener(this);
        } // if (OPENCV_INIT_SUCCESS)
        else {
            dialog = new UnsupportedDeviceDialog();
            dialog.show(getSupportFragmentManager(), "UnsupportedDeviceDialog");
        } // else
    } // onCreate(Bundle)

    /**
     * Triggered when activity gets focus.
     */
    @Override
    @UiThread
    protected void onResume() {
        SharedPreferences sp;

        super.onResume();
        if (OPENCV_INIT_SUCCESS) {
            sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
            mSndVol = sp.getFloat("sndVol", 0.5f);
            mMediaPlayer.start();
        } // if (OPENCV_INIT_SUCCESS)
    } // onResume()

    /**
     * Entry of the sub thread. Do consuming operations here.
     */
    @Override
    @WorkerThread
    public void run() {
        Looper.prepare();
        mSubHandler = new Handler(this::handleMessage2);
        setStatus(STAT_WELCOME);
        Looper.loop();
    } // run()

    /**
     * Let our UI wait the number of specified milli seconds.
     *
     * @param millis How many milli seconds to wait.
     */
    @WorkerThread
    private void threadWait(long millis) {
        long end = System.currentTimeMillis() + millis / mSpeed;

        while (System.currentTimeMillis() < end) {
            mSubHandler.removeCallbacksAndMessages(null);
        } // while (System.currentTimeMillis() < end)
    } // threadWait(long)

    /**
     * The unique AI entry point.
     */
    @WorkerThread
    private void requestAI() {
        int idxBest;

        if (!mAIRunning) {
            mAIRunning = true;
            while ((mStatus == Player.YOU && mAuto) ||
                    mStatus == Player.COM1 ||
                    mStatus == Player.COM2 ||
                    mStatus == Player.COM3) {
                setStatus(STAT_IDLE); // block tap down events when idle
                idxBest = mUno.getDifficulty() == Uno.LV_EASY
                        ? mAI.easyAI_bestCardIndex4NowPlayer(mBestColor)
                        : mUno.isSevenZeroRule()
                        ? mAI.sevenZeroAI_bestCardIndex4NowPlayer(mBestColor)
                        : mUno.is2vs2()
                        ? mAI.teamAI_bestCardIndex4NowPlayer(mBestColor)
                        : mAI.hardAI_bestCardIndex4NowPlayer(mBestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, mBestColor[0]);
                } // if (idxBest >= 0)
                else {
                    // No appropriate cards to play, or no card to play
                    draw(1, /* force */ false);
                } // else
            } // while ((mStatus == Player.YOU && mAuto) || ...)

            mAIRunning = false;
        } // if (!mAIRunning)
    } // requestAI()

    /**
     * Change the value of global variable [mStatus]
     * and do the following operations when necessary.
     *
     * @param status New status value.
     */
    @WorkerThread
    private void setStatus(int status) {
        switch (mStatus = status) {
            case STAT_WELCOME:
                if (mAdjustOptions) {
                    refreshScreen(i18n.info_ruleSettings());
                } // if (mAdjustOptions)
                else {
                    refreshScreen(i18n.info_welcome());
                } // else
                break; // case STAT_WELCOME

            case STAT_NEW_GAME:
                // New game
                mStatus = STAT_IDLE; // block tap down events when idle

                // You will lose 200 points if you quit during the game
                mScore -= 200;
                mUno.start();
                mSelectedIdx = -1;
                refreshScreen(i18n.info_ready());
                threadWait(2000);
                switch (mUno.getRecentInfo()[3].card.content) {
                    case DRAW2:
                        // If starting with a [+2], let dealer draw 2 cards.
                        draw(2, /* force */ true);
                        break; // case DRAW2

                    case SKIP:
                        // If starting with a [skip], skip dealer's turn.
                        refreshScreen(i18n.info_skipped(mUno.getNow()), 0x00);
                        threadWait(1500);
                        setStatus(mUno.switchNow());
                        break; // case SKIP

                    case REV:
                        // If starting with a [reverse], change the action
                        // sequence to COUNTER CLOCKWISE.
                        mUno.switchDirection();
                        refreshScreen(i18n.info_dirChanged());
                        threadWait(1500);
                        setStatus(mUno.getNow());
                        break; // case REV

                    default:
                        // Otherwise, go to dealer's turn.
                        setStatus(mUno.getNow());
                        break; // default
                } // switch (mUno.getRecentInfo()[3].card.content)
                break; // case STAT_NEW_GAME

            case Player.YOU:
                // Your turn, select a hand card to play, or draw a card
                if (mAuto) {
                    requestAI();
                } // if (mAuto)
                else if (mAdjustOptions) {
                    refreshScreen("");
                } // else if (mAdjustOptions)
                else if (mUno.legalCardsCount4NowPlayer() == 0) {
                    draw(1, /* force */ false);
                } // else if (mUno.legalCardsCount4NowPlayer() == 0)
                else if (mUno.getHandCardsOf(Player.YOU).size() == 1) {
                    play(0, Color.NONE);
                } // else if (mUno.getHandCardsOf(Player.YOU).size() == 1)
                else if (mSelectedIdx < 0) {
                    int c = mUno.getDraw2StackCount();

                    refreshScreen(c == 0
                            ? i18n.info_yourTurn()
                            : mUno.getStackRule() != 2
                            ? i18n.info_yourTurn_stackDraw2(c)
                            : i18n.info_yourTurn_stackDraw2(c, 2));
                } // else if (mSelectedIdx < 0)
                else {
                    List<Card> hand = mUno.getHandCardsOf(Player.YOU);
                    Card card = hand.get(mSelectedIdx);

                    refreshScreen(mUno.isLegalToPlay(card)
                            ? i18n.info_clickAgainToPlay(card.name)
                            : i18n.info_cannotPlay(card.name));
                } // else
                break; // case Player.YOU

            case STAT_WILD_COLOR:
                // Need to specify the following legal color after played a
                // wild card. Draw color sectors in the center of screen
                refreshScreen(i18n.ask_color());
                break; // case STAT_WILD_COLOR

            case STAT_DOUBT_WILD4:
                if (mUno.getNext() == Player.YOU && !mAuto) {
                    // Challenge or not is decided by you
                    refreshScreen(i18n.ask_challenge(
                            mUno.next2lastColor().ordinal()), 0x00);
                } // if (mUno.getNext() == Player.YOU && !mAuto)
                else if (mAI.needToChallenge()) {
                    // Challenge or not is decided by AI
                    onChallenge();
                } // else if (mAI.needToChallenge())
                else {
                    mUno.switchNow();
                    draw(4, /* force */ true);
                } // else
                break; // case STAT_DOUBT_WILD4

            case STAT_SEVEN_TARGET:
                // In 7-0 rule, when someone put down a seven card, the player
                // must swap hand cards with another player immediately.
                if (mAuto || mUno.getNow() != Player.YOU) {
                    // Seven-card is played by AI. Select target automatically.
                    swapWith(mAI.calcBestSwapTarget4NowPlayer());
                } // if (mAuto || mUno.getNow() != Player.YOU)
                else {
                    // Seven-card is played by you. Select target manually.
                    refreshScreen(i18n.ask_target(), 0x00);
                } // else
                break; // case STAT_SEVEN_TARGET

            case STAT_ASK_KEEP_PLAY:
                if (mAuto) {
                    play(mSelectedIdx, mAI.calcBestColor4NowPlayer());
                } // if (mAuto)
                else {
                    refreshScreen(i18n.ask_keep_play(), 0x01);
                } // else
                break; // case STAT_ASK_KEEP_PLAY

            case Player.COM1:
            case Player.COM2:
            case Player.COM3:
                // AI players' turn
                requestAI();
                break; // case Player.COM1, Player.COM2, Player.COM3

            case STAT_GAME_OVER:
                // Game over
                if (mAdjustOptions) {
                    refreshScreen(i18n.info_ruleSettings());
                } // if (mAdjustOptions)
                else {
                    refreshScreen(i18n.info_gameOver(mScore, mDiff));
                } // else
                break; // case STAT_GAME_OVER

            default:
                break; // default
        } // switch (mStatus = status)
    } // setStatus(int)

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
    private int getTextWidth(String text) {
        int width = 0;
        char[] txt = text.toCharArray();

        for (int i = 0, n = txt.length; i < n; ++i) {
            if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']') {
                i += 2;
            } // if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']')
            else {
                width += ' ' <= txt[i] && txt[i] <= '~' ? 17 : 33;
            } // else
        } // for (int i = 0, n = txt.length; i < n; ++i)

        return width;
    } // getTextWidth(String)

    /**
     * Put text on image, using our custom font.
     * <p>
     * SPECIAL: In the text string, you can use color marks ([R], [B],
     * [G], [W] and [Y]) to control the color of the remaining text.
     *
     * @param text Put which text.
     * @param x    Put on where (x coordinate).
     * @param y    Put on where (y coordinate).
     */
    private void putText(String text, int x, int y) {
        int pen_red = 0xffff7777;
        int pen_blue = 0xff7777ff;
        int pen_green = 0xff77cc77;
        int pen_white = 0xffcccccc;
        int pen_yellow = 0xffffcc11;
        int width = getTextWidth(text);

        if (width > 0) {
            char[] txt = text.toCharArray();
            Mat mat = mScr.submat(y - 36, y + 12, x, x + width);
            Bitmap bmp = Bitmap.createBitmap(width, 48, mBmp.getConfig());
            Canvas cvs = new Canvas(bmp);

            mPen.setColor(pen_white);
            Utils.matToBitmap(mat, bmp);
            for (int i = x = 0, n = txt.length; i < n; ++i) {
                if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']') {
                    ++i;
                    if (txt[i] == 'R')
                        mPen.setColor(pen_red);
                    if (txt[i] == 'B')
                        mPen.setColor(pen_blue);
                    if (txt[i] == 'G')
                        mPen.setColor(pen_green);
                    if (txt[i] == 'W')
                        mPen.setColor(pen_white);
                    if (txt[i] == 'Y')
                        mPen.setColor(pen_yellow);
                    ++i;
                } // if ('[' == txt[i] && i + 2 < n && txt[i + 2] == ']')
                else {
                    cvs.drawText(txt, i, 1, x, 36, mPen);
                    x += ' ' <= txt[i] && txt[i] <= '~' ? 17 : 33;
                } // else
            } // for (int i = x = 0, n = txt.length; i < n; ++i)

            Utils.bitmapToMat(bmp, mat);
            bmp.recycle();
        } // if (width > 0)
    } // putText(String, int, int)

    /**
     * Refresh the screen display.
     *
     * @param message Extra message to show.
     */
    @WorkerThread
    private void refreshScreen(String message) {
        refreshScreen(message, /* area */ 0xff);
    } // refreshScreen(String)

    /**
     * Refresh the screen display.
     *
     * @param message Extra message to show.
     * @param area    Refresh which area. Use | to select multiple areas.
     *                0x01 = Your hand card area
     *                0x02 = WEST's hand card area
     *                0x04 = NORTH's hand card area
     *                0x08 = EAST's hand card area
     *                0x10 = (Reversed bit, not used yet)
     *                0x20 = (Reversed bit, not used yet)
     *                0x40 = Center deck area
     *                0x80 = Recent card area
     *                0xff = Full screen (default)
     */
    @WorkerThread
    private void refreshScreen(String message, int area) {
        Mat image;
        Size axes;
        String info;
        Point center;
        boolean active;
        Uno.RecentInfo[] recent;
        int i, x, y, remain, size, status, used, width;

        // Lock the value of member [mStatus]
        status = mStatus;

        // Clear
        if ((area &= 0xff) == 0xff) {
            mUno.getBackground().copyTo(mScr);
        } // if ((area &= 0xff) == 0xff)
        else {
            image = mUno.getBackground().submat(584, 632, 200, 1400);
            image.copyTo(mScr.submat(584, 632, 200, 1400));
        } // else

        // Message area
        width = getTextWidth(message);
        putText(message, 800 - width / 2, 620);

        // Left-bottom corner: <OPTIONS> button
        // Shows only when game is not in process
        if (status == Player.YOU ||
                status == STAT_WELCOME ||
                status == STAT_GAME_OVER) {
            active = mAdjustOptions;
            putText(i18n.btn_settings(active), 20, 880);
        } // if (status == Player.YOU || ...)

        // Right-bottom corner: <AUTO> button
        if (status == Player.YOU && !mAuto && !mAdjustOptions) {
            width = getTextWidth(i18n.btn_auto());
            putText(i18n.btn_auto(), 1580 - width, 880);
        } // if (status == Player.YOU && !mAuto && !mAdjustOptions)
        else if (status == STAT_WELCOME && !mAdjustOptions) {
            // [UPDATE] When in welcome screen, change to <LOAD> button
            width = getTextWidth("[G]<LOAD>");
            putText("[G]<LOAD>", 1580 - width, 880);
        } // else if (status == STAT_WELCOME && !mAdjustOptions)
        else if (status == STAT_GAME_OVER && !mAdjustOptions) {
            // [UPDATE] When game over, change to <SAVE> button
            width = getTextWidth("[B]<SAVE>");
            putText("[B]<SAVE>", 1580 - width, 880);
        } // else if (status == STAT_GAME_OVER && !mAdjustOptions)

        if (mAdjustOptions) {
            // Show special screen when configuring game options
            // BGM switch
            putText(i18n.label_bgm(), 60, 160);
            image = mBgmVol > 0.0f
                    ? mUno.findCard(Color.RED, Content.SKIP).darkImg
                    : mUno.findCard(Color.RED, Content.SKIP).image;
            image.copyTo(mScr.submat(60, 241, 150, 271), image);
            image = mBgmVol > 0.0f
                    ? mUno.findCard(Color.GREEN, Content.REV).image
                    : mUno.findCard(Color.GREEN, Content.REV).darkImg;
            image.copyTo(mScr.submat(60, 241, 330, 451), image);

            // Sound effect switch
            putText(i18n.label_snd(), 60, 350);
            image = mSndVol > 0.0f
                    ? mUno.findCard(Color.RED, Content.SKIP).darkImg
                    : mUno.findCard(Color.RED, Content.SKIP).image;
            image.copyTo(mScr.submat(250, 431, 150, 271), image);
            image = mSndVol > 0.0f
                    ? mUno.findCard(Color.GREEN, Content.REV).image
                    : mUno.findCard(Color.GREEN, Content.REV).darkImg;
            image.copyTo(mScr.submat(250, 431, 330, 451), image);

            // Speed
            putText(i18n.label_speed(), 780, 350);
            image = mSpeed < 2
                    ? mUno.findCard(Color.GREEN, Content.NUM1).image
                    : mUno.findCard(Color.GREEN, Content.NUM1).darkImg;
            image.copyTo(mScr.submat(250, 431, 930, 1051), image);
            image = mSpeed == 2
                    ? mUno.findCard(Color.YELLOW, Content.NUM2).image
                    : mUno.findCard(Color.YELLOW, Content.NUM2).darkImg;
            image.copyTo(mScr.submat(250, 431, 1110, 1231), image);
            image = mSpeed > 2
                    ? mUno.findCard(Color.BLUE, Content.NUM3).image
                    : mUno.findCard(Color.BLUE, Content.NUM3).darkImg;
            image.copyTo(mScr.submat(250, 431, 1290, 1411), image);

            if (status != Player.YOU) {
                // [Level] option: easy / hard
                putText(i18n.label_level(), 780, 160);
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_EASY,
                        /* hiLight */ mUno.getDifficulty() == Uno.LV_EASY
                ); // image = mUno.getLevelImage()
                image.copyTo(mScr.submat(60, 241, 930, 1051), image);
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_HARD,
                        /* hiLight */ mUno.getDifficulty() == Uno.LV_HARD
                ); // image = mUno.getLevelImage()
                image.copyTo(mScr.submat(60, 241, 1110, 1231), image);

                // Rule settings
                // Initial Cards
                putText(i18n.label_initialCards(), 60, 670);
                width = mUno.getInitialCards();
                info = "" + width / 10 + width % 10;
                putText("<-", 896, 670);
                putText(info, 1127, 670);
                putText("+>", 1358, 670);

                // Game Mode
                putText(i18n.label_gameMode(), 60, 720);
                active = mUno.getPlayers() == 3;
                putText(i18n.btn_3p(active), 896, 720);
                active = mUno.getPlayers() == 4
                        && !mUno.isSevenZeroRule()
                        && !mUno.is2vs2();
                putText(i18n.btn_4p(active), 1028, 720);
                active = mUno.isSevenZeroRule();
                putText(i18n.btn_7_0(active), 1159, 720);
                active = mUno.is2vs2();
                putText(i18n.btn_2vs2(active), 1290, 720);

                // Force play switch
                i = mUno.getForcePlayRule();
                putText(i18n.label_forcePlay(), 60, 770);
                putText(i18n.btn_keep(i == 0), 896, 770);
                putText(i18n.btn_ask(i == 1), 1110, 770);
                putText(i18n.btn_play(i == 2), 1290, 770);

                // +2 stack
                i = mUno.getStackRule();
                putText(i18n.label_draw2Stack(), 60, 820);
                putText(i18n.btn_none(i == 0), 896, 820);
                putText(i18n.btn_d2(i == 1), 1110, 820);
                putText(i18n.btn_d4(i == 2), 1290, 820);
            } // if (status != Player.YOU)

            // Show image
            Utils.matToBitmap(mScr, mBmp);
            mUIHandler.sendEmptyMessage(0); // -> handleMessage()
            return;
        } // if (mAdjustOptions)

        if (status == STAT_WELCOME) {
            // For welcome screen, show the start button and your score
            image = mUno.getBackImage();
            image.copyTo(mScr.submat(360, 541, 740, 861), image);
            width = getTextWidth(i18n.label_score());
            putText(i18n.label_score(), 500 - width, 800);
            if (mScore < 0) {
                image = mUno.getColoredWildImage(Color.NONE);
            } // if (mScore < 0)
            else {
                i = mScore / 1000;
                image = mUno.findCard(Color.RED, Content.values()[i]).image;
            } // else

            image.copyTo(mScr.submat(700, 881, 520, 641), image);
            i = Math.abs(mScore / 100 % 10);
            image = mUno.findCard(Color.BLUE, Content.values()[i]).image;
            image.copyTo(mScr.submat(700, 881, 660, 781), image);
            i = Math.abs(mScore / 10 % 10);
            image = mUno.findCard(Color.GREEN, Content.values()[i]).image;
            image.copyTo(mScr.submat(700, 881, 800, 921), image);
            i = Math.abs(mScore % 10);
            image = mUno.findCard(Color.YELLOW, Content.values()[i]).image;
            image.copyTo(mScr.submat(700, 881, 940, 1061), image);

            // Show image
            Utils.matToBitmap(mScr, mBmp);
            mUIHandler.sendEmptyMessage(0); // -> handleMessage()
            return;
        } // if (status == STAT_WELCOME)

        // Center: card deck & recent played card
        if ((area & 0x40) != 0x00) {
            if (area != 0xff) {
                image = mUno.getBackground().submat(270, 541, 270, 541);
                image.copyTo(mScr.submat(270, 541, 270, 541));
            } // if (area != 0xff)

            image = mUno.getBackImage();
            image.copyTo(mScr.submat(360, 541, 338, 459), image);
        } // if ((area & 0x40) != 0x00)

        if ((area & 0x80) != 0x00) {
            recent = mUno.getRecentInfo();
            for (i = 0, x = 986; i < 4; ++i, x += 44) {
                if (recent[i].card == null) {
                    continue;
                } // if (recent[i].card == null)
                else if (recent[i].card.content == Content.WILD) {
                    image = mUno.getColoredWildImage(recent[i].color);
                } // else if (recent[i].card.content == Content.WILD)
                else if (recent[i].card.content == Content.WILD_DRAW4) {
                    image = mUno.getColoredWildDraw4Image(recent[i].color);
                } // else if (recent[i].card.content == Content.WILD_DRAW4)
                else {
                    image = recent[i].card.image;
                } // else

                image.copyTo(mScr.submat(360, 541, x, x + 121), image);
            } // for (i = 0, x = 986; i < 4; ++i, x += 44)
        } // if ((area & 0x80) != 0x00)

        // Left-top corner: remain / used
        if (area != 0xff) {
            image = mUno.getBackground().submat(6, 54, 20, 190);
            image.copyTo(mScr.submat(6, 54, 20, 190));
        } // if (area != 0xff)

        remain = mUno.getDeckCount();
        used = mUno.getUsedCount();
        info = i18n.label_remain_used(remain, used);
        putText(info, 20, 42);

        // Right-top corner: lacks
        if (area != 0xff) {
            image = mUno.getBackground().submat(6, 54, 1410, 1580);
            image.copyTo(mScr.submat(6, 54, 1410, 1580));
        } // if (area != 0xff)

        if (mUno.getPlayer(Player.COM2).getWeakColor() == Color.RED)
            info = "LACK: [R]N";
        else if (mUno.getPlayer(Player.COM2).getWeakColor() == Color.BLUE)
            info = "LACK: [B]N";
        else if (mUno.getPlayer(Player.COM2).getWeakColor() == Color.GREEN)
            info = "LACK: [G]N";
        else if (mUno.getPlayer(Player.COM2).getWeakColor() == Color.YELLOW)
            info = "LACK: [Y]N";
        else
            info = "LACK: N";
        if (mUno.getPlayer(Player.COM3).getWeakColor() == Color.RED)
            info += "[R]E";
        else if (mUno.getPlayer(Player.COM3).getWeakColor() == Color.BLUE)
            info += "[B]E";
        else if (mUno.getPlayer(Player.COM3).getWeakColor() == Color.GREEN)
            info += "[G]E";
        else if (mUno.getPlayer(Player.COM3).getWeakColor() == Color.YELLOW)
            info += "[Y]E";
        else
            info += "[W]E";
        if (mUno.getPlayer(Player.COM1).getWeakColor() == Color.RED)
            info += "[R]W";
        else if (mUno.getPlayer(Player.COM1).getWeakColor() == Color.BLUE)
            info += "[B]W";
        else if (mUno.getPlayer(Player.COM1).getWeakColor() == Color.GREEN)
            info += "[G]W";
        else if (mUno.getPlayer(Player.COM1).getWeakColor() == Color.YELLOW)
            info += "[Y]W";
        else
            info += "[W]W";
        if (mUno.getPlayer(Player.YOU).getWeakColor() == Color.RED)
            info += "[R]S";
        else if (mUno.getPlayer(Player.YOU).getWeakColor() == Color.BLUE)
            info += "[B]S";
        else if (mUno.getPlayer(Player.YOU).getWeakColor() == Color.GREEN)
            info += "[G]S";
        else if (mUno.getPlayer(Player.YOU).getWeakColor() == Color.YELLOW)
            info += "[Y]S";
        else
            info += "[W]S";
        width = getTextWidth(info);
        putText(info, 1580 - width, 42);

        // Left-center: Hand cards of Player West (COM1)
        if ((area & 0x02) != 0x00) {
            if (area != 0xff) {
                image = mUno.getBackground().submat(96, 805, 20, 185);
                image.copyTo(mScr.submat(96, 805, 20, 185));
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && mWinner == Player.COM1) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 80 - width / 2, 461);
            } // if (status == STAT_GAME_OVER && mWinner == Player.COM1)
            else if (((mHideFlag >> 1) & 0x01) == 0x00) {
                Player p = mUno.getPlayer(Player.COM1);
                List<Card> hand = p.getHandCards();

                size = hand.size();
                width = 44 * Math.min(size, 13) + 136;
                for (i = 0; i < size; ++i) {
                    x = 20 + i / 13 * 44;
                    y = 450 - width / 2 + i % 13 * 44;
                    image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                    image.copyTo(mScr.submat(y, y + 181, x, x + 121), image);
                } // for (i = 0; i < size; ++i)

                if (size == 1) {
                    // Show "UNO" warning when only one card in hand
                    width = getTextWidth("UNO");
                    putText("[Y]UNO", 80 - width / 2, 584);
                } // if (size == 1)
            } // else if (((mHideFlag >> 1) & 0x01) == 0x00)
        } // if ((area & 0x02) != 0x00)

        // Top-center: Hand cards of Player North (COM2)
        if ((area & 0x04) != 0x00) {
            if (area != 0xff) {
                image = mUno.getBackground().submat(20, 201, 190, 1411);
                image.copyTo(mScr.submat(20, 201, 190, 1411));
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && mWinner == Player.COM2) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 800 - width / 2, 121);
            } // if (status == STAT_GAME_OVER && mWinner == Player.COM2)
            else if (((mHideFlag >> 2) & 0x01) == 0x00) {
                Player p = mUno.getPlayer(Player.COM2);
                List<Card> hand = p.getHandCards();

                size = hand.size();
                width = 44 * size + 76;
                for (i = 0, x = 800 - width / 2; i < size; ++i, x += 44) {
                    image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                    image.copyTo(mScr.submat(20, 201, x, x + 121), image);
                } // for (i = 0, x = 800 - width / 2; i < size; ++i, x += 44)

                if (size == 1) {
                    // Show "UNO" warning when only one card in hand
                    width = getTextWidth("UNO");
                    putText("[Y]UNO", 720 - width, 121);
                } // if (size == 1)
            } // else if (((mHideFlag >> 2) & 0x01) == 0x00)
        } // if ((area & 0x04) != 0x00)

        // Right-center: Hand cards of Player East (COM3)
        if ((area & 0x08) != 0x00) {
            if (area != 0xff) {
                image = mUno.getBackground().submat(96, 805, 1416, 1581);
                image.copyTo(mScr.submat(96, 805, 1416, 1581));
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && mWinner == Player.COM3) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 1520 - width / 2, 461);
            } // if (status == STAT_GAME_OVER && mWinner == Player.COM3)
            else if (((mHideFlag >> 3) & 0x01) == 0x00) {
                Player p = mUno.getPlayer(Player.COM3);
                List<Card> hand = p.getHandCards();

                size = hand.size();
                width = 44 * Math.min(size, 13) + 136;
                for (i = 13; i < size; ++i) {
                    x = 1416;
                    y = 450 - width / 2 + (i - 13) * 44;
                    image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                    image.copyTo(mScr.submat(y, y + 181, x, x + 121), image);
                } // for (i = 13; i < size; ++i)

                for (i = 0; i < 13 && i < size; ++i) {
                    x = 1460;
                    y = 450 - width / 2 + i * 44;
                    image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                    image.copyTo(mScr.submat(y, y + 181, x, x + 121), image);
                } // for (i = 0; i < 13 && i < size; ++i)

                if (size == 1) {
                    // Show "UNO" warning when only one card in hand
                    width = getTextWidth("UNO");
                    putText("[Y]UNO", 1520 - width / 2, 584);
                } // if (size == 1)
            } // else if (((mHideFlag >> 3) & 0x01) == 0x00)
        } // if ((area & 0x08) != 0x00)

        // Bottom: Your hand cards
        if ((area & 0x01) != 0x00) {
            if (area != 0xff) {
                image = mUno.getBackground().submat(680, 881, 190, 1411);
                image.copyTo(mScr.submat(680, 881, 190, 1411));
            } // if (area != 0xff)

            if (status == STAT_GAME_OVER && mWinner == Player.YOU) {
                // Played all hand cards, it's winner
                width = getTextWidth("WIN");
                putText("[G]WIN", 800 - width / 2, 801);
            } // if (status == STAT_GAME_OVER && mWinner == Player.YOU)
            else if ((mHideFlag & 0x01) == 0x00) {
                // Show your all hand cards
                List<Card> hand = mUno.getHandCardsOf(Player.YOU);

                size = hand.size();
                width = 44 * size + 76;
                for (i = 0, x = 800 - width / 2; i < size; ++i, x += 44) {
                    image = status == STAT_GAME_OVER
                            || (status == Player.YOU && mUno.isLegalToPlay(hand.get(i)))
                            || (status == STAT_ASK_KEEP_PLAY && i == mSelectedIdx)
                            ? hand.get(i).image
                            : hand.get(i).darkImg;
                    y = i == mSelectedIdx ? 680 : 700;
                    image.copyTo(mScr.submat(y, y + 181, x, x + 121), image);
                } // for (i = 0, x = 800 - width / 2; i < size; ++i, x += 44)

                if (size == 1) {
                    // Show "UNO" warning when only one card in hand
                    putText("[Y]UNO", 880, 801);
                } // if (size == 1)
            } // else if ((mHideFlag & 0x01) == 0x00)
        } // if ((area & 0x01) != 0x00)

        // Extra sectors in special status
        switch (status) {
            case STAT_WILD_COLOR:
                // Need to specify the following legal color after played a
                // wild card. Draw color sectors in the center of screen
                center = new Point(405, 405);
                axes = new Size(135, 135);

                // Draw blue sector
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 0,
                        /* startAngle */ 0,
                        /* endAngle   */ -90,
                        /* color      */ RGB_BLUE,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()

                // Draw green sector
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 0,
                        /* startAngle */ 0,
                        /* endAngle   */ 90,
                        /* color      */ RGB_GREEN,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()

                // Draw red sector
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 180,
                        /* startAngle */ 0,
                        /* endAngle   */ 90,
                        /* color      */ RGB_RED,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()

                // Draw yellow sector
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 180,
                        /* startAngle */ 0,
                        /* endAngle   */ -90,
                        /* color      */ RGB_YELLOW,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()
                break; // case STAT_WILD_COLOR

            case STAT_DOUBT_WILD4:
            case STAT_ASK_KEEP_PLAY:
                // Ask whether you want to challenge your previous player
                center = new Point(405, 405);
                axes = new Size(135, 135);

                // Draw YES button
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 0,
                        /* startAngle */ 0,
                        /* endAngle   */ -180,
                        /* color      */ RGB_GREEN,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()
                width = getTextWidth(i18n.label_yes());
                putText(i18n.label_yes(), 405 - width / 2, 358);

                // Draw NO button
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 0,
                        /* startAngle */ 0,
                        /* endAngle   */ 180,
                        /* color      */ RGB_RED,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()
                width = getTextWidth(i18n.label_no());
                putText(i18n.label_no(), 405 - width / 2, 472);
                break; // case STAT_DOUBT_WILD4, STAT_ASK_KEEP_PLAY

            case STAT_SEVEN_TARGET:
                // Ask the target you want to swap hand cards with
                center = new Point(405, 405);
                axes = new Size(135, 135);

                // Draw west sector (red)
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 90,
                        /* startAngle */ 0,
                        /* endAngle   */ 120,
                        /* color      */ RGB_RED,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()
                width = getTextWidth("W");
                putText("W", 338 - width / 2, 440);

                // Draw east sector (green)
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ 90,
                        /* startAngle */ 0,
                        /* endAngle   */ -120,
                        /* color      */ RGB_GREEN,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()
                width = getTextWidth("E");
                putText("E", 472 - width / 2, 440);

                // Draw north sector (yellow)
                Imgproc.ellipse(
                        /* img        */ mScr,
                        /* center     */ center,
                        /* axes       */ axes,
                        /* angle      */ -150,
                        /* startAngle */ 0,
                        /* endAngle   */ 120,
                        /* color      */ RGB_YELLOW,
                        /* thickness  */ -1,
                        /* lineType   */ Imgproc.LINE_AA
                ); // Imgproc.ellipse()
                width = getTextWidth("N");
                putText("N", 405 - width / 2, 360);
                break; // case STAT_SEVEN_TARGET

            default:
                break; // default
        } // switch (status)

        // Show screen
        Utils.matToBitmap(mScr, mBmp);
        mUIHandler.sendEmptyMessage(0); // -> handleMessage()
    } // refreshScreen(String, int)

    /**
     * Draw [mBmp] on the screen. Called by system.
     */
    @Override
    @UiThread
    public boolean handleMessage(@NonNull Message message) {
        mImgScreen.setImageBitmap(mBmp);
        return true;
    } // handleMessage(@NonNull Message)

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass
     * the hand cards to the next player.
     */
    @WorkerThread
    private void cycle() {
        final int[] x = {740, 160, 740, 1320};
        final int[] y = {670, 360, 50, 360};
        int curr, next, oppo, prev;

        setStatus(STAT_IDLE);
        mHideFlag = 0x0f;
        refreshScreen(i18n.info_0_rotate(), 0x0f);
        curr = mUno.getNow();
        next = mUno.getNext();
        oppo = mUno.getOppo();
        prev = mUno.getPrev();
        mLayer[0].elem = mUno.getBackImage();
        mLayer[0].startLeft = x[curr];
        mLayer[0].startTop = y[curr];
        mLayer[0].endLeft = x[next];
        mLayer[0].endTop = y[next];
        mLayer[1].elem = mUno.getBackImage();
        mLayer[1].startLeft = x[next];
        mLayer[1].startTop = y[next];
        mLayer[1].endLeft = x[oppo];
        mLayer[1].endTop = y[oppo];
        if (mUno.getPlayers() == 3) {
            mLayer[2].elem = mUno.getBackImage();
            mLayer[2].startLeft = x[oppo];
            mLayer[2].startTop = y[oppo];
            mLayer[2].endLeft = x[curr];
            mLayer[2].endTop = y[curr];
            animate(3, mLayer);
        } // if (mUno.getPlayers() == 3)
        else {
            mLayer[2].elem = mUno.getBackImage();
            mLayer[2].startLeft = x[oppo];
            mLayer[2].startTop = y[oppo];
            mLayer[2].endLeft = x[prev];
            mLayer[2].endTop = y[prev];
            mLayer[3].elem = mUno.getBackImage();
            mLayer[3].startLeft = x[prev];
            mLayer[3].startTop = y[prev];
            mLayer[3].endLeft = x[curr];
            mLayer[3].endTop = y[curr];
            animate(4, mLayer);
        } // else

        mHideFlag = 0x00;
        mUno.cycle();
        refreshScreen(i18n.info_0_rotate(), 0x0f);
        threadWait(1500);
        setStatus(mUno.switchNow());
    } // cycle()

    /**
     * The player in action swap hand cards with another player.
     *
     * @param whom Swap with whom. Must be one of the following:
     *             Player.YOU, Player.COM1, Player.COM2, Player.COM3
     */
    @WorkerThread
    private void swapWith(int whom) {
        final int[] x = {740, 160, 740, 1320};
        final int[] y = {670, 360, 50, 360};
        int curr, flag;

        setStatus(STAT_IDLE);
        curr = mUno.getNow();
        mHideFlag = (1 << curr) | (1 << whom);
        flag = (1 << curr) | (1 << whom) | (curr == Player.YOU ? 0x40 : 0x00);
        refreshScreen(i18n.info_7_swap(curr, whom), flag);
        mLayer[0].elem = mLayer[1].elem = mUno.getBackImage();
        mLayer[0].startLeft = x[curr];
        mLayer[0].startTop = y[curr];
        mLayer[0].endLeft = x[whom];
        mLayer[0].endTop = y[whom];
        mLayer[1].startLeft = x[whom];
        mLayer[1].startTop = y[whom];
        mLayer[1].endLeft = x[curr];
        mLayer[1].endTop = y[curr];
        animate(2, mLayer);
        mHideFlag = 0x00;
        mUno.swap(curr, whom);
        refreshScreen(i18n.info_7_swap(curr, whom), flag & ~0x40);
        threadWait(1500);
        setStatus(mUno.switchNow());
    } // swapWith(int)

    /**
     * The player in action play a card.
     *
     * @param index Play which card. Pass the corresponding card's index of the
     *              player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     */
    @WorkerThread
    private void play(int index, Color color) {
        Card card;
        int c, now, size, width, next, flag;

        setStatus(STAT_IDLE); // block tap down events when idle
        now = mUno.getNow();
        size = mUno.getCurrPlayer().getHandSize();
        if (mUno.getStackRule() == 2 && mUno.getCurrPlayer()
                .getHandCards().get(index).content == Content.WILD_DRAW4) {
            color = mUno.lastColor();
        } // if (mUno.getStackRule() == 2 && ...)

        card = mUno.play(now, index, color);
        mSelectedIdx = -1;
        mSoundPool.play(sndPlay, mSndVol, mSndVol, 1, 0, 1.0f);
        if (size == 2) {
            mSoundPool.play(sndUno, mSndVol, mSndVol, 1, 0, 1.0f);
        } // if (size == 2)

        if (card != null) {
            mLayer[0].elem = card.image;
            switch (now) {
                case Player.COM1:
                    width = 44 * Math.min(size, 13) + 136;
                    mLayer[0].startLeft = 160 + index / 13 * 44;
                    mLayer[0].startTop = 450 - width / 2 + index % 13 * 44;
                    break; // case Player.COM1

                case Player.COM2:
                    width = 44 * size + 76;
                    mLayer[0].startLeft = 800 - width / 2 + 44 * index;
                    mLayer[0].startTop = 50;
                    break; // case Player.COM2

                case Player.COM3:
                    width = 44 * Math.min(size, 13) + 136;
                    mLayer[0].startLeft = 1320 - index / 13 * 44;
                    mLayer[0].startTop = 450 - width / 2 + index % 13 * 44;
                    break; // case Player.COM3

                default:
                    width = 44 * size + 76;
                    mLayer[0].startLeft = 800 - width / 2 + 44 * index;
                    mLayer[0].startTop = 680;
                    break; // default
            } // switch (now)

            mLayer[0].endLeft = 1118;
            mLayer[0].endTop = 360;
            animate(1, mLayer);
            if (size == 1) {
                // The player in action becomes winner when it played the
                // final card in its hand successfully
                if (mUno.is2vs2()) {
                    if (now == Player.YOU || now == Player.COM2) {
                        mDiff = 2 * (mUno.getPlayer(Player.COM1).getHandScore()
                                + mUno.getPlayer(Player.COM3).getHandScore());
                        mScore = Math.min(9999, 200 + mScore + mDiff);
                        mSoundPool.play(sndWin, mSndVol, mSndVol, 1, 0, 1.0f);
                    } // if (now == Player.YOU || now == Player.COM2)
                    else {
                        mDiff = -2 * (mUno.getPlayer(Player.YOU).getHandScore()
                                + mUno.getPlayer(Player.COM2).getHandScore());
                        mScore = Math.max(-999, 200 + mScore + mDiff);
                        mSoundPool.play(sndLose, mSndVol, mSndVol, 1, 0, 1.0f);
                    } // else
                } // if (mUno.is2vs2())
                else if (now == Player.YOU) {
                    mDiff = mUno.getPlayer(Player.COM1).getHandScore() +
                            mUno.getPlayer(Player.COM2).getHandScore() +
                            mUno.getPlayer(Player.COM3).getHandScore();
                    mScore = Math.min(9999, 200 + mScore + mDiff);
                    mSoundPool.play(sndWin, mSndVol, mSndVol, 1, 0, 1.0f);
                } // else if (now == Player.YOU)
                else {
                    mDiff = -mUno.getPlayer(Player.YOU).getHandScore();
                    mScore = Math.max(-999, 200 + mScore + mDiff);
                    mSoundPool.play(sndLose, mSndVol, mSndVol, 1, 0, 1.0f);
                } // else

                mAuto = false; // Force disable the AUTO switch
                mWinner = now;
                setStatus(STAT_GAME_OVER);
            } // if (size == 1)
            else {
                // When the played card is an action card or a wild card,
                // do the necessary things according to the game rule
                flag = now == Player.YOU ? 0xff : (1 << now) | 0x80;
                switch (card.content) {
                    case DRAW2:
                        next = mUno.switchNow();
                        if (mUno.getStackRule() != 0) {
                            c = mUno.getDraw2StackCount();
                            refreshScreen(i18n.act_playDraw2(now, next, c), flag);
                            threadWait(1500);
                            setStatus(next);
                        } // if (mUno.getStackRule() != 0)
                        else {
                            refreshScreen(i18n.act_playDraw2(now, next, 2), flag);
                            threadWait(1500);
                            draw(2, /* force */ true);
                        } // else
                        break; // case DRAW2

                    case SKIP:
                        next = mUno.switchNow();
                        refreshScreen(i18n.act_playSkip(now, next), flag);
                        threadWait(1500);
                        setStatus(mUno.switchNow());
                        break; // case SKIP

                    case REV:
                        mUno.switchDirection();
                        refreshScreen(i18n.act_playRev(now));
                        threadWait(1500);
                        setStatus(mUno.switchNow());
                        break; // case REV

                    case WILD:
                        refreshScreen(i18n.act_playWild(now, color.ordinal()), flag);
                        threadWait(1500);
                        setStatus(mUno.switchNow());
                        break; // case WILD

                    case WILD_DRAW4:
                        if (mUno.getStackRule() == 2) {
                            next = mUno.switchNow();
                            c = mUno.getDraw2StackCount();
                            refreshScreen(i18n.act_playDraw2(now, next, c), flag);
                            threadWait(1500);
                            setStatus(next);
                        } // if (mUno.getStackRule() == 2)
                        else {
                            next = mUno.getNext();
                            refreshScreen(i18n.act_playWildDraw4(now, next), flag);
                            threadWait(1500);
                            setStatus(STAT_DOUBT_WILD4);
                        } // else
                        break; // case WILD_DRAW4

                    case NUM7:
                        if (mUno.isSevenZeroRule()) {
                            refreshScreen(i18n.act_playCard(now, card.name), flag);
                            threadWait(750);
                            setStatus(STAT_SEVEN_TARGET);
                            break; // case NUM7
                        } // if (mUno.isSevenZeroRule())
                        // else fall through

                    case NUM0:
                        if (mUno.isSevenZeroRule()) {
                            refreshScreen(i18n.act_playCard(now, card.name), flag);
                            threadWait(750);
                            cycle();
                            break; // case NUM0
                        } // if (mUno.isSevenZeroRule())
                        // else fall through

                    default:
                        refreshScreen(i18n.act_playCard(now, card.name), flag);
                        threadWait(1500);
                        setStatus(mUno.switchNow());
                        break; // default
                } // switch (card.content)
            } // else
        } // if (card != null)
    } // play(int, Color)

    /**
     * The player in action draw one or more cards.
     *
     * @param count How many cards to draw.
     * @param force Pass true if the specified player is required to draw cards,
     *              i.e. previous player played a [+2] or [wild +4] to let this
     *              player draw cards. Or false if the specified player draws a
     *              card by itself in its action.
     */
    @WorkerThread
    private void draw(int count, boolean force) {
        Card drawn;
        String message;
        int i, index, c, now, size, width, flag;

        setStatus(STAT_IDLE); // block tap down events when idle
        c = mUno.getDraw2StackCount();
        if (c > 0) {
            count = c;
            force = true;
        } // if (c > 0)

        index = -1;
        drawn = null;
        now = mUno.getNow();
        flag = now == Player.YOU ? 0xff : 1 << now;
        mSelectedIdx = -1;
        for (i = 0; i < count; ++i) {
            index = mUno.draw(now, force);
            if (index >= 0) {
                drawn = mUno.getCurrPlayer().getHandCards().get(index);
                size = mUno.getCurrPlayer().getHandSize();
                mLayer[0].startLeft = 338;
                mLayer[0].startTop = 360;
                switch (now) {
                    case Player.COM1:
                        width = 44 * Math.min(size, 13) + 136;
                        mLayer[0].elem = mUno.getBackImage();
                        mLayer[0].endLeft = 20 + index / 13 * 44;
                        mLayer[0].endTop = 450 - width / 2 + index % 13 * 44;
                        message = i18n.act_drawCardCount(now, count);
                        break; // case Player.COM1

                    case Player.COM2:
                        width = 44 * size + 76;
                        mLayer[0].elem = mUno.getBackImage();
                        mLayer[0].endLeft = 800 - width / 2 + 44 * index;
                        mLayer[0].endTop = 20;
                        message = i18n.act_drawCardCount(now, count);
                        break; // case Player.COM2

                    case Player.COM3:
                        width = 44 * Math.min(size, 13) + 136;
                        mLayer[0].elem = mUno.getBackImage();
                        mLayer[0].endLeft = 1460 - index / 13 * 44;
                        mLayer[0].endTop = 450 - width / 2 + index % 13 * 44;
                        message = i18n.act_drawCardCount(now, count);
                        break; // case Player.COM3

                    default:
                        width = 44 * size + 76;
                        mLayer[0].elem = drawn.image;
                        mLayer[0].endLeft = 800 - width / 2 + 44 * index;
                        mLayer[0].endTop = 700;
                        message = i18n.act_drawCard(now, drawn.name);
                        break; // default
                } // switch (now)

                // Animation
                mSoundPool.play(sndDraw, mSndVol, mSndVol, 1, 0, 1.0f);
                animate(1, mLayer);
                refreshScreen(message, flag);
                threadWait(300);
            } // if (index >= 0)
            else {
                message = i18n.info_cannotDraw(now, Uno.MAX_HOLD_CARDS);
                refreshScreen(message, flag);
                break;
            } // else
        } // for (i = 0; i < count; ++i)

        threadWait(750);
        if (count == 1 &&
                drawn != null &&
                mUno.getForcePlayRule() != 0 &&
                mUno.isLegalToPlay(drawn)) {
            // Player drew one card by itself, the drawn card
            // can be played immediately if it's legal to play
            if (mAuto || now != Player.YOU) {
                play(index, mAI.calcBestColor4NowPlayer());
            } // if (mAuto || now != Player.YOU)
            else if (mUno.getForcePlayRule() == 1) {
                // Store index value as global value. This value
                // will be used after the wild color determined.
                mSelectedIdx = index;
                setStatus(STAT_ASK_KEEP_PLAY);
            } // else if (mUno.getForcePlayRule() == 1)
            else if (!drawn.isWild()) {
                // Force play a non-wild card
                play(index, drawn.color);
            } // else if (!drawn.isWild())
            else if (mUno.getStackRule() == 2 &&
                    drawn.content == Content.WILD_DRAW4) {
                // Force play a Wild +4 card, but do not change the next color
                play(index, mUno.lastColor());
            } // else if (mUno.getStackRule() == 2 && ...)
            else {
                // Force play a Wild / Wild +4 card, and change the next color
                mSelectedIdx = index;
                setStatus(STAT_WILD_COLOR);
            } // else
        } // if (count == 1 && ...)
        else {
            refreshScreen(i18n.act_pass(now), 0x00);
            threadWait(750);
            setStatus(mUno.switchNow());
        } // else
    } // draw(int, boolean)

    /**
     * Do uniform motion for objects from somewhere to somewhere.
     * NOTE: This method does not draw the last frame. After animation,
     * you need to call refreshScreen() method to draw the last frame.
     *
     * @param layerCount Move how many objects at the same time.
     * @param layer      Describe your movements by AnimateLayer objects.
     *                   Specifying parameters in AnimateLayer object to
     *                   describe your expected movements, such as:
     *                   [elem]      Move which object.
     *                   [startLeft] The object's start X coordinate.
     *                   [startTop]  The object's start Y coordinate.
     *                   [endLeft]   The object's end X coordinate.
     *                   [endTop]    The object's end Y coordinate.
     */
    @WorkerThread
    private void animate(int layerCount, AnimateLayer[] layer) {
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < layerCount; ++j) {
                AnimateLayer l = layer[j];
                int x1 = l.startLeft + (l.endLeft - l.startLeft) * i / 10;
                int y1 = l.startTop + (l.endTop - l.startTop) * i / 10;
                int x2 = x1 + l.elem.cols();
                int y2 = y1 + l.elem.rows();
                Mat roi1 = mScr.submat(y1, y2, x1, x2);
                Mat roi2 = mBackup[j].submat(y1, y2, x1, x2);
                roi1.copyTo(roi2);
            } // for (int j = 0; j < layerCount; ++j)

            for (int j = 0; j < layerCount; ++j) {
                AnimateLayer l = layer[j];
                int x1 = l.startLeft + (l.endLeft - l.startLeft) * i / 10;
                int y1 = l.startTop + (l.endTop - l.startTop) * i / 10;
                int x2 = x1 + l.elem.cols();
                int y2 = y1 + l.elem.rows();
                l.elem.copyTo(mScr.submat(y1, y2, x1, x2), l.elem);
            } // for (int j = 0; j < layerCount; ++j)

            Utils.matToBitmap(mScr, mBmp);
            mUIHandler.sendEmptyMessage(0); // -> handleMessage()
            threadWait(15);
            for (int j = 0; j < layerCount; ++j) {
                AnimateLayer l = layer[j];
                int x1 = l.startLeft + (l.endLeft - l.startLeft) * i / 10;
                int y1 = l.startTop + (l.endTop - l.startTop) * i / 10;
                int x2 = x1 + l.elem.cols();
                int y2 = y1 + l.elem.rows();
                Mat roi1 = mScr.submat(y1, y2, x1, x2);
                Mat roi2 = mBackup[j].submat(y1, y2, x1, x2);
                roi2.copyTo(roi1);
            } // for (int j = 0; j < layerCount; ++j)
        } // for (int i = 0; i < 10; ++i)
    } // animate(int, AnimateLayer[])

    /**
     * Triggered on challenge chance. When a player played a [wild +4], the next
     * player can challenge its legality. Only when you have no cards that match
     * the previous played card's color, you can play a [wild +4].
     * Next player does not challenge: next player draw 4 cards;
     * Challenge success: current player draw 4 cards;
     * Challenge failure: next player draw 6 cards.
     */
    @WorkerThread
    private void onChallenge() {
        int now, challenger;
        boolean challengeSuccess;

        setStatus(STAT_IDLE); // block tap down events when idle
        now = mUno.getNow();
        challenger = mUno.getNext();
        challengeSuccess = mUno.challenge(now);
        refreshScreen(i18n.info_challenge(challenger, now,
                mUno.next2lastColor().ordinal()), (1 << now) | 0x40);
        threadWait(1500);
        if (challengeSuccess) {
            // Challenge success, who played [wild +4] draws 4 cards
            refreshScreen(i18n.info_challengeSuccess(now), 0x00);
            threadWait(1500);
            draw(4, /* force */ true);
        } // if (challengeSuccess)
        else {
            // Challenge failure, challenger draws 6 cards
            refreshScreen(i18n.info_challengeFailure(challenger), 0x00);
            threadWait(1500);
            mUno.switchNow();
            draw(6, /* force */ true);
        } // else
    } // onChallenge()

    /**
     * Triggered when a touch event occurred.
     *
     * @param v     Touch event occurred on which view object.
     * @param event Which event occurred. Call event.getAction() to get the type
     *              of occurred touch event, such as MotionEvent.ACTION_DOWN.
     * @return True because the touch events will be handled by our listener.
     */
    @Override
    @UiThread
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = event.getAction() == MotionEvent.ACTION_DOWN;

        if (handled) {
            int x = (int) (event.getX() * 1600 / v.getWidth());
            int y = (int) (event.getY() * 900 / v.getHeight());
            Message message = mSubHandler.obtainMessage(0, x, y);

            mSubHandler.sendMessage(message);
        } // if (handled)

        return handled;
    } // onTouch(View, MotionEvent)

    /**
     * Triggered when a touch event occurred.
     * Called by onTouch(View, MotionEvent) method, and handled by this method.
     *
     * @param message Touch on where.
     *                Read message.arg1 to get the X coordinate.
     *                Read message.arg2 to get the Y coordinate.
     * @return True because the touch events will be handled by us.
     */
    @WorkerThread
    private boolean handleMessage2(Message message) {
        int x = message.arg1, y = message.arg2;

        if (message.what == 1) {
            // When message.what == 1
            // load the replay file named [message.obj]
            loadReplay((String) message.obj);
        } // if (message.what == 1)
        else if (mAdjustOptions) {
            // Do special behaviors when configuring game options
            if (60 <= y && y <= 240) {
                if (150 <= x && x <= 270) {
                    // BGM OFF button
                    mBgmVol = 0.0f;
                    mMediaPlayer.setVolume(0.0f, 0.0f);
                    setStatus(mStatus);
                } // if (150 <= x && x <= 270)
                else if (330 <= x && x <= 450) {
                    // BGM ON button
                    mBgmVol = 0.5f;
                    mMediaPlayer.setVolume(0.5f, 0.5f);
                    setStatus(mStatus);
                } // else if (330 <= x && x <= 450)
                else if (930 <= x && x <= 1050 && mStatus != Player.YOU) {
                    // Easy AI Level
                    mUno.setDifficulty(Uno.LV_EASY);
                    setStatus(mStatus);
                } // else if (930 <= x && x <= 1050 && mStatus != Player.YOU)
                else if (1110 <= x && x <= 1230 && mStatus != Player.YOU) {
                    // Hard AI Level
                    mUno.setDifficulty(Uno.LV_HARD);
                    setStatus(mStatus);
                } // else if (1110 <= x && x <= 1230 && mStatus != Player.YOU)
            } // if (60 <= y && y <= 240)
            else if (270 <= y && y <= 450) {
                if (150 <= x && x <= 270) {
                    // SND OFF button
                    mSndVol = 0.0f;
                    setStatus(mStatus);
                } // if (150 <= x && x <= 270)
                else if (330 <= x && x <= 450) {
                    // SND ON button
                    mSndVol = 0.5f;
                    mSoundPool.play(sndPlay, 0.5f, 0.5f, 1, 0, 1.0f);
                    setStatus(mStatus);
                } // else if (330 <= x && x <= 450)
                else if (930 <= x && x <= 1050) {
                    // Speed = 1
                    mSpeed = 1;
                    setStatus(mStatus);
                } // else if (930 <= x && x <= 1050)
                else if (1110 <= x && x <= 1230) {
                    // Speed = 2
                    mSpeed = 2;
                    setStatus(mStatus);
                } // else if (1110 <= x && x <= 1230)
                else if (1290 <= x && x <= 1410) {
                    // Speed = 3
                    mSpeed = 3;
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1410)
            } // else if (270 <= y && y <= 450)
            else if (649 <= y && y <= 670 && mStatus != Player.YOU) {
                if (896 <= x && x <= 929) {
                    // Decrease initial cards
                    mUno.decreaseInitialCards();
                    setStatus(mStatus);
                } // if (896 <= x && x <= 929)
                else if (1358 <= x && x <= 1391) {
                    // Increase initial cards
                    mUno.increaseInitialCards();
                    setStatus(mStatus);
                } // else if (1358 <= x && x <= 1391)
            } // else if (649 <= y && y <= 670 && mStatus != Player.YOU)
            else if (699 <= y && y <= 720 && mStatus != Player.YOU) {
                if (896 <= x && x <= 963) {
                    // Game mode, <3P> button
                    mUno.setPlayers(3);
                    setStatus(mStatus);
                } // if (896 <= x && x <= 963)
                else if (1028 <= x && x <= 1095) {
                    // Game mode, <4P> button
                    mUno.setPlayers(4);
                    setStatus(mStatus);
                } // else if (1028 <= x && x <= 1095)
                else if (1159 <= x && x <= 1243) {
                    // Game mode, <7-0> button
                    mUno.setSevenZeroRule(true);
                    setStatus(mStatus);
                } // else if (1159 <= x && x <= 1243)
                else if (1290 <= x && x <= 1391) {
                    // Game mode, <2vs2> button
                    mUno.set2vs2(true);
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1391)
            } // else if (699 <= y && y <= 720 && mStatus != Player.YOU)
            else if (749 <= y && y <= 770 && mStatus != Player.YOU) {
                if (896 <= x && x <= 997) {
                    // Force play, <KEEP> button
                    mUno.setForcePlayRule(0);
                    setStatus(mStatus);
                } // if (896 <= x && x <= 997)
                else if (1110 <= x && x <= 1194) {
                    // Force play, <ASK> button
                    mUno.setForcePlayRule(1);
                    setStatus(mStatus);
                } // else if (1110 <= x && x <= 1194)
                else if (1290 <= x && x <= 1391) {
                    // Force play, <PLAY> button
                    mUno.setForcePlayRule(2);
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1391)
            } // else if (749 <= y && y <= 770 && mStatus != Player.YOU)
            else if (799 <= y && y <= 820 && mStatus != Player.YOU) {
                if (896 <= x && x <= 997) {
                    // stacking, <NONE> button
                    mUno.setStackRule(0);
                    setStatus(mStatus);
                } // if (896 <= x && x <= 997)
                else if (1110 <= x && x <= 1177) {
                    // stacking, <+2> button
                    mUno.setStackRule(1);
                    setStatus(mStatus);
                } // else if (1110 <= x && x <= 1177)
                else if (1290 <= x && x <= 1391) {
                    // stacking, <+2+4> button
                    mUno.setStackRule(2);
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1391)
            } // else if (799 <= y && y <= 820 && mStatus != Player.YOU)
            else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                // <OPTIONS> button
                // Leave options page
                mAdjustOptions = false;
                setStatus(mStatus);
            } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
        } // else if (mAdjustOptions)
        else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580) {
            // <AUTO> button
            // In player's action, automatically play or draw cards by AI
            if (mStatus == Player.YOU && !mAuto) {
                mAuto = true;
                setStatus(mStatus);
            } // if (mStatus == Player.YOU && !mAuto)
            else if (mStatus == STAT_WELCOME) {
                // [UPDATE] When in welcome screen, change to <LOAD> button
                new OpenDialog().show(getSupportFragmentManager(), "OpenDialog");
            } // else if (mStatus == STAT_WELCOME)
            else if (mStatus == STAT_GAME_OVER && !mGameSaved) {
                // [UPDATE] When game over, change to <SAVE> button
                String replayName = mUno.save();

                if (!replayName.isEmpty()) {
                    mGameSaved = true;
                    refreshScreen("Replay file saved as " + replayName);
                } // if (!replayName.isEmpty())
                else {
                    refreshScreen("Failed to save replay file");
                } // else
            } // else if (mStatus == STAT_GAME_OVER && !mGameSaved)
        } // else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580)
        else {
            switch (mStatus) {
                case STAT_WELCOME:
                    if (360 <= y && y <= 540 && 740 <= x && x <= 860) {
                        // UNO button, start a new game
                        mGameSaved = false;
                        setStatus(STAT_NEW_GAME);
                    } // if (360 <= y && y <= 540 && 740 <= x && x <= 860)
                    else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                        // <OPTIONS> button
                        mAdjustOptions = true;
                        setStatus(mStatus);
                    } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
                    break; // case STAT_WELCOME

                case Player.YOU:
                    if (mAuto) {
                        // Do operations automatically by AI strategies
                        break; // case Player.YOU
                    } // if (mAuto)
                    else if (700 <= y && y <= 880) {
                        List<Card> hand = mUno.getHandCardsOf(Player.YOU);
                        int size = hand.size();
                        int width = 44 * size + 76;
                        int startX = 800 - width / 2;

                        if (startX <= x && x <= startX + width) {
                            // Hand card area
                            // Calculate which card clicked by the X-coordinate
                            int index = Math.min((x - startX) / 44, size - 1);
                            Card card = hand.get(index);

                            // Try to play it
                            if (index != mSelectedIdx) {
                                mSelectedIdx = index;
                                setStatus(mStatus);
                            } // if (index != mSelectedIdx)
                            else if (mUno.isLegalToPlay(card)) {
                                if (!card.isWild() || size < 2) {
                                    play(index, Color.NONE);
                                } // if (!card.isWild() || size < 2)
                                else if (mUno.getStackRule() != 2 ||
                                        card.content != Content.WILD_DRAW4) {
                                    setStatus(STAT_WILD_COLOR);
                                } // else if (mUno.getStackRule() != 2 || ...)
                                else {
                                    play(index, mUno.lastColor());
                                } // else
                            } // else if (mUno.isLegalToPlay(card))
                        } // if (startX <= x && x <= startX + width)
                        else if (y >= 859 && 20 <= x && x <= 200) {
                            // <OPTIONS> button
                            mAdjustOptions = true;
                            setStatus(mStatus);
                        } // else if (y >= 859 && 20 <= x && x <= 200)
                        else {
                            // Blank area, cancel your selection
                            mSelectedIdx = -1;
                            setStatus(mStatus);
                        } // else
                    } // else if (700 <= y && y <= 880)
                    else if (360 <= y && y <= 540 && 338 <= x && x <= 458) {
                        // Card deck area, draw a card
                        draw(1, /* force */ false);
                    } // else if (360 <= y && y <= 540 && 338 <= x && x <= 458)
                    break; // case Player.YOU

                case STAT_WILD_COLOR:
                    if (310 < y && y < 405) {
                        if (310 < x && x < 405) {
                            // Red sector
                            play(mSelectedIdx, Color.RED);
                        } // if (310 < x && x < 405)
                        else if (405 < x && x < 500) {
                            // Blue sector
                            play(mSelectedIdx, Color.BLUE);
                        } // else if (405 < x && x < 500)
                    } // if (310 < y && y < 405)
                    else if (405 < y && y < 500) {
                        if (310 < x && x < 405) {
                            // Yellow sector
                            play(mSelectedIdx, Color.YELLOW);
                        } // if (310 < x && x < 405)
                        else if (405 < x && x < 500) {
                            // Green sector
                            play(mSelectedIdx, Color.GREEN);
                        } // else if (405 < x && x < 500)
                    } // else if (405 < y && y < 500)
                    break; // case STAT_WILD_COLOR

                case STAT_DOUBT_WILD4:
                    // Asking if you want to challenge your previous player
                    if (310 < x && x < 500) {
                        if (310 < y && y < 405) {
                            // YES button, challenge wild +4
                            onChallenge();
                        } // if (310 < y && y < 405)
                        else if (405 < y && y < 500) {
                            // NO button, do not challenge wild +4
                            mUno.switchNow();
                            draw(4, /* force */ true);
                        } // else if (405 < y && y < 500)
                    } // if (310 < x && x < 500)
                    break; // case STAT_DOUBT_WILD4

                case STAT_ASK_KEEP_PLAY:
                    if (310 < x && x < 500 && 405 < y && y < 500) {
                        // No button, keep the drawn card
                        mSelectedIdx = -1;
                        setStatus(STAT_IDLE);
                        refreshScreen(i18n.act_pass(Player.YOU));
                        threadWait(750);
                        setStatus(mUno.switchNow());
                    } // if (310 < x && x < 500 && 405 < y && y < 500)
                    else if (310 < x && x < 500 && 310 < y && y < 405) {
                        // YES button, play the drawn card
                        List<Card> hand = mUno.getHandCardsOf(Player.YOU);
                        Card card = hand.get(mSelectedIdx);

                        if (!card.isWild() || hand.size() < 2) {
                            play(mSelectedIdx, card.color);
                        } // if (!card.isWild() || hand.size() < 2)
                        else if (mUno.getStackRule() != 2 ||
                                card.content != Content.WILD_DRAW4) {
                            setStatus(STAT_WILD_COLOR);
                        } // else if (mUno.getStackRule() != 2 || ...)
                        else {
                            play(mSelectedIdx, mUno.lastColor());
                        } // else
                    } // else if (310 < x && x < 500 && 310 < y && y < 405)
                    else if (700 <= y && y <= 880) {
                        List<Card> hand = mUno.getHandCardsOf(Player.YOU);
                        int size = hand.size();
                        int width = 44 * size + 76;
                        int startX = 800 - width / 2;
                        Card card = hand.get(mSelectedIdx);
                        int index = Math.min((x - startX) / 44, size - 1);

                        if (!(startX <= x && x <= startX + width)) {
                            break; // case STAT_ASK_KEEP_PLAY
                        } // if (!(startX <= x && x <= startX + width))

                        if (index != mSelectedIdx) {
                            break; // case STAT_ASK_KEEP_PLAY
                        } // if (index != mSelectedIdx)

                        if (!card.isWild() || hand.size() < 2) {
                            play(mSelectedIdx, card.color);
                        } // if (!card.isWild() || hand.size() < 2)
                        else if (mUno.getStackRule() != 2 ||
                                card.content != Content.WILD_DRAW4) {
                            setStatus(STAT_WILD_COLOR);
                        } // else if (mUno.getStackRule() != 2 || ...)
                        else {
                            play(mSelectedIdx, mUno.lastColor());
                        } // else
                    } // else if (700 <= y && y <= 880)
                    break; // case STAT_ASK_KEEP_PLAY

                case STAT_SEVEN_TARGET:
                    if (288 < y && y < 366 && mUno.getPlayers() == 4) {
                        if (338 < x && x < 472) {
                            // North sector
                            swapWith(Player.COM2);
                        } // if (338 < x && x < 472)
                    } // if (288 < y && y < 366 && mUno.getPlayers() == 4)
                    else if (405 < y && y < 500) {
                        if (310 < x && x < 405) {
                            // West sector
                            swapWith(Player.COM1);
                        } // if (310 < x && x < 405)
                        else if (405 < x && x < 500) {
                            // East sector
                            swapWith(Player.COM3);
                        } // else if (405 < x && x < 500)
                    } // else if (405 < y && y < 500)
                    break; // case STAT_SEVEN_TARGET

                case STAT_GAME_OVER:
                    if (360 <= y && y <= 540 && 338 <= x && x <= 458) {
                        // Card deck area, start a new game
                        mGameSaved = false;
                        setStatus(STAT_NEW_GAME);
                    } // if (360 <= y && y <= 540 && 338 <= x && x <= 458)
                    else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                        // <OPTIONS> button
                        mAdjustOptions = true;
                        setStatus(mStatus);
                    } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
                    break; // case STAT_GAME_OVER

                default:
                    break; // default
            } // switch (mStatus)
        } // else

        return true;
    } // handleMessage2(Message)

    /**
     * Load a existed replay file.
     *
     * @param replayName Provide the file name of your replay.
     */
    @WorkerThread
    private void loadReplay(String replayName) {
        if (mUno.loadReplay(replayName)) {
            final int[] x = {740, 160, 740, 1320};
            final int[] y = {670, 360, 50, 360};
            int[] params = {0, 0, 0};

            setStatus(STAT_IDLE);
            while (true) {
                Card card;
                String cmd;
                int a, b, c, d, i, size, width;

                if ((cmd = mUno.forwardReplay(params)).equals("ST")) {
                    refreshScreen(i18n.info_ready());
                    threadWait(1000);
                } // if ((cmd = mUno.forwardReplay(params)).equals("ST"))
                else if (cmd.equals("DR")) {
                    List<Card> h = mUno.getHandCardsOf(a = params[0]);

                    card = mUno.findCardById(params[1]);
                    i = Collections.binarySearch(h, card);
                    size = h.size();
                    mLayer[0].startLeft = 338;
                    mLayer[0].startTop = 360;
                    mLayer[0].elem = card.image;
                    if (a == Player.COM1) {
                        width = 44 * Math.min(size, 13) + 136;
                        mLayer[0].endLeft = 20 + i / 13 * 44;
                        mLayer[0].endTop = 450 - width / 2 + i % 13 * 44;
                    } // if (a == Player.COM1)
                    else if (a == Player.COM2) {
                        width = 44 * size + 76;
                        mLayer[0].endLeft = 800 - width / 2 + 44 * i;
                        mLayer[0].endTop = 20;
                    } // else if (a == Player.COM2)
                    else if (a == Player.COM3) {
                        width = 44 * Math.min(size, 13) + 136;
                        mLayer[0].endLeft = 1460 - i / 13 * 44;
                        mLayer[0].endTop = 450 - width / 2 + i % 13 * 44;
                    } // else if (a == Player.COM3)
                    else {
                        width = 44 * size + 76;
                        mLayer[0].endLeft = 800 - width / 2 + 44 * i;
                        mLayer[0].endTop = 700;
                    } // else

                    // Animation
                    mSoundPool.play(sndDraw, mSndVol, mSndVol, 1, 0, 1.0f);
                    animate(1, mLayer);
                    refreshScreen(i18n.act_drawCard(a, card.name));
                    threadWait(300);
                } // else if (cmd.equals("DR"))
                else if (cmd.equals("PL")) {
                    List<Card> h = mUno.getHandCardsOf(a = params[0]);

                    card = mUno.findCardById(params[1]);
                    i = Collections.binarySearch(h, card);
                    if (i < 0) i = ~i;
                    size = h.size() + 1;
                    threadWait(750);
                    mSoundPool.play(sndPlay, mSndVol, mSndVol, 1, 0, 1.0f);
                    mLayer[0].elem = card.image;
                    if (a == Player.COM1) {
                        width = 44 * Math.min(size, 13) + 136;
                        mLayer[0].startLeft = 160 + i / 13 * 44;
                        mLayer[0].startTop = 450 - width / 2 + i % 13 * 44;
                    } // if (a == Player.COM1)
                    else if (a == Player.COM2) {
                        width = 44 * size + 76;
                        mLayer[0].startLeft = 800 - width / 2 + 44 * i;
                        mLayer[0].startTop = 50;
                    } // else if (a == Player.COM2)
                    else if (a == Player.COM3) {
                        width = 44 * Math.min(size, 13) + 136;
                        mLayer[0].startLeft = 1320 - i / 13 * 44;
                        mLayer[0].startTop = 450 - width / 2 + i % 13 * 44;
                    } // else if (a == Player.COM3)
                    else {
                        width = 44 * size + 76;
                        mLayer[0].startLeft = 800 - width / 2 + 44 * i;
                        mLayer[0].startTop = 680;
                    } // else

                    // Animation
                    mLayer[0].endLeft = 1118;
                    mLayer[0].endTop = 360;
                    animate(1, mLayer);
                    if (size == 2)
                        mSoundPool.play(sndUno, mSndVol, mSndVol, 1, 0, 1.0f);
                    refreshScreen(i18n.act_playCard(a, card.name));
                    threadWait(750);
                } // else if (cmd.equals("PL"))
                else if (cmd.equals("DF")) {
                    a = params[0];
                    refreshScreen(i18n.info_cannotDraw(a, Uno.MAX_HOLD_CARDS));
                    threadWait(750);
                } // else if (cmd.equals("DF"))
                else if (cmd.equals("CH")) {
                    a = params[0];
                    threadWait(750);
                    if (mUno.challenge(a)) {
                        refreshScreen(i18n.info_challengeSuccess(a));
                    } // if (mUno.challenge(a))
                    else {
                        b = mUno.getNext();
                        refreshScreen(i18n.info_challengeFailure(b));
                    } // else

                    threadWait(750);
                } // else if (cmd.equals("CH"))
                else if (cmd.equals("SW")) {
                    a = params[0];
                    b = params[1];

                    // Animation
                    mHideFlag = (1 << a) | (1 << b);
                    refreshScreen(i18n.info_7_swap(a, b));
                    mLayer[0].elem = mLayer[1].elem = mUno.getBackImage();
                    mLayer[0].startLeft = x[a];
                    mLayer[0].startTop = y[a];
                    mLayer[0].endLeft = x[b];
                    mLayer[0].endTop = y[b];
                    mLayer[1].startLeft = x[b];
                    mLayer[1].startTop = y[b];
                    mLayer[1].endLeft = x[a];
                    mLayer[1].endTop = y[a];
                    animate(2, mLayer);
                    mHideFlag = 0x00;
                    refreshScreen(i18n.info_7_swap(a, b));
                    threadWait(750);
                } // else if (cmd.equals("SW"))
                else if (cmd.equals("CY")) {
                    // Animation
                    mHideFlag = 0x0f;
                    refreshScreen(i18n.info_0_rotate());
                    a = mUno.getNow();
                    b = mUno.getNext();
                    c = mUno.getOppo();
                    d = mUno.getPrev();
                    mLayer[0].elem = mUno.getBackImage();
                    mLayer[0].startLeft = x[a];
                    mLayer[0].startTop = y[a];
                    mLayer[0].endLeft = x[b];
                    mLayer[0].endTop = y[b];
                    mLayer[1].elem = mUno.getBackImage();
                    mLayer[1].startLeft = x[b];
                    mLayer[1].startTop = y[b];
                    mLayer[1].endLeft = x[c];
                    mLayer[1].endTop = y[c];
                    if (mUno.getPlayers() == 3) {
                        mLayer[2].elem = mUno.getBackImage();
                        mLayer[2].startLeft = x[c];
                        mLayer[2].startTop = y[c];
                        mLayer[2].endLeft = x[a];
                        mLayer[2].endTop = y[a];
                        animate(3, mLayer);
                    } // if (mUno.getPlayers() == 3)
                    else {
                        mLayer[2].elem = mUno.getBackImage();
                        mLayer[2].startLeft = x[c];
                        mLayer[2].startTop = y[c];
                        mLayer[2].endLeft = x[d];
                        mLayer[2].endTop = y[d];
                        mLayer[3].elem = mUno.getBackImage();
                        mLayer[3].startLeft = x[d];
                        mLayer[3].startTop = y[d];
                        mLayer[3].endLeft = x[a];
                        mLayer[3].endTop = y[a];
                        animate(4, mLayer);
                    } // else

                    mHideFlag = 0x00;
                    refreshScreen(i18n.info_0_rotate());
                    threadWait(750);
                } // else if (cmd.equals("CY"))
                else {
                    break;
                } // else
            } // while (true)

            if ((mUno.is2vs2() && mUno.getHandCardsOf(Player.COM2).isEmpty()) ||
                    mUno.getHandCardsOf(Player.YOU).isEmpty()) {
                mSoundPool.play(sndWin, mSndVol, mSndVol, 1, 0, 1.0f);
            } // if ((mUno.is2vs2() && ...)
            else {
                mSoundPool.play(sndLose, mSndVol, mSndVol, 1, 0, 1.0f);
            } // else

            threadWait(3000);
            setStatus(STAT_WELCOME);
        } // if (mUno.loadReplay(replayName))
        else {
            refreshScreen("[Y]Failed to load " + replayName);
        } // else
    } // loadReplay(String)

    /**
     * Triggered when user pressed a system key.
     *
     * @param keyCode Which key is pressed, e.g. KeyEvent.KEYCODE_BACK
     * @param event   Detail of the key down event.
     * @return If the pressed key is BACK, return true because we have consumed
     * the key down event. Otherwise, follow the super method's steps.
     */
    @Override
    @UiThread
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Pop-up confirm dialog box when BACK key is pressed
            new ExitDialog().show(getSupportFragmentManager(), "ExitDialog");
            return true;
        } // if (keyCode == KeyEvent.KEYCODE_BACK)
        else {
            // Follow the super method's steps if another key is pressed
            return super.onKeyDown(keyCode, event);
        } // else
    } // onKeyDown(int, KeyEvent)

    /**
     * Triggered when activity loses focus.
     */
    @Override
    @UiThread
    protected void onPause() {
        if (OPENCV_INIT_SUCCESS) {
            getSharedPreferences("UnoStat", Context.MODE_PRIVATE)
                    .edit()
                    .putFloat("sndVol", mSndVol)
                    .putFloat("bgmVol", mBgmVol)
                    .putInt("players", mUno.getPlayers())
                    .putInt("score", Math.max(-999, mScore))
                    .putInt("difficulty", mUno.getDifficulty())
                    .putInt("forcePlayRule", mUno.getForcePlayRule())
                    .putBoolean("sevenZero", mUno.isSevenZeroRule())
                    .putInt("stackRule", mUno.getStackRule())
                    .putInt("initialCards", mUno.getInitialCards())
                    .putBoolean("2vs2", mUno.is2vs2())
                    .putInt("speed", mSpeed)
                    .apply();
            mSndVol = 0.0f;
            mMediaPlayer.pause();
        } // if (OPENCV_INIT_SUCCESS)

        super.onPause();
    } // onPause()

    /**
     * Triggered when activity destroyed.
     */
    @Override
    @UiThread
    protected void onDestroy() {
        if (OPENCV_INIT_SUCCESS) {
            mSoundPool.release();
            if (mSubHandler != null) {
                mSubHandler.removeCallbacksAndMessages(null);
                mSubHandler.getLooper().quit();
            } // if (mSubHandler != null)
        } // if (OPENCV_INIT_SUCCESS)

        mUIHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    } // onDestroy()

    /**
     * Unsupported Device Dialog.
     */
    public static class UnsupportedDeviceDialog extends DialogFragment
            implements DialogInterface.OnClickListener {
        /**
         * Triggered when dialog box created.
         *
         * @return Created dialog object.
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(requireActivity())
                    .setCancelable(false)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.dlgUnsupportedDeviceMsg)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(android.R.string.ok, this)
                    .create();
        } // onCreateDialog(Bundle)

        /**
         * Implementation method of interface DialogInterface.OnClickListener.
         * Triggered when a dialog button is pressed.
         *
         * @param dialog The button in which dialog box is pressed.
         * @param which  Which button is pressed, e.g.
         *               DialogInterface.BUTTON_POSITIVE
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
            requireActivity().finish();
        } // onClick(DialogInterface, int)
    } // UnsupportedDeviceDialog Inner Class

    public static class OpenDialog extends DialogFragment
            implements DialogInterface.OnClickListener {
        private WeakReference<MainActivity> parent;
        private String[] files;

        /**
         * Triggered when dialog box created.
         *
         * @return Created dialog object.
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            MainActivity p = (MainActivity) requireActivity();
            File replayDir = p.getExternalFilesDir("replay");

            parent = new WeakReference<>(p);
            files = replayDir != null ? replayDir.list() : new String[0];
            return new AlertDialog.Builder(p)
                    .setTitle("Choose a replay file")
                    .setItems(files, this)
                    .setIcon(android.R.drawable.ic_dialog_dialer)
                    .setNegativeButton(android.R.string.cancel, this)
                    .create();
        } // onCreateDialog(Bundle)

        /**
         * Implementation method of interface DialogInterface.OnClickListener.
         * Triggered when a dialog button is pressed.
         *
         * @param dialog The button in which dialog box is pressed.
         * @param which  Which button is pressed, e.g.
         *               DialogInterface.BUTTON_POSITIVE
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
            if (which >= 0) {
                MainActivity p = parent.get();
                Handler h = p == null ? null : p.mSubHandler;

                if (h != null) {
                    h.sendMessage(h.obtainMessage(1, files[which]));
                } // if (h != null)
            } // if (which >= 0)
        } // onClick(DialogInterface, int)
    } // OpenDialog Inner Class

    /**
     * Exit Dialog.
     */
    public static class ExitDialog extends DialogFragment
            implements DialogInterface.OnClickListener {
        /**
         * Triggered when dialog box created.
         *
         * @return Created dialog object.
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.dlgExitMsg)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .create();
        } // onCreateDialog(Bundle)

        /**
         * Implementation method of interface DialogInterface.OnClickListener.
         * Triggered when a dialog button is pressed.
         *
         * @param dialog The button in which dialog box is pressed.
         * @param which  Which button is pressed, e.g.
         *               DialogInterface.BUTTON_POSITIVE
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
            if (which == DialogInterface.BUTTON_POSITIVE) {
                // [OK] Button
                requireActivity().finish();
            } // if (which == DialogInterface.BUTTON_POSITIVE)
        } // onClick(DialogInterface, int)
    } // ExitDialog Inner Class
} // MainActivity Class

// E.O.F