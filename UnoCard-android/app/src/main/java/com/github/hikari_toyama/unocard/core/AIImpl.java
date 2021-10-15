////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import static com.github.hikari_toyama.unocard.core.Color.BLUE;
import static com.github.hikari_toyama.unocard.core.Color.GREEN;
import static com.github.hikari_toyama.unocard.core.Color.NONE;
import static com.github.hikari_toyama.unocard.core.Color.RED;
import static com.github.hikari_toyama.unocard.core.Color.YELLOW;
import static com.github.hikari_toyama.unocard.core.Content.NUM0;
import static com.github.hikari_toyama.unocard.core.Content.NUM7;
import static com.github.hikari_toyama.unocard.core.Content.WILD;
import static com.github.hikari_toyama.unocard.core.Content.WILD_DRAW4;

import android.content.Context;

import java.util.List;
import java.util.Random;

/**
 * AI Strategies.
 */
class AIImpl extends AI {
    /**
     * Random number generator.
     */
    private static final Random RND = new Random();

    /**
     * Constructor.
     *
     * @param context Pass a Context (MainActivity.this) to let us get the Uno
     *                runtime instance. Uno runtime needs a Context to refer
     *                application resources, such as card images.
     */
    AIImpl(Context context) {
        super(context);
    } // AIImpl(Context) (Class Constructor)

    /**
     * Evaluate which color is the best for current player. In our evaluation
     * system, zero cards / reverse cards are worth 2 points, non-zero number
     * cards are worth 4 points, and skip / draw two cards are worth 5 points.
     * Finally, the color which contains the worthiest cards becomes the best
     * color.
     *
     * @return Current player's best color.
     */
    @Override
    public Color calcBestColor4NowPlayer() {
        Color bestColor;
        Player next, oppo, prev;
        Color nextWeak, nextStrong;
        Color oppoWeak, oppoStrong;
        Color prevWeak, prevStrong;
        boolean nextIsUno, oppoIsUno, prevIsUno;

        // When defensing UNO dash, use others' weak color as your best color
        next = uno.getPlayer(uno.getNext());
        oppo = uno.getPlayer(uno.getOppo());
        prev = uno.getPlayer(uno.getPrev());
        nextIsUno = next.getHandSize() == 1;
        oppoIsUno = oppo.getHandSize() == 1;
        prevIsUno = prev.getHandSize() == 1;
        nextWeak = next.getWeakColor();
        oppoWeak = oppo.getWeakColor();
        prevWeak = prev.getWeakColor();
        if (nextIsUno && nextWeak != NONE) {
            bestColor = nextWeak;
        } // if (nextIsUno && nextWeak != NONE)
        else if (oppoIsUno && oppoWeak != NONE) {
            bestColor = oppoWeak;
        } // else if (oppoIsUno && oppoWeak != NONE)
        else if (prevIsUno && prevWeak != NONE) {
            bestColor = prevWeak;
        } // else if (prevIsUno && prevWeak != NONE)
        else {
            int[] score = {0, 0, 0, 0, 0};
            Player curr = uno.getPlayer(uno.getNow());

            for (Card card : curr.getHandCards()) {
                switch (card.content) {
                    case WILD:
                    case WILD_DRAW4:
                        break; // case WILD, WILD_DRAW4

                    case REV:
                    case NUM0:
                        score[card.color.ordinal()] += 2;
                        break; // case REV, NUM0

                    case SKIP:
                    case DRAW2:
                        score[card.color.ordinal()] += 5;
                        break; // case SKIP, DRAW2

                    default:
                        score[card.color.ordinal()] += 4;
                        break; // default
                } // switch (card.content)
            } // for (Card card : curr.getHandCards())

            // Calculate the best color
            bestColor = NONE;
            if (score[RED.ordinal()] > score[bestColor.ordinal()]) {
                bestColor = RED;
            } // if (score[RED.ordinal()] > score[bestColor.ordinal()])

            if (score[BLUE.ordinal()] > score[bestColor.ordinal()]) {
                bestColor = BLUE;
            } // if (score[BLUE.ordinal()] > score[bestColor.ordinal()])

            if (score[GREEN.ordinal()] > score[bestColor.ordinal()]) {
                bestColor = GREEN;
            } // if (score[GREEN.ordinal()] > score[bestColor.ordinal()])

            if (score[YELLOW.ordinal()] > score[bestColor.ordinal()]) {
                bestColor = YELLOW;
            } // if (score[YELLOW.ordinal()] > score[bestColor.ordinal()])

            if (bestColor == NONE) {
                // Only wild cards in hand
                // Use others' weak color as your best color
                bestColor
                        = prevWeak != NONE ? prevWeak
                        : oppoWeak != NONE ? oppoWeak
                        : nextWeak != NONE ? nextWeak : RED;
            } // if (bestColor == NONE)
        } // else

        // Determine your best color in dangerous cases. Be careful of the
        // conflict with other opponents' strong colors.
        nextStrong = next.getStrongColor();
        oppoStrong = oppo.getStrongColor();
        prevStrong = prev.getStrongColor();
        while ((nextIsUno && bestColor == nextStrong)
                || (oppoIsUno && bestColor == oppoStrong)
                || (prevIsUno && bestColor == prevStrong)) {
            bestColor = Color.values()[RND.nextInt(4) + 1];
        } // while (nextIsUno && bestColor == nextStrong || ...)

        return bestColor;
    } // calcBestColor4NowPlayer()

    /**
     * In 7-0 rule, when a seven card is put down, the player must swap hand
     * cards with another player immediately. This API returns that swapping
     * with whom is the best answer for current player.
     *
     * @return Current player swaps with whom. Must be one of the following:
     * Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     */
    @Override
    public int bestSwapTarget4NowPlayer() {
        int who;
        Color lastColor;
        Player next, oppo, target;

        // Swap with your previous player as default
        who = uno.getPrev();
        target = uno.getPlayer(who);
        lastColor = uno.lastColor();

        // If your opponent player has the strong color matching the last card,
        // or it holds fewer cards, change the swap target to it
        oppo = uno.getPlayer(uno.getOppo());
        if ((oppo.getHandSize() < target.getHandSize()
                && oppo.getWeakColor() != lastColor)
                || (oppo.getStrongColor() == lastColor
                && target.getStrongColor() != lastColor)) {
            who = uno.getOppo();
            target = uno.getPlayer(who);
        } // if (oppo.getHandSize() < target.getHandSize() && ...)

        // If your next player has the strong color matching the last card,
        // or it holds fewer cards, change the swap target to it
        next = uno.getPlayer(uno.getNext());
        if ((next.getHandSize() < next.getHandSize()
                && next.getWeakColor() != lastColor)
                || (next.getStrongColor() == lastColor
                && target.getStrongColor() != lastColor)) {
            who = uno.getNext();
        } // if (next.getHandSize() < target.getHandSize() && ...)

        return who;
    } // bestSwapTarget4NowPlayer()

    /**
     * AI strategies of determining if it's necessary to challenge previous
     * player's [wild +4] card's legality.
     *
     * @return True if it's necessary to make a challenge.
     */
    @Override
    public boolean needToChallenge() {
        int size = uno.getPlayer(uno.getNext()).getHandSize();

        // Challenge when defending my UNO dash
        // Challenge when I have 10 or more cards already
        // Challenge when legal color has not been changed
        return (size == 1 ||
                size >= Uno.MAX_HOLD_CARDS - 4 ||
                uno.lastColor() == uno.next2lastColor());
    } // needToChallenge()

    /**
     * AI Strategies (Difficulty: EASY). Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param outColor This is a out parameter. Pass a Color array (length>=1)
     *                 in order to let us pass the return value by assigning
     *                 outColor[0]. When the best card to play becomes a wild
     *                 card, outColor[0] will become the following legal color
     *                 to change. When the best card to play becomes an action
     *                 or a number card, outColor[0] will become the player's
     *                 best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @Override
    public int easyAI_bestCardIndex4NowPlayer(Color[] outColor) {
        Card card;
        String errMsg;
        int i, idxBest;
        List<Card> hand;
        Color bestColor, lastColor;
        int yourSize, nextSize, prevSize;
        int idxNum, idxRev, idxSkip, idxDraw2, idxWild, idxWD4;
        boolean hasNum, hasRev, hasSkip, hasDraw2, hasWild, hasWD4;

        if (outColor == null || outColor.length == 0) {
            errMsg = "outColor cannot be null or Color[0]";
            throw new IllegalArgumentException(errMsg);
        }  // if (outColor == null || outColor.length == 0)

        hand = uno.getPlayer(uno.getNow()).getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            outColor[0] = card.color;
            return uno.isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        idxBest = -1;
        lastColor = uno.lastColor();
        bestColor = calcBestColor4NowPlayer();
        idxNum = idxRev = idxSkip = idxDraw2 = idxWild = idxWD4 = -1;
        hasNum = hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (uno.isLegalToPlay(card)) {
                switch (card.content) {
                    case DRAW2:
                        if (!hasDraw2 || card.color == bestColor) {
                            idxDraw2 = i;
                            hasDraw2 = true;
                        } // if (!hasDraw2 || card.color == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (!hasSkip || card.color == bestColor) {
                            idxSkip = i;
                            hasSkip = true;
                        } // if (!hasSkip || card.color == bestColor)
                        break; // case SKIP

                    case REV:
                        if (!hasRev || card.color == bestColor) {
                            idxRev = i;
                            hasRev = true;
                        } // if (!hasRev || card.color == bestColor)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        hasWild = true;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWD4 = i;
                        hasWD4 = true;
                        break; // case WILD_DRAW4

                    default: // number cards
                        if (!hasNum || card.color == bestColor) {
                            idxNum = i;
                            hasNum = true;
                        } // if (!hasNum || card.color == bestColor)
                        break; // default
                } // switch (card.content)
            } // if (uno.isLegalToPlay(card))
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        nextSize = uno.getPlayer(uno.getNext()).getHandSize();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2)
                idxBest = idxDraw2;
            else if (hasSkip)
                idxBest = idxSkip;
            else if (hasRev)
                idxBest = idxRev;
            else if (hasWD4 && lastColor != bestColor)
                idxBest = idxWD4;
            else if (hasWild && lastColor != bestColor)
                idxBest = idxWild;
            else if (hasNum)
                idxBest = idxNum;
        } // if (nextSize == 1)
        else {
            // Normal strategies
            prevSize = uno.getPlayer(uno.getPrev()).getHandSize();
            if (hasRev && prevSize > nextSize)
                idxBest = idxRev;
            else if (hasNum)
                idxBest = idxNum;
            else if (hasSkip)
                idxBest = idxSkip;
            else if (hasDraw2)
                idxBest = idxDraw2;
            else if (hasRev && prevSize > 1)
                idxBest = idxRev;
            else if (hasWild)
                idxBest = idxWild;
            else if (hasWD4)
                idxBest = idxWD4;
        } // else

        outColor[0] = bestColor;
        return idxBest;
    } // easyAI_bestCardIndex4NowPlayer(Card, Color[])

    /**
     * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
     * and calculate which is the best card to play.
     *
     * @param outColor This is a out parameter. Pass a Color array (length>=1)
     *                 in order to let us pass the return value by assigning
     *                 outColor[0]. When the best card to play becomes a wild
     *                 card, outColor[0] will become the following legal color
     *                 to change. When the best card to play becomes an action
     *                 or a number card, outColor[0] will become the player's
     *                 best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @Override
    @SuppressWarnings("CStyleArrayDeclaration")
    public int hardAI_bestCardIndex4NowPlayer(Color[] outColor) {
        Card card;
        String errMsg;
        int i, idxBest;
        boolean allWild;
        List<Card> hand;
        Player next, oppo, prev;
        Color bestColor, lastColor;
        int yourSize, nextSize, oppoSize, prevSize;
        Color nextWeak, nextStrong, oppoStrong, prevStrong;
        int idxNumIn[], idxRev, idxSkip, idxDraw2, idxWild, idxWD4;
        boolean hasNumIn[], hasRev, hasSkip, hasDraw2, hasWild, hasWD4;

        if (outColor == null || outColor.length == 0) {
            errMsg = "outColor cannot be null or Color[0]";
            throw new IllegalArgumentException(errMsg);
        }  // if (outColor == null || outColor.length == 0)

        hand = uno.getPlayer(uno.getNow()).getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            outColor[0] = card.color;
            return uno.isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        idxBest = -1;
        allWild = true;
        lastColor = uno.lastColor();
        bestColor = calcBestColor4NowPlayer();
        idxNumIn = new int[]{-1, -1, -1, -1, -1};
        idxRev = idxSkip = idxDraw2 = idxWild = idxWD4 = -1;
        hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
        hasNumIn = new boolean[]{false, false, false, false, false};
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            allWild = allWild && card.isWild();
            if (uno.isLegalToPlay(card)) {
                switch (card.content) {
                    case DRAW2:
                        if (!hasDraw2 || card.color == bestColor) {
                            idxDraw2 = i;
                            hasDraw2 = true;
                        } // if (!hasDraw2 || card.color == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (!hasSkip || card.color == bestColor) {
                            idxSkip = i;
                            hasSkip = true;
                        } // if (!hasSkip || card.color == bestColor)
                        break; // case SKIP

                    case REV:
                        if (!hasRev || card.color == bestColor) {
                            idxRev = i;
                            hasRev = true;
                        } // if (!hasRev || card.color == bestColor)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        hasWild = true;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWD4 = i;
                        hasWD4 = true;
                        break; // case WILD_DRAW4

                    default: // number cards
                        idxNumIn[card.color.ordinal()] = i;
                        hasNumIn[card.color.ordinal()] = true;
                        break; // default
                } // switch (card.content)
            } // if (uno.isLegalToPlay(card))
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        next = uno.getPlayer(uno.getNext());
        nextSize = next.getHandSize();
        nextWeak = next.getWeakColor();
        nextStrong = next.getStrongColor();
        oppo = uno.getPlayer(uno.getOppo());
        oppoSize = oppo.getHandSize();
        oppoStrong = oppo.getStrongColor();
        prev = uno.getPlayer(uno.getPrev());
        prevSize = prev.getHandSize();
        prevStrong = prev.getStrongColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2)
                idxBest = idxDraw2;
            else if (lastColor == nextStrong) {
                // Priority when next called Uno & lastColor == nextStrong:
                // 0: Number cards, NOT in color of nextStrong
                // 1: Skip cards, in any color
                // 2: Wild cards, switch to your best color
                // 3: Wild +4 cards, switch to your best color
                // 4: Reverse cards, in any color
                // 5: Draw one, and pray to get one of the above...
                if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()] && nextStrong != RED
                        && (prevSize > 1 || prevStrong != RED)
                        && (oppoSize > 1 || oppoStrong != RED))
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()] && nextStrong != BLUE
                        && (prevSize > 1 || prevStrong != BLUE)
                        && (oppoSize > 1 || oppoStrong != BLUE))
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()] && nextStrong != GREEN
                        && (prevSize > 1 || prevStrong != GREEN)
                        && (oppoSize > 1 || oppoStrong != GREEN))
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()] && nextStrong != YELLOW
                        && (prevSize > 1 || prevStrong != YELLOW)
                        && (oppoSize > 1 || oppoStrong != YELLOW))
                    idxBest = idxNumIn[YELLOW.ordinal()];
                else if (hasSkip)
                    idxBest = idxSkip;
                else if (hasWild)
                    idxBest = idxWild;
                else if (hasWD4)
                    idxBest = idxWD4;
                else if (hasRev)
                    idxBest = idxRev;
            } // if (lastColor == nextStrong)
            else if (nextStrong != NONE) {
                // Priority when next called Uno & lastColor != nextStrong:
                // (nextStrong is known)
                // 0: Number cards, NOT in color of nextStrong
                // 1: Reverse cards, NOT in color of nextStrong
                // 2: Skip cards, NOT in color of nextStrong
                // 3: Draw one because it's not necessary to use wild cards
                if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()] && nextStrong != RED
                        && (prevSize > 1 || prevStrong != RED)
                        && (oppoSize > 1 || oppoStrong != RED))
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()] && nextStrong != BLUE
                        && (prevSize > 1 || prevStrong != BLUE)
                        && (oppoSize > 1 || oppoStrong != BLUE))
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()] && nextStrong != GREEN
                        && (prevSize > 1 || prevStrong != GREEN)
                        && (oppoSize > 1 || oppoStrong != GREEN))
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()] && nextStrong != YELLOW
                        && (prevSize > 1 || prevStrong != YELLOW)
                        && (oppoSize > 1 || oppoStrong != YELLOW))
                    idxBest = idxNumIn[YELLOW.ordinal()];
                else if (hasRev && prevSize >= 4
                        && hand.get(idxRev).color != nextStrong)
                    idxBest = idxRev;
                else if (hasSkip && hand.get(idxSkip).color != nextStrong)
                    idxBest = idxSkip;
            } // else if (nextStrong != NONE)
            else {
                // Priority when next called Uno & nextStrong is unknown:
                // 0: Skip cards, in any color
                // 1: Reverse cards, in any color
                // 2: Wild +4 cards, if no cards matching last color
                // 3: Number cards, in your best color
                // 4: Wild cards, switch to your best color
                // 5: Wild +4 cards, switch to your best color
                // 6: Number cards, in any color
                if (hasSkip)
                    idxBest = idxSkip;
                else if (hasRev)
                    idxBest = idxRev;
                else if (hasWD4 && !hasNumIn[lastColor.ordinal()])
                    idxBest = idxWD4;
                else if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasWild)
                    idxBest = idxWild;
                else if (hasWD4)
                    idxBest = idxWD4;
                else if (hasNumIn[RED.ordinal()]
                        && (prevSize > 1 || prevStrong != RED)
                        && (oppoSize > 1 || oppoStrong != RED))
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()]
                        && (prevSize > 1 || prevStrong != BLUE)
                        && (oppoSize > 1 || oppoStrong != BLUE))
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()]
                        && (prevSize > 1 || prevStrong != GREEN)
                        && (oppoSize > 1 || oppoStrong != GREEN))
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()]
                        && (prevSize > 1 || prevStrong != YELLOW)
                        && (oppoSize > 1 || oppoStrong != YELLOW))
                    idxBest = idxNumIn[YELLOW.ordinal()];
            } // else
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Save your action cards as much as you can, because once a reverse
            // card is put down, you can use these cards to limit your previous
            // player's action.
            if (lastColor == prevStrong) {
                // Priority when prev called Uno & lastColor == prevStrong:
                // 0: Skip cards, NOT in color of prevStrong
                // 1: Wild cards, switch to your best color
                // 2: Wild +4 cards, switch to your best color
                // 3: Number cards, in any color, but firstly your best color
                // 4: Draw one because it's not necessary to use other cards
                if (hasSkip && hand.get(idxSkip).color != prevStrong)
                    idxBest = idxSkip;
                else if (hasWild)
                    idxBest = idxWild;
                else if (hasWD4)
                    idxBest = idxWD4;
                else if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()])
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()])
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()])
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()])
                    idxBest = idxNumIn[YELLOW.ordinal()];
            } // if (lastColor == prevStrong)
            else if (prevStrong != NONE) {
                // Priority when prev called Uno & lastColor != prevStrong:
                // (prevStrong is known)
                // 0: Reverse cards, NOT in color of prevStrong
                // 1: Number cards, NOT in color of prevStrong
                // 2: Draw one because it's not necessary to use other cards
                if (hasRev && hand.get(idxRev).color != prevStrong)
                    idxBest = idxRev;
                else if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()] && prevStrong != RED
                        && (oppoSize > 1 || oppoStrong != RED))
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()] && prevStrong != BLUE
                        && (oppoSize > 1 || oppoStrong != BLUE))
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()] && prevStrong != GREEN
                        && (oppoSize > 1 || oppoStrong != GREEN))
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()] && prevStrong != YELLOW
                        && (oppoSize > 1 || oppoStrong != YELLOW))
                    idxBest = idxNumIn[YELLOW.ordinal()];
            } // else if (prevStrong != NONE)
            else {
                // Priority when prev called Uno & prevStrong is unknown:
                // 0: Number cards, in your best color
                // 1: Wild cards, switch to your best color
                // 2: Wild +4 cards, switch to your best color
                // 3: Number cards, in any color
                // 4: Draw one. DO NOT PLAY REVERSE CARDS!
                if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasWild && lastColor != bestColor)
                    idxBest = idxWild;
                else if (hasWD4 && lastColor != bestColor)
                    idxBest = idxWD4;
                else if (hasNumIn[RED.ordinal()])
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()])
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()])
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()])
                    idxBest = idxNumIn[YELLOW.ordinal()];
            } // else
        } // else if (prevSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Give more freedom to your next player, the only one that can
            // directly limit your opposite player's action.
            if (lastColor == oppoStrong) {
                // Priority when oppo called Uno & lastColor == oppoStrong:
                // 0: Number cards, NOT in color of oppoStrong
                // 1: Reverse cards, NOT in color of oppoStrong
                // 2: Skip cards, NOT in color of oppoStrong
                // 3: +2 cards, NOT in color of oppoStrong
                // 4: Wild cards, switch to your best color
                // 5: Wild +4 cards, switch to your best color
                // 6: Reverse cards, in color of oppoStrong
                //    (only when prevSize > nextSize)
                //    (pray that prev can limit oppo!)
                // 7: Number cards, in color of oppoStrong
                //    (pray that next can limit oppo!)
                if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()] && oppoStrong != RED)
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()] && oppoStrong != BLUE)
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()] && oppoStrong != GREEN)
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()] && oppoStrong != YELLOW)
                    idxBest = idxNumIn[YELLOW.ordinal()];
                else if (hasRev && hand.get(idxRev).color != oppoStrong)
                    idxBest = idxRev;
                else if (hasSkip && hand.get(idxSkip).color != oppoStrong)
                    idxBest = idxSkip;
                else if (hasDraw2 && hand.get(idxDraw2).color != oppoStrong)
                    idxBest = idxDraw2;
                else if (hasWild)
                    idxBest = idxWild;
                else if (hasWD4)
                    idxBest = idxWD4;
                else if (hasRev && prevSize > nextSize)
                    idxBest = idxRev;
                else if (hasNumIn[RED.ordinal()])
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()])
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()])
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()])
                    idxBest = idxNumIn[YELLOW.ordinal()];
            } // if (lastColor == oppoStrong)
            else if (oppoStrong != NONE) {
                // Priority when oppo called Uno & lastColor != oppoStrong:
                // (oppoStrong is known)
                // 0: Number cards, NOT in color of oppoStrong
                // 1: Reverse cards, NOT in color of oppoStrong
                // 2: Skip cards, NOT in color of oppoStrong
                // 3: +2 cards, NOT in color of oppoStrong
                // 4: Draw one because it's not necessary to use other cards
                if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()] && oppoStrong != RED)
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()] && oppoStrong != BLUE)
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()] && oppoStrong != GREEN)
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()] && oppoStrong != YELLOW)
                    idxBest = idxNumIn[YELLOW.ordinal()];
                else if (hasRev && hand.get(idxRev).color != oppoStrong)
                    idxBest = idxRev;
                else if (hasSkip && nextSize <= 4
                        && hand.get(idxSkip).color != oppoStrong)
                    idxBest = idxSkip;
                else if (hasDraw2 && nextSize <= 4
                        && hand.get(idxDraw2).color != oppoStrong)
                    idxBest = idxDraw2;
            } // else if (oppoStrong != NONE)
            else {
                // Priority when oppo called Uno & oppoStrong is unknown:
                // 0: Reverse cards, in any color
                //    (only when prevSize > nextSize)
                // 1: Number cards, in any color, but firstly your best color
                // 2: Wild cards, switch to your best color
                // 3: Wild +4 cards, switch to your best color
                // 4: Draw one because it's not necessary to use other cards
                if (hasRev && prevSize > nextSize)
                    idxBest = idxRev;
                else if (hasNumIn[bestColor.ordinal()])
                    idxBest = idxNumIn[bestColor.ordinal()];
                else if (hasNumIn[RED.ordinal()])
                    idxBest = idxNumIn[RED.ordinal()];
                else if (hasNumIn[BLUE.ordinal()])
                    idxBest = idxNumIn[BLUE.ordinal()];
                else if (hasNumIn[GREEN.ordinal()])
                    idxBest = idxNumIn[GREEN.ordinal()];
                else if (hasNumIn[YELLOW.ordinal()])
                    idxBest = idxNumIn[YELLOW.ordinal()];
                else if (hasWild && lastColor != bestColor)
                    idxBest = idxWild;
                else if (hasWD4 && lastColor != bestColor && nextSize <= 4)
                    idxBest = idxWD4;
            } // else
        } // else if (oppoSize == 1)
        else if (allWild) {
            // Strategies when you remain only wild cards.
            // When your next player remains only a few cards, use [Wild +4]
            // cards at first. Otherwise, use [Wild] cards at first.
            if (nextSize <= 4)
                idxBest = hasWD4 ? idxWD4 : idxWild;
            else
                idxBest = hasWild ? idxWild : idxWD4;
        } // else if (allWild)
        else if (lastColor == nextWeak && yourSize > 2) {
            // Strategies when your next player drew a card in its last action.
            // Unless keeping or changing to your best color, you do not need to
            // play your limitation/wild cards. Use them in more dangerous cases.
            // Priority:
            // 0: Reverse cards, in any color
            //    (only when prevSize > nextSize)
            // 1: Number cards, in (nextWeak > bestColor > others)
            // 2: Reverse cards, in any color
            // 3: Skip cards, in your best color
            // 4: +2 cards, in your best color
            if (hasRev && prevSize > nextSize)
                idxBest = idxRev;
            else if (hasNumIn[nextWeak.ordinal()])
                idxBest = idxNumIn[nextWeak.ordinal()];
            else if (hasNumIn[bestColor.ordinal()])
                idxBest = idxNumIn[bestColor.ordinal()];
            else if (hasNumIn[RED.ordinal()])
                idxBest = idxNumIn[RED.ordinal()];
            else if (hasNumIn[BLUE.ordinal()])
                idxBest = idxNumIn[BLUE.ordinal()];
            else if (hasNumIn[GREEN.ordinal()])
                idxBest = idxNumIn[GREEN.ordinal()];
            else if (hasNumIn[YELLOW.ordinal()])
                idxBest = idxNumIn[YELLOW.ordinal()];
            else if (hasRev && (prevSize >= 4 || prev.getRecent() == null))
                idxBest = idxRev;
            else if (hasSkip && oppoSize >= 3
                    && hand.get(idxSkip).color == bestColor)
                idxBest = idxSkip;
            else if (hasDraw2 && oppoSize >= 3
                    && hand.get(idxDraw2).color == bestColor)
                idxBest = idxDraw2;
        } // else if (lastColor == nextWeak && yourSize > 2)
        else {
            // Normal strategies
            // Priority:
            // 0: +2 cards, in any color, when nextSize <= 4
            // 1: Skip cards, in any color, when nextSize <= 4
            // 2: Reverse cards, in any color, when prevSize > nextSize,
            //    or prev drew a card in its last action
            // 3: Number cards, in any color, but firstly your best color
            // 4: Skip cards, in your best color
            // 5: +2 cards, in your best color
            // 6: Wild cards, switch to your best color, when nextSize <= 4
            // 7: Wild +4 cards, switch to your best color, when nextSize <= 4
            // 8: Wild +4 cards, when yourSize == 2 && prevSize <= 3 (UNO dash!)
            // 9: Wild cards, when yourSize == 2 && prevSize <= 3 (UNO dash!)
            if (hasDraw2 && nextSize <= 4 && nextSize - oppoSize <= 1)
                idxBest = idxDraw2;
            else if (hasSkip && nextSize <= 4 && nextSize - oppoSize <= 1)
                idxBest = idxSkip;
            else if (hasRev &&
                    (prevSize > nextSize || prev.getRecent() == null))
                idxBest = idxRev;
            else if (hasNumIn[bestColor.ordinal()])
                idxBest = idxNumIn[bestColor.ordinal()];
            else if (hasNumIn[RED.ordinal()])
                idxBest = idxNumIn[RED.ordinal()];
            else if (hasNumIn[BLUE.ordinal()])
                idxBest = idxNumIn[BLUE.ordinal()];
            else if (hasNumIn[GREEN.ordinal()])
                idxBest = idxNumIn[GREEN.ordinal()];
            else if (hasNumIn[YELLOW.ordinal()])
                idxBest = idxNumIn[YELLOW.ordinal()];
            else if (hasRev && prevSize >= 4)
                idxBest = idxRev;
            else if (hasSkip && oppoSize >= 3
                    && hand.get(idxSkip).color == bestColor)
                idxBest = idxSkip;
            else if (hasDraw2 && oppoSize >= 3
                    && hand.get(idxDraw2).color == bestColor)
                idxBest = idxDraw2;
            else if (hasWild && nextSize <= 4)
                idxBest = idxWild;
            else if (hasWD4 && nextSize <= 4)
                idxBest = idxWD4;
            else if (hasWD4 && yourSize == 2 && prevSize <= 3)
                idxBest = idxWD4;
            else if (hasWild && yourSize == 2 && prevSize <= 3)
                idxBest = idxWild;
            else if (yourSize == Uno.MAX_HOLD_CARDS) {
                // When you are holding 14 cards, which means you cannot hold
                // more cards, you need to play your action/wild cards to keep
                // game running, even if it's not worth enough to use them.
                if (hasSkip)
                    idxBest = idxSkip;
                else if (hasDraw2)
                    idxBest = idxDraw2;
                else if (hasRev)
                    idxBest = idxRev;
                else if (hasWild)
                    idxBest = idxWild;
                else if (hasWD4)
                    idxBest = idxWD4;
            } // else if (yourSize == Uno.MAX_HOLD_CARDS)
        } // else

        outColor[0] = bestColor;
        return idxBest;
    } // hardAI_bestCardIndex4NowPlayer(Card, Color[])

    /**
     * AI Strategies in 7-0 special rule. Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param outColor This is a out parameter. Pass a Color array (length>=1)
     *                 in order to let us pass the return value by assigning
     *                 outColor[0]. When the best card to play becomes a wild
     *                 card, outColor[0] will become the following legal color
     *                 to change. When the best card to play becomes an action
     *                 or a number card, outColor[0] will become the player's
     *                 best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @Override
    public int sevenZeroAI_bestCardIndex4NowPlayer(Color[] outColor) {
        Card card;
        String errMsg;
        int i, idxBest;
        List<Card> hand;
        Player next, oppo, prev;
        Color bestColor, lastColor;
        int idx0, idxNum, idxWild, idxWD4;
        int idx7, idxRev, idxSkip, idxDraw2;
        boolean has0, hasNum, hasWild, hasWD4;
        boolean has7, hasRev, hasSkip, hasDraw2;
        Color nextStrong, oppoStrong, prevStrong;
        int yourSize, nextSize, oppoSize, prevSize;

        if (outColor == null || outColor.length == 0) {
            errMsg = "outColor cannot be null or Color[0]";
            throw new IllegalArgumentException(errMsg);
        }  // if (outColor == null || outColor.length == 0)

        hand = uno.getPlayer(uno.getNow()).getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            outColor[0] = card.color;
            return uno.isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        idxBest = -1;
        lastColor = uno.lastColor();
        bestColor = calcBestColor4NowPlayer();
        idx0 = idxNum = idxWild = idxWD4 = -1;
        has0 = hasNum = hasWild = hasWD4 = false;
        idx7 = idxRev = idxSkip = idxDraw2 = -1;
        has7 = hasRev = hasSkip = hasDraw2 = false;
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (uno.isLegalToPlay(card)) {
                switch (card.content) {
                    case DRAW2:
                        if (!hasDraw2 || card.color == bestColor) {
                            idxDraw2 = i;
                            hasDraw2 = true;
                        } // if (!hasDraw2 || card.color == bestColor)
                        break; // case DRAW2

                    case SKIP:
                        if (!hasSkip || card.color == bestColor) {
                            idxSkip = i;
                            hasSkip = true;
                        } // if (!hasSkip || card.color == bestColor)
                        break; // case SKIP

                    case REV:
                        if (!hasRev || card.color == bestColor) {
                            idxRev = i;
                            hasRev = true;
                        } // if (!hasRev || card.color == bestColor)
                        break; // case REV

                    case WILD:
                        idxWild = i;
                        hasWild = true;
                        break; // case WILD

                    case WILD_DRAW4:
                        idxWD4 = i;
                        hasWD4 = true;
                        break; // case WILD_DRAW4

                    case NUM7:
                        if (!has7 || card.color == bestColor) {
                            idx7 = i;
                            has7 = true;
                        } // if (!has7 || card.color == bestColor)
                        break; // case NUM7

                    case NUM0:
                        if (!has0 || card.color == bestColor) {
                            idx0 = i;
                            has0 = true;
                        } // if (!has0 || card.color == bestColor)
                        break; // case NUM0

                    default: // number cards
                        if (!hasNum || card.color == bestColor) {
                            idxNum = i;
                            hasNum = true;
                        } // if (!hasNum || card.color == bestColor)
                        break; // default
                } // switch (card.content)
            } // if (uno.isLegalToPlay(card))
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        next = uno.getPlayer(uno.getNext());
        nextSize = next.getHandSize();
        nextStrong = next.getStrongColor();
        oppo = uno.getPlayer(uno.getOppo());
        oppoSize = oppo.getHandSize();
        oppoStrong = oppo.getStrongColor();
        prev = uno.getPlayer(uno.getPrev());
        prevSize = prev.getHandSize();
        prevStrong = prev.getStrongColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Firstly consider to use a 7 to steal the UNO, if can't,
            // limit your next player's action as well as you can.
            if (has7 && (yourSize > 2
                    || hand.get(1 - idx7).content != NUM7
                    && hand.get(1 - idx7).content != WILD
                    && hand.get(1 - idx7).content != WILD_DRAW4
                    && hand.get(1 - idx7).color != hand.get(idx7).color))
                idxBest = idx7;
            else if (has0 && (yourSize > 2
                    || hand.get(1 - idx0).content != NUM0
                    && hand.get(1 - idx0).content != WILD
                    && hand.get(1 - idx0).content != WILD_DRAW4
                    && hand.get(1 - idx0).color != hand.get(idx0).color))
                idxBest = idx0;
            else if (hasDraw2)
                idxBest = idxDraw2;
            else if (hasSkip)
                idxBest = idxSkip;
            else if (hasRev)
                idxBest = idxRev;
            else if (hasWD4 && lastColor != bestColor)
                idxBest = idxWD4;
            else if (hasWild && lastColor != bestColor)
                idxBest = idxWild;
            else if (hasNum && hand.get(idxNum).color != nextStrong)
                idxBest = idxNum;
            else if (hasWild && (has7 || has0))
                idxBest = idxWild;
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Consider to use a 0 or 7 to steal the UNO.
            if (has0)
                idxBest = idx0;
            else if (has7)
                idxBest = idx7;
            else if (hasNum)
                idxBest = idxNum;
            else if (hasSkip && hand.get(idxSkip).color != prevStrong)
                idxBest = idxSkip;
            else if (hasDraw2 && hand.get(idxDraw2).color != prevStrong)
                idxBest = idxDraw2;
            else if (hasWild)
                idxBest = idxWild;
            else if (hasWD4)
                idxBest = idxWD4;
        } // else if (prevSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Consider to use a 7 to steal the UNO.
            if (has7)
                idxBest = idx7;
            else if (has0)
                idxBest = idx0;
            else if (hasNum)
                idxBest = idxNum;
            else if (hasRev && prevSize > nextSize)
                idxBest = idxRev;
            else if (hasSkip && hand.get(idxSkip).color != oppoStrong)
                idxBest = idxSkip;
            else if (hasDraw2 && hand.get(idxDraw2).color != oppoStrong)
                idxBest = idxDraw2;
            else if (hasWild)
                idxBest = idxWild;
            else if (hasWD4)
                idxBest = idxWD4;
        } // else if (oppoSize == 1)
        else {
            // Normal strategies
            if (has0 && hand.get(idx0).color == prevStrong)
                idxBest = idx0;
            else if (has7 && (hand.get(idx7).color == prevStrong
                    || hand.get(idx7).color == oppoStrong
                    || hand.get(idx7).color == nextStrong))
                idxBest = idx7;
            else if (hasRev && prevSize > nextSize)
                idxBest = idxRev;
            else if (hasNum)
                idxBest = idxNum;
            else if (hasSkip)
                idxBest = idxSkip;
            else if (hasDraw2)
                idxBest = idxDraw2;
            else if (hasRev)
                idxBest = idxRev;
            else if (has0 && (yourSize > 2
                    || hand.get(1 - idx0).content != NUM0
                    && hand.get(1 - idx0).content != WILD
                    && hand.get(1 - idx0).content != WILD_DRAW4
                    && hand.get(1 - idx0).color != hand.get(idx0).color))
                idxBest = idx0;
            else if (has7)
                idxBest = idx7;
            else if (hasWild)
                idxBest = idxWild;
            else if (hasWD4)
                idxBest = idxWD4;
        } // else

        outColor[0] = bestColor;
        return idxBest;
    } // sevenZeroAI_bestCardIndex4NowPlayer(Card, Color[])
} // AIImpl Class

// E.O.F