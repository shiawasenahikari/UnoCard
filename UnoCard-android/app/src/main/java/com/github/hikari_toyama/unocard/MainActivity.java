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
import org.opencv.core.Rect;
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
        implements Runnable, View.OnTouchListener {
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
    private int CLOSED_FLAG = 0x00000000;
    private MediaPlayer mMediaPlayer;
    private boolean mAdjustOptions;
    private SoundPool mSoundPool;
    private ImageView mImgScreen;
    private boolean mAIRunning;
    private Handler mHandler;
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
    private int mScore;
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
        mHandler = new Handler();
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
            mAI = AI.getInstance(mUno);
            mAdjustOptions = false;
            mWinner = Player.YOU;
            mAIRunning = false;
            mSelectedIdx = -1;
            mHideFlag = 0x00;
            mAuto = false;
            mScr = Mat.zeros(720, 1280, CvType.CV_8UC3);
            mBackup = new Mat[]{
                    Mat.zeros(720, 1280, CvType.CV_8UC3),
                    Mat.zeros(720, 1280, CvType.CV_8UC3),
                    Mat.zeros(720, 1280, CvType.CV_8UC3),
                    Mat.zeros(720, 1280, CvType.CV_8UC3)
            }; // new Mat[]{}
            mBmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
            mImgScreen = findViewById(R.id.imgMainScreen);
            new Thread(() -> setStatus(STAT_WELCOME)).start();
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
     * Call this method to avoid from writing the complex code
     * <code>
     * try { Thread.sleep(milliSeconds); }
     * catch (InterruptedException e) { e.printStackTrace(); }
     * </code>
     * again and again.
     *
     * @param millis How many milli seconds to sleep.
     */
    @WorkerThread
    private void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } // try
        catch (InterruptedException e) {
            e.printStackTrace();
        } // catch (InterruptedException e)
    } // threadSleep(int)

    /**
     * AI Strategies (Difficulty: EASY).
     */
    @WorkerThread
    private void easyAI() {
        int idxBest;
        Color[] bestColor;

        if (!mAIRunning) {
            mAIRunning = true;
            while (mStatus == Player.COM1
                    || mStatus == Player.COM2
                    || mStatus == Player.COM3
                    || (mStatus == Player.YOU && mAuto)) {
                setStatus(STAT_IDLE); // block tap down events when idle
                bestColor = new Color[1];
                idxBest = mAI.easyAI_bestCardIndex4NowPlayer(bestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, bestColor[0]);
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
        Color[] bestColor;

        if (!mAIRunning) {
            mAIRunning = true;
            while (mStatus == Player.COM1
                    || mStatus == Player.COM2
                    || mStatus == Player.COM3
                    || (mStatus == Player.YOU && mAuto)) {
                setStatus(STAT_IDLE); // block tap down events when idle
                bestColor = new Color[1];
                idxBest = mAI.hardAI_bestCardIndex4NowPlayer(bestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, bestColor[0]);
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
     * AI Strategies (Difficulty: EASY).
     */
    @WorkerThread
    private void sevenZeroAI() {
        int idxBest;
        Color[] bestColor;

        if (!mAIRunning) {
            mAIRunning = true;
            while (mStatus == Player.COM1
                    || mStatus == Player.COM2
                    || mStatus == Player.COM3
                    || (mStatus == Player.YOU && mAuto)) {
                setStatus(STAT_IDLE); // block tap down events when idle
                bestColor = new Color[1];
                idxBest = mAI.sevenZeroAI_bestCardIndex4NowPlayer(bestColor);
                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    play(idxBest, bestColor[0]);
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
     * @param status New status value. Only 31 low bits are available.
     */
    @WorkerThread
    private void setStatus(int status) {
        switch ((mStatus = (status & 0x7fffffff) | CLOSED_FLAG)) {
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
                mUno.start();
                mSelectedIdx = -1;
                refreshScreen(i18n.info_ready());
                threadSleep(2000);
                switch (mUno.getRecent().get(0).content) {
                    case DRAW2:
                        // If starting with a [+2], let dealer draw 2 cards.
                        draw(2, /* force */ true);
                        break; // case DRAW2

                    case SKIP:
                        // If starting with a [skip], skip dealer's turn.
                        refreshScreen(i18n.info_skipped(mUno.getNow()));
                        threadSleep(1500);
                        setStatus(mUno.switchNow());
                        break; // case SKIP

                    case REV:
                        // If starting with a [reverse], change the action
                        // sequence to COUNTER CLOCKWISE.
                        mUno.switchDirection();
                        refreshScreen(i18n.info_dirChanged());
                        threadSleep(1500);
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
                    refreshScreen(i18n.info_gameOver(mScore));
                    if (mAuto && !mAdjustOptions) {
                        threadSleep(5000);
                        if (mAuto && !mAdjustOptions
                                && mStatus == STAT_GAME_OVER) {
                            setStatus(STAT_NEW_GAME);
                        } // if (mAuto && ...)
                    } // if (mAuto && !mAdjustOptions)
                } // else
                break; // case STAT_GAME_OVER

            default:
                break; // default
        } // switch ((mStatus = (status & 0x7fffffff) | CLOSED_FLAG))
    } // setStatus(int)

    /**
     * Refresh the screen display.
     *
     * @param message Extra message to show.
     */
    @WorkerThread
    private void refreshScreen(String message) {
        Rect roi;
        Mat image;
        Size axes;
        String info;
        Point center;
        Color fontColor;
        List<Card> recent;
        List<Color> recentColors;
        int i, remain, size, status, used, width;

        // Lock the value of member [mStatus]
        status = mStatus;

        // Clear
        mUno.getBackground().copyTo(mScr);

        // Message area
        width = mUno.getTextWidth(message);
        mUno.putText(mScr, message, 640 - width / 2, 487, null);

        // Right-bottom corner: <AUTO> button
        fontColor = mAuto ? Color.YELLOW : null;
        width = mUno.getTextWidth(i18n.btn_auto());
        mUno.putText(mScr, i18n.btn_auto(), 1260 - width, 700, fontColor);

        // Left-bottom corner: <OPTIONS> button
        // Shows only when game is not in process
        if (status == STAT_WELCOME || status == STAT_GAME_OVER) {
            fontColor = mAdjustOptions ? Color.YELLOW : null;
            mUno.putText(mScr, i18n.btn_settings(), 20, 700, fontColor);
        } // if (status == STAT_WELCOME || status == STAT_GAME_OVER)

        if (mAdjustOptions) {
            // Show special screen when configuring game options
            // BGM switch
            mUno.putText(mScr, i18n.label_bgm(), 60, 160, null);
            image = mBgmVol > 0.0f ?
                    mUno.findCard(Color.RED, Content.SKIP).darkImg :
                    mUno.findCard(Color.RED, Content.SKIP).image;
            roi = new Rect(150, 60, 121, 181);
            image.copyTo(new Mat(mScr, roi), image);
            image = mBgmVol > 0.0f ?
                    mUno.findCard(Color.GREEN, Content.REV).image :
                    mUno.findCard(Color.GREEN, Content.REV).darkImg;
            roi.x = 330;
            image.copyTo(new Mat(mScr, roi), image);

            // Sound effect switch
            mUno.putText(mScr, i18n.label_snd(), 60, 350, null);
            image = mSndVol > 0.0f ?
                    mUno.findCard(Color.RED, Content.SKIP).darkImg :
                    mUno.findCard(Color.RED, Content.SKIP).image;
            roi.x = 150;
            roi.y = 250;
            image.copyTo(new Mat(mScr, roi), image);
            image = mSndVol > 0.0f ?
                    mUno.findCard(Color.GREEN, Content.REV).image :
                    mUno.findCard(Color.GREEN, Content.REV).darkImg;
            roi.x = 330;
            image.copyTo(new Mat(mScr, roi), image);

            // [Level] option: easy / hard
            mUno.putText(mScr, i18n.label_level(), 640, 160, null);
            if (mUno.isSevenZeroRule()) {
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_EASY,
                        /* hiLight */ false
                ); // image = mUno.getLevelImage()
            } // if (mUno.isSevenZeroRule())
            else {
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_EASY,
                        /* hiLight */ mUno.getDifficulty() == Uno.LV_EASY
                ); // image = mUno.getLevelImage()
            } // else

            roi.x = 790;
            roi.y = 60;
            image.copyTo(new Mat(mScr, roi), image);
            if (mUno.isSevenZeroRule()) {
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_HARD,
                        /* hiLight */ false
                ); // image = mUno.getLevelImage()
            } // if (mUno.isSevenZeroRule())
            else {
                image = mUno.getLevelImage(
                        /* level   */ Uno.LV_HARD,
                        /* hiLight */ mUno.getDifficulty() == Uno.LV_HARD
                ); // image = mUno.getLevelImage()
            } // else
            roi.x = 970;
            image.copyTo(new Mat(mScr, roi), image);

            // [Players] option: 3 / 4
            mUno.putText(mScr, i18n.label_players(), 640, 350, null);
            image = mUno.getPlayers() == 3 ?
                    mUno.findCard(Color.GREEN, Content.NUM3).image :
                    mUno.findCard(Color.GREEN, Content.NUM3).darkImg;
            roi.x = 790;
            roi.y = 250;
            image.copyTo(new Mat(mScr, roi), image);
            image = mUno.getPlayers() == 4 ?
                    mUno.findCard(Color.YELLOW, Content.NUM4).image :
                    mUno.findCard(Color.YELLOW, Content.NUM4).darkImg;
            roi.x = 970;
            image.copyTo(new Mat(mScr, roi), image);

            // Rule settings
            // Force play switch
            mUno.putText(mScr, i18n.label_forcePlay(), 60, 540, null);
            fontColor = mUno.isForcePlay() ? null : Color.RED;
            mUno.putText(mScr, i18n.btn_keep(), 790, 540, fontColor);
            fontColor = mUno.isForcePlay() ? Color.GREEN : null;
            mUno.putText(mScr, i18n.btn_play(), 970, 540, fontColor);

            // 7-0
            mUno.putText(mScr, i18n.label_7_0(), 60, 590, null);
            fontColor = mUno.isSevenZeroRule() ? null : Color.RED;
            mUno.putText(mScr, i18n.btn_off(), 790, 590, fontColor);
            fontColor = mUno.isSevenZeroRule() ? Color.GREEN : null;
            mUno.putText(mScr, i18n.btn_on(), 970, 590, fontColor);

            // +2 stack
            mUno.putText(mScr, i18n.label_draw2Stack(), 60, 640, null);
            fontColor = mUno.isDraw2StackRule() ? null : Color.RED;
            mUno.putText(mScr, i18n.btn_off(), 790, 640, fontColor);
            fontColor = mUno.isDraw2StackRule() ? Color.GREEN : null;
            mUno.putText(mScr, i18n.btn_on(), 970, 640, fontColor);

            // Show image
            Utils.matToBitmap(mScr, mBmp);
            mHandler.post(this); // -> run()
            return;
        } // if (mAdjustOptions)

        if (status == STAT_WELCOME) {
            // For welcome screen, show the start button and your score
            image = mUno.getBackImage();
            roi = new Rect(580, 270, 121, 181);
            image.copyTo(new Mat(mScr, roi), image);
            width = mUno.getTextWidth(i18n.label_score());
            mUno.putText(mScr, i18n.label_score(), 340 - width, 620, null);
            if (mScore < 0) {
                image = mUno.getColoredWildImage(Color.NONE);
            } // if (mScore < 0)
            else {
                i = mScore / 1000;
                image = mUno.findCard(Color.RED, Content.values()[i]).image;
            } // else

            roi.x = 360;
            roi.y = 520;
            image.copyTo(new Mat(mScr, roi), image);
            i = Math.abs(mScore / 100 % 10);
            image = mUno.findCard(Color.BLUE, Content.values()[i]).image;
            roi.x += 140;
            image.copyTo(new Mat(mScr, roi), image);
            i = Math.abs(mScore / 10 % 10);
            image = mUno.findCard(Color.GREEN, Content.values()[i]).image;
            roi.x += 140;
            image.copyTo(new Mat(mScr, roi), image);
            i = Math.abs(mScore % 10);
            image = mUno.findCard(Color.YELLOW, Content.values()[i]).image;
            roi.x += 140;
            image.copyTo(new Mat(mScr, roi), image);

            // Show image
            Utils.matToBitmap(mScr, mBmp);
            mHandler.post(this); // -> run()
            return;
        } // if (status == STAT_WELCOME)

        // Center: card deck & recent played card
        image = mUno.getBackImage();
        roi = new Rect(338, 270, 121, 181);
        image.copyTo(new Mat(mScr, roi), image);
        recentColors = mUno.getRecentColors();
        recent = mUno.getRecent();
        size = recent.size();
        width = 45 * size + 75;
        roi.x = 792 - width / 2;
        roi.y = 270;
        for (i = 0; i < size; ++i) {
            if (recent.get(i).content == Content.WILD) {
                image = mUno.getColoredWildImage(recentColors.get(i));
            } // if (recent.get(i).content == Content.WILD)
            else if (recent.get(i).content == Content.WILD_DRAW4) {
                image = mUno.getColoredWildDraw4Image(recentColors.get(i));
            } // else if (recent.get(i).content == Content.WILD_DRAW4)
            else {
                image = recent.get(i).image;
            } // else

            image.copyTo(new Mat(mScr, roi), image);
            roi.x += 45;
        } // for (i = 0; i < size; ++i)

        // Left-top corner: remain / used
        remain = mUno.getDeckCount();
        used = mUno.getUsedCount();
        info = i18n.label_remain_used(remain, used);
        mUno.putText(mScr, info, 20, 42, null);

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
            roi.x = 20;
            roi.y = 290 - 20 * size;
            for (i = 0; i < size; ++i) {
                image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                image.copyTo(new Mat(mScr, roi), image);
                roi.y += 40;
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = mUno.getTextWidth("UNO");
                mUno.putText(mScr, "UNO", 80 - width / 2, 494, Color.YELLOW);
            } // if (size == 1)
        } // else if (((mHideFlag >> 1) & 0x01) == 0x00)

        // Top-center: Hand cards of Player North (COM2)
        if (status == STAT_GAME_OVER && mWinner == Player.COM2) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 640 - width / 2, 121, Color.YELLOW);
        } // if (status == STAT_GAME_OVER && mWinner == Player.COM2)
        else if (((mHideFlag >> 2) & 0x01) == 0x00) {
            Player p = mUno.getPlayer(Player.COM2);
            List<Card> hand = p.getHandCards();
            size = hand.size();
            roi.x = (1205 - 45 * size) / 2;
            roi.y = 20;
            for (i = 0; i < size; ++i) {
                image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                image.copyTo(new Mat(mScr, roi), image);
                roi.x += 45;
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = mUno.getTextWidth("UNO");
                mUno.putText(mScr, "UNO", 560 - width, 121, Color.YELLOW);
            } // if (size == 1)
        } // else if (((mHideFlag >> 2) & 0x01) == 0x00)

        // Right-center: Hand cards of Player East (COM3)
        if (status == STAT_GAME_OVER && mWinner == Player.COM3) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 1200 - width / 2, 461, Color.YELLOW);
        } // if (status == STAT_GAME_OVER && mWinner == Player.COM3)
        else if (((mHideFlag >> 3) & 0x01) == 0x00) {
            Player p = mUno.getPlayer(Player.COM3);
            List<Card> hand = p.getHandCards();
            size = hand.size();
            roi.x = 1140;
            roi.y = 290 - 20 * size;
            for (i = 0; i < size; ++i) {
                image = p.isOpen(i) ? hand.get(i).image : mUno.getBackImage();
                image.copyTo(new Mat(mScr, roi), image);
                roi.y += 40;
            } // for (i = 0; i < size; ++i)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                width = mUno.getTextWidth("UNO");
                mUno.putText(mScr, "UNO", 1200 - width / 2, 494, Color.YELLOW);
            } // if (size == 1)
        } // else if (((mHideFlag >> 3) & 0x01) == 0x00)

        // Bottom: Your hand cards
        if (status == STAT_GAME_OVER && mWinner == Player.YOU) {
            // Played all hand cards, it's winner
            width = mUno.getTextWidth("WIN");
            mUno.putText(mScr, "WIN", 640 - width / 2, 621, Color.YELLOW);
        } // if (status == STAT_GAME_OVER && mWinner == Player.YOU)
        else if ((mHideFlag & 0x01) == 0x00) {
            // Show your all hand cards
            List<Card> hand = mUno.getPlayer(Player.YOU).getHandCards();
            size = hand.size();
            roi.x = 610 - 30 * size;
            for (Card card : hand) {
                image = status == STAT_GAME_OVER
                        || (status == Player.YOU && mUno.isLegalToPlay(card))
                        ? card.image : card.darkImg;
                roi.y = i == mSelectedIdx ? 500 : 520;
                image.copyTo(new Mat(mScr, roi), image);
                roi.x += 60;
            } // for (Card card : hand)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                mUno.putText(mScr, "UNO", 720, 621, Color.YELLOW);
            } // if (size == 1)
        } // else if ((mHideFlag & 0x01) == 0x00)

        // Extra sectors in special status
        switch (status) {
            case STAT_WILD_COLOR:
                // Need to specify the following legal color after played a
                // wild card. Draw color sectors in the center of screen
                center = new Point(405, 315);
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
                center = new Point(405, 315);
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
                        /* y     */ 268,
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
                        /* y     */ 382,
                        /* color */ null
                ); // mUno.putText()
                break; // case STAT_DOUBT_WILD4

            case STAT_SEVEN_TARGET:
                // Ask the target you want to swap hand cards with
                center = new Point(405, 315);
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
                mUno.putText(mScr, "W", 338 - width / 2, 350, null);

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
                mUno.putText(mScr, "E", 472 - width / 2, 350, null);

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
                mUno.putText(mScr, "N", 405 - width / 2, 270, null);
                break; // case STAT_SEVEN_TARGET

            default:
                break; // default
        } // switch (status)

        // Show screen
        Utils.matToBitmap(mScr, mBmp);
        mHandler.post(this); // -> run()
    } // refreshScreen(String)

    /**
     * Draw [mBmp] on the screen. Called by system.
     */
    @Override
    public void run() {
        if (CLOSED_FLAG == 0x00000000) {
            mImgScreen.setImageBitmap(mBmp);
        } // if (CLOSED_FLAG == 0x00000000)
    } // run()

    /**
     * In 7-0 rule, when a zero card is put down, everyone need to pass
     * the hand cards to the next player.
     */
    @WorkerThread
    private void cycle() {
        final int[] x = {580, 160, 580, 1000};
        final int[] y = {490, 270, 50, 270};
        int curr, next, oppo, prev;
        AnimateLayer[] layer;

        setStatus(STAT_IDLE);
        mHideFlag = 0x0f;
        refreshScreen(i18n.info_0_rotate());
        curr = mUno.getNow();
        next = mUno.getNext();
        oppo = mUno.getOppo();
        prev = mUno.getPrev();
        layer = new AnimateLayer[]{
                new AnimateLayer(),
                new AnimateLayer(),
                new AnimateLayer(),
                new AnimateLayer()
        }; // new AnimateLayer[]{}
        layer[0].elem = mUno.getBackImage();
        layer[0].startLeft = x[curr];
        layer[0].startTop = y[curr];
        layer[0].endLeft = x[next];
        layer[0].endTop = y[next];
        layer[1].elem = mUno.getBackImage();
        layer[1].startLeft = x[next];
        layer[1].startTop = y[next];
        layer[1].endLeft = x[oppo];
        layer[1].endTop = y[oppo];
        if (mUno.getPlayers() == 3) {
            layer[2].elem = mUno.getBackImage();
            layer[2].startLeft = x[oppo];
            layer[2].startTop = y[oppo];
            layer[2].endLeft = x[curr];
            layer[2].endTop = y[curr];
            animate(3, layer);
        } // if (mUno.getPlayers() == 3)
        else {
            layer[2].elem = mUno.getBackImage();
            layer[2].startLeft = x[oppo];
            layer[2].startTop = y[oppo];
            layer[2].endLeft = x[prev];
            layer[2].endTop = y[prev];
            layer[3].elem = mUno.getBackImage();
            layer[3].startLeft = x[prev];
            layer[3].startTop = y[prev];
            layer[3].endLeft = x[curr];
            layer[3].endTop = y[curr];
            animate(4, layer);
        } // else

        mHideFlag = 0x00;
        mUno.cycle();
        refreshScreen(i18n.info_0_rotate());
        threadSleep(1500);
        setStatus(mUno.switchNow());
    } // cycle()

    /**
     * The player in action swap hand cards with another player.
     *
     * @param whom Swap with whom. Must be one of the following:
     *             Player.YOU, Player.COM1, Player.COM2, Player.COM3
     */
    @WorkerThread
    void swapWith(int whom) {
        final int[] x = {580, 160, 580, 1000};
        final int[] y = {490, 270, 50, 270};
        AnimateLayer[] layer;
        int curr;

        setStatus(STAT_IDLE);
        curr = mUno.getNow();
        mHideFlag = (1 << curr) | (1 << whom);
        refreshScreen(i18n.info_7_swap(curr, whom));
        layer = new AnimateLayer[]{
                new AnimateLayer(),
                new AnimateLayer()
        }; // new AnimateLayer[]{}
        layer[0].elem = layer[1].elem = mUno.getBackImage();
        layer[0].startLeft = x[curr];
        layer[0].startTop = y[curr];
        layer[0].endLeft = x[whom];
        layer[0].endTop = y[whom];
        layer[1].startLeft = x[whom];
        layer[1].startTop = y[whom];
        layer[1].endLeft = x[curr];
        layer[1].endTop = y[curr];
        animate(2, layer);
        mHideFlag = 0x00;
        mUno.swap(curr, whom);
        refreshScreen(i18n.info_7_swap(curr, whom));
        threadSleep(1500);
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
        AnimateLayer[] layer;
        int c, now, size, recentSize, next;

        setStatus(STAT_IDLE); // block tap down events when idle
        now = mUno.getNow();
        size = mUno.getCurrPlayer().getHandSize();
        card = mUno.play(now, index, color);
        mSelectedIdx = -1;
        mSoundPool.play(sndPlay, mSndVol, mSndVol, 1, 0, 1.0f);
        if (card != null) {
            layer = new AnimateLayer[]{new AnimateLayer()};
            layer[0].elem = card.image;
            switch (now) {
                case Player.COM1:
                    layer[0].startLeft = 160;
                    layer[0].startTop = 290 - 20 * size + 40 * index;
                    break; // case Player.COM1

                case Player.COM2:
                    layer[0].startLeft = (1205 - 45 * size + 90 * index) / 2;
                    layer[0].startTop = 50;
                    break; // case Player.COM2

                case Player.COM3:
                    layer[0].startLeft = 1000;
                    layer[0].startTop = 290 - 20 * size + 40 * index;
                    break; // case Player.COM3

                default:
                    layer[0].startLeft = 610 - 30 * size + 60 * index;
                    layer[0].startTop = 500;
                    break; // default
            } // switch (now)

            recentSize = mUno.getRecent().size();
            layer[0].endLeft = (45 * recentSize + 1419) / 2;
            layer[0].endTop = 270;
            animate(1, layer);
            if (size == 1) {
                // The player in action becomes winner when it played the
                // final card in its hand successfully
                if (now == Player.YOU) {
                    mScore += mUno.getPlayer(Player.COM1).getHandScore()
                            + mUno.getPlayer(Player.COM2).getHandScore()
                            + mUno.getPlayer(Player.COM3).getHandScore();
                    if (mScore > 9999) mScore = 9999;
                    mSoundPool.play(sndWin, mSndVol, mSndVol, 1, 0, 1.0f);
                } // if (now == Player.YOU)
                else {
                    mScore -= mUno.getPlayer(Player.YOU).getHandScore();
                    if (mScore < -999) mScore = -999;
                    mSoundPool.play(sndLose, mSndVol, mSndVol, 1, 0, 1.0f);
                } // else

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
                            threadSleep(1500);
                            setStatus(next);
                        } // if (mUno.isDraw2StackRule())
                        else {
                            refreshScreen(i18n.act_playDraw2(now, next, 2));
                            threadSleep(1500);
                            draw(2, /* force */ true);
                        } // else
                        break; // case DRAW2

                    case SKIP:
                        next = mUno.switchNow();
                        refreshScreen(i18n.act_playSkip(now, next));
                        threadSleep(1500);
                        setStatus(mUno.switchNow());
                        break; // case SKIP

                    case REV:
                        mUno.switchDirection();
                        refreshScreen(i18n.act_playRev(now));
                        threadSleep(1500);
                        setStatus(mUno.switchNow());
                        break; // case REV

                    case WILD:
                        refreshScreen(i18n.act_playWild(now, color.ordinal()));
                        threadSleep(1500);
                        setStatus(mUno.switchNow());
                        break; // case WILD

                    case WILD_DRAW4:
                        next = mUno.getNext();
                        refreshScreen(i18n.act_playWildDraw4(now, next));
                        threadSleep(1500);
                        setStatus(STAT_DOUBT_WILD4);
                        break; // case WILD_DRAW4

                    case NUM7:
                        if (mUno.isSevenZeroRule()) {
                            refreshScreen(i18n.act_playCard(now, card.name));
                            threadSleep(750);
                            setStatus(STAT_SEVEN_TARGET);
                            break; // case NUM7
                        } // if (mUno.isSevenZeroRule())
                        // else fall through

                    case NUM0:
                        if (mUno.isSevenZeroRule()) {
                            refreshScreen(i18n.act_playCard(now, card.name));
                            threadSleep(750);
                            cycle();
                            break; // case NUM0
                        } // if (mUno.isSevenZeroRule())
                        // else fall through

                    default:
                        refreshScreen(i18n.act_playCard(now, card.name));
                        threadSleep(1500);
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
        AnimateLayer[] layer;
        int i, index, c, now, size;

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
                layer = new AnimateLayer[]{new AnimateLayer()};
                layer[0].startLeft = 338;
                layer[0].startTop = 270;
                switch (now) {
                    case Player.COM1:
                        layer[0].elem = mUno.getBackImage();
                        layer[0].endLeft = 20;
                        layer[0].endTop = 290 - 20 * size + 40 * index;
                        message = i18n.act_drawCardCount(now, count);
                        break; // case Player.COM1

                    case Player.COM2:
                        layer[0].elem = mUno.getBackImage();
                        layer[0].endLeft = (1205 - 45 * size + 90 * index) / 2;
                        layer[0].endTop = 20;
                        message = i18n.act_drawCardCount(now, count);
                        break; // case Player.COM2

                    case Player.COM3:
                        layer[0].elem = mUno.getBackImage();
                        layer[0].endLeft = 1140;
                        layer[0].endTop = 290 - 20 * size + 40 * index;
                        message = i18n.act_drawCardCount(now, count);
                        break; // case Player.COM3

                    default:
                        layer[0].elem = drawn.image;
                        layer[0].endLeft = 610 - 30 * size + 60 * index;
                        layer[0].endTop = 520;
                        message = i18n.act_drawCard(now, drawn.name);
                        break; // default
                } // switch (now)

                // Animation
                mSoundPool.play(sndDraw, mSndVol, mSndVol, 1, 0, 1.0f);
                animate(1, layer);
                refreshScreen(message);
                threadSleep(300);
            } // if (index >= 0)
            else {
                message = i18n.info_cannotDraw(now, Uno.MAX_HOLD_CARDS);
                refreshScreen(message);
                break;
            } // else
        } // for (i = 0; i < count; ++i)

        threadSleep(750);
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
            threadSleep(750);
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
        int i, j;
        Rect roi;

        roi = new Rect();
        for (i = 0; i < 5; ++i) {
            for (j = 0; j < layerCount; ++j) {
                AnimateLayer l = layer[j];
                roi.x = l.startLeft + (l.endLeft - l.startLeft) * i / 5;
                roi.y = l.startTop + (l.endTop - l.startTop) * i / 5;
                roi.width = l.elem.cols();
                roi.height = l.elem.rows();
                new Mat(mScr, roi).copyTo(new Mat(mBackup[j], roi));
            } // for (j = 0; j < layerCount; ++j)

            for (j = 0; j < layerCount; ++j) {
                AnimateLayer l = layer[j];
                roi.x = l.startLeft + (l.endLeft - l.startLeft) * i / 5;
                roi.y = l.startTop + (l.endTop - l.startTop) * i / 5;
                roi.width = l.elem.cols();
                roi.height = l.elem.rows();
                l.elem.copyTo(new Mat(mScr, roi), l.elem);
            } // for (j = 0; j < layerCount; ++j)

            Utils.matToBitmap(mScr, mBmp);
            mHandler.post(this); // -> run()
            threadSleep(30);
            for (j = 0; j < layerCount; ++j) {
                AnimateLayer l = layer[j];
                roi.x = l.startLeft + (l.endLeft - l.startLeft) * i / 5;
                roi.y = l.startTop + (l.endTop - l.startTop) * i / 5;
                roi.width = l.elem.cols();
                roi.height = l.elem.rows();
                new Mat(mBackup[j], roi).copyTo(new Mat(mScr, roi));
            } // for (j = 0; j < layerCount; ++j)
        } // for (i = 0; i < 5; ++i)
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
        threadSleep(1500);
        if (challengeSuccess) {
            // Challenge success, who played [wild +4] draws 4 cards
            refreshScreen(i18n.info_challengeSuccess(now));
            threadSleep(1500);
            draw(4, /* force */ true);
        } // if (challengeSuccess)
        else {
            // Challenge failure, challenger draws 6 cards
            refreshScreen(i18n.info_challengeFailure(challenger));
            threadSleep(1500);
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
     * @return True because our listener has consumed the touch events.
     */
    @Override
    @UiThread
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            // Only response to tap down events, and ignore the others
            return false;
        } // if (event.getAction() != MotionEvent.ACTION_DOWN)

        // Coordinates must be measured in UI thread
        // Measurement in sub thread will cause measurement error
        int x = (int) (event.getX() * 1280 / v.getWidth());
        int y = (int) (event.getY() * 720 / v.getHeight());
        new Thread(() -> {
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
                    else if (790 <= x && x <= 910) {
                        // Easy AI Level
                        mUno.setDifficulty(Uno.LV_EASY);
                        setStatus(mStatus);
                    } // else if (790 <= x && x <= 910)
                    else if (970 <= x && x <= 1090) {
                        // Hard AI Level
                        mUno.setDifficulty(Uno.LV_HARD);
                        setStatus(mStatus);
                    } // else if (970 <= x && x <= 1090)
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
                    else if (790 <= x && x <= 910) {
                        // 3 players
                        mUno.setPlayers(3);
                        setStatus(mStatus);
                    } // else if (790 <= x && x <= 910)
                    else if (970 <= x && x <= 1090) {
                        // 4 players
                        mUno.setPlayers(4);
                        setStatus(mStatus);
                    } // else if (970 <= x && x <= 1090)
                } // else if (270 <= y && y <= 450)
                else if (519 <= y && y <= 540) {
                    if (800 <= x && x <= 927) {
                        // Force play, <KEEP> button
                        mUno.setForcePlay(false);
                        setStatus(mStatus);
                    } // if (800 <= x && x <= 927)
                    else if (980 <= x && x <= 1104) {
                        // Force play, <PLAY> button
                        mUno.setForcePlay(true);
                        setStatus(mStatus);
                    } // else if (980 <= x && x <= 1104)
                } // else if (519 <= y && y <= 540)
                else if (569 <= y && y <= 590) {
                    if (800 <= x && x <= 906) {
                        // 7-0, <OFF> button
                        mUno.setSevenZeroRule(false);
                        setStatus(mStatus);
                    } // if (800 <= x && x <= 906)
                    else if (980 <= x && x <= 1072) {
                        // 7-0, <ON> button
                        mUno.setSevenZeroRule(true);
                        setStatus(mStatus);
                    } // else if (980 <= x && x <= 1072)
                } // else if (569 <= y && y <= 590)
                else if (619 <= y && y <= 640) {
                    if (800 <= x && x <= 906) {
                        // +2 stack, <OFF> button
                        mUno.setDraw2StackRule(false);
                        setStatus(mStatus);
                    } // if (800 <= x && x <= 906)
                    else if (980 <= x && x <= 1072) {
                        // +2 stack, <ON> button
                        mUno.setDraw2StackRule(true);
                        setStatus(mStatus);
                    } // else if (980 <= x && x <= 1072)
                } // else if (619 <= y && y <= 640)
                else if (679 <= y && y <= 700) {
                    if (20 <= x && x <= 200) {
                        // <OPTIONS> button
                        // Leave options page
                        mAdjustOptions = false;
                        setStatus(mStatus);
                    } // if (20 <= x && x <= 200)
                    else if (1130 <= x && x <= 1260) {
                        // <AUTO> button
                        mAuto = !mAuto;
                        setStatus(mStatus);
                    } // else if (1130 <= x && x <= 1260)
                } // else if (679 <= y && y <= 700)
                return;
            } // if (mAdjustOptions)

            if (679 <= y && y <= 700 && 1130 <= x && x <= 1260) {
                // <AUTO> button
                // In player's action, automatically play or draw cards by AI
                mAuto = !mAuto;
                setStatus(mStatus == STAT_WILD_COLOR ? Player.YOU : mStatus);
                return;
            } // if (679 <= y && y <= 700 && 1130 <= x && x <= 1260)

            switch (mStatus) {
                case STAT_WELCOME:
                    if (270 <= y && y <= 450) {
                        if (580 <= x && x <= 700) {
                            // UNO button, start a new game
                            setStatus(STAT_NEW_GAME);
                        } // if (580 <= x && x <= 700)
                    } // if (270 <= y && y <= 450)
                    else if (679 <= y && y <= 700) {
                        if (20 <= x && x <= 200) {
                            // <OPTIONS> button
                            mAdjustOptions = true;
                            setStatus(mStatus);
                        } // if (20 <= x && x <= 200)
                    } // else if (679 <= y && y <= 700)
                    break; // case STAT_WELCOME

                case Player.YOU:
                    if (mAuto) {
                        // Do operations automatically by AI strategies
                        break; // case Player.YOU
                    } // if (mAuto)
                    else if (520 <= y && y <= 700) {
                        Player now = mUno.getPlayer(Player.YOU);
                        List<Card> hand = now.getHandCards();
                        int size = hand.size();
                        int width = 60 * size + 60;
                        int startX = 640 - width / 2;
                        if (startX <= x && x <= startX + width) {
                            // Hand card area
                            // Calculate which card clicked by the X-coordinate
                            int index = Math.min((x - startX) / 60, size - 1);
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
                        else {
                            // Blank area, cancel your selection
                            mSelectedIdx = -1;
                            setStatus(mStatus);
                        } // else
                    } // else if (520 <= y && y <= 700)
                    else if (270 <= y && y <= 450 && 338 <= x && x <= 458) {
                        // Card deck area, draw a card
                        draw(1, /* force */ false);
                    } // else if (270 <= y && y <= 450 && 338 <= x && x <= 458)
                    break; // case Player.YOU

                case STAT_WILD_COLOR:
                    if (220 < y && y < 315) {
                        if (310 < x && x < 405) {
                            // Red sector
                            play(mSelectedIdx, Color.RED);
                        } // if (310 < x && x < 405)
                        else if (405 < x && x < 500) {
                            // Blue sector
                            play(mSelectedIdx, Color.BLUE);
                        } // else if (405 < x && x < 500)
                    } // if (220 < y && y < 315)
                    else if (315 < y && y < 410) {
                        if (310 < x && x < 405) {
                            // Yellow sector
                            play(mSelectedIdx, Color.YELLOW);
                        } // if (310 < x && x < 405)
                        else if (405 < x && x < 500) {
                            // Green sector
                            play(mSelectedIdx, Color.GREEN);
                        } // else if (405 < x && x < 500)
                    } // else if (315 < y && y < 410)
                    break; // case STAT_WILD_COLOR

                case STAT_DOUBT_WILD4:
                    // Asking if you want to challenge your previous player
                    if (310 < x && x < 500) {
                        if (220 < y && y < 315) {
                            // YES button, challenge wild +4
                            onChallenge();
                        } // if (220 < y && y < 315)
                        else if (315 < y && y < 410) {
                            // NO button, do not challenge wild +4
                            mUno.switchNow();
                            draw(4, /* force */ true);
                        } // else if (315 < y && y < 410)
                    } // if (310 < x && x < 500)
                    break; // case STAT_DOUBT_WILD4

                case STAT_SEVEN_TARGET:
                    if (198 < y && y < 276 && mUno.getPlayers() == 4) {
                        if (338 < x && x < 472) {
                            // North sector
                            swapWith(Player.COM2);
                        } // if (338 < x && x < 472)
                    } // if (198 < y && y < 276 && mUno.getPlayers() == 4)
                    else if (315 < y && y < 410) {
                        if (310 < x && x < 405) {
                            // West sector
                            swapWith(Player.COM1);
                        } // if (310 < x && x < 405)
                        else if (405 < x && x < 500) {
                            // East sector
                            swapWith(Player.COM3);
                        } // else if (405 < x && x < 500)
                    } // else if (315 < y && y < 410)
                    break; // case STAT_SEVEN_TARGET

                case STAT_GAME_OVER:
                    if (270 <= y && y <= 450) {
                        if (338 <= x && x <= 458) {
                            // Card deck area, start a new game
                            setStatus(STAT_NEW_GAME);
                        } // if (338 <= x && x <= 458)
                    } // if (270 <= y && y <= 450)
                    else if (679 <= y && y <= 700) {
                        if (20 <= x && x <= 200) {
                            // <OPTIONS> button
                            mAdjustOptions = true;
                            setStatus(mStatus);
                        } // if (20 <= x && x <= 200)
                    } // else if (679 <= y && y <= 700)
                    break; // case STAT_GAME_OVER

                default:
                    break; // default
            } // switch (mStatus)
        }).start();

        return true;
    } // onTouch()

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
    } // onKeyDown()

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
            editor.putInt("score", mScore);
            editor.putFloat("sndVol", mSndVol);
            editor.putFloat("bgmVol", mBgmVol);
            editor.putInt("players", mUno.getPlayers());
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
        CLOSED_FLAG = 0x80000000;
        if (OPENCV_INIT_SUCCESS) {
            mSoundPool.release();
        } // if (OPENCV_INIT_SUCCESS)

        mHandler.removeCallbacksAndMessages(null);
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
        } // onCreateDialog()

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
        } // onClick()
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
        } // onCreateDialog()

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
        } // onClick()
    } // ExitDialog Inner Class
} // MainActivity Class

// E.O.F