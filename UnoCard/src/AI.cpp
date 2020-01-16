////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Visual Studio 2015, Windows 10 x64
//
////////////////////////////////////////////////////////////////////////////////

#include <AI.h>
#include <Uno.h>
#include <vector>

static Uno* sUno = Uno::getInstance();

/**
 * AI strategies of determining if it's necessary to challenge previous
 * player's [wild +4] card's legality.
 *
 * @param challenger Who challenges. Must be one of the following values:
 *                   Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @return Whether the challenger needs to make a challenge.
 */
bool needToChallenge(int challenger) {
	bool challenge;
	Card* next2last;
	Color draw4Color;
	Color colorBeforeDraw4;
	std::vector<Card*> hand, recent;

	hand = sUno->getPlayer(challenger)->getHandCards();
	if (hand.size() == 1) {
		// Challenge when defending my UNO dash
		challenge = true;
	} // if (hand.size() == 1)
	else if (hand.size() >= Uno::MAX_HOLD_CARDS - 4) {
		// Challenge when I have 10 or more cards already, thus even if
		// challenge failed, I draw at most 4 cards.
		challenge = true;
	} // else if (hand.size() >= Uno::MAX_HOLD_CARDS - 4)
	else {
		// Challenge when legal color has not been changed
		recent = sUno->getRecent();
		next2last = recent.at(recent.size() - 2);
		colorBeforeDraw4 = next2last->getRealColor();
		draw4Color = recent.back()->getRealColor();
		challenge = draw4Color == colorBeforeDraw4;
	} // else

	return challenge;
} // challengeTo()

/**
 * AI Strategies (Difficulty: EASY).
 *
 * @param whom      Analyze whose hand cards. Must be one of the following:
 *                  Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @param drawnCard When the specified player drew a card in its turn just
 *                  now, pass the drawn card. If not, pass nullptr. If drew
 *                  a card from deck, then you can play only the drawn card,
 *                  but not the other cards in your hand, immediately.
 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
 *                  in order to let we pass the return value by assigning
 *                  outColor[0]. When the best card to play becomes a wild
 *                  card, outColor[0] will become the following legal color
 *                  to change. When the best card to play becomes an action
 *                  or a number card, outColor[0] will become the player's
 *                  best color.
 * @return Index of the best card to play, in the specified player's hand.
 *         Or a negative number that means no appropriate card to play.
 * @deprecated Use easyAI_bestCardIndex4NowPlayer(Card*, Color[]) instead.
 */
[[deprecated]]
int easyAI_bestCardIndexFor(int whom, Card* drawnCard, Color outColor[]) {
	if (whom == sUno->getNow()) {
		return easyAI_bestCardIndex4NowPlayer(drawnCard, outColor);
	} // if (whom == sUno->getNow())
	else {
		throw "DO NOT CALL DEPRECATED API!";
	} // else
} // easyAI_bestCardIndexFor()

/**
 * AI Strategies (Difficulty: EASY). Analyze current player's hand cards,
 * and calculate which is the best card to play out.
 *
 * @param drawnCard When current player drew a card just now, pass the drawn
 *                  card. If not, pass nullptr. If drew a card from deck,
 *                  then you can play only the drawn card, but not the other
 *                  cards in your hand, immediately.
 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
 *                  in order to let we pass the return value by assigning
 *                  outColor[0]. When the best card to play becomes a wild
 *                  card, outColor[0] will become the following legal color
 *                  to change. When the best card to play becomes an action
 *                  or a number card, outColor[0] will become the player's
 *                  best color.
 * @return Index of the best card to play, in current player's hand.
 *         Or a negative number that means no appropriate card to play.
 */
int easyAI_bestCardIndex4NowPlayer(Card* drawnCard, Color outColor[]) {
	int i;
	bool legal;
	Card* card;
	Player* curr;
	Player* next;
	Player* prev;
	Color bestColor;
	std::vector<Card*> hand;
	int yourSize, nextSize, prevSize;
	bool hasZero, hasWild, hasWildDraw4;
	bool hasNum, hasRev, hasSkip, hasDraw2;
	int idxBest, idxRev, idxSkip, idxDraw2;
	int idxZero, idxNum, idxWild, idxWildDraw4;

	if (outColor == nullptr) {
		throw "outColor[] cannot be nullptr";
	} // if (outColor == nullptr)

	curr = sUno->getPlayer(sUno->getNow());
	hand = curr->getHandCards();
	yourSize = int(hand.size());
	if (yourSize == 1) {
		// Only one card remained. Play it when it's legal.
		card = hand.at(0);
		outColor[0] = card->getRealColor();
		return sUno->isLegalToPlay(card) ? 0 : -1;
	} // if (yourSize == 1)

	next = sUno->getPlayer(sUno->getNext());
	nextSize = int(next->getHandCards().size());
	prev = sUno->getPlayer(sUno->getPrev());
	prevSize = int(prev->getHandCards().size());
	hasRev = hasSkip = hasDraw2 = false;
	idxBest = idxRev = idxSkip = idxDraw2 = -1;
	idxZero = idxNum = idxWild = idxWildDraw4 = -1;
	hasZero = hasNum = hasWild = hasWildDraw4 = false;
	bestColor = curr->calcBestColor();
	for (i = 0; i < yourSize; ++i) {
		// Index of any kind
		card = hand.at(i);
		if (drawnCard == nullptr) {
			legal = sUno->isLegalToPlay(card);
		} // if (drawnCard == nullptr)
		else {
			legal = card == drawnCard;
		} // else

		if (legal) {
			switch (card->getContent()) {
			case NUM0:
				if (!hasZero || card->getRealColor() == bestColor) {
					idxZero = i;
					hasZero = true;
				} // if (!hasZero || card->getRealColor() == bestColor)
				break; // case NUM0

			case DRAW2:
				if (!hasDraw2 || card->getRealColor() == bestColor) {
					idxDraw2 = i;
					hasDraw2 = true;
				} // if (!hasDraw2 || card->getRealColor() == bestColor)
				break; // case DRAW2

			case SKIP:
				if (!hasSkip || card->getRealColor() == bestColor) {
					idxSkip = i;
					hasSkip = true;
				} // if (!hasSkip || card->getRealColor() == bestColor)
				break; // case SKIP

			case REV:
				if (!hasRev || card->getRealColor() == bestColor) {
					idxRev = i;
					hasRev = true;
				} // if (!hasRev || card->getRealColor() == bestColor)
				break; // case REV

			case WILD:
				idxWild = i;
				hasWild = true;
				break; // case WILD

			case WILD_DRAW4:
				idxWildDraw4 = i;
				hasWildDraw4 = true;
				break; // case WILD_DRAW4

			default: // non-zero number cards
				if (!hasNum || card->getRealColor() == bestColor) {
					idxNum = i;
					hasNum = true;
				} // if (!hasNum || card->getRealColor() == bestColor)
				break; // default
			} // switch (card->getContent())
		} // if (legal)
	} // for (i = 0; i < yourSize; ++i)

	// Decision tree
	if (hasDraw2) {
		idxBest = idxDraw2;
	} // if (hasDraw2)
	else if (hasSkip) {
		idxBest = idxSkip;
	} // else if (hasSkip)
	else if (hasRev && prevSize > nextSize) {
		idxBest = idxRev;
	} // else if (hasRev && prevSize > nextSize)
	else if (hasZero) {
		idxBest = idxZero;
	} // else if (hasZero)
	else if (hasNum) {
		idxBest = idxNum;
	} // else if (hasNum)
	else if (hasWildDraw4) {
		idxBest = idxWildDraw4;
	} // else if (hasWildDraw4)
	else if (hasWild) {
		idxBest = idxWild;
	} // else if (hasWild)

	outColor[0] = bestColor;
	return idxBest;
} // easyAI_bestCardIndex4NowPlayer()

/**
 * AI Strategies (Difficulty: HARD).
 *
 * @param whom      Analyze whose hand cards. Must be one of the following:
 *                  Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @param drawnCard When the specified player drew a card in its turn just
 *                  now, pass the drawn card. If not, pass nullptr. If drew
 *                  a card from deck, then you can play only the drawn card,
 *                  but not the other cards in your hand, immediately.
 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
 *                  in order to let we pass the return value by assigning
 *                  outColor[0]. When the best card to play becomes a wild
 *                  card, outColor[0] will become the following legal color
 *                  to change. When the best card to play becomes an action
 *                  or a number card, outColor[0] will become the player's
 *                  best color.
 * @return Index of the best card to play, in the specified player's hand.
 *         Or a negative number that means no appropriate card to play.
 * @deprecated Use hardAI_bestCardIndex4NowPlayer(Card*, Color[]) instead.
 */
[[deprecated]]
int hardAI_bestCardIndexFor(int whom, Card* drawnCard, Color outColor[]) {
	if (whom == sUno->getNow()) {
		return hardAI_bestCardIndex4NowPlayer(drawnCard, outColor);
	} // if (whom == sUno->getNow())
	else {
		throw "DO NOT CALL DEPRECATED API!";
	} // else
} // hardAI_bestCardIndexFor()

/**
 * AI Strategies (Difficulty: HARD). Analyze current player's hand cards,
 * and calculate which is the best card to play.
 *
 * @param drawnCard When current player drew a card just now, pass the drawn
 *                  card. If not, pass nullptr. If drew a card from deck,
 *                  then you can play only the drawn card, but not the other
 *                  cards in your hand, immediately.
 * @param outColor  This is a out parameter. Pass a Color array (length>=1)
 *                  in order to let we pass the return value by assigning
 *                  outColor[0]. When the best card to play becomes a wild
 *                  card, outColor[0] will become the following legal color
 *                  to change. When the best card to play becomes an action
 *                  or a number card, outColor[0] will become the player's
 *                  best color.
 * @return Index of the best card to play, in current player's hand.
 *         Or a negative number that means no appropriate card to play.
 */
int hardAI_bestCardIndex4NowPlayer(Card* drawnCard, Color outColor[]) {
	int i;
	bool legal;
	Card* card;
	Card* last;
	Player* curr;
	Player* next;
	Player* oppo;
	Player* prev;
	std::vector<Card*> hand;
	Color bestColor, lastColor;
	bool hasRev, hasSkip, hasDraw2;
	int idxNumIn[5], idxWild, idxWildDraw4;
	int idxBest, idxRev, idxSkip, idxDraw2;
	bool hasNumIn[5], hasWild, hasWildDraw4;
	int yourSize, nextSize, oppoSize, prevSize;
	Color nextSafeColor, oppoSafeColor, prevSafeColor;
	Color nextDangerColor, oppoDangerColor, prevDangerColor;

	if (outColor == nullptr) {
		throw "outColor[] cannot be nullptr";
	} // if (outColor == nullptr)

	curr = sUno->getPlayer(sUno->getNow());
	hand = curr->getHandCards();
	yourSize = int(hand.size());
	if (yourSize == 1) {
		// Only one card remained. Play it when it's legal.
		card = hand.at(0);
		outColor[0] = card->getRealColor();
		return sUno->isLegalToPlay(card) ? 0 : -1;
	} // if (yourSize == 1)

	next = sUno->getPlayer(sUno->getNext());
	nextSize = int(next->getHandCards().size());
	oppo = sUno->getPlayer(sUno->getOppo());
	oppoSize = int(oppo->getHandCards().size());
	prev = sUno->getPlayer(sUno->getPrev());
	prevSize = int(prev->getHandCards().size());
	hasRev = hasSkip = hasDraw2 = hasWild = hasWildDraw4 = false;
	idxBest = idxRev = idxSkip = idxDraw2 = idxWild = idxWildDraw4 = -1;
	for (i = 0; i < 5; ++i) {
		hasNumIn[i] = false;
		idxNumIn[i] = -1;
	} // for (i = 0; i < 5; ++i)

	bestColor = curr->calcBestColor();
	last = sUno->getRecent().back();
	lastColor = last->getRealColor();
	for (i = 0; i < yourSize; ++i) {
		// Index of any kind
		card = hand.at(i);
		if (drawnCard == nullptr) {
			legal = sUno->isLegalToPlay(card);
		} // if (drawnCard == nullptr)
		else {
			legal = card == drawnCard;
		} // else

		if (legal) {
			switch (card->getContent()) {
			case DRAW2:
				if (!hasDraw2 || card->getRealColor() == bestColor) {
					idxDraw2 = i;
					hasDraw2 = true;
				} // if (!hasDraw2 || card->getRealColor() == bestColor)
				break; // case DRAW2

			case SKIP:
				if (!hasSkip || card->getRealColor() == bestColor) {
					idxSkip = i;
					hasSkip = true;
				} // if (!hasSkip || card->getRealColor() == bestColor)
				break; // case SKIP

			case REV:
				if (!hasRev || card->getRealColor() == bestColor) {
					idxRev = i;
					hasRev = true;
				} // if (!hasRev || card->getRealColor() == bestColor)
				break; // case REV

			case WILD:
				idxWild = i;
				hasWild = true;
				break; // case WILD

			case WILD_DRAW4:
				idxWildDraw4 = i;
				hasWildDraw4 = true;
				break; // case WILD_DRAW4

			default: // number cards
				if (!hasNumIn[card->getRealColor()]) {
					idxNumIn[card->getRealColor()] = i;
					hasNumIn[card->getRealColor()] = true;
				} // if (!hasNumIn[card->getRealColor()])
				break; // default
			} // switch (card->getContent())
		} // if (legal)
	} // for (i = 0; i < yourSize; ++i)

	// Decision tree
	nextSafeColor = next->getSafeColor();
	oppoSafeColor = oppo->getSafeColor();
	prevSafeColor = prev->getSafeColor();
	nextDangerColor = next->getDangerousColor();
	oppoDangerColor = oppo->getDangerousColor();
	prevDangerColor = prev->getDangerousColor();
	if (nextSize <= 2) {
		// Strategies when your next player remains less than 3 cards.
		// Limit your next player's action as well as you can.
		if (hasDraw2) {
			// Play a [+2] to make your next player draw two cards!
			idxBest = idxDraw2;
		} // if (hasDraw2)
		else if (lastColor == nextDangerColor) {
			// Your next player only remains less than 3 cards, and what's
			// worse is that the legal color is still its dangerous color.
			// You have to change the following legal color.
			while (bestColor == nextDangerColor
				|| oppoSize == 1 && bestColor == oppoDangerColor
				|| prevSize == 1 && bestColor == prevDangerColor) {
				// In dangerous cases, you could not just play wild cards
				// and select your best color. Do not choose any of your
				// opponents' dangerous color!
				bestColor = Color(rand() % 4 + 1);
			} // while (bestColor == nextDangerColor || ...)

			if (hasNumIn[bestColor]) {
				idxBest = idxNumIn[bestColor];
			} // if (hasNumIn[bestColor])
			else if (hasNumIn[RED] && nextDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && nextDangerColor != RED)
			else if (hasNumIn[BLUE] && nextDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && nextDangerColor != BLUE)
			else if (hasNumIn[GREEN] && nextDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && nextDangerColor != GREEN)
			else if (hasNumIn[YELLOW] && nextDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && nextDangerColor != YELLOW)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // else if (hasSkip)
			else if (hasWildDraw4) {
				// Now start to use wild cards.
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
			else if (hasWild) {
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasRev && prevSize >= nextSize) {
				// Finally play a [reverse] to get help from other players.
				idxBest = idxRev;
			} // else if (hasRev && prevSize >= nextSize)
			else if (hasNumIn[RED]) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED])
			else if (hasNumIn[BLUE]) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE])
			else if (hasNumIn[GREEN]) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN])
			else if (hasNumIn[YELLOW]) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW])
		} // else if (lastColor == nextDangerColor)
		else if (nextDangerColor != NONE) {
			// Your next player only remains less than 3 cards, but
			// fortunately, current legal color is not its dangerous color.
			// Be careful not to re-change the legal color to its dangerous
			// color again.
			if (hasNumIn[bestColor] && nextDangerColor != bestColor) {
				idxBest = idxNumIn[bestColor];
			} // if (hasNumIn[bestColor] && nextDangerColor != bestColor)
			else if (hasNumIn[RED] && nextDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && nextDangerColor != RED)
			else if (hasNumIn[BLUE] && nextDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && nextDangerColor != BLUE)
			else if (hasNumIn[GREEN] && nextDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && nextDangerColor != GREEN)
			else if (hasNumIn[YELLOW] && nextDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && nextDangerColor != YELLOW)
			else if (hasRev &&
				prevSize >= 4 &&
				hand.at(idxRev)->getRealColor() != nextDangerColor) {
				idxBest = idxRev;
			} // else if (hasRev && ...)
			else if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != nextDangerColor) {
				idxBest = idxSkip;
			} // else if (hasSkip && ...)
		} // else if (nextDangerColor != NONE)
		else {
			// Your next player started an UNO dash without playing a wild
			// card, so use normal defense strategies.
			if (nextSafeColor != NONE &&
				(oppoSize > 1 || oppoDangerColor != nextSafeColor) &&
				(prevSize > 1 || prevDangerColor != nextSafeColor)) {
				// Determine your best color in dangerous cases. Firstly
				// choose next player's safe color, but be careful of the
				// conflict with other opponents' dangerous colors!
				bestColor = nextSafeColor;
			} // if (nextSafeColor != NONE && ...)
			else while (bestColor == nextDangerColor
				|| oppoSize == 1 && bestColor == oppoDangerColor
				|| prevSize == 1 && bestColor == prevDangerColor) {
				bestColor = Color(rand() % 4 + 1);
			} // else while (bestColor == nextDangerColor || ...)

			if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // if (hasSkip)
			else if (hasWildDraw4 &&
				lastColor != bestColor &&
				!hasNumIn[lastColor]) {
				// Then play a [wild +4] to make your next player draw four
				// cards (if it's legal to play this card).
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4 && ...)
			else if (hasRev) {
				// Play a [reverse] to get help from your opposite player.
				idxBest = idxRev;
			} // else if (hasRev)
			else if (hasNumIn[bestColor]) {
				// Play a number card or a wild card to change legal color
				// to your best as far as you can.
				idxBest = idxNumIn[bestColor];
			} // else if (hasNumIn[bestColor])
			else if (hasWild && lastColor != bestColor) {
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasWildDraw4 && lastColor != bestColor) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4 && lastColor != bestColor)
			else if (hasNumIn[RED]) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED])
			else if (hasNumIn[BLUE]) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE])
			else if (hasNumIn[GREEN]) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN])
			else if (hasNumIn[YELLOW]) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW])
		} // else
	} // if (nextSize <= 2)
	else if (prevSize == 1) {
		// Strategies when your previous player remains only one card.
		// Save your action cards as much as you can. once a reverse card is
		// played, you can use these cards to limit your previous player's
		// action.
		if (lastColor == prevDangerColor) {
			// Your previous player played a wild card, started an UNO dash
			// in its last action. You have to change the following legal
			// color, or you will approximately 100% lose this game.
			while (bestColor == prevDangerColor
				|| oppoSize == 1 && bestColor == oppoDangerColor) {
				// In dangerous cases, you could not just play wild cards
				// and select your best color. Do not choose any of your
				// opponents' dangerous color!
				bestColor = Color(rand() % 4 + 1);
			} // while (bestColor == prevDangerColor || ...)

			if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != prevDangerColor) {
				// When your opposite player played a [skip], and you have a
				// [skip] with different color, play it.
				idxBest = idxSkip;
			} // if (hasSkip && ...)
			else if (hasWild) {
				// Now start to use wild cards.
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasWildDraw4) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
			else if (hasNumIn[bestColor]) {
				// When you have no wild cards, play a number card and try
				// to get help from other players.
				idxBest = idxNumIn[bestColor];
			} // else if (hasNumIn[bestColor])
			else if (hasNumIn[RED]) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED])
			else if (hasNumIn[BLUE]) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE])
			else if (hasNumIn[GREEN]) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN])
			else if (hasNumIn[YELLOW]) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW])
		} // if (lastColor == prevDangerColor)
		else if (prevDangerColor != NONE) {
			// Your previous player played a wild card, started an UNO dash
			// in its last action, but fortunately the legal color has been
			// changed already. Just be careful not to re-change the legal
			// color to the dangerous color again.
			if (hasNumIn[bestColor] && prevDangerColor != bestColor) {
				idxBest = idxNumIn[bestColor];
			} // if (hasNumIn[bestColor] && prevDangerColor != bestColor)
			else if (hasNumIn[RED] && prevDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && prevDangerColor != RED)
			else if (hasNumIn[BLUE] && prevDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && prevDangerColor != BLUE)
			else if (hasNumIn[GREEN] && prevDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && prevDangerColor != GREEN)
			else if (hasNumIn[YELLOW] && prevDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && prevDangerColor != YELLOW)
		} // else if (prevDangerColor != NONE)
		else {
			// Your previous player started an UNO dash without playing a
			// wild card, so use normal defense strategies.
			if (prevSafeColor != NONE &&
				(oppoSize > 1 || oppoDangerColor != prevSafeColor)) {
				// Determine your best color in dangerous cases. Firstly
				// choose previous player's safe color, but be careful of
				// the conflict with other opponents' dangerous colors!
				bestColor = prevSafeColor;
			} // if (prevSafeColor != NONE && ...)
			else while (bestColor == prevDangerColor
				|| oppoSize == 1 && bestColor == oppoDangerColor) {
				bestColor = Color(rand() % 4 + 1);
			} // else while (bestColor == prevDangerColor || ...)

			if (hasNumIn[bestColor]) {
				idxBest = idxNumIn[bestColor];
			} // if (hasNumIn[bestColor])
			else if (hasWild && lastColor != bestColor) {
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasWildDraw4 && lastColor != bestColor) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4 && lastColor != bestColor)
			else if (hasNumIn[RED]) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED])
			else if (hasNumIn[BLUE]) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE])
			else if (hasNumIn[GREEN]) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN])
			else if (hasNumIn[YELLOW]) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW])
		} // else
	} // else if (prevSize == 1)
	else if (oppoSize == 1) {
		// Strategies when your opposite player remains only one card.
		// Give more freedom to your next player, the only one that can
		// directly limit your opposite player's action.
		if (lastColor == oppoDangerColor) {
			// Your opposite player played a wild card, started an UNO dash
			// in its last action, and what's worse is that the legal color
			// has not been changed yet. You have to change the following
			// legal color, or you will approximately 100% lose this game.
			while (bestColor == oppoDangerColor) {
				// In dangerous cases, you could not just play wild cards
				// and select your best color. Do not choose any of your
				// opponents' dangerous color!
				bestColor = Color(rand() % 4 + 1);
			} // while (bestColor == oppoDangerColor)

			if (hasNumIn[bestColor]) {
				// At first, try to change legal color by playing an action
				// card or a number card, instead of using wild cards.
				idxBest = idxNumIn[bestColor];
			} // if (hasNumIn[bestColor])
			else if (hasNumIn[RED] && oppoDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && oppoDangerColor != RED)
			else if (hasNumIn[BLUE] && oppoDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && oppoDangerColor != BLUE)
			else if (hasNumIn[GREEN] && oppoDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && oppoDangerColor != GREEN)
			else if (hasNumIn[YELLOW] && oppoDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && oppoDangerColor != YELLOW)
			else if (hasRev &&
				hand.at(idxRev)->getRealColor() != oppoDangerColor) {
				idxBest = idxRev;
			} // else if (hasRev && ...)
			else if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != oppoDangerColor) {
				idxBest = idxSkip;
			} // else if (hasSkip && ...)
			else if (hasDraw2 &&
				hand.at(idxDraw2)->getRealColor() != oppoDangerColor) {
				idxBest = idxDraw2;
			} // else if (hasDraw2 && ...)
			else if (hasWild) {
				// Now start to use your wild cards.
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasWildDraw4) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
			else if (hasRev && prevSize - nextSize >= 3) {
				// Finally try to get help from other players.
				idxBest = idxRev;
			} // else if (hasRev && prevSize - nextSize >= 3)
			else if (hasNumIn[RED]) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED])
			else if (hasNumIn[BLUE]) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE])
			else if (hasNumIn[GREEN]) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN])
			else if (hasNumIn[YELLOW]) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW])
		} // if (lastColor == oppoDangerColor)
		else if (oppoDangerColor != NONE) {
			// Your opposite player played a wild card, started an UNO dash
			// in its last action, but fortunately the legal color has been
			// changed already. Just be careful not to re-change the legal
			// color to the dangerous color again.
			if (hasNumIn[bestColor] && oppoDangerColor != bestColor) {
				idxBest = idxNumIn[bestColor];
			} // if (hasNumIn[bestColor] && oppoDangerColor != bestColor)
			else if (hasNumIn[RED] && oppoDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && oppoDangerColor != RED)
			else if (hasNumIn[BLUE] && oppoDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && oppoDangerColor != BLUE)
			else if (hasNumIn[GREEN] && oppoDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && oppoDangerColor != GREEN)
			else if (hasNumIn[YELLOW] && oppoDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && oppoDangerColor != YELLOW)
			else if (hasRev &&
				hand.at(idxRev)->getRealColor() != oppoDangerColor) {
				idxBest = idxRev;
			} // else if (hasRev && ...)
			else if (hasSkip &&
				nextSize <= 4 &&
				hand.at(idxSkip)->getRealColor() != oppoDangerColor) {
				idxBest = idxSkip;
			} // else if (hasSkip && ...)
			else if (hasDraw2 &&
				nextSize <= 4 &&
				hand.at(idxDraw2)->getRealColor() != oppoDangerColor) {
				idxBest = idxDraw2;
			} // else if (hasDraw2 && ...)
		} // else if (oppoDangerColor != NONE)
		else {
			// Your opposite player started an UNO dash without playing a
			// wild card, so use normal defense strategies.
			if (oppoSafeColor != NONE) {
				// Determine your best color in dangerous cases. Firstly
				// choose opposite player's safe color, but be careful of
				// the conflict with other opponents' dangerous colors!
				bestColor = oppoSafeColor;
			} // if (oppoSafeColor != NONE)
			else while (bestColor == oppoDangerColor) {
				bestColor = Color(rand() % 4 + 1);
			} // else while (bestColor == oppoDangerColor)

			if (hasRev && prevSize - nextSize >= 3) {
				// Firstly play a [reverse] when your next player remains
				// only a few cards but your previous player remains a lot
				// of cards, because it seems that your previous player has
				// more possibility to limit your opposite player's action.
				idxBest = idxRev;
			} // if (hasRev && prevSize - nextSize >= 3)
			else if (hasNumIn[bestColor]) {
				// Then you can play a number card.
				idxBest = idxNumIn[bestColor];
			} // else if (hasNumIn[bestColor])
			else if (hasNumIn[RED]) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED])
			else if (hasNumIn[BLUE]) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE])
			else if (hasNumIn[GREEN]) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN])
			else if (hasNumIn[YELLOW]) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW])
			else if (hasWild && lastColor != bestColor) {
				// When you have no more legal number/reverse cards to play,
				// try to play a wild card and change the legal color to
				// your best. Do not play any [+2]/[skip] to your next
				// player!
				idxBest = idxWild;
			} // else if (hasWild && lastColor != bestColor)
			else if (hasWildDraw4 && lastColor != bestColor) {
				// When you have no more legal number/reverse cards to play,
				// try to play a wild card and change the legal color to
				// your best. Specially, for [wild +4] cards, you can only
				// play it when your next player remains only a few cards,
				// and what you did can help your next player find more
				// useful cards, such as action cards, or [wild +4] cards.
				if (nextSize <= 4) {
					idxBest = idxWildDraw4;
				} // if (nextSize <= 4)
			} // else if (hasWildDraw4 && lastColor != bestColor)
		} // else
	} // else if (oppoSize == 1)
	else if (next->getRecent() == nullptr && yourSize > 2) {
		// Strategies when your next player drew a card in its last action.
		// Unless keeping or changing to your best color, you do not need to
		// play your limitation/wild cards. Use them in more dangerous cases.
		if (hasRev && prevSize - nextSize >= 3) {
			idxBest = idxRev;
		} // if (hasRev && prevSize - nextSize >= 3)
		else if (hasNumIn[bestColor]) {
			idxBest = idxNumIn[bestColor];
		} // else if (hasNumIn[bestColor])
		else if (hasNumIn[RED]) {
			idxBest = idxNumIn[RED];
		} // else if (hasNumIn[RED])
		else if (hasNumIn[BLUE]) {
			idxBest = idxNumIn[BLUE];
		} // else if (hasNumIn[BLUE])
		else if (hasNumIn[GREEN]) {
			idxBest = idxNumIn[GREEN];
		} // else if (hasNumIn[GREEN])
		else if (hasNumIn[YELLOW]) {
			idxBest = idxNumIn[YELLOW];
		} // else if (hasNumIn[YELLOW])
		else if (hasRev &&
			(prevSize >= 4 || prev->getRecent() == nullptr)) {
			idxBest = idxRev;
		} // else if (hasRev && ...)
		else if (hasSkip &&
			hand.at(idxSkip)->getRealColor() == bestColor) {
			idxBest = idxSkip;
		} // else if (hasSkip && ...)
		else if (hasDraw2 &&
			hand.at(idxDraw2)->getRealColor() == bestColor) {
			idxBest = idxDraw2;
		} // else if (hasDraw2 && ...)
	} // else if (next->getRecent() == nullptr && yourSize > 2)
	else {
		// Normal strategies
		if (hasDraw2 && nextSize <= 4) {
			// Play a [+2] when your next player remains only a few cards.
			idxBest = idxDraw2;
		} // if (hasDraw2 && nextSize <= 4)
		else if (hasSkip && nextSize <= 4) {
			// Play a [skip] when your next player remains only a few cards.
			idxBest = idxSkip;
		} // else if (hasSkip && nextSize <= 4)
		else if (hasRev && prevSize - nextSize >= 3) {
			// Play a [reverse] when your next player remains only a few
			// cards but your previous player remains a lot of cards, in
			// order to balance everyone's hand-card amount.
			idxBest = idxRev;
		} // else if (hasRev && prevSize - nextSize >= 3)
		else if (hasNumIn[bestColor]) {
			// Play number cards.
			idxBest = idxNumIn[bestColor];
		} // else if (hasNumIn[bestColor])
		else if (hasNumIn[RED]) {
			idxBest = idxNumIn[RED];
		} // else if (hasNumIn[RED])
		else if (hasNumIn[BLUE]) {
			idxBest = idxNumIn[BLUE];
		} // else if (hasNumIn[BLUE])
		else if (hasNumIn[GREEN]) {
			idxBest = idxNumIn[GREEN];
		} // else if (hasNumIn[GREEN])
		else if (hasNumIn[YELLOW]) {
			idxBest = idxNumIn[YELLOW];
		} // else if (hasNumIn[YELLOW])
		else if (hasRev &&
			(prevSize >= 4 || prev->getRecent() == nullptr)) {
			// When you have no more legal number cards to play, you can
			// play a [reverse] safely when your previous player still has
			// a number of cards, or drew a card in its previous action
			// (this probably makes it draw a card again).
			idxBest = idxRev;
		} // else if (hasRev && ...)
		else if (hasSkip &&
			hand.at(idxSkip)->getRealColor() == bestColor) {
			// Unless keeping or changing to your best color, you do not
			// need to play your limitation/wild cards when your next player
			// still has a number of cards. Use them in more dangerous cases.
			idxBest = idxSkip;
		} // else if (hasSkip && ...)
		else if (hasDraw2 &&
			hand.at(idxDraw2)->getRealColor() == bestColor) {
			idxBest = idxDraw2;
		} // else if (hasDraw2 && ...)
		else if (hasWild && nextSize <= 4 && lastColor != nextSafeColor) {
			// When your next player remains only a few cards, and you have
			// no more legal number/action cards to play, try to play a
			// wild card and change the legal color to your best color.
			idxBest = idxWild;
		} // else if (hasWild && ...)
		else if (hasWildDraw4 && nextSize <= 4 && lastColor != nextSafeColor) {
			// When your next player remains only a few cards, and you have
			// no more legal number/action cards to play, try to play a
			// wild card and change the legal color to your best color.
			idxBest = idxWildDraw4;
		} // else if (hasWildDraw4 && ...)
		else if (hasWild && yourSize == 2 && prevSize <= 3) {
			// When you remain only 2 cards, including a wild card, and your
			// previous player seems no enough power to limit you (has too
			// few cards), start your UNO dash!
			idxBest = idxWild;
		} // else if (hasWild && yourSize == 2 && prevSize <= 3)
		else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3) {
			idxBest = idxWildDraw4;
		} // else if (hasWildDraw4 && yourSize == 2 && prevSize <= 3)
		else if (yourSize == Uno::MAX_HOLD_CARDS) {
			// When you are holding 14 cards, which means you cannot hold
			// more cards, you need to play your action/wild cards to keep
			// game running, even if it's not worth enough to use them.
			if (hasSkip) {
				idxBest = idxSkip;
			} // if (hasSkip)
			else if (hasDraw2) {
				idxBest = idxDraw2;
			} // else if (hasDraw2)
			else if (hasRev) {
				idxBest = idxRev;
			} // else if (hasRev)
			else if (hasWild) {
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasWildDraw4) {
				idxBest = idxWildDraw4;
			} // else if (hasWildDraw4)
		} // else if (yourSize == Uno::MAX_HOLD_CARDS)
	} // else

	outColor[0] = bestColor;
	return idxBest;
} // hardAI_bestCardIndex4NowPlayer()

// E.O.F