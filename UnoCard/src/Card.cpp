////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include <string>
#include <Card.h>
#include <Color.h>
#include <Content.h>
#include <opencv2/core.hpp>

/**
 * Constructor. Provide parameters for an Uno card and create its instance.
 */
Card::Card(cv::Mat image, cv::Mat darkImg,
	Color color, Content content, std::string name) :
	name(name),
	image(image),
	color(color),
	content(content),
	darkImg(darkImg),
	order((color << 8) + content) {
} // Card(Mat, Mat, Color, Content, string) (Class Constructor)

/**
 * Override relational operator (<).
 */
bool Card::operator<(const Card& card) {
	return order < card.order;
} // operator<()

/**
 * Override relational operator (<=).
 */
bool Card::operator<=(const Card& card) {
	return order <= card.order;
} // operator<=()

/**
 * Override relational operator (==).
 */
bool Card::operator==(const Card& card) {
	return order == card.order;
} // operator==()

/**
 * Override relational operator (>=).
 */
bool Card::operator>=(const Card& card) {
	return order >= card.order;
} // operator>=()

/**
 * Override relational operator (>).
 */
bool Card::operator>(const Card& card) {
	return order > card.order;
} // operator>()

/**
 * Override relational operator (!=).
 */
bool Card::operator!=(const Card& card) {
	return order != card.order;
} // operator!=()

/**
 * @return Card's image resource.
 * @deprecated Please directly access this field (this->image).
 */
[[deprecated]]
const cv::Mat& Card::getImage() {
	return image;
} // getImage()

/**
 * @return Card's dark image resource.
 * @deprecated Please directly access this field (this->darkImg).
 */
[[deprecated]]
const cv::Mat& Card::getDarkImg() {
	return darkImg;
} // getDarkImg()

/**
 * @return For non-wild cards, return card's color. For wild cards, return
 *         the specified wild color by the player who played this card, or
 *         Color::NONE if this card is in hand or card deck.
 */
Color Card::getRealColor() {
	return color;
} // getRealColor()

/**
 * @return Card's content.
 * @deprecated Please directly access this field (this->content).
 */
[[deprecated]]
Content Card::getContent() {
	return content;
} // getContent()

/**
 * @return Card's name.
 * @deprecated Please directly access this field (this->name).
 */
[[deprecated]]
const std::string& Card::getName() {
	return name;
} // getName()

/**
 * @return Whether the card is a [wild] or [wild +4].
 */
bool Card::isWild() {
	return content == WILD || content == WILD_DRAW4;
} // isWild()

// E.O.F