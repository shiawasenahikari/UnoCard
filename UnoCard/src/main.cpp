////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include <Uno.h>
#include <string>
#include <vector>
#include <cstdlib>
#include <cstring>
#include <fstream>
#include <sstream>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>

using namespace cv;
using namespace std;

// Constants
static const int LV_EASY = 0;
static const int LV_HARD = 1;
static const int STAT_IDLE = 0x1111;
static const int STAT_WELCOME = 0x2222;
static const int STAT_NEW_GAME = 0x3333;
static const int STAT_GAME_OVER = 0x4444;
static const int STAT_WILD_COLOR = 0x5555;
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
static bool sAuto;
static bool sTest;
static Mat sScreen;
static int sStatus;
static int sWinner;
static int sEasyWin;
static int sHardWin;
static int sEasyTotal;
static int sHardTotal;
static int sDifficulty;
static bool sAIRunning;

// Functions
static void easyAI();
static void hardAI();
static void waitMs(int delay = 0);
static void onStatusChanged(int status);
static void refreshScreen(string message);
static void play(int index, Color color = NONE);
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
	sAuto = false;
	sTest = false;
	sAIRunning = false;
	sWinner = Player::YOU;
	sStatus = STAT_WELCOME;
	sScreen = sUno->getBackground().clone();
	namedWindow("Uno");
	refreshScreen("WELCOME TO UNO CARD GAME");
	setMouseCallback("Uno", onMouse, NULL);
	for (;;) {
		waitMs(0); // prevent from blocking main thread
	} // for (;;)
} // main()

/**
 * AI Strategies (Difficulty: EASY).
 */
static void easyAI() {
	Card* card;
	Card* last;
	Player* next;
	Player* oppo;
	Player* prev;
	int i, direction;
	vector<Card*> hand;
	Color bestColor, lastColor;
	bool hasZero, hasWild, hasWildDraw4;
	bool hasNum, hasRev, hasSkip, hasDraw2;
	int idxBest, idxRev, idxSkip, idxDraw2;
	int idxZero, idxNum, idxWild, idxWildDraw4;
	int yourSize, nextSize, oppoSize, prevSize;

	sAIRunning = true;
	while (sStatus == Player::COM1
		|| sStatus == Player::COM2
		|| sStatus == Player::COM3
		|| sStatus == Player::YOU && sAuto) {
		hand = sUno->getPlayer(sStatus)->getHandCards();
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

		direction = sUno->getDirection();
		next = sUno->getPlayer((sStatus + direction) % 4);
		nextSize = (int)next->getHandCards().size();
		oppo = sUno->getPlayer((sStatus + 2) % 4);
		oppoSize = (int)oppo->getHandCards().size();
		prev = sUno->getPlayer((4 + sStatus - direction) % 4);
		prevSize = (int)prev->getHandCards().size();
		idxBest = idxRev = idxSkip = idxDraw2 = -1;
		idxZero = idxNum = idxWild = idxWildDraw4 = -1;
		bestColor = sUno->bestColorFor(sStatus);
		last = sUno->getRecent().back();
		if (last->isWild()) {
			lastColor = last->getWildColor();
		} // if (last->isWild())
		else {
			lastColor = last->getColor();
		} // else

		for (i = 0; i < yourSize; ++i) {
			// Index of any kind
			card = hand.at(i);
			if (sUno->isLegalToPlay(card)) {
				switch (card->getContent()) {
				case NUM0:
					if (idxZero < 0 || card->getColor() == bestColor) {
						idxZero = i;
					} // if (idxZero < 0 || card->getColor() == bestColor)
					break; // case NUM0

				case DRAW2:
					if (idxDraw2 < 0 || card->getColor() == bestColor) {
						idxDraw2 = i;
					} // if (idxDraw2 < 0 || card->getColor() == bestColor)
					break; // case DRAW2

				case SKIP:
					if (idxSkip < 0 || card->getColor() == bestColor) {
						idxSkip = i;
					} // if (idxSkip < 0 || card->getColor() == bestColor)
					break; // case SKIP

				case REV:
					if (idxRev < 0 || card->getColor() == bestColor) {
						idxRev = i;
					} // if (idxRev < 0 || card->getColor() == bestColor)
					break; // case REV

				case WILD:
					idxWild = i;
					break; // case WILD

				case WILD_DRAW4:
					idxWildDraw4 = i;
					break; // case WILD_DRAW4

				default: // non-zero number cards
					if (idxNum < 0 || card->getColor() == bestColor) {
						idxNum = i;
					} // if (idxNum < 0 || card->getColor() == bestColor)
					break; // default
				} // switch (card->getContent())
			} // if (sUno->isLegalToPlay(card))
		} // for (i = 0; i < yourSize; ++i)

		// Decision tree
		hasNum = (idxNum >= 0);
		hasRev = (idxRev >= 0);
		hasZero = (idxZero >= 0);
		hasSkip = (idxSkip >= 0);
		hasWild = (idxWild >= 0);
		hasDraw2 = (idxDraw2 >= 0);
		hasWildDraw4 = (idxWildDraw4 >= 0);
		if (nextSize == 1) {
			// Strategies when your next player remains only one card.
			// Limit your next player's action as well as you can.
			if (hasDraw2) {
				// Play a [+2] to make your next player draw two cards!
				idxBest = idxDraw2;
			} // if (hasDraw2)
			else if (hasWildDraw4) {
				// Play a [wild +4] to make your next player draw four cards,
				// even if the legal color is already your best color!
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // else if (hasSkip)
			else if (hasRev) {
				// Play a [reverse] to get help from your opposite player.
				idxBest = idxRev;
			} // else if (hasRev)
			else if (hasWild && lastColor != bestColor) {
				// Play a [wild] and change the legal color to your best to
				// decrease its possibility of playing the final card legally.
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasZero) {
				// No more powerful choices. Play a number card.
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasNum) {
				idxBest = idxNum;
			} // else if (hasNum)
		} // if (nextSize == 1)
		else if (oppoSize == 1) {
			// Strategies when your opposite player remains only one card.
			// Give more freedom to your next player, the only one that can
			// directly limit your opposite player's action.
			if (hasRev && prevSize - nextSize >= 3) {
				// Play a [reverse] when your next player remains only a few
				// cards but your previous player remains a lot of cards,
				// because your previous player has more possibility to limit
				// your opposite player's action.
				idxBest = idxRev;
			} // if (hasRev && prevSize - nextSize >= 3)
			else if (hasNum) {
				// Then you can play a number card. In order to increase your
				// next player's possibility of changing the legal color, do
				// not play zero cards preferentially this time.
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasZero) {
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasWild && lastColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Do not play any [+2]/[skip] to your next player!
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasWildDraw4 && lastColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Specially, for [wild +4] cards, you can only play it
				// when your next player remains only a few cards, and what you
				// did can help your next player find more useful cards, such as
				// action cards, or [wild +4] cards.
				if (nextSize <= 4) {
					idxBest = idxWildDraw4;
				} // if (nextSize <= 4)
			} // else if (hasWildDraw4 && lastColor != bestColor)
		} // else if (oppoSize == 1)
		else {
			// Normal strategies
			if (hasZero) {
				// Play zero cards at first because of their rarity.
				idxBest = idxZero;
			} // if (hasZero)
			else if (hasNum) {
				// Then consider to play a number card.
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasRev && prevSize >= 3) {
				// Then consider to play an action card.
				idxBest = idxRev;
			} // else if (hasRev && prevSize >= 3)
			else if (hasSkip) {
				idxBest = idxSkip;
			} // else if (hasSkip)
			else if (hasDraw2) {
				idxBest = idxDraw2;
			} // else if (hasDraw2)
			else if (hasWild) {
				// Then consider to play a wild card.
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasWildDraw4) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
		} // else

		if (idxBest >= 0) {
			// Found an appropriate card to play
			play(idxBest, bestColor);
		} // if (idxBest >= 0)
		else {
			// No appropriate cards to play, or no card is legal to play
			draw();
		} // else
	} // while (sStatus == Player::COM1 || ...)

	sAIRunning = false;
} // easyAI()

/**
 * AI Strategies (Difficulty: HARD).
 */
static void hardAI() {
	Card* card;
	Card* last;
	Player* next;
	Player* oppo;
	Player* prev;
	Color bestColor;
	Color lastColor;
	int i, direction;
	Color dangerColor;
	vector<Card*> hand;
	bool hasZero, hasWild, hasWildDraw4;
	bool hasNum, hasRev, hasSkip, hasDraw2;
	int idxBest, idxRev, idxSkip, idxDraw2;
	int idxZero, idxNum, idxWild, idxWildDraw4;
	int yourSize, nextSize, oppoSize, prevSize;

	sAIRunning = true;
	while (sStatus == Player::COM1
		|| sStatus == Player::COM2
		|| sStatus == Player::COM3
		|| sStatus == Player::YOU && sAuto) {
		hand = sUno->getPlayer(sStatus)->getHandCards();
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

		direction = sUno->getDirection();
		next = sUno->getPlayer((sStatus + direction) % 4);
		nextSize = (int)next->getHandCards().size();
		oppo = sUno->getPlayer((sStatus + 2) % 4);
		oppoSize = (int)oppo->getHandCards().size();
		prev = sUno->getPlayer((4 + sStatus - direction) % 4);
		prevSize = (int)prev->getHandCards().size();
		idxBest = idxRev = idxSkip = idxDraw2 = -1;
		idxZero = idxNum = idxWild = idxWildDraw4 = -1;
		bestColor = sUno->bestColorFor(sStatus);
		last = sUno->getRecent().back();
		if (last->isWild()) {
			lastColor = last->getWildColor();
		} // if (last->isWild())
		else {
			lastColor = last->getColor();
		} // else

		for (i = 0; i < yourSize; ++i) {
			// Index of any kind
			card = hand.at(i);
			if (sUno->isLegalToPlay(card)) {
				switch (card->getContent()) {
				case NUM0:
					if (idxZero < 0 || card->getColor() == bestColor) {
						idxZero = i;
					} // if (idxZero < 0 || card->getColor() == bestColor)
					break; // case NUM0

				case DRAW2:
					if (idxDraw2 < 0 || card->getColor() == bestColor) {
						idxDraw2 = i;
					} // if (idxDraw2 < 0 || card->getColor() == bestColor)
					break; // case DRAW2

				case SKIP:
					if (idxSkip < 0 || card->getColor() == bestColor) {
						idxSkip = i;
					} // if (idxSkip < 0 || card->getColor() == bestColor)
					break; // case SKIP

				case REV:
					if (idxRev < 0 || card->getColor() == bestColor) {
						idxRev = i;
					} // if (idxRev < 0 || card->getColor() == bestColor)
					break; // case REV

				case WILD:
					idxWild = i;
					break; // case WILD

				case WILD_DRAW4:
					idxWildDraw4 = i;
					break; // case WILD_DRAW4

				default: // non-zero number cards
					if (idxNum < 0 || card->getColor() == bestColor) {
						idxNum = i;
					} // if (idxNum < 0 || card->getColor() == bestColor)
					break; // default
				} // switch (card->getContent())
			} // if (sUno->isLegalToPlay(card))
		} // for (i = 0; i < yourSize; ++i)

		// Decision tree
		hasNum = (idxNum >= 0);
		hasRev = (idxRev >= 0);
		hasZero = (idxZero >= 0);
		hasSkip = (idxSkip >= 0);
		hasWild = (idxWild >= 0);
		hasDraw2 = (idxDraw2 >= 0);
		hasWildDraw4 = (idxWildDraw4 >= 0);
		if (nextSize == 1) {
			// Strategies when your next player remains only one card.
			// Limit your next player's action as well as you can.
			dangerColor = next->getDangerousColor();
			if (hasDraw2) {
				// Play a [+2] to make your next player draw two cards!
				idxBest = idxDraw2;
			} // if (hasDraw2)
			else if (lastColor == dangerColor) {
				// Your next player played a wild card, started a UNO dash in
				// its last action, and what's worse is that the legal color has
				// not been changed yet. You have to change the following legal
				// color, or you will approximately 100% lose this game.
				if (hasZero && hand.at(idxZero)->getColor() != dangerColor) {
					// When you have no [+2] cards, you have to change the legal
					// color, or use [wild +4] cards. At first, try to change
					// legal color by playing a number card, instead of using
					// wild cards.
					idxBest = idxZero;
				} // if (hasZero && ...)
				else if (hasNum && hand.at(idxNum)->getColor() != dangerColor) {
					idxBest = idxNum;
				} // else if (hasNum && ...)
				else if (hasSkip) {
					// Play a [skip] to skip its turn and wait for more chances.
					idxBest = idxSkip;
				} // else if (hasSkip)
				else if (hasWildDraw4) {
					// Now start to use wild cards. Use [wild +4] cards priorly,
					// because this card makes your next player draw four cards.
					while (bestColor == oppo->getDangerousColor()
						|| bestColor == prev->getDangerousColor()) {
						bestColor = Color(rand() % 4 + 1);
					} // while (bestColor == oppo->getDangerousColor() || ...)

					idxBest = idxWildDraw4;
				} // else if (hasWildDraw4)
				else if (hasWild) {
					// If you only have [wild] cards, firstly consider to change
					// to your best color, but when your next player's last card
					// has the same color to your best color, you have to change
					// to another color.
					while (bestColor == dangerColor
						|| bestColor == oppo->getDangerousColor()
						|| bestColor == prev->getDangerousColor()) {
						bestColor = Color(rand() % 4 + 1);
					} // while (bestColor == dangerColor || ...)

					idxBest = idxWild;
				} // else if (hasWild)
				else if (hasRev) {
					// Finally play a [reverse] to get help from other players.
					// If you even do not have this choice, you lose this game.
					idxBest = idxRev;
				} // else if (hasRev)
			} // else if (lastColor == dangerColor)
			else if (dangerColor != NONE) {
				// Your next player played a wild card, started a UNO dash in
				// its last action, but fortunately the legal color has been
				// changed already. Just be careful not to re-change the legal
				// color to the dangerous color again.
				if (hasZero && hand.at(idxZero)->getColor() != dangerColor) {
					idxBest = idxZero;
				} // if (hasZero && ...)
				else if (hasNum && hand.at(idxNum)->getColor() != dangerColor) {
					idxBest = idxNum;
				} // else if (hasNum && ...)
				else if (hasSkip && hand.at(idxSkip)->getColor() != dangerColor) {
					idxBest = idxSkip;
				} // else if (hasSkip && ...)
				else if (hasRev && hand.at(idxRev)->getColor() != dangerColor) {
					if (prevSize >= 4) {
						idxBest = idxRev;
					} // if (prevSize >= 4)
				} // else if (hasRev && ...)
			} // else if (dangerColor != NONE)
			else if (hasWildDraw4) {
				// Your next player started a UNO dash without playing a wild
				// card, so use normal defense strategies. Firstly play a
				// [wild +4] to make your next player draw four cards, even if
				// the legal color is already your best color!
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // else if (hasSkip)
			else if (hasRev) {
				// Play a [reverse] to get help from your opposite player.
				idxBest = idxRev;
			} // else if (hasRev)
			else if (hasWild && lastColor != bestColor) {
				// Play a [wild] and change the legal color to your best to
				// decrease its possibility of playing the final card legally.
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasZero) {
				// No more powerful choices. Play a number card.
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasNum) {
				idxBest = idxNum;
			} // else if (hasNum)
		} // if (nextSize == 1)
		else if (prevSize == 1) {
			// Strategies when your previous player remains only one card.
			// Save your action cards as much as you can. once a reverse card is
			// played, you can use these cards to limit your previous player's
			// action.
			dangerColor = prev->getDangerousColor();
			if (lastColor == dangerColor) {
				// Your previous player played a wild card, started a UNO dash
				// in its last action. You have to change the following legal
				// color, or you will approximately 100% lose this game.
				if (hasSkip && hand.at(idxSkip)->getColor() != dangerColor) {
					// When your opposite player played a [skip], and you have a
					// [skip] with different color, play it.
					idxBest = idxSkip;
				} // if (hasSkip && ...)
				else if (hasWild) {
					// Now start to use wild cards. Firstly consider to change
					// to your best color, but when your next player's last card
					// has the same color to your best color, you have to change
					// to another color.
					while (bestColor == dangerColor
						|| bestColor == next->getDangerousColor()
						|| bestColor == oppo->getDangerousColor()) {
						bestColor = Color(rand() % 4 + 1);
					} // while (bestColor == dangerColor || ...)

					idxBest = idxWild;
				} // else if (hasWild)
				else if (hasWildDraw4) {
					while (bestColor == dangerColor
						|| bestColor == next->getDangerousColor()
						|| bestColor == oppo->getDangerousColor()) {
						bestColor = Color(rand() % 4 + 1);
					} // while (bestColor == dangerColor || ...)

					idxBest = idxWild;
				} // else if (hasWildDraw4)
				else if (hasNum) {
					// When you have no wild cards, play a number card and try
					// to get help from other players. In order to increase your
					// following players' possibility of changing the legal
					// color, do not play zero cards priorly.
					idxBest = idxNum;
				} // else if (hasNum)
				else if (hasZero) {
					idxBest = idxZero;
				} // else if (hasZero)
			} // if (lastColor == dangerColor)
			else if (hasNum) {
				// Your next player started a UNO dash without playing a wild
				// card, so use normal defense strategies. In order to increase
				// your following players' possibility of changing the legal
				// color, do not play zero cards priorly.
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasZero) {
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasWild && lastColor != bestColor) {
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasWildDraw4 && lastColor != bestColor) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4 && lastColor != bestColor)
		} // else if (prevSize == 1)
		else if (oppoSize == 1) {
			// Strategies when your opposite player remains only one card.
			// Give more freedom to your next player, the only one that can
			// directly limit your opposite player's action.
			dangerColor = oppo->getDangerousColor();
			if (lastColor == dangerColor) {
				// Your opposite player played a wild card, started a UNO dash
				// in its last action, and what's worse is that the legal color
				// has not been changed yet. You have to change the following
				// legal color, or you will approximately 100% lose this game.
				if (hasZero && hand.at(idxZero)->getColor() != dangerColor) {
					// At first, try to change legal color by playing an action
					// card or a number card, instead of using wild cards.
					idxBest = idxZero;
				} // if (hasZero && ...)
				else if (hasNum && hand.at(idxNum)->getColor() != dangerColor) {
					idxBest = idxNum;
				} // else if (hasNum && ...)
				else if (hasRev && hand.at(idxRev)->getColor() != dangerColor) {
					idxBest = idxRev;
				} // else if (hasRev && ...)
				else if (hasSkip && hand.at(idxSkip)->getColor() != dangerColor) {
					idxBest = idxSkip;
				} // else if (hasSkip && ...)
				else if (hasDraw2 && hand.at(idxDraw2)->getColor() != dangerColor) {
					idxBest = idxDraw2;
				} // else if (hasDraw2 && ...)
				else if (hasWild) {
					// Now start to use your wild cards. Firstly consider to
					// change to your best color, but when your next player's
					// last card has the same color to your best color, you
					// have to change to another color.
					while (bestColor == dangerColor
						|| bestColor == next->getDangerousColor()
						|| bestColor == prev->getDangerousColor()) {
						bestColor = Color(rand() % 4 + 1);
					} // while (bestColor == dangerColor || ...)

					idxBest = idxWild;
				} // else if (hasWild)
				else if (hasWildDraw4) {
					while (bestColor == dangerColor
						|| bestColor == next->getDangerousColor()
						|| bestColor == prev->getDangerousColor()) {
						bestColor = Color(rand() % 4 + 1);
					} // while (bestColor == dangerColor || ...)

					idxBest = idxWildDraw4;
				} // else if (hasWildDraw4)
				else if (hasRev && prevSize - nextSize >= 3) {
					// Finally try to get help from other players.
					idxBest = idxRev;
				} // else if (hasRev && prevSize - nextSize >= 3)
				else if (hasNum) {
					idxBest = idxNum;
				} // else if (hasNum)
				else if (hasZero) {
					idxBest = idxZero;
				} // else if (hasZero)
			} // if (lastColor == dangerColor)
			else if (dangerColor != NONE) {
				// Your opposite player played a wild card, started a UNO dash
				// in its last action, but fortunately the legal color has been
				// changed already. Just be careful not to re-change the legal
				// color to the dangerous color again.
				if (hasZero && hand.at(idxZero)->getColor() != dangerColor) {
					idxBest = idxZero;
				} // if (hasZero && ...)
				else if (hasNum && hand.at(idxNum)->getColor() != dangerColor) {
					idxBest = idxNum;
				} // else if (hasNum && ...)
				else if (hasSkip && hand.at(idxSkip)->getColor() != dangerColor) {
					idxBest = idxSkip;
				} // else if (hasSkip && ...)
				else if (hasDraw2 && hand.at(idxDraw2)->getColor() != dangerColor) {
					idxBest = idxDraw2;
				} // else if (hasDraw2 && ...)
				else if (hasRev && hand.at(idxRev)->getColor() != dangerColor) {
					if (prevSize >= 4) {
						idxBest = idxRev;
					} // if (prevSize >= 4)
				} // else if (hasRev && ...)
			} // else if (dangerColor != NONE)
			else if (hasRev && prevSize - nextSize >= 3) {
				// Your opposite player started a UNO dash without playing a
				// wild card, so use normal defense strategies. Firstly play a
				// [reverse] when your next player remains only a few cards but
				// your previous player remains a lot of cards, because your
				// previous player has more possibility to limit your opposite
				// player's action.
				idxBest = idxRev;
			} // else if (hasRev && prevSize - nextSize >= 3)
			else if (hasNum) {
				// Then you can play a number card. In order to increase your
				// next player's possibility of changing the legal color, do
				// not play zero cards priorly.
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasZero) {
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasWild && lastColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Do not play any [+2]/[skip] to your next player!
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasWildDraw4 && lastColor != bestColor) {
				// When you have no more legal number/reverse cards to play, try
				// to play a wild card and change the legal color to your best
				// color. Specially, for [wild +4] cards, you can only play it
				// when your next player remains only a few cards, and what you
				// did can help your next player find more useful cards, such as
				// action cards, or [wild +4] cards.
				if (nextSize <= 4) {
					idxBest = idxWildDraw4;
				} // if (nextSize <= 4)
			} // else if (hasWildDraw4 && lastColor != bestColor)
		} // else if (oppoSize == 1)
		else if (next->getRecent() == NULL && yourSize > 2) {
			// Strategies when your next player drew a card in its last action.
			// You do not need to play your limitation/wild cards when you are
			// not ready to start dash. Use them in more dangerous cases.
			if (hasRev && prevSize - nextSize >= 3) {
				idxBest = idxRev;
			} // if (hasRev && prevSize - nextSize >= 3)
			else if (hasZero) {
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasNum) {
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasRev && prevSize >= 4) {
				idxBest = idxRev;
			} // else if (hasRev && prevSize >= 4)
		} // else if (next->getRecent() == NULL && yourSize > 2)
		else {
			// Normal strategies
			if (hasRev && prevSize - nextSize >= 3) {
				// Play a [reverse] when your next player remains only a few
				// cards but your previous player remains a lot of cards, in
				// order to balance everyone's hand-card amount.
				idxBest = idxRev;
			} // if (hasRev && prevSize - nextSize >= 3)
			else if (hasDraw2 && nextSize <= 4) {
				// Play a [+2] when your next player remains only a few cards.
				idxBest = idxDraw2;
			} // else if (hasDraw2 && nextSize <= 4)
			else if (hasSkip && nextSize <= 4) {
				// Play a [skip] when your next player remains only a few cards.
				idxBest = idxSkip;
			} // else if (hasSkip && nextSize <= 4)
			else if (hasZero) {
				// Play zero cards at first because of their rarity.
				idxBest = idxZero;
			} // else if (hasZero)
			else if (hasNum) {
				// Then consider to play a number card.
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasRev && prevSize >= 4) {
				// When you have no more legal number cards to play, you can
				// play a [reverse] safely when your previous player still has
				// a number of cards.
				idxBest = idxRev;
			} // else if (hasRev && prevSize >= 4)
			else if (hasWild && nextSize <= 4) {
				// When your next player remains only a few cards, and you have
				// no more legal number/action cards to play, try to play a
				// wild card and change the legal color to your best color.
				idxBest = idxWild;
			} // else if (hasWild && nextSize <= 4)
			else if (hasWildDraw4 && nextSize <= 4) {
				// When your next player remains only a few cards, and you have
				// no more legal number/action cards to play, try to play a
				// wild card and change the legal color to your best color.
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4 && nextSize <= 4)
			else if (hasWild && yourSize == 2 && prevSize <= 3) {
				// When you remain only 2 cards, including a wild card, and your
				// previous player seems no enough power to limit you (has too
				// few cards), start your UNO dash!
				if (hasDraw2) {
					// When you still have a [+2] card, play it, even if it's
					// not worth to let your next player draw cards.
					idxBest = idxDraw2;
				} // if (hasDraw2)
				else if (hasSkip) {
					// When you still have a [skip] card, play it, even if it's
					// not worth to let your next player skip its turn.
					idxBest = idxSkip;
				} // else if (hasSkip)
				else {
					// When you have no more legal draw2/skip cards, play your
					// wild card to start your UNO dash.
					idxBest = idxWild;
				} // else
			} // else if (hasWild && yourSize == 2 && prevSize <= 3)
			else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3) {
				if (hasDraw2) {
					idxBest = idxDraw2;
				} // if (hasDraw2)
				else if (hasSkip) {
					idxBest = idxSkip;
				} // else if (hasSkip)
				else {
					idxBest = idxWildDraw4;
				} // else
			} // else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3)
		} // else

		if (idxBest >= 0) {
			// Found an appropriate card to play
			play(idxBest, bestColor);
		} // if (idxBest >= 0)
		else {
			// No appropriate cards to play, or no card is legal to play
			draw();
		} // else
	} // while (sStatus == Player::COM1 || ...)

	sAIRunning = false;
} // hardAI()

/**
 * Wrapper function of cv::waitKey(). Enable test mode when "UnoTest" typed.
 */
static void waitMs(int delay) {
	static string input;
	static int key, len;

	key = waitKey(delay);
	if (key != -1) {
		input.append(1, (char)key);
		len = (int)input.size();
		if (len == 7 && strcmp(input.c_str(), "UnoTest") == 0) {
			// "UnoTest" typed. Enable or disable test mode.
			input.clear();
			sTest = !sTest;
		} // if (len == 7 && strcmp(input.c_str(), "UnoTest") == 0)
		else if (strncmp(input.c_str(), "UnoTest", len) != 0) {
			// Mismatched. Clear buffer.
			input.clear();
			if (key == 'U') {
				input.append(1, (char)key);
			} // if (key == 'U')
		} // else if (strncmp(input.c_str(), "UnoTest", len) != 0)
	} // if (key != -1)
} // waitMs()

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
		refreshScreen("GET READY");
		waitMs(2000);
		sStatus = sWinner;
		onStatusChanged(sStatus);
		break; // case STAT_NEW_GAME

	case Player::YOU:
		// Your turn, select a hand card to play, or draw a card
		if (sAuto) {
			if (!sAIRunning) {
				if (sDifficulty == LV_EASY) {
					easyAI();
				} // if (sDifficulty == LV_EASY)
				else {
					hardAI();
				} // else
			} // if (!sAIRunning)
		} // if (sAuto)
		else {
			refreshScreen("Your turn, play or draw a card");
		} // else
		break; // case Player::YOU

	case STAT_WILD_COLOR:
		// Need to specify the following legal color after played a
		// wild card. Draw color sectors in the center of screen
		refreshScreen("^ Specify the following legal color");
		rect = Rect(360, 270, 181, 181);
		sUno->getBackground()(rect).copyTo(sScreen(rect));
		center = Point(450, 360);
		axes = Size(90, 90);
		ellipse(sScreen, center, axes, 0, 0, -90, RGB_BLUE, -1, 16);
		ellipse(sScreen, center, axes, 0, 0, 90, RGB_GREEN, -1, 16);
		ellipse(sScreen, center, axes, 180, 0, 90, RGB_RED, -1, 16);
		ellipse(sScreen, center, axes, 180, 0, -90, RGB_YELLOW, -1, 16);
		imshow("Uno", sScreen);
		break; // case STAT_WILD_COLOR

	case Player::COM1:
	case Player::COM2:
	case Player::COM3:
		// AI players' turn
		if (!sAIRunning) {
			if (sDifficulty == LV_EASY) {
				easyAI();
			} // if (sDifficulty == LV_EASY)
			else {
				hardAI();
			} // else
		} // if (!sAIRunning)
		break; // case Player::COM1, Player::COM2, Player::COM3

	case STAT_GAME_OVER:
		// Game over
		if (sWinner == Player::YOU) {
			if (sDifficulty == LV_EASY) {
				++sEasyWin;
			} // if (sDifficulty == LV_EASY)
			else {
				++sHardWin;
			} // else
		} // if (sWinner == Player::YOU)

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
	int i, status, size, width, height, remain, used, easyRate, hardRate;

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

	// Right-bottom corner: <AUTO> button
	point.x = 1130;
	point.y = 700;
	if (sAuto) {
		putText(sScreen, "<AUTO>", point, FONT_SANS, 1.0, RGB_YELLOW);
	} // if (sAuto)
	else {
		putText(sScreen, "<AUTO>", point, FONT_SANS, 1.0, RGB_WHITE);
	} // else

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
		buff << "Remain/Used: " << remain << "/" << used;
		putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);

		// Left-center: Hand cards of Player West (COM1)
		hand = sUno->getPlayer(Player::COM1)->getHandCards();
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
			height = 40 * size + 140;
			roi.x = 20;
			roi.y = 360 - height / 2;
			if (sTest || status == STAT_GAME_OVER) {
				// Show remained cards to everyone when testing or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (sTest || status == STAT_GAME_OVER)
			else {
				// Only show card backs in game process
				image = sUno->getBackImage();
				for (i = 0; i < size; ++i) {
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (i = 0; i < size; ++i)
			} // else

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 47;
				point.y = 494;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else

		// Top-center: Hand cards of Player North (COM2)
		hand = sUno->getPlayer(Player::COM2)->getHandCards();
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
			width = 45 * size + 75;
			roi.x = 640 - width / 2;
			roi.y = 20;
			if (sTest || status == STAT_GAME_OVER) {
				// Show remained hand cards when testing or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.x += 45;
				} // for (Card* card : hand)
			} // if (sTest || status == STAT_GAME_OVER)
			else {
				// Only show card backs in game process
				image = sUno->getBackImage();
				for (i = 0; i < size; ++i) {
					image.copyTo(sScreen(roi), image);
					roi.x += 45;
				} // for (i = hand.begin(); i != hand.end(); ++i)
			} // else

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 500;
				point.y = 121;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else

		// Right-center: Hand cards of Player East (COM3)
		hand = sUno->getPlayer(Player::COM3)->getHandCards();
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
			height = 40 * size + 140;
			roi.x = 1140;
			roi.y = 360 - height / 2;
			if (sTest || status == STAT_GAME_OVER) {
				// Show remained hand cards when testing or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (sTest || status == STAT_GAME_OVER)
			else {
				// Only show card backs in game process
				image = sUno->getBackImage();
				for (i = 0; i < size; ++i) {
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (i = 0; i < size; ++i)
			} // else

			if (size == 1) {
				// Show "UNO" warning when only one card in hand
				point.x = 1166;
				point.y = 494;
				putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else

		// Bottom: Your hand cards
		hand = sUno->getPlayer(Player::YOU)->getHandCards();
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
				if (status == Player::YOU && sUno->isLegalToPlay(card)) {
					image = card->getImage();
				} // if (status == Player::YOU && sUno->isLegalToPlay(card))
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
	string message;
	int x, y, now, size, width, height, next, direction;

	now = sStatus;
	sStatus = STAT_IDLE; // block mouse click events when idle
	size = (int)sUno->getPlayer(now)->getHandCards().size();
	card = sUno->play(now, index, color);
	if (card != NULL) {
		image = card->getImage();
		switch (now) {
		case Player::COM1:
			height = 40 * size + 140;
			x = 160;
			y = 360 - height / 2 + 40 * index;
			break; // case Player::COM1

		case Player::COM2:
			width = 45 * size + 75;
			x = 640 - width / 2 + 45 * index;
			y = 70;
			break; // case Player::COM2

		case Player::COM3:
			height = 40 * size + 140;
			x = 1000;
			y = 360 - height / 2 + 40 * index;
			break; // case Player::COM3

		default:
			width = 45 * size + 75;
			x = 640 - width / 2 + 45 * index;
			y = 470;
			break; // default
		} // switch (now)

		// Animation
		roi = Rect(x, y, 121, 181);
		image.copyTo(sScreen(roi), image);
		imshow("Uno", sScreen);
		waitMs(300);
		if (sUno->getPlayer(now)->getHandCards().size() == 0) {
			// The player in action becomes winner when it played the
			// final card in its hand successfully
			sWinner = now;
			sStatus = STAT_GAME_OVER;
			onStatusChanged(sStatus);
		} // if (sUno->getPlayer(now)->getHandCards().size() == 0)
		else {
			// When the played card is an action card or a wild card,
			// do the necessary things according to the game rule
			message = NAME[now];
			switch (card->getContent()) {
			case DRAW2:
				direction = sUno->getDirection();
				next = (now + direction) % 4;
				message += ": Let " + NAME[next] + " draw 2 cards";
				refreshScreen(message);
				waitMs(1500);
				draw(next, 2);
				break; // case DRAW2

			case SKIP:
				direction = sUno->getDirection();
				next = (now + direction) % 4;
				if (next == Player::YOU) {
					message += ": Skip your turn";
				} // if (next == Player::YOU)
				else {
					message += ": Skip " + NAME[next] + "'s turn";
				} // else

				refreshScreen(message);
				waitMs(1500);
				sStatus = (next + direction) % 4;
				onStatusChanged(sStatus);
				break; // case SKIP

			case REV:
				direction = sUno->switchDirection();
				if (direction == Uno::DIR_LEFT) {
					message += ": Change direction to CLOCKWISE";
				} // if (direction == Uno::DIR_LEFT)
				else {
					message += ": Change direction to COUNTER CLOCKWISE";
				} // else

				refreshScreen(message);
				waitMs(1500);
				sStatus = (now + direction) % 4;
				onStatusChanged(sStatus);
				break; // case REV

			case WILD:
				direction = sUno->getDirection();
				message += ": Change the following legal color";
				refreshScreen(message);
				waitMs(1500);
				sStatus = (now + direction) % 4;
				onStatusChanged(sStatus);
				break; // case WILD

			case WILD_DRAW4:
				direction = sUno->getDirection();
				next = (now + direction) % 4;
				message += ": Let " + NAME[next] + " draw 4 cards";
				refreshScreen(message);
				waitMs(1500);
				draw(next, 4);
				break; // case WILD_DRAW4

			default:
				direction = sUno->getDirection();
				message += ": " + card->getName();
				refreshScreen(message);
				waitMs(1500);
				sStatus = (now + direction) % 4;
				onStatusChanged(sStatus);
				break; // default
			} // switch (card->getContent())
		} // else
	} // if (card != NULL)
} // play()

/**
 * Let a player draw one or more cards, and skip its turn.
 *
 * @param who   Who draws cards. Must be one of the following values:
 *              Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 *              Default to the player in turn, i.e. sStatus.
 * @param count How many cards to draw. Default to 1.
 */
static void draw(int who, int count) {
	int i;
	Rect roi;
	Mat image;
	Card* card;
	stringstream buff;

	sStatus = STAT_IDLE; // block mouse click events when idle
	for (i = 0; i < count; ++i) {
		buff.str("");
		card = sUno->draw(who);
		if (card != NULL) {
			switch (who) {
			case Player::COM1:
				image = sUno->getBackImage();
				roi = Rect(160, 270, 121, 181);
				if (count == 1) {
					buff << NAME[who] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[who] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM1

			case Player::COM2:
				image = sUno->getBackImage();
				roi = Rect(580, 70, 121, 181);
				if (count == 1) {
					buff << NAME[who] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[who] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM2

			case Player::COM3:
				image = sUno->getBackImage();
				roi = Rect(1000, 270, 121, 181);
				if (count == 1) {
					buff << NAME[who] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[who] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM3

			default:
				image = card->getImage();
				roi = Rect(580, 470, 121, 181);
				buff << NAME[who] << ": Draw " + card->getName();
				break; // default
			} // switch (who)

			// Animation
			image.copyTo(sScreen(roi), image);
			imshow("Uno", sScreen);
			waitMs(300);
			refreshScreen(buff.str());
			waitMs(300);
		} // if (card != NULL)
		else {
			buff << NAME[who];
			buff << " cannot hold more than ";
			buff << Uno::MAX_HOLD_CARDS << " cards";
			refreshScreen(buff.str());
			break;
		} // else
	} // for (i = 0; i < count; ++i)

	waitMs(1500);
	sStatus = (who + sUno->getDirection()) % 4;
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
	static Point point;
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
		else if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260) {
			// <AUTO> button
			// In player's action, automatically play or draw cards by AI
			sAuto = !sAuto;
			switch (sStatus) {
			case Player::YOU:
				onStatusChanged(sStatus);
				break; // case Player::YOU

			case STAT_WILD_COLOR:
				sStatus = Player::YOU;
				onStatusChanged(sStatus);
				break; // case STAT_WILD_COLOR

			default:
				point = Point(1130, 700);
				if (sAuto) {
					putText(sScreen, "<AUTO>", point, FONT_SANS, 1.0, RGB_YELLOW);
				} // if (sAuto)
				else {
					putText(sScreen, "<AUTO>", point, FONT_SANS, 1.0, RGB_WHITE);
				} // else

				imshow("Uno", sScreen);
				break; // default
			} // switch (sStatus)
		} // else if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260)
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

		case Player::YOU:
			if (sAuto) {
				break; // case Player::YOU
			} // if (sAuto)
			else if (y >= 520 && y <= 700) {
				hand = sUno->getPlayer(Player::YOU)->getHandCards();
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
			} // else if (y >= 520 && y <= 700)
			else if (y >= 270 && y <= 450 && x >= 420 && x <= 540) {
				// Card deck area, draw a card
				draw();
			} // else if (y >= 270 && y <= 450 && x >= 420 && x <= 540)
			break; // case Player::YOU

		case STAT_WILD_COLOR:
			sStatus = Player::YOU;
			if (y > 296 && y < 360 && x > 386 && x < 450) {
				// Red sector
				play(index, RED);
			} // if (y > 296 && y < 360 && x > 386 && x < 450)
			else if (y > 296 && y < 360 && x > 450 && x < 514) {
				// Blue sector
				play(index, BLUE);
			} // else if (y > 296 && y < 360 && x > 450 && x < 514)
			else if (y > 360 && y < 424 && x > 386 && x < 450) {
				// Yellow sector
				play(index, YELLOW);
			} // else if (y > 360 && y < 424 && x > 386 && x < 450)
			else if (y > 360 && y < 424 && x > 450 && x < 514) {
				// Green sector
				play(index, GREEN);
			} // else if (y > 360 && y < 424 && x > 450 && x < 514)
			else {
				// Undo
				onStatusChanged(sStatus);
			} // else
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