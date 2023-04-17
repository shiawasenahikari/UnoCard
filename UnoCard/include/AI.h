////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __AI_H_494649FDFA62B3C015120BCB9BE17613__
#define __AI_H_494649FDFA62B3C015120BCB9BE17613__

#include <map>
#include <vector>
#include <cstdlib>
#include "include/Uno.h"
#include "include/Card.h"
#include "include/Color.h"
#include "include/Player.h"
#include "include/Content.h"

/**
 * AI Strategies.
 */
class AI {
private:
    /**
     * Uno runtime.
     */
    Uno* uno = Uno::getInstance();

    /**
     * Record the priorities of your candidates. Used by hard AI.
     */
    std::map<int, Card*> candidates;

    /**
     * Default constructor.
     */
    AI() = default;

public:
    /**
     * In main.cpp, get AI instance here.
     *
     * @return Reference of our singleton.
     */
    inline static AI* getInstance() {
        static AI instance;
        return &instance;
    } // getInstance()

    /**
     * Evaluate which color is the best for current player. In our evaluation
     * system, zero cards / reverse cards are worth 2 points, non-zero number
     * cards are worth 4 points, and skip / draw two cards are worth 5 points.
     * Finally, the color which contains the worthiest cards becomes the best
     * color.
     *
     * @return Current player's best color.
     */
    inline Color calcBestColor4NowPlayer() {
        Color bestColor;
        Player *next, *oppo, *prev;
        Color nextWeak, nextStrong;
        Color oppoWeak, oppoStrong;
        Color prevWeak, prevStrong;
        bool nextIsUno, oppoIsUno, prevIsUno;

        // When defensing UNO dash, use others' weak color as your best color
        next = uno->getNextPlayer();
        oppo = uno->getOppoPlayer();
        prev = uno->getPrevPlayer();
        nextIsUno = next->getHandSize() == 1;
        oppoIsUno = oppo->getHandSize() == 1;
        prevIsUno = prev->getHandSize() == 1;
        nextStrong = next->getStrongColor();
        oppoStrong = oppo->getStrongColor();
        prevStrong = prev->getStrongColor();
        nextWeak = next->getWeakColor();
        oppoWeak = oppo->getWeakColor();
        prevWeak = prev->getWeakColor();
        if (nextIsUno && nextWeak != NONE) {
            bestColor = nextWeak;
        } // if (nextIsUno && nextWeak != NONE)
        else if (oppoIsUno && oppoWeak != NONE && !uno->is2vs2()) {
            bestColor = oppoWeak;
        } // else if (oppoIsUno && oppoWeak != NONE && !uno->is2vs2())
        else if (prevIsUno && prevWeak != NONE) {
            bestColor = prevWeak;
        } // else if (prevIsUno && prevWeak != NONE)
        else if (uno->is2vs2() &&
            oppoStrong != NONE &&
            uno->getCurrPlayer()->getHandSize() > oppo->getHandSize()) {
            bestColor = oppoStrong;
        } // else if (uno->is2vs2() && ...)
        else {
            int score[] = { 0, 0, 0, 0, 0 };
            Player *curr = uno->getCurrPlayer();

            for (Card* card : curr->getHandCards()) {
                switch (card->content) {
                case WILD:
                case WILD_DRAW4:
                    break; // case WILD, WILD_DRAW4

                case REV:
                case NUM0:
                    score[card->color] += 2;
                    break; // case REV, NUM0

                case SKIP:
                case DRAW2:
                    score[card->color] += 5;
                    break; // case SKIP, DRAW2

                default:
                    score[card->color] += 4;
                    break; // default
                } // switch (card->content)
            } // for (Card* card : curr->getHandCards())

            // Calculate the best color
            bestColor = NONE;
            if (score[RED] > score[bestColor]) {
                bestColor = RED;
            } // if (score[RED] > score[bestColor])

            if (score[BLUE] > score[bestColor]) {
                bestColor = BLUE;
            } // if (score[BLUE] > score[bestColor]

            if (score[GREEN] > score[bestColor]) {
                bestColor = GREEN;
            } // if (score[GREEN] > score[bestColor])

            if (score[YELLOW] > score[bestColor]) {
                bestColor = YELLOW;
            } // if (score[YELLOW] > score[bestColor])

            if (bestColor == NONE) {
                // Only wild cards in hand
                // Use others' weak color as your best color
                bestColor
                    = prevWeak != NONE ? prevWeak
                    : oppoWeak != NONE && !uno->is2vs2() ? oppoWeak
                    : nextWeak != NONE ? nextWeak : RED;
            } // if (bestColor == NONE)
        } // else

        // Determine your best color in dangerous cases. Be careful of the
        // conflict with other opponents' strong colors.
        while ((nextIsUno && bestColor == nextStrong)
            || (oppoIsUno && bestColor == oppoStrong && !uno->is2vs2())
            || (prevIsUno && bestColor == prevStrong)) {
            bestColor = Color(rand() % 4 + 1);
        } // while (nextIsUno && bestColor == nextStrong || ...)

        return bestColor;
    } // calcBestColor4NowPlayer()

    /**
     * In 7-0 rule, when a seven card is put down, the player must swap hand
     * cards with another player immediately. This API returns that swapping
     * with whom is the best answer for current player.
     *
     * @return Current player swaps with whom. Must be one of the following:
     *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
     */
    inline int calcBestSwapTarget4NowPlayer() {
        int target;
        std::vector<Card*> hand;
        Player *curr, *next, *oppo, *prev;

        next = uno->getNextPlayer();
        oppo = uno->getOppoPlayer();
        prev = uno->getPrevPlayer();
        if (next->getHandSize() == 1) {
            target = uno->getNext();
        } // if (next->getHandSize() == 1)
        else if (prev->getHandSize() == 1) {
            target = uno->getPrev();
        } // else if (prev->getHandSize() == 1)
        else if (oppo->getHandSize() == 1) {
            target = uno->getOppo();
        } // else if (oppo->getHandSize() == 1)
        else if (prev->getStrongColor() == uno->lastColor()) {
            target = uno->getPrev();
        } // else if (prev->getStrongColor() == uno->lastColor())
        else if (oppo->getStrongColor() == uno->lastColor()) {
            target = uno->getOppo();
        } // else if (oppo->getStrongColor() == uno->lastColor())
        else {
            target = uno->getNext();
        } // else

        curr = uno->getCurrPlayer();
        hand = curr->getHandCards();
        if (hand.size() == 1 &&
            target == uno->getNext() &&
            uno->isLegalToPlay(hand.at(0))) {
            // Do not swap with your next player when your final card is a legal
            // card. This will make your next player win the game.
            target = uno->getPrev();
        } // if (hand.size() == 1 && ...)

        return target;
    } // calcBestSwapTarget4NowPlayer()

    /**
     * AI strategies of determining if it's necessary to challenge previous
     * player's [wild +4] card's legality.
     *
     * @return True if it's necessary to make a challenge.
     */
    inline bool needToChallenge() {
        Color lastColor = uno->lastColor();
        Color next2lastColor = uno->next2lastColor();
        int size = uno->getNextPlayer()->getHandSize();

        // Challenge when defending my UNO dash
        // Challenge when I have 10 or more cards already
        // Challenge when legal color has not been changed
        return size == 1 || size >= 10 || lastColor == next2lastColor;
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
     *         Or a negative number that means no appropriate card to play.
     */
    inline int easyAI_bestCardIndex4NowPlayer(Color outColor[]) {
        Card* card;
        Player* prev;
        int i, iBest, matches;
        std::vector<Card*> hand;
        Color bestColor, lastColor;
        int yourSize, nextSize, prevSize;
        int iNum, iRev, iSkip, iDraw2, iWild, iWD4;
        bool hasNum, hasRev, hasSkip, hasDraw2, hasWild, hasWD4;

        if (outColor == nullptr) {
            throw "outColor[] cannot be nullptr";
        } // if (outColor == nullptr)

        hand = uno->getCurrPlayer()->getHandCards();
        yourSize = int(hand.size());
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.at(0);
            outColor[0] = card->color;
            return uno->isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        iBest = -1;
        lastColor = uno->lastColor();
        bestColor = calcBestColor4NowPlayer();
        iNum = iRev = iSkip = iDraw2 = iWild = iWD4 = -1;
        hasNum = hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
        for (i = matches = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.at(i);
            if (card->color == lastColor) {
                ++matches;
            } // if (card->color == lastColor)

            if (uno->isLegalToPlay(card)) {
                switch (card->content) {
                case DRAW2:
                    if (!hasDraw2 || card->color == bestColor) {
                        iDraw2 = i;
                        hasDraw2 = true;
                    } // if (!hasDraw2 || card->color == bestColor)
                    break; // case DRAW2

                case SKIP:
                    if (!hasSkip || card->color == bestColor) {
                        iSkip = i;
                        hasSkip = true;
                    } // if (!hasSkip || card->color == bestColor)
                    break; // case SKIP

                case REV:
                    if (!hasRev || card->color == bestColor) {
                        iRev = i;
                        hasRev = true;
                    } // if (!hasRev || card->color == bestColor)
                    break; // case REV

                case WILD:
                    iWild = i;
                    hasWild = true;
                    break; // case WILD

                case WILD_DRAW4:
                    iWD4 = i;
                    hasWD4 = true;
                    break; // case WILD_DRAW4

                default: // number cards
                    if (!hasNum || card->color == bestColor) {
                        iNum = i;
                        hasNum = true;
                    } // if (!hasNum || card->color == bestColor)
                    break; // default
                } // switch (card->content)
            } // if (uno->isLegalToPlay(card))
        } // for (i = matches = 0; i < yourSize; ++i)

        // Decision tree
        nextSize = uno->getNextPlayer()->getHandSize();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2)
                iBest = iDraw2;
            else if (hasSkip)
                iBest = iSkip;
            else if (hasRev)
                iBest = iRev;
            else if (hasWD4 && matches == 0)
                iBest = iWD4;
            else if (hasWild && lastColor != bestColor)
                iBest = iWild;
            else if (hasWD4 && lastColor != bestColor)
                iBest = iWD4;
            else if (hasNum)
                iBest = iNum;
        } // if (nextSize == 1)
        else {
            // Normal strategies
            prev = uno->getPrevPlayer();
            prevSize = prev->getHandSize();
            if (hasRev && (prevSize > nextSize
                || prev->getRecent() == nullptr))
                iBest = iRev;
            else if (hasNum)
                iBest = iNum;
            else if (hasSkip)
                iBest = iSkip;
            else if (hasDraw2)
                iBest = iDraw2;
            else if (hasRev && prevSize > 1)
                iBest = iRev;
            else if (hasWild)
                iBest = iWild;
            else if (hasWD4)
                iBest = iWD4;
        } // else

        outColor[0] = bestColor;
        return iBest;
    } // easyAI_bestCardIndex4NowPlayer(Color[])

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
     *         Or a negative number that means no appropriate card to play.
     */
    inline int hardAI_bestCardIndex4NowPlayer(Color outColor[]) {
        Card* card;
        bool allWild;
        std::vector<Card*> hand;
        Player *next, *oppo, *prev;
        Color bestColor, lastColor;
        int i, iBest, matches, score;
        int iRev, iSkip, iDraw2, iWild, iWD4;
        int yourSize, nextSize, oppoSize, prevSize;
        bool hasRev, hasSkip, hasDraw2, hasWild, hasWD4;
        Color nextWeak, nextStrong, oppoStrong, prevStrong;

        if (outColor == nullptr) {
            throw "outColor[] cannot be nullptr";
        } // if (outColor == nullptr)

        hand = uno->getCurrPlayer()->getHandCards();
        yourSize = int(hand.size());
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.at(0);
            outColor[0] = card->color;
            return uno->isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        iBest = -1;
        allWild = true;
        candidates.clear();
        lastColor = uno->lastColor();
        bestColor = calcBestColor4NowPlayer();
        iRev = iSkip = iDraw2 = iWild = iWD4 = -1;
        hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
        for (i = matches = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.at(i);
            allWild = allWild && card->isWild();
            if (card->color == lastColor) {
                ++matches;
            } // if (card->color == lastColor)

            if (uno->isLegalToPlay(card)) {
                switch (card->content) {
                case DRAW2:
                    if (!hasDraw2 || card->color == bestColor) {
                        iDraw2 = i;
                        hasDraw2 = true;
                    } // if (!hasDraw2 || card->color == bestColor)
                    break; // case DRAW2

                case SKIP:
                    if (!hasSkip || card->color == bestColor) {
                        iSkip = i;
                        hasSkip = true;
                    } // if (!hasSkip || card->color == bestColor)
                    break; // case SKIP

                case REV:
                    if (!hasRev || card->color == bestColor) {
                        iRev = i;
                        hasRev = true;
                    } // if (!hasRev || card->color == bestColor)
                    break; // case REV

                case WILD:
                    iWild = i;
                    hasWild = true;
                    break; // case WILD

                case WILD_DRAW4:
                    iWD4 = i;
                    hasWD4 = true;
                    break; // case WILD_DRAW4

                default: // number cards
                    // When you have multiple choices, firstly choose the
                    // cards in your best color, then choose the cards
                    // of the contents that appeared a lot of times.
                    // This can reduce the possibility of changing color
                    // by your opponents. e.g. When you put down the 8th
                    // [nine] card after 7 [nine] cards appeared, no one
                    // can change the color by using another [nine] card.
                    score = -1000000 * (card->color == bestColor ? 1 : 0)
                        - 10000 * uno->getContentAnalysis(card->content)
                        - 100 * uno->getColorAnalysis(card->color)
                        - i;
                    candidates.insert({ score, card });
                    break; // default
                } // switch (card->content)
            } // if (uno->isLegalToPlay(card))
        } // for (i = matches = 0; i < yourSize; ++i)

        // Decision tree
        next = uno->getNextPlayer();
        nextSize = next->getHandSize();
        nextWeak = next->getWeakColor();
        nextStrong = next->getStrongColor();
        oppo = uno->getOppoPlayer();
        oppoSize = oppo->getHandSize();
        oppoStrong = oppo->getStrongColor();
        prev = uno->getPrevPlayer();
        prevSize = prev->getHandSize();
        prevStrong = prev->getStrongColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Limit your next player's action as well as you can.
            if (hasDraw2)
                iBest = iDraw2;
            else if (lastColor == nextStrong) {
                // Priority when next called Uno & lastColor == nextStrong:
                // 0: Number cards, NOT in color of nextStrong
                // 1: Skip cards, in any color
                // 2: Wild cards, switch to your best color
                // 3: Wild +4 cards, switch to your best color
                // 4: Reverse cards, in any color
                // 5: Draw one, and pray to get one of the above...
                for (auto& can : candidates) {
                    if (can.second->color != nextStrong) {
                        iBest = -can.first % 100;
                        break;
                    } // if (can.second->color != nextStrong)
                } // for (auto& can : candidates)
                if (iBest < 0 && hasSkip)
                    iBest = iSkip;
                if (iBest < 0 && hasWild)
                    iBest = iWild;
                if (iBest < 0 && hasWD4)
                    iBest = iWD4;
                if (iBest < 0 && hasRev)
                    iBest = iRev;
            } // else if (lastColor == nextStrong)
            else if (nextStrong != NONE) {
                // Priority when next called Uno & lastColor != nextStrong:
                // (nextStrong is known)
                // 0: Number cards, NOT in color of nextStrong
                // 1: Reverse cards, NOT in color of nextStrong
                // 2: Skip cards, NOT in color of nextStrong
                // 3: Draw one because it's not necessary to use wild cards
                for (auto& can : candidates) {
                    if (can.second->color != nextStrong) {
                        iBest = -can.first % 100;
                        break;
                    } // if (can.second->color != nextStrong)
                } // for (auto& can : candidates)
                if (iBest < 0 && hasRev && prevSize >= 4 &&
                    hand.at(iRev)->color != nextStrong)
                    iBest = iRev;
                if (iBest < 0 && hasSkip &&
                    hand.at(iSkip)->color != nextStrong)
                    iBest = iSkip;
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
                    iBest = iSkip;
                if (iBest < 0 && hasRev)
                    iBest = iRev;
                if (iBest < 0 && hasWD4 && matches == 0)
                    iBest = iWD4;
                if (iBest < 0 && !candidates.empty() &&
                    candidates.begin()->second->color == bestColor)
                    iBest = -candidates.begin()->first % 100;
                if (iBest < 0 && hasWild)
                    iBest = iWild;
                if (iBest < 0 && hasWD4)
                    iBest = iWD4;
                if (iBest < 0 && !candidates.empty())
                    iBest = -candidates.begin()->first % 100;
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
                if (hasSkip && hand.at(iSkip)->color != prevStrong)
                    iBest = iSkip;
                if (iBest < 0 && hasWild)
                    iBest = iWild;
                if (iBest < 0 && hasWD4)
                    iBest = iWD4;
                if (iBest < 0 && !candidates.empty())
                    iBest = -candidates.begin()->first % 100;
            } // if (lastColor == prevStrong)
            else if (prevStrong != NONE) {
                // Priority when prev called Uno & lastColor != prevStrong:
                // (prevStrong is known)
                // 0: Reverse cards, NOT in color of prevStrong
                // 1: Number cards, NOT in color of prevStrong
                // 2: Draw one because it's not necessary to use other cards
                if (hasRev && hand.at(iRev)->color != prevStrong)
                    iBest = iRev;
                if (iBest < 0) for (auto& can : candidates) {
                    if (can.second->color != prevStrong) {
                        iBest = -can.first % 100;
                        break;
                    } // if (can.second->color != prevStrong)
                } // if (iBest < 0) for (auto& can : candidates)
            } // else if (prevStrong != NONE)
            else {
                // Priority when prev called Uno & prevStrong is unknown:
                // 0: Number cards, in your best color
                // 1: Wild cards, switch to your best color
                // 2: Wild +4 cards, switch to your best color
                // 3: Number cards, in any color
                // 4: Draw one. DO NOT PLAY REVERSE CARDS!
                if (!candidates.empty() &&
                    candidates.begin()->second->color == bestColor)
                    iBest = -candidates.begin()->first % 100;
                if (iBest < 0 && hasWild && lastColor != bestColor)
                    iBest = iWild;
                if (iBest < 0 && hasWD4 && lastColor != bestColor)
                    iBest = iWD4;
                if (iBest < 0 && !candidates.empty())
                    iBest = -candidates.begin()->first % 100;
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
                for (auto& can : candidates) {
                    if (can.second->color != oppoStrong) {
                        iBest = -can.first % 100;
                        break;
                    } // if (can.second->color != oppoStrong)
                } // for (auto& can : candidates)
                if (iBest < 0 && hasRev &&
                    hand.at(iRev)->color != oppoStrong)
                    iBest = iRev;
                if (iBest < 0 && hasSkip &&
                    hand.at(iSkip)->color != oppoStrong)
                    iBest = iSkip;
                if (iBest < 0 && hasDraw2 &&
                    hand.at(iDraw2)->color != oppoStrong)
                    iBest = iDraw2;
                if (iBest < 0 && hasWild)
                    iBest = iWild;
                if (iBest < 0 && hasWD4)
                    iBest = iWD4;
                if (iBest < 0 && hasRev && prevSize > nextSize)
                    iBest = iRev;
                if (iBest < 0 && !candidates.empty())
                    iBest = -candidates.begin()->first % 100;
            } // if (lastColor == oppoStrong)
            else if (oppoStrong != NONE) {
                // Priority when oppo called Uno & lastColor != oppoStrong:
                // (oppoStrong is known)
                // 0: Number cards, NOT in color of oppoStrong
                // 1: Reverse cards, NOT in color of oppoStrong
                // 2: Skip cards, NOT in color of oppoStrong
                // 3: +2 cards, NOT in color of oppoStrong
                // 4: Draw one because it's not necessary to use other cards
                for (auto& can : candidates) {
                    if (can.second->color != oppoStrong) {
                        iBest = -can.first % 100;
                        break;
                    } // if (can.second->color != oppoStrong)
                } // for (auto& can : candidates)
                if (iBest < 0 && hasRev &&
                    hand.at(iRev)->color != oppoStrong)
                    iBest = iRev;
                if (iBest < 0 && hasSkip &&
                    hand.at(iSkip)->color != oppoStrong)
                    iBest = iSkip;
                if (iBest < 0 && hasDraw2 &&
                    hand.at(iDraw2)->color != oppoStrong)
                    iBest = iDraw2;
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
                    iBest = iRev;
                if (iBest < 0 && !candidates.empty())
                    iBest = -candidates.begin()->first % 100;
                if (iBest < 0 && hasWild && lastColor != bestColor)
                    iBest = iWild;
                if (iBest < 0 && hasWD4 && lastColor != bestColor &&
                    nextSize <= 4)
                    iBest = iWD4;
            } // else
        } // else if (oppoSize == 1)
        else if (allWild) {
            // Strategies when you remain only wild cards.
            // When your next player remains only a few cards, use [Wild +4]
            // cards at first. Otherwise, use [Wild] cards at first.
            if (nextSize <= 4)
                iBest = hasWD4 ? iWD4 : iWild;
            else
                iBest = hasWild ? iWild : iWD4;
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
                iBest = iRev;
            if (iBest < 0) for (auto& can : candidates) {
                if (can.second->color == nextWeak) {
                    iBest = -can.first % 100;
                    break;
                } // if (can.second->color == nextWeak)
            } // if (iBest < 0) for (auto& can : candidates)
            if (iBest < 0 && !candidates.empty())
                iBest = -candidates.begin()->first % 100;
            if (iBest < 0 && hasRev &&
                (prevSize >= 4 || prev->getRecent() == nullptr))
                iBest = iRev;
            if (iBest < 0 && hasSkip && oppoSize >= 3 &&
                hand.at(iSkip)->color == bestColor)
                iBest = iSkip;
            if (iBest < 0 && hasDraw2 && oppoSize >= 3 &&
                hand.at(iDraw2)->color == bestColor)
                iBest = iDraw2;
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
            if ((hasDraw2 || hasSkip) &&
                nextSize <= 4 && nextSize - oppoSize <= 1)
                iBest = std::max(iDraw2, iSkip);
            if (iBest < 0 && hasRev &&
                (prevSize > nextSize || prev->getRecent() == nullptr))
                iBest = iRev;
            if (iBest < 0 && !candidates.empty())
                iBest = -candidates.begin()->first % 100;
            if (iBest < 0 && hasRev && prevSize >= 4)
                iBest = iRev;
            if (iBest < 0 && hasSkip && oppoSize >= 3 &&
                hand.at(iSkip)->color == bestColor)
                iBest = iSkip;
            if (iBest < 0 && hasDraw2 && oppoSize >= 3 &&
                hand.at(iDraw2)->color == bestColor)
                iBest = iDraw2;
            if (iBest < 0 && hasWild && nextSize <= 4)
                iBest = iWild;
            if (iBest < 0 && hasWD4 && nextSize <= 4)
                iBest = iWD4;
            if (iBest < 0 && hasWD4 && yourSize == 2 && prevSize <= 3)
                iBest = iWD4;
            if (iBest < 0 && hasWild && yourSize == 2 && prevSize <= 3)
                iBest = iWild;
            if (iBest < 0 && yourSize == Uno::MAX_HOLD_CARDS) {
                // When you are holding 26 cards, which means you cannot hold
                // more cards, you need to play your action/wild cards to keep
                // game running, even if it's not worth enough to use them.
                if (hasSkip)
                    iBest = iSkip;
                else if (hasDraw2)
                    iBest = iDraw2;
                else if (hasRev)
                    iBest = iRev;
                else if (hasWild)
                    iBest = iWild;
                else if (hasWD4)
                    iBest = iWD4;
            } // if (iBest < 0 && yourSize == Uno::MAX_HOLD_CARDS)
        } // else

        outColor[0] = bestColor;
        return iBest;
    } // hardAI_bestCardIndex4NowPlayer(Color[])

    /**
     * AI Strategies in 2vs2 special rule. Analyze current player's hand cards,
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
     *         Or a negative number that means no appropriate card to play.
     */
    inline int teamAI_bestCardIndex4NowPlayer(Color outColor[]) {
        // TODO: Strategy unimplemented, using easy AI as default.
        return easyAI_bestCardIndex4NowPlayer(outColor);
    } // teamAI_bestCardIndex4NowPlayer(Color[])

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
     *         Or a negative number that means no appropriate card to play.
     */
    inline int sevenZeroAI_bestCardIndex4NowPlayer(Color outColor[]) {
        Card* card;
        int i, iBest, matches;
        std::vector<Card*> hand;
        Player *next, *oppo, *prev;
        Color bestColor, lastColor;
        Color nextStrong, oppoStrong, prevStrong;
        int yourSize, nextSize, oppoSize, prevSize;
        int i0, i7, iNum, iRev, iSkip, iDraw2, iWild, iWD4;
        bool has0, has7, hasNum, hasRev, hasSkip, hasDraw2, hasWild, hasWD4;

        if (outColor == nullptr) {
            throw "outColor[] cannot be nullptr";
        } // if (outColor == nullptr)

        hand = uno->getCurrPlayer()->getHandCards();
        yourSize = int(hand.size());
        if (yourSize == 1) {
            // Only one card remained. Play it when it's legal.
            card = hand.at(0);
            outColor[0] = card->color;
            return uno->isLegalToPlay(card) ? 0 : -1;
        } // if (yourSize == 1)

        iBest = -1;
        lastColor = uno->lastColor();
        bestColor = calcBestColor4NowPlayer();
        i0 = iNum = iWild = iWD4 = -1;
        i7 = iRev = iSkip = iDraw2 = -1;
        has0 = hasNum = hasWild = hasWD4 = false;
        has7 = hasRev = hasSkip = hasDraw2 = false;
        for (i = matches = 0; i < yourSize; ++i) {
            // Index of any kind
            card = hand.at(i);
            if (card->color == lastColor) {
                ++matches;
            } // if (card->color == lastColor)

            if (uno->isLegalToPlay(card)) {
                switch (card->content) {
                case DRAW2:
                    if (!hasDraw2 || card->color == bestColor) {
                        iDraw2 = i;
                        hasDraw2 = true;
                    } // if (!hasDraw2 || card->color == bestColor)
                    break; // case DRAW2

                case SKIP:
                    if (!hasSkip || card->color == bestColor) {
                        iSkip = i;
                        hasSkip = true;
                    } // if (!hasSkip || card->color == bestColor)
                    break; // case SKIP

                case REV:
                    if (!hasRev || card->color == bestColor) {
                        iRev = i;
                        hasRev = true;
                    } // if (!hasRev || card->color == bestColor)
                    break; // case REV

                case WILD:
                    iWild = i;
                    hasWild = true;
                    break; // case WILD

                case WILD_DRAW4:
                    iWD4 = i;
                    hasWD4 = true;
                    break; // case WILD_DRAW4

                case NUM7:
                    if (!has7 || card->color == bestColor) {
                        i7 = i;
                        has7 = true;
                    } // if (!has7 || card->color == bestColor)
                    break; // case NUM7

                case NUM0:
                    if (!has0 || card->color == bestColor) {
                        i0 = i;
                        has0 = true;
                    } // if (!has0 || card->color == bestColor)
                    break; // case NUM0

                default: // number cards
                    if (!hasNum || card->color == bestColor) {
                        iNum = i;
                        hasNum = true;
                    } // if (!hasNum || card->color == bestColor)
                    break; // default
                } // switch (card->content)
            } // if (uno->isLegalToPlay(card))
        } // for (i = matches = 0; i < yourSize; ++i)

        // Decision tree
        next = uno->getNextPlayer();
        nextSize = next->getHandSize();
        nextStrong = next->getStrongColor();
        oppo = uno->getOppoPlayer();
        oppoSize = oppo->getHandSize();
        oppoStrong = oppo->getStrongColor();
        prev = uno->getPrevPlayer();
        prevSize = prev->getHandSize();
        prevStrong = prev->getStrongColor();
        if (nextSize == 1) {
            // Strategies when your next player remains only one card.
            // Firstly consider to use a 7 to steal the UNO, if can't,
            // limit your next player's action as well as you can.
            if (has7 && (yourSize > 2
                || (hand.at(1 - i7)->content != NUM7
                    && hand.at(1 - i7)->content != WILD
                    && hand.at(1 - i7)->content != WILD_DRAW4
                    && hand.at(1 - i7)->color != hand.at(i7)->color)))
                iBest = i7;
            else if (has0 && (yourSize > 2
                || (hand.at(1 - i0)->content != NUM0
                    && hand.at(1 - i0)->content != WILD
                    && hand.at(1 - i0)->content != WILD_DRAW4
                    && hand.at(1 - i0)->color != hand.at(i0)->color)))
                iBest = i0;
            else if (hasDraw2)
                iBest = iDraw2;
            else if (hasSkip)
                iBest = iSkip;
            else if (hasRev)
                iBest = iRev;
            else if (hasWD4 && matches == 0)
                iBest = iWD4;
            else if (hasWild && lastColor != bestColor)
                iBest = iWild;
            else if (hasWD4 && lastColor != bestColor)
                iBest = iWD4;
            else if (hasNum && hand.at(iNum)->color != nextStrong)
                iBest = iNum;
            else if (hasWild && (has7 || has0))
                iBest = iWild;
        } // if (nextSize == 1)
        else if (prevSize == 1) {
            // Strategies when your previous player remains only one card.
            // Consider to use a 0 or 7 to steal the UNO.
            if (has0)
                iBest = i0;
            else if (has7)
                iBest = i7;
            else if (hasNum && hand.at(iNum)->color != prevStrong)
                iBest = iNum;
            else if (hasSkip && hand.at(iSkip)->color != prevStrong)
                iBest = iSkip;
            else if (hasDraw2 && hand.at(iDraw2)->color != prevStrong)
                iBest = iDraw2;
            else if (hasWild && lastColor != bestColor)
                iBest = iWild;
            else if (hasWD4 && lastColor != bestColor)
                iBest = iWD4;
            else if (hasNum)
                iBest = iNum;
        } // else if (prevSize == 1)
        else if (oppoSize == 1) {
            // Strategies when your opposite player remains only one card.
            // Consider to use a 7 to steal the UNO.
            if (has7)
                iBest = i7;
            else if (has0)
                iBest = i0;
            else if (hasNum && hand.at(iNum)->color != oppoStrong)
                iBest = iNum;
            else if (hasRev && prevSize > nextSize)
                iBest = iRev;
            else if (hasSkip && hand.at(iSkip)->color != oppoStrong)
                iBest = iSkip;
            else if (hasDraw2 && hand.at(iDraw2)->color != oppoStrong)
                iBest = iDraw2;
            else if (hasWild && lastColor != bestColor)
                iBest = iWild;
            else if (hasWD4 && lastColor != bestColor)
                iBest = iWD4;
            else if (hasNum)
                iBest = iNum;
        } // else if (oppoSize == 1)
        else {
            // Normal strategies
            if (has0 && hand.at(i0)->color == prevStrong)
                iBest = i0;
            else if (has7 && (hand.at(i7)->color == prevStrong
                || hand.at(i7)->color == oppoStrong
                || hand.at(i7)->color == nextStrong))
                iBest = i7;
            else if (hasRev && (prevSize > nextSize
                || prev->getRecent() == nullptr))
                iBest = iRev;
            else if (hasNum)
                iBest = iNum;
            else if (hasSkip)
                iBest = iSkip;
            else if (hasDraw2)
                iBest = iDraw2;
            else if (hasRev)
                iBest = iRev;
            else if (hasWild)
                iBest = iWild;
            else if (hasWD4)
                iBest = iWD4;
            else if (has0 && (yourSize > 2
                || (hand.at(1 - i0)->content != NUM0
                    && hand.at(1 - i0)->content != WILD
                    && hand.at(1 - i0)->content != WILD_DRAW4
                    && hand.at(1 - i0)->color != hand.at(i0)->color)))
                iBest = i0;
            else if (has7)
                iBest = i7;
        } // else

        outColor[0] = bestColor;
        return iBest;
    } // sevenZeroAI_bestCardIndex4NowPlayer(Color[])
}; // AI Class

#endif // __AI_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F