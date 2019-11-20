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
        mRnd = new Random();
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
        Card card, last;
        int i, direction;
        List<Card> hand, recent;
        Player next, oppo, prev;
        Color bestColor, lastColor;
        int idxBest, idxRev, idxSkip, idxDraw2;
        boolean hasZero, hasWild, hasWildDraw4;
        boolean hasNum, hasRev, hasSkip, hasDraw2;
        int idxZero, idxNum, idxWild, idxWildDraw4;
        int yourSize, nextSize, oppoSize, prevSize;

        hand = mUno.getPlayer(mStatus).getHandCards();
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
        next = mUno.getPlayer((mStatus + direction) % 4);
        nextSize = next.getHandCards().size();
        oppo = mUno.getPlayer((mStatus + 2) % 4);
        oppoSize = oppo.getHandCards().size();
        prev = mUno.getPlayer((4 + mStatus - direction) % 4);
        prevSize = prev.getHandCards().size();
        idxBest = idxRev = idxSkip = idxDraw2 = -1;
        idxZero = idxNum = idxWild = idxWildDraw4 = -1;
        bestColor = mUno.bestColorFor(mStatus);
        recent = mUno.getRecent();
        last = recent.get(recent.size() - 1);
        if (last.isWild()) {
            lastColor = last.getWildColor();
        } // if (last.isWild())
        else {
            lastColor = last.getColor();
        } // else

        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (mUno.isLegalToPlay(card)) {
                switch (card.getContent()) {
                    case NUM0:
                        if (idxZero < 0 || card.getColor() == bestColor) {
                            idxZero = i;
                        } // if (idxZero < 0 || card.getColor() == bestColor)
                        break; // case NUM0

                    case DRAW2:
                        if (idxDraw2 < 0 || card.getColor() == bestColor) {
                            idxDraw2 = i;
                        } // if (idxDraw2 < 0 || card.getColor() == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (idxSkip < 0 || card.getColor() == bestColor) {
                            idxSkip = i;
                        } // if (idxSkip < 0 || card.getColor() == bestColor)
                        break; // case SKIP

                    case REV:
                        if (idxRev < 0 || card.getColor() == bestColor) {
                            idxRev = i;
                        } // if (idxRev < 0 || card.getColor() == bestColor)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWildDraw4 = i;
                        break; // case WILD_DRAW4

                    default: // non-zero number cards
                        if (idxNum < 0 || card.getColor() == bestColor) {
                            idxNum = i;
                        } // if (idxNum < 0 || card.getColor() == bestColor)
                        break; // default
                } // switch (card.getContent())
            } // if (mUno.isLegalToPlay(card))
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
            play(idxBest, bestColor);
        } // if (idxBest >= 0)
        else {
            // No appropriate cards to play, or no card is legal to play
            draw(mStatus, 1);
        } // else
    } // easyAI()

    /**
     * AI Strategies (Difficulty: HARD).
     */
    private void hardAI() {
        Card card, last;
        int i, direction;
        List<Card> hand, recent;
        Player next, oppo, prev;
        Color bestColor, lastColor;
        int idxBest, idxRev, idxSkip, idxDraw2;
        boolean hasZero, hasWild, hasWildDraw4;
        boolean hasNum, hasRev, hasSkip, hasDraw2;
        int idxZero, idxNum, idxWild, idxWildDraw4;
        int yourSize, nextSize, oppoSize, prevSize;

        hand = mUno.getPlayer(mStatus).getHandCards();
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
        next = mUno.getPlayer((mStatus + direction) % 4);
        nextSize = next.getHandCards().size();
        oppo = mUno.getPlayer((mStatus + 2) % 4);
        oppoSize = oppo.getHandCards().size();
        prev = mUno.getPlayer((4 + mStatus - direction) % 4);
        prevSize = prev.getHandCards().size();
        idxBest = idxRev = idxSkip = idxDraw2 = -1;
        idxZero = idxNum = idxWild = idxWildDraw4 = -1;
        bestColor = mUno.bestColorFor(mStatus);
        recent = mUno.getRecent();
        last = recent.get(recent.size() - 1);
        if (last.isWild()) {
            lastColor = last.getWildColor();
        } // if (last.isWild())
        else {
            lastColor = last.getColor();
        } // else

        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (mUno.isLegalToPlay(card)) {
                switch (card.getContent()) {
                    case NUM0:
                        if (idxZero < 0 || card.getColor() == bestColor) {
                            idxZero = i;
                        } // if (idxZero < 0 || card.getColor() == bestColor)
                        break; // case NUM0

                    case DRAW2:
                        if (idxDraw2 < 0 || card.getColor() == bestColor) {
                            idxDraw2 = i;
                        } // if (idxDraw2 < 0 || card.getColor() == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (idxSkip < 0 || card.getColor() == bestColor) {
                            idxSkip = i;
                        } // if (idxSkip < 0 || card.getColor() == bestColor)
                        break; // case SKIP

                    case REV:
                        if (idxRev < 0 || card.getColor() == bestColor) {
                            idxRev = i;
                        } // if (idxRev < 0 || card.getColor() == bestColor)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWildDraw4 = i;
                        break; // case WILD_DRAW4

                    default: // non-zero number cards
                        if (idxNum < 0 || card.getColor() == bestColor) {
                            idxNum = i;
                        } // if (idxNum < 0 || card.getColor() == bestColor)
                        break; // default
                } // switch (card.getContent())
            } // if (mUno.isLegalToPlay(card))
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
            else if (next.getRecent().getWildColor() == lastColor) {
                // Your next player played a wild card, started a UNO dash in
                // its last action, and what's worse is that the legal color has
                // not been changed yet. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasRev && hand.get(idxRev).getColor() != lastColor) {
                    // At first, try to change legal color by playing an action
                    // card or a number card, instead of using wild cards.
                    idxBest = idxRev;
                } // if (hasRev && ...)
                else if (hasZero && hand.get(idxZero).getColor() != lastColor) {
                    idxBest = idxZero;
                } // else if (hasZero && ...)
                else if (hasNum && hand.get(idxNum).getColor() != lastColor) {
                    idxBest = idxNum;
                } // else if (hasNum && ...)
                else if (hasWild) {
                    // When you cannot change legal color by playing an action
                    // card or a number card, you have to use your Wild card.
                    // Firstly consider to change to your best color, but when
                    // your next player's last card has the same color to your
                    // best color, you have to change to another color.
                    while (lastColor == bestColor) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (lastColor == bestColor)

                    idxBest = idxWild;
                } // else if (hasWild)
            } // else if (next.getRecent().getWildColor() == lastColor)
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
            if (prev.getRecent().getWildColor() == lastColor) {
                // Your previous player played a wild card, started a UNO dash
                // in its last action. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasWild) {
                    // Firstly consider to change to your best color, but when
                    // your next player's last card has the same color to your
                    // best color, you have to change to another color.
                    while (lastColor == bestColor) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (lastColor == bestColor)

                    idxBest = idxWild;
                } // if (hasWild)
                else if (hasWildDraw4) {
                    while (lastColor == bestColor) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (lastColor == bestColor)

                    idxBest = idxWild;
                } // else if (hasWildDraw4)
                else if (hasNum) {
                    // When you have no wild cards, play a number card and try
                    // to get help from other players. In order to increase your
                    // following players' possibility of changing the legal
                    // color, do not play zero cards priorly.
                    idxBest = idxNum;
                } // else if (hasNum)
                else if (hasZero) {
                    idxBest = idxZero;
                } // else if (hasZero)
            } // if (prev.getRecent().getWildColor() == lastColor)
            else if (hasNum) {
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
            if (oppo.getRecent().getWildColor() == lastColor) {
                // Your opposite player played a wild card, started a UNO dash
                // in its last action, and what's worse is that the legal color
                // has not been changed yet. You have to change the following
                // legal color, or you will approximately 100% lose this game.
                if (hasZero && hand.get(idxZero).getColor() != lastColor) {
                    // At first, try to change legal color by playing an action
                    // card or a number card, instead of using wild cards.
                    idxBest = idxZero;
                } // if (hasZero && ...)
                else if (hasNum && hand.get(idxNum).getColor() != lastColor) {
                    idxBest = idxNum;
                } // else if (hasNum && ...)
                else if (hasRev && hand.get(idxRev).getColor() != lastColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip && hand.get(idxSkip).getColor() != lastColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 && hand.get(idxDraw2).getColor() != lastColor) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2 && ...)
                else if (hasWild) {
                    // When you cannot change legal color by playing an action
                    // card or a number card, you have to use your Wild card.
                    // Firstly consider to change to your best color, but when
                    // your next player's last card has the same color to your
                    // best color, you have to change to another color.
                    while (lastColor == bestColor) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (lastColor == bestColor)

                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasWildDraw4) {
                    while (lastColor == bestColor) {
                        bestColor = Color.values()[mRnd.nextInt(4) + 1];
                    } // while (lastColor == bestColor)

                    idxBest = idxWildDraw4;
                } // else if (hasWildDraw4)
                else if (hasNum) {
                    // When you have no wild cards, play a number card and try
                    // to get help from your next player.
                    idxBest = idxNum;
                } // else if (hasNum)
                else if (hasZero) {
                    idxBest = idxZero;
                } // else if (hasZero)
            } // if (oppo.getRecent().getWildColor() == lastColor)
            else if (hasRev && prevSize - nextSize >= 3) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards,
                // because your previous player has more possibility to limit
                // your opposite player's action.
                idxBest = idxRev;
            } // else if (hasRev && prevSize - nextSize >= 3)
            else if (hasNum) {
                // Then you can play a number card. In order to increase your
                // next player's possibility of changing the legal color, do
                // not play zero cards priorly.
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
        } // else

        if (idxBest >= 0) {
            // Found an appropriate card to play
            play(idxBest, bestColor);
        } // if (idxBest >= 0)
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
        int i, status, size, width, height;
        int remain, used, easyRate, hardRate;

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
        hand = mUno.getPlayer(Uno.PLAYER_COM1).getHandCards();
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
            if (status == STAT_GAME_OVER) {
                // Show remained cards to everyone when game over
                for (Card card : hand) {
                    image = card.getImage();
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (Card card : hand)
            } // if (status == STAT_GAME_OVER)
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
        hand = mUno.getPlayer(Uno.PLAYER_COM2).getHandCards();
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
            if (status == STAT_GAME_OVER) {
                // Show remained hand cards when game over
                for (Card card : hand) {
                    image = card.getImage();
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.x += 45;
                } // for (Card card : hand)
            } // if (status == STAT_GAME_OVER)
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
        hand = mUno.getPlayer(Uno.PLAYER_COM3).getHandCards();
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
            if (status == STAT_GAME_OVER) {
                // Show remained hand cards when game over
                for (Card card : hand) {
                    image = card.getImage();
                    image.copyTo(new Mat(mScr, roi), image);
                    roi.y += 40;
                } // for (Card card : hand)
            } // if (status == STAT_GAME_OVER)
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
        hand = mUno.getPlayer(Uno.PLAYER_YOU).getHandCards();
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
        int now;
        Rect roi;
        Mat image;
        Card card;
        Runnable delayedTask;

        now = mStatus;
        mStatus = STAT_IDLE; // block tap down events when idle
        card = mUno.play(now, index, color);
        if (card != null) {
            image = card.getImage();
            switch (now) {
                case Uno.PLAYER_COM1:
                    roi = new Rect(160, 270, 121, 181);
                    break; // case Uno.PLAYER_COM1

                case Uno.PLAYER_COM2:
                    roi = new Rect(650, 220, 121, 181);
                    break; // case Uno.PLAYER_COM2

                case Uno.PLAYER_COM3:
                    roi = new Rect(1000, 270, 121, 181);
                    break; // case Uno.PLAYER_COM3

                default:
                    roi = new Rect(650, 320, 121, 181);
                    break; // default
            } // switch (now)

            // Animation
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
            mHandler.postDelayed(delayedTask, 300);
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
                        hand = mUno.getPlayer(Uno.PLAYER_YOU).getHandCards();
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
                        break; // case Uno.PLAYER_COM1

                    case Uno.PLAYER_COM2:
                        if (count == 1) {
                            message = NAME[who] + ": Draw a card";
                        } // if (count == 1)
                        else {
                            message = NAME[who] + ": Draw " + count + " cards";
                        } // else

                        image = mUno.getBackImage();
                        roi = new Rect(420, 220, 121, 181);
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
                        break; // case Uno.PLAYER_COM3

                    default:
                        message = NAME[who] + ": Draw " + card.getName();
                        image = card.getImage();
                        roi = new Rect(420, 320, 121, 181);
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