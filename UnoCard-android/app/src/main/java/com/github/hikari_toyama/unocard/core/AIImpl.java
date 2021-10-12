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
     * Uno runtime.
     */
    private final Uno uno;

    /**
     * Constructor.
     */
    AIImpl(Context context) {
        uno = Uno.getInstance(context);
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
    private Color calcBestColor4NowPlayer() {
        Color best = RED;
        int[] score = {0, 0, 0, 0, 0};
        Player curr = uno.getPlayer(uno.getNow());

        for (Card card : curr.getHandCards()) {
            switch (card.content) {
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

        // default to red, when only wild cards in hand,
        // function will return Color.RED
        if (score[BLUE.ordinal()] > score[best.ordinal()]) {
            best = BLUE;
        } // if (score[BLUE.ordinal()] > score[best.ordinal()])

        if (score[GREEN.ordinal()] > score[best.ordinal()]) {
            best = GREEN;
        } // if (score[GREEN.ordinal()] > score[best.ordinal()])

        if (score[YELLOW.ordinal()] > score[best.ordinal()]) {
            best = YELLOW;
        } // if (score[YELLOW.ordinal()] > score[best.ordinal()])

        return best;
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
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass null. If drew a card from deck, then
     *                  you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let us pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @Override
    public int easyAI_bestCardIndex4NowPlayer(Card drawnCard,
                                              Color[] outColor) {
        Card card;
        boolean legal;
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
            if (drawnCard == null) {
                legal = uno.isLegalToPlay(card);
            } // if (drawnCard == null)
            else {
                legal = card == drawnCard;
            } // else

            if (legal) {
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
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        nextSize = uno.getPlayer(uno.getNext()).getHandSize();
        prevSize = uno.getPlayer(uno.getPrev()).getHandSize();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2) {
                idxBest = idxDraw2;
            } // if (hasDraw2)
            else if (hasSkip) {
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasRev) {
                idxBest = idxRev;
            } // else if (hasRev)
            else if (hasWD4 && lastColor != bestColor) {
                idxBest = idxWD4;
            } // else if (hasWD4 && lastColor != bestColor)
            else if (hasWild && lastColor != bestColor) {
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
        } // if (nextSize == 1)
        else {
            // Normal strategies
            if (hasRev && prevSize > nextSize) {
                idxBest = idxRev;
            } // if (hasRev && prevSize > nextSize)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasSkip) {
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasDraw2) {
                idxBest = idxDraw2;
            } // else if (hasDraw2)
            else if (hasRev && prevSize > 1) {
                idxBest = idxRev;
            } // else if (hasRev && prevSize > 1)
            else if (hasWild) {
                idxBest = idxWild;
            } // else if (hasWild)
            else if (hasWD4) {
                idxBest = idxWD4;
            } // else if (hasWD4)
        } // else

        outColor[0] = bestColor;
        return idxBest;
    } // easyAI_bestCardIndex4NowPlayer(Card, Color[])

    /**
     * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
     * and calculate which is the best card to play.
     *
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass null. If drew a card from deck, then
     *                  you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let us pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @Override
    @SuppressWarnings("CStyleArrayDeclaration")
    public int hardAI_bestCardIndex4NowPlayer(Card drawnCard,
                                              Color[] outColor) {
        Card card;
        String errMsg;
        int i, idxBest;
        List<Card> hand;
        boolean legal, allWild;
        Player curr, next, oppo, prev;
        Color bestColor, lastColor, safeColor;
        int yourSize, nextSize, oppoSize, prevSize;
        Color nextWeakColor, oppoWeakColor, prevWeakColor;
        Color nextStrongColor, oppoStrongColor, prevStrongColor;
        int idxNumIn[], idxRev, idxSkip, idxDraw2, idxWild, idxWD4;
        boolean hasNumIn[], hasRev, hasSkip, hasDraw2, hasWild, hasWD4;

        if (outColor == null || outColor.length == 0) {
            errMsg = "outColor cannot be null or Color[0]";
            throw new IllegalArgumentException(errMsg);
        }  // if (outColor == null || outColor.length == 0)

        curr = uno.getPlayer(uno.getNow());
        hand = curr.getHandCards();
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
            if (drawnCard == null) {
                legal = uno.isLegalToPlay(card);
            } // if (drawnCard == null)
            else {
                legal = card == drawnCard;
            } // else

            if (legal) {
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
                        if (!hasNumIn[card.color.ordinal()]) {
                            idxNumIn[card.color.ordinal()] = i;
                            hasNumIn[card.color.ordinal()] = true;
                        } // if (!hasNumIn[card.color.ordinal()])
                        break; // default
                } // switch (card.content)
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        next = uno.getPlayer(uno.getNext());
        nextSize = next.getHandSize();
        nextWeakColor = next.getWeakColor();
        nextStrongColor = next.getStrongColor();
        oppo = uno.getPlayer(uno.getOppo());
        oppoSize = oppo.getHandSize();
        oppoWeakColor = oppo.getWeakColor();
        oppoStrongColor = oppo.getStrongColor();
        prev = uno.getPlayer(uno.getPrev());
        prevSize = prev.getHandSize();
        prevWeakColor = prev.getWeakColor();
        prevStrongColor = prev.getStrongColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2) {
                // Play a [+2] to make your next player draw two cards!
                outColor[0] = bestColor;
                return idxDraw2;
            } // if (hasDraw2)

            // Now calculate your safe color, which can make your
            // next player NOT to win the game.
            if ((nextStrongColor != NONE && nextStrongColor != bestColor)
                    || nextWeakColor == NONE) {
                safeColor = bestColor;
            } // if (nextStrongColor != NONE && ...)
            else {
                safeColor = nextWeakColor;
            } // else

            while (safeColor == nextStrongColor
                    || (oppoSize == 1 && safeColor == oppoStrongColor)
                    || (prevSize == 1 && safeColor == prevStrongColor)) {
                // Determine your best color in dangerous cases. Firstly
                // choose next player's weak color, but be careful of the
                // conflict with other opponents' strong colors!
                safeColor = Color.values()[RND.nextInt(4) + 1];
            } // while (safeColor == nextStrongColor || ...)

            if (lastColor == nextStrongColor) {
                // Your next player played a wild card, started an UNO dash
                // in its last action, and what's worse is that the legal color
                // has not been changed yet. You have to change the following
                // legal color, or you will approximately 100% lose this game.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != RED) &&
                        (oppoSize > 1 || oppoStrongColor != RED) &&
                        nextStrongColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != BLUE) &&
                        (oppoSize > 1 || oppoStrongColor != BLUE) &&
                        nextStrongColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != GREEN) &&
                        (oppoSize > 1 || oppoStrongColor != GREEN) &&
                        nextStrongColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != YELLOW) &&
                        (oppoSize > 1 || oppoStrongColor != YELLOW) &&
                        nextStrongColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasSkip) {
                    // Play a [skip] to skip its turn and wait for more chances.
                    idxBest = idxSkip;
                } // else if (hasSkip)
                else if (hasWD4) {
                    // Now start to use wild cards.
                    bestColor = safeColor;
                    idxBest = idxWD4;
                } // else if (hasWD4)
                else if (hasWild) {
                    bestColor = safeColor;
                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasRev) {
                    // Finally play a [reverse] to get help from other players.
                    // If you even do not have this choice, you lose this game.
                    idxBest = idxRev;
                } // else if (hasRev)
            } // if (lastColor == nextStrongColor)
            else if (nextStrongColor != NONE) {
                // Your next player played a wild card, started an UNO dash
                // in its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the strong color again.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != RED) &&
                        (oppoSize > 1 || oppoStrongColor != RED) &&
                        nextStrongColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != BLUE) &&
                        (oppoSize > 1 || oppoStrongColor != BLUE) &&
                        nextStrongColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != GREEN) &&
                        (oppoSize > 1 || oppoStrongColor != GREEN) &&
                        nextStrongColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        (prevSize > 1 || prevStrongColor != YELLOW) &&
                        (oppoSize > 1 || oppoStrongColor != YELLOW) &&
                        nextStrongColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasRev &&
                        prevSize >= 4 &&
                        hand.get(idxRev).color != nextStrongColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).color != nextStrongColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
            } // else if (nextStrongColor != NONE)
            else {
                // Your next player started an UNO dash without playing a wild
                // card, so use normal defense strategies.
                if (hasSkip) {
                    // Play a [skip] to skip its turn and wait for more chances.
                    idxBest = idxSkip;
                } // if (hasSkip)
                else if (hasWD4 && !hasNumIn[lastColor.ordinal()]) {
                    // Then play a [wild +4] to make your next player draw four
                    // cards (if it's legal to play this card).
                    if (oppoSize == 1 || prevSize == 1) {
                        // Be careful when multiple opponents UNOed.
                        bestColor = safeColor;
                    } // if (oppoSize == 1 || prevSize == 1)

                    idxBest = idxWD4;
                } // else if (hasWD4 && !hasNumIn[lastColor.ordinal()])
                else if (hasRev) {
                    // Play a [reverse] to get help from your opposite player.
                    idxBest = idxRev;
                } // else if (hasRev)
                else if (hasNumIn[safeColor.ordinal()]) {
                    // Play a number card or a wild card to change legal color
                    // to your best as far as you can.
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // else if (hasNumIn[safeColor.ordinal()])
                else if (lastColor != safeColor) {
                    if (hasWild) {
                        bestColor = safeColor;
                        idxBest = idxWild;
                    } // if (hasWild)
                    else if (hasWD4) {
                        bestColor = safeColor;
                        idxBest = idxWD4;
                    } // else if (hasWD4)
                    else if (hasNumIn[RED.ordinal()] &&
                            (prevSize > 1 || prevStrongColor != RED) &&
                            (oppoSize > 1 || oppoStrongColor != RED)) {
                        idxBest = idxNumIn[RED.ordinal()];
                    } // else if (hasNumIn[RED.ordinal()] && ...)
                    else if (hasNumIn[BLUE.ordinal()] &&
                            (prevSize > 1 || prevStrongColor != BLUE) &&
                            (oppoSize > 1 || oppoStrongColor != BLUE)) {
                        idxBest = idxNumIn[BLUE.ordinal()];
                    } // else if (hasNumIn[BLUE.ordinal()] && ...)
                    else if (hasNumIn[GREEN.ordinal()] &&
                            (prevSize > 1 || prevStrongColor != GREEN) &&
                            (oppoSize > 1 || oppoStrongColor != GREEN)) {
                        idxBest = idxNumIn[GREEN.ordinal()];
                    } // else if (hasNumIn[GREEN.ordinal()] && ...)
                    else if (hasNumIn[YELLOW.ordinal()] &&
                            (prevSize > 1 || prevStrongColor != YELLOW) &&
                            (oppoSize > 1 || oppoStrongColor != YELLOW)) {
                        idxBest = idxNumIn[YELLOW.ordinal()];
                    } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                } // else if (lastColor != safeColor)
            } // else
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Save your action cards as much as you can. once a reverse card is
            // played, you can use these cards to limit your previous player's
            // action. Now calculate your safe color, which can make your
            // previous player NOT to win the game.
            if ((prevStrongColor != NONE && prevStrongColor != bestColor)
                    || prevWeakColor == NONE) {
                safeColor = bestColor;
            } // if (prevStrongColor != NONE && ...)
            else {
                safeColor = prevWeakColor;
            } // else

            while (safeColor == prevStrongColor
                    || (oppoSize == 1 && safeColor == oppoStrongColor)) {
                // Determine your best color in dangerous cases. Firstly
                // choose previous player's weak color, but be careful of
                // the conflict with other opponents' strong colors!
                safeColor = Color.values()[RND.nextInt(4) + 1];
            } // while (safeColor == prevStrongColor || ...)

            if (lastColor == prevStrongColor) {
                // Your previous player played a wild card, started an UNO dash
                // in its last action. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasSkip &&
                        hand.get(idxSkip).color != prevStrongColor) {
                    // When your opposite player played a [skip], and you have a
                    // [skip] with different color, play it.
                    idxBest = idxSkip;
                } // if (hasSkip && ...)
                else if (hasWild) {
                    // Now start to use wild cards.
                    bestColor = safeColor;
                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasWD4) {
                    bestColor = safeColor;
                    idxBest = idxWD4;
                } // else if (hasWD4)
                else if (hasNumIn[bestColor.ordinal()]) {
                    // When you have no wild cards, play a number card and try
                    // to get help from other players.
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()])
                else if (hasNumIn[RED.ordinal()]) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()])
                else if (hasNumIn[BLUE.ordinal()]) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()])
                else if (hasNumIn[GREEN.ordinal()]) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()])
                else if (hasNumIn[YELLOW.ordinal()]) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()])
            } // if (lastColor == prevStrongColor)
            else if (prevStrongColor != NONE) {
                // Your previous player played a wild card, started an UNO dash
                // in its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the strong color again.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()] &&
                        (oppoSize > 1 || oppoStrongColor != RED) &&
                        prevStrongColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        (oppoSize > 1 || oppoStrongColor != BLUE) &&
                        prevStrongColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        (oppoSize > 1 || oppoStrongColor != GREEN) &&
                        prevStrongColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        (oppoSize > 1 || oppoStrongColor != YELLOW) &&
                        prevStrongColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
            } // else if (prevStrongColor != NONE)
            else {
                // Your previous player started an UNO dash without playing a
                // wild card, so use normal defense strategies.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasWild && lastColor != safeColor) {
                    bestColor = safeColor;
                    idxBest = idxWild;
                } // else if (hasWild && lastColor != safeColor)
                else if (hasWD4 && lastColor != safeColor) {
                    bestColor = safeColor;
                    idxBest = idxWD4;
                } // else if (hasWD4 && lastColor != safeColor)
                else if (hasNumIn[bestColor.ordinal()]) {
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()])
                else if (hasNumIn[RED.ordinal()]) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()])
                else if (hasNumIn[BLUE.ordinal()]) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()])
                else if (hasNumIn[GREEN.ordinal()]) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()])
                else if (hasNumIn[YELLOW.ordinal()]) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()])
            } // else
        } // else if (prevSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Give more freedom to your next player, the only one that can
            // directly limit your opposite player's action. Now calculate
            // your safe color, which can make your opposite player NOT to
            // win the game.
            if ((oppoStrongColor != NONE && oppoStrongColor != bestColor)
                    || oppoWeakColor == NONE) {
                safeColor = bestColor;
            } // if (oppoStrongColor != NONE && ...)
            else {
                safeColor = oppoWeakColor;
            } // else

            while (safeColor == oppoStrongColor) {
                // Determine your best color in dangerous cases. Firstly
                // choose opposite player's weak color, but be careful of
                // the conflict with other opponents' strong colors!
                safeColor = Color.values()[RND.nextInt(4) + 1];
            } // while (safeColor == oppoStrongColor)

            if (lastColor == oppoStrongColor) {
                // Your opposite player played a wild card, started an UNO dash
                // in its last action, and what's worse is that the legal color
                // has not been changed yet. You have to change the following
                // legal color, or you will approximately 100% lose this game.
                if (hasNumIn[safeColor.ordinal()]) {
                    // At first, try to change legal color by playing an action
                    // card or a number card, instead of using wild cards.
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[bestColor.ordinal()]
                        && oppoStrongColor != bestColor) {
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()] && ...)
                else if (hasNumIn[RED.ordinal()]
                        && oppoStrongColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()]
                        && oppoStrongColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()]
                        && oppoStrongColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()]
                        && oppoStrongColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasRev &&
                        hand.get(idxRev).color != oppoStrongColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).color != oppoStrongColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 &&
                        hand.get(idxDraw2).color != oppoStrongColor) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2 && ...)
                else if (hasWild) {
                    // Now start to use your wild cards.
                    bestColor = safeColor;
                    idxBest = idxWild;
                } // else if (hasWild)
                else if (hasWD4) {
                    bestColor = safeColor;
                    idxBest = idxWD4;
                } // else if (hasWD4)
                else if (hasRev && prevSize - nextSize >= 3) {
                    // Finally try to get help from other players.
                    idxBest = idxRev;
                } // else if (hasRev && prevSize - nextSize >= 3)
                else if (hasNumIn[bestColor.ordinal()]) {
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()])
                else if (hasNumIn[RED.ordinal()]) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()])
                else if (hasNumIn[BLUE.ordinal()]) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()])
                else if (hasNumIn[GREEN.ordinal()]) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()])
                else if (hasNumIn[YELLOW.ordinal()]) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()])
            } // if (lastColor == oppoStrongColor)
            else if (oppoStrongColor != NONE) {
                // Your opposite player played a wild card, started an UNO dash
                // in its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the strong color again.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[bestColor.ordinal()]
                        && oppoStrongColor != bestColor) {
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()] && ...)
                else if (hasNumIn[RED.ordinal()]
                        && oppoStrongColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()]
                        && oppoStrongColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()]
                        && oppoStrongColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()]
                        && oppoStrongColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasRev &&
                        hand.get(idxRev).color != oppoStrongColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        nextSize <= 4 &&
                        hand.get(idxSkip).color != oppoStrongColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 &&
                        nextSize <= 4 &&
                        hand.get(idxDraw2).color != oppoStrongColor) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2 && ...)
            } // else if (oppoStrongColor != NONE)
            else {
                // Your opposite player started an UNO dash without playing a
                // wild card, so use normal defense strategies.
                if (hasRev && prevSize - nextSize >= 3) {
                    // Firstly play a [reverse] when your next player remains
                    // only a few cards but your previous player remains a lot
                    // of cards, because it seems that your previous player has
                    // more possibility to limit your opposite player's action.
                    idxBest = idxRev;
                } // if (hasRev && prevSize - nextSize >= 3)
                else if (hasNumIn[safeColor.ordinal()]) {
                    // Then you can play a number card.
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // else if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()]) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()])
                else if (hasNumIn[BLUE.ordinal()]) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()])
                else if (hasNumIn[GREEN.ordinal()]) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()])
                else if (hasNumIn[YELLOW.ordinal()]) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()])
                else if (hasWild && lastColor != safeColor) {
                    // When you have no more legal number/reverse cards to play,
                    // try to play a wild card and change the legal color to
                    // your best. Do not play any [+2]/[skip] to your next
                    // player!
                    bestColor = safeColor;
                    idxBest = idxWild;
                } // else if (hasWild && lastColor != safeColor)
                else if (hasWD4 && lastColor != safeColor && nextSize <= 4) {
                    // When you have no more legal number/reverse cards to play,
                    // try to play a wild card and change the legal color to
                    // your best. Specially, for [wild +4] cards, you can only
                    // play it when your next player remains only a few cards,
                    // and what you did can help your next player find more
                    // useful cards, such as action cards, or [wild +4] cards.
                    bestColor = safeColor;
                    idxBest = idxWD4;
                } // else if (hasWD4 && lastColor != safeColor && nextSize <= 4)
            } // else
        } // else if (oppoSize == 1)
        else if (allWild) {
            // Strategies when you remain only wild cards.
            // Set the following legal color to one of your opponents' weak
            // colors, in order to prevent them from playing a [+2] to you.
            if (prevWeakColor != NONE) {
                bestColor = prevWeakColor;
            } // if (prevWeakColor != NONE)
            else if (oppoWeakColor != NONE) {
                bestColor = oppoWeakColor;
            } // else if (oppoWeakColor != NONE)
            else if (nextWeakColor != NONE) {
                bestColor = nextWeakColor;
            } // else if (nextWeakColor != NONE)
            else {
                while (bestColor == prevStrongColor
                        || bestColor == oppoStrongColor
                        || bestColor == nextStrongColor) {
                    bestColor = Color.values()[RND.nextInt(4) + 1];
                } // while (bestColor == prevStrongColor || ...)
            } // else

            // When your next player remains only a few cards, use [Wild +4]
            // cards at first. Otherwise, use [Wild] cards at first.
            if (nextSize <= 4) {
                idxBest = hasWD4 ? idxWD4 : idxWild;
            } // if (nextSize <= 4)
            else {
                idxBest = hasWild ? idxWild : idxWD4;
            } // else
        } // else if (allWild)
        else if (lastColor == nextWeakColor && yourSize > 2) {
            // Strategies when your next player drew a card in its last action.
            // Unless keeping or changing to your best color, you do not need to
            // play your limitation/wild cards. Use them in more dangerous cases.
            if (hasRev && prevSize - nextSize >= 3) {
                idxBest = idxRev;
            } // if (hasRev && prevSize - nextSize >= 3)
            else if (hasNumIn[nextWeakColor.ordinal()]) {
                idxBest = idxNumIn[nextWeakColor.ordinal()];
            } // else if (hasNumIn[nextWeakColor.ordinal()])
            else if (hasNumIn[bestColor.ordinal()]) {
                idxBest = idxNumIn[bestColor.ordinal()];
            } // else if (hasNumIn[bestColor.ordinal()])
            else if (hasNumIn[RED.ordinal()]) {
                idxBest = idxNumIn[RED.ordinal()];
            } // else if (hasNumIn[RED.ordinal()])
            else if (hasNumIn[BLUE.ordinal()]) {
                idxBest = idxNumIn[BLUE.ordinal()];
            } // else if (hasNumIn[BLUE.ordinal()])
            else if (hasNumIn[GREEN.ordinal()]) {
                idxBest = idxNumIn[GREEN.ordinal()];
            } // else if (hasNumIn[GREEN.ordinal()])
            else if (hasNumIn[YELLOW.ordinal()]) {
                idxBest = idxNumIn[YELLOW.ordinal()];
            } // else if (hasNumIn[YELLOW.ordinal()])
            else if (hasRev &&
                    (prevSize >= 4 || prev.getRecent() == null)) {
                idxBest = idxRev;
            } // else if (hasRev && ...)
            else if (hasSkip &&
                    oppoSize >= 3 &&
                    hand.get(idxSkip).color == bestColor) {
                idxBest = idxSkip;
            } // else if (hasSkip && ...)
            else if (hasDraw2 &&
                    oppoSize >= 3 &&
                    hand.get(idxDraw2).color == bestColor) {
                idxBest = idxDraw2;
            } // else if (hasDraw2 && ...)
        } // else if (lastColor == nextWeakColor && yourSize > 2)
        else {
            // Normal strategies
            if (hasDraw2 && nextSize <= 4 && nextSize - oppoSize <= 1) {
                // Play a [+2] when your next player remains only a few cards.
                idxBest = idxDraw2;
            } // if (hasDraw2 && nextSize <= 4 && nextSize - oppoSize <= 1)
            else if (hasSkip && nextSize <= 4 && nextSize - oppoSize <= 1) {
                // Play a [skip] when your next player remains only a few cards.
                idxBest = idxSkip;
            } // else if (hasSkip && nextSize <= 4 && nextSize - oppoSize <= 1)
            else if (hasRev &&
                    (prevSize - nextSize >= 3 || prev.getRecent() == null)) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards, in
                // order to balance everyone's hand-card amount.  Also, when
                // your previous player drew a card in its last action, you
                // can play a reverse card too. What you did probably makes
                // it draw a card again.
                idxBest = idxRev;
            } // else if (hasRev && ...)
            else if (hasNumIn[bestColor.ordinal()]) {
                // Play number cards.
                idxBest = idxNumIn[bestColor.ordinal()];
            } // else if (hasNumIn[bestColor.ordinal()])
            else if (hasNumIn[RED.ordinal()]) {
                idxBest = idxNumIn[RED.ordinal()];
            } // else if (hasNumIn[RED.ordinal()])
            else if (hasNumIn[BLUE.ordinal()]) {
                idxBest = idxNumIn[BLUE.ordinal()];
            } // else if (hasNumIn[BLUE.ordinal()])
            else if (hasNumIn[GREEN.ordinal()]) {
                idxBest = idxNumIn[GREEN.ordinal()];
            } // else if (hasNumIn[GREEN.ordinal()])
            else if (hasNumIn[YELLOW.ordinal()]) {
                idxBest = idxNumIn[YELLOW.ordinal()];
            } // else if (hasNumIn[YELLOW.ordinal()])
            else if (hasRev && prevSize >= 4) {
                // When you have no more legal number cards to play, you can
                // play a [reverse] safely when your previous player still has
                // a number of cards.
                idxBest = idxRev;
            } // else if (hasRev && prevSize >= 4)
            else if (hasSkip &&
                    oppoSize >= 3 &&
                    hand.get(idxSkip).color == bestColor) {
                // Unless keeping or changing to your best color, you do not
                // need to play your limitation/wild cards when your next player
                // still has a number of cards. Use them in more dangerous cases.
                idxBest = idxSkip;
            } // else if (hasSkip && ...)
            else if (hasDraw2 &&
                    oppoSize >= 3 &&
                    hand.get(idxDraw2).color == bestColor) {
                idxBest = idxDraw2;
            } // else if (hasDraw2 && ...)
            else if (hasWild && nextSize <= 4) {
                // When your next player remains only a few cards, and you have
                // no more legal number/action cards to play, try to play a
                // wild card and change the legal color to your best color.
                idxBest = idxWild;
            } // else if (hasWild && nextSize <= 4)
            else if (hasWD4 && nextSize <= 4) {
                // When your next player remains only a few cards, and you have
                // no more legal number/action cards to play, try to play a
                // wild card and change the legal color to your best color.
                idxBest = idxWD4;
            } // else if (hasWD4 && nextSize <= 4)
            else if (hasWild && yourSize == 2 && prevSize <= 3) {
                // When you remain only 2 cards, including a wild card, and your
                // previous player seems no enough power to limit you (has too
                // few cards), start your UNO dash!
                idxBest = idxWild;
            } // else if (hasWild && yourSize == 2 && prevSize <= 3)
            else if (hasWD4 && yourSize == 2 && prevSize <= 3) {
                idxBest = idxWD4;
            } // else if (hasWD4 && yourSize == 2 && prevSize <= 3)
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
                else if (hasWD4) {
                    idxBest = idxWD4;
                } // else if (hasWD4)
            } // else if (yourSize == Uno.MAX_HOLD_CARDS)
        } // else

        outColor[0] = bestColor;
        return idxBest;
    } // hardAI_bestCardIndex4NowPlayer(Card, Color[])

    /**
     * AI Strategies in 7-0 special rule. Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass null. If drew a card from deck, then
     *                  you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let us pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @Override
    public int sevenZeroAI_bestCardIndex4NowPlayer(Card drawnCard,
                                                   Color[] outColor) {
        Card card;
        boolean legal;
        String errMsg;
        int i, idxBest;
        List<Card> hand;
        Player next, oppo, prev;
        Color bestColor, lastColor;
        int idx0, idxNum, idxWild, idxWD4;
        int idx7, idxRev, idxSkip, idxDraw2;
        boolean has0, hasNum, hasWild, hasWD4;
        boolean has7, hasRev, hasSkip, hasDraw2;
        int yourSize, nextSize, oppoSize, prevSize;
        Color nextStrongColor, oppoStrongColor, prevStrongColor;

        if (outColor == null) {
            errMsg = "outColor[] cannot be nullptr";
            throw new IllegalArgumentException(errMsg);
        } // if (outColor == null)

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
            if (drawnCard == null) {
                legal = uno.isLegalToPlay(card);
            } // if (drawnCard == null)
            else {
                legal = card == drawnCard;
            } // else

            if (legal) {
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
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        next = uno.getPlayer(uno.getNext());
        nextSize = next.getHandSize();
        nextStrongColor = next.getStrongColor();
        oppo = uno.getPlayer(uno.getOppo());
        oppoSize = oppo.getHandSize();
        oppoStrongColor = oppo.getStrongColor();
        prev = uno.getPlayer(uno.getPrev());
        prevSize = prev.getHandSize();
        prevStrongColor = prev.getStrongColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Firstly consider to use a 7 to steal the UNO, if can't,
            // limit your next player's action as well as you can.
            if (has7 && (yourSize > 2
                    || hand.get(1 - idx7).content != Content.NUM7
                    && hand.get(1 - idx7).content != Content.WILD
                    && hand.get(1 - idx7).content != Content.WILD_DRAW4
                    && hand.get(1 - idx7).color != hand.get(idx7).color)) {
                idxBest = idx7;
            } // if (has7 && ...)
            else if (has0 && (yourSize > 2
                    || hand.get(1 - idx0).content != Content.NUM0
                    && hand.get(1 - idx0).content != Content.WILD
                    && hand.get(1 - idx0).content != Content.WILD_DRAW4
                    && hand.get(1 - idx0).color != hand.get(idx0).color)) {
                idxBest = idx0;
            } // else if (has0 && ...)
            else if (hasDraw2) {
                idxBest = idxDraw2;
            } // else if (hasDraw2)
            else if (hasSkip) {
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasRev) {
                idxBest = idxRev;
            } // else if (hasRev)
            else if (hasWD4 && lastColor != bestColor) {
                idxBest = idxWD4;
            } // else if (hasWD4 && lastColor != bestColor)
            else if (hasWild && lastColor != bestColor) {
                idxBest = idxWild;
            } // else if (hasWild && lastColor != bestColor)
            else if (hasNum &&
                    hand.get(idxNum).color != nextStrongColor) {
                idxBest = idxNum;
            } // else if (hasNum && ...)
            else if (hasWild && (has7 || has0)) {
                idxBest = idxWild;
            } // else if (hasWild && (has7 || has0))
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Consider to use a 0 or 7 to steal the UNO.
            if (has0) {
                idxBest = idx0;
            } // if (has0)
            else if (has7) {
                idxBest = idx7;
            } // else if (has7)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasSkip &&
                    hand.get(idxSkip).color != prevStrongColor) {
                idxBest = idxSkip;
            } // else if (hasSkip && ...)
            else if (hasDraw2 &&
                    hand.get(idxDraw2).color != prevStrongColor) {
                idxBest = idxDraw2;
            } // else if (hasDraw2 && ...)
            else if (hasWild && bestColor != prevStrongColor) {
                idxBest = idxWild;
            } // else if (hasWild && bestColor != prevStrongColor)
            else if (hasWD4 && bestColor != prevStrongColor) {
                idxBest = idxWD4;
            } // else if (hasWD4 && bestColor != prevStrongColor)
        } // else if (prevSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Consider to use a 7 to steal the UNO.
            if (has7) {
                idxBest = idx7;
            } // if (has7)
            else if (has0) {
                idxBest = idx0;
            } // else if (has0)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasRev && prevSize > nextSize) {
                idxBest = idxRev;
            } // else if (hasRev && prevSize > nextSize)
            else if (hasSkip &&
                    hand.get(idxSkip).color != oppoStrongColor) {
                idxBest = idxSkip;
            } // else if (hasSkip && ...)
            else if (hasDraw2 &&
                    hand.get(idxDraw2).color != oppoStrongColor) {
                idxBest = idxDraw2;
            } // else if (hasDraw2 && ...)
            else if (hasWild && bestColor != prevStrongColor) {
                idxBest = idxWild;
            } // else if (hasWild && bestColor != prevStrongColor)
            else if (hasWD4 && bestColor != prevStrongColor) {
                idxBest = idxWD4;
            } // else if (hasWD4 && bestColor != prevStrongColor)
        } // else if (oppoSize == 1)
        else {
            // Normal strategies
            if (has0 && hand.get(idx0).color == prevStrongColor) {
                idxBest = idx0;
            } // if (has0 && hand.at(idx0).color == nextStrongColor)
            else if (has7 && (hand.get(idx7).color == prevStrongColor
                    || hand.get(idx7).color == oppoStrongColor
                    || hand.get(idx7).color == nextStrongColor)) {
                idxBest = idx7;
            } // else if (has7 && ...)
            else if (hasRev && prevSize > nextSize) {
                idxBest = idxRev;
            } // else if (hasRev && prevSize > nextSize)
            else if (hasNum) {
                idxBest = idxNum;
            } // else if (hasNum)
            else if (hasSkip) {
                idxBest = idxSkip;
            } // else if (hasSkip)
            else if (hasDraw2) {
                idxBest = idxDraw2;
            } // else if (hasDraw2)
            else if (hasRev) {
                idxBest = idxRev;
            } // else if (hasRev)
            else if (has0 && (yourSize > 2
                    || hand.get(1 - idx0).content != Content.NUM0
                    && hand.get(1 - idx0).content != Content.WILD
                    && hand.get(1 - idx0).content != Content.WILD_DRAW4
                    && hand.get(1 - idx0).color != hand.get(idx0).color)) {
                idxBest = idx0;
            } // else if (has0 && ...)
            else if (has7) {
                idxBest = idx7;
            } // else if (has7)
            else if (hasWild) {
                idxBest = idxWild;
            } // else if (hasWild)
            else if (hasWD4) {
                idxBest = idxWD4;
            } // else if (hasWD4)
        } // else

        outColor[0] = bestColor;
        return idxBest;
    } // sevenZeroAI_bestCardIndex4NowPlayer(Card, Color[])
} // AIImpl Class

// E.O.F