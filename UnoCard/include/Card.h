////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __CARD_H_494649FDFA62B3C015120BCB9BE17613__
#define __CARD_H_494649FDFA62B3C015120BCB9BE17613__

#include <Color.h>
#include <Content.h>
#include <opencv2/core.hpp>

/**
 * Uno Card Class.
 */
class Card {
private:
    /**
     * Card's color, e.g. Color::BLUE
     */
    Color color;

    /**
     * Card's order. Used in the hand card sorting procedure.
     * Everyone's hand cards will be sorted based on the order sequence.
     */
    const int order;

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    Card(cv::Mat, cv::Mat, Color, Content, const char*);

    /**
     * Grant Uno class to access our constructors (to create Card instances) and
     * our private fields (to change the wild color when necessary).
     */
    friend class Uno;

public:
    /**
     * Card's name, e.g. "Blue 3"
     */
    const char* name;

    /**
     * Card's image resource, e.g. cv::imread("resource/front_b3.png")
     */
    const cv::Mat image;

    /**
     * Card's dark image resource, e.g. cv::imread("resource/dark_b3.png")
     */
    const cv::Mat darkImg;

    /**
     * Card's content, e.g. Content::NUM3
     */
    const Content content;

    /**
     * @return For non-wild cards, return card's color. For wild cards, return
     *         the specified wild color by the player who played this card, or
     *         Color::NONE if this card is in hand or card deck.
     */
    Color getRealColor();

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    bool isWild();
}; // Card Class

#endif // __CARD_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F