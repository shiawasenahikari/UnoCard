////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __CARD_H_494649FDFA62B3C015120BCB9BE17613__
#define __CARD_H_494649FDFA62B3C015120BCB9BE17613__

#include <string>
#include <Color.h>
#include <Content.h>
#include <opencv2/core.hpp>

/**
 * Uno Card Class.
 */
class Card {
public:
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
	 * Card's name, e.g. "Blue 3"
	 */
	const std::string name;

	/**
	 * Override relational operator (<).
	 */
	bool operator<(const Card& card);

	/**
	 * Override relational operator (<=).
	 */
	bool operator<=(const Card& card);

	/**
	 * Override relational operator (==).
	 */
	bool operator==(const Card& card);

	/**
	 * Override relational operator (>=).
	 */
	bool operator>=(const Card& card);

	/**
	 * Override relational operator (>).
	 */
	bool operator>(const Card& card);

	/**
	 * Override relational operator (!=).
	 */
	bool operator!=(const Card& card);

	/**
	 * @return Card's image resource.
	 * @deprecated Please directly access this field (this->image).
	 */
	[[deprecated]]
	const cv::Mat& getImage();

	/**
	 * @return Card's dark image resource.
	 * @deprecated Please directly access this field (this->darkImg).
	 */
	[[deprecated]]
	const cv::Mat& getDarkImg();

	/**
	 * @return For non-wild cards, return card's color. For wild cards, return
	 *         the specified wild color by the player who played this card, or
	 *         Color::NONE if this card is in hand or card deck.
	 */
	Color getRealColor();

	/**
	 * @return Card's content.
	 * @deprecated Please directly access this field (this->content).
	 */
	[[deprecated]]
	Content getContent();

	/**
	 * @return Card's name.
	 * @deprecated Please directly access this field (this->name).
	 */
	[[deprecated]]
	const std::string& getName();

	/**
	 * @return Whether the card is a [wild] or [wild +4].
	 */
	bool isWild();

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
	Card(cv::Mat, cv::Mat, Color, Content, std::string);

	/**
	 * Grant Uno class to access our constructors (to create Card instances) and
	 * our private fields (to change the wild color when necessary).
	 */
	friend class Uno;
}; // Card Class

#endif // __CARD_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F