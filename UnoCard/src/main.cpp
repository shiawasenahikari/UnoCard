////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include "Uno.h"
#include <string>
#include <vector>
#include <cstdlib>
#include <cstring>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>

using namespace cv;
using namespace std;

#define LV_EASY         0
#define LV_HARD         1
#define DIR_LEFT        1
#define DIR_RIGHT       3
#define STAT_IDLE       0x1111
#define STAT_WELCOME    0x2222
#define STAT_NEW_GAME   0x3333
#define STAT_GAME_OVER  0x4444
#define STAT_WILD_COLOR 0x5555

// Constants
static const Scalar RGB_RED = CV_RGB(0xFF, 0x55, 0x55);
static const Scalar RGB_BLUE = CV_RGB(0x55, 0x55, 0xFF);
static const Scalar RGB_GREEN = CV_RGB(0x55, 0xAA, 0x55);
static const Scalar RGB_WHITE = CV_RGB(0xCC, 0xCC, 0xCC);
static const Scalar RGB_YELLOW = CV_RGB(0xFF, 0xAA, 0x11);
static const string NAME[] = { "YOU", "WEST", "NORTH", "EAST" };
static const enum HersheyFonts FONT_SANS = FONT_HERSHEY_DUPLEX;
static const char FILE_HEADER[] = {
	(char)('U' + 'N'),
	(char)('O' + '@'),
	(char)('H' + 'i'),
	(char)('k' + 'a'),
	(char)('r' + 'i'),
	(char)('T' + 'o'),
	(char)('y' + 'a'),
	(char)('m' + 'a'), 0x00
}; // FILE_HEADER[]

// Global Variables
static Uno* sUno;
static Mat sScreen;
static int sStatus;
static int sWinner;
static int sEasyWin;
static int sHardWin;
static int sEasyTotal;
static int sHardTotal;
static int sDirection;
static int sDifficulty;
static bool sAIRunning;

// Functions
static void easyAI();
static void hardAI();
static void onStatusChanged(int status);
static void refreshScreen(string message);
static void play(int index, Color color = BLACK);
static void draw(int who = sStatus, int count = 1);
static void onMouse(int event, int x, int y, int flags, void* param);

/**
 * Defines the entry point for the console application.
 */
int main() {
	ifstream reader;
	int len, checksum;
	char header[9] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// Preparations
	sEasyWin = sHardWin = sEasyTotal = sHardTotal = 0;
	reader = ifstream("UnoStat.tmp", ios::in | ios::binary);
	if (!reader.fail()) {
		// Using statistics data in UnoStat.tmp file.
		reader.seekg(0, ios::end);
		len = (int)reader.tellg();
		reader.seekg(0, ios::beg);
		if (len == 8 + 5 * sizeof(int)) {
			reader.read(header, 8);
			reader.read((char*)&sEasyWin, sizeof(int));
			reader.read((char*)&sHardWin, sizeof(int));
			reader.read((char*)&sEasyTotal, sizeof(int));
			reader.read((char*)&sHardTotal, sizeof(int));
			reader.read((char*)&checksum, sizeof(int));
			if (strcmp(header, FILE_HEADER) != 0 ||
				checksum != sEasyWin + sHardWin + sEasyTotal + sHardTotal) {
				// File verification error. Use default statistics data.
				sEasyWin = sHardWin = sEasyTotal = sHardTotal = 0;
			} // if (strcmp(header, FILE_HEADER) != 0 || ...)
		} // if (len == 8 + 5 * sizeof(int))

		reader.close();
	} // if (!reader.fail())

	sUno = Uno::getInstance();
	sAIRunning = false;
	sWinner = PLAYER_YOU;
	sStatus = STAT_WELCOME;
	sScreen = sUno->getBackground().clone();
	namedWindow("Uno");
	refreshScreen("WELCOME TO UNO CARD GAME");
	setMouseCallback("Uno", onMouse, NULL);
	for (;;) {
		waitKey(0); // prevent from blocking main thread
	} // for (;;)
} // main()

/**
 * AI Strategies (Difficulty: EASY).
 */
static void easyAI() {
	Card* card;
	Card* previous;
	vector<Card*> hand;
	int i, next, oppo, prev;
	Color bestColor, prevColor;
	bool hasNum, hasWild, hasWildDraw4;
	bool hasRev, hasSkip, hasDraw2, hasZero;
	int yourSize, nextSize, oppoSize, prevSize;
	int idx_best, idx_rev, idx_skip, idx_draw2;
	int idx_zero, idx_num, idx_wild, idx_wildDraw4;

	sAIRunning = true;
	while (sStatus == PLAYER_COM1
		|| sStatus == PLAYER_COM2
		|| sStatus == PLAYER_COM3) {
		hand = sUno->getHandCardsOf(sStatus);
		yourSize = (int)hand.size();
		if (yourSize == 1) {
			// Only one card remained. Play it when it's legal.
			card = hand.at(0);
			if (sUno->isLegalToPlay(card)) {
				play(0);
			} // if (sUno->isLegalToPlay(card))
			else {
				draw();
			} // else

			continue;
		} // if (yourSize == 1)

		next = (sStatus + sDirection) % 4;
		nextSize = (int)sUno->getHandCardsOf(next).size();
		oppo = (sStatus + 2) % 4;
		oppoSize = (int)sUno->getHandCardsOf(oppo).size();
		prev = (4 + sStatus - sDirection) % 4;
		prevSize = (int)sUno->getHandCardsOf(prev).size();
		idx_best = idx_rev = idx_skip = idx_draw2 = -1;
		idx_zero = idx_num = idx_wild = idx_wildDraw4 = -1;
		bestColor = sUno->bestColorFor(sStatus);
		previous = sUno->getRecent().back();
		if (previous->isWild()) {
			prevColor = previous->getWildColor();
		} // if (previous->isWild())
		else {
			prevColor = previous->getColor();
		} // else

		for (i = 0; i < yourSize; ++i) {
			// Index of any kind
			card = hand.at(i);
			if (sUno->isLegalToPlay(card)) {
				switch (card->getContent()) {
				case NUM0:
					if (idx_zero < 0 || card->getColor() == bestColor) {
						idx_zero = i;
					} // if (idx_zero < 0 || card->getColor() == bestColor)
					break; // case NUM0

				case DRAW2:
					if (idx_draw2 < 0 || card->getColor() == bestColor) {
						idx_draw2 = i;
					} // if (idx_draw2 < 0 || card->getColor() == bestColor)
					break; // case DRAW2

				case SKIP:
					if (idx_skip < 0 || card->getColor() == bestColor) {
						idx_skip = i;
					} // if (idx_skip < 0 || card->getColor() == bestColor)
					break; // case SKIP

				case REV:
					if (idx_rev < 0 || card->getColor() == bestColor) {
						idx_rev = i;
					} // if (idx_rev < 0 || card->getColor() == bestColor)
					break; // case REV

				case WILD:
					idx_wild = i;
					break; // case WILD

				case WILD_DRAW4:
					idx_wildDraw4 = i;
					break; // case WILD_DRAW4

				default: // non-zero number cards
					if (idx_num < 0 || card->getColor() == bestColor) {
						idx_num = i;
					} // if (idx_num < 0 || card->getColor() == bestColor)
					break; // default
				} // switch (card->getContent())
			} // if (sUno->isLegalToPlay(card))
		} // for (i = 0; i < yourSize; ++i)

		// Decision tree
		hasNum = (idx_num >= 0);
		hasRev = (idx_rev >= 0);
		hasZero = (idx_zero >= 0);
		hasSkip = (idx_skip >= 0);
		hasWild = (idx_wild >= 0);
		hasDraw2 = (idx_draw2 >= 0);
		hasWildDraw4 = (idx_wildDraw4 >= 0);
		if (nextSize == 1) {
			// Strategies when your next player remains only one card.
			// Limit your next player's action as well as you can.
			if (hasDraw2) {
				// Play a [+2] to make your next player draw two cards!
				idx_best = idx_draw2;
			} // if (hasDraw2)
			else if (hasWildDraw4) {
				// Play a [wild +4] to make your next player draw four cards,
				// even if the legal color is already your best color!
				idx_best = idx_wildDraw4;
			} // else if (hasWildDraw4)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idx_best = idx_skip;
			} // else if (hasSkip)
			else if (hasRev) {
				// Play a [reverse] to get help from your opposite player.
				idx_best = idx_rev;
			} // else if (hasRev)
			else if (hasWild && prevColor != bestColor) {
				// Play a [wild] and change the legal color to your best to
				// decrease its possibility of playing the final card legally.
				idx_best = idx_wild;
			} // else if (hasWild && prevColor != bestColor)
			else if (hasZero) {
				// No more powerful choices. Play a number card.
				idx_best = idx_zero;
			} // else if (hasZero)
			else if (hasNum) {
				idx_best = idx_num;
			} // else if (hasNum)
		} // if (nextSize == 1)
		else if (oppoSize == 1) {
			// Strategies when your opposite player remains only one card.
			// Give more freedom to your next player, the only one that can
			// directly limit your opposite player's action.
			if (hasRev && nextSize <= 3 && prevSize >= 6) {
				// Play a [reverse] when your next player remains only a few
				// cards but your previous player remains a lot of cards,
				// because your previous player has more possibility to limit
				// your opposite player's action.
				idx_best = idx_rev;
			} // if (hasRev && nextSize <= 3 && prevSize >= 6)
			else if (hasNum) {
				// Then you can play a number card. In order to increase your
				// next player's possibility of changing the legal color, do
				// not play zero cards preferentially this time.
				idx_best = idx_num;
			} // else if (hasNum)
			else if (hasZero) {
				idx_best = idx_zero;
			} // else if (hasZero)
			else if (hasWild && prevColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Do not play any [+2]/[reverse] to your next player!
				idx_best = idx_wild;
			} // else if (hasWild && prevColor != bestColor)
			else if (hasWildDraw4 && prevColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Specially, for [wild +4] cards, you can only play it
				// when your next player remains only a few cards, and what you
				// did can help your next player find more useful cards, such as
				// action cards, or [wild +4] cards.
				if (nextSize <= 4) {
					idx_best = idx_wildDraw4;
				} // if (nextSize <= 4)
			} // else if (hasWildDraw4 && prevColor != bestColor)
		} // else if (oppoSize == 1)
		else {
			// Normal strategies
			if (hasZero) {
				// Play zero cards at first because of their rarity.
				idx_best = idx_zero;
			} // if (hasZero)
			else if (hasNum) {
				// Then consider to play a number card.
				idx_best = idx_num;
			} // else if (hasNum)
			else if (hasRev && prevSize >= 3) {
				// Then consider to play an action card.
				idx_best = idx_rev;
			} // else if (hasRev && prevSize >= 3)
			else if (hasSkip) {
				idx_best = idx_skip;
			} // else if (hasSkip)
			else if (hasDraw2) {
				idx_best = idx_draw2;
			} // else if (hasDraw2)
			else if (hasWild) {
				// Then consider to play a wild card.
				idx_best = idx_wild;
			} // else if (hasWild)
			else if (hasWildDraw4) {
				idx_best = idx_wildDraw4;
			} // else if (hasWildDraw4)
		} // else

		if (idx_best >= 0) {
			// Found an appropriate card to play
			play(idx_best, bestColor);
		} // if (idx_best >= 0)
		else {
			// No appropriate cards to play, or no card is legal to play
			draw();
		} // else
	} // while (sStatus == PLAYER_COM1 || ...)

	sAIRunning = false;
} // easyAI()

/**
 * AI Strategies (Difficulty: HARD).
 */
static void hardAI() {
	Card* card;
	Card* previous;
	vector<Card*> hand;
	int i, next, oppo, prev;
	Color bestColor, prevColor;
	bool hasNum, hasWild, hasWildDraw4;
	bool hasRev, hasSkip, hasDraw2, hasZero;
	int yourSize, nextSize, oppoSize, prevSize;
	int idx_best, idx_rev, idx_skip, idx_draw2;
	int idx_zero, idx_num, idx_wild, idx_wildDraw4;

	sAIRunning = true;
	while (sStatus == PLAYER_COM1
		|| sStatus == PLAYER_COM2
		|| sStatus == PLAYER_COM3) {
		hand = sUno->getHandCardsOf(sStatus);
		yourSize = (int)hand.size();
		if (yourSize == 1) {
			// Only one card remained. Play it when it's legal.
			card = hand.at(0);
			if (sUno->isLegalToPlay(card)) {
				play(0);
			} // if (sUno->isLegalToPlay(card))
			else {
				draw();
			} // else

			continue;
		} // if (yourSize == 1)

		next = (sStatus + sDirection) % 4;
		nextSize = (int)sUno->getHandCardsOf(next).size();
		oppo = (sStatus + 2) % 4;
		oppoSize = (int)sUno->getHandCardsOf(oppo).size();
		prev = (4 + sStatus - sDirection) % 4;
		prevSize = (int)sUno->getHandCardsOf(prev).size();
		idx_best = idx_rev = idx_skip = idx_draw2 = -1;
		idx_zero = idx_num = idx_wild = idx_wildDraw4 = -1;
		bestColor = sUno->bestColorFor(sStatus);
		previous = sUno->getRecent().back();
		if (previous->isWild()) {
			prevColor = previous->getWildColor();
		} // if (previous->isWild())
		else {
			prevColor = previous->getColor();
		} // else

		for (i = 0; i < yourSize; ++i) {
			// Index of any kind
			card = hand.at(i);
			if (sUno->isLegalToPlay(card)) {
				switch (card->getContent()) {
				case NUM0:
					if (idx_zero < 0 || card->getColor() == bestColor) {
						idx_zero = i;
					} // if (idx_zero < 0 || card->getColor() == bestColor)
					break; // case NUM0

				case DRAW2:
					if (idx_draw2 < 0 || card->getColor() == bestColor) {
						idx_draw2 = i;
					} // if (idx_draw2 < 0 || card->getColor() == bestColor)
					break; // case DRAW2

				case SKIP:
					if (idx_skip < 0 || card->getColor() == bestColor) {
						idx_skip = i;
					} // if (idx_skip < 0 || card->getColor() == bestColor)
					break; // case SKIP

				case REV:
					if (idx_rev < 0 || card->getColor() == bestColor) {
						idx_rev = i;
					} // if (idx_rev < 0 || card->getColor() == bestColor)
					break; // case REV

				case WILD:
					idx_wild = i;
					break; // case WILD

				case WILD_DRAW4:
					idx_wildDraw4 = i;
					break; // case WILD_DRAW4

				default: // non-zero number cards
					if (idx_num < 0 || card->getColor() == bestColor) {
						idx_num = i;
					} // if (idx_num < 0 || card->getColor() == bestColor)
					break; // default
				} // switch (card->getContent())
			} // if (sUno->isLegalToPlay(card))
		} // for (i = 0; i < yourSize; ++i)

		// Decision tree
		hasNum = (idx_num >= 0);
		hasRev = (idx_rev >= 0);
		hasZero = (idx_zero >= 0);
		hasSkip = (idx_skip >= 0);
		hasWild = (idx_wild >= 0);
		hasDraw2 = (idx_draw2 >= 0);
		hasWildDraw4 = (idx_wildDraw4 >= 0);
		if (nextSize == 1) {
			// Strategies when your next player remains only one card.
			// Limit your next player's action as well as you can.
			if (hasDraw2) {
				// Play a [+2] to make your next player draw two cards!
				idx_best = idx_draw2;
			} // if (hasDraw2)
			else if (hasWildDraw4) {
				// Play a [wild +4] to make your next player draw four cards,
				// even if the legal color is already your best color!
				idx_best = idx_wildDraw4;
			} // else if (hasWildDraw4)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idx_best = idx_skip;
			} // else if (hasSkip)
			else if (hasRev) {
				// Play a [reverse] to get help from your opposite player.
				idx_best = idx_rev;
			} // else if (hasRev)
			else if (hasWild && prevColor != bestColor) {
				// Play a [wild] and change the legal color to your best to
				// decrease its possibility of playing the final card legally.
				idx_best = idx_wild;
			} // else if (hasWild && prevColor != bestColor)
			else if (hasZero) {
				// No more powerful choices. Play a number card.
				idx_best = idx_zero;
			} // else if (hasZero)
			else if (hasNum) {
				idx_best = idx_num;
			} // else if (hasNum)
		} // if (nextSize == 1)
		else if (oppoSize == 1) {
			// Strategies when your opposite player remains only one card.
			// Give more freedom to your next player, the only one that can
			// directly limit your opposite player's action.
			if (hasRev && nextSize <= 3 && prevSize >= 6) {
				// Play a [reverse] when your next player remains only a few
				// cards but your previous player remains a lot of cards,
				// because your previous player has more possibility to limit
				// your opposite player's action.
				idx_best = idx_rev;
			} // if (hasRev && nextSize <= 3 && prevSize >= 6)
			else if (hasNum) {
				// Then you can play a number card. In order to increase your
				// next player's possibility of changing the legal color, do
				// not play zero cards priorly this time.
				idx_best = idx_num;
			} // else if (hasNum)
			else if (hasZero) {
				idx_best = idx_zero;
			} // else if (hasZero)
			else if (hasWild && prevColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Do not play any [+2]/[reverse] to your next player!
				idx_best = idx_wild;
			} // else if (hasWild && prevColor != bestColor)
			else if (hasWildDraw4 && prevColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Specially, for [wild +4] cards, you can only play it
				// when your next player remains only a few cards, and what you
				// did can help your next player find more useful cards, such as
				// action cards, or [wild +4] cards.
				if (nextSize <= 4) {
					idx_best = idx_wildDraw4;
				} // if (nextSize <= 4)
			} // else if (hasWildDraw4 && prevColor != bestColor)
		} // else if (oppoSize == 1)
		else if (prevSize == 1) {
			// Strategies when your previous player remains only one card.
			// Save your action cards s much as you can. once a reverse card is
			// played, you can use these cards to limit your previous player's
			// action.
			if (hasNum) {
				idx_best = idx_num;
			} // if (hasNum)
			else if (hasZero) {
				idx_best = idx_zero;
			} // else if (hasZero)
			else if (hasWild && prevColor != bestColor) {
				idx_best = idx_wild;
			} // else if (hasWild && prevColor != bestColor)
			else if (hasWildDraw4 && prevColor != bestColor) {
				idx_best = idx_wildDraw4;
			} // else if (hasWildDraw4 && prevColor != bestColor)
		} // else if (prevSize == 1)
		else {
			// Normal strategies
			if (hasRev && nextSize <= 3 && prevSize >= 6) {
				// Play a [reverse] when your next player remains only a few
				// cards but your previous player remains a lot of cards, in
				// order to balance everyone's hand-card amount.
				idx_best = idx_rev;
			} // if (hasRev && nextSize <= 3 && prevSize >= 6)
			else if (hasDraw2 && nextSize <= 4) {
				// Play a [+2] when your next player remains only a few cards.
				idx_best = idx_draw2;
			} // else if (hasDraw2 && nextSize <= 4)
			else if (hasSkip && nextSize <= 4) {
				// Play a [skip] when your next player remains only a few cards.
				idx_best = idx_skip;
			} // else if (hasSkip && nextSize <= 4)
			else if (hasZero) {
				// Play zero cards at first because of their rarity.
				idx_best = idx_zero;
			} // else if (hasZero)
			else if (hasNum) {
				// Then consider to play a number card.
				idx_best = idx_num;
			} // else if (hasNum)
			else if (hasRev && prevSize >= 4) {
				// When you have no more legal number cards to play, you can
				// play a [reverse] safely when your previous player still has
				// a number of cards.
				idx_best = idx_rev;
			} // else if (hasRev && prevSize >= 4)
			else if (hasWild && nextSize <= 4) {
				// When your next player remains only a few cards, and you have
				// no more legal number/action cards to play, try to play a
				// wild card and change the legal color to your best color.
				idx_best = idx_wild;
			} // else if (hasWild && nextSize <= 4)
			else if (hasWildDraw4 && nextSize <= 4) {
				// When your next player remains only a few cards, and you have
				// no more legal number/action cards to play, try to play a
				// wild card and change the legal color to your best color.
				idx_best = idx_wildDraw4;
			} // else if (hasWildDraw4 && nextSize <= 4)
			else if (hasWild && yourSize == 2 && prevSize <= 3) {
				// When you remain only 2 cards, including a wild card, and your
				// previous player seems no enough power to limit you (has too
				// few cards), start your UNO dash!
				if (hasDraw2) {
					// When you still have a [+2] card, play it, even if it's
					// not worth to let your next player draw cards.
					idx_best = idx_draw2;
				} // if (hasDraw2)
				else if (hasSkip) {
					// When you still have a [skip] card, play it, even if it's
					// not worth to let your next player skip its turn.
					idx_best = idx_skip;
				} // else if (hasSkip)
				else {
					// When you have no more legal draw2/skip cards, play your
					// wild card to start your UNO dash.
					idx_best = idx_wild;
				} // else
			} // else if (hasWild && yourSize == 2 && prevSize <= 3)
			else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3) {
				if (hasDraw2) {
					idx_best = idx_draw2;
				} // if (hasDraw2)
				else if (hasSkip) {
					idx_best = idx_skip;
				} // else if (hasSkip)
				else {
					idx_best = idx_wildDraw4;
				} // else
			} // else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3)
		} // else

		if (idx_best >= 0) {
			// Found an appropriate card to play
			play(idx_best, bestColor);
		} // if (idx_best >= 0)
		else {
			// No appropriate cards to play, or no card is legal to play
			draw();
		} // else
	} // while (sStatus == PLAYER_COM1 || ...)

	sAIRunning = false;
} // hardAI()

/**
 * Triggered when the value of global value [sStatus] changed.
 *
 * @param status New status value.
 */
static void onStatusChanged(int status) {
	static Rect rect;
	static Size axes;
	static Point center;

	switch (status) {
	case STAT_NEW_GAME:
		// New game
		if (sDifficulty == LV_EASY) {
			++sEasyTotal;
		} // if (sDifficulty == LV_EASY)
		else {
			++sHardTotal;
		} // else

		sUno->start();
		sDirection = DIR_LEFT;
		refreshScreen("GET READY");
		waitKey(2000);
		sStatus = sWinner;
		onStatusChanged(sStatus);
		break; // case STAT_NEW_GAME

	case PLAYER_YOU:
		// Your turn, select a hand card to play, or draw a card
		refreshScreen("Your turn, play or draw a card");
		break; // case PLAYER_YOU

	case STAT_WILD_COLOR:
		// Need to specify the following legal color after played a
		// wild card. Draw color sectors in the center of screen
		refreshScreen("^ Specify the following legal color");
		rect = Rect(450, 270, 181, 181);
		sUno->getBackground()(rect).copyTo(sScreen(rect));
		center = Point(450, 360);
		axes = Size(90, 90);
		ellipse(sScreen, center, axes, 0, 0, -90, RGB_BLUE, -1, 16);
		ellipse(sScreen, center, axes, 0, 0, 90, RGB_GREEN, -1, 16);
		ellipse(sScreen, center, axes, 180, 0, 90, RGB_RED, -1, 16);
		ellipse(sScreen, center, axes, 180, 0, -90, RGB_YELLOW, -1, 16);
		imshow("Uno", sScreen);
		break; // case STAT_WILD_COLOR

	case PLAYER_COM1:
	case PLAYER_COM2:
	case PLAYER_COM3:
		// AI players' turn
		if (!sAIRunning) {
			if (sDifficulty == LV_EASY) {
				easyAI();
			} // if (sDifficulty == LV_EASY)
			else {
				hardAI();
			} // else
		} // if (!sAIRunning)
		break; // case PLAYER_COM1, PLAYER_COM2, PLAYER_COM3

	case STAT_GAME_OVER:
		// Game over
		if (sWinner == PLAYER_YOU) {
			if (sDifficulty == LV_EASY) {
				++sEasyWin;
			} // if (sDifficulty == LV_EASY)
			else {
				++sHardWin;
			} // else
		} // if (sWinner == PLAYER_YOU)

		refreshScreen("Click the card deck to restart");
		break; // case STAT_GAME_OVER

	default:
		break; // default
	} // switch (status)
} // onStatusChanged()

/**
 * Refresh the screen display. The content of global variable [sScreen]
 * will be changed after calling this function.
 *
 * @param message Extra message to show.
 */
static void refreshScreen(string message) {
	Rect roi;
	Mat image;
	Point point;
	stringstream buff;
	vector<Card*> hand;
	int status, size, width, height, remain, used, easyRate, hardRate;

	// Lock the value of global variable [sStatus]
	status = sStatus;

	// Clear
	sUno->getBackground().copyTo(sScreen);

	// Message area
	width = getTextSize(message, FONT_SANS, 1.0, 1, NULL).width;
	point = Point(640 - width / 2, 480);
	putText(sScreen, message, point, FONT_SANS, 1.0, RGB_WHITE);

	// Right-top corner: <QUIT> button
	point.x = 1140;
	point.y = 42;
	putText(sScreen, "<QUIT>", point, FONT_SANS, 1.0, RGB_WHITE);

	// For welcome screen, only show difficulty buttons and winning rates
	if (status == STAT_WELCOME) {
		image = sUno->getEasyImage();
		roi = Rect(420, 270, 121, 181);
		image.copyTo(sScreen(roi), image);
		image = sUno->getHardImage();
		roi.x = 740;
		roi.y = 270;
		image.copyTo(sScreen(roi), image);
		easyRate = (sEasyTotal == 0 ? 0 : 100 * sEasyWin / sEasyTotal);
		hardRate = (sHardTotal == 0 ? 0 : 100 * sHardWin / sHardTotal);
		buff << easyRate << "% [WinningRate] " << hardRate << "%";
		width = getTextSize(buff.str(), FONT_SANS, 1.0, 1, NULL).width;
		point.x = 640 - width / 2;
		point.y = 250;
		putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);
	} // if (status == STAT_WELCOME)
	else {
		// Center: card deck & recent played card
		image = sUno->getBackImage();
		roi = Rect(420, 270, 121, 181);
		image.copyTo(sScreen(roi), image);
		hand = sUno->getRecent();
		size = (int)hand.size();
		width = 45 * size + 75;
		roi.x = 710 - width / 2;
		roi.y = 270;
		for (Card* recent : hand) {
			if (recent->getContent() == WILD) {
				image = sUno->getColoredWildImage(recent->getWildColor());
			} // if (recent->getContent() == WILD)
			else if (recent->getContent() == WILD_DRAW4) {
				image = sUno->getColoredWildDraw4Image(recent->getWildColor());
			} // else if (recent->getContent() == WILD_DRAW4)
			else {
				image = recent->getImage();
			} // else

			image.copyTo(sScreen(roi), image);
			roi.x += 45;
		} // for (Card* recent : hand)

		// Left-top corner: remain / used
		point = Point(20, 42);
		remain = sUno->getDeckCount();
		used = sUno->getUsedCount();
		buff.str("");
		buff << "Remain/Used: " << remain << "/" << used;
		putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);

		// Left-center: Hand cards of Player West (COM1)
		hand = sUno->getHandCardsOf(PLAYER_COM1);
		size = (int)hand.size();
		if (size == 0) {
			// Played all hand cards, it's winner
			if (status != STAT_WELCOME) {
				point.x = 51;
				point.y = 461;
				putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (status != STAT_WELCOME)
		} // if (size == 0)
		else {
			if (status == STAT_GAME_OVER) {
				// Show remained cards to everyone when game over
				height = 40 * size + 140;
				roi.x = 20;
				roi.y = 360 - height / 2;
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (status == STAT_GAME_OVER)
			else {
				// Only show a card back in game process
				image = sUno->getBackImage();
				roi.x = 20;
				roi.y = 270;
				image.copyTo(sScreen(roi), image);
			} // else

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 47;
				point.y = 494;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
			else if (status != STAT_GAME_OVER) {
				// When two or more cards in hand, show the amount
				point.x = 50;
				point.y = 494;
				buff.str("");
				buff << setw(2) << size;
				putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);
			} // else if (status != STAT_GAME_OVER)
		} // else

		// Top-center: Hand cards of Player North (COM2)
		hand = sUno->getHandCardsOf(PLAYER_COM2);
		size = (int)hand.size();
		if (size == 0) {
			// Played all hand cards, it's winner
			if (status != STAT_WELCOME) {
				point.x = 611;
				point.y = 121;
				putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (status != STAT_WELCOME)
		} // if (size == 0)
		else {
			if (status == STAT_GAME_OVER) {
				// Show remained hand cards when game over
				width = 45 * size + 75;
				roi.x = 640 - width / 2;
				roi.y = 20;
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.x += 45;
				} // for (Card* card : hand)
			} // if (status == STAT_GAME_OVER)
			else {
				// Only show a card back in game process
				image = sUno->getBackImage();
				roi.x = 580;
				roi.y = 20;
				image.copyTo(sScreen(roi), image);
			} // else

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 500;
				point.y = 121;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
			else if (status != STAT_GAME_OVER) {
				// When two or more cards in hand, show the amount
				point.x = 500;
				point.y = 121;
				buff.str("");
				buff << setw(2) << size;
				putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);
			} // else if (status != STAT_GAME_OVER)
		} // else

		// Right-center: Hand cards of Player East (COM3)
		hand = sUno->getHandCardsOf(PLAYER_COM3);
		size = (int)hand.size();
		if (size == 0) {
			// Played all hand cards, it's winner
			if (status != STAT_WELCOME) {
				point.x = 1170;
				point.y = 461;
				putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (status != STAT_WELCOME)
		} // if (size == 0)
		else {
			if (status == STAT_GAME_OVER) {
				// Show remained hand cards when game over
				height = 40 * size + 140;
				roi.x = 1140;
				roi.y = 360 - height / 2;
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (status == STAT_GAME_OVER)
			else {
				// Only show a card back in game process
				image = sUno->getBackImage();
				roi.x = 1140;
				roi.y = 270;
				image.copyTo(sScreen(roi), image);
			} // else

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 1166;
				point.y = 494;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
			else if (status != STAT_GAME_OVER) {
				// When two or more cards in hand, show the amount
				point.x = 1170;
				point.y = 494;
				buff.str("");
				buff << setw(2) << size;
				putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);
			} // else if (status != STAT_GAME_OVER)
		} // else

		// Bottom: Your hand cards
		hand = sUno->getHandCardsOf(PLAYER_YOU);
		size = (int)hand.size();
		if (size == 0) {
			// Played all hand cards, it's winner
			if (status != STAT_WELCOME) {
				point.x = 611;
				point.y = 621;
				putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (status != STAT_WELCOME)
		} // if (size == 0)
		else {
			// Show your all hand cards
			width = 45 * size + 75;
			roi.x = 640 - width / 2;
			roi.y = 520;
			for (Card* card : hand) {
				if (status == PLAYER_YOU && sUno->isLegalToPlay(card)) {
					image = card->getImage();
				} // if (status == PLAYER_YOU && sUno->isLegalToPlay(card))
				else if (status == STAT_GAME_OVER) {
					image = card->getImage();
				} // else if (status == STAT_GAME_OVER)
				else {
					image = card->getDarkImg();
				} // else

				image.copyTo(sScreen(roi), image);
				roi.x += 45;
			} // for (Card* card : hand)

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 720;
				point.y = 621;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else
	} // else

	// Show screen
	imshow("Uno", sScreen);
} // refreshScreen()

/**
 * The player in action play a card.
 *
 * @param index Play which card. Pass the corresponding card's index of the
 *              player's hand cards.
 * @param color Optional, available when the card to play is a wild card.
 *              Pass the specified following legal color.
 */
static void play(int index, Color color) {
	Rect roi;
	Mat image;
	Card* card;
	int now, next;
	string message;

	now = sStatus;
	sStatus = STAT_IDLE; // block mouse click events when idle
	card = sUno->play(now, index, color);
	if (card != NULL) {
		// Animation
		switch (now) {
		case PLAYER_COM1:
			image = card->getImage();
			roi = Rect(160, 270, 121, 181);
			image.copyTo(sScreen(roi), image);
			imshow("Uno", sScreen);
			waitKey(300);
			break; // case PLAYER_COM1

		case PLAYER_COM2:
			image = card->getImage();
			roi = Rect(720, 20, 121, 181);
			image.copyTo(sScreen(roi), image);
			imshow("Uno", sScreen);
			waitKey(300);
			break; // case PLAYER_COM2

		case PLAYER_COM3:
			image = card->getImage();
			roi = Rect(1000, 270, 121, 181);
			image.copyTo(sScreen(roi), image);
			imshow("Uno", sScreen);
			waitKey(300);
			break; // case PLAYER_COM3

		default:
			break; // default
		} // switch (now)

		if (sUno->getHandCardsOf(now).size() == 0) {
			// The player in action becomes winner when it played the
			// final card in its hand successfully
			sWinner = now;
			sStatus = STAT_GAME_OVER;
			onStatusChanged(sStatus);
		} // if (sUno->getHandCardsOf(now).size() == 0)
		else {
			// When the played card is an action card or a wild card,
			// do the necessary things according to the game rule
			message = NAME[now];
			switch (card->getContent()) {
			case DRAW2:
				next = (now + sDirection) % 4;
				message += ": Let " + NAME[next] + " draw 2 cards";
				refreshScreen(message);
				waitKey(1500);
				draw(next, 2);
				break; // case DRAW2

			case SKIP:
				next = (now + sDirection) % 4;
				if (next == PLAYER_YOU) {
					message += ": Skip your turn";
				} // if (next == PLAYER_YOU)
				else {
					message += ": Skip " + NAME[next] + "'s turn";
				} // else

				refreshScreen(message);
				waitKey(1500);
				sStatus = (next + sDirection) % 4;
				onStatusChanged(sStatus);
				break; // case SKIP

			case REV:
				if (sDirection == DIR_LEFT) {
					sDirection = DIR_RIGHT;
					message += ": Change direction to COUNTER CLOCKWISE";
				} // if (sDirection == DIR_LEFT)
				else {
					sDirection = DIR_LEFT;
					message += ": Change direction to CLOCKWISE";
				} // else

				refreshScreen(message);
				waitKey(1500);
				sStatus = (now + sDirection) % 4;
				onStatusChanged(sStatus);
				break; // case REV

			case WILD:
				message += ": Change the following legal color";
				refreshScreen(message);
				waitKey(1500);
				sStatus = (now + sDirection) % 4;
				onStatusChanged(sStatus);
				break; // case WILD

			case WILD_DRAW4:
				next = (now + sDirection) % 4;
				message += ": Let " + NAME[next] + " draw 4 cards";
				refreshScreen(message);
				waitKey(1500);
				draw(next, 4);
				break; // case WILD_DRAW4

			default:
				message += ": " + card->getName();
				refreshScreen(message);
				waitKey(1500);
				sStatus = (now + sDirection) % 4;
				onStatusChanged(sStatus);
				break; // default
			} // switch (card->getContent())
		} // else
	} // if (card != NULL)
} // play()

/**
 * Let a player draw one or more cards, and skip its turn.
 *
 * @param who   Who draws a card. Must be one of the following values:
 *              PLAYER_YOU, PLAYER_COM1, PLAYER_COM2, PLAYER_COM3.
 *              Default to the player in turn, i.e. sStatus.
 * @param count How many cards to draw. Default to 1.
 */
static void draw(int who, int count) {
	int i;
	Mat image;
	Card* card;
	stringstream buff;

	sStatus = STAT_IDLE; // block mouse click events when idle
	for (i = 0; i < count; ++i) {
		buff.str("");
		card = sUno->draw(who);
		if (card != NULL) {
			// Animation
			switch (who) {
			case PLAYER_COM1:
				image = sUno->getBackImage();
				image.copyTo(sScreen(Rect(160, 270, 121, 181)), image);
				imshow("Uno", sScreen);
				waitKey(300);
				if (count == 1) {
					buff << NAME[who] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[who] << ": Draw " << count << " cards";
				} // else

				refreshScreen(buff.str());
				waitKey(300);
				break; // case PLAYER_COM1

			case PLAYER_COM2:
				image = sUno->getBackImage();
				image.copyTo(sScreen(Rect(720, 20, 121, 181)), image);
				imshow("Uno", sScreen);
				waitKey(300);
				if (count == 1) {
					buff << NAME[who] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[who] << ": Draw " << count << " cards";
				} // else

				refreshScreen(buff.str());
				waitKey(300);
				break; // case PLAYER_COM2

			case PLAYER_COM3:
				image = sUno->getBackImage();
				image.copyTo(sScreen(Rect(1000, 270, 121, 181)), image);
				imshow("Uno", sScreen);
				waitKey(300);
				if (count == 1) {
					buff << NAME[who] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[who] << ": Draw " << count << " cards";
				} // else

				refreshScreen(buff.str());
				waitKey(300);
				break; // case PLAYER_COM3

			default:
				image = card->getImage();
				image.copyTo(sScreen(Rect(140, 520, 121, 181)), image);
				imshow("Uno", sScreen);
				waitKey(300);
				buff << NAME[who] << ": Draw " + card->getName();
				refreshScreen(buff.str());
				waitKey(300);
				break; // default
			} // switch (who)
		} // if (card != NULL)
		else {
			buff << NAME[who];
			buff << " cannot hold more than ";
			buff << MAX_HOLD_CARDS << " cards";
			refreshScreen(buff.str());
			break;
		} // else
	} // for (i = 0; i < count; ++i)

	waitKey(1500);
	sStatus = (who + sDirection) % 4;
	onStatusChanged(sStatus);
} // draw()

/**
 * Mouse event callback, used by OpenCV GUI windows. When a GUI window
 * registered this function as its mouse callback, once a mouse event
 * occurred in that window, this function will be called.
 *
 * @param event Which mouse event occurred, e.g. EVENT_LBUTTONDOWN
 * @param x     Mouse pointer's x-coordinate position when event occurred
 * @param y     Mouse pointer's y-coordinate position when event occurred
 * @param flags [UNUSED IN THIS CALLBACK]
 * @param param [UNUSED IN THIS CALLBACK]
 */
static void onMouse(int event, int x, int y, int /*flags*/, void* /*param*/) {
	static Card* card;
	static ofstream writer;
	static vector<Card*> hand;
	static int index, size, width, startX, checksum;

	if (event == EVENT_LBUTTONDOWN) {
		// Only response to left-click events, and ignore the others
		if (y >= 21 && y <= 42 && x >= 1140 && x <= 1260) {
			// <QUIT> button
			// Store statistics data to UnoStat.tmp file
			writer = ofstream("UnoStat.tmp", ios::out | ios::binary);
			if (!writer.fail()) {
				// Store statistics data to file
				checksum = sEasyWin + sHardWin + sEasyTotal + sHardTotal;
				writer.write(FILE_HEADER, 8);
				writer.write((char*)&sEasyWin, sizeof(int));
				writer.write((char*)&sHardWin, sizeof(int));
				writer.write((char*)&sEasyTotal, sizeof(int));
				writer.write((char*)&sHardTotal, sizeof(int));
				writer.write((char*)&checksum, sizeof(int));
				writer.close();
			} // if (!writer.fail())

			destroyAllWindows();
			exit(0);
		} // if (y >= 21 && y <= 42 && x >= 1140 && x <= 1260)
		else switch (sStatus) {
		case STAT_WELCOME:
			if (y >= 270 && y <= 450) {
				if (x >= 420 && x <= 540) {
					// Difficulty: EASY
					sDifficulty = LV_EASY;
					sStatus = STAT_NEW_GAME;
					onStatusChanged(sStatus);
				} // if (x >= 420 && x <= 540)
				else if (x >= 740 && x <= 860) {
					// Difficulty: HARD
					sDifficulty = LV_HARD;
					sStatus = STAT_NEW_GAME;
					onStatusChanged(sStatus);
				} // else if (x >= 740 && x <= 860)
			} // if (y >= 270 && y <= 450)
			break; // case STAT_WELCOME

		case PLAYER_YOU:
			if (y >= 520 && y <= 700) {
				hand = sUno->getHandCardsOf(PLAYER_YOU);
				size = (int)hand.size();
				width = 45 * size + 75;
				startX = 640 - width / 2;
				if (x >= startX && x <= startX + width) {
					// Hand card area
					// Calculate which card clicked by the X-coordinate
					index = (x - startX) / 45;
					if (index >= size) {
						index = size - 1;
					} // if (index >= size)

					// Try to play it
					card = hand.at(index);
					if (card->isWild() && size > 1) {
						sStatus = STAT_WILD_COLOR;
						onStatusChanged(sStatus);
					} // if (card->isWild() && size > 1)
					else if (sUno->isLegalToPlay(card)) {
						play(index);
					} // else if (sUno->isLegalToPlay(card))
				} // if (x >= startX && x <= startX + width)
			} // if (y >= 520 && y <= 700)
			else if (y >= 270 && y <= 450 && x >= 420 && x <= 540) {
				// Card deck area, draw a card
				draw();
			} // else if (y >= 270 && y <= 450 && x >= 420 && x <= 540)
			break; // case PLAYER_YOU

		case STAT_WILD_COLOR:
			if (y > 296 && y < 360) {
				if (x > 386 && x < 450) {
					// Red sector
					sStatus = PLAYER_YOU;
					play(index, RED);
				} // if (x > 386 && x < 450)
				else if (x > 450 && x < 514) {
					// Blue sector
					sStatus = PLAYER_YOU;
					play(index, BLUE);
				} // else if (x > 450 && x < 514)
			} // if (y > 296 && y < 360)
			else if (y > 360 && y < 424) {
				if (x > 386 && x < 450) {
					// Yellow sector
					sStatus = PLAYER_YOU;
					play(index, YELLOW);
				} // if (x > 386 && x < 450)
				else if (x > 450 && x < 514) {
					// Green sector
					sStatus = PLAYER_YOU;
					play(index, GREEN);
				} // else if (x > 450 && x < 514)
			} // else if (y > 360 && y < 424)
			break; // case STAT_WILD_COLOR

		case STAT_GAME_OVER:
			if (y >= 270 && y <= 450 && x >= 420 && x <= 540) {
				// Card deck area, start a new game
				sStatus = STAT_NEW_GAME;
				onStatusChanged(sStatus);
			} // if (y >= 270 && y <= 450 && x >= 420 && x <= 540)
			break; // case STAT_GAME_OVER

		default:
			break; // default
		} // else switch (sStatus)
	} // if (event == EVENT_LBUTTONDOWN)
} // onMouse()

// E.O.F