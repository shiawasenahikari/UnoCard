////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __CARD_H_494649FDFA62B3C015120BCB9BE17613__
#define __CARD_H_494649FDFA62B3C015120BCB9BE17613__

#include <QImage>
#include <QString>
#include "include/Color.h"
#include "include/Content.h"

/**
 * Uno Card Class.
 */
class Card {
public:
    /**
     * Card's color, e.g. Color::BLUE
     */
    const Color color;

    /**
     * Card's image resource, e.g. cv::imread("resource/front_b3.png")
     */
    const QImage image;

    /**
     * Card's dark image resource, e.g. cv::imread("resource/dark_b3.png")
     */
    const QImage darkImg;

    /**
     * Card's content, e.g. Content::NUM3
     */
    const Content content;

    /**
     * Card's name, e.g. "Blue 3"
     */
    const QString name;

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    bool isWild();

private:
    /**
     * Card's ID, has the value of 39 + content (for a wild card)
     * or 13 * (color - 1) + content (for a non-wild card)
     */
    const int id;

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    Card(QImage, QImage, Color, Content);

    /**
     * Grant Uno class to access our constructors (to create Card instances) and
     * our private fields (to change the wild color when necessary).
     */
    friend class Uno;
}; // Card Class

#endif // __CARD_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F