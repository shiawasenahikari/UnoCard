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
import java.util.Random;

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
    private Random mRnd;
    private Bitmap mBmp;
    private Mat mScr;
    private Uno mUno;

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
            mUno = Uno.getInstance(this);
            mWinner = Player.YOU;
            mStatus = STAT_WELCOME;
            mRnd = new Random();
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
        boolean legal;
        Card card, last;
        int i, direction;
        List<Card> hand, recent;
        Color bestColor, lastColor;
        Player curr, next, oppo, prev;
        int idxBest, idxRev, idxSkip, idxDraw2;
        boolean hasZero, hasWild, hasWildDraw4;
        boolean hasNum, hasRev, hasSkip, hasDraw2;
        int idxZero, idxNum, idxWild, idxWildDraw4;
        int yourSize, nextSize, oppoSize, prevSize;

        if (mChallengeAsk) {
            challengeAI();
            return;
        } // if (mChallengeAsk)

        curr = mUno.getPlayer(mStatus);
        hand = curr.getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            if (mUno.isLegalToPlay(card)) {
                play(0, card.getRealColor());
            } // if (mUno.isLegalToPlay(card))
            else {
                draw(mStatus, 1);
            } // else

            return;
        } // if (yourSize == 1)

        direction = mUno.getDirection();
        next = mUno.getPlayer((mStatus + direction) % 4);
        nextSize = next.getHandCards().size();
        oppo = mUno.getPlayer((mStatus + 2) % 4);
        oppoSize = oppo.getHandCards().size();
        prev = mUno.getPlayer((4 + mStatus - direction) % 4);
        prevSize = prev.getHandCards().size();
        idxBest = idxRev = idxSkip = idxDraw2 = -1;
        idxZero = idxNum = idxWild = idxWildDraw4 = -1;
        bestColor = curr.calcBestColor();
        recent = mUno.getRecent();
        last = recent.get(recent.size() - 1);
        lastColor = last.getRealColor();
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (mImmPlayAsk) {
                legal = card == mDrawnCard;
            } // if (mImmPlayAsk)
            else {
                legal = mUno.isLegalToPlay(card);
            } // else

            if (legal) {
                switch (card.getContent()) {
                    case NUM0:
                        if (idxZero < 0 || card.getRealColor() == bestColor) {
                            idxZero = i;
                        } // if (idxZero < 0 || ...)
                        break; // case NUM0

                    case DRAW2:
                        if (idxDraw2 < 0 || card.getRealColor() == bestColor) {
                            idxDraw2 = i;
                        } // if (idxDraw2 < 0 || ...)
                        break; // case DRAW2

                    case SKIP:
                        if (idxSkip < 0 || card.getRealColor() == bestColor) {
                            idxSkip = i;
                        } // if (idxSkip < 0 || ...)
                        break; // case SKIP

                    case REV:
                        if (idxRev < 0 || card.getRealColor() == bestColor) {
                            idxRev = i;
                        } // if (idxRev < 0 || ...)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWildDraw4 = i;
                        break; // case WILD_DRAW4

                    default: // non-zero number cards
                        if (idxNum < 0 || card.getRealColor() == bestColor) {
                            idxNum = i;
                        } // if (idxNum < 0 || ...)
                        break; // default
                } // switch (card.getContent())
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        hasNum = (idxNum >= 0);
        hasRev = (idxRev >= 0);
        hasZero = (idxZero >= 0);
        hasSkip = (idxSkip >= 0);
        hasWild = (idxWild >= 0);
        hasDraw2 = (idxDraw2 >= 0);
        hasWildDraw4 = (idxWildDraw4 >= 0);
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2) {
                // Play a [+2] to make your next player draw two cards!
                idxBest = idxDraw2;
            } // if (hasDraw2)
            else if (hasWildDraw4) {
                // Play a [wild +4] to make your next player draw four cards,
                // even if the legal color is already your best color!
                idxBest = idxWildDraw4;
            } // else if (hasWildDraw4)
            else if (hasSkip) {
                // Play a [skip] to skip its turn and wait for more chances.
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasRev) {
                // Play a [reverse] to get help from your opposite player.
                idxBest = idxRev;
            } // else if (hasRev)
            else if (hasWild && lastColor != bestColor) {
                // Play a [wild] and change the legal color to your best to
                // decrease its possibility of playing the final card legally.
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasZero) {
                // No more powerful choices. Play a number card.
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
        } // if (nextSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Give more freedom to your next player, the only one that can
            // directly limit your opposite player's action.
            if (hasRev && prevSize - nextSize >= 3) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards,
                // because your previous player has more possibility to limit
                // your opposite player's action.
                idxBest = idxRev;
            } // if (hasRev && prevSize - nextSize >= 3)
            else if (hasNum) {
                // Then you can play a number card. In order to increase your
                // next player's possibility of changing the legal color, do
                // not play zero cards preferentially this time.
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasZero) {
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasWild && lastColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Do not play any [+2]/[skip] to your next player!
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasWildDraw4 && lastColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Specially, for [wild +4] cards, you can only play it
                // when your next player remains only a few cards, and what you
                // did can help your next player find more useful cards, such as
                // action cards, or [wild +4] cards.
                if (nextSize <= 4) {
                    idxBest = idxWildDraw4;
                } // if (nextSize <= 4)
            } // else if (hasWildDraw4 && lastColor != bestColor)
        } // else if (oppoSize == 1)
        else {
            // Normal strategies
            if (hasZero) {
                // Play zero cards at first because of their rarity.
                idxBest = idxZero;
            } // if (hasZero)
            else if (hasNum) {
                // Then consider to play a number card.
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasRev && prevSize >= 3) {
                // Then consider to play an action card.
                idxBest = idxRev;
            } // else if (hasRev && prevSize >= 3)
            else if (hasSkip) {
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasDraw2) {
                idxBest = idxDraw2;
            } // else if (hasDraw2)
            else if (hasWild) {
                // Then consider to play a wild card.
                idxBest = idxWild;
            } // else if (hasWild)
            else if (hasWildDraw4) {
                idxBest = idxWildDraw4;
            } // else if (hasWildDraw4)
        } // else

        if (idxBest >= 0) {
            // Found an appropriate card to play
            mImmPlayAsk = false;
            play(idxBest, bestColor);
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
    } // easyAI()

    /**
     * AI Strategies (Difficulty: HARD).
     */
    private void hardAI() {
        boolean legal;
        Card card, last;
        Color bestColor;
        Color lastColor;
        int i, direction;
        Color dangerColor;
        List<Card> hand, recent;
        Player curr, next, oppo, prev;
        int idxBest, idxRev, idxSkip, idxDraw2;
        boolean hasZero, hasWild, hasWildDraw4;
        boolean hasNum, hasRev, hasSkip, hasDraw2;
        int idxZero, idxNum, idxWild, idxWildDraw4;
        int yourSize, nextSize, oppoSize, prevSize;

        if (mChallengeAsk) {
            challengeAI();
            return;
        } // if (mChallengeAsk)

        curr = mUno.getPlayer(mStatus);
        hand = curr.getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            if (mUno.isLegalToPlay(card)) {
                play(0, card.getRealColor());
            } // if (mUno.isLegalToPlay(card))
            else {
                draw(mStatus, 1);
            } // else

            return;
        } // if (yourSize == 1)

        direction = mUno.getDirection();
        next = mUno.getPlayer((mStatus + direction) % 4);
        nextSize = next.getHandCards().size();
        oppo = mUno.getPlayer((mStatus + 2) % 4);
        oppoSize = oppo.getHandCards().size();
        prev = mUno.getPlayer((4 + mStatus - direction) % 4);
        prevSize = prev.getHandCards().size();
        idxBest = idxRev = idxSkip = idxDraw2 = -1;
        idxZero = idxNum = idxWild = idxWildDraw4 = -1;
        bestColor = curr.calcBestColor();
        recent = mUno.getRecent();
        last = recent.get(recent.size() - 1);
        lastColor = last.getRealColor();
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (mImmPlayAsk) {
                legal = card == mDrawnCard;
            } // if (mImmPlayAsk)
            else {
                legal = mUno.isLegalToPlay(card);
            } // else

            if (legal) {
                switch (card.getContent()) {
                    case NUM0:
                        if (idxZero < 0 || card.getRealColor() == bestColor) {
                            idxZero = i;
                        } // if (idxZero < 0 || ...)
                        break; // case NUM0

                    case DRAW2:
                        if (idxDraw2 < 0 || card.getRealColor() == bestColor) {
                            idxDraw2 = i;
                        } // if (idxDraw2 < 0 || ...)
                        break; // case DRAW2

                    case SKIP:
                        if (idxSkip < 0 || card.getRealColor() == bestColor) {
                            idxSkip = i;
                        } // if (idxSkip < 0 || ...)
                        break; // case SKIP

                    case REV:
                        if (idxRev < 0 || card.getRealColor() == bestColor) {
                            idxRev = i;
                        } // if (idxRev < 0 || ...)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWildDraw4 = i;
                        break; // case WILD_DRAW4

                    default: // non-zero number cards
                        if (idxNum < 0 || card.getRealColor() == bestColor) {
                            idxNum = i;
                        } // if (idxNum < 0 || ...)
                        break; // default
                } // switch (card.getContent())
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        hasNum = (idxNum >= 0);
        hasRev = (idxRev >= 0);
        hasZero = (idxZero >= 0);
        hasSkip = (idxSkip >= 0);
        hasWild = (idxWild >= 0);
        hasDraw2 = (idxDraw2 >= 0);
        hasWildDraw4 = (idxWildDraw4 >= 0);
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            dangerColor = next.getDangerousColor();
            if (hasDraw2) {
                // Play a [+2] to make your next player draw two cards!
                idxBest = idxDraw2;
            } // if (hasDraw2)
            else if (lastColor == dangerColor) {
                // Your next player played a wild card, started an UNO dash in
                // its last action, and what's worse is that the legal color has
                // not been changed yet. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasZero &&
                        hand.get(idxZero).getRealColor() != dangerColor) {
                    // When you have no [+2] cards, you have to change the legal
                    // color, or use [wild +4] cards. At first, try to change
                    // legal color by playing a number card, instead of using
                    // wild cards.
                    idxBest = idxZero;
                } // if (hasZero && ...)
                else if (hasNum &&
                        hand.get(idxNum).getRealColor() != dangerColor) {
                    idxBest = idxNum;
                } // else if (hasNum && ...)
                else if (hasSkip) {
                    // Play a [skip] to skip its turn and wait for more chances.
                    idxBest = idxSkip;
                } // else if (hasSkip)
                else if (hasWildDraw4) {
                    // Now start to use wild cards. Use [wild +4] cards firstly,
                    // because this card makes your next player draw four cards.
                    while (bestColor == dangerColor ||
                            bestColor == oppo.getDangerousColor() ||
                            bestColor == prev.getDangerousColor()) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (bestColor == dangerColor || ...)

                    idxBest = idxWildDraw4;
                } // else if (hasWildDraw4)
                else if (hasWild) {
                    // If you only have [wild] cards, firstly consider to change
                    // to your best color, but when your next player's last card
                    // has the same color to your best color, you have to change
                    // to another color.
                    while (bestColor == dangerColor ||
                            bestColor == oppo.getDangerousColor() ||
                            bestColor == prev.getDangerousColor()) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (bestColor == dangerColor || ...)

                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasRev) {
                    // Finally play a [reverse] to get help from other players.
                    // If you even do not have this choice, you lose this game.
                    idxBest = idxRev;
                } // else if (hasRev)
            } // else if (lastColor == dangerColor)
            else if (dangerColor != Color.NONE) {
                // Your next player played a wild card, started an UNO dash in
                // its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the dangerous color again.
                if (hasZero &&
                        hand.get(idxZero).getRealColor() != dangerColor) {
                    idxBest = idxZero;
                } // if (hasZero && ...)
                else if (hasNum &&
                        hand.get(idxNum).getRealColor() != dangerColor) {
                    idxBest = idxNum;
                } // else if (hasNum && ...)
                else if (hasRev &&
                        prevSize >= 4 &&
                        hand.get(idxRev).getRealColor() != dangerColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != dangerColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
            } // else if (dangerColor != Color.NONE)
            else if (hasWildDraw4) {
                // Your next player started an UNO dash without playing a wild
                // card, so use normal defense strategies. Firstly play a
                // [wild +4] to make your next player draw four cards, even if
                // the legal color is already your best color!
                idxBest = idxWildDraw4;
            } // else if (hasWildDraw4)
            else if (hasSkip) {
                // Play a [skip] to skip its turn and wait for more chances.
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasRev) {
                // Play a [reverse] to get help from your opposite player.
                idxBest = idxRev;
            } // else if (hasRev)
            else if (hasWild && lastColor != bestColor) {
                // Play a [wild] and change the legal color to your best to
                // decrease its possibility of playing the final card legally.
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasZero) {
                // No more powerful choices. Play a number card.
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Save your action cards as much as you can. once a reverse card is
            // played, you can use these cards to limit your previous player's
            // action.
            dangerColor = prev.getDangerousColor();
            if (lastColor == dangerColor) {
                // Your previous player played a wild card, started an UNO dash
                // in its last action. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != dangerColor) {
                    // When your opposite player played a [skip], and you have a
                    // [skip] with different color, play it.
                    idxBest = idxSkip;
                } // if (hasSkip && ...)
                else if (hasWild) {
                    // Now start to use wild cards. Firstly consider to change
                    // to your best color, but when your next player's last card
                    // has the same color to your best color, you have to change
                    // to another color.
                    while (bestColor == dangerColor ||
                            bestColor == next.getDangerousColor() ||
                            bestColor == oppo.getDangerousColor()) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (bestColor == dangerColor || ...)

                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasWildDraw4) {
                    while (bestColor == dangerColor ||
                            bestColor == next.getDangerousColor() ||
                            bestColor == oppo.getDangerousColor()) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (bestColor == dangerColor || ...)

                    idxBest = idxWild;
                } // else if (hasWildDraw4)
                else if (hasNum) {
                    // When you have no wild cards, play a number card and try
                    // to get help from other players. In order to increase your
                    // following players' possibility of changing the legal
                    // color, do not play zero cards preferentially.
                    idxBest = idxNum;
                } // else if (hasNum)
                else if (hasZero) {
                    idxBest = idxZero;
                } // else if (hasZero)
            } // if (lastColor == dangerColor)
            else if (hasNum) {
                // Your previous player started an UNO dash without playing a
                // wild card, so use normal defense strategies. In order to
                // increase your following players' possibility of changing the
                // legal color, do not play zero cards preferentially.
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasZero) {
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasWild && lastColor != bestColor) {
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasWildDraw4 && lastColor != bestColor) {
                idxBest = idxWildDraw4;
            } // else if (hasWildDraw4 && lastColor != bestColor)
        } // else if (prevSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Give more freedom to your next player, the only one that can
            // directly limit your opposite player's action.
            dangerColor = oppo.getDangerousColor();
            if (lastColor == dangerColor) {
                // Your opposite player played a wild card, started an UNO dash
                // in its last action, and what's worse is that the legal color
                // has not been changed yet. You have to change the following
                // legal color, or you will approximately 100% lose this game.
                if (hasZero &&
                        hand.get(idxZero).getRealColor() != dangerColor) {
                    // At first, try to change legal color by playing an action
                    // card or a number card, instead of using wild cards.
                    idxBest = idxZero;
                } // if (hasZero && ...)
                else if (hasNum &&
                        hand.get(idxNum).getRealColor() != dangerColor) {
                    idxBest = idxNum;
                } // else if (hasNum && ...)
                else if (hasRev &&
                        hand.get(idxRev).getRealColor() != dangerColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != dangerColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 &&
                        hand.get(idxDraw2).getRealColor() != dangerColor) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2 && ...)
                else if (hasWild) {
                    // Now start to use your wild cards. Firstly consider to
                    // change to your best color, but when your next player's
                    // last card has the same color to your best color, you
                    // have to change to another color.
                    while (bestColor == dangerColor ||
                            bestColor == next.getDangerousColor() ||
                            bestColor == prev.getDangerousColor()) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (bestColor == dangerColor || ...)

                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasWildDraw4) {
                    while (bestColor == dangerColor ||
                            bestColor == next.getDangerousColor() ||
                            bestColor == prev.getDangerousColor()) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (bestColor == dangerColor || ...)

                    idxBest = idxWildDraw4;
                } // else if (hasWildDraw4)
                else if (hasRev && prevSize - nextSize >= 3) {
                    // Finally try to get help from other players.
                    idxBest = idxRev;
                } // else if (hasRev && prevSize - nextSize >= 3)
                else if (hasNum) {
                    idxBest = idxNum;
                } // else if (hasNum)
                else if (hasZero) {
                    idxBest = idxZero;
                } // else if (hasZero)
            } // if (lastColor == dangerColor)
            else if (dangerColor != Color.NONE) {
                // Your opposite player played a wild card, started an UNO dash
                // in its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the dangerous color again.
                if (hasZero &&
                        hand.get(idxZero).getRealColor() != dangerColor) {
                    idxBest = idxZero;
                } // if (hasZero && ...)
                else if (hasNum &&
                        hand.get(idxNum).getRealColor() != dangerColor) {
                    idxBest = idxNum;
                } // else if (hasNum && ...)
                else if (hasRev &&
                        prevSize >= 4 &&
                        hand.get(idxRev).getRealColor() != dangerColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != dangerColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 &&
                        hand.get(idxDraw2).getRealColor() != dangerColor) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2 && ...)
            } // else if (dangerColor != Color.NONE)
            else if (hasRev && prevSize - nextSize >= 3) {
                // Your opposite player started an UNO dash without playing a
                // wild card, so use normal defense strategies. Firstly play a
                // [reverse] when your next player remains only a few cards but
                // your previous player remains a lot of cards, because your
                // previous player has more possibility to limit your opposite
                // player's action.
                idxBest = idxRev;
            } // else if (hasRev && prevSize - nextSize >= 3)
            else if (hasNum) {
                // Then you can play a number card. In order to increase your
                // next player's possibility of changing the legal color, do
                // not play zero cards preferentially.
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasZero) {
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasWild && lastColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Do not play any [+2]/[skip] to your next player!
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasWildDraw4 && lastColor != bestColor) {
                // When you have no more legal number/reverse cards to play, try
                // to play a wild card and change the legal color to your best
                // color. Specially, for [wild +4] cards, you can only play it
                // when your next player remains only a few cards, and what you
                // did can help your next player find more useful cards, such as
                // action cards, or [wild +4] cards.
                if (nextSize <= 4) {
                    idxBest = idxWildDraw4;
                } // if (nextSize <= 4)
            } // else if (hasWildDraw4 && lastColor != bestColor)
        } // else if (oppoSize == 1)
        else if (next.getRecent() == null && yourSize > 2) {
            // Strategies when your next player drew a card in its last action.
            // You do not need to play your limitation/wild cards when you are
            // not ready to start dash. Use them in more dangerous cases.
            if (hasRev && prevSize - nextSize >= 3) {
                idxBest = idxRev;
            } // if (hasRev && prevSize - nextSize >= 3)
            else if (hasZero) {
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasRev && prevSize >= 4) {
                idxBest = idxRev;
            } // else if (hasRev && prevSize >= 4)
        } // else if (next.getRecent() == null && yourSize > 2)
        else {
            // Normal strategies
            if (hasRev && prevSize - nextSize >= 3) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards, in
                // order to balance everyone's hand-card amount.
                idxBest = idxRev;
            } // if (hasRev && prevSize - nextSize >= 3)
            else if (hasDraw2 && nextSize <= 4) {
                // Play a [+2] when your next player remains only a few cards.
                idxBest = idxDraw2;
            } // else if (hasDraw2 && nextSize <= 4)
            else if (hasSkip && nextSize <= 4) {
                // Play a [skip] when your next player remains only a few cards.
                idxBest = idxSkip;
            } // else if (hasSkip && nextSize <= 4)
            else if (hasZero) {
                // Play zero cards at first because of their rarity.
                idxBest = idxZero;
            } // else if (hasZero)
            else if (hasNum) {
                // Then consider to play a number card.
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasRev && prevSize >= 4) {
                // When you have no more legal number cards to play, you can
                // play a [reverse] safely when your previous player still has
                // a number of cards.
                idxBest = idxRev;
            } // else if (hasRev && prevSize >= 4)
            else if (hasWild && nextSize <= 4) {
                // When your next player remains only a few cards, and you have
                // no more legal number/action cards to play, try to play a
                // wild card and change the legal color to your best color.
                idxBest = idxWild;
            } // else if (hasWild && nextSize <= 4)
            else if (hasWildDraw4 && nextSize <= 4) {
                // When your next player remains only a few cards, and you have
                // no more legal number/action cards to play, try to play a
                // wild card and change the legal color to your best color.
                idxBest = idxWildDraw4;
            } // else if (hasWildDraw4 && nextSize <= 4)
            else if (hasWild && yourSize == 2 && prevSize <= 3) {
                // When you remain only 2 cards, including a wild card, and your
                // previous player seems no enough power to limit you (has too
                // few cards), start your UNO dash!
                if (hasDraw2) {
                    // When you still have a [+2] card, play it, even if it's
                    // not worth to let your next player draw cards.
                    idxBest = idxDraw2;
                } // if (hasDraw2)
                else if (hasSkip) {
                    // When you still have a [skip] card, play it, even if it's
                    // not worth to let your next player skip its turn.
                    idxBest = idxSkip;
                } // else if (hasSkip)
                else {
                    // When you have no more legal draw2/skip cards, play your
                    // wild card to start your UNO dash.
                    idxBest = idxWild;
                } // else
            } // else if (hasWild && yourSize == 2 && prevSize <= 3)
            else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3) {
                if (hasDraw2) {
                    idxBest = idxDraw2;
                } // if (hasDraw2)
                else if (hasSkip) {
                    idxBest = idxSkip;
                } // else if (hasSkip)
                else {
                    idxBest = idxWildDraw4;
                } // else
            } // else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3)
            else if (yourSize == Uno.MAX_HOLD_CARDS) {
                // When you are holding 14 cards, which means you cannot hold
                // more cards, you need to play your action/wild cards to keep
                // game running, even if it's not worth enough to use them.
                if (hasSkip) {
                    idxBest = idxSkip;
                } // if (hasSkip)
                else if (hasDraw2) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2)
                else if (hasRev) {
                    idxBest = idxRev;
                } // else if (hasRev)
                else if (hasWild) {
                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasWildDraw4) {
                    idxBest = idxWildDraw4;
                } // else if (hasWildDraw4)
            } // else if (yourSize == Uno.MAX_HOLD_CARDS)
        } // else

        if (idxBest >= 0) {
            // Found an appropriate card to play
            mImmPlayAsk = false;
            play(idxBest, bestColor);
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
    } // hardAI()

    /**
     * AI strategies when determining whether it's necessary to
     * challenge previous player's [wild +4] card.
     */
    private void challengeAI() {
        Card next2last;
        Color draw4Color;
        boolean challenge;
        Color colorBeforeDraw4;
        List<Card> hand, recent;

        hand = mUno.getPlayer(mStatus).getHandCards();
        if (hand.size() == 1) {
            // Challenge when defending my UNO dash
            challenge = true;
        } // if (hand.size() == 1)
        else if (hand.size() >= Uno.MAX_HOLD_CARDS - 4) {
            // Challenge when I have 10 or more cards already, thus even if
            // challenge failed, I draw at most 4 cards.
            challenge = true;
        } // else if (hand.size() >= Uno.MAX_HOLD_CARDS - 4)
        else {
            // Challenge when legal color has not been changed
            recent = mUno.getRecent();
            next2last = recent.get(recent.size() - 2);
            colorBeforeDraw4 = next2last.getRealColor();
            draw4Color = recent.get(recent.size() - 1).getRealColor();
            challenge = draw4Color == colorBeforeDraw4;
        } // else

        onChallenge(mStatus, challenge);
    } // challengeAI()

    /**
     * Pass someone's round.
     *
     * @param who Pass whose round. Must be one of the following values:
     *            Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    private void pass(int who) {
        Runnable delayedTask;

        if (who >= 0 && who < 4) {
            mStatus = STAT_IDLE; // block mouse click events when idle
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
            mStatus = STAT_IDLE; // block mouse click events when idle
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

                case Player.YOU:
                    if (mAuto) {
                        break; // case Player.YOU
                    } // if (mAuto)
                    else if (mImmPlayAsk) {
                        if (x > 310 && x < 500) {
                            if (y > 220 && y < 315) {
                                // YES button, play the drawn card
                                mImmPlayAsk = false;
                                hand = mUno.getPlayer(mStatus).getHandCards();
                                size = hand.size();
                                for (index = 0; index < size; ++index) {
                                    card = hand.get(index);
                                    if (card == mDrawnCard) {
                                        play(index, card.getRealColor());
                                        break;
                                    } // if (card == mDrawnCard)
                                } // for (index = 0; index < size; ++index)
                            } // if (y > 220 && y < 315)
                            else if (y > 315 && y < 410) {
                                // NO button, go to next player's round
                                mImmPlayAsk = false;
                                pass(mStatus);
                            } // else if (y > 315 && y < 410)
                        } // if (x > 310 && x < 500)
                    } // else if (mImmPlayAsk)
                    else if (mChallengeAsk) {
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
                    mStatus = Player.YOU;
                    if (y > 220 && y < 315 && x > 310 && x < 405) {
                        // Red sector
                        play(mWildIndex, Color.RED);
                    } // if (y > 220 && y < 315 && x > 310 && x < 405)
                    else if (y > 220 && y < 315 && x > 405 && x < 500) {
                        // Blue sector
                        play(mWildIndex, Color.BLUE);
                    } // else if (y > 220 && y < 315 && x > 405 && x < 500)
                    else if (y > 315 && y < 410 && x > 310 && x < 405) {
                        // Yellow sector
                        play(mWildIndex, Color.YELLOW);
                    } // else if (y > 315 && y < 410 && x > 310 && x < 405)
                    else if (y > 315 && y < 410 && x > 405 && x < 500) {
                        // Green sector
                        play(mWildIndex, Color.GREEN);
                    } // else if (y > 315 && y < 410 && x > 405 && x < 500)
                    else {
                        // Undo
                        onStatusChanged(mStatus);
                    } // else
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