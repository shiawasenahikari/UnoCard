////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import android.content.Context;

import java.util.List;
import java.util.Random;

import static com.github.hikari_toyama.unocard.core.Color.BLUE;
import static com.github.hikari_toyama.unocard.core.Color.GREEN;
import static com.github.hikari_toyama.unocard.core.Color.NONE;
import static com.github.hikari_toyama.unocard.core.Color.RED;
import static com.github.hikari_toyama.unocard.core.Color.YELLOW;

/**
 * AI Strategies.
 */
public class AI {
    /**
     * Random number generator.
     */
    private Random mRnd;

    /**
     * Uno runtime singleton.
     */
    private Uno mUno;

    /**
     * Constructor.
     */
    public AI(Context context) {
        mRnd = new Random();
        mUno = Uno.getInstance(context);
    } // AI() (Class Constructor)

    /**
     * AI strategies of determining if it's necessary to challenge previous
     * player's [wild +4] card's legality.
     *
     * @param challenger Who challenges. Must be one of the following values:
     *                   Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @return Whether the challenger needs to make a challenge.
     */
    public boolean needToChallenge(int challenger) {
        Card next2last;
        Color draw4Color;
        boolean challenge;
        Color colorBeforeDraw4;
        List<Card> hand, recent;

        hand = mUno.getPlayer(challenger).getHandCards();
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

        return challenge;
    } // needToChallenge()

    /**
     * AI Strategies (Difficulty: EASY).
     *
     * @param whom      Analyze whose hand cards. Must be one of the following:
     *                  Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param drawnCard When the specified player drew a card in its turn just
     *                  now, pass the drawn card. If not, pass null. If drew a
     *                  card from deck, then you can play only the drawn card,
     *                  but not the other cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let we pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in the specified player's hand.
     * Or a negative number that means no appropriate card to play.
     * @deprecated Use easyAI_bestCardIndex4NowPlayer(Card, Color[]) instead.
     */
    @Deprecated
    public int easyAI_bestCardIndexFor(int whom,
                                       Card drawnCard,
                                       Color[] outColor) {
        if (whom == mUno.getNow()) {
            return easyAI_bestCardIndex4NowPlayer(drawnCard, outColor);
        } // if (whom == mUno.getNow())
        else {
            throw new IllegalStateException("DO NOT CALL DEPRECATED API!");
        } // else
    } // easyAI_bestCardIndexFor()

    /**
     * AI Strategies (Difficulty: EASY). Analyze current player's hand cards,
     * and calculate which is the best card to play out.
     *
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass nullptr. If drew a card from deck,
     *                  then you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let we pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    public int easyAI_bestCardIndex4NowPlayer(Card drawnCard,
                                              Color[] outColor) {
        int i;
        Card card;
        boolean legal;
        String errMsg;
        Color bestColor;
        List<Card> hand;
        Player curr, next, prev;
        int yourSize, nextSize, prevSize;
        boolean hasNum, hasRev, hasSkip, hasDraw2, hasWild, hasWD4;
        int idxBest, idxNum, idxRev, idxSkip, idxDraw2, idxWild, idxWD4;

        if (outColor == null || outColor.length == 0) {
            errMsg = "outColor cannot be null or Color[0]";
            throw new IllegalArgumentException(errMsg);
        }  // if (outColor == null || outColor.length == 0)

        curr = mUno.getPlayer(mUno.getNow());
        hand = curr.getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            outColor[0] = card.getRealColor();
            return mUno.isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        next = mUno.getPlayer(mUno.getNext());
        nextSize = next.getHandCards().size();
        prev = mUno.getPlayer(mUno.getPrev());
        prevSize = prev.getHandCards().size();
        hasNum = hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
        idxBest = idxNum = idxRev = idxSkip = idxDraw2 = idxWild = idxWD4 = -1;
        bestColor = curr.calcBestColor();
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            if (drawnCard == null) {
                legal = mUno.isLegalToPlay(card);
            } // if (drawnCard == null)
            else {
                legal = card == drawnCard;
            } // else

            if (legal) {
                switch (card.getContent()) {
                    case DRAW2:
                        if (!hasDraw2 || card.getRealColor() == bestColor) {
                            idxDraw2 = i;
                            hasDraw2 = true;
                        } // if (!hasDraw2 || ...)
                        break; // case DRAW2

                    case SKIP:
                        if (!hasSkip || card.getRealColor() == bestColor) {
                            idxSkip = i;
                            hasSkip = true;
                        } // if (!hasSkip || ...)
                        break; // case SKIP

                    case REV:
                        if (!hasRev || card.getRealColor() == bestColor) {
                            idxRev = i;
                            hasRev = true;
                        } // if (!hasRev || ...)
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
                        if (!hasNum || card.getRealColor() == bestColor) {
                            idxNum = i;
                            hasNum = true;
                        } // if (!hasNum || ...)
                        break; // default
                } // switch (card.getContent())
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        if (hasDraw2) {
            idxBest = idxDraw2;
        } // if (hasDraw2)
        else if (hasSkip) {
            idxBest = idxSkip;
        } // else if (hasSkip)
        else if (hasRev && (prevSize > nextSize || prevSize >= 4)) {
            idxBest = idxRev;
        } // else if (hasRev && (prevSize > nextSize || prevSize >= 4))
        else if (hasNum) {
            idxBest = idxNum;
        } // else if (hasNum)
        else if (hasWild) {
            idxBest = idxWild;
        } // else if (hasWild)
        else if (hasWD4) {
            idxBest = idxWD4;
        } // else if (hasWD4)

        outColor[0] = bestColor;
        return idxBest;
    } // easyAI_bestCardIndex4NowPlayer()

    /**
     * AI Strategies (Difficulty: HARD).
     *
     * @param whom      Analyze whose hand cards. Must be one of the following:
     *                  Player.YOU, Player.COM1, Player.COM2, Player.COM3.
     * @param drawnCard When the specified player drew a card in its turn just
     *                  now, pass the drawn card. If not, pass null. If drew a
     *                  card from deck, then you can play only the drawn card,
     *                  but not the other cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let we pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in the specified player's hand.
     * Or a negative number that means no appropriate card to play.
     * @deprecated Use hardAI_bestCardIndex4NowPlayer(Card, Color[]) instead.
     */
    @Deprecated
    public int hardAI_bestCardIndexFor(int whom,
                                       Card drawnCard,
                                       Color[] outColor) {
        if (whom == mUno.getNow()) {
            return hardAI_bestCardIndex4NowPlayer(drawnCard, outColor);
        } // if (whom == mUno.getNow())
        else {
            throw new IllegalStateException("DO NOT CALL DEPRECATED API!");
        } // else
    } // hardAI_bestCardIndexFor()

    /**
     * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
     * and calculate which is the best card to play.
     *
     * @param drawnCard When current player drew a card just now, pass the drawn
     *                  card. If not, pass nullptr. If drew a card from deck,
     *                  then you can play only the drawn card, but not the other
     *                  cards in your hand, immediately.
     * @param outColor  This is a out parameter. Pass a Color array (length>=1)
     *                  in order to let we pass the return value by assigning
     *                  outColor[0]. When the best card to play becomes a wild
     *                  card, outColor[0] will become the following legal color
     *                  to change. When the best card to play becomes an action
     *                  or a number card, outColor[0] will become the player's
     *                  best color.
     * @return Index of the best card to play, in current player's hand.
     * Or a negative number that means no appropriate card to play.
     */
    @SuppressWarnings("CStyleArrayDeclaration")
    public int hardAI_bestCardIndex4NowPlayer(Card drawnCard,
                                              Color[] outColor) {
        int i;
        String errMsg;
        Card card, last;
        boolean legal, allWild;
        List<Card> hand, recent;
        Player curr, next, oppo, prev;
        Color bestColor, lastColor, safeColor;
        int yourSize, nextSize, oppoSize, prevSize;
        Color nextSafeColor, oppoSafeColor, prevSafeColor;
        Color nextDangerColor, oppoDangerColor, prevDangerColor;
        boolean hasNumIn[], hasRev, hasSkip, hasDraw2, hasWild, hasWD4;
        int idxBest, idxNumIn[], idxRev, idxSkip, idxDraw2, idxWild, idxWD4;

        if (outColor == null || outColor.length == 0) {
            errMsg = "outColor cannot be null or Color[0]";
            throw new IllegalArgumentException(errMsg);
        }  // if (outColor == null || outColor.length == 0)

        curr = mUno.getPlayer(mUno.getNow());
        hand = curr.getHandCards();
        yourSize = hand.size();
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.get(0);
            outColor[0] = card.getRealColor();
            return mUno.isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        next = mUno.getPlayer(mUno.getNext());
        nextSize = next.getHandCards().size();
        oppo = mUno.getPlayer(mUno.getOppo());
        oppoSize = oppo.getHandCards().size();
        prev = mUno.getPlayer(mUno.getPrev());
        prevSize = prev.getHandCards().size();
        hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
        idxBest = idxRev = idxSkip = idxDraw2 = idxWild = idxWD4 = -1;
        hasNumIn = new boolean[]{false, false, false, false, false};
        idxNumIn = new int[]{-1, -1, -1, -1, -1};
        bestColor = curr.calcBestColor();
        recent = mUno.getRecent();
        last = recent.get(recent.size() - 1);
        lastColor = last.getRealColor();
        allWild = true;
        for (i = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.get(i);
            allWild = allWild && card.isWild();
            if (drawnCard == null) {
                legal = mUno.isLegalToPlay(card);
            } // if (drawnCard == null)
            else {
                legal = card == drawnCard;
            } // else

            if (legal) {
                switch (card.getContent()) {
                    case DRAW2:
                        if (!hasDraw2 || card.getRealColor() == bestColor) {
                            idxDraw2 = i;
                            hasDraw2 = true;
                        } // if (!hasDraw2 || ...)
                        break; // case DRAW2

                    case SKIP:
                        if (!hasSkip || card.getRealColor() == bestColor) {
                            idxSkip = i;
                            hasSkip = true;
                        } // if (!hasSkip || ...)
                        break; // case SKIP

                    case REV:
                        if (!hasRev || card.getRealColor() == bestColor) {
                            idxRev = i;
                            hasRev = true;
                        } // if (!hasRev || ...)
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
                        if (!hasNumIn[card.getRealColor().ordinal()]) {
                            idxNumIn[card.getRealColor().ordinal()] = i;
                            hasNumIn[card.getRealColor().ordinal()] = true;
                        } // if (!hasNumIn[card.getRealColor().ordinal()])
                        break; // default
                } // switch (card.getContent())
            } // if (legal)
        } // for (i = 0; i < yourSize; ++i)

        // Decision tree
        nextSafeColor = next.getSafeColor();
        oppoSafeColor = oppo.getSafeColor();
        prevSafeColor = prev.getSafeColor();
        nextDangerColor = next.getDangerousColor();
        oppoDangerColor = oppo.getDangerousColor();
        prevDangerColor = prev.getDangerousColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (nextSafeColor == NONE ||
                    nextDangerColor != NONE && nextDangerColor != bestColor) {
                safeColor = bestColor;
            } // if (nextSafeColor == NONE || ...)
            else {
                safeColor = nextSafeColor;
            } // else

            while (safeColor == nextDangerColor
                    || oppoSize == 1 && safeColor == oppoDangerColor
                    || prevSize == 1 && safeColor == prevDangerColor) {
                // Determine your best color in dangerous cases. Firstly
                // choose next player's safe color, but be careful of the
                // conflict with other opponents' dangerous colors!
                safeColor = Color.values()[mRnd.nextInt(4) + 1];
            } // while (safeColor == nextDangerColor || ...)

            if (hasDraw2) {
                // Play a [+2] to make your next player draw two cards!
                idxBest = idxDraw2;
            } // if (hasDraw2)
            else if (lastColor == nextDangerColor) {
                // Your next player played a wild card, started an UNO dash in
                // its last action. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != RED) &&
                        (oppoSize > 1 || oppoDangerColor != RED) &&
                        nextDangerColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != BLUE) &&
                        (oppoSize > 1 || oppoDangerColor != BLUE) &&
                        nextDangerColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != GREEN) &&
                        (oppoSize > 1 || oppoDangerColor != GREEN) &&
                        nextDangerColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != YELLOW) &&
                        (oppoSize > 1 || oppoDangerColor != YELLOW) &&
                        nextDangerColor != YELLOW) {
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
            } // else if (lastColor == nextDangerColor)
            else if (nextDangerColor != NONE) {
                // Your next player played a wild card, started an UNO dash in
                // its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the dangerous color again.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != RED) &&
                        (oppoSize > 1 || oppoDangerColor != RED) &&
                        nextDangerColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != BLUE) &&
                        (oppoSize > 1 || oppoDangerColor != BLUE) &&
                        nextDangerColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != GREEN) &&
                        (oppoSize > 1 || oppoDangerColor != GREEN) &&
                        nextDangerColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        (prevSize > 1 || prevDangerColor != YELLOW) &&
                        (oppoSize > 1 || oppoDangerColor != YELLOW) &&
                        nextDangerColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasRev &&
                        prevSize >= 4 &&
                        hand.get(idxRev).getRealColor() != nextDangerColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != nextDangerColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
            } // else if (nextDangerColor != NONE)
            else {
                // Your next player started dash without playing a wild
                // card, so use normal defense strategies.
                if (hasSkip) {
                    // Play a [skip] to skip its turn and wait for more chances.
                    idxBest = idxSkip;
                } // if (hasSkip)
                else if (hasWD4 &&
                        lastColor != safeColor &&
                        !hasNumIn[lastColor.ordinal()]) {
                    // Then play a [wild +4] to make your next player draw four
                    // cards (if it's legal to play this card).
                    bestColor = safeColor;
                    idxBest = idxWD4;
                } // else if (hasWD4 && ...)
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
                            (prevSize > 1 || prevDangerColor != RED) &&
                            (oppoSize > 1 || oppoDangerColor != RED)) {
                        idxBest = idxNumIn[RED.ordinal()];
                    } // else if (hasNumIn[RED.ordinal()] && ...)
                    else if (hasNumIn[BLUE.ordinal()] &&
                            (prevSize > 1 || prevDangerColor != BLUE) &&
                            (oppoSize > 1 || oppoDangerColor != BLUE)) {
                        idxBest = idxNumIn[BLUE.ordinal()];
                    } // else if (hasNumIn[BLUE.ordinal()] && ...)
                    else if (hasNumIn[GREEN.ordinal()] &&
                            (prevSize > 1 || prevDangerColor != GREEN) &&
                            (oppoSize > 1 || oppoDangerColor != GREEN)) {
                        idxBest = idxNumIn[GREEN.ordinal()];
                    } // else if (hasNumIn[GREEN.ordinal()] && ...)
                    else if (hasNumIn[YELLOW.ordinal()] &&
                            (prevSize > 1 || prevDangerColor != YELLOW) &&
                            (oppoSize > 1 || oppoDangerColor != YELLOW)) {
                        idxBest = idxNumIn[YELLOW.ordinal()];
                    } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                } // else if (lastColor != safeColor)
            } // else
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Save your action cards as much as you can. once a reverse card is
            // played, you can use these cards to limit your previous player's
            // action.
            if (prevSafeColor == NONE ||
                    prevDangerColor != NONE && prevDangerColor != bestColor) {
                safeColor = bestColor;
            } // if (prevSafeColor == NONE || ...)
            else {
                safeColor = prevSafeColor;
            } // else

            while (safeColor == prevDangerColor
                    || oppoSize == 1 && safeColor == oppoDangerColor) {
                // Determine your best color in dangerous cases. Firstly
                // choose previous player's safe color, but be careful of
                // the conflict with other opponents' dangerous colors!
                safeColor = Color.values()[mRnd.nextInt(4) + 1];
            } // while (safeColor == prevDangerColor || ...)

            if (lastColor == prevDangerColor) {
                // Your previous player played a wild card, started an UNO dash
                // in its last action. You have to change the following legal
                // color, or you will approximately 100% lose this game.
                if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != prevDangerColor) {
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
            } // if (lastColor == prevDangerColor)
            else if (prevDangerColor != NONE) {
                // Your previous player played a wild card, started an UNO dash
                // in its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the dangerous color again.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[RED.ordinal()] &&
                        (oppoSize > 1 || oppoDangerColor != RED) &&
                        prevDangerColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        (oppoSize > 1 || oppoDangerColor != BLUE) &&
                        prevDangerColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        (oppoSize > 1 || oppoDangerColor != GREEN) &&
                        prevDangerColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        (oppoSize > 1 || oppoDangerColor != YELLOW) &&
                        prevDangerColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
            } // else if (prevDangerColor != NONE)
            else {
                // Your previous player started an UNO dash without playing a
                // wild card, so use normal defense strategies.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor].ordinal())
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
            // directly limit your opposite player's action.
            if (oppoSafeColor == NONE ||
                    oppoDangerColor != NONE && oppoDangerColor != bestColor) {
                safeColor = bestColor;
            } // if (oppoSafeColor == NONE || ...)
            else {
                safeColor = oppoSafeColor;
            } // else

            while (safeColor == oppoDangerColor) {
                // Determine your best color in dangerous cases. Firstly
                // choose opposite player's safe color, but be careful of
                // the conflict with other opponents' dangerous colors!
                safeColor = Color.values()[mRnd.nextInt(4) + 1];
            } // while (safeColor == oppoDangerColor || ...)

            if (lastColor == oppoDangerColor) {
                // Your opposite player played a wild card, started an UNO dash
                // in its last action, and what's worse is that the legal color
                // has not been changed yet. You have to change the following
                // legal color, or you will approximately 100% lose this game.
                if (hasNumIn[safeColor.ordinal()]) {
                    // At first, try to change legal color by playing an action
                    // card or a number card, instead of using wild cards.
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[bestColor.ordinal()] &&
                        oppoDangerColor != bestColor) {
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()] && ...)
                else if (hasNumIn[RED.ordinal()] &&
                        oppoDangerColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        oppoDangerColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        oppoDangerColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        oppoDangerColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasRev &&
                        hand.get(idxRev).getRealColor() != oppoDangerColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        hand.get(idxSkip).getRealColor() != oppoDangerColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 &&
                        hand.get(idxDraw2).getRealColor() != oppoDangerColor) {
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
            } // if (lastColor == oppoDangerColor)
            else if (oppoDangerColor != NONE) {
                // Your opposite player played a wild card, started an UNO dash
                // in its last action, but fortunately the legal color has been
                // changed already. Just be careful not to re-change the legal
                // color to the dangerous color again.
                if (hasNumIn[safeColor.ordinal()]) {
                    idxBest = idxNumIn[safeColor.ordinal()];
                } // if (hasNumIn[safeColor.ordinal()])
                else if (hasNumIn[bestColor.ordinal()] &&
                        oppoDangerColor != bestColor) {
                    idxBest = idxNumIn[bestColor.ordinal()];
                } // else if (hasNumIn[bestColor.ordinal()] && ...)
                else if (hasNumIn[RED.ordinal()] &&
                        oppoDangerColor != RED) {
                    idxBest = idxNumIn[RED.ordinal()];
                } // else if (hasNumIn[RED.ordinal()] && ...)
                else if (hasNumIn[BLUE.ordinal()] &&
                        oppoDangerColor != BLUE) {
                    idxBest = idxNumIn[BLUE.ordinal()];
                } // else if (hasNumIn[BLUE.ordinal()] && ...)
                else if (hasNumIn[GREEN.ordinal()] &&
                        oppoDangerColor != GREEN) {
                    idxBest = idxNumIn[GREEN.ordinal()];
                } // else if (hasNumIn[GREEN.ordinal()] && ...)
                else if (hasNumIn[YELLOW.ordinal()] &&
                        oppoDangerColor != YELLOW) {
                    idxBest = idxNumIn[YELLOW.ordinal()];
                } // else if (hasNumIn[YELLOW.ordinal()] && ...)
                else if (hasRev &&
                        hand.get(idxRev).getRealColor() != oppoDangerColor) {
                    idxBest = idxRev;
                } // else if (hasRev && ...)
                else if (hasSkip &&
                        nextSize <= 4 &&
                        hand.get(idxSkip).getRealColor() != oppoDangerColor) {
                    idxBest = idxSkip;
                } // else if (hasSkip && ...)
                else if (hasDraw2 &&
                        nextSize <= 4 &&
                        hand.get(idxDraw2).getRealColor() != oppoDangerColor) {
                    idxBest = idxDraw2;
                } // else if (hasDraw2 && ...)
            } // else if (oppoDangerColor != NONE)
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
            // Set the following legal color to one of your opponents' safe
            // colors, in order to prevent them from playing a [+2] to you.
            if (prevSafeColor != NONE) {
                bestColor = prevSafeColor;
            } // if (prevSafeColor != NONE)
            else if (oppoSafeColor != NONE) {
                bestColor = oppoSafeColor;
            } // else if (oppoSafeColor != NONE)
            else if (nextSafeColor != NONE) {
                bestColor = nextSafeColor;
            } // else if (nextSafeColor != NONE)
            else {
                while (bestColor == prevDangerColor
                        || bestColor == oppoDangerColor
                        || bestColor == nextDangerColor) {
                    bestColor = Color.values()[mRnd.nextInt(4) + 1];
                } // while (bestColor == prevDangerColor || ...)
            } // else

            // When your next player remains only a few cards, use [Wild +4]
            // cards at first. Otherwise, use [Wild] cards at first.
            if (nextSize <= 4) {
                if (hasWD4) {
                    idxBest = idxWD4;
                } // if (hasWD4)
                else {
                    idxBest = idxWild;
                } // else
            } // if (nextSize <= 4)
            else {
                if (hasWild) {
                    idxBest = idxWild;
                } // if (hasWild)
                else {
                    idxBest = idxWD4;
                } // else
            } // else
        } // else if (allWild)
        else if (lastColor == nextSafeColor && yourSize > 2) {
            // Strategies when your next player drew a card in its last action.
            // Unless keeping or changing to your best color, you do not need to
            // play your limitation/wild cards. Use them in more dangerous cases.
            if (hasRev && prevSize - nextSize >= 3) {
                idxBest = idxRev;
            } // if (hasRev && prevSize - nextSize >= 3)
            else if (hasNumIn[nextSafeColor.ordinal()]) {
                idxBest = idxNumIn[nextSafeColor.ordinal()];
            } // else if (hasNumIn[nextSafeColor.ordinal()])
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
                    hand.get(idxSkip).getRealColor() == bestColor) {
                idxBest = idxSkip;
            } // else if (hasSkip && ...)
            else if (hasDraw2 &&
                    hand.get(idxDraw2).getRealColor() == bestColor) {
                idxBest = idxDraw2;
            } // else if (hasDraw2 && ...)
        } // else if (lastColor == nextSafeColor && yourSize > 2)
        else {
            // Normal strategies
            if (hasDraw2 && nextSize <= 4) {
                // Play a [+2] when your next player remains only a few cards.
                idxBest = idxDraw2;
            } // if (hasDraw2 && nextSize <= 4)
            else if (hasSkip && nextSize <= 4) {
                // Play a [skip] when your next player remains only a few cards.
                idxBest = idxSkip;
            } // else if (hasSkip && nextSize <= 4)
            else if (hasRev && prevSize - nextSize >= 3) {
                // Play a [reverse] when your next player remains only a few
                // cards but your previous player remains a lot of cards, in
                // order to balance everyone's hand-card amount.
                idxBest = idxRev;
            } // else if (hasRev && prevSize - nextSize >= 3)
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
            else if (hasRev &&
                    (prevSize >= 4 || prev.getRecent() == null)) {
                // When you have no more legal number cards to play, you can
                // play a [reverse] safely when your previous player still has
                // a number of cards, or drew a card in its previous action
                // (this probably makes it draw a card again).
                idxBest = idxRev;
            } // else if (hasRev && ...)
            else if (hasSkip &&
                    hand.get(idxSkip).getRealColor() == bestColor) {
                // Unless keeping or changing to your best color, you do not
                // need to play your limitation/wild cards when your next player
                // still has a number of cards. Use them in more dangerous cases.
                idxBest = idxSkip;
            } // else if (hasSkip && ...)
            else if (hasDraw2 &&
                    hand.get(idxDraw2).getRealColor() == bestColor) {
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
    } // hardAI_bestCardIndex4NowPlayer()
} // AI Class

// E.O.F