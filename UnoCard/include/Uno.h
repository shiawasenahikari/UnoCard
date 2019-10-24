////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __UNO_H_494649FDFA62B3C015120BCB9BE17613__
#define __UNO_H_494649FDFA62B3C015120BCB9BE17613__

#include <string>
#include <vector>
#include <opencv2/core.hpp>

#define PLAYER_YOU      0
#define PLAYER_COM1     1
#define PLAYER_COM2     2
#define PLAYER_COM3     3
#define MAX_HOLD_CARDS  15

/**
 * Uno Color Enumeration.
 */
typedef enum {
	BLACK, RED, BLUE, GREEN, YELLOW
} Color;

/**
 * Uno Content Enumeration.
 */
typedef enum {
	NUM0, NUM1, NUM2, NUM3, NUM4,
	NUM5, NUM6, NUM7, NUM8, NUM9,
	DRAW2, SKIP, REV, WILD, WILD_DRAW4
} Content;

/**
 * Uno Card Class.
 */
class Card {
public:
	/**
	 * Default constructor.
	 */
	Card();

	/**
	 * Constructor. Provide parameters for an Uno card and create its instance.
	 */
	Card(cv::Mat, cv::Mat, Color, Content, std::string);

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
	 */
	const cv::Mat& getImage();

	/**
	 * @return Card's dark image resource.
	 */
	const cv::Mat& getDarkImg();

	/**
	 * @return Card's color.
	 */
	Color getColor();

	/**
	 * Valid only when this is a wild card. Get the specified following legal
	 * color by the player who played this wild card.
	 *
	 * @return Card's wild color.
	 */
	Color getWildColor();

	/**
	 * Valid only when this is a wild card. Set the specified following legal
	 * color by the player who played this wild card.
	 *
	 * @param color New wild color value.
	 */
	void setWildColor(Color wildColor);

	/**
	 * @return Card's content.
	 */
	Content getContent();

	/**
	 * @return Card's name.
	 */
	std::string getName();

	/**
	 * @return Whether the card is an action card.
	 */
	bool isAction();

	/**
	 * @return Whether the card is a wild card.
	 */
	bool isWild();

	/**
	 * @return Whether the card is a zero card.
	 */
	bool isZero();

	/**
	 * @return Whether the card is a non-zero number card.
	 */
	bool isNonZeroNumber();

private:
	/**
	 * Card's order. Used in the hand card sorting procedure.
	 * Everyone's hand cards will be sorted based on the order sequence.
	 */
	int order;

	/**
	 * Card's image resource, e.g. cv::imread("resource/front_b3.png")
	 */
	cv::Mat image;

	/**
	 * Card's dark image resource, e.g. cv::imread("resource/dark_b3.png")
	 */
	cv::Mat darkImg;

	/**
	 * Card's color, e.g. Color::BLUE
	 */
	Color color;

	/**
	 * Card's wild color. Valid only when this is a wild card.
	 */
	Color wildColor;

	/**
	 * Card's content, e.g. Content::NUM3
	 */
	Content content;

	/**
	 * Card's name, e.g. "Blue 3"
	 */
	std::string name;

}; // Card Class

/**
 * Uno Runtime Class (Singleton).
 */
class Uno {
public:
	/**
	 * @return Reference of our singleton.
	 */
	static Uno* getInstance();

	/**
	 * @return Card back image resource.
	 */
	const cv::Mat& getBackImage();

	/**
	 * @return Difficulty button image resource (EASY).
	 */
	const cv::Mat& getEasyImage();

	/**
	 * @return Difficulty button image resource (HARD).
	 */
	const cv::Mat& getHardImage();

	/**
	 * @return Background image resource.
	 */
	const cv::Mat& getBackground();

	/**
	 * When a player played a wild card and specified a following legal color,
	 * get the corresponding color-filled image here, and show it in recent
	 * card area.
	 *
	 * @param color The wild image with which color filled you want to get.
	 * @return Corresponding color-filled image.
	 */
	const cv::Mat& getColoredWildImage(Color color);

	/**
	 * When a player played a wild +4 card and specified a following legal
	 * color, get the corresponding color-filled image here, and show it in
	 * recent card area.
	 *
	 * @param color The wild +4 image with which color filled you want to get.
	 * @return Corresponding color-filled image.
	 */
	const cv::Mat& getColoredWildDraw4Image(Color color);

	/**
	 * @return How many cards in deck (haven't been used yet).
	 */
	int getDeckCount();

	/**
	 * @return How many cards have been used.
	 */
	int getUsedCount();

	/**
	 * @return Recent played cards.
	 */
	const std::vector<Card*>& getRecent();

	/**
	 * @param whom Get whose hand cards. Must be one of the following values:
	 *             PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
	 * @return Specified player's all hand cards.
	 */
	const std::vector<Card*>& getHandCardsOf(int whom);

	/**
	 * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
	 * then determine our start card.
	 */
	void start();

	/**
	 * Call this function when someone needs to draw a card.
	 * <p>
	 * NOTE: Everyone can hold 15 cards at most in this program, so even if this
	 * function is called, the specified player may not draw a card as a result.
	 *
	 * @param who Who draws a card. Must be one of the following values:
	 *            PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
	 * @return Reference of the drawn card, or null if the specified player
	 *         didn't draw a card because of the limit.
	 */
	Card* draw(int who);

	/**
	 * Evaluate which color is the best color for the specified player. In our
	 * evaluation system, zero cards are worth 1 point, non-zero number cards
	 * are worth 2 points, and action cards are worth 3 points. Finally, the
	 * color which contains the worthiest cards becomes the best color.
	 *
	 * @param whom Evaluate whose best color. Must be one of the following
	 *             values: PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
	 * @return The best color for the specified player now. Specially, when an
	 *         illegal [whom] parameter was passed in, or the specified. player
	 *         remains only wild cards, function will return a default value,
	 *         which is Color::RED.
	 */
	Color bestColorFor(int whom);

	/**
	 * Check whether the specified card is legal to play. It's legal only when
	 * it's wild, or it has the same color/content to the previous played card.
	 *
	 * @param card The card you want to check whether it's legal to play.
	 * @return Whether the specified card is legal to play.
	 */
	bool isLegalToPlay(Card* card);

	/**
	 * Call this function when someone needs to play a card. The played card
	 * replaces the "previous played card", and the original "previous played
	 * card" becomes a used card at the same time.
	 * <p>
	 * NOTE: Before calling this function, you must call isLegalToPlay(Card*)
	 * function at first to check whether the specified card is legal to play.
	 * This function will play the card directly without checking the legality.
	 *
	 * @param who   Who plays a card. Must be one of the following values:
	 *              PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
	 * @param index Play which card. Pass the corresponding card's index of the
	 *              specified player's hand cards.
	 * @param color Optional, available when the card to play is a wild card.
	 *              Pass the specified following legal color.
	 * @return Reference of the played card.
	 */
	Card* play(int who, int index, Color color = BLACK);

private:
	/**
	 * Card table.
	 */
	Card table[108];

	/**
	 * Card back image resource.
	 */
	cv::Mat backImage;

	/**
	 * Difficulty button image resource (EASY).
	 */
	cv::Mat easyImage;

	/**
	 * Difficulty button image resource (HARD).
	 */
	cv::Mat hardImage;

	/**
	 * Background image resource.
	 */
	cv::Mat background;

	/**
	 * Image resources for wild cards.
	 */
	cv::Mat wildImage[5];

	/**
	 * Image resources for wild +4 cards.
	 */
	cv::Mat wildDraw4Image[5];

	/**
	 * Card deck (ready to use).
	 */
	std::vector<Card*> deck;

	/**
	 * Used cards.
	 */
	std::vector<Card*> used;

	/**
	 * Recent played cards.
	 */
	std::vector<Card*> recent;

	/**
	 * Everyone's hand cards.
	 */
	std::vector<Card*> hand[4];

	/**
	 * Singleton, hide default constructor.
	 */
	Uno();

}; // Uno Class

#endif // __UNO_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F