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
#include <Card.h>
#include <cstdlib>
#include <Color.h>
#include <Player.h>
#include <Content.h>

/**
 * AI strategies of determining if it's necessary to challenge previous
 * player's [wild +4] card's legality.
 *
 * @param challenger Who challenges. Must be one of the following values:
 *                   Player::YOU, Player::COM1, Player::COM2, Player::COM3.
 * @return Whether the challenger needs to make a challenge.
 */
bool AI::needToChallenge(int challenger) {
	bool challenge;
	Card* next2last;
	Color draw4Color;
	Color colorBeforeDraw4;
	std::vector<Card*> hand, recent;

	hand = uno->getPlayer(challenger)->getHandCards();
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
		recent = uno->getRecent();
		next2last = recent.at(recent.size() - 2);
		colorBeforeDraw4 = next2last->getRealColor();
		draw4Color = recent.back()->getRealColor();
		challenge = draw4Color == colorBeforeDraw4;
	} // else

	return challenge;
} // needToChallenge()

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
int AI::easyAI_bestCardIndex4NowPlayer(Card* drawnCard, Color outColor[]) {
	int i;
	bool legal;
	Card* card;
	Card* last;
	Player* curr;
	Player* next;
	Player* prev;
	std::vector<Card*> hand;
	Color bestColor, lastColor;
	int yourSize, nextSize, prevSize;
	bool hasNum, hasRev, hasSkip, hasDraw2, hasWild, hasWD4;
	int idxBest, idxNum, idxRev, idxSkip, idxDraw2, idxWild, idxWD4;

	if (outColor == nullptr) {
		throw "outColor[] cannot be nullptr";
	} // if (outColor == nullptr)

	curr = uno->getPlayer(uno->getNow());
	hand = curr->getHandCards();
	yourSize = int(hand.size());
	if (yourSize == 1) {
		// Only one card remained. Play it when it's legal.
		card = hand.at(0);
		outColor[0] = card->getRealColor();
		return uno->isLegalToPlay(card) ? 0 : -1;
	} // if (yourSize == 1)

	next = uno->getPlayer(uno->getNext());
	nextSize = int(next->getHandCards().size());
	prev = uno->getPlayer(uno->getPrev());
	prevSize = int(prev->getHandCards().size());
	hasNum = hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
	idxBest = idxNum = idxRev = idxSkip = idxDraw2 = idxWild = idxWD4 = -1;
	bestColor = calcBestColor4NowPlayer();
	last = uno->getRecent().back();
	lastColor = last->getRealColor();
	for (i = 0; i < yourSize; ++i) {
		// Index of any kind
		card = hand.at(i);
		if (drawnCard == nullptr) {
			legal = uno->isLegalToPlay(card);
		} // if (drawnCard == nullptr)
		else {
			legal = card == drawnCard;
		} // else

		if (legal) {
			switch (card->content) {
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
				idxWD4 = i;
				hasWD4 = true;
				break; // case WILD_DRAW4

			default: // number cards
				if (!hasNum || card->getRealColor() == bestColor) {
					idxNum = i;
					hasNum = true;
				} // if (!hasNum || card->getRealColor() == bestColor)
				break; // default
			} // switch (card->content)
		} // if (legal)
	} // for (i = 0; i < yourSize; ++i)

	// Decision tree
	if (nextSize == 1) {
		// Strategies when your next player remains only one card.
		// Limit your next player's action as well as you can.
		if (hasDraw2) {
			idxBest = idxDraw2;
		} // if (hasDraw2)
		else if (hasSkip) {
			idxBest = idxSkip;
		} // else if (hasSkip)
		else if (hasRev) {
			idxBest = idxRev;
		} // else if (hasRev)
		else if (hasWD4) {
			idxBest = idxWD4;
		} // else if (hasWD4)
		else if (hasWild && lastColor != bestColor) {
			idxBest = idxWild;
		} // else if (hasWild && lastColor != bestColor)
		else if (hasNum) {
			idxBest = idxNum;
		} // else if (hasNum)
	} // if (nextSize == 1)
	else {
		// Normal strategies
		if (hasRev && prevSize > nextSize) {
			idxBest = idxRev;
		} // if (hasRev && prevSize > nextSize)
		else if (hasNum) {
			idxBest = idxNum;
		} // else if (hasNum)
		else if (hasSkip) {
			idxBest = idxSkip;
		} // else if (hasSkip)
		else if (hasDraw2) {
			idxBest = idxDraw2;
		} // else if (hasDraw2)
		else if (hasRev && prevSize >= 4) {
			idxBest = idxRev;
		} // else if (hasRev && prevSize >= 4)
		else if (hasWild) {
			idxBest = idxWild;
		} // else if (hasWild)
		else if (hasWD4) {
			idxBest = idxWD4;
		} // else if (hasWD4)
	} // else

	outColor[0] = bestColor;
	return idxBest;
} // easyAI_bestCardIndex4NowPlayer()

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
int AI::hardAI_bestCardIndex4NowPlayer(Card* drawnCard, Color outColor[]) {
	int i;
	Card* card;
	Card* last;
	Player* curr;
	Player* next;
	Player* oppo;
	Player* prev;
	bool legal, allWild;
	std::vector<Card*> hand;
	Color bestColor, lastColor, safeColor;
	int yourSize, nextSize, oppoSize, prevSize;
	Color nextSafeColor, oppoSafeColor, prevSafeColor;
	Color nextDangerColor, oppoDangerColor, prevDangerColor;
	bool hasNumIn[5], hasRev, hasSkip, hasDraw2, hasWild, hasWD4;
	int idxBest, idxNumIn[5], idxRev, idxSkip, idxDraw2, idxWild, idxWD4;

	if (outColor == nullptr) {
		throw "outColor[] cannot be nullptr";
	} // if (outColor == nullptr)

	curr = uno->getPlayer(uno->getNow());
	hand = curr->getHandCards();
	yourSize = int(hand.size());
	if (yourSize == 1) {
		// Only one card remained. Play it when it's legal.
		card = hand.at(0);
		outColor[0] = card->getRealColor();
		return uno->isLegalToPlay(card) ? 0 : -1;
	} // if (yourSize == 1)

	next = uno->getPlayer(uno->getNext());
	nextSize = int(next->getHandCards().size());
	oppo = uno->getPlayer(uno->getOppo());
	oppoSize = int(oppo->getHandCards().size());
	prev = uno->getPlayer(uno->getPrev());
	prevSize = int(prev->getHandCards().size());
	hasRev = hasSkip = hasDraw2 = hasWild = hasWD4 = false;
	idxBest = idxRev = idxSkip = idxDraw2 = idxWild = idxWD4 = -1;
	idxNumIn[0] = idxNumIn[1] = idxNumIn[2] = idxNumIn[3] = idxNumIn[4] = -1;
	hasNumIn[0] = hasNumIn[1] = hasNumIn[2] = hasNumIn[3] = hasNumIn[4] = false;
	bestColor = calcBestColor4NowPlayer();
	last = uno->getRecent().back();
	lastColor = last->getRealColor();
	allWild = true;
	for (i = 0; i < yourSize; ++i) {
		// Index of any kind
		card = hand.at(i);
		allWild = allWild && card->isWild();
		if (drawnCard == nullptr) {
			legal = uno->isLegalToPlay(card);
		} // if (drawnCard == nullptr)
		else {
			legal = card == drawnCard;
		} // else

		if (legal) {
			switch (card->content) {
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
				idxWD4 = i;
				hasWD4 = true;
				break; // case WILD_DRAW4

			default: // number cards
				if (!hasNumIn[card->getRealColor()]) {
					idxNumIn[card->getRealColor()] = i;
					hasNumIn[card->getRealColor()] = true;
				} // if (!hasNumIn[card->getRealColor()])
				break; // default
			} // switch (card->content)
		} // if (legal)
	} // for (i = 0; i < yourSize; ++i)

	// Decision tree
	nextSafeColor = next->getSafeColor();
	oppoSafeColor = oppo->getSafeColor();
	prevSafeColor = prev->getSafeColor();
	nextDangerColor = next->getDangerousColor();
	oppoDangerColor = oppo->getDangerousColor();
	prevDangerColor = prev->getDangerousColor();
	if (nextSize == 1) {
		// Strategies when your next player remains only one card.
		// Limit your next player's action as well as you can.
		if (nextSafeColor == NONE ||
			nextDangerColor != NONE && nextDangerColor != bestColor) {
			safeColor = bestColor;
		} // if (nextSafeColor == NONE || ...)
		else {
			safeColor = nextSafeColor;
		} // else

		while (safeColor == nextDangerColor
			|| oppoSize == 1 && safeColor == oppoDangerColor
			|| prevSize == 1 && safeColor == prevDangerColor) {
			// Determine your best color in dangerous cases. Firstly
			// choose next player's safe color, but be careful of the
			// conflict with other opponents' dangerous colors!
			safeColor = Color(rand() % 4 + 1);
		} // while (safeColor == nextDangerColor || ...)

		if (hasDraw2) {
			// Play a [+2] to make your next player draw two cards!
			idxBest = idxDraw2;
		} // if (hasDraw2)
		else if (lastColor == nextDangerColor) {
			// Your next player played a wild card, started an UNO dash in
			// its last action. You have to change the following legal
			// color, or you will approximately 100% lose this game.
			if (hasNumIn[safeColor]) {
				idxBest = idxNumIn[safeColor];
			} // if (hasNumIn[safeColor])
			else if (hasNumIn[RED] &&
				(prevSize > 1 || prevDangerColor != RED) &&
				(oppoSize > 1 || oppoDangerColor != RED) &&
				nextDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && ...)
			else if (hasNumIn[BLUE] &&
				(prevSize > 1 || prevDangerColor != BLUE) &&
				(oppoSize > 1 || oppoDangerColor != BLUE) &&
				nextDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && ...)
			else if (hasNumIn[GREEN] &&
				(prevSize > 1 || prevDangerColor != GREEN) &&
				(oppoSize > 1 || oppoDangerColor != GREEN) &&
				nextDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && ...)
			else if (hasNumIn[YELLOW] &&
				(prevSize > 1 || prevDangerColor != YELLOW) &&
				(oppoSize > 1 || oppoDangerColor != YELLOW) &&
				nextDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && ...)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // else if (hasSkip)
			else if (hasWD4) {
				// Now start to use wild cards.
				bestColor = safeColor;
				idxBest = idxWD4;
			} // else if (hasWD4)
			else if (hasWild) {
				bestColor = safeColor;
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasRev) {
				// Finally play a [reverse] to get help from other players.
				// If you even do not have this choice, you lose this game.
				idxBest = idxRev;
			} // else if (hasRev)
		} // else if (lastColor == nextDangerColor)
		else if (nextDangerColor != NONE) {
			// Your next player played a wild card, started an UNO dash in
			// its last action, but fortunately the legal color has been
			// changed already. Just be careful not to re-change the legal
			// color to the dangerous color again.
			if (hasNumIn[safeColor]) {
				idxBest = idxNumIn[safeColor];
			} // if (hasNumIn[safeColor])
			else if (hasNumIn[RED] &&
				(prevSize > 1 || prevDangerColor != RED) &&
				(oppoSize > 1 || oppoDangerColor != RED) &&
				nextDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && ...)
			else if (hasNumIn[BLUE] &&
				(prevSize > 1 || prevDangerColor != BLUE) &&
				(oppoSize > 1 || oppoDangerColor != BLUE) &&
				nextDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && ...)
			else if (hasNumIn[GREEN] &&
				(prevSize > 1 || prevDangerColor != GREEN) &&
				(oppoSize > 1 || oppoDangerColor != GREEN) &&
				nextDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && ...)
			else if (hasNumIn[YELLOW] &&
				(prevSize > 1 || prevDangerColor != YELLOW) &&
				(oppoSize > 1 || oppoDangerColor != YELLOW) &&
				nextDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && ...)
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
			if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // if (hasSkip)
			else if (hasWD4 && !hasNumIn[lastColor]) {
				// Then play a [wild +4] to make your next player draw four
				// cards (if it's legal to play this card).
				if (oppoSize == 1 || prevSize == 1) {
					// Be careful when multiple opponents UNOed.
					bestColor = safeColor;
				} // if (oppoSize == 1 || prevSize == 1)

				idxBest = idxWD4;
			} // else if (hasWD4 && !hasNumIn[lastColor])
			else if (hasRev) {
				// Play a [reverse] to get help from your opposite player.
				idxBest = idxRev;
			} // else if (hasRev)
			else if (hasNumIn[safeColor]) {
				// Play a number card or a wild card to change legal color
				// to your best as far as you can.
				idxBest = idxNumIn[safeColor];
			} // else if (hasNumIn[safeColor])
			else if (lastColor != safeColor) {
				if (hasWild) {
					bestColor = safeColor;
					idxBest = idxWild;
				} // if (hasWild)
				else if (hasWD4) {
					bestColor = safeColor;
					idxBest = idxWD4;
				} // else if (hasWD4)
				else if (hasNumIn[RED] &&
					(prevSize > 1 || prevDangerColor != RED) &&
					(oppoSize > 1 || oppoDangerColor != RED)) {
					idxBest = idxNumIn[RED];
				} // else if (hasNumIn[RED] && ...)
				else if (hasNumIn[BLUE] &&
					(prevSize > 1 || prevDangerColor != BLUE) &&
					(oppoSize > 1 || oppoDangerColor != BLUE)) {
					idxBest = idxNumIn[BLUE];
				} // else if (hasNumIn[BLUE] && ...)
				else if (hasNumIn[GREEN] &&
					(prevSize > 1 || prevDangerColor != GREEN) &&
					(oppoSize > 1 || oppoDangerColor != GREEN)) {
					idxBest = idxNumIn[GREEN];
				} // else if (hasNumIn[GREEN] && ...)
				else if (hasNumIn[YELLOW] &&
					(prevSize > 1 || prevDangerColor != YELLOW) &&
					(oppoSize > 1 || oppoDangerColor != YELLOW)) {
					idxBest = idxNumIn[YELLOW];
				} // else if (hasNumIn[YELLOW] && ...)
			} // else if (lastColor != safeColor)
		} // else
	} // if (nextSize == 1)
	else if (prevSize == 1) {
		// Strategies when your previous player remains only one card.
		// Save your action cards as much as you can. once a reverse card is
		// played, you can use these cards to limit your previous player's
		// action.
		if (prevSafeColor == NONE ||
			prevDangerColor != NONE && prevDangerColor != bestColor) {
			safeColor = bestColor;
		} // if (prevSafeColor == NONE || ...)
		else {
			safeColor = prevSafeColor;
		} // else

		while (safeColor == prevDangerColor
			|| oppoSize == 1 && safeColor == oppoDangerColor) {
			// Determine your best color in dangerous cases. Firstly
			// choose previous player's safe color, but be careful of
			// the conflict with other opponents' dangerous colors!
			safeColor = Color(rand() % 4 + 1);
		} // while (safeColor == prevDangerColor || ...)

		if (lastColor == prevDangerColor) {
			// Your previous player played a wild card, started an UNO dash
			// in its last action. You have to change the following legal
			// color, or you will approximately 100% lose this game.
			if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != prevDangerColor) {
				// When your opposite player played a [skip], and you have a
				// [skip] with different color, play it.
				idxBest = idxSkip;
			} // if (hasSkip && ...)
			else if (hasWild) {
				// Now start to use wild cards.
				bestColor = safeColor;
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasWD4) {
				bestColor = safeColor;
				idxBest = idxWD4;
			} // else if (hasWD4)
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
			if (hasNumIn[safeColor]) {
				idxBest = idxNumIn[safeColor];
			} // if (hasNumIn[safeColor])
			else if (hasNumIn[RED] &&
				(oppoSize > 1 || oppoDangerColor != RED) &&
				prevDangerColor != RED) {
				idxBest = idxNumIn[RED];
			} // else if (hasNumIn[RED] && ...)
			else if (hasNumIn[BLUE] &&
				(oppoSize > 1 || oppoDangerColor != BLUE) &&
				prevDangerColor != BLUE) {
				idxBest = idxNumIn[BLUE];
			} // else if (hasNumIn[BLUE] && ...)
			else if (hasNumIn[GREEN] &&
				(oppoSize > 1 || oppoDangerColor != GREEN) &&
				prevDangerColor != GREEN) {
				idxBest = idxNumIn[GREEN];
			} // else if (hasNumIn[GREEN] && ...)
			else if (hasNumIn[YELLOW] &&
				(oppoSize > 1 || oppoDangerColor != YELLOW) &&
				prevDangerColor != YELLOW) {
				idxBest = idxNumIn[YELLOW];
			} // else if (hasNumIn[YELLOW] && ...)
		} // else if (prevDangerColor != NONE)
		else {
			// Your previous player started an UNO dash without playing a
			// wild card, so use normal defense strategies.
			if (hasNumIn[safeColor]) {
				idxBest = idxNumIn[safeColor];
			} // if (hasNumIn[safeColor])
			else if (hasWild && lastColor != safeColor) {
				bestColor = safeColor;
				idxBest = idxWild;
			} // else if (hasWild && lastColor != safeColor)
			else if (hasWD4 && lastColor != safeColor) {
				bestColor = safeColor;
				idxBest = idxWD4;
			} // else if (hasWD4 && lastColor != safeColor)
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
		} // else
	} // else if (prevSize == 1)
	else if (oppoSize == 1) {
		// Strategies when your opposite player remains only one card.
		// Give more freedom to your next player, the only one that can
		// directly limit your opposite player's action.
		if (oppoSafeColor == NONE ||
			oppoDangerColor != NONE && oppoDangerColor != bestColor) {
			safeColor = bestColor;
		} // if (oppoSafeColor == NONE || ...)
		else {
			safeColor = oppoSafeColor;
		} // else

		while (safeColor == oppoDangerColor) {
			// Determine your best color in dangerous cases. Firstly
			// choose opposite player's safe color, but be careful of
			// the conflict with other opponents' dangerous colors!
			safeColor = Color(rand() % 4 + 1);
		} // while (safeColor == oppoDangerColor || ...)

		if (lastColor == oppoDangerColor) {
			// Your opposite player played a wild card, started an UNO dash
			// in its last action, and what's worse is that the legal color
			// has not been changed yet. You have to change the following
			// legal color, or you will approximately 100% lose this game.
			if (hasNumIn[safeColor]) {
				// At first, try to change legal color by playing an action
				// card or a number card, instead of using wild cards.
				idxBest = idxNumIn[safeColor];
			} // if (hasNumIn[safeColor])
			else if (hasNumIn[bestColor] && oppoDangerColor != bestColor) {
				idxBest = idxNumIn[bestColor];
			} // else if (hasNumIn[bestColor] && oppoDangerColor != bestColor)
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
				bestColor = safeColor;
				idxBest = idxWild;
			} // else if (hasWild)
			else if (hasWD4) {
				bestColor = safeColor;
				idxBest = idxWD4;
			} // else if (hasWD4)
			else if (hasRev && prevSize - nextSize >= 3) {
				// Finally try to get help from other players.
				idxBest = idxRev;
			} // else if (hasRev && prevSize - nextSize >= 3)
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
		} // if (lastColor == oppoDangerColor)
		else if (oppoDangerColor != NONE) {
			// Your opposite player played a wild card, started an UNO dash
			// in its last action, but fortunately the legal color has been
			// changed already. Just be careful not to re-change the legal
			// color to the dangerous color again.
			if (hasNumIn[safeColor]) {
				idxBest = idxNumIn[safeColor];
			} // if (hasNumIn[safeColor])
			else if (hasNumIn[bestColor] && oppoDangerColor != bestColor) {
				idxBest = idxNumIn[bestColor];
			} // else if (hasNumIn[bestColor] && oppoDangerColor != bestColor)
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
			if (hasRev && prevSize - nextSize >= 3) {
				// Firstly play a [reverse] when your next player remains
				// only a few cards but your previous player remains a lot
				// of cards, because it seems that your previous player has
				// more possibility to limit your opposite player's action.
				idxBest = idxRev;
			} // if (hasRev && prevSize - nextSize >= 3)
			else if (hasNumIn[safeColor]) {
				// Then you can play a number card.
				idxBest = idxNumIn[safeColor];
			} // else if (hasNumIn[safeColor])
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
			else if (hasWild && lastColor != safeColor) {
				// When you have no more legal number/reverse cards to play,
				// try to play a wild card and change the legal color to
				// your best. Do not play any [+2]/[skip] to your next
				// player!
				bestColor = safeColor;
				idxBest = idxWild;
			} // else if (hasWild && lastColor != safeColor)
			else if (hasWD4 && lastColor != safeColor && nextSize <= 4) {
				// When you have no more legal number/reverse cards to play,
				// try to play a wild card and change the legal color to
				// your best. Specially, for [wild +4] cards, you can only
				// play it when your next player remains only a few cards,
				// and what you did can help your next player find more
				// useful cards, such as action cards, or [wild +4] cards.
				bestColor = safeColor;
				idxBest = idxWD4;
			} // else if (hasWD4 && lastColor != safeColor && nextSize <= 4)
		} // else
	} // else if (oppoSize == 1)
	else if (allWild) {
		// Strategies when you remain only wild cards.
		// Set the following legal color to one of your opponents' safe
		// colors, in order to prevent them from playing a [+2] to you.
		if (prevSafeColor != NONE) {
			bestColor = prevSafeColor;
		} // if (prevSafeColor != NONE)
		else if (oppoSafeColor != NONE) {
			bestColor = oppoSafeColor;
		} // else if (oppoSafeColor != NONE)
		else if (nextSafeColor != NONE) {
			bestColor = nextSafeColor;
		} // else if (nextSafeColor != NONE)
		else {
			while (bestColor == prevDangerColor
				|| bestColor == oppoDangerColor
				|| bestColor == nextDangerColor) {
				bestColor = Color(rand() % 4 + 1);
			} // while (bestColor == prevDangerColor || ...)
		} // else

		// When your next player remains only a few cards, use [Wild +4]
		// cards at first. Otherwise, use [Wild] cards at first.
		if (nextSize <= 4) {
			if (hasWD4) {
				idxBest = idxWD4;
			} // if (hasWD4)
			else {
				idxBest = idxWild;
			} // else
		} // if (nextSize <= 4)
		else {
			if (hasWild) {
				idxBest = idxWild;
			} // if (hasWild)
			else {
				idxBest = idxWD4;
			} // else
		} // else
	} // else if (allWild)
	else if (lastColor == nextSafeColor && yourSize > 2) {
		// Strategies when your next player drew a card in its last action.
		// Unless keeping or changing to your best color, you do not need to
		// play your limitation/wild cards. Use them in more dangerous cases.
		if (hasRev && prevSize - nextSize >= 3) {
			idxBest = idxRev;
		} // if (hasRev && prevSize - nextSize >= 3)
		else if (hasNumIn[nextSafeColor]) {
			idxBest = idxNumIn[nextSafeColor];
		} // else if (hasNumIn[nextSafeColor])
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
			oppoSize >= 3 &&
			hand.at(idxSkip)->getRealColor() == bestColor) {
			idxBest = idxSkip;
		} // else if (hasSkip && ...)
		else if (hasDraw2 &&
			oppoSize >= 3 &&
			hand.at(idxDraw2)->getRealColor() == bestColor) {
			idxBest = idxDraw2;
		} // else if (hasDraw2 && ...)
	} // else if (lastColor == nextSafeColor && yourSize > 2)
	else {
		// Normal strategies
		if (hasDraw2 && nextSize <= 4 && nextSize - oppoSize <= 1) {
			// Play a [+2] when your next player remains only a few cards.
			idxBest = idxDraw2;
		} // if (hasDraw2 && nextSize <= 4 && nextSize - oppoSize <= 1)
		else if (hasSkip && nextSize <= 4 && nextSize - oppoSize <= 1) {
			// Play a [skip] when your next player remains only a few cards.
			idxBest = idxSkip;
		} // else if (hasSkip && nextSize <= 4 && nextSize - oppoSize <= 1)
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
			oppoSize >= 3 &&
			hand.at(idxSkip)->getRealColor() == bestColor) {
			// Unless keeping or changing to your best color, you do not
			// need to play your limitation/wild cards when your next player
			// still has a number of cards. Use them in more dangerous cases.
			idxBest = idxSkip;
		} // else if (hasSkip && ...)
		else if (hasDraw2 &&
			oppoSize >= 3 &&
			hand.at(idxDraw2)->getRealColor() == bestColor) {
			idxBest = idxDraw2;
		} // else if (hasDraw2 && ...)
		else if (hasWild && nextSize <= 4) {
			// When your next player remains only a few cards, and you have
			// no more legal number/action cards to play, try to play a
			// wild card and change the legal color to your best color.
			idxBest = idxWild;
		} // else if (hasWild && nextSize <= 4)
		else if (hasWD4 && nextSize <= 4) {
			// When your next player remains only a few cards, and you have
			// no more legal number/action cards to play, try to play a
			// wild card and change the legal color to your best color.
			idxBest = idxWD4;
		} // else if (hasWD4 && nextSize <= 4)
		else if (hasWild && yourSize == 2 && prevSize <= 3) {
			// When you remain only 2 cards, including a wild card, and your
			// previous player seems no enough power to limit you (has too
			// few cards), start your UNO dash!
			idxBest = idxWild;
		} // else if (hasWild && yourSize == 2 && prevSize <= 3)
		else if (hasWD4 && yourSize == 2 && prevSize <= 3) {
			idxBest = idxWD4;
		} // else if (hasWD4 && yourSize == 2 && prevSize <= 3)
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
			else if (hasWD4) {
				idxBest = idxWD4;
			} // else if (hasWD4)
		} // else if (yourSize == Uno::MAX_HOLD_CARDS)
	} // else

	outColor[0] = bestColor;
	return idxBest;
} // hardAI_bestCardIndex4NowPlayer()

/**
 * Evaluate which color is the best for current player. In our evaluation
 * system, zero cards are worth 2 points, non-zero number cards are worth
 * 4 points, and action cards are worth 5 points. Finally, the color which
 * contains the worthiest cards becomes the best color.
 *
 * @return Current player's best color.
 */
Color AI::calcBestColor4NowPlayer() {
	Color best = RED;
	int score[5] = { 0, 0, 0, 0, 0 };
	Player* curr = uno->getPlayer(uno->getNow());

	for (Card* card : curr->getHandCards()) {
		switch (card->content) {
		case REV:
		case NUM0:
			score[card->getRealColor()] += 2;
			break; // case REV, NUM0

		case SKIP:
		case DRAW2:
			score[card->getRealColor()] += 5;
			break; // case SKIP, DRAW2

		default:
			score[card->getRealColor()] += 4;
			break; // default
		} // switch (card->content)
	} // for (Card* card : curr->getHandCards())

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
} // calcBestColor4NowPlayer()

// E.O.F