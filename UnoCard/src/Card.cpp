////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <QImage>
#include <QString>
#include "include/Card.h"
#include "include/Color.h"
#include "include/Content.h"

/**
 * Color part of name string.
 */
static const QString A[] = {
    "", "Red ", "Blue ", "Green ", "Yellow "
}; // A[]

/**
 * Content part of name string.
 */
static const QString B[] = {
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
    "+2", "Skip", "Reverse", "Wild", "Wild +4"
}; // B[]

/**
 * Constructor. Provide parameters for an Uno card and create its instance.
 */
Card::Card(QImage image, QImage darkImg, Color color, Content content) :
    color(color),
    image(image),
    darkImg(darkImg),
    content(content),
    name(A[color] + B[content]),
    id(isWild() ? 39 + content : 13 * (color - 1) + content) {
} // Card(QImage, QImage, Color, Content) (Class Constructor)

/**
 * Override relation operator "<".
 *
 * @param that Provide another Card instance.
 * @return true if this->id < that.id.
 */
bool Card::operator<(const Card& that) {
    return this->id < that.id;
} // operator<(const Card&)

/**
 * Override relation operator "<=".
 *
 * @param that Provide another Card instance.
 * @return true if this->id <= that.id.
 */
bool Card::operator<=(const Card& that) {
    return this->id <= that.id;
} // operator<=(const Card&)

/**
 * Override relation operator "==".
 *
 * @param that Provide another Card instance.
 * @return true if this->id == that.id.
 */
bool Card::operator==(const Card& that) {
    return this->id == that.id;
} // operator==(const Card&)

/**
 * Override relation operator ">=".
 *
 * @param that Provide another Card instance.
 * @return true if this->id >= that.id.
 */
bool Card::operator>=(const Card& that) {
    return this->id >= that.id;
} // operator>=(const Card&)

/**
 * Override relation operator ">".
 *
 * @param that Provide another Card instance.
 * @return true if this->id > that.id.
 */
bool Card::operator>(const Card& that) {
    return this->id > that.id;
} // operator>(const Card&)

/**
 * Override relation operator "!=".
 *
 * @param that Provide another Card instance.
 * @return true if this->id != that.id.
 */
bool Card::operator!=(const Card& that) {
    return this->id != that.id;
} // operator!=(const Card&)

/**
 * @return Whether the card is a [wild] or [wild +4].
 */
bool Card::isWild() {
    return color == NONE;
} // isWild()

// E.O.F