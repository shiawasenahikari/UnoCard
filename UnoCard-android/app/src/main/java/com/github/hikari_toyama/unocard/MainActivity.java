////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import org.opencv.core.Core;
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
    private static final String[] NAME = {"YOU", "WEST", "NORTH", "EAST"};
    private static final Scalar RGB_YELLOW = new Scalar(0xFF, 0xAA, 0x11);
    private static final Scalar RGB_WHITE = new Scalar(0xCC, 0xCC, 0xCC);
    private static final Scalar RGB_GREEN = new Scalar(0x55, 0xAA, 0x55);
    private static final Scalar RGB_BLUE = new Scalar(0x55, 0x55, 0xFF);
    private static final Scalar RGB_RED = new Scalar(0xFF, 0x55, 0x55);
    private static final int FONT_SANS = Core.FONT_HERSHEY_DUPLEX;
    private static final int STAT_WILD_COLOR = 0x5555;
    private static final int STAT_GAME_OVER = 0x4444;
    private static final int STAT_NEW_GAME = 0x3333;
    private static final int STAT_WELCOME = 0x2222;
    private static final int STAT_IDLE = 0x1111;
    private static final int LV_HARD = 1;
    private static final int LV_EASY = 0;
    private boolean mChallengeAsk;
    private ImageView mImgScreen;
    private boolean mChallenged;
    private boolean mImmPlayAsk;
    private Handler mHandler;
    private Card mDrawnCard;
    private int mDifficulty;
    private int mWildIndex;
    private int mHardTotal;
    private int mEasyTotal;
    private boolean mAuto;
    private int mHardWin;
    private int mEasyWin;
    private int mStatus;
    private int mWinner;
    private Bitmap mBmp;
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
        sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
        mHandler = new Handler();
        mEasyWin = sp.getInt("easyWin", 0);
        mHardWin = sp.getInt("hardWin", 0);
        mEasyTotal = sp.getInt("easyTotal", 0);
        mHardTotal = sp.getInt("hardTotal", 0);
        if (OPENCV_INIT_SUCCESS) {
            mAI = new AI(this);
            mUno = Uno.getInstance(this);
            mWinner = Player.YOU;
            mStatus = STAT_WELCOME;
            mScr = mUno.getBackground().clone();
            mBmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
            mImgScreen = findViewById(R.id.imgMainScreen);
            refreshScreen("WELCOME TO UNO CARD GAME");
            mImgScreen.setOnTouchListener(this);
        } // if (OPENCV_INIT_SUCCESS)
        else {
            dialog = new UnsupportedDeviceDialog();
            dialog.show(getSupportFragmentManager(), "UnsupportedDeviceDialog");
        } // else
    } // onCreate()

    /**
     * AI Strategies (Difficulty: EASY).
     */
    private void easyAI() {
        int idxBest;
        Color[] bestColor;

        if (mChallengeAsk) {
            onChallenge(mStatus, mAI.needToChallenge(mStatus));
        } // if (mChallengeAsk)
        else {
            bestColor = new Color[1];
            idxBest = mAI.easyAI_bestCardIndexFor(
                    /* whom      */ mStatus,
                    /* drawnCard */ mImmPlayAsk ? mDrawnCard : null,
                    /* outColor  */ bestColor
            ); // idxBest = mAI.easyAI_bestCardIndexFor()

            if (idxBest >= 0) {
                // Found an appropriate card to play
                mImmPlayAsk = false;
                play(idxBest, bestColor[0]);
            } // if (idxBest >= 0)
            else {
                // No appropriate cards to play, or no card is legal to play
                if (mImmPlayAsk) {
                    mImmPlayAsk = false;
                    pass(mStatus);
                } // if (mImmPlayAsk)
                else {
                    draw(mStatus, 1);
                } // else
            } // else
        } // else
    } // easyAI()

    /**
     * AI Strategies (Difficulty: HARD).
     */
    private void hardAI() {
        int idxBest;
        Color[] bestColor;

        if (mChallengeAsk) {
            onChallenge(mStatus, mAI.needToChallenge(mStatus));
        } // if (mChallengeAsk)
        else {
            bestColor = new Color[1];
            idxBest = mAI.hardAI_bestCardIndexFor(
                    /* whom      */ mStatus,
                    /* drawnCard */ mImmPlayAsk ? mDrawnCard : null,
                    /* outColor  */ bestColor
            ); // idxBest = mAI.hardAI_bestCardIndexFor()

            if (idxBest >= 0) {
                // Found an appropriate card to play
                mImmPlayAsk = false;
                play(idxBest, bestColor[0]);
            } // if (idxBest >= 0)
            else {
                // No appropriate cards to play, or no card is legal to play
                if (mImmPlayAsk) {
                    mImmPlayAsk = false;
                    pass(mStatus);
                } // if (mImmPlayAsk)
                else {
                    draw(mStatus, 1);
                } // else
            } // else
        } // else
    } // hardAI()

    /**
     * Pass someone's round.
     *
     * @param who Pass whose round. Must be one of the following values:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    private void pass(int who) {
        Runnable delayedTask;

        if (who >= 0 && who < 4) {
            mStatus = STAT_IDLE; // block tap down events when idle
            refreshScreen(NAME[who] + ": Pass");
            delayedTask = () -> {
                mStatus = (who + mUno.getDirection()) % 4;
                onStatusChanged(mStatus);
            }; // delayedTask = () -> {}
            mHandler.postDelayed(delayedTask, 750);
        } // if (who >= 0 && who < 4)
    } // pass()

    /**
     * Triggered when the value of member [mStatus] changed.
     *
     * @param status New status value.
     */
    private void onStatusChanged(int status) {
        Rect rect;
        Size axes;
        Point center;
        Mat areaToErase;
        Runnable delayedTask;

        switch (status) {
            case STAT_NEW_GAME:
                // New game
                if (mDifficulty == LV_EASY) {
                    ++mEasyTotal;
                } // if (mDifficulty == LV_EASY)
                else {
                    ++mHardTotal;
                } // else

                mUno.start();
                refreshScreen("GET READY");
                delayedTask = () -> {
                    mStatus = mWinner;
                    onStatusChanged(mStatus);
                }; // delayedTask = () -> {}
                mHandler.postDelayed(delayedTask, 2000);
                break; // case STAT_NEW_GAME

            case Player.YOU:
                // Your turn, select a hand card to play, or draw a card
                if (mAuto) {
                    if (mDifficulty == LV_EASY) {
                        easyAI();
                    } // if (mDifficulty == LV_EASY)
                    else {
                        hardAI();
                    } // else
                } // if (mAuto)
                else if (mImmPlayAsk) {
                    refreshScreen("^ Play " + mDrawnCard.getName() + "?");
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
                    mImgScreen.setImageBitmap(mBmp);
                } // else if (mImmPlayAsk)
                else if (mChallengeAsk) {
                    refreshScreen("^ Challenge the legality of Wild +4?");
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
                    mImgScreen.setImageBitmap(mBmp);
                } // else if (mChallengeAsk)
                else {
                    refreshScreen("Your turn, play or draw a card");
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
                mImgScreen.setImageBitmap(mBmp);
                break; // case STAT_WILD_COLOR

            case Player.COM1:
            case Player.COM2:
            case Player.COM3:
                // AI players' turn
                if (mDifficulty == LV_EASY) {
                    easyAI();
                } // if (mDifficulty == LV_EASY)
                else {
                    hardAI();
                } // else
                break; // case Player.COM1, Player.COM2, Player.COM3

            case STAT_GAME_OVER:
                // Game over
                if (mWinner == Player.YOU) {
                    if (mDifficulty == LV_EASY) {
                        ++mEasyWin;
                    } // if (mDifficulty == LV_EASY)
                    else {
                        ++mHardWin;
                    } // else
                } // if (mWinner == Player.YOU)

                refreshScreen("Click the card deck to restart");
                break; // case STAT_GAME_OVER

            default:
                break; // default
        } // switch (status)
    } // onStatusChanged()

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
        int remain, used, easyRate, hardRate;
        int i, status, next, size, width, height;

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
        if (mAuto) {
            Imgproc.putText(mScr, "<AUTO>", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (mAuto)
        else {
            Imgproc.putText(mScr, "<AUTO>", point, FONT_SANS, 1.0, RGB_WHITE);
        } // else

        // For welcome screen, only show difficulty buttons and winning rates
        if (status == STAT_WELCOME) {
            image = mUno.getEasyImage();
            roi = new Rect(490, 270, 121, 181);
            image.copyTo(new Mat(mScr, roi), image);
            image = mUno.getHardImage();
            roi.x = 670;
            roi.y = 270;
            image.copyTo(new Mat(mScr, roi), image);
            easyRate = (mEasyTotal == 0 ? 0 : 100 * mEasyWin / mEasyTotal);
            hardRate = (mHardTotal == 0 ? 0 : 100 * mHardWin / mHardTotal);
            info = easyRate + "% WinRate " + hardRate + "%";
            textSize = Imgproc.getTextSize(info, FONT_SANS, 1.0, 1, null);
            point.x = 640 - textSize.width / 2;
            point.y = 250;
            Imgproc.putText(mScr, info, point, FONT_SANS, 1.0, RGB_WHITE);
            Utils.matToBitmap(mScr, mBmp);
            mImgScreen.setImageBitmap(mBmp);
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
            if (recent.getContent() == Content.WILD) {
                image = mUno.getColoredWildImage(recent.getRealColor());
            } // if (recent.getContent() == Content.WILD)
            else if (recent.getContent() == Content.WILD_DRAW4) {
                image = mUno.getColoredWildDraw4Image(recent.getRealColor());
            } // else if (recent.getContent() == Content.WILD_DRAW4)
            else {
                image = recent.getImage();
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
            next = (Player.COM1 + mUno.getDirection()) % 4;
            beChallenged = mChallenged && status == next;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained cards to everyone
                // when being challenged or game over
                for (Card card : hand) {
                    image = card.getImage();
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
            point.x = 611;
            point.y = 121;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            width = 45 * size + 75;
            roi.x = 640 - width / 2;
            roi.y = 20;
            next = (Player.COM2 + mUno.getDirection()) % 4;
            beChallenged = mChallenged && status == next;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained hand cards
                // when being challenged or game over
                for (Card card : hand) {
                    image = card.getImage();
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
            next = (Player.COM3 + mUno.getDirection()) % 4;
            beChallenged = mChallenged && status == next;
            if (beChallenged || status == STAT_GAME_OVER) {
                // Show remained hand cards
                // when being challenged or game over
                for (Card card : hand) {
                    image = card.getImage();
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
            roi.y = 520;
            for (Card card : hand) {
                switch (status) {
                    case Player.YOU:
                        if (mImmPlayAsk) {
                            image = (card == mDrawnCard) ?
                                    card.getImage() :
                                    card.getDarkImg();
                        } // if (mImmPlayAsk)
                        else if (mChallengeAsk || mChallenged) {
                            image = card.getImage();
                        } // else if (mChallengeAsk || mChallenged)
                        else {
                            image = (mUno.isLegalToPlay(card)) ?
                                    card.getImage() :
                                    card.getDarkImg();
                        } // else
                        break; // case Player.YOU

                    case STAT_GAME_OVER:
                        image = card.getImage();
                        break; // case STAT_GAME_OVER

                    default:
                        image = card.getDarkImg();
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
        mImgScreen.setImageBitmap(mBmp);
    } // refreshScreen()

    /**
     * The player in action play a card.
     *
     * @param index Play which card. Pass the corresponding card's index of the
     *              player's hand cards.
     * @param color Optional, available when the card to play is a wild card.
     *              Pass the specified following legal color.
     */
    private void play(int index, Color color) {
        Rect roi;
        Mat image;
        Card card;
        Runnable delayedTask;
        int x, y, now, size, width, height;

        now = mStatus;
        mStatus = STAT_IDLE; // block tap down events when idle
        size = mUno.getPlayer(now).getHandCards().size();
        card = mUno.play(now, index, color);
        if (card != null) {
            image = card.getImage();
            switch (now) {
                case Player.COM1:
                    height = 40 * size + 140;
                    x = 160;
                    y = 360 - height / 2 + 40 * index;
                    break; // case Player.COM1

                case Player.COM2:
                    width = 45 * size + 75;
                    x = 640 - width / 2 + 45 * index;
                    y = 70;
                    break; // case Player.COM2

                case Player.COM3:
                    height = 40 * size + 140;
                    x = 1000;
                    y = 360 - height / 2 + 40 * index;
                    break; // case Player.COM3

                default:
                    width = 60 * size + 60;
                    x = 640 - width / 2 + 60 * index;
                    y = 470;
                    break; // default
            } // switch (now)

            // Animation
            roi = new Rect(x, y, 121, 181);
            image.copyTo(new Mat(mScr, roi), image);
            Utils.matToBitmap(mScr, mBmp);
            mImgScreen.setImageBitmap(mBmp);
            delayedTask = () -> {
                String message;
                int next, direction;

                if (mUno.getPlayer(now).getHandCards().size() == 0) {
                    // The player in action becomes winner when it played the
                    // final card in its hand successfully
                    mWinner = now;
                    mStatus = STAT_GAME_OVER;
                    onStatusChanged(mStatus);
                } // if (mUno.getPlayer(now).getHandCards().size() == 0)
                else {
                    // When the played card is an action card or a wild card,
                    // do the necessary things according to the game rule
                    switch (card.getContent()) {
                        case DRAW2:
                            direction = mUno.getDirection();
                            next = (now + direction) % 4;
                            message = NAME[now] + ": Let "
                                    + NAME[next] + " draw 2 cards";
                            refreshScreen(message);
                            mHandler.postDelayed(() -> draw(next, 2), 1500);
                            break; // case DRAW2

                        case SKIP:
                            direction = mUno.getDirection();
                            next = (now + direction) % 4;
                            if (next == Player.YOU) {
                                message = NAME[now] + ": Skip your turn";
                            } // if (next == Player.YOU)
                            else {
                                message = NAME[now] + ": Skip "
                                        + NAME[next] + "'s turn";
                            } // else

                            refreshScreen(message);
                            mHandler.postDelayed(() -> {
                                mStatus = (next + direction) % 4;
                                onStatusChanged(mStatus);
                            }, 1500); // mHandler.postDelayed()
                            break; // case SKIP

                        case REV:
                            direction = mUno.switchDirection();
                            if (direction == Uno.DIR_LEFT) {
                                message = NAME[now] + ": Change direction to "
                                        + "CLOCKWISE";
                            } // if (direction == Uno.DIR_LEFT)
                            else {
                                message = NAME[now] + ": Change direction to "
                                        + "COUNTER CLOCKWISE";
                            } // else

                            refreshScreen(message);
                            mHandler.postDelayed(() -> {
                                mStatus = (now + direction) % 4;
                                onStatusChanged(mStatus);
                            }, 1500); // mHandler.postDelayed()
                            break; // case REV

                        case WILD:
                            direction = mUno.getDirection();
                            message = NAME[now]
                                    + ": Change the following legal color";
                            refreshScreen(message);
                            mHandler.postDelayed(() -> {
                                mStatus = (now + direction) % 4;
                                onStatusChanged(mStatus);
                            }, 1500); // mHandler.postDelayed()
                            break; // case WILD

                        case WILD_DRAW4:
                            direction = mUno.getDirection();
                            next = (now + direction) % 4;
                            message = NAME[now] + ": Let "
                                    + NAME[next] + " draw 4 cards";
                            refreshScreen(message);
                            mHandler.postDelayed(() -> {
                                mStatus = next;
                                mChallengeAsk = true;
                                onStatusChanged(mStatus);
                            }, 1500); // mHandler.postDelayed()
                            break; // case WILD_DRAW4

                        default:
                            direction = mUno.getDirection();
                            message = NAME[now] + ": " + card.getName();
                            refreshScreen(message);
                            mHandler.postDelayed(() -> {
                                mStatus = (now + direction) % 4;
                                onStatusChanged(mStatus);
                            }, 1500); // mHandler.postDelayed()
                            break; // default
                    } // switch (card.getContent())
                } // else
            }; // delayedTask = () -> {}
            mHandler.postDelayed(delayedTask, 300);
        } // if (card != null)
    } // play()

    /**
     * Let a player draw one or more cards, and skip its turn.
     *
     * @param who   Who draws cards. Must be one of the following values:
     *              Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param count How many cards to draw.
     * @see DrawLoop
     */
    private void draw(int who, int count) {
        mStatus = STAT_IDLE; // block click events when idle

        // This is a time-consuming procedure, so we cannot use the traditional
        // for-loop. We need to create a Runnable object, and make a delayed
        // cycle by executing mHandler.postDelayed(this, delayedMs); in the
        // run() method of the created Runnable object.
        mHandler.post(new DrawLoop(who, count));
    } // draw()

    /**
     * Triggered on challenge chance. When a player played a [wild +4], the next
     * player can challenge its legality. Only when you have no cards that match
     * the previous played card's color, you can play a [wild +4].
     * Next player does not challenge: next player draw 4 cards;
     * Challenge success: current player draw 4 cards;
     * Challenge failure: next player draw 6 cards.
     *
     * @param challenger Who challenges. Must be one of the following values:
     *                   Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     * @param challenged Whether the challenger challenged the [wild +4].
     */
    private void onChallenge(int challenger, boolean challenged) {
        int prev;
        String message;
        Runnable delayedTask;

        mChallenged = challenged;
        mChallengeAsk = false;
        if (challenged) {
            prev = (challenger + 4 - mUno.getDirection()) % 4;
            message = NAME[challenger] + " challenged " + NAME[prev];
            refreshScreen(message);
            mStatus = STAT_IDLE; // block tap down click events when idle
            delayedTask = () -> {
                Card next2last;
                StringBuilder sb;
                List<Card> recent;
                boolean draw4IsLegal;
                Color colorBeforeDraw4;

                recent = mUno.getRecent();
                next2last = recent.get(recent.size() - 2);
                colorBeforeDraw4 = next2last.getRealColor();
                draw4IsLegal = true;
                for (Card card : mUno.getPlayer(prev).getHandCards()) {
                    if (card.getRealColor() == colorBeforeDraw4) {
                        // Found a card that matches the next-to-last recent
                        // played card's color, [wild +4] is illegally used
                        draw4IsLegal = false;
                        break;
                    } // if (card.getRealColor() == colorBeforeDraw4)
                } // for (Card card : mUno.getPlayer(prev).getHandCards())

                if (draw4IsLegal) {
                    // Challenge failure, challenger draws 6 cards
                    sb = new StringBuilder("Challenge failure, ");
                    sb.append(NAME[challenger]);
                    if (challenger == Player.YOU) {
                        sb.append(" draw 6 cards");
                    } // if (challenger == Player.YOU)
                    else {
                        sb.append(" draws 6 cards");
                    } // else

                    refreshScreen(sb.toString());
                    mHandler.postDelayed(() -> {
                        mChallenged = false;
                        draw(challenger, 6);
                    }, 1500);
                } // if (draw4IsLegal)
                else {
                    // Challenge success, who played [wild +4] draws 4 cards
                    sb = new StringBuilder("Challenge success, ");
                    sb.append(NAME[prev]);
                    if (prev == Player.YOU) {
                        sb.append(" draw 4 cards");
                    } // if (prev == Player.YOU)
                    else {
                        sb.append(" draws 4 cards");
                    } // else

                    refreshScreen(sb.toString());
                    mHandler.postDelayed(() -> {
                        mChallenged = false;
                        draw(prev, 4);
                    }, 1500);
                } // else
            }; // delayedTask = () -> {}
            mHandler.postDelayed(delayedTask, 1500);
        } // if (challenged)
        else {
            draw(challenger, 4);
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
    public boolean onTouch(View v, MotionEvent event) {
        Card card;
        Point point;
        List<Card> hand;
        int x, y, index, size, width, startX;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Only response to tap down events, and ignore the others
            x = (int) (event.getX() * mUno.getBgWidth() / v.getWidth());
            y = (int) (event.getY() * mUno.getBgHeight() / v.getHeight());
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
                        point = new Point(1130, 700);
                        if (mAuto) {
                            Imgproc.putText(
                                    /* img       */ mScr,
                                    /* text      */ "<AUTO>",
                                    /* org       */ point,
                                    /* fontFace  */ FONT_SANS,
                                    /* fontScale */ 1.0,
                                    /* color     */ RGB_YELLOW
                            ); // Imgproc.putText()
                        } // if (mAuto)
                        else {
                            Imgproc.putText(
                                    /* img       */ mScr,
                                    /* text      */ "<AUTO>",
                                    /* org       */ point,
                                    /* fontFace  */ FONT_SANS,
                                    /* fontScale */ 1.0,
                                    /* color     */ RGB_WHITE
                            ); // Imgproc.putText()
                        } // else

                        Utils.matToBitmap(mScr, mBmp);
                        mImgScreen.setImageBitmap(mBmp);
                        break; // default
                } // switch (mStatus)
            } // if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260)
            else switch (mStatus) {
                case STAT_WELCOME:
                    if (y >= 270 && y <= 450) {
                        if (x >= 490 && x <= 610) {
                            // Difficulty: EASY
                            mDifficulty = LV_EASY;
                            mStatus = STAT_NEW_GAME;
                            onStatusChanged(mStatus);
                        } // if (x >= 490 && x <= 610)
                        else if (x >= 670 && x <= 790) {
                            // Difficulty: HARD
                            mDifficulty = LV_HARD;
                            mStatus = STAT_NEW_GAME;
                            onStatusChanged(mStatus);
                        } // else if (x >= 670 && x <= 790)
                    } // if (y >= 270 && y <= 450)
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
                                mImmPlayAsk = false;
                                pass(mStatus);
                            } // else if (y > 315 && y < 410)
                        } // else if (x > 310 && x < 500)
                    } // else if (mImmPlayAsk)
                    else if (mChallengeAsk) {
                        // Asking if you want to challenge your previous player
                        if (x > 310 && x < 500) {
                            if (y > 220 && y < 315) {
                                // YES button, challenge wild +4
                                onChallenge(mStatus, true);
                            } // if (y > 220 && y < 315)
                            else if (y > 315 && y < 410) {
                                // NO button, do not challenge wild +4
                                onChallenge(mStatus, false);
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
                            if (card.isWild() && size > 1) {
                                // Store index value as class member. This value
                                // will be used after the wild color determined.
                                mWildIndex = index;
                                mStatus = STAT_WILD_COLOR;
                                onStatusChanged(mStatus);
                            } // if (card.isWild() && size > 1)
                            else if (mUno.isLegalToPlay(card)) {
                                play(index, card.getRealColor());
                            } // else if (mUno.isLegalToPlay(card))
                        } // if (x >= startX && x <= startX + width)
                    } // if (y >= 520 && y <= 700)
                    else if (y >= 270 && y <= 450 && x >= 338 && x <= 458) {
                        // Card deck area, draw a card
                        draw(Player.YOU, 1);
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

        sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putInt("easyWin", mEasyWin);
        editor.putInt("hardWin", mHardWin);
        editor.putInt("easyTotal", mEasyTotal);
        editor.putInt("hardTotal", mHardTotal);
        editor.apply();
        super.onPause();
    } // onPause()

    /**
     * Triggered when activity destroyed.
     */
    @Override
    protected void onDestroy() {
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

    /**
     * Runnable objects generated by draw() method.
     *
     * @see MainActivity#draw(int, int)
     */
    private class DrawLoop implements Runnable {
        private int who;
        private int count;
        private int times;

        /**
         * DrawLoop Constructor.
         *
         * @param who   Who draws cards. Must be one of the following values:
         *              Player.YOU, Player.COM1, Player.COM2, Player.COM3.
         * @param count How many cards to draw.
         */
        private DrawLoop(int who, int count) {
            this.who = who;
            this.count = count;
            this.times = 0;
        } // DrawLoop() (Class Constructor)

        /**
         * Loop task.
         */
        @Override
        public void run() {
            Rect roi;
            Mat image;
            String message;
            Runnable delayedTask;

            mDrawnCard = mUno.draw(who);
            if (mDrawnCard != null) {
                switch (who) {
                    case Player.COM1:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(160, 270, 121, 181);
                        break; // case Player.COM1

                    case Player.COM2:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(580, 70, 121, 181);
                        break; // case Player.COM2

                    case Player.COM3:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(1000, 270, 121, 181);
                        break; // case Player.COM3

                    default:
                        message = NAME[who] + ": Draw " + mDrawnCard.getName();
                        image = mDrawnCard.getImage();
                        roi = new Rect(580, 470, 121, 181);
                        break; // default
                } // switch (who)

                // Animation
                image.copyTo(new Mat(mScr, roi), image);
                Utils.matToBitmap(mScr, mBmp);
                mImgScreen.setImageBitmap(mBmp);
                delayedTask = () -> {
                    refreshScreen(message);
                    ++times;
                    if (times < count) {
                        // Loop until all requested cards are drawn
                        mHandler.postDelayed(this, 300);
                    } // if (times < count)
                    else {
                        // All requested cards are drawn, do following things
                        mHandler.postDelayed(this::afterDrawn, 750);
                    } // else
                }; // delayedTask = () -> {}
                mHandler.postDelayed(delayedTask, 300);
            } // if (mDrawnCard != null)
            else {
                message = NAME[who] + " cannot hold more than "
                        + Uno.MAX_HOLD_CARDS + " cards";
                refreshScreen(message);
                mHandler.postDelayed(this::afterDrawn, 750);
            } // else
        } // run()

        /**
         * Triggered when all requested cards are drawn.
         */
        private void afterDrawn() {
            if (count == 1 &&
                    mDrawnCard != null &&
                    mUno.isLegalToPlay(mDrawnCard)) {
                // Player drew one card by itself, the drawn card
                // can be played immediately if it's legal to play
                mStatus = who;
                mImmPlayAsk = true;
                onStatusChanged(mStatus);
            } // if (count == 1 && ...)
            else {
                pass(who);
            } // else
        } // afterDrawn()
    } // DrawLoop Inner Class
} // MainActivity Class

// E.O.F