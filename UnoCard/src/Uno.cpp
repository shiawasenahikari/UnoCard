////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
// COPYRIGHT HIKARI TOYAMA, 1992-2021. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#include <ctime>
#include <Uno.h>
#include <string>
#include <vector>
#include <Card.h>
#include <cstdlib>
#include <Color.h>
#include <iostream>
#include <Player.h>
#include <Content.h>
#include <opencv2/highgui.hpp>

static const char* BROKEN_IMAGE_RESOURCES_EXCEPTION =
"One or more image resources are broken. Re-install this application.";

/**
 * Singleton, hide default constructor.
 */
Uno::Uno() {
	int i, done, total;
	cv::Mat br[54], dk[54];

	// Preparations
	done = 0;
	total = 124;
	std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;

	// Load background image resources
	bgWelcome = cv::imread("resource/bg_welcome.png");
	bgCounter = cv::imread("resource/bg_counter.png");
	bgClockwise = cv::imread("resource/bg_clockwise.png");
	if (bgWelcome.empty() ||
		bgWelcome.rows != 720 ||
		bgWelcome.cols != 1280 ||
		bgCounter.empty() ||
		bgCounter.rows != 720 ||
		bgCounter.cols != 1280 ||
		bgClockwise.empty() ||
		bgClockwise.rows != 720 ||
		bgClockwise.cols != 1280) {
		std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
		exit(1);
	} // if (bgWelcome.empty() || ...)
	else {
		done += 3;
		std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
	} // else

	// Load card back image resource
	backImage = cv::imread("resource/back.png");
	if (backImage.empty() ||
		backImage.rows != 181 ||
		backImage.cols != 121) {
		std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
		exit(1);
	} // if (backImage.empty() || ...)
	else {
		++done;
		std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
	} // else

	// Load difficulty image resources
	easyImage = cv::imread("resource/lv_easy.png");
	hardImage = cv::imread("resource/lv_hard.png");
	easyImage_d = cv::imread("resource/lv_easy_dark.png");
	hardImage_d = cv::imread("resource/lv_hard_dark.png");
	if (easyImage.empty() ||
		easyImage.rows != 181 ||
		easyImage.cols != 121 ||
		hardImage.empty() ||
		hardImage.rows != 181 ||
		hardImage.cols != 121 ||
		easyImage_d.empty() ||
		easyImage_d.rows != 181 ||
		easyImage_d.cols != 121 ||
		hardImage_d.empty() ||
		hardImage_d.rows != 181 ||
		hardImage_d.cols != 121) {
		std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
		exit(1);
	} // if (easyImage.empty() || ...)
	else {
		done += 4;
		std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
	} // else

	// Load cards' front image resources
	br[0] = cv::imread("resource/front_r0.png");
	br[1] = cv::imread("resource/front_r1.png");
	br[2] = cv::imread("resource/front_r2.png");
	br[3] = cv::imread("resource/front_r3.png");
	br[4] = cv::imread("resource/front_r4.png");
	br[5] = cv::imread("resource/front_r5.png");
	br[6] = cv::imread("resource/front_r6.png");
	br[7] = cv::imread("resource/front_r7.png");
	br[8] = cv::imread("resource/front_r8.png");
	br[9] = cv::imread("resource/front_r9.png");
	br[10] = cv::imread("resource/front_r+.png");
	br[11] = cv::imread("resource/front_r@.png");
	br[12] = cv::imread("resource/front_r$.png");
	br[13] = cv::imread("resource/front_b0.png");
	br[14] = cv::imread("resource/front_b1.png");
	br[15] = cv::imread("resource/front_b2.png");
	br[16] = cv::imread("resource/front_b3.png");
	br[17] = cv::imread("resource/front_b4.png");
	br[18] = cv::imread("resource/front_b5.png");
	br[19] = cv::imread("resource/front_b6.png");
	br[20] = cv::imread("resource/front_b7.png");
	br[21] = cv::imread("resource/front_b8.png");
	br[22] = cv::imread("resource/front_b9.png");
	br[23] = cv::imread("resource/front_b+.png");
	br[24] = cv::imread("resource/front_b@.png");
	br[25] = cv::imread("resource/front_b$.png");
	br[26] = cv::imread("resource/front_g0.png");
	br[27] = cv::imread("resource/front_g1.png");
	br[28] = cv::imread("resource/front_g2.png");
	br[29] = cv::imread("resource/front_g3.png");
	br[30] = cv::imread("resource/front_g4.png");
	br[31] = cv::imread("resource/front_g5.png");
	br[32] = cv::imread("resource/front_g6.png");
	br[33] = cv::imread("resource/front_g7.png");
	br[34] = cv::imread("resource/front_g8.png");
	br[35] = cv::imread("resource/front_g9.png");
	br[36] = cv::imread("resource/front_g+.png");
	br[37] = cv::imread("resource/front_g@.png");
	br[38] = cv::imread("resource/front_g$.png");
	br[39] = cv::imread("resource/front_y0.png");
	br[40] = cv::imread("resource/front_y1.png");
	br[41] = cv::imread("resource/front_y2.png");
	br[42] = cv::imread("resource/front_y3.png");
	br[43] = cv::imread("resource/front_y4.png");
	br[44] = cv::imread("resource/front_y5.png");
	br[45] = cv::imread("resource/front_y6.png");
	br[46] = cv::imread("resource/front_y7.png");
	br[47] = cv::imread("resource/front_y8.png");
	br[48] = cv::imread("resource/front_y9.png");
	br[49] = cv::imread("resource/front_y+.png");
	br[50] = cv::imread("resource/front_y@.png");
	br[51] = cv::imread("resource/front_y$.png");
	br[52] = cv::imread("resource/front_kw.png");
	br[53] = cv::imread("resource/front_kw+.png");
	dk[0] = cv::imread("resource/dark_r0.png");
	dk[1] = cv::imread("resource/dark_r1.png");
	dk[2] = cv::imread("resource/dark_r2.png");
	dk[3] = cv::imread("resource/dark_r3.png");
	dk[4] = cv::imread("resource/dark_r4.png");
	dk[5] = cv::imread("resource/dark_r5.png");
	dk[6] = cv::imread("resource/dark_r6.png");
	dk[7] = cv::imread("resource/dark_r7.png");
	dk[8] = cv::imread("resource/dark_r8.png");
	dk[9] = cv::imread("resource/dark_r9.png");
	dk[10] = cv::imread("resource/dark_r+.png");
	dk[11] = cv::imread("resource/dark_r@.png");
	dk[12] = cv::imread("resource/dark_r$.png");
	dk[13] = cv::imread("resource/dark_b0.png");
	dk[14] = cv::imread("resource/dark_b1.png");
	dk[15] = cv::imread("resource/dark_b2.png");
	dk[16] = cv::imread("resource/dark_b3.png");
	dk[17] = cv::imread("resource/dark_b4.png");
	dk[18] = cv::imread("resource/dark_b5.png");
	dk[19] = cv::imread("resource/dark_b6.png");
	dk[20] = cv::imread("resource/dark_b7.png");
	dk[21] = cv::imread("resource/dark_b8.png");
	dk[22] = cv::imread("resource/dark_b9.png");
	dk[23] = cv::imread("resource/dark_b+.png");
	dk[24] = cv::imread("resource/dark_b@.png");
	dk[25] = cv::imread("resource/dark_b$.png");
	dk[26] = cv::imread("resource/dark_g0.png");
	dk[27] = cv::imread("resource/dark_g1.png");
	dk[28] = cv::imread("resource/dark_g2.png");
	dk[29] = cv::imread("resource/dark_g3.png");
	dk[30] = cv::imread("resource/dark_g4.png");
	dk[31] = cv::imread("resource/dark_g5.png");
	dk[32] = cv::imread("resource/dark_g6.png");
	dk[33] = cv::imread("resource/dark_g7.png");
	dk[34] = cv::imread("resource/dark_g8.png");
	dk[35] = cv::imread("resource/dark_g9.png");
	dk[36] = cv::imread("resource/dark_g+.png");
	dk[37] = cv::imread("resource/dark_g@.png");
	dk[38] = cv::imread("resource/dark_g$.png");
	dk[39] = cv::imread("resource/dark_y0.png");
	dk[40] = cv::imread("resource/dark_y1.png");
	dk[41] = cv::imread("resource/dark_y2.png");
	dk[42] = cv::imread("resource/dark_y3.png");
	dk[43] = cv::imread("resource/dark_y4.png");
	dk[44] = cv::imread("resource/dark_y5.png");
	dk[45] = cv::imread("resource/dark_y6.png");
	dk[46] = cv::imread("resource/dark_y7.png");
	dk[47] = cv::imread("resource/dark_y8.png");
	dk[48] = cv::imread("resource/dark_y9.png");
	dk[49] = cv::imread("resource/dark_y+.png");
	dk[50] = cv::imread("resource/dark_y@.png");
	dk[51] = cv::imread("resource/dark_y$.png");
	dk[52] = cv::imread("resource/dark_kw.png");
	dk[53] = cv::imread("resource/dark_kw+.png");
	for (i = 0; i < 54; ++i) {
		if (br[i].empty() || br[i].rows != 181 || br[i].cols != 121 ||
			dk[i].empty() || dk[i].rows != 181 || dk[i].cols != 121) {
			std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
			exit(1);
		} // if (br[i].empty() || ...)
		else {
			done += 2;
			std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
		} // else
	} // for (i = 0; i < 54; ++i)

	// Load wild & wild +4 image resources
	wildImage[0] = br[52];
	wildImage[1] = cv::imread("resource/front_rw.png");
	wildImage[2] = cv::imread("resource/front_bw.png");
	wildImage[3] = cv::imread("resource/front_gw.png");
	wildImage[4] = cv::imread("resource/front_yw.png");
	wildDraw4Image[0] = br[53];
	wildDraw4Image[1] = cv::imread("resource/front_rw+.png");
	wildDraw4Image[2] = cv::imread("resource/front_bw+.png");
	wildDraw4Image[3] = cv::imread("resource/front_gw+.png");
	wildDraw4Image[4] = cv::imread("resource/front_yw+.png");
	for (i = 1; i < 5; ++i) {
		if (wildImage[i].empty() ||
			wildImage[i].rows != 181 ||
			wildImage[i].cols != 121 ||
			wildDraw4Image[i].empty() ||
			wildDraw4Image[i].rows != 181 ||
			wildDraw4Image[i].cols != 121) {
			std::cout << BROKEN_IMAGE_RESOURCES_EXCEPTION << std::endl;
			exit(1);
		} // if (wildImage[i].empty() || ...)
		else {
			done += 2;
			std::cout << "Loading... (" << 100 * done / total << "%)" << std::endl;
		} // else
	} // for (i = 1; i < 5; ++i)

	// Generate card table
	table.push_back(Card(br[0], dk[0], RED, NUM0, "Red 0"));
	table.push_back(Card(br[1], dk[1], RED, NUM1, "Red 1"));
	table.push_back(Card(br[2], dk[2], RED, NUM2, "Red 2"));
	table.push_back(Card(br[3], dk[3], RED, NUM3, "Red 3"));
	table.push_back(Card(br[4], dk[4], RED, NUM4, "Red 4"));
	table.push_back(Card(br[5], dk[5], RED, NUM5, "Red 5"));
	table.push_back(Card(br[6], dk[6], RED, NUM6, "Red 6"));
	table.push_back(Card(br[7], dk[7], RED, NUM7, "Red 7"));
	table.push_back(Card(br[8], dk[8], RED, NUM8, "Red 8"));
	table.push_back(Card(br[9], dk[9], RED, NUM9, "Red 9"));
	table.push_back(Card(br[10], dk[10], RED, DRAW2, "Red +2"));
	table.push_back(Card(br[11], dk[11], RED, SKIP, "Red Skip"));
	table.push_back(Card(br[12], dk[12], RED, REV, "Red Reverse"));
	table.push_back(Card(br[13], dk[13], BLUE, NUM0, "Blue 0"));
	table.push_back(Card(br[14], dk[14], BLUE, NUM1, "Blue 1"));
	table.push_back(Card(br[15], dk[15], BLUE, NUM2, "Blue 2"));
	table.push_back(Card(br[16], dk[16], BLUE, NUM3, "Blue 3"));
	table.push_back(Card(br[17], dk[17], BLUE, NUM4, "Blue 4"));
	table.push_back(Card(br[18], dk[18], BLUE, NUM5, "Blue 5"));
	table.push_back(Card(br[19], dk[19], BLUE, NUM6, "Blue 6"));
	table.push_back(Card(br[20], dk[20], BLUE, NUM7, "Blue 7"));
	table.push_back(Card(br[21], dk[21], BLUE, NUM8, "Blue 8"));
	table.push_back(Card(br[22], dk[22], BLUE, NUM9, "Blue 9"));
	table.push_back(Card(br[23], dk[23], BLUE, DRAW2, "Blue +2"));
	table.push_back(Card(br[24], dk[24], BLUE, SKIP, "Blue Skip"));
	table.push_back(Card(br[25], dk[25], BLUE, REV, "Blue Reverse"));
	table.push_back(Card(br[26], dk[26], GREEN, NUM0, "Green 0"));
	table.push_back(Card(br[27], dk[27], GREEN, NUM1, "Green 1"));
	table.push_back(Card(br[28], dk[28], GREEN, NUM2, "Green 2"));
	table.push_back(Card(br[29], dk[29], GREEN, NUM3, "Green 3"));
	table.push_back(Card(br[30], dk[30], GREEN, NUM4, "Green 4"));
	table.push_back(Card(br[31], dk[31], GREEN, NUM5, "Green 5"));
	table.push_back(Card(br[32], dk[32], GREEN, NUM6, "Green 6"));
	table.push_back(Card(br[33], dk[33], GREEN, NUM7, "Green 7"));
	table.push_back(Card(br[34], dk[34], GREEN, NUM8, "Green 8"));
	table.push_back(Card(br[35], dk[35], GREEN, NUM9, "Green 9"));
	table.push_back(Card(br[36], dk[36], GREEN, DRAW2, "Green +2"));
	table.push_back(Card(br[37], dk[37], GREEN, SKIP, "Green Skip"));
	table.push_back(Card(br[38], dk[38], GREEN, REV, "Green Reverse"));
	table.push_back(Card(br[39], dk[39], YELLOW, NUM0, "Yellow 0"));
	table.push_back(Card(br[40], dk[40], YELLOW, NUM1, "Yellow 1"));
	table.push_back(Card(br[41], dk[41], YELLOW, NUM2, "Yellow 2"));
	table.push_back(Card(br[42], dk[42], YELLOW, NUM3, "Yellow 3"));
	table.push_back(Card(br[43], dk[43], YELLOW, NUM4, "Yellow 4"));
	table.push_back(Card(br[44], dk[44], YELLOW, NUM5, "Yellow 5"));
	table.push_back(Card(br[45], dk[45], YELLOW, NUM6, "Yellow 6"));
	table.push_back(Card(br[46], dk[46], YELLOW, NUM7, "Yellow 7"));
	table.push_back(Card(br[47], dk[47], YELLOW, NUM8, "Yellow 8"));
	table.push_back(Card(br[48], dk[48], YELLOW, NUM9, "Yellow 9"));
	table.push_back(Card(br[49], dk[49], YELLOW, DRAW2, "Yellow +2"));
	table.push_back(Card(br[50], dk[50], YELLOW, SKIP, "Yellow Skip"));
	table.push_back(Card(br[51], dk[51], YELLOW, REV, "Yellow Reverse"));
	table.push_back(Card(br[52], dk[52], NONE, WILD, "Wild"));
	table.push_back(Card(br[52], dk[52], NONE, WILD, "Wild"));
	table.push_back(Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"));
	table.push_back(Card(br[1], dk[1], RED, NUM1, "Red 1"));
	table.push_back(Card(br[2], dk[2], RED, NUM2, "Red 2"));
	table.push_back(Card(br[3], dk[3], RED, NUM3, "Red 3"));
	table.push_back(Card(br[4], dk[4], RED, NUM4, "Red 4"));
	table.push_back(Card(br[5], dk[5], RED, NUM5, "Red 5"));
	table.push_back(Card(br[6], dk[6], RED, NUM6, "Red 6"));
	table.push_back(Card(br[7], dk[7], RED, NUM7, "Red 7"));
	table.push_back(Card(br[8], dk[8], RED, NUM8, "Red 8"));
	table.push_back(Card(br[9], dk[9], RED, NUM9, "Red 9"));
	table.push_back(Card(br[10], dk[10], RED, DRAW2, "Red +2"));
	table.push_back(Card(br[11], dk[11], RED, SKIP, "Red Skip"));
	table.push_back(Card(br[12], dk[12], RED, REV, "Red Reverse"));
	table.push_back(Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"));
	table.push_back(Card(br[14], dk[14], BLUE, NUM1, "Blue 1"));
	table.push_back(Card(br[15], dk[15], BLUE, NUM2, "Blue 2"));
	table.push_back(Card(br[16], dk[16], BLUE, NUM3, "Blue 3"));
	table.push_back(Card(br[17], dk[17], BLUE, NUM4, "Blue 4"));
	table.push_back(Card(br[18], dk[18], BLUE, NUM5, "Blue 5"));
	table.push_back(Card(br[19], dk[19], BLUE, NUM6, "Blue 6"));
	table.push_back(Card(br[20], dk[20], BLUE, NUM7, "Blue 7"));
	table.push_back(Card(br[21], dk[21], BLUE, NUM8, "Blue 8"));
	table.push_back(Card(br[22], dk[22], BLUE, NUM9, "Blue 9"));
	table.push_back(Card(br[23], dk[23], BLUE, DRAW2, "Blue +2"));
	table.push_back(Card(br[24], dk[24], BLUE, SKIP, "Blue Skip"));
	table.push_back(Card(br[25], dk[25], BLUE, REV, "Blue Reverse"));
	table.push_back(Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"));
	table.push_back(Card(br[27], dk[27], GREEN, NUM1, "Green 1"));
	table.push_back(Card(br[28], dk[28], GREEN, NUM2, "Green 2"));
	table.push_back(Card(br[29], dk[29], GREEN, NUM3, "Green 3"));
	table.push_back(Card(br[30], dk[30], GREEN, NUM4, "Green 4"));
	table.push_back(Card(br[31], dk[31], GREEN, NUM5, "Green 5"));
	table.push_back(Card(br[32], dk[32], GREEN, NUM6, "Green 6"));
	table.push_back(Card(br[33], dk[33], GREEN, NUM7, "Green 7"));
	table.push_back(Card(br[34], dk[34], GREEN, NUM8, "Green 8"));
	table.push_back(Card(br[35], dk[35], GREEN, NUM9, "Green 9"));
	table.push_back(Card(br[36], dk[36], GREEN, DRAW2, "Green +2"));
	table.push_back(Card(br[37], dk[37], GREEN, SKIP, "Green Skip"));
	table.push_back(Card(br[38], dk[38], GREEN, REV, "Green Reverse"));
	table.push_back(Card(br[53], dk[53], NONE, WILD_DRAW4, "Wild +4"));
	table.push_back(Card(br[40], dk[40], YELLOW, NUM1, "Yellow 1"));
	table.push_back(Card(br[41], dk[41], YELLOW, NUM2, "Yellow 2"));
	table.push_back(Card(br[42], dk[42], YELLOW, NUM3, "Yellow 3"));
	table.push_back(Card(br[43], dk[43], YELLOW, NUM4, "Yellow 4"));
	table.push_back(Card(br[44], dk[44], YELLOW, NUM5, "Yellow 5"));
	table.push_back(Card(br[45], dk[45], YELLOW, NUM6, "Yellow 6"));
	table.push_back(Card(br[46], dk[46], YELLOW, NUM7, "Yellow 7"));
	table.push_back(Card(br[47], dk[47], YELLOW, NUM8, "Yellow 8"));
	table.push_back(Card(br[48], dk[48], YELLOW, NUM9, "Yellow 9"));
	table.push_back(Card(br[49], dk[49], YELLOW, DRAW2, "Yellow +2"));
	table.push_back(Card(br[50], dk[50], YELLOW, SKIP, "Yellow Skip"));
	table.push_back(Card(br[51], dk[51], YELLOW, REV, "Yellow Reverse"));
	table.push_back(Card(br[52], dk[52], NONE, WILD, "Wild"));
	table.push_back(Card(br[52], dk[52], NONE, WILD, "Wild"));

	// Initialize other members
	players = 3;
	direction = 0;
	now = Player::YOU;
	difficulty = LV_EASY;

	// Generate a random seed based on the current time stamp
	srand((unsigned)time(nullptr));
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
const cv::Mat& Uno::getBackImage() {
	return backImage;
} // getBackImage()

/**
 * @param level   Pass LV_EASY or LV_HARD.
 * @param hiLight Pass true if you want to get a hi-lighted image,
 *                or false if you want to get a dark image.
 * @return Corresponding difficulty button image.
 */
const cv::Mat& Uno::getLevelImage(int level, bool hiLight) {
	return level == LV_EASY ?
		/* level == LV_EASY */ (hiLight ? easyImage : easyImage_d) :
		/* level == LV_HARD */ (hiLight ? hardImage : hardImage_d);
} // getLevelImage(int, bool)

/**
 * @return Background image resource in current direction.
 */
const cv::Mat& Uno::getBackground() {
	switch (direction) {
	case DIR_LEFT:
		return bgClockwise; // case DIR_LEFT

	case DIR_RIGHT:
		return bgCounter; // case DIR_RIGHT

	default:
		return bgWelcome; // default
	} // switch (direction)
} // getBackground()

/**
 * When a player played a wild card and specified a following legal color,
 * get the corresponding color-filled image here, and show it in recent
 * card area.
 *
 * @param color The wild image with which color filled you want to get.
 * @return Corresponding color-filled image.
 */
const cv::Mat& Uno::getColoredWildImage(Color color) {
	return wildImage[color];
} // getColoredWildImage(Color)

/**
 * When a player played a wild +4 card and specified a following legal
 * color, get the corresponding color-filled image here, and show it in
 * recent card area.
 *
 * @param color The wild +4 image with which color filled you want to get.
 * @return Corresponding color-filled image.
 */
const cv::Mat& Uno::getColoredWildDraw4Image(Color color) {
	return wildDraw4Image[color];
} // getColoredWildDraw4Image(Color)

/**
 * @return Player in turn. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::getNow() {
	return now;
} // getNow()

/**
 * Switch to next player's turn.
 *
 * @return Player in turn after switched. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::switchNow() {
	return (now = getNext());
} // switchNow()

/**
 * @return Current player's next player. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::getNext() {
	int next = (now + direction) % 4;
	if (players == 3 && next == Player::COM2) {
		next = (next + direction) % 4;
	} // if (players == 3 && next == Player::COM2)

	return next;
} // getNext()

/**
 * @return Current player's opposite player. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 *         NOTE: When only 3 players in game, getOppo() == getPrev().
 */
int Uno::getOppo() {
	int oppo = (getNext() + direction) % 4;
	if (players == 3 && oppo == Player::COM2) {
		oppo = (oppo + direction) % 4;
	} // if (players == 3 && oppo == Player::COM2)

	return oppo;
} // getOppo()

/**
 * @return Current player's previous player. Must be one of the following:
 *         Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 */
int Uno::getPrev() {
	int prev = (4 + now - direction) % 4;
	if (players == 3 && prev == Player::COM2) {
		prev = (4 + prev - direction) % 4;
	} // if (players == 3 && prev == Player::COM2)

	return prev;
} // getPrev()

/**
 * @return How many players in game (3 or 4).
 */
int Uno::getPlayers() {
	return players;
} // getPlayers()

/**
 * Set the amount of players in game.
 *
 * @param players Supports 3 and 4.
 */
void Uno::setPlayers(int players) {
	if (players == 3 || players == 4) {
		this->players = players;
	} // if (players == 3 || players == 4)
} // setPlayers(int)

/**
 * Switch current action sequence.
 *
 * @return Switched action sequence. DIR_LEFT for clockwise,
 *         or DIR_RIGHT for counter-clockwise.
 */
int Uno::switchDirection() {
	return (direction = 4 - direction);
} // switchDirection()

/**
 * @param who Get which player's instance. Must be one of the following:
 *            Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @return Specified player's instance.
 */
Player* Uno::getPlayer(int who) {
	return who < Player::YOU || who > Player::COM3 ? nullptr : &player[who];
} // getPlayer(int)

/**
 * @return Current difficulty (LV_EASY / LV_HARD).
 */
int Uno::getDifficulty() {
	return difficulty;
} // getDifficulty()

/**
 * Set game difficulty.
 *
 * @param difficulty Pass target difficulty value.
 *                   Only LV_EASY and LV_HARD are available.
 */
void Uno::setDifficulty(int difficulty) {
	if (difficulty == LV_EASY || difficulty == LV_HARD) {
		this->difficulty = difficulty;
	} // if (difficulty == LV_EASY || difficulty == LV_HARD)
} // setDifficulty(int)

/**
 * Find a card instance in card table.
 *
 * @param color   Color of the card you want to get.
 * @param content Content of the card you want to get.
 * @return Corresponding card instance.
 */
Card* Uno::findCard(Color color, Content content) {
	int i;
	Card* result;

	result = nullptr;
	for (i = 0; i < 108; ++i) {
		if (table[i].color == color && table[i].content == content) {
			result = &table[i];
			break;
		} // if (table[i].color == color && table[i].content == content)
	} // for (i = 0; i < 108; ++i)

	return result;
} // findCard(Color, Content)

/**
 * @return How many cards in deck (haven't been used yet).
 */
int Uno::getDeckCount() {
	return int(deck.size());
} // getDeckCount()

/**
 * @return How many cards have been used.
 */
int Uno::getUsedCount() {
	return int((used.size() + recent.size()));
} // getUsedCount()

/**
 * @return Recent played cards.
 */
const std::vector<Card*>& Uno::getRecent() {
	return recent;
} // getRecent()

/**
 * Start a new Uno game. Shuffle cards, let everyone draw 7 cards,
 * then determine our start card.
 */
void Uno::start() {
	Card* card;
	int i, size;

	// Reset direction
	direction = DIR_LEFT;

	// Clear card deck, used card deck, recent played cards,
	// everyone's hand cards, and everyone's strong/weak colors
	deck.clear();
	used.clear();
	recent.clear();
	for (i = Player::YOU; i <= Player::COM3; ++i) {
		player[i].handCards.clear();
		player[i].weakColor = NONE;
		player[i].strongColor = NONE;
	} // for (i = Player::YOU; i <= Player::COM3; ++i)

	// Generate a temporary sequenced card deck
	for (i = 0; i < 108; ++i) {
		if (table[i].isWild()) {
			// reset the wild cards' colors
			table[i].color = NONE;
		} // if (table[i].isWild())

		deck.push_back(&table[i]);
	} // for (i = 0; i < 108; ++i)

	// Shuffle cards
	size = int(deck.size());
	while (size > 0) {
		i = rand() % size--;
		card = deck[i]; deck[i] = deck[size]; deck[size] = card;
	} // while (size > 0)

	// Let everyone draw 7 cards
	if (players == 3) {
		for (i = 0; i < 7; ++i) {
			draw(Player::YOU,  /* force */ true);
			draw(Player::COM1, /* force */ true);
			draw(Player::COM3, /* force */ true);
		} // for (i = 0; i < 7; ++i)
	} // if (players == 3)
	else {
		for (i = 0; i < 7; ++i) {
			draw(Player::YOU,  /* force */ true);
			draw(Player::COM1, /* force */ true);
			draw(Player::COM2, /* force */ true);
			draw(Player::COM3, /* force */ true);
		} // for (i = 0; i < 7; ++i)
	} // else

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
 * NOTE: Everyone can hold 14 cards at most in this program, so even if this
 * function is called, the specified player may not draw a card as a result.
 *
 * @param who   Who draws a card. Must be one of the following values:
 *              Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @param force Pass true if the specified player is required to draw cards,
 *              i.e. previous player played a [+2] or [wild +4] to let this
 *              player draw cards. Or false if the specified player draws a
 *              card by itself in its action.
 * @return Reference of the drawn card, or nullptr if the specified player
 *         didn't draw a card because of the limitation.
 */
Card* Uno::draw(int who, bool force) {
	Card* card;
	Card* picked;
	int index, size;
	std::vector<Card*>* hand;
	std::vector<Card*>::iterator i;

	card = nullptr;
	if (who >= Player::YOU && who <= Player::COM3) {
		if (!force) {
			// Draw a card by player itself, register weak color
			player[who].weakColor = recent.back()->color;
			if (player[who].weakColor == player[who].strongColor) {
				// Weak color cannot also be strong color
				player[who].strongColor = NONE;
			} // if (player[who].weakColor == player[who].strongColor)
		} // if (!force)

		hand = &(player[who].handCards);
		if (hand->size() < MAX_HOLD_CARDS) {
			// Draw a card from card deck, and put it to an appropriate position
			card = deck.back();
			deck.pop_back();
			for (i = hand->begin(); i != hand->end(); ++i) {
				if ((*i)->order > card->order) {
					// Found an appropriate position to insert the new card,
					// which keeps the player's hand cards sequenced
					break;
				} // if ((*i)->order > card->order)
			} // for (i = hand->begin(); i != hand->end(); ++i)

			hand->insert(i, card);
			player[who].recent = nullptr;
			if (deck.empty()) {
				// Re-use the used cards when there are no more cards in deck
				size = int(used.size());
				while (size > 0) {
					index = rand() % size;
					picked = used.at(index);
					if (picked->isWild()) {
						// reset the used wild cards' colors
						picked->color = NONE;
					} // if (picked->isWild())

					deck.push_back(picked);
					used.erase(used.begin() + index);
					--size;
				} // while (size > 0)
			} // if (deck.empty())
		} // if (hand->size() < MAX_HOLD_CARDS)
	} // if (who >= Player::YOU && who <= Player::COM3)

	return card;
} // draw(int, bool)

/**
 * Check whether the specified card is legal to play. It's legal only when
 * it's wild, or it has the same color/content to the previous played card.
 *
 * @param card Check which card's legality.
 * @return Whether the specified card is legal to play.
 */
bool Uno::isLegalToPlay(Card* card) {
	bool result;
	Card* previous;

	if (card == nullptr || recent.empty()) {
		// Null Pointer
		result = false;
	} // if (card == nullptr || recent.empty())
	else if (card->isWild()) {
		// Wild cards: LEGAL
		result = true;
	} // else if (card->isWild())
	else {
		// Same content to previous: LEGAL
		// Same color to previous: LEGAL
		// Other cards: ILLEGAL
		previous = recent.back();
		result = card->color == previous->color
			|| card->content == previous->content;
	} // else

	return result;
} // isLegalToPlay(Card*)

/**
 * @return How many legal cards (the cards that can be played legally)
 *         in now player's hand.
 */
int Uno::legalCardsCount4NowPlayer() {
	int count = 0;

	for (Card* card : player[now].handCards) {
		if (isLegalToPlay(card)) {
			++count;
		} // if (isLegalToPlay(card))
	} // for (Card* card : player[now].handCards)

	return count;
} // legalCardsCount4NowPlayer()

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
 *              Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @param index Play which card. Pass the corresponding card's index of the
 *              specified player's hand cards.
 * @param color Optional, available when the card to play is a wild card.
 *              Pass the specified following legal color.
 * @return Reference of the played card.
 */
Card* Uno::play(int who, int index, Color color) {
	int size;
	Card* card;
	std::vector<Card*>* hand;

	card = nullptr;
	if (who >= Player::YOU && who <= Player::COM3) {
		hand = &(player[who].handCards);
		size = int(hand->size());
		if (index < size) {
			card = hand->at(index);
			hand->erase(hand->begin() + index);
			if (card->isWild()) {
				// When a wild card is played, register the specified
				// following legal color as the player's strong color
				card->color = color;
				player[who].strongColor = color;
				player[who].strongCount = 1 + size / 3;
				if (color == player[who].weakColor) {
					// Strong color cannot also be weak color
					player[who].weakColor = NONE;
				} // if (color == player[who].weakColor)
			} // if (card->isWild())
			else if (card->color == player[who].strongColor) {
				// Played a card that matches the registered
				// strong color, strong counter counts down
				--player[who].strongCount;
				if (player[who].strongCount == 0) {
					player[who].strongColor = NONE;
				} // if (player[who].strongCount == 0)
			} // else if (card->color == player[who].strongColor)
			else if (player[who].strongCount > size - 1) {
				// Correct the value of strong counter when necessary
				player[who].strongCount = size - 1;
			} // else if (player[who].strongCount > size - 1)

			player[who].recent = card;
			recent.push_back(card);
			if (recent.size() > 5) {
				used.push_back(recent.front());
				recent.erase(recent.begin());
			} // if (recent.size() > 5)

			if (hand->size() == 0) {
				// Game over, change background image
				direction = 0;
			} // if (hand->size() == 0)
		} // if (index < size)
	} // if (who >= Player::YOU && who <= Player::COM3)

	return card;
} // play(int, int, Color)

// E.O.F