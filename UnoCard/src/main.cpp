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

// Constants
static const int STAT_IDLE = 0x1111;
static const int STAT_WELCOME = 0x2222;
static const int STAT_NEW_GAME = 0x3333;
static const int STAT_GAME_OVER = 0x4444;
static const int STAT_WILD_COLOR = 0x5555;
static const cv::Scalar RGB_RED = CV_RGB(0xFF, 0x55, 0x55);
static const cv::Scalar RGB_BLUE = CV_RGB(0x55, 0x55, 0xFF);
static const cv::Scalar RGB_GREEN = CV_RGB(0x55, 0xAA, 0x55);
static const cv::Scalar RGB_WHITE = CV_RGB(0xCC, 0xCC, 0xCC);
static const cv::Scalar RGB_YELLOW = CV_RGB(0xFF, 0xAA, 0x11);
static const std::string NAME[] = { "YOU", "WEST", "NORTH", "EAST" };
static const enum cv::HersheyFonts FONT_SANS = cv::FONT_HERSHEY_DUPLEX;
static const std::string CL[] = { "", "RED", "BLUE", "GREEN", "YELLOW" };
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
static int sStatus;
static int sWinner;
static int sEasyWin;
static int sHardWin;
static int sEasyWin3;
static int sHardWin3;
static int sEasyTotal;
static int sHardTotal;
static int sEasyTotal3;
static int sHardTotal3;
static bool sAIRunning;
static cv::Mat sScreen;
static Card* sDrawnCard;
static bool sImmPlayAsk;
static bool sChallenged;
static bool sChallengeAsk;

// Functions
static void easyAI();
static void hardAI();
static void onStatusChanged(int status);
static void play(int index, Color color = NONE);
static void draw(int count = 1, bool force = false);
static void refreshScreen(const std::string& message);
static void onChallengeChance(bool challenged = true);
static void onMouse(int event, int x, int y, int flags, void* param);

/**
 * Defines the entry point for the console application.
 */
int main() {
	int len, dw[11], sum;
	std::ifstream reader;
	char header[9] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// Preparations
	sUno = Uno::getInstance();
	sEasyWin = sHardWin = sEasyTotal = sHardTotal = 0;
	sEasyWin3 = sHardWin3 = sEasyTotal3 = sHardTotal3 = 0;
	reader = std::ifstream("UnoStat.tmp", std::ios::in | std::ios::binary);
	if (!reader.fail()) {
		// Using statistics data in UnoStat.tmp file.
		reader.seekg(0, std::ios::end);
		len = int(reader.tellg());
		reader.seekg(0, std::ios::beg);
		switch (len) {
		case 8 + 5 * sizeof(int) :
			// old version
			reader.read(header, 8);
			reader.read((char*)dw, 5 * sizeof(int));
			sum = dw[0] + dw[1] + dw[2] + dw[3];
			if (strcmp(header, FILE_HEADER) == 0 && sum == dw[4]) {
				// File verification success
				sEasyWin = dw[0];
				sHardWin = dw[1];
				sEasyTotal = dw[2];
				sHardTotal = dw[3];
			} // if (strcmp(header, FILE_HEADER) == 0 && sum == dw[4])
			break; // case 8 + 5 * sizeof(int)

		case 8 + 11 * sizeof(int) :
			// new version
			reader.read(header, 8);
			reader.read((char*)dw, 11 * sizeof(int));
			sum = dw[0] + dw[1] + dw[2] + dw[3] + dw[4]
				+ dw[5] + dw[6] + dw[7] + dw[8] + dw[9];
			if (strcmp(header, FILE_HEADER) == 0 && sum == dw[10]) {
				// File verification success
				sEasyWin = dw[0];
				sHardWin = dw[1];
				sEasyWin3 = dw[2];
				sHardWin3 = dw[3];
				sEasyTotal = dw[4];
				sHardTotal = dw[5];
				sEasyTotal3 = dw[6];
				sHardTotal3 = dw[7];
				sUno->setPlayers(dw[8]);
				sUno->setDifficulty(dw[9]);
			} // if (strcmp(header, FILE_HEADER) == 0 && sum == dw[10])
			break; // case 8 + 11 * sizeof(int)

		default:
			break; // default
		} // switch (len)

		reader.close();
	} // if (!reader.fail())

	sUno = Uno::getInstance();
	sWinner = Player::YOU;
	sStatus = STAT_WELCOME;
	sScreen = sUno->getBackground().clone();
	cv::namedWindow("Uno");
	onStatusChanged(sStatus);
	cv::setMouseCallback("Uno", onMouse, nullptr);
	for (;;) {
		cv::waitKey(0); // prevent from blocking main thread
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
					cv::waitKey(750);
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
					cv::waitKey(750);
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
	cv::Rect rect;
	cv::Size axes;
	Card* next2last;
	cv::Point center;
	std::string message;
	std::vector<Card*> recent;

	switch (status) {
	case STAT_WELCOME:
		refreshScreen("WELCOME TO UNO CARD GAME, CLICK UNO TO START");
		break; // case STAT_WELCOME

	case STAT_NEW_GAME:
		// New game
		switch (sUno->getDifficulty() << 4 | sUno->getPlayers()) {
		case 0x03:
			// Difficulty = EASY(0), Players = 3
			++sEasyTotal3;
			break; // case 0x03

		case 0x04:
			// Difficulty = EASY(0), Players = 4
			++sEasyTotal;
			break; // case 0x04

		case 0x13:
			// Difficulty = HARD(1), Players = 3
			++sHardTotal3;
			break; // case 0x13

		case 0x14:
			// Difficulty = HARD(1), Players = 4
			++sHardTotal;
			break; // case 0x14

		default:
			break; // default
		} // switch (sUno->getDifficulty() << 4 | sUno->getPlayers())

		sUno->start();
		refreshScreen("GET READY");
		cv::waitKey(2000);
		switch (sUno->getRecent().at(0)->getContent()) {
		case DRAW2:
			// If starting with a [+2], let dealer draw 2 cards.
			draw(2, /* force */ true);
			break; // case DRAW2

		case SKIP:
			// If starting with a [skip], skip dealer's turn.
			refreshScreen(NAME[sUno->getNow()] + ": Skipped");
			cv::waitKey(1500);
			sStatus = sUno->switchNow();
			onStatusChanged(sStatus);
			break; // case SKIP

		case REV:
			// If starting with a [reverse], change the action
			// sequence to COUNTER CLOCKWISE.
			sUno->switchDirection();
			refreshScreen("Direction changed");
			cv::waitKey(1500);
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
				if (sUno->getDifficulty() == Uno::LV_EASY) {
					easyAI();
				} // if (sUno->getDifficulty() == Uno::LV_EASY)
				else {
					hardAI();
				} // else
			} // if (!sAIRunning)
		} // if (sAuto)
		else if (sImmPlayAsk) {
			refreshScreen("^ Play " + sDrawnCard->getName() + "?");
			rect = cv::Rect(338, 270, 121, 181);
			sUno->getBackground()(rect).copyTo(sScreen(rect));
			center = cv::Point(405, 315);
			axes = cv::Size(135, 135);

			// Draw YES button
			cv::ellipse(
				/* img        */ sScreen,
				/* center     */ center,
				/* axes       */ axes,
				/* angle      */ 0,
				/* startAngle */ 0,
				/* endAngle   */ -180,
				/* color      */ RGB_GREEN,
				/* thickness  */ -1,
				/* lineType   */ cv::LINE_AA
			); // cv::ellipse()
			cv::putText(
				/* img       */ sScreen,
				/* text      */ "YES",
				/* org       */ cv::Point(346, 295),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // cv::putText()

			// Draw NO button
			cv::ellipse(
				/* img        */ sScreen,
				/* center     */ center,
				/* axes       */ axes,
				/* angle      */ 0,
				/* startAngle */ 0,
				/* endAngle   */ 180,
				/* color      */ RGB_RED,
				/* thickness  */ -1,
				/* lineType   */ cv::LINE_AA
			); // cv::ellipse()
			cv::putText(
				/* img       */ sScreen,
				/* text      */ "NO",
				/* org       */ cv::Point(360, 378),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // cv::putText()

			// Show screen
			imshow("Uno", sScreen);
		} // else if (sImmPlayAsk)
		else if (sChallengeAsk) {
			recent = sUno->getRecent();
			next2last = recent.at(recent.size() - 2);
			message = "^ Do you think"
				+ NAME[sUno->getNow()] + " still has "
				+ CL[next2last->getRealColor()] + "?";
			refreshScreen(message);
			rect = cv::Rect(338, 270, 121, 181);
			sUno->getBackground()(rect).copyTo(sScreen(rect));
			center = cv::Point(405, 315);
			axes = cv::Size(135, 135);

			// Draw YES button
			cv::ellipse(
				/* img        */ sScreen,
				/* center     */ center,
				/* axes       */ axes,
				/* angle      */ 0,
				/* startAngle */ 0,
				/* endAngle   */ -180,
				/* color      */ RGB_GREEN,
				/* thickness  */ -1,
				/* lineType   */ cv::LINE_AA
			); // cv::ellipse()
			cv::putText(
				/* img       */ sScreen,
				/* text      */ "YES",
				/* org       */ cv::Point(346, 295),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // cv::putText()

			// Draw NO button
			cv::ellipse(
				/* img        */ sScreen,
				/* center     */ center,
				/* axes       */ axes,
				/* angle      */ 0,
				/* startAngle */ 0,
				/* endAngle   */ 180,
				/* color      */ RGB_RED,
				/* thickness  */ -1,
				/* lineType   */ cv::LINE_AA
			); // cv::ellipse()
			cv::putText(
				/* img       */ sScreen,
				/* text      */ "NO",
				/* org       */ cv::Point(360, 378),
				/* fontFace  */ FONT_SANS,
				/* fontScale */ 2.0,
				/* color     */ RGB_WHITE,
				/* thickness */ 2
			); // cv::putText()

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
		rect = cv::Rect(338, 270, 121, 181);
		sUno->getBackground()(rect).copyTo(sScreen(rect));
		center = cv::Point(405, 315);
		axes = cv::Size(135, 135);

		// Draw blue sector
		cv::ellipse(
			/* img        */ sScreen,
			/* center     */ center,
			/* axes       */ axes,
			/* angle      */ 0,
			/* startAngle */ 0,
			/* endAngle   */ -90,
			/* color      */ RGB_BLUE,
			/* thickness  */ -1,
			/* lineType   */ cv::LINE_AA
		); // cv::ellipse()

		// Draw green sector
		cv::ellipse(
			/* img        */ sScreen,
			/* center     */ center,
			/* axes       */ axes,
			/* angle      */ 0,
			/* startAngle */ 0,
			/* endAngle   */ 90,
			/* color      */ RGB_GREEN,
			/* thickness  */ -1,
			/* lineType   */ cv::LINE_AA
		); // cv::ellipse()

		// Draw red sector
		cv::ellipse(
			/* img        */ sScreen,
			/* center     */ center,
			/* axes       */ axes,
			/* angle      */ 180,
			/* startAngle */ 0,
			/* endAngle   */ 90,
			/* color      */ RGB_RED,
			/* thickness  */ -1,
			/* lineType   */ cv::LINE_AA
		); // cv::ellipse()

		// Draw yellow sector
		cv::ellipse(
			/* img        */ sScreen,
			/* center     */ center,
			/* axes       */ axes,
			/* angle      */ 180,
			/* startAngle */ 0,
			/* endAngle   */ -90,
			/* color      */ RGB_YELLOW,
			/* thickness  */ -1,
			/* lineType   */ cv::LINE_AA
		); // cv::ellipse()

		// Show screen
		imshow("Uno", sScreen);
		break; // case STAT_WILD_COLOR

	case Player::COM1:
	case Player::COM2:
	case Player::COM3:
		// AI players' turn
		if (!sAIRunning) {
			if (sUno->getDifficulty() == Uno::LV_EASY) {
				easyAI();
			} // if (sUno->getDifficulty() == Uno::LV_EASY)
			else {
				hardAI();
			} // else
		} // if (!sAIRunning)
		break; // case Player::COM1, Player::COM2, Player::COM3

	case STAT_GAME_OVER:
		// Game over
		if (sWinner == Player::YOU) {
			switch (sUno->getDifficulty() << 4 | sUno->getPlayers()) {
			case 0x03:
				// Difficulty = EASY(0), Players = 3
				++sEasyWin3;
				break; // case 0x03

			case 0x04:
				// Difficulty = EASY(0), Players = 4
				++sEasyWin;
				break; // case 0x04

			case 0x13:
				// Difficulty = HARD(1), Players = 3
				++sHardWin3;
				break; // case 0x13

			case 0x14:
				// Difficulty = HARD(1), Players = 4
				++sHardWin;
				break; // case 0x14

			default:
				break; // default
			} // switch (sUno->getDifficulty() << 4 | sUno->getPlayers())
		} // if (sWinner == Player::YOU)

		refreshScreen("Click the card deck to restart");
		if (sAuto) {
			cv::waitKey(5000);
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
static void refreshScreen(const std::string& message) {
	cv::Rect roi;
	cv::Mat image;
	cv::Point point;
	bool beChallenged;
	std::stringstream buff;
	std::vector<Card*> hand;
	int i, remain, used, rate;
	int status, size, width, height;

	// Lock the value of global variable [sStatus]
	status = sStatus;

	// Clear
	sUno->getBackground().copyTo(sScreen);

	// Message area
	width = cv::getTextSize(message, FONT_SANS, 1.0, 1, nullptr).width;
	point = cv::Point(640 - width / 2, 480);
	cv::putText(sScreen, message, point, FONT_SANS, 1.0, RGB_WHITE);

	// Right-top corner: <QUIT> button
	point.x = 1140;
	point.y = 42;
	cv::putText(sScreen, "<QUIT>", point, FONT_SANS, 1.0, RGB_WHITE);

	// Right-bottom corner: <AUTO> button
	point.x = 1130;
	point.y = 700;
	cv::putText(
		/* img       */ sScreen,
		/* text      */ "<AUTO>",
		/* org       */ point,
		/* fontFace  */ FONT_SANS,
		/* fontScale */ 1.0,
		/* color     */ sAuto ? RGB_YELLOW : RGB_WHITE
	); // cv::putText()

	// For welcome screen, only show difficulty buttons and winning rates
	if (status == STAT_WELCOME) {
		// [Level] option: easy / hard
		point.x = 340;
		point.y = 160;
		cv::putText(sScreen, "LEVEL", point, FONT_SANS, 1.0, RGB_WHITE);
		image = sUno->getLevelImage(
			/* level   */ Uno::LV_EASY,
			/* hiLight */ sUno->getDifficulty() == Uno::LV_EASY
		); // image = sUno->getLevelImage()
		roi = cv::Rect(490, 60, 121, 181);
		image.copyTo(sScreen(roi), image);
		image = sUno->getLevelImage(
			/* level   */ Uno::LV_HARD,
			/* hiLight */ sUno->getDifficulty() == Uno::LV_HARD
		); // image = sUno->getLevelImage()
		roi.x = 670;
		image.copyTo(sScreen(roi), image);

		// [Players] option: 3 / 4
		point.x = 340;
		point.y = 370;
		cv::putText(sScreen, "PLAYERS", point, FONT_SANS, 1.0, RGB_WHITE);
		image = sUno->getPlayers() == 3 ?
			sUno->findCard(BLUE, NUM3)->getImage() :
			sUno->findCard(BLUE, NUM3)->getDarkImg();
		roi.x = 490;
		roi.y = 270;
		image.copyTo(sScreen(roi), image);
		image = sUno->getPlayers() == 4 ?
			sUno->findCard(BLUE, NUM4)->getImage() :
			sUno->findCard(BLUE, NUM4)->getDarkImg();
		roi.x = 670;
		image.copyTo(sScreen(roi), image);

		// Win rate
		switch (sUno->getDifficulty() << 4 | sUno->getPlayers()) {
		case 0x03:
			// Difficulty = EASY(0), Players = 3
			rate = sEasyTotal3 == 0 ? 0 : 100 * sEasyWin3 / sEasyTotal3;
			break; // case 0x03

		case 0x04:
			// Difficulty = EASY(0), Players = 4
			rate = sEasyTotal == 0 ? 0 : 100 * sEasyWin / sEasyTotal;
			break; // case 0x04

		case 0x13:
			// Difficulty = HARD(1), Players = 3
			rate = sHardTotal3 == 0 ? 0 : 100 * sHardWin3 / sHardTotal3;
			break; // case 0x13

		case 0x14:
			// Difficulty = HARD(1), Players = 4
			rate = sHardTotal == 0 ? 0 : 100 * sHardWin / sHardTotal;
			break; // case 0x14

		default:
			rate = 0;
			break; // default
		} // switch (sUno->getDifficulty() << 4 | sUno->getPlayers())

		buff << "win rate: " << rate << "%";
		width = cv::getTextSize(buff.str(), FONT_SANS, 1.0, 1, nullptr).width;
		point.x = 930 - width / 2;
		cv::putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);

		// [UNO] button: start a new game
		image = sUno->getBackImage();
		roi.x = 580;
		roi.y = 520;
		image.copyTo(sScreen(roi), image);
	} // if (status == STAT_WELCOME)
	else {
		// Center: card deck & recent played card
		image = sUno->getBackImage();
		roi = cv::Rect(338, 270, 121, 181);
		image.copyTo(sScreen(roi), image);
		hand = sUno->getRecent();
		size = int(hand.size());
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
		point = cv::Point(20, 42);
		remain = sUno->getDeckCount();
		used = sUno->getUsedCount();
		buff << "Remain/Used: " << remain << "/" << used;
		cv::putText(sScreen, buff.str(), point, FONT_SANS, 1.0, RGB_WHITE);

		// Left-center: Hand cards of Player West (COM1)
		hand = sUno->getPlayer(Player::COM1)->getHandCards();
		size = int(hand.size());
		if (size == 0) {
			// Played all hand cards, it's winner
			point.x = 51;
			point.y = 461;
			cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
		} // if (size == 0)
		else {
			height = 40 * size + 140;
			roi.x = 20;
			roi.y = 360 - height / 2;
			beChallenged = sChallenged && sUno->getNow() == Player::COM1;
			if (beChallenged || status == STAT_GAME_OVER) {
				// Show remained cards to everyone
				// when being challenged or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (beChallenged || status == STAT_GAME_OVER)
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
				cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else

		// Top-center: Hand cards of Player North (COM2)
		hand = sUno->getPlayer(Player::COM2)->getHandCards();
		size = int(hand.size());
		if (size == 0) {
			// Played all hand cards, it's winner
			if (sUno->getPlayers() == 4) {
				point.x = 611;
				point.y = 121;
				cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (sUno->getPlayers() == 4)
		} // if (size == 0)
		else {
			width = 45 * size + 75;
			roi.x = 640 - width / 2;
			roi.y = 20;
			beChallenged = sChallenged && sUno->getNow() == Player::COM2;
			if (beChallenged || status == STAT_GAME_OVER) {
				// Show remained cards to everyone
				// when being challenged or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.x += 45;
				} // for (Card* card : hand)
			} // if (beChallenged || status == STAT_GAME_OVER)
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
				cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else

		// Right-center: Hand cards of Player East (COM3)
		hand = sUno->getPlayer(Player::COM3)->getHandCards();
		size = int(hand.size());
		if (size == 0) {
			// Played all hand cards, it's winner
			point.x = 1170;
			point.y = 461;
			cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
		} // if (size == 0)
		else {
			height = 40 * size + 140;
			roi.x = 1140;
			roi.y = 360 - height / 2;
			beChallenged = sChallenged && sUno->getNow() == Player::COM3;
			if (beChallenged || status == STAT_GAME_OVER) {
				// Show remained cards to everyone
				// when being challenged or game over
				for (Card* card : hand) {
					image = card->getImage();
					image.copyTo(sScreen(roi), image);
					roi.y += 40;
				} // for (Card* card : hand)
			} // if (beChallenged || status == STAT_GAME_OVER)
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
				cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
			} // if (size == 1)
		} // else

		// Bottom: Your hand cards
		hand = sUno->getPlayer(Player::YOU)->getHandCards();
		size = int(hand.size());
		if (size == 0) {
			// Played all hand cards, it's winner
			point.x = 611;
			point.y = 621;
			cv::putText(sScreen, "WIN", point, FONT_SANS, 1.0, RGB_YELLOW);
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
						image = card == sDrawnCard ?
							card->getImage() :
							card->getDarkImg();
					} // if (sImmPlayAsk)
					else if (sChallengeAsk || sChallenged) {
						image = card->getDarkImg();
					} // else if (sChallengeAsk || sChallenged)
					else {
						image = sUno->isLegalToPlay(card) ?
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
				cv::putText(sScreen, "UNO", point, FONT_SANS, 1.0, RGB_YELLOW);
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
	Card* card;
	cv::Rect roi;
	cv::Mat image;
	std::string message;
	int x, y, now, size, width, height, next;

	sStatus = STAT_IDLE; // block mouse click events when idle
	now = sUno->getNow();
	size = int(sUno->getPlayer(now)->getHandCards().size());
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
		roi = cv::Rect(x, y, 121, 181);
		image.copyTo(sScreen(roi), image);
		imshow("Uno", sScreen);
		cv::waitKey(300);
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
				cv::waitKey(1500);
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
				cv::waitKey(1500);
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
				cv::waitKey(1500);
				sStatus = sUno->switchNow();
				onStatusChanged(sStatus);
				break; // case REV

			case WILD:
				message += ": Change the following legal color";
				refreshScreen(message);
				cv::waitKey(1500);
				sStatus = sUno->switchNow();
				onStatusChanged(sStatus);
				break; // case WILD

			case WILD_DRAW4:
				next = sUno->getNext();
				message += ": Let " + NAME[next] + " draw 4 cards";
				refreshScreen(message);
				cv::waitKey(1500);
				sStatus = next;
				sChallengeAsk = true;
				onStatusChanged(sStatus);
				break; // case WILD_DRAW4

			default:
				message += ": " + card->getName();
				refreshScreen(message);
				cv::waitKey(1500);
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
	int i, now;
	cv::Rect roi;
	cv::Mat image;
	std::stringstream buff;

	sStatus = STAT_IDLE; // block mouse click events when idle
	now = sUno->getNow();
	for (i = 0; i < count; ++i) {
		buff.str("");
		sDrawnCard = sUno->draw(now, force);
		if (sDrawnCard != nullptr) {
			switch (now) {
			case Player::COM1:
				image = sUno->getBackImage();
				roi = cv::Rect(160, 270, 121, 181);
				if (count == 1) {
					buff << NAME[now] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[now] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM1

			case Player::COM2:
				image = sUno->getBackImage();
				roi = cv::Rect(580, 70, 121, 181);
				if (count == 1) {
					buff << NAME[now] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[now] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM2

			case Player::COM3:
				image = sUno->getBackImage();
				roi = cv::Rect(1000, 270, 121, 181);
				if (count == 1) {
					buff << NAME[now] << ": Draw a card";
				} // if (count == 1)
				else {
					buff << NAME[now] << ": Draw " << count << " cards";
				} // else
				break; // case Player::COM3

			default:
				image = sDrawnCard->getImage();
				roi = cv::Rect(580, 470, 121, 181);
				buff << NAME[now] << ": Draw " + sDrawnCard->getName();
				break; // default
			} // switch (now)

			// Animation
			image.copyTo(sScreen(roi), image);
			imshow("Uno", sScreen);
			cv::waitKey(300);
			refreshScreen(buff.str());
			cv::waitKey(300);
		} // if (sDrawnCard != nullptr)
		else {
			buff << NAME[now];
			buff << " cannot hold more than ";
			buff << Uno::MAX_HOLD_CARDS << " cards";
			refreshScreen(buff.str());
			break;
		} // else
	} // for (i = 0; i < count; ++i)

	cv::waitKey(750);
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
		cv::waitKey(750);
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
	Card* next2last;
	bool draw4IsLegal;
	std::string message;
	int curr, challenger;
	Color colorBeforeDraw4;
	std::vector<Card*> recent;

	sStatus = STAT_IDLE; // block mouse click events when idle
	sChallenged = challenged;
	sChallengeAsk = false;
	if (challenged) {
		curr = sUno->getNow();
		challenger = sUno->getNext();
		recent = sUno->getRecent();
		next2last = recent.at(recent.size() - 2);
		colorBeforeDraw4 = next2last->getRealColor();
		if (curr == Player::YOU) {
			message = NAME[challenger]
				+ " doubted that you still have "
				+ CL[colorBeforeDraw4];
		} // if (curr == Player::YOU)
		else {
			message = NAME[challenger]
				+ " doubted that " + NAME[curr]
				+ " still has " + CL[colorBeforeDraw4];
		} // else

		refreshScreen(message);
		cv::waitKey(1500);
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
			if (challenger == Player::YOU) {
				message = "Challenge failure, you draw 6 cards";
			} // if (challenger == Player::YOU)
			else {
				message = "Challenge failure, "
					+ NAME[challenger] + " draws 6 cards";
			} // else

			refreshScreen(message);
			cv::waitKey(1500);
			sChallenged = false;
			sUno->switchNow();
			draw(6, /* force */ true);
		} // if (draw4IsLegal)
		else {
			// Challenge success, who played [wild +4] draws 4 cards
			if (curr == Player::YOU) {
				message = "Challenge success, you draw 4 cards";
			} // if (curr == Player::YOU)
			else {
				message = "Challenge success, "
					+ NAME[curr] + "draws 4 cards";
			} // else

			refreshScreen(message);
			cv::waitKey(1500);
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
	static int dw[11];
	static Card* card;
	static std::ofstream writer;
	static std::vector<Card*> hand;
	static int i, index, size, width, startX;

	if (event == cv::EVENT_LBUTTONDOWN) {
		// Only response to left-click events, and ignore the others
		if (y >= 21 && y <= 42 && x >= 1140 && x <= 1260) {
			// <QUIT> button
			// Store statistics data to UnoStat.tmp file
			writer = std::ofstream("UnoStat.tmp", std::ios::out | std::ios::binary);
			if (!writer.fail()) {
				// Store statistics data to file
				dw[0] = sEasyWin;
				dw[1] = sHardWin;
				dw[2] = sEasyWin3;
				dw[3] = sHardWin3;
				dw[4] = sEasyTotal;
				dw[5] = sHardTotal;
				dw[6] = sEasyTotal3;
				dw[7] = sHardTotal3;
				dw[8] = sUno->getPlayers();
				dw[9] = sUno->getDifficulty();
				for (dw[0] = 0, i = 0; i < 10; ++i) {
					dw[10] += dw[i];
				} // for (dw[0] = 0, i = 0; i < 10; ++i)

				writer.write(FILE_HEADER, 8);
				writer.write((char*)dw, 11 * sizeof(int));
				writer.close();
			} // if (!writer.fail())

			cv::destroyAllWindows();
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
				cv::putText(
					/* img       */ sScreen,
					/* text      */ "<AUTO>",
					/* org       */ cv::Point(1130, 700),
					/* fontFace  */ FONT_SANS,
					/* fontScale */ 1.0,
					/* color     */ sAuto ? RGB_YELLOW : RGB_WHITE
				); // cv::putText()

				imshow("Uno", sScreen);
				break; // default
			} // switch (sStatus)
		} // else if (y >= 679 && y <= 700 && x >= 1130 && x <= 1260)
		else switch (sStatus) {
		case STAT_WELCOME:
			if (y >= 60 && y <= 240) {
				if (x >= 490 && x <= 610) {
					// Difficulty: EASY
					sUno->setDifficulty(Uno::LV_EASY);
					onStatusChanged(sStatus);
				} // if (x >= 490 && x <= 610)
				else if (x >= 670 && x <= 790) {
					// Difficulty: HARD
					sUno->setDifficulty(Uno::LV_HARD);
					onStatusChanged(sStatus);
				} // else if (x >= 670 && x <= 790)
			} // if (y >= 60 && y <= 240)
			else if (y >= 270 && y <= 450) {
				if (x >= 490 && x <= 610) {
					// 3-player mode
					sUno->setPlayers(3);
					onStatusChanged(sStatus);
				} // if (x >= 490 && x <= 610)
				else if (x >= 670 && x <= 790) {
					// 4-player mode
					sUno->setPlayers(4);
					onStatusChanged(sStatus);
				} // else if (x >= 670 && x <= 790)
			} // else if (y >= 270 && y <= 450)
			else if (y >= 520 && y <= 700) {
				if (x >= 580 && x <= 700) {
					// UNO button, start a new game
					sStatus = STAT_NEW_GAME;
					onStatusChanged(sStatus);
				} // if (x >= 580 && x <= 700)
			} // else if (y >= 520 && y <= 700)
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
					size = int(hand.size());
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
						size = int(hand.size());
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
						cv::waitKey(750);
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
				size = int(hand.size());
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
	} // if (event == cv::EVENT_LBUTTONDOWN)
} // onMouse()

// E.O.F