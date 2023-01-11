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
            else {
                i18n = I18N.EN_US;
            } // else

            sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
            mScore = sp.getInt("score", 0);
            mBgmVol = sp.getFloat("bgmVol", 0.5f);
            mUno = Uno.getInstance(this);
            mUno.setPlayers(sp.getInt("players", 3));
            mUno.setDifficulty(sp.getInt("difficulty", Uno.LV_EASY));
            mUno.setForcePlay(sp.getBoolean("forcePlay", true));
            mUno.setSevenZeroRule(sp.getBoolean("sevenZero", false));
            mUno.setDraw2StackRule(sp.getBoolean("stackDraw2", false));
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
            mScr = Mat.zeros(900, 1600, CvType.CV_8UC3);
            mLayer = new AnimateLayer[]{
                    new AnimateLayer(),
                    new AnimateLayer(),
                    new AnimateLayer(),
                    new AnimateLayer()
            }; // new AnimateLayer[4]
            mBackup = new Mat[]{
                    Mat.zeros(900, 1600, CvType.CV_8UC3),
                    Mat.zeros(900, 1600, CvType.CV_8UC3),
                    Mat.zeros(900, 1600, CvType.CV_8UC3),
                    Mat.zeros(900, 1600, CvType.CV_8UC3)
            }; // new Mat[]{}
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
        mSubHandler = new Handler(Looper.myLooper(), this::handleMessage2);
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
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < millis) {
            mSubHandler.removeMessages(0);
        } // while (System.currentTimeMillis() - st < millis)
    } // threadWait(long)

    /**
     * AI Strategies (Difficulty: EASY).
     */
    @WorkerThread
    private void easyAI() {
        int idxBest;

        if (!mAIRunning) {
            mAIRunning = true;
            while (mStatus == Player.COM1
                    || mStatus == Player.COM2
                    || mStatus == Player.COM3
                    || (mStatus == Player.YOU && mAuto)) {
                setStatus(STAT_IDLE); // block tap down events when idle
                idxBest = mAI.easyAI_bestCardIndex4NowPlayer(mBestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, mBestColor[0]);
                } // if (idxBest >= 0)
                else {
                    // No appropriate cards to play, or no card to play
                    draw(1, /* force */ false);
                } // else
            } // while (mStatus == Player.COM1 || ...)

            mAIRunning = false;
        } // if (!mAIRunning)
    } // easyAI()

    /**
     * AI Strategies (Difficulty: HARD).
     */
    @WorkerThread
    private void hardAI() {
        int idxBest;

        if (!mAIRunning) {
            mAIRunning = true;
            while (mStatus == Player.COM1
                    || mStatus == Player.COM2
                    || mStatus == Player.COM3
                    || (mStatus == Player.YOU && mAuto)) {
                setStatus(STAT_IDLE); // block tap down events when idle
                idxBest = mAI.hardAI_bestCardIndex4NowPlayer(mBestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, mBestColor[0]);
                } // if (idxBest >= 0)
                else {
                    // No appropriate cards to play, or no card to play
                    draw(1, /* force */ false);
                } // else
            } // while (mStatus == Player.COM1 || ...)

            mAIRunning = false;
        } // if (!mAIRunning)
    } // hardAI()

    /**
     * Special AI strategies in 7-0 rule.
     */
    @WorkerThread
    private void sevenZeroAI() {
        int idxBest;

        if (!mAIRunning) {
            mAIRunning = true;
            while (mStatus == Player.COM1
                    || mStatus == Player.COM2
                    || mStatus == Player.COM3
                    || (mStatus == Player.YOU && mAuto)) {
                setStatus(STAT_IDLE); // block tap down events when idle
                idxBest = mAI.sevenZeroAI_bestCardIndex4NowPlayer(mBestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, mBestColor[0]);
                } // if (idxBest >= 0)
                else {
                    // No appropriate cards to play, or no card to play
                    draw(1, /* force */ false);
                } // else
            } // while (mStatus == Player.COM1 || ...)

            mAIRunning = false;
        } // if (!mAIRunning)
    } // sevenZeroAI()

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
                switch (mUno.getRecent().get(0).content) {
                    case DRAW2:
                        // If starting with a [+2], let dealer draw 2 cards.
                        draw(2, /* force */ true);
                        break; // case DRAW2

                    case SKIP:
                        // If starting with a [skip], skip dealer's turn.
                        refreshScreen(i18n.info_skipped(mUno.getNow()));
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
                } // switch (mUno.getRecent().get(0).content)
                break; // case STAT_NEW_GAME

            case Player.YOU:
                // Your turn, select a hand card to play, or draw a card
                if (mAuto) {
                    if (mUno.isSevenZeroRule()) {
                        sevenZeroAI();
                    } // if (mUno.isSevenZeroRule())
                    else if (mUno.getDifficulty() == Uno.LV_EASY) {
                        easyAI();
                    } // else if (mUno.getDifficulty() == Uno.LV_EASY)
                    else {
                        hardAI();
                    } // else
                } // if (mAuto)
                else if (mUno.legalCardsCount4NowPlayer() == 0) {
                    draw(1, /* force */ false);
                } // else if (mUno.legalCardsCount4NowPlayer() == 0)
                else if (mAdjustOptions) {
                    refreshScreen("");
                } // else if (mAdjustOptions)
                else {
                    List<Card> hand = mUno.getPlayer(Player.YOU).getHandCards();
                    if (hand.size() == 1) {
                        play(0, Color.NONE);
                    } // if (hand.size() == 1)
                    else if (mSelectedIdx < 0) {
                        int c = mUno.getDraw2StackCount();
                        refreshScreen(c == 0
                                ? i18n.info_yourTurn()
                                : i18n.info_yourTurn_stackDraw2(c));
                    } // else if (mSelectedIdx < 0)
                    else {
                        Card card = hand.get(mSelectedIdx);
                        refreshScreen(mUno.isLegalToPlay(card)
                                ? i18n.info_clickAgainToPlay(card.name)
                                : i18n.info_cannotPlay(card.name));
                    } // else
                } // else
                break; // case Player.YOU

            case STAT_WILD_COLOR:
                // Need to specify the following legal color after played a
                // wild card. Draw color sectors in the center of screen
                refreshScreen(i18n.ask_color());
                break; // case STAT_WILD_COLOR

            case STAT_DOUBT_WILD4:
                if (mAuto || mUno.getNext() != Player.YOU) {
                    // Challenge or not is decided by AI
                    if (mAI.needToChallenge()) {
                        onChallenge();
                    } // if (mAI->needToChallenge())
                    else {
                        mUno.switchNow();
                        draw(4, /* force */ true);
                    } // else
                } // if (mAuto || mUno.getNext() != Player.YOU)
                else {
                    // Challenge or not is decided by you
                    refreshScreen(i18n.ask_challenge(
                            mUno.next2lastColor().ordinal()));
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
                    refreshScreen(i18n.ask_target());
                } // else
                break; // case STAT_SEVEN_TARGET

            case Player.COM1:
            case Player.COM2:
            case Player.COM3:
                // AI players' turn
                if (mUno.isSevenZeroRule()) {
                    sevenZeroAI();
                } // if (mUno.isSevenZeroRule())
                else if (mUno.getDifficulty() == Uno.LV_EASY) {
                    easyAI();
                } // else if (mUno.getDifficulty() == Uno.LV_EASY)
                else {
                    hardAI();
                } // else
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
     * Refresh the screen display.
     *
     * @param message Extra message to show.
     */
    @WorkerThread
    private void refreshScreen(String message) {
        Mat image;
        Size axes;
        String lack;
        Point center;
        Color fontColor;
        List<Card> recent;
        List<Color> recentColors;
        int i, x, y, remain, size, status, used, width;

        // Lock the value of member [mStatus]
        status = mStatus;

        // Clear
        mUno.getBackground().copyTo(mScr);

        // Message area
        width = mUno.getFormatTextWidth(message);
        mUno.putFormatText(mScr, message, 800 - width / 2, 620);

        // Left-bottom corner: <OPTIONS> button
        // Shows only when game is not in process
        if (status == Player.YOU ||
                status == STAT_WELCOME ||
                status == STAT_GAME_OVER) {
            fontColor = mAdjustOptions ? Color.YELLOW : null;
            mUno.putText(mScr, i18n.btn_settings(), 20, 880, fontColor);
        } // if (status == Player.YOU || ...)

        // Right-bottom corner: <AUTO> button
        if (status == Player.YOU && !mAuto && !mAdjustOptions) {
            width = mUno.getTextWidth(i18n.btn_auto());
            mUno.putText(mScr, i18n.btn_auto(), 1580 - width, 880, null);
        } // if (status == Player.YOU && !mAuto && !mAdjustOptions)

        if (mAdjustOptions) {
            // Show special screen when configuring game options
            // BGM switch
            mUno.putText(mScr, i18n.label_bgm(), 60, 160, null);
            image = mBgmVol > 0.0f ?
                    mUno.findCard(Color.RED, Content.SKIP).darkImg :
                    mUno.findCard(Color.RED, Content.SKIP).image;
            image.copyTo(mScr.submat(60, 241, 150, 271), image);
            image = mBgmVol > 0.0f ?
                    mUno.findCard(Color.GREEN, Content.REV).image :
                    mUno.findCard(Color.GREEN, Content.REV).darkImg;
            image.copyTo(mScr.submat(60, 241, 330, 451), image);

            // Sound effect switch
            mUno.putText(mScr, i18n.label_snd(), 60, 350, null);
            image = mSndVol > 0.0f ?
                    mUno.findCard(Color.RED, Content.SKIP).darkImg :
                    mUno.findCard(Color.RED, Content.SKIP).image;
            image.copyTo(mScr.submat(250, 431, 150, 271), image);
            image = mSndVol > 0.0f ?
                    mUno.findCard(Color.GREEN, Content.REV).image :
                    mUno.findCard(Color.GREEN, Content.REV).darkImg;
            image.copyTo(mScr.submat(250, 431, 330, 451), image);

            if (status != Player.YOU) {
                // [Level] option: easy / hard
                mUno.putText(mScr, i18n.label_level(), 780, 160, null);
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_EASY,
                        /* hiLight */ !mUno.isSevenZeroRule() &&
                                mUno.getDifficulty() == Uno.LV_EASY
                ); // image = mUno.getLevelImage()
                image.copyTo(mScr.submat(60, 241, 930, 1051), image);
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_HARD,
                        /* hiLight */ !mUno.isSevenZeroRule() &&
                                mUno.getDifficulty() == Uno.LV_HARD
                ); // image = mUno.getLevelImage()
                image.copyTo(mScr.submat(60, 241, 1110, 1231), image);

                // [Players] option: 3 / 4 / 2vs2
                mUno.putText(mScr, i18n.label_players(), 780, 350, null);
                image = mUno.getPlayers() == 3 ?
                        mUno.findCard(Color.GREEN, Content.NUM3).image :
                        mUno.findCard(Color.GREEN, Content.NUM3).darkImg;
                image.copyTo(mScr.submat(250, 431, 930, 1051), image);
                image = mUno.getPlayers() == 4 ?
                        mUno.findCard(Color.YELLOW, Content.NUM4).image :
                        mUno.findCard(Color.YELLOW, Content.NUM4).darkImg;
                image.copyTo(mScr.submat(250, 431, 1110, 1231), image);
                image = mUno.get2vs2Image();
                image.copyTo(mScr.submat(250, 431, 1290, 1411), image);

                // Rule settings
                // Initial Cards
                mUno.putText(mScr, i18n.label_initialCards(), 60, 670, null);
                width = mUno.getInitialCards();
                lack = "" + width / 10 + width % 10;
                mUno.putText(mScr, "< " + lack + " >", 1110, 670, null);

                // Force play switch
                mUno.putText(mScr, i18n.label_forcePlay(), 60, 720, null);
                fontColor = mUno.isForcePlay() ? null : Color.RED;
                mUno.putText(mScr, i18n.btn_keep(), 1110, 720, fontColor);
                fontColor = mUno.isForcePlay() ? Color.GREEN : null;
                mUno.putText(mScr, i18n.btn_play(), 1290, 720, fontColor);

                // 7-0
                mUno.putText(mScr, i18n.label_7_0(), 60, 770, null);
                fontColor = mUno.isSevenZeroRule() ? null : Color.RED;
                mUno.putText(mScr, i18n.btn_off(), 1110, 770, fontColor);
                fontColor = mUno.isSevenZeroRule() ? Color.GREEN : null;
                mUno.putText(mScr, i18n.btn_on(), 1290, 770, fontColor);

                // +2 stack
                mUno.putText(mScr, i18n.label_draw2Stack(), 60, 820, null);
                fontColor = mUno.isDraw2StackRule() ? null : Color.RED;
                mUno.putText(mScr, i18n.btn_off(), 1110, 820, fontColor);
                fontColor = mUno.isDraw2StackRule() ? Color.GREEN : null;
                mUno.putText(mScr, i18n.btn_on(), 1290, 820, fontColor);
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
            width = mUno.getTextWidth(i18n.label_score());
            mUno.putText(mScr, i18n.label_score(), 500 - width, 800, null);
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
        image = mUno.getBackImage();
        image.copyTo(mScr.submat(360, 541, 338, 459), image);
        recentColors = mUno.getRecentColors();
        recent = mUno.getRecent();
        size = recent.size();
        width = 44 * size + 76;
        for (i = 0, x = 1112 - width / 2; i < size; ++i, x += 44) {
            if (recent.get(i).content == Content.WILD) {
                image = mUno.getColoredWildImage(recentColors.get(i));
            } // if (recent.get(i).content == Content.WILD)
            else if (recent.get(i).content == Content.WILD_DRAW4) {
                image = mUno.getColoredWildDraw4Image(recentColors.get(i));
            } // else if (recent.get(i).content == Content.WILD_DRAW4)
            else {
                image = recent.get(i).image;
            } // else

            image.copyTo(mScr.submat(360, 541, x, x + 121), image);
        } // for (i = 0, x = 1112 - width / 2; i < size; ++i, x += 44)

        // Left-top corner: remain / used
        remain = mUno.getDeckCount();
        used = mUno.getUsedCount();
        mUno.putText(mScr, i18n.label_remain_used(remain, used), 20, 42, null);

        // Right-top corner: lacks
        lack = "LACK: ";
        switch (mUno.getPlayer(Player.COM2).getWeakColor()) {
            case RED:
                lack += "[R]N";
                break; // case RED

            case BLUE:
                lack += "[B]N";
                break; // case BLUE

            case GREEN:
                lack += "[G]N";
                break; // case GREEN

            case YELLOW:
                lack += "[Y]N";
                break; // case YELLOW

            default:
                lack += "[W]N";
                break; // default
        } // switch (mUno.getPlayer(Player.COM2).getWeakColor())

        switch (mUno.getPlayer(Player.COM3).getWeakColor()) {
            case RED:
                lack += "[R]E";
                break; // case RED

            case BLUE:
                lack += "[B]E";
                break; // case BLUE

            case GREEN:
                lack += "[G]E";
                break; // case GREEN

            case YELLOW:
                lack += "[Y]E";
                break; // case YELLOW

            default:
                lack += "[W]E";
                break; // default
        } // switch (mUno.getPlayer(Player.COM3).getWeakColor())

        switch (mUno.getPlayer(Player.COM1).getWeakColor()) {
            case RED:
                lack += "[R]W";
                break; // case RED

            case BLUE:
                lack += "[B]W";
                break; // case BLUE

            case GREEN:
                lack += "[G]W";
                break; // case GREEN

            case YELLOW:
                lack += "[Y]W";
                break; // case YELLOW

            default:
                lack += "[W]W";
                break; // default
        } // switch (mUno.getPlayer(Player.COM1).getWeakColor())

        switch (mUno.getPlayer(Player.YOU).getWeakColor()) {
            case RED:
                lack += "[R]S";
                break; // case RED

            case BLUE:
                lack += "[B]S";
                break; // case BLUE

            case GREEN:
                lack += "[G]S";
                break; // case GREEN

            case YELLOW:
                lack += "[Y]S";
                break; // case YELLOW

            default:
                lack += "[W]S";
                break; // default
        } // switch (mUno.getPlayer(Player.YOU).getWeakColor())

        width = mUno.getFormatTextWidth(lack);
        mUno.putFormatText(mScr, lack, 1580 - width, 42);

        // Left-center: Hand cards of Player West (COM1)
        if (status == STAT_GAME_OVER && mWinner == Player.COM1) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 80 - width / 2, 461, Color.YELLOW);
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
                width = mUno.getTextWidth("UNO");
                mUno.putText(mScr, "UNO", 80 - width / 2, 584, Color.YELLOW);
            } // if (size == 1)
        } // else if (((mHideFlag >> 1) & 0x01) == 0x00)

        // Top-center: Hand cards of Player North (COM2)
        if (status == STAT_GAME_OVER && mWinner == Player.COM2) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 800 - width / 2, 121, Color.YELLOW);
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
                width = mUno.getTextWidth("UNO");
                mUno.putText(mScr, "UNO", 720 - width, 121, Color.YELLOW);
            } // if (size == 1)
        } // else if (((mHideFlag >> 2) & 0x01) == 0x00)

        // Right-center: Hand cards of Player East (COM3)
        if (status == STAT_GAME_OVER && mWinner == Player.COM3) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 1520 - width / 2, 461, Color.YELLOW);
        } // if (status == STAT_GAME_OVER && mWinner == Player.COM3)
        else if (((mHideFlag >> 3) & 0x01) == 0x00) {
            Player p = mUno.getPlayer(Player.COM3);
            List<Card> hand = p.getHandCards();
            size = hand.size();
            width = 44 * Math.min(size, 13) + 136;
            for (i = 0; i < size; ++i) {
                x = 1460 - i / 13 * 44;
                y = 450 - width / 2 + i % 13 * 44;
                image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                image.copyTo(mScr.submat(y, y + 181, x, x + 121), image);
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = mUno.getTextWidth("UNO");
                mUno.putText(mScr, "UNO", 1520 - width / 2, 584, Color.YELLOW);
            } // if (size == 1)
        } // else if (((mHideFlag >> 3) & 0x01) == 0x00)

        // Bottom: Your hand cards
        if (status == STAT_GAME_OVER && mWinner == Player.YOU) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 800 - width / 2, 801, Color.YELLOW);
        } // if (status == STAT_GAME_OVER && mWinner == Player.YOU)
        else if ((mHideFlag & 0x01) == 0x00) {
            // Show your all hand cards
            List<Card> hand = mUno.getPlayer(Player.YOU).getHandCards();
            size = hand.size();
            width = 44 * size + 76;
            for (i = 0, x = 800 - width / 2; i < size; ++i, x += 44) {
                Card card = hand.get(i);
                image = status == STAT_GAME_OVER
                        || (status == Player.YOU
                        && mUno.isLegalToPlay(card))
                        ? card.image : card.darkImg;
                y = i == mSelectedIdx ? 680 : 700;
                image.copyTo(mScr.submat(y, y + 181, x, x + 121), image);
            } // for (i = 0, x = 800 - width / 2; i < size; ++i, x += 44)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                mUno.putText(mScr, "UNO", 880, 801, Color.YELLOW);
            } // if (size == 1)
        } // else if ((mHideFlag & 0x01) == 0x00)

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
                width = mUno.getTextWidth(i18n.label_yes());
                mUno.putText(
                        /* img   */ mScr,
                        /* text  */ i18n.label_yes(),
                        /* x     */ 405 - width / 2,
                        /* y     */ 358,
                        /* color */ null
                ); // mUno.putText()

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
                width = mUno.getTextWidth(i18n.label_no());
                mUno.putText(
                        /* img   */ mScr,
                        /* text  */ i18n.label_no(),
                        /* x     */ 405 - width / 2,
                        /* y     */ 472,
                        /* color */ null
                ); // mUno.putText()
                break; // case STAT_DOUBT_WILD4

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
                width = mUno.getTextWidth("W");
                mUno.putText(mScr, "W", 338 - width / 2, 440, null);

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
                width = mUno.getTextWidth("E");
                mUno.putText(mScr, "E", 472 - width / 2, 440, null);

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
                width = mUno.getTextWidth("N");
                mUno.putText(mScr, "N", 405 - width / 2, 360, null);
                break; // case STAT_SEVEN_TARGET

            default:
                break; // default
        } // switch (status)

        // Show screen
        Utils.matToBitmap(mScr, mBmp);
        mUIHandler.sendEmptyMessage(0); // -> handleMessage()
    } // refreshScreen(String)

    /**
     * Draw [mBmp] on the screen. Called by system.
     */
    @Override
    @UiThread
    public boolean handleMessage(Message message) {
        mImgScreen.setImageBitmap(mBmp);
        return true;
    } // handleMessage(Message)

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
        refreshScreen(i18n.info_0_rotate());
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
        refreshScreen(i18n.info_0_rotate());
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
        int curr;

        setStatus(STAT_IDLE);
        curr = mUno.getNow();
        mHideFlag = (1 << curr) | (1 << whom);
        refreshScreen(i18n.info_7_swap(curr, whom));
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
        refreshScreen(i18n.info_7_swap(curr, whom));
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
        int c, now, size, width, recentSize, next;

        setStatus(STAT_IDLE); // block tap down events when idle
        now = mUno.getNow();
        size = mUno.getCurrPlayer().getHandSize();
        card = mUno.play(now, index, color);
        mSelectedIdx = -1;
        mSoundPool.play(sndPlay, mSndVol, mSndVol, 1, 0, 1.0f);
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

            recentSize = mUno.getRecent().size();
            mLayer[0].endLeft = 22 * recentSize + 1030;
            mLayer[0].endTop = 360;
            animate(1, mLayer);
            if (size == 1) {
                // The player in action becomes winner when it played the
                // final card in its hand successfully
                if (now == Player.YOU) {
                    mDiff = mUno.getPlayer(Player.COM1).getHandScore() +
                            mUno.getPlayer(Player.COM2).getHandScore() +
                            mUno.getPlayer(Player.COM3).getHandScore();
                    mScore = Math.min(9999, 200 + mScore + mDiff);
                    mSoundPool.play(sndWin, mSndVol, mSndVol, 1, 0, 1.0f);
                } // if (now == Player.YOU)
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
                if (size == 2) {
                    mSoundPool.play(sndUno, mSndVol, mSndVol, 1, 0, 1.0f);
                } // if (size == 2)

                switch (card.content) {
                    case DRAW2:
                        next = mUno.switchNow();
                        if (mUno.isDraw2StackRule()) {
                            c = mUno.getDraw2StackCount();
                            refreshScreen(i18n.act_playDraw2(now, next, c));
                            threadWait(1500);
                            setStatus(next);
                        } // if (mUno.isDraw2StackRule())
                        else {
                            refreshScreen(i18n.act_playDraw2(now, next, 2));
                            threadWait(1500);
                            draw(2, /* force */ true);
                        } // else
                        break; // case DRAW2

                    case SKIP:
                        next = mUno.switchNow();
                        refreshScreen(i18n.act_playSkip(now, next));
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
                        refreshScreen(i18n.act_playWild(now, color.ordinal()));
                        threadWait(1500);
                        setStatus(mUno.switchNow());
                        break; // case WILD

                    case WILD_DRAW4:
                        next = mUno.getNext();
                        refreshScreen(i18n.act_playWildDraw4(now, next));
                        threadWait(1500);
                        setStatus(STAT_DOUBT_WILD4);
                        break; // case WILD_DRAW4

                    case NUM7:
                        if (mUno.isSevenZeroRule()) {
                            refreshScreen(i18n.act_playCard(now, card.name));
                            threadWait(750);
                            setStatus(STAT_SEVEN_TARGET);
                            break; // case NUM7
                        } // if (mUno.isSevenZeroRule())
                        // else fall through

                    case NUM0:
                        if (mUno.isSevenZeroRule()) {
                            refreshScreen(i18n.act_playCard(now, card.name));
                            threadWait(750);
                            cycle();
                            break; // case NUM0
                        } // if (mUno.isSevenZeroRule())
                        // else fall through

                    default:
                        refreshScreen(i18n.act_playCard(now, card.name));
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
        int i, index, c, now, size, width;

        setStatus(STAT_IDLE); // block tap down events when idle
        c = mUno.getDraw2StackCount();
        if (c > 0) {
            count = c;
            force = true;
        } // if (c > 0)

        index = -1;
        drawn = null;
        now = mUno.getNow();
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
                refreshScreen(message);
                threadWait(300);
            } // if (index >= 0)
            else {
                message = i18n.info_cannotDraw(now, Uno.MAX_HOLD_CARDS);
                refreshScreen(message);
                break;
            } // else
        } // for (i = 0; i < count; ++i)

        threadWait(750);
        if (count == 1 &&
                drawn != null &&
                mUno.isForcePlay() &&
                mUno.isLegalToPlay(drawn)) {
            // Player drew one card by itself, the drawn card
            // can be played immediately if it's legal to play
            if (!drawn.isWild()) {
                play(index, Color.NONE);
            } // if (!drawn.isWild())
            else if (mAuto || now != Player.YOU) {
                play(index, mAI.calcBestColor4NowPlayer());
            } // else if (mAuto || now != Player.YOU)
            else {
                // Store index value as global value. This value
                // will be used after the wild color determined.
                mSelectedIdx = index;
                setStatus(STAT_WILD_COLOR);
            } // else
        } // if (count == 1 && ...)
        else {
            refreshScreen(i18n.act_pass(now));
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
        refreshScreen(i18n.info_challenge(
                challenger, now, mUno.next2lastColor().ordinal()));
        threadWait(1500);
        if (challengeSuccess) {
            // Challenge success, who played [wild +4] draws 4 cards
            refreshScreen(i18n.info_challengeSuccess(now));
            threadWait(1500);
            draw(4, /* force */ true);
        } // if (challengeSuccess)
        else {
            // Challenge failure, challenger draws 6 cards
            refreshScreen(i18n.info_challengeFailure(challenger));
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

        if (mAdjustOptions) {
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
                else if (930 <= x && x <= 1050 && mStatus != Player.YOU) {
                    // 3 players
                    mUno.setPlayers(3);
                    setStatus(mStatus);
                } // else if (930 <= x && x <= 1050 && mStatus != Player.YOU)
                else if (1110 <= x && x <= 1230 && mStatus != Player.YOU) {
                    // 4 players
                    mUno.setPlayers(4);
                    setStatus(mStatus);
                } // else if (1110 <= x && x <= 1230 && mStatus != Player.YOU)
            } // else if (270 <= y && y <= 450)
            else if (649 <= y && y <= 670 && mStatus != Player.YOU) {
                if (1030 <= x && x <= 1130) {
                    // Decrease initial cards
                    mUno.decreaseInitialCards();
                    setStatus(mStatus);
                } // if (1030 <= x && x <= 1130)
                else if (1187 <= x && x <= 1287) {
                    // Increase initial cards
                    mUno.increaseInitialCards();
                    setStatus(mStatus);
                } // else if (1187 <= x && x <= 1287)
            } // else if (649 <= y && y <= 670 && mStatus != Player.YOU)
            else if (699 <= y && y <= 720 && mStatus != Player.YOU) {
                if (1110 <= x && x <= 1237) {
                    // Force play, <KEEP> button
                    mUno.setForcePlay(false);
                    setStatus(mStatus);
                } // if (1110 <= x && x <= 1237)
                else if (1290 <= x && x <= 1414) {
                    // Force play, <PLAY> button
                    mUno.setForcePlay(true);
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1414)
            } // else if (699 <= y && y <= 720 && mStatus != Player.YOU)
            else if (749 <= y && y <= 770 && mStatus != Player.YOU) {
                if (1110 <= x && x <= 1216) {
                    // 7-0, <OFF> button
                    mUno.setSevenZeroRule(false);
                    setStatus(mStatus);
                } // if (1110 <= x && x <= 1216)
                else if (1290 <= x && x <= 1382) {
                    // 7-0, <ON> button
                    mUno.setSevenZeroRule(true);
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1382)
            } // else if (749 <= y && y <= 770 && mStatus != Player.YOU)
            else if (799 <= y && y <= 820 && mStatus != Player.YOU) {
                if (1110 <= x && x <= 1216) {
                    // +2 stack, <OFF> button
                    mUno.setDraw2StackRule(false);
                    setStatus(mStatus);
                } // if (1110 <= x && x <= 1216)
                else if (1290 <= x && x <= 1382) {
                    // +2 stack, <ON> button
                    mUno.setDraw2StackRule(true);
                    setStatus(mStatus);
                } // else if (1290 <= x && x <= 1382)
            } // else if (799 <= y && y <= 820 && mStatus != Player.YOU)
            else if (859 <= y && y <= 880 && 20 <= x && x <= 200) {
                // <OPTIONS> button
                // Leave options page
                mAdjustOptions = false;
                setStatus(mStatus);
            } // else if (859 <= y && y <= 880 && 20 <= x && x <= 200)
        } // if (mAdjustOptions)
        else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580) {
            // <AUTO> button
            // In player's action, automatically play or draw cards by AI
            if (mStatus == Player.YOU) {
                mAuto = true;
                setStatus(mStatus);
            } // if (mStatus == Player.YOU)
        } // else if (859 <= y && y <= 880 && 1450 <= x && x <= 1580)
        else {
            switch (mStatus) {
                case STAT_WELCOME:
                    if (360 <= y && y <= 540 && 740 <= x && x <= 860) {
                        // UNO button, start a new game
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
                        Player now = mUno.getPlayer(Player.YOU);
                        List<Card> hand = now.getHandCards();
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
                                if (card.isWild() && size > 1) {
                                    setStatus(STAT_WILD_COLOR);
                                } // if (card.isWild() && size > 1)
                                else {
                                    play(index, Color.NONE);
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
        SharedPreferences sp;
        SharedPreferences.Editor editor;

        if (OPENCV_INIT_SUCCESS) {
            sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
            editor = sp.edit();
            editor.putFloat("sndVol", mSndVol);
            editor.putFloat("bgmVol", mBgmVol);
            editor.putInt("players", mUno.getPlayers());
            editor.putInt("score", Math.max(-999, mScore));
            editor.putInt("difficulty", mUno.getDifficulty());
            editor.putBoolean("forcePlay", mUno.isForcePlay());
            editor.putBoolean("sevenZero", mUno.isSevenZeroRule());
            editor.putBoolean("stackDraw2", mUno.isDraw2StackRule());
            editor.apply();
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