////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include "Uno.h"
#include <ctime>
#include <string>
#include <vector>
#include <cstdlib>
#include <opencv2/highgui.hpp>

using namespace cv;
using namespace std;

static const string BROKEN_IMAGE_RESOURCES_EXCEPTION =
"One or more image resources are broken. Re-install this application.";

/* -------------------- Method Definitions for Card Class ------------------- */

/**
 * Default constructor.
 */
Card::Card() {
	name = "";
	image = Mat();
	color = BLACK;
	content = WILD;
	darkImg = Mat();
	wildColor = BLACK;
	order = (color << 8) + content;
} // Card() (Class Constructor)

/**
 * Constructor. Provide parameters for an Uno card and create its instance.
 */
Card::Card(Mat image, Mat darkImg, Color color, Content content, string name) {
	this->name = name;
	this->image = image;
	this->color = color;
	this->content = content;
	this->darkImg = darkImg;
	this->wildColor = BLACK;
	this->order = (color << 8) + content;
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
 */
const Mat& Card::getImage() {
	return image;
} // getImage()

/**
 * @return Card's dark image resource.
 */
const Mat& Card::getDarkImg() {
	return darkImg;
} // getDarkImg()

/**
 * @return Card's color.
 */
Color Card::getColor() {
	return color;
} // getColor()

/**
 * Valid only when this is a wild card. Get the specified following legal
 * color by the player who played this wild card.
 *
 * @return Card's wild color.
 */
Color Card::getWildColor() {
	return wildColor;
} // getWildColor()

/**
 * @return Card's content.
 */
Content Card::getContent() {
	return content;
} // getContent()

/**
 * @return Card's name.
 */
const string& Card::getName() {
	return name;
} // getName()

/**
 * @return Whether the card is an action card.
 */
bool Card::isAction() {
	return content == DRAW2 || content == SKIP || content == REV;
} // isAction()

/**
 * @return Whether the card is a wild card.
 */
bool Card::isWild() {
	return content == WILD || content == WILD_DRAW4;
} // isWild()

/**
 * @return Whether the card is a zero card.
 */
bool Card::isZero() {
	return content == NUM0;
} // isZero()

/**
 * @return Whether the card is a non-zero number card.
 */
bool Card::isNonZeroNumber() {
	return content == NUM1 || content == NUM2 || content == NUM3
		|| content == NUM4 || content == NUM5 || content == NUM6
		|| content == NUM7 || content == NUM8 || content == NUM9;
} // isNonZeroNumber()

/* ------------------ Method Definitions for Player Class ------------------ */

/**
 * Default constructor.
 */
Player::Player() {
	// NOP (Only change accessibility to private)
} // Player() (Class Constructor)

/**
 * @return This player's all hand cards.
 */
const vector<Card*>& Player::getHandCards() {
	return handCards;
} // getHandCards()

/**
 * @return This player's recent played card, or null if this player drew one
 *         or more cards in its previous action.
 */
Card* Player::getRecent() {
	return recent;
} // getRecent()

/* -------------------- Method Definitions for Uno Class -------------------- */

/**
 * Singleton, hide default constructor.
 */
Uno::Uno() {
	int i;
	bool broken;
	Mat br[54], dk[54];

	// Load background image resources
	bgCounter = imread("resource/bg_counter.png");
	bgClockwise = imread("resource/bg_clockwise.png");
	broken = bgCounter.empty();
	broken = broken || bgCounter.rows != 720;
	broken = broken || bgCounter.cols != 1280;
	broken = broken || bgClockwise.empty();
	broken = broken || bgClockwise.rows != 720;
	broken = broken || bgClockwise.cols != 1280;
	if (broken) {
		// Create blank backgrounds when background resources are invalid
		bgCounter = bgClockwise = Mat::zeros(720, 1280, CV_8UC3);
	} // if (broken)

	// Load card back image resource
	backImage = imread("resource/back.png");
	broken = backImage.empty();
	broken = broken || backImage.cols != 121;
	broken = broken || backImage.rows != 181;
	if (broken) {
		throw BROKEN_IMAGE_RESOURCES_EXCEPTION;
	} // if (broken)

	// Load difficulty image resources
	easyImage = imread("resource/lv_easy.png");
	hardImage = imread("resource/lv_hard.png");
	broken = easyImage.empty();
	broken = broken || easyImage.cols != 121;
	broken = broken || easyImage.rows != 181;
	broken = broken || hardImage.empty();
	broken = broken || hardImage.cols != 121;
	broken = broken || hardImage.rows != 181;
	if (broken) {
		throw BROKEN_IMAGE_RESOURCES_EXCEPTION;
	} // if (broken)

	// Load cards' front image resources
	br[0] = imread("resource/front_r0.png");
	br[1] = imread("resource/front_r1.png");
	br[2] = imread("resource/front_r2.png");
	br[3] = imread("resource/front_r3.png");
	br[4] = imread("resource/front_r4.png");
	br[5] = imread("resource/front_r5.png");
	br[6] = imread("resource/front_r6.png");
	br[7] = imread("resource/front_r7.png");
	br[8] = imread("resource/front_r8.png");
	br[9] = imread("resource/front_r9.png");
	br[10] = imread("resource/front_r+.png");
	br[11] = imread("resource/front_r@.png");
	br[12] = imread("resource/front_r$.png");
	br[13] = imread("resource/front_b0.png");
	br[14] = imread("resource/front_b1.png");
	br[15] = imread("resource/front_b2.png");
	br[16] = imread("resource/front_b3.png");
	br[17] = imread("resource/front_b4.png");
	br[18] = imread("resource/front_b5.png");
	br[19] = imread("resource/front_b6.png");
	br[20] = imread("resource/front_b7.png");
	br[21] = imread("resource/front_b8.png");
	br[22] = imread("resource/front_b9.png");
	br[23] = imread("resource/front_b+.png");
	br[24] = imread("resource/front_b@.png");
	br[25] = imread("resource/front_b$.png");
	br[26] = imread("resource/front_g0.png");
	br[27] = imread("resource/front_g1.png");
	br[28] = imread("resource/front_g2.png");
	br[29] = imread("resource/front_g3.png");
	br[30] = imread("resource/front_g4.png");
	br[31] = imread("resource/front_g5.png");
	br[32] = imread("resource/front_g6.png");
	br[33] = imread("resource/front_g7.png");
	br[34] = imread("resource/front_g8.png");
	br[35] = imread("resource/front_g9.png");
	br[36] = imread("resource/front_g+.png");
	br[37] = imread("resource/front_g@.png");
	br[38] = imread("resource/front_g$.png");
	br[39] = imread("resource/front_y0.png");
	br[40] = imread("resource/front_y1.png");
	br[41] = imread("resource/front_y2.png");
	br[42] = imread("resource/front_y3.png");
	br[43] = imread("resource/front_y4.png");
	br[44] = imread("resource/front_y5.png");
	br[45] = imread("resource/front_y6.png");
	br[46] = imread("resource/front_y7.png");
	br[47] = imread("resource/front_y8.png");
	br[48] = imread("resource/front_y9.png");
	br[49] = imread("resource/front_y+.png");
	br[50] = imread("resource/front_y@.png");
	br[51] = imread("resource/front_y$.png");
	br[52] = imread("resource/front_kw.png");
	br[53] = imread("resource/front_kw+.png");
	dk[0] = imread("resource/dark_r0.png");
	dk[1] = imread("resource/dark_r1.png");
	dk[2] = imread("resource/dark_r2.png");
	dk[3] = imread("resource/dark_r3.png");
	dk[4] = imread("resource/dark_r4.png");
	dk[5] = imread("resource/dark_r5.png");
	dk[6] = imread("resource/dark_r6.png");
	dk[7] = imread("resource/dark_r7.png");
	dk[8] = imread("resource/dark_r8.png");
	dk[9] = imread("resource/dark_r9.png");
	dk[10] = imread("resource/dark_r+.png");
	dk[11] = imread("resource/dark_r@.png");
	dk[12] = imread("resource/dark_r$.png");
	dk[13] = imread("resource/dark_b0.png");
	dk[14] = imread("resource/dark_b1.png");
	dk[15] = imread("resource/dark_b2.png");
	dk[16] = imread("resource/dark_b3.png");
	dk[17] = imread("resource/dark_b4.png");
	dk[18] = imread("resource/dark_b5.png");
	dk[19] = imread("resource/dark_b6.png");
	dk[20] = imread("resource/dark_b7.png");
	dk[21] = imread("resource/dark_b8.png");
	dk[22] = imread("resource/dark_b9.png");
	dk[23] = imread("resource/dark_b+.png");
	dk[24] = imread("resource/dark_b@.png");
	dk[25] = imread("resource/dark_b$.png");
	dk[26] = imread("resource/dark_g0.png");
	dk[27] = imread("resource/dark_g1.png");
	dk[28] = imread("resource/dark_g2.png");
	dk[29] = imread("resource/dark_g3.png");
	dk[30] = imread("resource/dark_g4.png");
	dk[31] = imread("resource/dark_g5.png");
	dk[32] = imread("resource/dark_g6.png");
	dk[33] = imread("resource/dark_g7.png");
	dk[34] = imread("resource/dark_g8.png");
	dk[35] = imread("resource/dark_g9.png");
	dk[36] = imread("resource/dark_g+.png");
	dk[37] = imread("resource/dark_g@.png");
	dk[38] = imread("resource/dark_g$.png");
	dk[39] = imread("resource/dark_y0.png");
	dk[40] = imread("resource/dark_y1.png");
	dk[41] = imread("resource/dark_y2.png");
	dk[42] = imread("resource/dark_y3.png");
	dk[43] = imread("resource/dark_y4.png");
	dk[44] = imread("resource/dark_y5.png");
	dk[45] = imread("resource/dark_y6.png");
	dk[46] = imread("resource/dark_y7.png");
	dk[47] = imread("resource/dark_y8.png");
	dk[48] = imread("resource/dark_y9.png");
	dk[49] = imread("resource/dark_y+.png");
	dk[50] = imread("resource/dark_y@.png");
	dk[51] = imread("resource/dark_y$.png");
	dk[52] = imread("resource/dark_kw.png");
	dk[53] = imread("resource/dark_kw+.png");
	for (i = 0; i < 54; ++i) {
		broken = br[i].empty();
		broken = broken || br[i].cols != 121;
		broken = broken || br[i].rows != 181;
		broken = broken || dk[i].empty();
		broken = broken || dk[i].cols != 121;
		broken = broken || dk[i].rows != 181;
		if (broken) {
			throw BROKEN_IMAGE_RESOURCES_EXCEPTION;
		} // if (broken)
	} // for (i = 0; i < 54; ++i)

	// Load wild & wild +4 image resources
	wildImage[0] = br[52];
	wildImage[1] = imread("resource/front_rw.png");
	wildImage[2] = imread("resource/front_bw.png");
	wildImage[3] = imread("resource/front_gw.png");
	wildImage[4] = imread("resource/front_yw.png");
	wildDraw4Image[0] = br[53];
	wildDraw4Image[1] = imread("resource/front_rw+.png");
	wildDraw4Image[2] = imread("resource/front_bw+.png");
	wildDraw4Image[3] = imread("resource/front_gw+.png");
	wildDraw4Image[4] = imread("resource/front_yw+.png");
	for (i = 1; i < 5; ++i) {
		broken = wildImage[i].empty();
		broken = broken || wildImage[i].cols != 121;
		broken = broken || wildImage[i].rows != 181;
		broken = broken || wildDraw4Image[i].empty();
		broken = broken || wildDraw4Image[i].cols != 121;
		broken = broken || wildDraw4Image[i].rows != 181;
		if (broken) {
			throw BROKEN_IMAGE_RESOURCES_EXCEPTION;
		} // if (broken)
	} // for (i = 1; i < 5; ++i)

	// Generate card table
	table[0] = Card(br[0], dk[0], RED, NUM0, "Red 0");
	table[1] = Card(br[1], dk[1], RED, NUM1, "Red 1");
	table[2] = Card(br[2], dk[2], RED, NUM2, "Red 2");
	table[3] = Card(br[3], dk[3], RED, NUM3, "Red 3");
	table[4] = Card(br[4], dk[4], RED, NUM4, "Red 4");
	table[5] = Card(br[5], dk[5], RED, NUM5, "Red 5");
	table[6] = Card(br[6], dk[6], RED, NUM6, "Red 6");
	table[7] = Card(br[7], dk[7], RED, NUM7, "Red 7");
	table[8] = Card(br[8], dk[8], RED, NUM8, "Red 8");
	table[9] = Card(br[9], dk[9], RED, NUM9, "Red 9");
	table[10] = Card(br[10], dk[10], RED, DRAW2, "Red +2");
	table[11] = Card(br[11], dk[11], RED, SKIP, "Red Skip");
	table[12] = Card(br[12], dk[12], RED, REV, "Red Reverse");
	table[13] = Card(br[13], dk[13], BLUE, NUM0, "Blue 0");
	table[14] = Card(br[14], dk[14], BLUE, NUM1, "Blue 1");
	table[15] = Card(br[15], dk[15], BLUE, NUM2, "Blue 2");
	table[16] = Card(br[16], dk[16], BLUE, NUM3, "Blue 3");
	table[17] = Card(br[17], dk[17], BLUE, NUM4, "Blue 4");
	table[18] = Card(br[18], dk[18], BLUE, NUM5, "Blue 5");
	table[19] = Card(br[19], dk[19], BLUE, NUM6, "Blue 6");
	table[20] = Card(br[20], dk[20], BLUE, NUM7, "Blue 7");
	table[21] = Card(br[21], dk[21], BLUE, NUM8, "Blue 8");
	table[22] = Card(br[22], dk[22], BLUE, NUM9, "Blue 9");
	table[23] = Card(br[23], dk[23], BLUE, DRAW2, "Blue +2");
	table[24] = Card(br[24], dk[24], BLUE, SKIP, "Blue Skip");
	table[25] = Card(br[25], dk[25], BLUE, REV, "Blue Reverse");
	table[26] = Card(br[26], dk[26], GREEN, NUM0, "Green 0");
	table[27] = Card(br[27], dk[27], GREEN, NUM1, "Green 1");
	table[28] = Card(br[28], dk[28], GREEN, NUM2, "Green 2");
	table[29] = Card(br[29], dk[29], GREEN, NUM3, "Green 3");
	table[30] = Card(br[30], dk[30], GREEN, NUM4, "Green 4");
	table[31] = Card(br[31], dk[31], GREEN, NUM5, "Green 5");
	table[32] = Card(br[32], dk[32], GREEN, NUM6, "Green 6");
	table[33] = Card(br[33], dk[33], GREEN, NUM7, "Green 7");
	table[34] = Card(br[34], dk[34], GREEN, NUM8, "Green 8");
	table[35] = Card(br[35], dk[35], GREEN, NUM9, "Green 9");
	table[36] = Card(br[36], dk[36], GREEN, DRAW2, "Green +2");
	table[37] = Card(br[37], dk[37], GREEN, SKIP, "Green Skip");
	table[38] = Card(br[38], dk[38], GREEN, REV, "Green Reverse");
	table[39] = Card(br[39], dk[39], YELLOW, NUM0, "Yellow 0");
	table[40] = Card(br[40], dk[40], YELLOW, NUM1, "Yellow 1");
	table[41] = Card(br[41], dk[41], YELLOW, NUM2, "Yellow 2");
	table[42] = Card(br[42], dk[42], YELLOW, NUM3, "Yellow 3");
	table[43] = Card(br[43], dk[43], YELLOW, NUM4, "Yellow 4");
	table[44] = Card(br[44], dk[44], YELLOW, NUM5, "Yellow 5");
	table[45] = Card(br[45], dk[45], YELLOW, NUM6, "Yellow 6");
	table[46] = Card(br[46], dk[46], YELLOW, NUM7, "Yellow 7");
	table[47] = Card(br[47], dk[47], YELLOW, NUM8, "Yellow 8");
	table[48] = Card(br[48], dk[48], YELLOW, NUM9, "Yellow 9");
	table[49] = Card(br[49], dk[49], YELLOW, DRAW2, "Yellow +2");
	table[50] = Card(br[50], dk[50], YELLOW, SKIP, "Yellow Skip");
	table[51] = Card(br[51], dk[51], YELLOW, REV, "Yellow Reverse");
	table[52] = Card(br[52], dk[52], BLACK, WILD, "Wild");
	table[53] = Card(br[52], dk[52], BLACK, WILD, "Wild");
	table[54] = Card(br[53], dk[53], BLACK, WILD_DRAW4, "Wild +4");
	table[55] = Card(br[1], dk[1], RED, NUM1, "Red 1");
	table[56] = Card(br[2], dk[2], RED, NUM2, "Red 2");
	table[57] = Card(br[3], dk[3], RED, NUM3, "Red 3");
	table[58] = Card(br[4], dk[4], RED, NUM4, "Red 4");
	table[59] = Card(br[5], dk[5], RED, NUM5, "Red 5");
	table[60] = Card(br[6], dk[6], RED, NUM6, "Red 6");
	table[61] = Card(br[7], dk[7], RED, NUM7, "Red 7");
	table[62] = Card(br[8], dk[8], RED, NUM8, "Red 8");
	table[63] = Card(br[9], dk[9], RED, NUM9, "Red 9");
	table[64] = Card(br[10], dk[10], RED, DRAW2, "Red +2");
	table[65] = Card(br[11], dk[11], RED, SKIP, "Red Skip");
	table[66] = Card(br[12], dk[12], RED, REV, "Red Reverse");
	table[67] = Card(br[53], dk[53], BLACK, WILD_DRAW4, "Wild +4");
	table[68] = Card(br[14], dk[14], BLUE, NUM1, "Blue 1");
	table[69] = Card(br[15], dk[15], BLUE, NUM2, "Blue 2");
	table[70] = Card(br[16], dk[16], BLUE, NUM3, "Blue 3");
	table[71] = Card(br[17], dk[17], BLUE, NUM4, "Blue 4");
	table[72] = Card(br[18], dk[18], BLUE, NUM5, "Blue 5");
	table[73] = Card(br[19], dk[19], BLUE, NUM6, "Blue 6");
	table[74] = Card(br[20], dk[20], BLUE, NUM7, "Blue 7");
	table[75] = Card(br[21], dk[21], BLUE, NUM8, "Blue 8");
	table[76] = Card(br[22], dk[22], BLUE, NUM9, "Blue 9");
	table[77] = Card(br[23], dk[23], BLUE, DRAW2, "Blue +2");
	table[78] = Card(br[24], dk[24], BLUE, SKIP, "Blue Skip");
	table[79] = Card(br[25], dk[25], BLUE, REV, "Blue Reverse");
	table[80] = Card(br[53], dk[53], BLACK, WILD_DRAW4, "Wild +4");
	table[81] = Card(br[27], dk[27], GREEN, NUM1, "Green 1");
	table[82] = Card(br[28], dk[28], GREEN, NUM2, "Green 2");
	table[83] = Card(br[29], dk[29], GREEN, NUM3, "Green 3");
	table[84] = Card(br[30], dk[30], GREEN, NUM4, "Green 4");
	table[85] = Card(br[31], dk[31], GREEN, NUM5, "Green 5");
	table[86] = Card(br[32], dk[32], GREEN, NUM6, "Green 6");
	table[87] = Card(br[33], dk[33], GREEN, NUM7, "Green 7");
	table[88] = Card(br[34], dk[34], GREEN, NUM8, "Green 8");
	table[89] = Card(br[35], dk[35], GREEN, NUM9, "Green 9");
	table[90] = Card(br[36], dk[36], GREEN, DRAW2, "Green +2");
	table[91] = Card(br[37], dk[37], GREEN, SKIP, "Green Skip");
	table[92] = Card(br[38], dk[38], GREEN, REV, "Green Reverse");
	table[93] = Card(br[53], dk[53], BLACK, WILD_DRAW4, "Wild +4");
	table[94] = Card(br[40], dk[40], YELLOW, NUM1, "Yellow 1");
	table[95] = Card(br[41], dk[41], YELLOW, NUM2, "Yellow 2");
	table[96] = Card(br[42], dk[42], YELLOW, NUM3, "Yellow 3");
	table[97] = Card(br[43], dk[43], YELLOW, NUM4, "Yellow 4");
	table[98] = Card(br[44], dk[44], YELLOW, NUM5, "Yellow 5");
	table[99] = Card(br[45], dk[45], YELLOW, NUM6, "Yellow 6");
	table[100] = Card(br[46], dk[46], YELLOW, NUM7, "Yellow 7");
	table[101] = Card(br[47], dk[47], YELLOW, NUM8, "Yellow 8");
	table[102] = Card(br[48], dk[48], YELLOW, NUM9, "Yellow 9");
	table[103] = Card(br[49], dk[49], YELLOW, DRAW2, "Yellow +2");
	table[104] = Card(br[50], dk[50], YELLOW, SKIP, "Yellow Skip");
	table[105] = Card(br[51], dk[51], YELLOW, REV, "Yellow Reverse");
	table[106] = Card(br[52], dk[52], BLACK, WILD, "Wild");
	table[107] = Card(br[52], dk[52], BLACK, WILD, "Wild");

	// Generate a random seed based on the current time stamp
	srand((unsigned)time(NULL));
} // Uno() (Class Constructor)

/**
 * @return Reference of our singleton.
 */
Uno* Uno::getInstance() {
	static Uno instance;
	return &instance;
} // getInstance()

/**
 * @return Card back image resource.
 */
const Mat& Uno::getBackImage() {
	return backImage;
} // getBackImage()

/**
 * @return Difficulty button image resource (EASY).
 */
const Mat& Uno::getEasyImage() {
	return easyImage;
} // getEasyImage()

/**
 * @return Difficulty button image resource (HARD).
 */
const Mat& Uno::getHardImage() {
	return hardImage;
} // getHardImage()

/**
 * @return Background image resource in current direction.
 */
const Mat& Uno::getBackground() {
	return direction == DIR_RIGHT ? bgCounter : bgClockwise;
} // getBackground()

/**
 * When a player played a wild card and specified a following legal color,
 * get the corresponding color-filled image here, and show it in recent
 * card area.
 *
 * @param color The wild image with which color filled you want to get.
 * @return Corresponding color-filled image.
 */
const Mat& Uno::getColoredWildImage(Color color) {
	return wildImage[color];
} // getColoredWildImage()

/**
 * When a player played a wild +4 card and specified a following legal
 * color, get the corresponding color-filled image here, and show it in
 * recent card area.
 *
 * @param color The wild +4 image with which color filled you want to get.
 * @return Corresponding color-filled image.
 */
const Mat& Uno::getColoredWildDraw4Image(Color color) {
	return wildDraw4Image[color];
} // getColoredWildDraw4Image()

/**
 * Get current action sequence. You can get the next player by calculating
 * (now + this->getDirection()) % 4.
 *
 * @return Current action sequence. DIR_LEFT for clockwise,
 *         or DIR_RIGHT for counter-clockwise.
 */
int Uno::getDirection() {
	return direction;
} // getDirection()

/**
 * Switch current action sequence.
 *
 * @return Switched action sequence. DIR_LEFT for clockwise,
 *         or DIR_RIGHT for counter-clockwise.
 */
int Uno::switchDirection() {
	direction = 4 - direction;
	return direction;
} // switchDirection()

/**
 * @param who Get which player's instance. Must be one of the following
 *        values: PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
 * @return Specified player's instance.
 */
Player* Uno::getPlayer(int who) {
	if (who >= 0 && who < 4) {
		return &player[who];
	} // if (who >= 0 && who < 4)
	else {
		throw "Illegal parameter";
	} // else
} // getPlayer()

/**
 * @return How many cards in deck (haven't been used yet).
 */
int Uno::getDeckCount() {
	return (int)deck.size();
} // getDeckCount()

/**
 * @return How many cards have been used.
 */
int Uno::getUsedCount() {
	return (int)(used.size() + recent.size());
} // getUsedCount()

/**
 * @return Recent played cards.
 */
const vector<Card*>& Uno::getRecent() {
	return recent;
} // getRecent()

/**
 * @param whom Get whose hand cards. Must be one of the following values:
 *             PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
 * @return Specified player's all hand cards.
 * @deprecated Use getPlayer(whom)->getHandCards() instead.
 */
[[deprecated]] const vector<Card*>& Uno::getHandCardsOf(int whom) {
	return getPlayer(whom)->getHandCards();
} // getHandCardsOf()

/**
 * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
 * then determine our start card.
 */
void Uno::start() {
	Card* card;
	int i, index, size;
	vector<Card*> allCards;
	vector<Card>::iterator it;

	// Reset direction
	direction = DIR_LEFT;

	// Clear card deck, used card deck, recent played cards,
	// and everyone's hand cards
	deck.clear();
	used.clear();
	recent.clear();
	player[0].handCards.clear();
	player[1].handCards.clear();
	player[2].handCards.clear();
	player[3].handCards.clear();

	// Generate a temporary sequenced card deck
	for (i = 0; i < 108; ++i) {
		allCards.push_back(&table[i]);
	} // for (i = 0; i < 108; ++i)

	// Keep picking a card from the temporary card deck randomly,
	// until all cards are picked to the real card deck (shuffle cards)
	size = (int)allCards.size();
	while (size > 0) {
		index = rand() % size;
		deck.push_back(allCards.at(index));
		allCards.erase(allCards.begin() + index);
		--size;
	} // while (size > 0)

	// Let everyone draw 7 cards
	for (i = 0; i < 28; ++i) {
		draw(i % 4);
	} // for (i = 0; i < 28; ++i)

	// Determine a start card as the previous played card
	do {
		card = deck.back();
		deck.pop_back();
		if (card->isWild()) {
			// Start card cannot be a wild card, so return it
			// to the bottom of card deck and pick another card
			deck.insert(deck.begin(), card);
		} // if (card->isWild())
		else {
			// Any non-wild card can be start card
			// Start card determined
			recent.push_back(card);
		} // else
	} while (recent.empty());
} // start()

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
Card* Uno::draw(int who) {
	Card* card;
	Player* player;
	int index, size;
	vector<Card*>* hand;
	vector<Card*>::iterator i;

	card = NULL;
	player = getPlayer(who);
	hand = &player->handCards;
	if (hand->size() < MAX_HOLD_CARDS) {
		// Draw a card from card deck, and put it to an appropriate position
		card = deck.back();
		deck.pop_back();
		for (i = hand->begin(); i != hand->end(); ++i) {
			if (*(*i) > *card) {
				// Found an appropriate position to insert the new card,
				// which keeps the player's hand cards sequenced
				break;
			} // if (*(*i) > *card)
		} // for (i = hand->begin(); i != hand->end(); ++i)

		hand->insert(i, card);
		player->recent = NULL;
		if (deck.empty()) {
			// Re-use the used cards when there are no more cards in deck
			size = (int)used.size();
			while (size > 0) {
				index = rand() % size;
				deck.push_back(used.at(index));
				used.erase(used.begin() + index);
				--size;
			} // while (size > 0)
		} // if (deck.empty())
	} // if (hand->size() < MAX_HOLD_CARDS)

	return card;
} // draw()

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
Color Uno::bestColorFor(int whom) {
	Color best = RED;
	int score[5] = { 0, 0, 0, 0, 0 };

	for (Card* card : getPlayer(whom)->handCards) {
		if (card->isZero()) {
			++score[card->color];
		} // if (card->isZero())
		else if (card->isAction()) {
			score[card->color] += 3;
		} // else if (card->isAction())
		else {
			score[card->color] += 2;
		} // else
	} // for (Card* card : getPlayer(whom)->handCards)

	// default to red, when only wild cards in hand,
	// function will return Color::RED
	if (score[BLUE] > score[best]) {
		best = BLUE;
	} // if (score[BLUE] > score[best]

	if (score[GREEN] > score[best]) {
		best = GREEN;
	} // if (score[GREEN] > score[best])

	if (score[YELLOW] > score[best]) {
		best = YELLOW;
	} // if (score[YELLOW] > score[best])

	return best;
} // bestColorFor()

/**
 * Check whether the specified card is legal to play. It's legal only when
 * it's wild, or it has the same color/content to the previous played card.
 *
 * @param card The card you want to check whether it's legal to play.
 * @return Whether the specified card is legal to play.
 */
bool Uno::isLegalToPlay(Card* card) {
	bool result;
	Card* previous;
	Color prevColor;

	if (card == NULL || recent.empty()) {
		// NULL Pointer
		result = false;
	} // if (card == NULL || recent.empty())
	else if (card->isWild()) {
		// Wild cards: LEGAL
		result = true;
	} // else if (card->isWild())
	else {
		// Same content to previous: LEGAL
		// Same color to previous: LEGAL
		// Other cards: ILLEGAL
		previous = recent.back();
		if (card->content == previous->content) {
			result = true;
		} // if (card->content == previous->content)
		else {
			if (previous->isWild()) {
				prevColor = previous->wildColor;
			} // if (previous->isWild())
			else {
				prevColor = previous->color;
			} // else

			result = (card->color == prevColor);
		} // else
	} // else

	return result;
} // isLegalToPlay()

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
Card* Uno::play(int who, int index, Color color) {
	Card* card;
	Player* player;
	vector<Card*>* hand;

	card = NULL;
	player = getPlayer(who);
	hand = &player->handCards;
	if (index < hand->size()) {
		card = hand->at(index);
		hand->erase(hand->begin() + index);
		if (card->isWild()) {
			card->wildColor = color;
		} // if (card->isWild())

		player->recent = card;
		recent.push_back(card);
		if (recent.size() > 5) {
			used.push_back(recent.front());
			recent.erase(recent.begin());
		} // if (recent.size() > 5)
	} // if (index < hand->size())

	return card;
} // play()

// E.O.F