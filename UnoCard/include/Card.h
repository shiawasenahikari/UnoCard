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
#include "include/Color.h"
#include "include/Content.h"

typedef const char* String;

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
     * Card's name, e.g. "[B]3"
     */
    const String name;

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
     * Generate card name via given color and content.
     */
    inline static String getName(Color color, Content content) {
        static String name[] = {
            "[R]0", "[R]1", "[R]2", "[R]3", "[R]4", "[R]5", "[R]6",
            "[R]7", "[R]8", "[R]9", "[R]+2", "[R]Reverse", "[R]Skip",
            "[B]0", "[B]1", "[B]2", "[B]3", "[B]4", "[B]5", "[B]6",
            "[B]7", "[B]8", "[B]9", "[B]+2", "[B]Reverse", "[B]Skip",
            "[G]0", "[G]1", "[G]2", "[G]3", "[G]4", "[G]5", "[G]6",
            "[G]7", "[G]8", "[G]9", "[G]+2", "[G]Reverse", "[G]Skip",
            "[Y]0", "[Y]1", "[Y]2", "[Y]3", "[Y]4", "[Y]5", "[Y]6",
            "[Y]7", "[Y]8", "[Y]9", "[Y]+2", "[Y]Reverse", "[Y]Skip",
            "Wild", "Wild +4"
        }; // name[]
        return name[color == NONE ? 39 + content : 13 * (color - 1) + content];
    } // getName(Color, Content)

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    inline Card(QImage image, QImage darkImg, Color color, Content content) :
        color(color),
        image(image),
        darkImg(darkImg),
        content(content),
        name(getName(color, content)),
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