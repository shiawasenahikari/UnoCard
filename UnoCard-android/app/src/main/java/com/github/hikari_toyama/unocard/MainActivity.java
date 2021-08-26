////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.github.hikari_toyama.unocard.core.AI;
import com.github.hikari_toyama.unocard.core.Card;
import com.github.hikari_toyama.unocard.core.Color;
import com.github.hikari_toyama.unocard.core.Content;
import com.github.hikari_toyama.unocard.core.Player;
import com.github.hikari_toyama.unocard.core.Uno;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Game UI.
 */
@SuppressWarnings("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity
        implements View.OnTouchListener {
    private static final boolean OPENCV_INIT_SUCCESS = OpenCVLoader.initDebug();
    private static final String[] CL = {"", "RED", "BLUE", "GREEN", "YELLOW"};
    private static final String[] NAME = {"YOU", "WEST", "NORTH", "EAST"};
    private static final Scalar RGB_YELLOW = new Scalar(0xFF, 0xAA, 0x11);
    private static final Scalar RGB_WHITE = new Scalar(0xCC, 0xCC, 0xCC);
    private static final Scalar RGB_GREEN = new Scalar(0x55, 0xAA, 0x55);
    private static final Scalar RGB_BLUE = new Scalar(0x55, 0x55, 0xFF);
    private static final Scalar RGB_RED = new Scalar(0xFF, 0x55, 0x55);
    private static final int FONT_SANS = Imgproc.FONT_HERSHEY_DUPLEX;
    private static final int STAT_WILD_COLOR = 0x5555;
    private static final int STAT_GAME_OVER = 0x4444;
    private static final int STAT_NEW_GAME = 0x3333;
    private static final int STAT_WELCOME = 0x2222;
    private static final int STAT_IDLE = 0x1111;
    private MediaPlayer mMediaPlayer;
    private boolean mChallengeAsk;
    private SoundPool mSoundPool;
    private ImageView mImgScreen;
    private boolean mChallenged;
    private boolean mImmPlayAsk;
    private Card mSelectedCard;
    private Handler mHandler;
    private Card mDrawnCard;
    private int mHardTotal3;
    private int mEasyTotal3;
    private int mHardTotal;
    private int mEasyTotal;
    private int mWildIndex;
    private boolean mAuto;
    private int mHardWin3;
    private int mEasyWin3;
    private int mHardWin;
    private int mEasyWin;
    private int mStatus;
    private int mWinner;
    private Bitmap mBmp;
    private int sndPlay;
    private int sndDraw;
    private int sndLose;
    private int sndWin;
    private int sndUno;
    private Mat mScr;
    private Uno mUno;
    private AI mAI;

    /**
     * Activity initialization.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp;
        DialogFragment dialog;

        // Preparations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        if (OPENCV_INIT_SUCCESS) {
            sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
            mEasyWin = sp.getInt("easyWin", 0);
            mHardWin = sp.getInt("hardWin", 0);
            mEasyWin3 = sp.getInt("easyWin3", 0);
            mHardWin3 = sp.getInt("hardWin3", 0);
            mEasyTotal = sp.getInt("easyTotal", 0);
            mHardTotal = sp.getInt("hardTotal", 0);
            mEasyTotal3 = sp.getInt("easyTotal3", 0);
            mHardTotal3 = sp.getInt("hardTotal3", 0);
            mUno = Uno.getInstance(this);
            mUno.setPlayers(sp.getInt("players", 3));
            mUno.setDifficulty(sp.getInt("difficulty", Uno.LV_EASY));
            mAI = AI.getInstance(this);
            mWinner = Player.YOU;
            mStatus = STAT_WELCOME;
            mScr = mUno.getBackground().clone();
            mBmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
            mImgScreen = findViewById(R.id.imgMainScreen);
            onStatusChanged(mStatus);
            mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            sndUno = mSoundPool.load(this, R.raw.snd_uno, 1);
            sndWin = mSoundPool.load(this, R.raw.snd_win, 1);
            sndLose = mSoundPool.load(this, R.raw.snd_lose, 1);
            sndPlay = mSoundPool.load(this, R.raw.snd_play, 1);
            sndDraw = mSoundPool.load(this, R.raw.snd_draw, 1);
            mMediaPlayer = MediaPlayer.create(this, R.raw.bgm);
            mMediaPlayer.setVolume(0.5f, 0.5f);
            mMediaPlayer.setLooping(true);
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
    protected void onResume() {
        super.onResume();
        if (OPENCV_INIT_SUCCESS) {
            mMediaPlayer.start();
        } // if (OPENCV_INIT_SUCCESS)
    } // onResume()

    /**
     * AI Strategies (Difficulty: EASY).
     */
    private void easyAI() {
        new Thread(() -> {
            int idxBest, now;
            Color[] bestColor;

            if (mChallengeAsk) {
                onChallengeChance(mAI.needToChallenge(mStatus));
            } // if (mChallengeAsk)
            else {
                now = mStatus;
                mStatus = STAT_IDLE; // block tap down events when idle
                bestColor = new Color[1];
                idxBest = mAI.easyAI_bestCardIndex4NowPlayer(
                        /* drawnCard */ mImmPlayAsk ? mDrawnCard : null,
                        /* outColor  */ bestColor
                ); // idxBest = mAI.easyAI_bestCardIndex4NowPlayer()

                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    mImmPlayAsk = false;
                    play(idxBest, bestColor[0]);
                } // if (idxBest >= 0)
                else {
                    // No appropriate cards to play, or no card is legal to play
                    if (mImmPlayAsk) {
                        mImmPlayAsk = false;
                        refreshScreen(NAME[now] + ": Pass");
                        try {
                            Thread.sleep(750);
                        } // try
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // catch (InterruptedException e)

                        mStatus = mUno.switchNow();
                        onStatusChanged(mStatus);
                    } // if (mImmPlayAsk)
                    else {
                        draw(1, /* force */ false);
                    } // else
                } // else
            } // else
        }).start();
    } // easyAI()

    /**
     * AI Strategies (Difficulty: HARD).
     */
    private void hardAI() {
        new Thread(() -> {
            int idxBest, now;
            Color[] bestColor;

            if (mChallengeAsk) {
                onChallengeChance(mAI.needToChallenge(mStatus));
            } // if (mChallengeAsk)
            else {
                now = mStatus;
                mStatus = STAT_IDLE; // block tap down events when idle
                bestColor = new Color[1];
                idxBest = mAI.hardAI_bestCardIndex4NowPlayer(
                        /* drawnCard */ mImmPlayAsk ? mDrawnCard : null,
                        /* outColor  */ bestColor
                ); // idxBest = mAI.hardAI_bestCardIndex4NowPlayer()

                if (idxBest >= 0) {
                    // Found an appropriate card to play
                    mImmPlayAsk = false;
                    play(idxBest, bestColor[0]);
                } // if (idxBest >= 0)
                else {
                    // No appropriate cards to play, or no card is legal to play
                    if (mImmPlayAsk) {
                        mImmPlayAsk = false;
                        refreshScreen(NAME[now] + ": Pass");
                        try {
                            Thread.sleep(750);
                        } // try
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // catch (InterruptedException e)

                        mStatus = mUno.switchNow();
                        onStatusChanged(mStatus);
                    } // if (mImmPlayAsk)
                    else {
                        draw(1, /* force */ false);
                    } // else
                } // else
            } // else
        }).start();
    } // hardAI()

    /**
     * Triggered when the value of member [mStatus] changed.
     *
     * @param status New status value.
     */
    private void onStatusChanged(int status) {
        new Thread(() -> {
            Rect rect;
            Size axes;
            Point center;
            Card next2last;
            String message;
            Mat areaToErase;
            List<Card> recent;

            switch (status) {
                case STAT_WELCOME:
                    refreshScreen(
                            "WELCOME TO UNO CARD GAME, CLICK UNO TO START"
                    ); // refreshScreen()
                    break; // case STAT_WELCOME

                case STAT_NEW_GAME:
                    // New game
                    switch (mUno.getDifficulty() << 4 | mUno.getPlayers()) {
                        case 0x03:
                            // Difficulty = EASY(0), Players = 3
                            ++mEasyTotal3;
                            break; // case 0x03

                        case 0x04:
                            // Difficulty = EASY(0), Players = 4
                            ++mEasyTotal;
                            break; // case 0x04

                        case 0x13:
                            // Difficulty = HARD(1), Players = 3
                            ++mHardTotal3;
                            break; // case 0x13

                        case 0x14:
                            // Difficulty = HARD(1), Players = 4
                            ++mHardTotal;
                            break; // case 0x14

                        default:
                            break; // default
                    } // switch (mUno.getDifficulty() << 4 | mUno.getPlayers())

                    mUno.start();
                    mSelectedCard = null;
                    refreshScreen("GET READY");
                    try {
                        Thread.sleep(2000);
                    } // try
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    } // catch (InterruptedException e)

                    switch (mUno.getRecent().get(0).content) {
                        case DRAW2:
                            // If starting with a [+2], let dealer draw 2 cards.
                            draw(2, /* force */ true);
                            break; // case DRAW2

                        case SKIP:
                            // If starting with a [skip], skip dealer's turn.
                            refreshScreen(NAME[mUno.getNow()] + ": Skipped");
                            mHandler.postDelayed(() -> {
                                mStatus = mUno.switchNow();
                                onStatusChanged(mStatus);
                            }, 1500);
                            break; // case SKIP

                        case REV:
                            // If starting with a [reverse], change the action
                            // sequence to COUNTER CLOCKWISE.
                            mUno.switchDirection();
                            refreshScreen("Direction changed");
                            mHandler.postDelayed(() -> {
                                mStatus = mUno.getNow();
                                onStatusChanged(mStatus);
                            }, 1500);
                            break; // case REV

                        default:
                            // Otherwise, go to dealer's turn.
                            mStatus = mUno.getNow();
                            onStatusChanged(mStatus);
                            break; // default
                    } // switch (mUno.getRecent().get(0).content)
                    break; // case STAT_NEW_GAME

                case Player.YOU:
                    // Your turn, select a hand card to play, or draw a card
                    if (mAuto) {
                        if (mUno.getDifficulty() == Uno.LV_EASY) {
                            easyAI();
                        } // if (mUno.getDifficulty() == Uno.LV_EASY)
                        else {
                            hardAI();
                        } // else
                    } // if (mAuto)
                    else if (mImmPlayAsk) {
                        mSelectedCard = mDrawnCard;
                        refreshScreen("^ Play " + mDrawnCard + "?");
                        rect = new Rect(338, 270, 121, 181);
                        areaToErase = new Mat(mUno.getBackground(), rect);
                        areaToErase.copyTo(new Mat(mScr, rect));
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
                        Imgproc.putText(
                                /* img       */ mScr,
                                /* text      */ "YES",
                                /* org       */ new Point(346, 295),
                                /* fontFace  */ FONT_SANS,
                                /* fontScale */ 2.0,
                                /* color     */ RGB_WHITE,
                                /* thickness */ 2
                        ); // Imgproc.putText()

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
                        Imgproc.putText(
                                /* img        */ mScr,
                                /* text       */ "NO",
                                /* org        */ new Point(360, 378),
                                /* fontFace   */ FONT_SANS,
                                /* fontScale  */ 2.0,
                                /* color      */ RGB_WHITE,
                                /* thickness  */ 2
                        ); // Imgproc.putText()

                        // Show screen
                        Utils.matToBitmap(mScr, mBmp);
                        mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                    } // else if (mImmPlayAsk)
                    else if (mChallengeAsk) {
                        recent = mUno.getRecent();
                        next2last = recent.get(recent.size() - 2);
                        message = "^ Do you think "
                                + NAME[mUno.getNow()] + " still has "
                                + CL[next2last.getRealColor().ordinal()] + "?";
                        refreshScreen(message);
                        rect = new Rect(338, 270, 121, 181);
                        areaToErase = new Mat(mUno.getBackground(), rect);
                        areaToErase.copyTo(new Mat(mScr, rect));
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
                        Imgproc.putText(
                                /* img       */ mScr,
                                /* text      */ "YES",
                                /* org       */ new Point(346, 295),
                                /* fontFace  */ FONT_SANS,
                                /* fontScale */ 2.0,
                                /* color     */ RGB_WHITE,
                                /* thickness */ 2
                        ); // Imgproc.putText()

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
                        Imgproc.putText(
                                /* img        */ mScr,
                                /* text       */ "NO",
                                /* org        */ new Point(360, 378),
                                /* fontFace   */ FONT_SANS,
                                /* fontScale  */ 2.0,
                                /* color      */ RGB_WHITE,
                                /* thickness  */ 2
                        ); // Imgproc.putText()

                        // Show screen
                        Utils.matToBitmap(mScr, mBmp);
                        mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                    } // else if (mChallengeAsk)
                    else if (mUno.legalCardsCount4NowPlayer() == 0) {
                        refreshScreen(
                                "No card can be played... Draw a card from deck"
                        ); // refreshScreen()
                    } // else if (mUno.legalCardsCount4NowPlayer() == 0)
                    else if (mSelectedCard == null) {
                        refreshScreen(
                                "Select a card to play or draw a card from deck"
                        ); // refreshScreen()
                    } // else if (mSelectedCard == null)
                    else {
                        refreshScreen("Click again to play");
                    } // else
                    break; // case Player.YOU

                case STAT_WILD_COLOR:
                    // Need to specify the following legal color after played a
                    // wild card. Draw color sectors in the center of screen
                    refreshScreen("^ Specify the following legal color");
                    rect = new Rect(338, 270, 121, 181);
                    areaToErase = new Mat(mUno.getBackground(), rect);
                    areaToErase.copyTo(new Mat(mScr, rect));
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

                    // Show screen
                    Utils.matToBitmap(mScr, mBmp);
                    mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                    break; // case STAT_WILD_COLOR

                case Player.COM1:
                case Player.COM2:
                case Player.COM3:
                    // AI players' turn
                    if (mUno.getDifficulty() == Uno.LV_EASY) {
                        easyAI();
                    } // if (mUno.getDifficulty() == Uno.LV_EASY)
                    else {
                        hardAI();
                    } // else
                    break; // case Player.COM1, Player.COM2, Player.COM3

                case STAT_GAME_OVER:
                    // Game over
                    if (mWinner == Player.YOU) {
                        mSoundPool.play(sndWin, 1.0f, 1.0f, 1, 0, 1.0f);
                        switch (mUno.getDifficulty() << 4 | mUno.getPlayers()) {
                            case 0x03:
                                // Difficulty = EASY(0), Players = 3
                                ++mEasyWin3;
                                break; // case 0x03

                            case 0x04:
                                // Difficulty = EASY(0), Players = 4
                                ++mEasyWin;
                                break; // case 0x04

                            case 0x13:
                                // Difficulty = HARD(1), Players = 3
                                ++mHardWin3;
                                break; // case 0x13

                            case 0x14:
                                // Difficulty = HARD(1), Players = 4
                                ++mHardWin;
                                break; // case 0x14

                            default:
                                break; // default
                        } // switch (...)
                    } // if (mWinner == Player.YOU)
                    else {
                        mSoundPool.play(sndLose, 1.0f, 1.0f, 1, 0, 1.0f);
                    } // else

                    refreshScreen("Click the card deck to restart");
                    if (mAuto) {
                        try {
                            Thread.sleep(5000);
                        } // try
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // catch (InterruptedException e)

                        if (mAuto && mStatus == STAT_GAME_OVER) {
                            mStatus = STAT_NEW_GAME;
                            onStatusChanged(mStatus);
                        } // if (mAuto && mStatus == STAT_GAME_OVER)
                    } // if (mAuto)
                    break; // case STAT_GAME_OVER

                default:
                    break; // default
            } // switch (status)
        }).start();
    } // onStatusChanged(int)

    /**
     * Refresh the screen display.
     *
     * @param message Extra message to show.
     */
    private void refreshScreen(String message) {
        Rect roi;
        Mat image;
        Point point;
        String info;
        Size textSize;
        List<Card> hand;
        boolean beChallenged;
        int i, remain, used, rate;
        int status, size, width, height;

        // Lock the value of member [mStatus]
        status = mStatus;

        // Clear
        mUno.getBackground().copyTo(mScr);

        // Message area
        textSize = Imgproc.getTextSize(message, FONT_SANS, 1.0, 1, null);
        point = new Point(640 - textSize.width / 2, 480);
        Imgproc.putText(mScr, message, point, FONT_SANS, 1.0, RGB_WHITE);

        // Right-bottom corner: <AUTO> button
        point.x = 1130;
        point.y = 700;
        Imgproc.putText(
                /* img       */ mScr,
                /* text      */ "<AUTO>",
                /* point     */ point,
                /* fontFace  */ FONT_SANS,
                /* fontScale */ 1.0,
                /* color     */ mAuto ? RGB_YELLOW : RGB_WHITE
        ); // Imgproc.putText()

        // For welcome screen, only show difficulty buttons and winning rates
        if (status == STAT_WELCOME) {
            // [Level] option: easy / hard
            point.x = 340;
            point.y = 160;
            Imgproc.putText(mScr, "LEVEL", point, FONT_SANS, 1.0, RGB_WHITE);
            image = mUno.getLevelImage(
                    /* level   */ Uno.LV_EASY,
                    /* hiLight */ mUno.getDifficulty() == Uno.LV_EASY
            ); // image = mUno.getLevelImage()
            roi = new Rect(490, 60, 121, 181);
            image.copyTo(new Mat(mScr, roi), image);
            image = mUno.getLevelImage(
                    /* level   */ Uno.LV_HARD,
                    /* hiLight */ mUno.getDifficulty() == Uno.LV_HARD
            ); // image = mUno.getLevelImage()
            roi.x = 670;
            image.copyTo(new Mat(mScr, roi), image);

            // [Players] option: 3 / 4
            point.x = 340;
            point.y = 370;
            Imgproc.putText(mScr, "PLAYERS", point, FONT_SANS, 1.0, RGB_WHITE);
            image = mUno.getPlayers() == 3 ?
                    mUno.findCard(Color.BLUE, Content.NUM3).image :
                    mUno.findCard(Color.BLUE, Content.NUM3).darkImg;
            roi.x = 490;
            roi.y = 270;
            image.copyTo(new Mat(mScr, roi), image);
            image = mUno.getPlayers() == 4 ?
                    mUno.findCard(Color.BLUE, Content.NUM4).image :
                    mUno.findCard(Color.BLUE, Content.NUM4).darkImg;
            roi.x = 670;
            image.copyTo(new Mat(mScr, roi), image);

            // Win rate
            switch (mUno.getDifficulty() << 4 | mUno.getPlayers()) {
                case 0x03:
                    // Difficulty = EASY(0), Players = 3
                    rate = mEasyTotal3 == 0 ? 0 : 100 * mEasyWin3 / mEasyTotal3;
                    break; // case 0x03

                case 0x04:
                    // Difficulty = EASY(0), Players = 4
                    rate = mEasyTotal == 0 ? 0 : 100 * mEasyWin / mEasyTotal;
                    break; // case 0x04

                case 0x13:
                    // Difficulty = HARD(1), Players = 3
                    rate = mHardTotal3 == 0 ? 0 : 100 * mHardWin3 / mHardTotal3;
                    break; // case 0x13

                case 0x14:
                    // Difficulty = HARD(1), Players = 4
                    rate = mHardTotal == 0 ? 0 : 100 * mHardWin / mHardTotal;
                    break; // case 0x14

                default:
                    rate = 0;
                    break; // default
            } // switch (mUno.getDifficulty() << 4 | mUno.getPlayers())

            info = "win rate: " + rate + "%";
            textSize = Imgproc.getTextSize(info, FONT_SANS, 1.0, 1, null);
            point.x = 930 - textSize.width / 2;
            Imgproc.putText(mScr, info, point, FONT_SANS, 1.0, RGB_WHITE);

            // [UNO] button: start a new game
            image = mUno.getBackImage();
            roi.x = 580;
            roi.y = 520;
            image.copyTo(new Mat(mScr, roi), image);

            // Show image
            Utils.matToBitmap(mScr, mBmp);
            mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
            return;
        } // if (status == STAT_WELCOME)

        // Center: card deck & recent played card
        image = mUno.getBackImage();
        roi = new Rect(338, 270, 121, 181);
        image.copyTo(new Mat(mScr, roi), image);
        hand = mUno.getRecent();
        size = hand.size();
        width = 45 * size + 75;
        roi.x = 792 - width / 2;
        roi.y = 270;
        for (Card recent : hand) {
            if (recent.content == Content.WILD) {
                image = mUno.getColoredWildImage(recent.getRealColor());
            } // if (recent.content == Content.WILD)
            else if (recent.content == Content.WILD_DRAW4) {
                image = mUno.getColoredWildDraw4Image(recent.getRealColor());
            } // else if (recent.content == Content.WILD_DRAW4)
            else {
                image = recent.image;
            } // else

            image.copyTo(new Mat(mScr, roi), image);
            roi.x += 45;
        } // for (Card recent : hand)

        // Left-top corner: remain / used
        point.x = 20;
        point.y = 42;
        remain = mUno.getDeckCount();
        used = mUno.getUsedCount();
        info = "Remain/Used: " + remain + "/" + used;
        Imgproc.putText(mScr, info, point, FONT_SANS, 1.0, RGB_WHITE);

        // Left-center: Hand cards of Player West (COM1)
        hand = mUno.getPlayer(Player.COM1).getHandCards();
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            point.x = 51;
            point.y = 461;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            height = 40 * size + 140;
            roi.x = 20;
            roi.y = 360 - height / 2;
            beChallenged = mChallenged && mUno.getNow() == Player.COM1;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained cards to everyone
                // when being challenged or game over
                for (Card card : hand) {
                    image = card.image;
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (Card card : hand)
            } // if (beChallenged || status == STAT_GAME_OVER)
            else {
                // Only show card backs in game process
                image = mUno.getBackImage();
                for (i = 0; i < size; ++i) {
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (i = 0; i < size; ++i)
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 47;
                point.y = 494;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Top-center: Hand cards of Player North (COM2)
        hand = mUno.getPlayer(Player.COM2).getHandCards();
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            if (mUno.getPlayers() == 4) {
                point.x = 611;
                point.y = 121;
                Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (mUno.getPlayers() == 4)
        } // if (size == 0)
        else {
            width = 45 * size + 75;
            roi.x = 640 - width / 2;
            roi.y = 20;
            beChallenged = mChallenged && mUno.getNow() == Player.COM2;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained hand cards
                // when being challenged or game over
                for (Card card : hand) {
                    image = card.image;
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.x += 45;
                } // for (Card card : hand)
            } // if (beChallenged || status == STAT_GAME_OVER)
            else {
                // Only show card backs in game process
                image = mUno.getBackImage();
                for (i = 0; i < size; ++i) {
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.x += 45;
                } // for (i = 0; i < size; ++i)
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 500;
                point.y = 121;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Right-center: Hand cards of Player East (COM3)
        hand = mUno.getPlayer(Player.COM3).getHandCards();
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            point.x = 1170;
            point.y = 461;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            height = 40 * size + 140;
            roi.x = 1140;
            roi.y = 360 - height / 2;
            beChallenged = mChallenged && mUno.getNow() == Player.COM3;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained hand cards
                // when being challenged or game over
                for (Card card : hand) {
                    image = card.image;
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (Card card : hand)
            } // if (beChallenged || status == STAT_GAME_OVER)
            else {
                // Only show card backs in game process
                image = mUno.getBackImage();
                for (i = 0; i < size; ++i) {
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (i = 0; i < size; ++i)
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 1166;
                point.y = 494;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Bottom: Your hand cards
        hand = mUno.getPlayer(Player.YOU).getHandCards();
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            point.x = 611;
            point.y = 621;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            // Show your all hand cards
            width = 60 * size + 60;
            roi.x = 640 - width / 2;
            for (Card card : hand) {
                switch (status) {
                    case Player.YOU:
                        if (mImmPlayAsk) {
                            if (card == mDrawnCard) {
                                image = card.image;
                                roi.y = 490;
                            } // if (card == mDrawnCard)
                            else {
                                image = card.darkImg;
                                roi.y = 520;
                            } // else
                        } // if (mImmPlayAsk)
                        else if (mChallengeAsk || mChallenged) {
                            image = card.darkImg;
                            roi.y = 520;
                        } // else if (mChallengeAsk || mChallenged)
                        else {
                            image = mUno.isLegalToPlay(card) ?
                                    card.image :
                                    card.darkImg;
                            roi.y = card == mSelectedCard ? 490 : 520;
                        } // else
                        break; // case Player.YOU

                    case STAT_WILD_COLOR:
                        image = card.darkImg;
                        roi.y = card == mSelectedCard ? 490 : 520;
                        break; // case STAT_WILD_COLOR

                    case STAT_GAME_OVER:
                        image = card.image;
                        roi.y = 520;
                        break; // case STAT_GAME_OVER

                    default:
                        image = card.darkImg;
                        roi.y = 520;
                        break; // default
                } // switch (status)

                image.copyTo(new Mat(mScr, roi), image);
                roi.x += 60;
            } // for (Card card : hand)

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 720;
                point.y = 621;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
        } // else

        // Show screen
        Utils.matToBitmap(mScr, mBmp);
        mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
    } // refreshScreen(String)

    /**
     * The player in action play a card.
     *
     * @param index Play which card. Pass the corresponding card's index of the
     *              player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     */
    private void play(int index, Color color) {
        new Thread(() -> {
            Rect roi;
            Mat image;
            Card card;
            String message;
            int x, y, now, size, width, height, next;

            mStatus = STAT_IDLE; // block tap down events when idle
            now = mUno.getNow();
            size = mUno.getPlayer(now).getHandCards().size();
            card = mUno.play(now, index, color);
            mSelectedCard = null;
            mSoundPool.play(sndPlay, 1.0f, 1.0f, 1, 0, 1.0f);
            if (card != null) {
                // Animation
                image = card.image;
                switch (now) {
                    case Player.COM1:
                        height = 40 * size + 140;
                        x = 160;
                        y = 360 - height / 2 + 40 * index;
                        roi = new Rect(x, y, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                        try {
                            Thread.sleep(300);
                        } // try
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // catch (InterruptedException e)
                        break; // case Player.COM1

                    case Player.COM2:
                        width = 45 * size + 75;
                        x = 640 - width / 2 + 45 * index;
                        y = 50;
                        roi = new Rect(x, y, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                        try {
                            Thread.sleep(300);
                        } // try
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // catch (InterruptedException e)
                        break; // case Player.COM2

                    case Player.COM3:
                        height = 40 * size + 140;
                        x = 1000;
                        y = 360 - height / 2 + 40 * index;
                        roi = new Rect(x, y, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                        try {
                            Thread.sleep(300);
                        } // try
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } // catch (InterruptedException e)
                        break; // case Player.COM3

                    default:
                        break; // default
                } // switch (now)

                if (size == 2) {
                    mSoundPool.play(sndUno, 1.0f, 1.0f, 1, 0, 1.0f);
                } // if (size == 2)

                if (size == 1) {
                    // The player in action becomes winner when it played the
                    // final card in its hand successfully
                    mWinner = now;
                    mStatus = STAT_GAME_OVER;
                    onStatusChanged(mStatus);
                } // if (size == 1)
                else {
                    // When the played card is an action card or a wild card,
                    // do the necessary things according to the game rule
                    switch (card.content) {
                        case DRAW2:
                            next = mUno.switchNow();
                            message = NAME[now] + ": Let "
                                    + NAME[next] + " draw 2 cards";
                            refreshScreen(message);
                            try {
                                Thread.sleep(1500);
                            } // try
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } // catch (InterruptedException e)

                            draw(2, /* force */ true);
                            break; // case DRAW2

                        case SKIP:
                            next = mUno.switchNow();
                            if (next == Player.YOU) {
                                message = NAME[now] + ": Skip your turn";
                            } // if (next == Player.YOU)
                            else {
                                message = NAME[now] + ": Skip "
                                        + NAME[next] + "'s turn";
                            } // else

                            refreshScreen(message);
                            try {
                                Thread.sleep(1500);
                            } // try
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } // catch (InterruptedException e)

                            mStatus = mUno.switchNow();
                            onStatusChanged(mStatus);
                            break; // case SKIP

                        case REV:
                            if (mUno.switchDirection() == Uno.DIR_LEFT) {
                                message = NAME[now] + ": Change direction to "
                                        + "CLOCKWISE";
                            } // if (mUno.switchDirection() == Uno.DIR_LEFT)
                            else {
                                message = NAME[now] + ": Change direction to "
                                        + "COUNTER CLOCKWISE";
                            } // else

                            refreshScreen(message);
                            try {
                                Thread.sleep(1500);
                            } // try
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } // catch (InterruptedException e)

                            mStatus = mUno.switchNow();
                            onStatusChanged(mStatus);
                            break; // case REV

                        case WILD:
                            message = NAME[now]
                                    + ": Change the following legal color";
                            refreshScreen(message);
                            try {
                                Thread.sleep(1500);
                            } // try
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } // catch (InterruptedException e)

                            mStatus = mUno.switchNow();
                            onStatusChanged(mStatus);
                            break; // case WILD

                        case WILD_DRAW4:
                            next = mUno.getNext();
                            message = NAME[now] + ": Let "
                                    + NAME[next] + " draw 4 cards";
                            refreshScreen(message);
                            try {
                                Thread.sleep(1500);
                            } // try
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } // catch (InterruptedException e)

                            mStatus = next;
                            mChallengeAsk = true;
                            onStatusChanged(mStatus);
                            break; // case WILD_DRAW4

                        default:
                            message = NAME[now] + ": " + card;
                            refreshScreen(message);
                            try {
                                Thread.sleep(1500);
                            } // try
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } // catch (InterruptedException e)

                            mStatus = mUno.switchNow();
                            onStatusChanged(mStatus);
                            break; // default
                    } // switch (card.content)
                } // else
            } // if (card != null)
        }).start();
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
    private void draw(int count, boolean force) {
        new Thread(() -> {
            Rect roi;
            Mat image;
            int i, now;
            String message;

            mStatus = STAT_IDLE; // block tap down events when idle
            now = mUno.getNow();
            mSelectedCard = null;
            for (i = 0; i < count; ++i) {
                mDrawnCard = mUno.draw(now, force);
                if (mDrawnCard != null) {
                    switch (now) {
                        case Player.COM1:
                            image = mUno.getBackImage();
                            roi = new Rect(160, 270, 121, 181);
                            if (count == 1) {
                                message = NAME[now] + ": Draw a card";
                            } // if (count == 1)
                            else {
                                message = NAME[now]
                                        + ": Draw " + count + " cards";
                            } // else
                            break; // case Player.COM1

                        case Player.COM2:
                            image = mUno.getBackImage();
                            roi = new Rect(580, 50, 121, 181);
                            if (count == 1) {
                                message = NAME[now] + ": Draw a card";
                            } // if (count == 1)
                            else {
                                message = NAME[now]
                                        + ": Draw " + count + " cards";
                            } // else
                            break; // case Player.COM2

                        case Player.COM3:
                            image = mUno.getBackImage();
                            roi = new Rect(1000, 270, 121, 181);
                            if (count == 1) {
                                message = NAME[now] + ": Draw a card";
                            } // if (count == 1)
                            else {
                                message = NAME[now] +
                                        ": Draw " + count + " cards";
                            } // else
                            break; // case Player.COM3

                        default:
                            image = mDrawnCard.image;
                            roi = new Rect(580, 490, 121, 181);
                            message = NAME[now] + ": Draw " + mDrawnCard.name;
                            break; // default
                    } // switch (now)

                    // Animation
                    image.copyTo(new Mat(mScr, roi), image);
                    Utils.matToBitmap(mScr, mBmp);
                    mHandler.post(() -> mImgScreen.setImageBitmap(mBmp));
                    mSoundPool.play(sndDraw, 1.0f, 1.0f, 1, 0, 1.0f);
                    try {
                        Thread.sleep(300);
                        refreshScreen(message);
                        Thread.sleep(300);
                    } // try
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    } // catch (InterruptedException)
                } // if (mDrawnCard != null)
                else {
                    message = NAME[now] + " cannot hold more than "
                            + Uno.MAX_HOLD_CARDS + " cards";
                    refreshScreen(message);
                    break;
                } // else
            } // for (i = 0; i < count; ++i)

            try {
                Thread.sleep(750);
            } // try
            catch (InterruptedException e) {
                e.printStackTrace();
            } // catch (InterruptedException e)

            if (count == 1 &&
                    mDrawnCard != null &&
                    mUno.isLegalToPlay(mDrawnCard)) {
                // Player drew one card by itself, the drawn card
                // can be played immediately if it's legal to play
                mStatus = now;
                mImmPlayAsk = true;
            } // if (count == 1 && ...)
            else {
                refreshScreen(NAME[now] + ": Pass");
                try {
                    Thread.sleep(750);
                } // try
                catch (InterruptedException e) {
                    e.printStackTrace();
                } // catch (InterruptedException e)

                mStatus = mUno.switchNow();
            } // else

            onStatusChanged(mStatus);
        }).start();
    } // draw(int, boolean)

    /**
     * Triggered on challenge chance. When a player played a [wild +4], the next
     * player can challenge its legality. Only when you have no cards that match
     * the previous played card's color, you can play a [wild +4].
     * Next player does not challenge: next player draw 4 cards;
     * Challenge success: current player draw 4 cards;
     * Challenge failure: next player draw 6 cards.
     *
     * @param challenged Whether the next player (challenger) challenged current
     *                   player(be challenged)'s [wild +4].
     */
    private void onChallengeChance(boolean challenged) {
        new Thread(() -> {
            Card next2last;
            String message;
            List<Card> recent;
            boolean draw4IsLegal;
            int curr, challenger;
            Color colorBeforeDraw4;

            mStatus = STAT_IDLE; // block tap down events when idle
            mChallenged = challenged;
            mChallengeAsk = false;
            if (challenged) {
                curr = mUno.getNow();
                challenger = mUno.getNext();
                recent = mUno.getRecent();
                next2last = recent.get(recent.size() - 2);
                colorBeforeDraw4 = next2last.getRealColor();
                if (curr == Player.YOU) {
                    message = NAME[challenger]
                            + " doubted that you still have "
                            + CL[colorBeforeDraw4.ordinal()];
                } // if (curr == Player.YOU)
                else {
                    message = NAME[challenger]
                            + " doubted that " + NAME[curr]
                            + " still has " + CL[colorBeforeDraw4.ordinal()];
                } // else

                refreshScreen(message);
                try {
                    Thread.sleep(1500);
                } // try
                catch (InterruptedException e) {
                    e.printStackTrace();
                } // catch (InterruptedException e)

                draw4IsLegal = true;
                for (Card card : mUno.getPlayer(curr).getHandCards()) {
                    if (card.getRealColor() == colorBeforeDraw4) {
                        // Found a card that matches the next-to-last recent
                        // played card's color, [wild +4] is illegally used
                        draw4IsLegal = false;
                        break;
                    } // if (card.getRealColor() == colorBeforeDraw4)
                } // for (Card card : mUno.getPlayer(curr).getHandCards())

                if (draw4IsLegal) {
                    // Challenge failure, challenger draws 6 cards
                    if (challenger == Player.YOU) {
                        message = "Challenge failure, you draw 6 cards";
                    } // if (challenger == Player.YOU)
                    else {
                        message = "Challenge failure, "
                                + NAME[challenger] + " draws 6 cards";
                    } // else

                    refreshScreen(message);
                    try {
                        Thread.sleep(1500);
                    } // try
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    } // catch (InterruptedException e)

                    mChallenged = false;
                    mUno.switchNow();
                    draw(6, /* force */ true);
                } // if (draw4IsLegal)
                else {
                    // Challenge success, who played [wild +4] draws 4 cards
                    if (curr == Player.YOU) {
                        message = "Challenge success, you draw 4 cards";
                    } // if (curr == Player.YOU)
                    else {
                        message = "Challenge success, "
                                + NAME[curr] + " draws 4 cards";
                    } // else

                    refreshScreen(message);
                    try {
                        Thread.sleep(1500);
                    } // try
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    } // catch (InterruptedException e)

                    mChallenged = false;
                    draw(4, /* force */ true);
                } // else
            } // if (challenged)
            else {
                mUno.switchNow();
                draw(4, /* force */ true);
            } // else
        }).start();
    } // onChallengeChance(boolean)

    /**
     * Triggered when a touch event occurred.
     *
     * @param v     Touch event occurred on which view object.
     * @param event Which event occurred. Call event.getAction() to get the type
     *              of occurred touch event, such as MotionEvent.ACTION_DOWN.
     * @return True because our listener has consumed the touch events.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Card card;
        List<Card> hand;
        Runnable delayedTask;
        int x, y, index, size, width, height, startX;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Only response to tap down events, and ignore the others
            width = mUno.getBackground().width();
            height = mUno.getBackground().height();
            x = (int) (event.getX() * width / v.getWidth());
            y = (int) (event.getY() * height / v.getHeight());
            if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260) {
                // <AUTO> button
                // In player's action, automatically play or draw cards by AI
                mAuto = !mAuto;
                switch (mStatus) {
                    case Player.YOU:
                        onStatusChanged(mStatus);
                        break; // case Player.YOU

                    case STAT_WILD_COLOR:
                        mStatus = Player.YOU;
                        onStatusChanged(mStatus);
                        break; // case STAT_WILD_COLOR

                    default:
                        Imgproc.putText(
                                /* img       */ mScr,
                                /* text      */ "<AUTO>",
                                /* org       */ new Point(1130, 700),
                                /* fontFace  */ FONT_SANS,
                                /* fontScale */ 1.0,
                                /* color     */ mAuto ? RGB_YELLOW : RGB_WHITE
                        ); // Imgproc.putText()

                        Utils.matToBitmap(mScr, mBmp);
                        mImgScreen.setImageBitmap(mBmp);
                        break; // default
                } // switch (mStatus)
            } // if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260)
            else switch (mStatus) {
                case STAT_WELCOME:
                    if (y >= 60 && y <= 240) {
                        if (x >= 490 && x <= 610) {
                            // Difficulty: EASY
                            mUno.setDifficulty(Uno.LV_EASY);
                            onStatusChanged(mStatus);
                        } // if (x >= 490 && x <= 610)
                        else if (x >= 670 && x <= 790) {
                            // Difficulty: HARD
                            mUno.setDifficulty(Uno.LV_HARD);
                            onStatusChanged(mStatus);
                        } // else if (x >= 670 && x <= 790)
                    } // if (y >= 60 && y <= 240)
                    else if (y >= 270 && y <= 450) {
                        if (x >= 490 && x <= 610) {
                            // 3-player mode
                            mUno.setPlayers(3);
                            onStatusChanged(mStatus);
                        } // if (x >= 490 && x <= 610)
                        else if (x >= 670 && x <= 790) {
                            // 4-player mode
                            mUno.setPlayers(4);
                            onStatusChanged(mStatus);
                        } // else if (x >= 670 && x <= 790)
                    } // else if (y >= 270 && y <= 450)
                    else if (y >= 520 && y <= 700) {
                        if (x >= 580 && x <= 700) {
                            // UNO button, start a new game
                            mStatus = STAT_NEW_GAME;
                            onStatusChanged(mStatus);
                        } // if (x >= 580 && x <= 700)
                    } // else if (y >= 520 && y <= 700)
                    break; // case STAT_WELCOME

                case Player.YOU:
                    if (mAuto) {
                        // Do operations automatically by AI strategies
                        break; // case Player.YOU
                    } // if (mAuto)
                    else if (mImmPlayAsk) {
                        // Asking if you want to play the drawn card immediately
                        if (y >= 520 && y <= 700) {
                            hand = mUno.getPlayer(Player.YOU).getHandCards();
                            size = hand.size();
                            width = 60 * size + 60;
                            startX = 640 - width / 2;
                            if (x >= startX && x <= startX + width) {
                                // Hand card area
                                // Calculate which card clicked
                                index = (x - startX) / 60;
                                if (index >= size) {
                                    index = size - 1;
                                } // if (index >= size)

                                // If clicked the drawn card, play it
                                card = hand.get(index);
                                if (card == mDrawnCard) {
                                    mImmPlayAsk = false;
                                    if (card.isWild() && size > 1) {
                                        // Store index value as class member.
                                        // This value will be used after the
                                        // wild color determined.
                                        mWildIndex = index;
                                        mStatus = STAT_WILD_COLOR;
                                        onStatusChanged(mStatus);
                                    } // if (card.isWild() && size > 1)
                                    else {
                                        play(index, card.getRealColor());
                                    } // else
                                } // if (card == mDrawnCard)
                            } // if (x >= startX && x <= startX + width)
                        } // if (y >= 520 && y <= 700)
                        else if (x > 310 && x < 500) {
                            if (y > 220 && y < 315) {
                                // YES button, play the drawn card
                                hand = mUno.getPlayer(mStatus).getHandCards();
                                size = hand.size();
                                for (index = 0; index < size; ++index) {
                                    card = hand.get(index);
                                    if (card == mDrawnCard) {
                                        mImmPlayAsk = false;
                                        if (card.isWild() && size > 1) {
                                            // Store index value as class
                                            // member. This value will be used
                                            // after the wild color determined.
                                            mWildIndex = index;
                                            mStatus = STAT_WILD_COLOR;
                                            onStatusChanged(mStatus);
                                        } // if (card.isWild() && size > 1)
                                        else {
                                            play(index, card.getRealColor());
                                        } // else
                                        break;
                                    } // if (card == mDrawnCard)
                                } // for (index = 0; index < size; ++index)
                            } // if (y > 220 && y < 315)
                            else if (y > 315 && y < 410) {
                                // NO button, go to next player's round
                                mStatus = STAT_IDLE;
                                mImmPlayAsk = false;
                                refreshScreen(NAME[Player.YOU] + ": Pass");
                                delayedTask = () -> {
                                    mStatus = mUno.switchNow();
                                    onStatusChanged(mStatus);
                                }; // delayedTask = () -> {}
                                mHandler.postDelayed(delayedTask, 750);
                            } // else if (y > 315 && y < 410)
                        } // else if (x > 310 && x < 500)
                    } // else if (mImmPlayAsk)
                    else if (mChallengeAsk) {
                        // Asking if you want to challenge your previous player
                        if (x > 310 && x < 500) {
                            if (y > 220 && y < 315) {
                                // YES button, challenge wild +4
                                onChallengeChance(true);
                            } // if (y > 220 && y < 315)
                            else if (y > 315 && y < 410) {
                                // NO button, do not challenge wild +4
                                onChallengeChance(false);
                            } // else if (y > 315 && y < 410)
                        } // if (x > 310 && x < 500)
                    } // else if (mChallengeAsk)
                    else if (y >= 520 && y <= 700) {
                        hand = mUno.getPlayer(Player.YOU).getHandCards();
                        size = hand.size();
                        width = 60 * size + 60;
                        startX = 640 - width / 2;
                        if (x >= startX && x <= startX + width) {
                            // Hand card area
                            // Calculate which card clicked by the X-coordinate
                            index = (x - startX) / 60;
                            if (index >= size) {
                                index = size - 1;
                            } // if (index >= size)

                            // Try to play it
                            card = hand.get(index);
                            if (card != mSelectedCard) {
                                mSelectedCard = card;
                                onStatusChanged(mStatus);
                            } // if (card != mSelectedCard)
                            else if (card.isWild() && size > 1) {
                                // Store index value as class member. This value
                                // will be used after the wild color determined.
                                mWildIndex = index;
                                mStatus = STAT_WILD_COLOR;
                                onStatusChanged(mStatus);
                            } // else if (card.isWild() && size > 1)
                            else if (mUno.isLegalToPlay(card)) {
                                play(index, card.getRealColor());
                            } // else if (mUno.isLegalToPlay(card))
                            else {
                                refreshScreen("Cannot play " + card.name);
                            } // else
                        } // if (x >= startX && x <= startX + width)
                        else {
                            // Blank area, cancel your selection
                            mSelectedCard = null;
                            onStatusChanged(mStatus);
                        } // else
                    } // if (y >= 520 && y <= 700)
                    else if (y >= 270 && y <= 450 && x >= 338 && x <= 458) {
                        // Card deck area, draw a card
                        draw(1, /* force */ false);
                    } // else if (y >= 270 && y <= 450 && x >= 338 && x <= 458)
                    break; // case Player.YOU

                case STAT_WILD_COLOR:
                    if (y > 220 && y < 315) {
                        if (x > 310 && x < 405) {
                            // Red sector
                            mStatus = Player.YOU;
                            play(mWildIndex, Color.RED);
                        } // if (x > 310 && x < 405)
                        else if (x > 405 && x < 500) {
                            // Blue sector
                            mStatus = Player.YOU;
                            play(mWildIndex, Color.BLUE);
                        } // else if (x > 405 && x < 500)
                    } // if (y > 220 && y < 315)
                    else if (y > 315 && y < 410) {
                        if (x > 310 && x < 405) {
                            // Yellow sector
                            mStatus = Player.YOU;
                            play(mWildIndex, Color.YELLOW);
                        } // if (x > 310 && x < 405)
                        else if (x > 405 && x < 500) {
                            // Green sector
                            mStatus = Player.YOU;
                            play(mWildIndex, Color.GREEN);
                        } // else if (x > 405 && x < 500)
                    } // else if (y > 315 && y < 410)
                    break; // case STAT_WILD_COLOR

                case STAT_GAME_OVER:
                    if (y >= 270 && y <= 450 && x >= 338 && x <= 458) {
                        // Card deck area, start a new game
                        mStatus = STAT_NEW_GAME;
                        onStatusChanged(mStatus);
                    } // if (y >= 270 && y <= 450 && x >= 338 && x <= 458)
                    break; // case STAT_GAME_OVER

                default:
                    break; // default
            } // else switch (mStatus)
        } // if (event.getAction() == MotionEvent.ACTION_DOWN)

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
    protected void onPause() {
        SharedPreferences sp;
        SharedPreferences.Editor editor;

        if (OPENCV_INIT_SUCCESS) {
            mMediaPlayer.pause();
            sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
            editor = sp.edit();
            editor.putInt("easyWin", mEasyWin);
            editor.putInt("hardWin", mHardWin);
            editor.putInt("easyWin3", mEasyWin3);
            editor.putInt("hardWin3", mHardWin3);
            editor.putInt("easyTotal", mEasyTotal);
            editor.putInt("hardTotal", mHardTotal);
            editor.putInt("easyTotal3", mEasyTotal3);
            editor.putInt("hardTotal3", mHardTotal3);
            editor.putInt("players", mUno.getPlayers());
            editor.putInt("difficulty", mUno.getDifficulty());
            editor.apply();
        } // if (OPENCV_INIT_SUCCESS)

        super.onPause();
    } // onPause()

    /**
     * Triggered when activity destroyed.
     */
    @Override
    protected void onDestroy() {
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