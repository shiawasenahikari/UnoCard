////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include <AI.h>
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
static Card* sDrawnCard;
static bool sImmPlayAsk;
static bool sChallenged;
static bool sChallengeAsk;

// Functions
static void easyAI();
static void hardAI();
static void onStatusChanged(int status);
static void refreshScreen(string message);
static void play(int index, Color color = NONE);
static void draw(int count = 1, bool force = false);
static void onChallengeChance(bool challenged = true);
static void onMouse(int event, int x, int y, int flags, void* param);

// Macros
#define WAIT_MS(delay) if (waitKey(delay) == '*') sTest = !sTest

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
	sWinner = Player::YOU;
	sStatus = STAT_WELCOME;
	sScreen = sUno->getBackground().clone();
	namedWindow("Uno");
	refreshScreen("WELCOME TO UNO CARD GAME");
	setMouseCallback("Uno", onMouse, nullptr);
	for (;;) {
		WAIT_MS(0); // prevent from blocking main thread
	} // for (;;)
} // main()

/**
 * AI Strategies (Difficulty: EASY).
 */
static void easyAI() {
	int idxBest, now;
	Color bestColor[1];

	sAIRunning = true;
	while (sStatus == Player::COM1
		|| sStatus == Player::COM2
		|| sStatus == Player::COM3
		|| sStatus == Player::YOU && sAuto) {
		if (sChallengeAsk) {
			onChallengeChance(needToChallenge(sStatus));
		} // if (sChallengeAsk)
		else {
			now = sStatus;
			sStatus = STAT_IDLE; // block mouse click events when idle
			idxBest = easyAI_bestCardIndex4NowPlayer(
				/* drawnCard */ sImmPlayAsk ? sDrawnCard : nullptr,
				/* outColor  */ bestColor
			); // idxBest = easyAI_bestCardIndex4NowPlayer()

			if (idxBest >= 0) {
				// Found an appropriate card to play
				sImmPlayAsk = false;
				play(idxBest, bestColor[0]);
			} // if (idxBest >= 0)
			else {
				// No appropriate cards to play, or no card is legal to play
				if (sImmPlayAsk) {
					sImmPlayAsk = false;
					refreshScreen(NAME[now] + ": Pass");
					WAIT_MS(750);
					sStatus = sUno->switchNow();
					onStatusChanged(sStatus);
				} // if (sImmPlayAsk)
				else {
					draw();
				} // else
			} // else
		} // else
	} // while (sStatus == Player::COM1 || ...)

	sAIRunning = false;
} // easyAI()

/**
 * AI Strategies (Difficulty: HARD).
 */
static void hardAI() {
	int idxBest, now;
	Color bestColor[1];

	sAIRunning = true;
	while (sStatus == Player::COM1
		|| sStatus == Player::COM2
		|| sStatus == Player::COM3
		|| sStatus == Player::YOU && sAuto) {
		if (sChallengeAsk) {
			onChallengeChance(needToChallenge(sStatus));
		} // if (sChallengeAsk)
		else {
			now = sStatus;
			sStatus = STAT_IDLE; // block mouse click events when idle
			idxBest = hardAI_bestCardIndex4NowPlayer(
				/* drawnCard */ sImmPlayAsk ? sDrawnCard : nullptr,
				/* outColor  */ bestColor
			); // idxBest = hardAI_bestCardIndex4NowPlayer()

			if (idxBest >= 0) {
				// Found an appropriate card to play
				sImmPlayAsk = false;
				play(idxBest, bestColor[0]);
			} // if (idxBest >= 0)
			else {
				// No appropriate cards to play, or no card is legal to play
				if (sImmPlayAsk) {
					sImmPlayAsk = false;
					refreshScreen(NAME[now] + ": Pass");
					WAIT_MS(750);
					sStatus = sUno->switchNow();
					onStatusChanged(sStatus);
				} // if (sImmPlayAsk)
				else {
					draw();
				} // else
			} // else
		} // else
	} // while (sStatus == Player::COM1 || ...)

	sAIRunning = false;
} // hardAI()

/**
 * Triggered when the value of global value [sStatus] changed.
 *
 * @param status New status value.
 */
static void onStatusChanged(int status) {
	Rect rect;
	Size axes;
	Point center;

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
		WAIT_MS(2000);
		switch (sUno->getRecent().at(0)->getContent()) {
		case DRAW2:
			// If starting with a [+2], let dealer draw 2 cards.
			draw(2, /* force */ true);
			break; // case DRAW2

		case SKIP:
			// If starting with a [skip], skip dealer's turn.
			refreshScreen(NAME[sUno->getNow()] + ": Skipped");
			WAIT_MS(1500);
			sStatus = sUno->switchNow();
			onStatusChanged(sStatus);
			break; // case SKIP

		case REV:
			// If starting with a [reverse], change the action
			// sequence to COUNTER CLOCKWISE.
			sUno->switchDirection();
			refreshScreen("Direction changed");
			WAIT_MS(1500);
			sStatus = sUno->getNow();
			onStatusChanged(sStatus);
			break; // case REV

		default:
			// Otherwise, go to dealer's turn.
			sStatus = sUno->getNow();
			onStatusChanged(sStatus);
			break; // default
		} // switch (sUno->getRecent().back()->getContent())
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
		else if (sImmPlayAsk) {
			refreshScreen("^ Play " + sDrawnCard->getName() + "?");
			rect = Rect(338, 270, 121, 181);
			sUno->getBackground()(rect).copyTo(sScreen(rect));
			center = Point(405, 315);
			axes = Size(135, 135);

			// Draw YES button
			ellipse(sScreen, center, axes, 0, 0, -180, RGB_GREEN, -1, LINE_AA);
			putText(
				/* img       */ sScreen,
				/* text      */ "YES",
				/* org       */ Point(346, 295),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // putText()

			// Draw NO button
			ellipse(sScreen, center, axes, 0, 0, 180, RGB_RED, -1, LINE_AA);
			putText(
				/* img       */ sScreen,
				/* text      */ "NO",
				/* org       */ Point(360, 378),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // putText()

			// Show screen
			imshow("Uno", sScreen);
		} // else if (sImmPlayAsk)
		else if (sChallengeAsk) {
			refreshScreen("^ Challenge the legality of Wild +4?");
			rect = Rect(338, 270, 121, 181);
			sUno->getBackground()(rect).copyTo(sScreen(rect));
			center = Point(405, 315);
			axes = Size(135, 135);

			// Draw YES button
			ellipse(sScreen, center, axes, 0, 0, -180, RGB_GREEN, -1, LINE_AA);
			putText(
				/* img       */ sScreen,
				/* text      */ "YES",
				/* org       */ Point(346, 295),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // putText()

			// Draw NO button
			ellipse(sScreen, center, axes, 0, 0, 180, RGB_RED, -1, LINE_AA);
			putText(
				/* img       */ sScreen,
				/* text      */ "NO",
				/* org       */ Point(360, 378),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // putText()

			// Show screen
			imshow("Uno", sScreen);
		} // else if (sChallengeAsk)
		else {
			refreshScreen("Your turn, play or draw a card");
		} // else
		break; // case Player::YOU

	case STAT_WILD_COLOR:
		// Need to specify the following legal color after played a
		// wild card. Draw color sectors in the center of screen
		refreshScreen("^ Specify the following legal color");
		rect = Rect(338, 270, 121, 181);
		sUno->getBackground()(rect).copyTo(sScreen(rect));
		center = Point(405, 315);
		axes = Size(135, 135);
		ellipse(sScreen, center, axes, 0, 0, -90, RGB_BLUE, -1, LINE_AA);
		ellipse(sScreen, center, axes, 0, 0, 90, RGB_GREEN, -1, LINE_AA);
		ellipse(sScreen, center, axes, 180, 0, 90, RGB_RED, -1, LINE_AA);
		ellipse(sScreen, center, axes, 180, 0, -90, RGB_YELLOW, -1, LINE_AA);
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
		if (sAuto) {
			WAIT_MS(5000);
			if (sAuto && sStatus == STAT_GAME_OVER) {
				sStatus = STAT_NEW_GAME;
				onStatusChanged(sStatus);
			} // if (sAuto && sStatus == STAT_GAME_OVER)
		} // if (sAuto)
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
	bool beChallenged;
	stringstream buff;
	vector<Card*> hand;
	int i, status, size, width, height;
	int remain, used, easyRate, hardRate;

	// Lock the value of global variable [sStatus]
	status = sStatus;

	// Clear
	sUno->getBackground().copyTo(sScreen);

	// Message area
	width = getTextSize(message, FONT_SANS, 1.0, 1, nullptr).width;
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
		roi = Rect(490, 270, 121, 181);
		image.copyTo(sScreen(roi), image);
		image = sUno->getHardImage();
		roi.x = 670;
		roi.y = 270;
		image.copyTo(sScreen(roi), image);
		easyRate = (sEasyTotal == 0 ? 0 : 100 * sEasyWin / sEasyTotal);
		hardRate = (sHardTotal == 0 ? 0 : 100 * sHardWin / sHardTotal);
		buff << easyRate << "% WinRate " << hardRate << "%";
		width = getTextSize(buff.str(), FONT_SANS, 1.0, 1, nullptr).width;
		point.x = 640 - width / 2;
		point.y = 250;
		putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);
	} // if (status == STAT_WELCOME)
	else {
		// Center: card deck & recent played card
		image = sUno->getBackImage();
		roi = Rect(338, 270, 121, 181);
		image.copyTo(sScreen(roi), image);
		hand = sUno->getRecent();
		size = (int)hand.size();
		width = 45 * size + 75;
		roi.x = 792 - width / 2;
		roi.y = 270;
		for (Card* recent : hand) {
			if (recent->getContent() == WILD) {
				image = sUno->getColoredWildImage(recent->getRealColor());
			} // if (recent->getContent() == WILD)
			else if (recent->getContent() == WILD_DRAW4) {
				image = sUno->getColoredWildDraw4Image(recent->getRealColor());
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
			beChallenged = sChallenged && sUno->getNow() == Player::COM1;
			if (beChallenged || sTest || status == STAT_GAME_OVER) {
				// Show remained cards to everyone
				// when being challenged, testing, or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (beChallenged || sTest || status == STAT_GAME_OVER)
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
			beChallenged = sChallenged && sUno->getNow() == Player::COM2;
			if (beChallenged || sTest || status == STAT_GAME_OVER) {
				// Show remained cards to everyone
				// when being challenged, testing, or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.x += 45;
				} // for (Card* card : hand)
			} // if (beChallenged || sTest || status == STAT_GAME_OVER)
			else {
				// Only show card backs in game process
				image = sUno->getBackImage();
				for (i = 0; i < size; ++i) {
					image.copyTo(sScreen(roi), image);
					roi.x += 45;
				} // for (i = 0; i < size; ++i)
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
			beChallenged = sChallenged && sUno->getNow() == Player::COM3;
			if (beChallenged || sTest || status == STAT_GAME_OVER) {
				// Show remained cards to everyone
				// when being challenged, testing, or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (beChallenged || sTest || status == STAT_GAME_OVER)
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
				switch (status) {
				case Player::YOU:
					if (sImmPlayAsk) {
						image = (card == sDrawnCard) ?
							card->getImage() :
							card->getDarkImg();
					} // if (sImmPlayAsk)
					else if (sChallengeAsk || sChallenged) {
						image = card->getDarkImg();
					} // else if (sChallengeAsk || sChallenged)
					else {
						image = (sUno->isLegalToPlay(card)) ?
							card->getImage() :
							card->getDarkImg();
					} // else
					break; // case Player::YOU

				case STAT_GAME_OVER:
					image = card->getImage();
					break; // case STAT_GAME_OVER

				default:
					image = card->getDarkImg();
					break; // default
				} // switch (status)

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
	int x, y, now, size, width, height, next;

	sStatus = STAT_IDLE; // block mouse click events when idle
	now = sUno->getNow();
	size = (int)sUno->getPlayer(now)->getHandCards().size();
	card = sUno->play(now, index, color);
	if (card != nullptr) {
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
		WAIT_MS(300);
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
				next = sUno->switchNow();
				message += ": Let " + NAME[next] + " draw 2 cards";
				refreshScreen(message);
				WAIT_MS(1500);
				draw(2, /* force */ true);
				break; // case DRAW2

			case SKIP:
				next = sUno->switchNow();
				if (next == Player::YOU) {
					message += ": Skip your turn";
				} // if (next == Player::YOU)
				else {
					message += ": Skip " + NAME[next] + "'s turn";
				} // else

				refreshScreen(message);
				WAIT_MS(1500);
				sStatus = sUno->switchNow();
				onStatusChanged(sStatus);
				break; // case SKIP

			case REV:
				if (sUno->switchDirection() == Uno::DIR_LEFT) {
					message += ": Change direction to CLOCKWISE";
				} // if (sUno->switchDirection() == Uno::DIR_LEFT)
				else {
					message += ": Change direction to COUNTER CLOCKWISE";
				} // else

				refreshScreen(message);
				WAIT_MS(1500);
				sStatus = sUno->switchNow();
				onStatusChanged(sStatus);
				break; // case REV

			case WILD:
				message += ": Change the following legal color";
				refreshScreen(message);
				WAIT_MS(1500);
				sStatus = sUno->switchNow();
				onStatusChanged(sStatus);
				break; // case WILD

			case WILD_DRAW4:
				next = sUno->getNext();
				message += ": Let " + NAME[next] + " draw 4 cards";
				refreshScreen(message);
				WAIT_MS(1500);
				sStatus = next;
				sChallengeAsk = true;
				onStatusChanged(sStatus);
				break; // case WILD_DRAW4

			default:
				message += ": " + card->getName();
				refreshScreen(message);
				WAIT_MS(1500);
				sStatus = sUno->switchNow();
				onStatusChanged(sStatus);
				break; // default
			} // switch (card->getContent())
		} // else
	} // if (card != nullptr)
} // play()

/**
 * The player in action draw one or more cards.
 *
 * @param count How many cards to draw.
 * @param force Pass true if the specified player is required to draw cards,
 *              i.e. previous player played a [+2] or [wild +4] to let this
 *              player draw cards. Or false if the specified player draws a
 *              card by itself in its action.
 */
static void draw(int count, bool force) {
	Rect roi;
	Mat image;
	int i, now;
	stringstream buff;

	sStatus = STAT_IDLE; // block mouse click events when idle
	now = sUno->getNow();
	for (i = 0; i < count; ++i) {
		buff.str("");
		sDrawnCard = sUno->draw(now, force);
		if (sDrawnCard != nullptr) {
			switch (now) {
			case Player::COM1:
				image = sUno->getBackImage();
				roi = Rect(160, 270, 121, 181);
				if (count == 1) {
					buff << NAME[now] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[now] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM1

			case Player::COM2:
				image = sUno->getBackImage();
				roi = Rect(580, 70, 121, 181);
				if (count == 1) {
					buff << NAME[now] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[now] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM2

			case Player::COM3:
				image = sUno->getBackImage();
				roi = Rect(1000, 270, 121, 181);
				if (count == 1) {
					buff << NAME[now] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[now] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM3

			default:
				image = sDrawnCard->getImage();
				roi = Rect(580, 470, 121, 181);
				buff << NAME[now] << ": Draw " + sDrawnCard->getName();
				break; // default
			} // switch (now)

			// Animation
			image.copyTo(sScreen(roi), image);
			imshow("Uno", sScreen);
			WAIT_MS(300);
			refreshScreen(buff.str());
			WAIT_MS(300);
		} // if (sDrawnCard != nullptr)
		else {
			buff << NAME[now];
			buff << " cannot hold more than ";
			buff << Uno::MAX_HOLD_CARDS << " cards";
			refreshScreen(buff.str());
			break;
		} // else
	} // for (i = 0; i < count; ++i)

	WAIT_MS(750);
	if (count == 1 &&
		sDrawnCard != nullptr &&
		sUno->isLegalToPlay(sDrawnCard)) {
		// Player drew one card by itself, the drawn card
		// can be played immediately if it's legal to play
		sStatus = now;
		sImmPlayAsk = true;
		onStatusChanged(sStatus);
	} // if (count == 1 && ...)
	else {
		refreshScreen(NAME[now] + ": Pass");
		WAIT_MS(750);
		sStatus = sUno->switchNow();
		onStatusChanged(sStatus);
	} // else
} // draw()

/**
 * Triggered on challenge chance. When a player played a [wild +4], the next
 * player can challenge its legality. Only when you have no cards that match
 * the previous played card's color, you can play a [wild +4].
 * Next player does not challenge: next player draw 4 cards;
 * Challenge success: current player draw 4 cards;
 * Challenge failure: next player draw 6 cards.
 *
 * @param challenged Whether the next player (challenger) challenged current
 *                   player(be challenged)'s [wild +4].
 */
static void onChallengeChance(bool challenged) {
	string message;
	Card* next2last;
	bool draw4IsLegal;
	int curr, challenger;
	vector<Card*> recent;
	Color colorBeforeDraw4;

	sStatus = STAT_IDLE; // block mouse click events when idle
	sChallenged = challenged;
	sChallengeAsk = false;
	if (challenged) {
		curr = sUno->getNow();
		challenger = sUno->getNext();
		message = NAME[challenger] + " challenged " + NAME[curr];
		refreshScreen(message);
		WAIT_MS(1500);
		recent = sUno->getRecent();
		next2last = recent.at(recent.size() - 2);
		colorBeforeDraw4 = next2last->getRealColor();
		draw4IsLegal = true;
		for (Card* card : sUno->getPlayer(curr)->getHandCards()) {
			if (card->getRealColor() == colorBeforeDraw4) {
				// Found a card that matches the next-to-last recent
				// played card's color, [wild +4] is illegally used
				draw4IsLegal = false;
				break;
			} // if (card->getRealColor() == colorBeforeDraw4)
		} // for (Card* card : sUno->getPlayer(curr)->getHandCards())

		if (draw4IsLegal) {
			// Challenge failure, challenger draws 6 cards
			message = "Challenge failure, " + NAME[challenger];
			if (challenger == Player::YOU) {
				message += " draw 6 cards";
			} // if (challenger == Player::YOU)
			else {
				message += " draws 6 cards";
			} // else

			refreshScreen(message);
			WAIT_MS(1500);
			sChallenged = false;
			sUno->switchNow();
			draw(6, /* force */ true);
		} // if (draw4IsLegal)
		else {
			// Challenge success, who played [wild +4] draws 4 cards
			message = "Challenge success, " + NAME[curr];
			if (curr == Player::YOU) {
				message += " draw 4 cards";
			} // if (curr == Player::YOU)
			else {
				message += " draws 4 cards";
			} // else

			refreshScreen(message);
			WAIT_MS(1500);
			sChallenged = false;
			draw(4, /* force */ true);
		} // else
	} // if (challenged)
	else {
		sUno->switchNow();
		draw(4, /* force */ true);
	} // else
} // onChallengeChance()

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
				if (x >= 490 && x <= 610) {
					// Difficulty: EASY
					sDifficulty = LV_EASY;
					sStatus = STAT_NEW_GAME;
					onStatusChanged(sStatus);
				} // if (x >= 490 && x <= 610)
				else if (x >= 670 && x <= 790) {
					// Difficulty: HARD
					sDifficulty = LV_HARD;
					sStatus = STAT_NEW_GAME;
					onStatusChanged(sStatus);
				} // else if (x >= 670 && x <= 790)
			} // if (y >= 270 && y <= 450)
			break; // case STAT_WELCOME

		case Player::YOU:
			if (sAuto) {
				// Do operations automatically by AI strategies
				break; // case Player::YOU
			} // if (sAuto)
			else if (sImmPlayAsk) {
				// Asking if you want to play the drawn card immediately
				if (y >= 520 && y <= 700) {
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

						// If clicked the drawn card, play it
						card = hand.at(index);
						if (card == sDrawnCard) {
							sImmPlayAsk = false;
							if (card->isWild() && size > 1) {
								sStatus = STAT_WILD_COLOR;
								onStatusChanged(sStatus);
							} // if (card->isWild() && size > 1)
							else {
								play(index);
							} // else
						} // if (card == sDrawnCard)
					} // if (x >= startX && x <= startX + width)
				} // if (y >= 520 && y <= 700)
				else if (x > 310 && x < 500) {
					if (y > 220 && y < 315) {
						// YES button, play the drawn card
						hand = sUno->getPlayer(sStatus)->getHandCards();
						size = (int)hand.size();
						for (index = 0; index < size; ++index) {
							card = hand.at(index);
							if (card == sDrawnCard) {
								sImmPlayAsk = false;
								if (card->isWild() && size > 1) {
									sStatus = STAT_WILD_COLOR;
									onStatusChanged(sStatus);
								} // if (card->isWild() && size > 1)
								else {
									play(index);
								} // else
								break;
							} // if (card == sDrawnCard)
						} // for (index = 0; index < size; ++index)
					} // if (y > 220 && y < 315)
					else if (y > 315 && y < 410) {
						// NO button, go to next player's round
						sStatus = STAT_IDLE; // block mouse events when idle
						sImmPlayAsk = false;
						refreshScreen(NAME[Player::YOU] + ": Pass");
						WAIT_MS(750);
						sStatus = sUno->switchNow();
						onStatusChanged(sStatus);
					} // else if (y > 315 && y < 410)
				} // else if (x > 310 && x < 500)
			} // else if (sImmPlayAsk)
			else if (sChallengeAsk) {
				// Asking if you want to challenge your previous player
				if (x > 310 && x < 500) {
					if (y > 220 && y < 315) {
						// YES button, challenge wild +4
						onChallengeChance(true);
					} // if (y > 220 && y < 315)
					else if (y > 315 && y < 410) {
						// NO button, do not challenge wild +4
						onChallengeChance(false);
					} // else if (y > 315 && y < 410)
				} // if (x > 310 && x < 500)
			} // else if (sChallengeAsk)
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
			else if (y >= 270 && y <= 450 && x >= 338 && x <= 458) {
				// Card deck area, draw a card
				draw();
			} // else if (y >= 270 && y <= 450 && x >= 338 && x <= 458)
			break; // case Player::YOU

		case STAT_WILD_COLOR:
			if (y > 220 && y < 315) {
				if (x > 310 && x < 405) {
					// Red sector
					sStatus = Player::YOU;
					play(index, RED);
				} // if (x > 310 && x < 405)
				else if (x > 405 && x < 500) {
					// Blue sector
					sStatus = Player::YOU;
					play(index, BLUE);
				} // else if (x > 405 && x < 500)
			} // if (y > 220 && y < 315)
			else if (y > 315 && y < 410) {
				if (x > 310 && x < 405) {
					// Yellow sector
					sStatus = Player::YOU;
					play(index, YELLOW);
				} // if (x > 310 && x < 405)
				else if (x > 405 && x < 500) {
					// Green sector
					sStatus = Player::YOU;
					play(index, GREEN);
				} // else if (x > 405 && x < 500)
			} // else if (y > 315 && y < 410)
			break; // case STAT_WILD_COLOR

		case STAT_GAME_OVER:
			if (y >= 270 && y <= 450 && x >= 338 && x <= 458) {
				// Card deck area, start a new game
				sStatus = STAT_NEW_GAME;
				onStatusChanged(sStatus);
			} // if (y >= 270 && y <= 450 && x >= 338 && x <= 458)
			break; // case STAT_GAME_OVER

		default:
			break; // default
		} // else switch (sStatus)
	} // if (event == EVENT_LBUTTONDOWN)
} // onMouse()

// E.O.F