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

import com.github.hikari_toyama.unocard.core.Card;
import com.github.hikari_toyama.unocard.core.Color;
import com.github.hikari_toyama.unocard.core.Content;
import com.github.hikari_toyama.unocard.core.Uno;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Locale;

/**
 * Game UI.
 */
@SuppressWarnings("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity
        implements View.OnTouchListener {
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
    private ImageView mImgScreen;
    private Handler mHandler;
    private int mDifficulty;
    private int mWildIndex;
    private int mHardTotal;
    private int mEasyTotal;
    private int mHardWin;
    private int mEasyWin;
    private int mStatus;
    private int mWinner;
    private Bitmap mBmp;
    private Mat mScr;
    private Uno mUno;

    /**
     * Activity initialization.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp;

        // Preparations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("UnoStat", Context.MODE_PRIVATE);
        mEasyWin = sp.getInt("easyWin", 0);
        mHardWin = sp.getInt("hardWin", 0);
        mEasyTotal = sp.getInt("easyTotal", 0);
        mHardTotal = sp.getInt("hardTotal", 0);
        mImgScreen = findViewById(R.id.imgMainScreen);
        mHandler = new Handler();
        mUno = Uno.getInstance(this);
        mWinner = Uno.PLAYER_YOU;
        mStatus = STAT_WELCOME;
        mScr = mUno.getBackground().clone();
        mBmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
        refreshScreen("WELCOME TO UNO CARD GAME");
        mImgScreen.setOnTouchListener(this);
    } // onCreate()

    /**
     * AI Strategies (Difficulty: EASY).
     */
    private void easyAI() {
        Card card, previous;
        List<Card> hand, recent;
        Color bestColor, prevColor;
        int i, next, oppo, prev, direction;
        boolean hasNum, hasWild, hasWildDraw4;
        boolean hasRev, hasSkip, hasDraw2, hasZero;
        int yourSize, nextSize, oppoSize, prevSize;
        int idx_best, idx_rev, idx_skip, idx_draw2;
        int idx_zero, idx_num, idx_wild, idx_wildDraw4;

        hand = mUno.getHandCardsOf(mStatus);
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            if (mUno.isLegalToPlay(card)) {
                play(0, card.getColor());
            } // if (mUno.isLegalToPlay(card))
            else {
                draw(mStatus, 1);
            } // else

            return;
        } // if (yourSize == 1)

        direction = mUno.getDirection();
        next = (mStatus + direction) % 4;
        nextSize = mUno.getHandCardsOf(next).size();
        oppo = (mStatus + 2) % 4;
        oppoSize = mUno.getHandCardsOf(oppo).size();
        prev = (4 + mStatus - direction) % 4;
        prevSize = mUno.getHandCardsOf(prev).size();
        idx_best = idx_rev = idx_skip = idx_draw2 = -1;
        idx_zero = idx_num = idx_wild = idx_wildDraw4 = -1;
        bestColor = mUno.bestColorFor(mStatus);
        recent = mUno.getRecent();
        previous = recent.get(recent.size() - 1);
        if (previous.isWild()) {
            prevColor = previous.getWildColor();
        } // if (previous.isWild())
        else {
            prevColor = previous.getColor();
        } // else

        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (mUno.isLegalToPlay(card)) {
                switch (card.getContent()) {
                    case NUM0:
                        if (idx_zero < 0 || card.getColor() == bestColor) {
                            idx_zero = i;
                        } // if (idx_zero < 0 || card.getColor() == bestColor)
                        break; // case NUM0

                    case DRAW2:
                        if (idx_draw2 < 0 || card.getColor() == bestColor) {
                            idx_draw2 = i;
                        } // if (idx_draw2 < 0 || card.getColor() == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (idx_skip < 0 || card.getColor() == bestColor) {
                            idx_skip = i;
                        } // if (idx_skip < 0 || card.getColor() == bestColor)
                        break; // case SKIP

                    case REV:
                        if (idx_rev < 0 || card.getColor() == bestColor) {
                            idx_rev = i;
                        } // if (idx_rev < 0 || card.getColor() == bestColor)
                        break; // case REV

                    case WILD:
                        idx_wild = i;
                        break; // case WILD

                    case WILD_DRAW4:
                        idx_wildDraw4 = i;
                        break; // case WILD_DRAW4

                    default: // non-zero number cards
                        if (idx_num < 0 || card.getColor() == bestColor) {
                            idx_num = i;
                        } // if (idx_num < 0 || card.getColor() == bestColor)
                        break; // default
                } // switch (card.getContent())
            } // if (mUno.isLegalToPlay(card))
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        hasNum = (idx_num >= 0);
        hasRev = (idx_rev >= 0);
        hasZero = (idx_zero >= 0);
        hasSkip = (idx_skip >= 0);
        hasWild = (idx_wild >= 0);
        hasDraw2 = (idx_draw2 >= 0);
        hasWildDraw4 = (idx_wildDraw4 >= 0);
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2) {
                // Play a [+2] to make your next player draw two cards!
                idx_best = idx_draw2;
            } // if (hasDraw2)
            else if (hasWildDraw4) {
                // Play a [wild +4] to make your next player draw four cards,
                // even if the legal color is already your best color!
                idx_best = idx_wildDraw4;
            } // else if (hasWildDraw4)
            else if (hasSkip) {
                // Play a [skip] to skip its turn and wait for more chances.
                idx_best = idx_skip;
            } // else if (hasSkip)
            else if (hasRev) {
                // Play a [reverse] to get help from your opposite player.
                idx_best = idx_rev;
            } // else if (hasRev)
            else if (hasWild && prevColor != bestColor) {
                // Play a [wild] and change the legal color to your best to
                // decrease its possibility of playing the final card legally.
                idx_best = idx_wild;
            } // else if (hasWild && prevColor != bestColor)
            else if (hasZero) {
                // No more powerful choices. Play a number card.
                idx_best = idx_zero;
            } // else if (hasZero)
            else if (hasNum) {
                idx_best = idx_num;
            } // else if (hasNum)
        } // if (nextSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Give more freedom to your next player, the only one that can
            // directly limit your opposite player's action.
            if (hasRev && nextSize <= 3 && prevSize >= 6) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards,
                // because your previous player has more possibility to limit
                // your opposite player's action.
                idx_best = idx_rev;
            } // if (hasRev && nextSize <= 3 && prevSize >= 6)
            else if (hasNum) {
                // Then you can play a number card. In order to increase your
                // next player's possibility of changing the legal color, do
                // not play zero cards preferentially this time.
                idx_best = idx_num;
            } // else if (hasNum)
            else if (hasZero) {
                idx_best = idx_zero;
            } // else if (hasZero)
            else if (hasWild && prevColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Do not play any [+2]/[reverse] to your next player!
                idx_best = idx_wild;
            } // else if (hasWild && prevColor != bestColor)
            else if (hasWildDraw4 && prevColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Specially, for [wild +4] cards, you can only play it
                // when your next player remains only a few cards, and what you
                // did can help your next player find more useful cards, such as
                // action cards, or [wild +4] cards.
                if (nextSize <= 4) {
                    idx_best = idx_wildDraw4;
                } // if (nextSize <= 4)
            } // else if (hasWildDraw4 && prevColor != bestColor)
        } // else if (oppoSize == 1)
        else {
            // Normal strategies
            if (hasZero) {
                // Play zero cards at first because of their rarity.
                idx_best = idx_zero;
            } // if (hasZero)
            else if (hasNum) {
                // Then consider to play a number card.
                idx_best = idx_num;
            } // else if (hasNum)
            else if (hasRev && prevSize >= 3) {
                // Then consider to play an action card.
                idx_best = idx_rev;
            } // else if (hasRev && prevSize >= 3)
            else if (hasSkip) {
                idx_best = idx_skip;
            } // else if (hasSkip)
            else if (hasDraw2) {
                idx_best = idx_draw2;
            } // else if (hasDraw2)
            else if (hasWild) {
                // Then consider to play a wild card.
                idx_best = idx_wild;
            } // else if (hasWild)
            else if (hasWildDraw4) {
                idx_best = idx_wildDraw4;
            } // else if (hasWildDraw4)
        } // else

        if (idx_best >= 0) {
            // Found an appropriate card to play
            play(idx_best, bestColor);
        } // if (idx_best >= 0)
        else {
            // No appropriate cards to play, or no card is legal to play
            draw(mStatus, 1);
        } // else
    } // easyAI()

    /**
     * AI Strategies (Difficulty: HARD).
     */
    private void hardAI() {
        Card card, previous;
        List<Card> hand, recent;
        Color bestColor, prevColor;
        int i, next, oppo, prev, direction;
        boolean hasNum, hasWild, hasWildDraw4;
        boolean hasRev, hasSkip, hasDraw2, hasZero;
        int yourSize, nextSize, oppoSize, prevSize;
        int idx_best, idx_rev, idx_skip, idx_draw2;
        int idx_zero, idx_num, idx_wild, idx_wildDraw4;

        hand = mUno.getHandCardsOf(mStatus);
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            if (mUno.isLegalToPlay(card)) {
                play(0, card.getColor());
            } // if (mUno.isLegalToPlay(card))
            else {
                draw(mStatus, 1);
            } // else

            return;
        } // if (yourSize == 1)

        direction = mUno.getDirection();
        next = (mStatus + direction) % 4;
        nextSize = mUno.getHandCardsOf(next).size();
        oppo = (mStatus + 2) % 4;
        oppoSize = mUno.getHandCardsOf(oppo).size();
        prev = (4 + mStatus - direction) % 4;
        prevSize = mUno.getHandCardsOf(prev).size();
        idx_best = idx_rev = idx_skip = idx_draw2 = -1;
        idx_zero = idx_num = idx_wild = idx_wildDraw4 = -1;
        bestColor = mUno.bestColorFor(mStatus);
        recent = mUno.getRecent();
        previous = recent.get(recent.size() - 1);
        if (previous.isWild()) {
            prevColor = previous.getWildColor();
        } // if (previous.isWild())
        else {
            prevColor = previous.getColor();
        } // else

        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (mUno.isLegalToPlay(card)) {
                switch (card.getContent()) {
                    case NUM0:
                        if (idx_zero < 0 || card.getColor() == bestColor) {
                            idx_zero = i;
                        } // if (idx_zero < 0 || card.getColor() == bestColor)
                        break; // case NUM0

                    case DRAW2:
                        if (idx_draw2 < 0 || card.getColor() == bestColor) {
                            idx_draw2 = i;
                        } // if (idx_draw2 < 0 || card.getColor() == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (idx_skip < 0 || card.getColor() == bestColor) {
                            idx_skip = i;
                        } // if (idx_skip < 0 || card.getColor() == bestColor)
                        break; // case SKIP

                    case REV:
                        if (idx_rev < 0 || card.getColor() == bestColor) {
                            idx_rev = i;
                        } // if (idx_rev < 0 || card.getColor() == bestColor)
                        break; // case REV

                    case WILD:
                        idx_wild = i;
                        break; // case WILD

                    case WILD_DRAW4:
                        idx_wildDraw4 = i;
                        break; // case WILD_DRAW4

                    default: // non-zero number cards
                        if (idx_num < 0 || card.getColor() == bestColor) {
                            idx_num = i;
                        } // if (idx_num < 0 || card.getColor() == bestColor)
                        break; // default
                } // switch (card.getContent())
            } // if (mUno.isLegalToPlay(card))
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        hasNum = (idx_num >= 0);
        hasRev = (idx_rev >= 0);
        hasZero = (idx_zero >= 0);
        hasSkip = (idx_skip >= 0);
        hasWild = (idx_wild >= 0);
        hasDraw2 = (idx_draw2 >= 0);
        hasWildDraw4 = (idx_wildDraw4 >= 0);
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2) {
                // Play a [+2] to make your next player draw two cards!
                idx_best = idx_draw2;
            } // if (hasDraw2)
            else if (hasWildDraw4) {
                // Play a [wild +4] to make your next player draw four cards,
                // even if the legal color is already your best color!
                idx_best = idx_wildDraw4;
            } // else if (hasWildDraw4)
            else if (hasSkip) {
                // Play a [skip] to skip its turn and wait for more chances.
                idx_best = idx_skip;
            } // else if (hasSkip)
            else if (hasRev) {
                // Play a [reverse] to get help from your opposite player.
                idx_best = idx_rev;
            } // else if (hasRev)
            else if (hasWild && prevColor != bestColor) {
                // Play a [wild] and change the legal color to your best to
                // decrease its possibility of playing the final card legally.
                idx_best = idx_wild;
            } // else if (hasWild && prevColor != bestColor)
            else if (hasZero) {
                // No more powerful choices. Play a number card.
                idx_best = idx_zero;
            } // else if (hasZero)
            else if (hasNum) {
                idx_best = idx_num;
            } // else if (hasNum)
        } // if (nextSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Give more freedom to your next player, the only one that can
            // directly limit your opposite player's action.
            if (hasRev && nextSize <= 3 && prevSize >= 6) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards,
                // because your previous player has more possibility to limit
                // your opposite player's action.
                idx_best = idx_rev;
            } // if (hasRev && nextSize <= 3 && prevSize >= 6)
            else if (hasNum) {
                // Then you can play a number card. In order to increase your
                // next player's possibility of changing the legal color, do
                // not play zero cards priorly this time.
                idx_best = idx_num;
            } // else if (hasNum)
            else if (hasZero) {
                idx_best = idx_zero;
            } // else if (hasZero)
            else if (hasWild && prevColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Do not play any [+2]/[reverse] to your next player!
                idx_best = idx_wild;
            } // else if (hasWild && prevColor != bestColor)
            else if (hasWildDraw4 && prevColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Specially, for [wild +4] cards, you can only play it
                // when your next player remains only a few cards, and what you
                // did can help your next player find more useful cards, such as
                // action cards, or [wild +4] cards.
                if (nextSize <= 4) {
                    idx_best = idx_wildDraw4;
                } // if (nextSize <= 4)
            } // else if (hasWildDraw4 && prevColor != bestColor)
        } // else if (oppoSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Save your action cards s much as you can. once a reverse card is
            // played, you can use these cards to limit your previous player's
            // action.
            if (hasNum) {
                idx_best = idx_num;
            } // if (hasNum)
            else if (hasZero) {
                idx_best = idx_zero;
            } // else if (hasZero)
            else if (hasWild && prevColor != bestColor) {
                idx_best = idx_wild;
            } // else if (hasWild && prevColor != bestColor)
            else if (hasWildDraw4 && prevColor != bestColor) {
                idx_best = idx_wildDraw4;
            } // else if (hasWildDraw4 && prevColor != bestColor)
        } // else if (prevSize == 1)
        else {
            // Normal strategies
            if (hasRev && nextSize <= 3 && prevSize >= 6) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards, in
                // order to balance everyone's hand-card amount.
                idx_best = idx_rev;
            } // if (hasRev && nextSize <= 3 && prevSize >= 6)
            else if (hasDraw2 && nextSize <= 4) {
                // Play a [+2] when your next player remains only a few cards.
                idx_best = idx_draw2;
            } // else if (hasDraw2 && nextSize <= 4)
            else if (hasSkip && nextSize <= 4) {
                // Play a [skip] when your next player remains only a few cards.
                idx_best = idx_skip;
            } // else if (hasSkip && nextSize <= 4)
            else if (hasZero) {
                // Play zero cards at first because of their rarity.
                idx_best = idx_zero;
            } // else if (hasZero)
            else if (hasNum) {
                // Then consider to play a number card.
                idx_best = idx_num;
            } // else if (hasNum)
            else if (hasRev && prevSize >= 4) {
                // When you have no more legal number cards to play, you can
                // play a [reverse] safely when your previous player still has
                // a number of cards.
                idx_best = idx_rev;
            } // else if (hasRev && prevSize >= 4)
            else if (hasWild && nextSize <= 4) {
                // When your next player remains only a few cards, and you have
                // no more legal number/action cards to play, try to play a
                // wild card and change the legal color to your best color.
                idx_best = idx_wild;
            } // else if (hasWild && nextSize <= 4)
            else if (hasWildDraw4 && nextSize <= 4) {
                // When your next player remains only a few cards, and you have
                // no more legal number/action cards to play, try to play a
                // wild card and change the legal color to your best color.
                idx_best = idx_wildDraw4;
            } // else if (hasWildDraw4 && nextSize <= 4)
            else if (hasWild && yourSize == 2 && prevSize <= 3) {
                // When you remain only 2 cards, including a wild card, and your
                // previous player seems no enough power to limit you (has too
                // few cards), start your UNO dash!
                if (hasDraw2) {
                    // When you still have a [+2] card, play it, even if it's
                    // not worth to let your next player draw cards.
                    idx_best = idx_draw2;
                } // if (hasDraw2)
                else if (hasSkip) {
                    // When you still have a [skip] card, play it, even if it's
                    // not worth to let your next player skip its turn.
                    idx_best = idx_skip;
                } // else if (hasSkip)
                else {
                    // When you have no more legal draw2/skip cards, play your
                    // wild card to start your UNO dash.
                    idx_best = idx_wild;
                } // else
            } // else if (hasWild && yourSize == 2 && prevSize <= 3)
            else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3) {
                if (hasDraw2) {
                    idx_best = idx_draw2;
                } // if (hasDraw2)
                else if (hasSkip) {
                    idx_best = idx_skip;
                } // else if (hasSkip)
                else {
                    idx_best = idx_wildDraw4;
                } // else
            } // else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3)
        } // else

        if (idx_best >= 0) {
            // Found an appropriate card to play
            play(idx_best, bestColor);
        } // if (idx_best >= 0)
        else {
            // No appropriate cards to play, or no card is legal to play
            draw(mStatus, 1);
        } // else
    } // hardAI()

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

            case Uno.PLAYER_YOU:
                // Your turn, select a hand card to play, or draw a card
                refreshScreen("Your turn, play or draw a card");
                break; // case Uno.PLAYER_YOU

            case STAT_WILD_COLOR:
                // Need to specify the following legal color after played a
                // wild card. Draw color sectors in the center of screen
                refreshScreen("^ Specify the following legal color");
                rect = new Rect(420, 270, 121, 181);
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

            case Uno.PLAYER_COM1:
            case Uno.PLAYER_COM2:
            case Uno.PLAYER_COM3:
                // AI players' turn
                if (mDifficulty == LV_EASY) {
                    easyAI();
                } // if (mDifficulty == LV_EASY)
                else {
                    hardAI();
                } // else
                break; // case Uno.PLAYER_COM1, Uno.PLAYER_COM2, Uno.PLAYER_COM3

            case STAT_GAME_OVER:
                // Game over
                if (mWinner == Uno.PLAYER_YOU) {
                    if (mDifficulty == LV_EASY) {
                        ++mEasyWin;
                    } // if (mDifficulty == LV_EASY)
                    else {
                        ++mHardWin;
                    } // else
                } // if (mWinner == Uno.PLAYER_YOU)

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
        int status, size, width, height, remain, used, easyRate, hardRate;

        // Lock the value of member [mStatus]
        status = mStatus;

        // Clear
        mUno.getBackground().copyTo(mScr);

        // Message area
        textSize = Imgproc.getTextSize(message, FONT_SANS, 1.0, 1, null);
        point = new Point(640 - textSize.width / 2, 480);
        Imgproc.putText(mScr, message, point, FONT_SANS, 1.0, RGB_WHITE);

        // For welcome screen, only show difficulty buttons and winning rates
        if (status == STAT_WELCOME) {
            image = mUno.getEasyImage();
            roi = new Rect(420, 270, 121, 181);
            image.copyTo(new Mat(mScr, roi), image);
            image = mUno.getHardImage();
            roi.x = 740;
            roi.y = 270;
            image.copyTo(new Mat(mScr, roi), image);
            easyRate = (mEasyTotal == 0 ? 0 : 100 * mEasyWin / mEasyTotal);
            hardRate = (mHardTotal == 0 ? 0 : 100 * mHardWin / mHardTotal);
            info = easyRate + "% [WinningRate] " + hardRate + "%";
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
        roi = new Rect(420, 270, 121, 181);
        image.copyTo(new Mat(mScr, roi), image);
        hand = mUno.getRecent();
        size = hand.size();
        width = 45 * size + 75;
        roi.x = 710 - width / 2;
        roi.y = 270;
        for (Card recent : hand) {
            if (recent.getContent() == Content.WILD) {
                image = mUno.getColoredWildImage(recent.getWildColor());
            } // if (recent.getContent() == Content.WILD)
            else if (recent.getContent() == Content.WILD_DRAW4) {
                image = mUno.getColoredWildDraw4Image(recent.getWildColor());
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
        hand = mUno.getHandCardsOf(Uno.PLAYER_COM1);
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            point.x = 51;
            point.y = 461;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            if (status == STAT_GAME_OVER) {
                // Show remained cards to everyone when game over
                height = 40 * size + 140;
                roi.x = 20;
                roi.y = 360 - height / 2;
                for (Card card : hand) {
                    image = card.getImage();
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (Card card : hand)
            } // if (status == STAT_GAME_OVER)
            else {
                // Only show a card back in game process
                image = mUno.getBackImage();
                roi.x = 20;
                roi.y = 270;
                image.copyTo(new Mat(mScr, roi), image);
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 47;
                point.y = 494;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
            else if (status != STAT_GAME_OVER) {
                // When two or more cards in hand, show the amount
                point.x = 50;
                point.y = 494;
                info = String.format(Locale.getDefault(), "%2d", size);
                Imgproc.putText(mScr, info, point, FONT_SANS, 1.0, RGB_WHITE);
            } // else if (status != STAT_GAME_OVER)
        } // else

        // Top-center: Hand cards of Player North (COM2)
        hand = mUno.getHandCardsOf(Uno.PLAYER_COM2);
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            point.x = 611;
            point.y = 121;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            if (status == STAT_GAME_OVER) {
                // Show remained hand cards when game over
                width = 45 * size + 75;
                roi.x = 640 - width / 2;
                roi.y = 20;
                for (Card card : hand) {
                    image = card.getImage();
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.x += 45;
                } // for (Card card : hand)
            } // if (status == STAT_GAME_OVER)
            else {
                // Only show a card back in game process
                image = mUno.getBackImage();
                roi.x = 580;
                roi.y = 20;
                image.copyTo(new Mat(mScr, roi), image);
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 500;
                point.y = 121;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
            else if (status != STAT_GAME_OVER) {
                // When two or more cards in hand, show the amount
                point.x = 500;
                point.y = 121;
                info = String.format(Locale.getDefault(), "%2d", size);
                Imgproc.putText(mScr, info, point, FONT_SANS, 1.0, RGB_WHITE);
            } // else if (status != STAT_GAME_OVER)
        } // else

        // Right-center: Hand cards of Player East (COM3)
        hand = mUno.getHandCardsOf(Uno.PLAYER_COM3);
        size = hand.size();
        if (size == 0) {
            // Played all hand cards, it's winner
            point.x = 1170;
            point.y = 461;
            Imgproc.putText(mScr, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
        } // if (size == 0)
        else {
            if (status == STAT_GAME_OVER) {
                // Show remained hand cards when game over
                height = 40 * size + 140;
                roi.x = 1140;
                roi.y = 360 - height / 2;
                for (Card card : hand) {
                    image = card.getImage();
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (Card card : hand)
            } // if (status == STAT_GAME_OVER)
            else {
                // Only show a card back in game process
                image = mUno.getBackImage();
                roi.x = 1140;
                roi.y = 270;
                image.copyTo(new Mat(mScr, roi), image);
            } // else

            if (size == 1) {
                // Show "UNO" warning when only one card in hand
                point.x = 1166;
                point.y = 494;
                Imgproc.putText(mScr, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
            } // if (size == 1)
            else if (status != STAT_GAME_OVER) {
                // When two or more cards in hand, show the amount
                point.x = 1170;
                point.y = 494;
                info = String.format(Locale.getDefault(), "%2d", size);
                Imgproc.putText(mScr, info, point, FONT_SANS, 1.0, RGB_WHITE);
            } // else if (status != STAT_GAME_OVER)
        } // else

        // Bottom: Your hand cards
        hand = mUno.getHandCardsOf(Uno.PLAYER_YOU);
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
                if (status == Uno.PLAYER_YOU && mUno.isLegalToPlay(card)) {
                    image = card.getImage();
                } // if (status == Uno.PLAYER_YOU && mUno.isLegalToPlay(card))
                else if (status == STAT_GAME_OVER) {
                    image = card.getImage();
                } // else if (status == STAT_GAME_OVER)
                else {
                    image = card.getDarkImg();
                } // else

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
        int now, waitMs;
        Runnable delayedTask;

        now = mStatus;
        mStatus = STAT_IDLE; // block tap down events when idle
        card = mUno.play(now, index, color);
        if (card != null) {
            // Animation
            switch (now) {
                case Uno.PLAYER_COM1:
                    image = card.getImage();
                    roi = new Rect(160, 270, 121, 181);
                    image.copyTo(new Mat(mScr, roi), image);
                    Utils.matToBitmap(mScr, mBmp);
                    mImgScreen.setImageBitmap(mBmp);
                    waitMs = 300;
                    break; // case Uno.PLAYER_COM1

                case Uno.PLAYER_COM2:
                    image = card.getImage();
                    roi = new Rect(720, 20, 121, 181);
                    image.copyTo(new Mat(mScr, roi), image);
                    Utils.matToBitmap(mScr, mBmp);
                    mImgScreen.setImageBitmap(mBmp);
                    waitMs = 300;
                    break; // case Uno.PLAYER_COM2

                case Uno.PLAYER_COM3:
                    image = card.getImage();
                    roi = new Rect(1000, 270, 121, 181);
                    image.copyTo(new Mat(mScr, roi), image);
                    Utils.matToBitmap(mScr, mBmp);
                    mImgScreen.setImageBitmap(mBmp);
                    waitMs = 300;
                    break; // case Uno.PLAYER_COM3

                default:
                    waitMs = 0;
                    break; // default
            } // switch (now)

            delayedTask = () -> {
                String message;
                int next, direction;

                if (mUno.getHandCardsOf(now).size() == 0) {
                    // The player in action becomes winner when it played the
                    // final card in its hand successfully
                    mWinner = now;
                    mStatus = STAT_GAME_OVER;
                    onStatusChanged(mStatus);
                } // if (mUno.getHandCardsOf(now).size() == 0)
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
                            if (next == Uno.PLAYER_YOU) {
                                message = NAME[now] + ": Skip your turn";
                            } // if (next == Uno.PLAYER_YOU)
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
                            mHandler.postDelayed(() -> draw(next, 4), 1500);
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
            mHandler.postDelayed(delayedTask, waitMs);
        } // if (card != null)
    } // play()

    /**
     * Let a player draw one or more cards, and skip its turn.
     *
     * @param who   Who draws cards. Must be one of the following values:
     *              PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
     * @param count How many cards to draw.
     * @see DrawLoop
     */
    private void draw(int who, int count) {
        mStatus = STAT_IDLE; // block click events when idle

        // This is a time-consuming procedure, so we cannot use the traditional
        // for-loop. We need to create a Runnable object, and make a delayed
        // cycle by executing mHandler.postDelayed(this, delayedMs); in the
        // run() method of the created Runnable object.
        new DrawLoop(who, count).run();
    } // draw()

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
        int x, y, index, size, width, startX;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Only response to tap down events, and ignore the others
            x = (int) (event.getX() * mUno.getBgWidth() / v.getWidth());
            y = (int) (event.getY() * mUno.getBgHeight() / v.getHeight());
            switch (mStatus) {
                case STAT_WELCOME:
                    if (y >= 270 && y <= 450) {
                        if (x >= 420 && x <= 540) {
                            // Difficulty: EASY
                            mDifficulty = LV_EASY;
                            mStatus = STAT_NEW_GAME;
                            onStatusChanged(mStatus);
                        } // if (x >= 420 && x <= 540)
                        else if (x >= 740 && x <= 860) {
                            // Difficulty: HARD
                            mDifficulty = LV_HARD;
                            mStatus = STAT_NEW_GAME;
                            onStatusChanged(mStatus);
                        } // else if (x >= 740 && x <= 860)
                    } // if (y >= 270 && y <= 450)
                    break; // case STAT_WELCOME

                case Uno.PLAYER_YOU:
                    if (y >= 520 && y <= 700) {
                        hand = mUno.getHandCardsOf(Uno.PLAYER_YOU);
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
                                play(index, card.getColor());
                            } // else if (mUno.isLegalToPlay(card))
                        } // if (x >= startX && x <= startX + width)
                    } // if (y >= 520 && y <= 700)
                    else if (y >= 270 && y <= 450 && x >= 420 && x <= 540) {
                        // Card deck area, draw a card
                        draw(Uno.PLAYER_YOU, 1);
                    } // else if (y >= 270 && y <= 450 && x >= 420 && x <= 540)
                    break; // case Uno.PLAYER_YOU

                case STAT_WILD_COLOR:
                    if (y > 220 && y < 315) {
                        if (x > 310 && x < 405) {
                            // Red sector
                            mStatus = Uno.PLAYER_YOU;
                            play(mWildIndex, Color.RED);
                        } // if (x > 310 && x < 405)
                        else if (x > 405 && x < 500) {
                            // Blue sector
                            mStatus = Uno.PLAYER_YOU;
                            play(mWildIndex, Color.BLUE);
                        } // else if (x > 405 && x < 500)
                    } // if (y > 220 && y < 315)
                    else if (y > 315 && y < 410) {
                        if (x > 310 && x < 405) {
                            // Yellow sector
                            mStatus = Uno.PLAYER_YOU;
                            play(mWildIndex, Color.YELLOW);
                        } // if (x > 310 && x < 405)
                        else if (x > 405 && x < 500) {
                            // Green sector
                            mStatus = Uno.PLAYER_YOU;
                            play(mWildIndex, Color.GREEN);
                        } // else if (x > 405 && x < 500)
                    } // else if (y > 315 && y < 410)
                    break; // case STAT_WILD_COLOR

                case STAT_GAME_OVER:
                    if (y >= 270 && y <= 450 && x >= 420 && x <= 540) {
                        // Card deck area, start a new game
                        mStatus = STAT_NEW_GAME;
                        onStatusChanged(mStatus);
                    } // if (y >= 270 && y <= 450 && x >= 420 && x <= 540)
                    break; // case STAT_GAME_OVER

                default:
                    break; // default
            } // switch (mStatus)
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
            dialog.dismiss();
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
         *              PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
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
            Card card;
            String message;
            Runnable delayedTask;

            card = mUno.draw(who);
            if (card != null) {
                // Animation
                switch (who) {
                    case Uno.PLAYER_COM1:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(160, 270, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mImgScreen.setImageBitmap(mBmp);
                        break; // case Uno.PLAYER_COM1

                    case Uno.PLAYER_COM2:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(720, 20, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mImgScreen.setImageBitmap(mBmp);
                        break; // case Uno.PLAYER_COM2

                    case Uno.PLAYER_COM3:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(1000, 270, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mImgScreen.setImageBitmap(mBmp);
                        break; // case Uno.PLAYER_COM3

                    default:
                        message = NAME[who] + ": Draw " + card.getName();
                        image = card.getImage();
                        roi = new Rect(140, 520, 121, 181);
                        image.copyTo(new Mat(mScr, roi), image);
                        Utils.matToBitmap(mScr, mBmp);
                        mImgScreen.setImageBitmap(mBmp);
                        break; // default
                } // switch (who)

                delayedTask = () -> {
                    refreshScreen(message);
                    ++times;
                    if (times < count) {
                        // Loop until all requested cards are drawn
                        mHandler.postDelayed(this, 300);
                    } // if (times < count)
                    else {
                        // All requested cards are drawn, do following things
                        mHandler.postDelayed(() -> {
                            mStatus = (who + mUno.getDirection()) % 4;
                            onStatusChanged(mStatus);
                        }, 1500); // mHandler.postDelayed()
                    } // else
                }; // delayedTask = () -> {}
                mHandler.postDelayed(delayedTask, 300);
            } // if (card != null)
            else {
                message = NAME[who] + " cannot hold more than "
                        + Uno.MAX_HOLD_CARDS + " cards";
                refreshScreen(message);
                mHandler.postDelayed(() -> {
                    mStatus = (who + mUno.getDirection()) % 4;
                    onStatusChanged(mStatus);
                }, 1500); // mHandler.postDelayed()
            } // else
        } // run()
    } // DrawLoop Inner Class
} // MainActivity Class

// E.O.F