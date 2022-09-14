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
     * Override relation operator "<".
     *
     * @param that Provide another Card instance.
     * @return true if this->id < that.id.
     */
    inline bool operator<(const Card& that) {
        return this->id < that.id;
    } // operator<(const Card&)

    /**
     * Override relation operator "<=".
     *
     * @param that Provide another Card instance.
     * @return true if this->id <= that.id.
     */
    inline bool operator<=(const Card& that) {
        return this->id <= that.id;
    } // operator<=(const Card&)

    /**
     * Override relation operator "==".
     *
     * @param that Provide another Card instance.
     * @return true if this->id == that.id.
     */
    inline bool operator==(const Card& that) {
        return this->id == that.id;
    } // operator==(const Card&)

    /**
     * Override relation operator ">=".
     *
     * @param that Provide another Card instance.
     * @return true if this->id >= that.id.
     */
    inline bool operator>=(const Card& that) {
        return this->id >= that.id;
    } // operator>=(const Card&)

    /**
     * Override relation operator ">".
     *
     * @param that Provide another Card instance.
     * @return true if this->id > that.id.
     */
    inline bool operator>(const Card& that) {
        return this->id > that.id;
    } // operator>(const Card&)

    /**
     * Override relation operator "!=".
     *
     * @param that Provide another Card instance.
     * @return true if this->id != that.id.
     */
    inline bool operator!=(const Card& that) {
        return this->id != that.id;
    } // operator!=(const Card&)

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    inline bool isWild() {
        return color == NONE;
    } // isWild()

private:
    /**
     * Card's ID, has the value of 39 + content (for a wild card)
     * or 13 * (color - 1) + content (for a non-wild card)
     */
    const int id;

    /**
     * Color part of name string.
     */
    inline static const QString& A(Color color) {
        static QString a[] = { "", "Red ", "Blue ", "Green ", "Yellow " };
        return a[color];
    } // A(Color)

    /**
     * Content part of name string.
     */
    inline static const QString& B(Content content) {
        static QString b[] = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "+2", "Skip", "Reverse", "Wild", "Wild +4"
        }; // b[]
        return b[content];
    } // B(Content)

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    inline Card(QImage image, QImage darkImg, Color color, Content content) :
        color(color),
        image(image),
        darkImg(darkImg),
        content(content),
        name(A(color) + B(content)),
        id(isWild() ? 39 + content : 13 * (color - 1) + content) {
    } // Card(QImage, QImage, Color, Content) (Class Constructor)

    /**
     * Grant Uno class to access our constructors (to create Card instances) and
     * our private fields (to change the wild color when necessary).
     */
    friend class Uno;
}; // Card Class

#endif // __CARD_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F