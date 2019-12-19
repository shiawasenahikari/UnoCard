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

using namespace std;

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
	vector<Card*> hand, recent;

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
 */
int easyAI_bestCardIndexFor(int whom, Card* drawnCard, Color outColor[]) {
	bool legal;
	Card* card;
	Card* last;
	Player* curr;
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

	if (outColor == nullptr) {
		throw "outColor[] cannot be nullptr";
	} // if (outColor == nullptr)

	curr = sUno->getPlayer(whom);
	hand = curr->getHandCards();
	yourSize = (int)hand.size();
	if (yourSize == 1) {
		// Only one card remained. Play it when it's legal.
		card = hand.at(0);
		outColor[0] = card->getRealColor();
		return sUno->isLegalToPlay(card) ? 0 : -1;
	} // if (yourSize == 1)

	direction = sUno->getDirection();
	next = sUno->getPlayer((whom + direction) % 4);
	nextSize = (int)next->getHandCards().size();
	oppo = sUno->getPlayer((whom + 2) % 4);
	oppoSize = (int)oppo->getHandCards().size();
	prev = sUno->getPlayer((4 + whom - direction) % 4);
	prevSize = (int)prev->getHandCards().size();
	idxBest = idxRev = idxSkip = idxDraw2 = -1;
	idxZero = idxNum = idxWild = idxWildDraw4 = -1;
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
			case NUM0:
				if (idxZero < 0 || card->getRealColor() == bestColor) {
					idxZero = i;
				} // if (idxZero < 0 || card->getRealColor() == bestColor)
				break; // case NUM0

			case DRAW2:
				if (idxDraw2 < 0 || card->getRealColor() == bestColor) {
					idxDraw2 = i;
				} // if (idxDraw2 < 0 || card->getRealColor() == bestColor)
				break; // case DRAW2

			case SKIP:
				if (idxSkip < 0 || card->getRealColor() == bestColor) {
					idxSkip = i;
				} // if (idxSkip < 0 || card->getRealColor() == bestColor)
				break; // case SKIP

			case REV:
				if (idxRev < 0 || card->getRealColor() == bestColor) {
					idxRev = i;
				} // if (idxRev < 0 || card->getRealColor() == bestColor)
				break; // case REV

			case WILD:
				idxWild = i;
				break; // case WILD

			case WILD_DRAW4:
				idxWildDraw4 = i;
				break; // case WILD_DRAW4

			default: // non-zero number cards
				if (idxNum < 0 || card->getRealColor() == bestColor) {
					idxNum = i;
				} // if (idxNum < 0 || card->getRealColor() == bestColor)
				break; // default
			} // switch (card->getContent())
		} // if (legal)
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

	outColor[0] = bestColor;
	return idxBest;
} // easyAI_bestCardIndexFor()

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
 */
int hardAI_bestCardIndexFor(int whom, Card* drawnCard, Color outColor[]) {
	bool legal;
	Card* card;
	Card* last;
	Player* curr;
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

	if (outColor == nullptr) {
		throw "outColor[] cannot be nullptr";
	} // if (outColor == nullptr)

	curr = sUno->getPlayer(whom);
	hand = curr->getHandCards();
	yourSize = (int)hand.size();
	if (yourSize == 1) {
		// Only one card remained. Play it when it's legal.
		card = hand.at(0);
		outColor[0] = card->getRealColor();
		return sUno->isLegalToPlay(card) ? 0 : -1;
	} // if (yourSize == 1)

	direction = sUno->getDirection();
	next = sUno->getPlayer((whom + direction) % 4);
	nextSize = (int)next->getHandCards().size();
	oppo = sUno->getPlayer((whom + 2) % 4);
	oppoSize = (int)oppo->getHandCards().size();
	prev = sUno->getPlayer((4 + whom - direction) % 4);
	prevSize = (int)prev->getHandCards().size();
	idxBest = idxRev = idxSkip = idxDraw2 = -1;
	idxZero = idxNum = idxWild = idxWildDraw4 = -1;
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
			case NUM0:
				if (idxZero < 0 || card->getRealColor() == bestColor) {
					idxZero = i;
				} // if (idxZero < 0 || card->getRealColor() == bestColor)
				break; // case NUM0

			case DRAW2:
				if (idxDraw2 < 0 || card->getRealColor() == bestColor) {
					idxDraw2 = i;
				} // if (idxDraw2 < 0 || card->getRealColor() == bestColor)
				break; // case DRAW2

			case SKIP:
				if (idxSkip < 0 || card->getRealColor() == bestColor) {
					idxSkip = i;
				} // if (idxSkip < 0 || card->getRealColor() == bestColor)
				break; // case SKIP

			case REV:
				if (idxRev < 0 || card->getRealColor() == bestColor) {
					idxRev = i;
				} // if (idxRev < 0 || card->getRealColor() == bestColor)
				break; // case REV

			case WILD:
				idxWild = i;
				break; // case WILD

			case WILD_DRAW4:
				idxWildDraw4 = i;
				break; // case WILD_DRAW4

			default: // non-zero number cards
				if (idxNum < 0 || card->getRealColor() == bestColor) {
					idxNum = i;
				} // if (idxNum < 0 || card->getRealColor() == bestColor)
				break; // default
			} // switch (card->getContent())
		} // if (legal)
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
			// Your next player played a wild card, started an UNO dash in
			// its last action, and what's worse is that the legal color has
			// not been changed yet. You have to change the following legal
			// color, or you will approximately 100% lose this game.
			if (hasZero &&
				hand.at(idxZero)->getRealColor() != dangerColor) {
				// When you have no [+2] cards, you have to change the legal
				// color, or use [wild +4] cards. At first, try to change
				// legal color by playing a number card, instead of using
				// wild cards.
				idxBest = idxZero;
			} // if (hasZero && ...)
			else if (hasNum &&
				hand.at(idxNum)->getRealColor() != dangerColor) {
				idxBest = idxNum;
			} // else if (hasNum && ...)
			else if (hasSkip) {
				// Play a [skip] to skip its turn and wait for more chances.
				idxBest = idxSkip;
			} // else if (hasSkip)
			else if (hasWildDraw4) {
				// Now start to use wild cards. Use [wild +4] cards firstly,
				// because this card makes your next player draw four cards.
				while (bestColor == dangerColor
					|| bestColor == oppo->getDangerousColor()
					|| bestColor == prev->getDangerousColor()) {
					bestColor = Color(rand() % 4 + 1);
				} // while (bestColor == dangerColor || ...)

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
			// Your next player played a wild card, started an UNO dash in
			// its last action, but fortunately the legal color has been
			// changed already. Just be careful not to re-change the legal
			// color to the dangerous color again.
			if (hasZero &&
				hand.at(idxZero)->getRealColor() != dangerColor) {
				idxBest = idxZero;
			} // if (hasZero && ...)
			else if (hasNum &&
				hand.at(idxNum)->getRealColor() != dangerColor) {
				idxBest = idxNum;
			} // else if (hasNum && ...)
			else if (hasRev &&
				prevSize >= 4 &&
				hand.at(idxRev)->getRealColor() != dangerColor) {
				idxBest = idxRev;
			} // else if (hasRev && ...)
			else if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != dangerColor) {
				idxBest = idxSkip;
			} // else if (hasSkip && ...)
		} // else if (dangerColor != NONE)
		else if (hasWildDraw4) {
			// Your next player started an UNO dash without playing a wild
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
			// Your previous player played a wild card, started an UNO dash
			// in its last action. You have to change the following legal
			// color, or you will approximately 100% lose this game.
			if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != dangerColor) {
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
				// color, do not play zero cards preferentially.
				idxBest = idxNum;
			} // else if (hasNum)
			else if (hasZero) {
				idxBest = idxZero;
			} // else if (hasZero)
		} // if (lastColor == dangerColor)
		else if (hasNum) {
			// Your previous player started an UNO dash without playing a
			// wild card, so use normal defense strategies. In order to
			// increase your following players' possibility of changing the
			// legal color, do not play zero cards preferentially.
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
			// Your opposite player played a wild card, started an UNO dash
			// in its last action, and what's worse is that the legal color
			// has not been changed yet. You have to change the following
			// legal color, or you will approximately 100% lose this game.
			if (hasZero &&
				hand.at(idxZero)->getRealColor() != dangerColor) {
				// At first, try to change legal color by playing an action
				// card or a number card, instead of using wild cards.
				idxBest = idxZero;
			} // if (hasZero && ...)
			else if (hasNum &&
				hand.at(idxNum)->getRealColor() != dangerColor) {
				idxBest = idxNum;
			} // else if (hasNum && ...)
			else if (hasRev &&
				hand.at(idxRev)->getRealColor() != dangerColor) {
				idxBest = idxRev;
			} // else if (hasRev && ...)
			else if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != dangerColor) {
				idxBest = idxSkip;
			} // else if (hasSkip && ...)
			else if (hasDraw2 &&
				hand.at(idxDraw2)->getRealColor() != dangerColor) {
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
			// Your opposite player played a wild card, started an UNO dash
			// in its last action, but fortunately the legal color has been
			// changed already. Just be careful not to re-change the legal
			// color to the dangerous color again.
			if (hasZero &&
				hand.at(idxZero)->getRealColor() != dangerColor) {
				idxBest = idxZero;
			} // if (hasZero && ...)
			else if (hasNum &&
				hand.at(idxNum)->getRealColor() != dangerColor) {
				idxBest = idxNum;
			} // else if (hasNum && ...)
			else if (hasRev &&
				prevSize >= 4 &&
				hand.at(idxRev)->getRealColor() != dangerColor) {
				idxBest = idxRev;
			} // else if (hasRev && ...)
			else if (hasSkip &&
				hand.at(idxSkip)->getRealColor() != dangerColor) {
				idxBest = idxSkip;
			} // else if (hasSkip && ...)
			else if (hasDraw2 &&
				hand.at(idxDraw2)->getRealColor() != dangerColor) {
				idxBest = idxDraw2;
			} // else if (hasDraw2 && ...)
		} // else if (dangerColor != NONE)
		else if (hasRev && prevSize - nextSize >= 3) {
			// Your opposite player started an UNO dash without playing a
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
			// not play zero cards preferentially.
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
	else if (next->getRecent() == nullptr && yourSize > 2) {
		// Strategies when your next player drew a card in its last action.
		// Unless keeping or changing to your best color, you do not need to
		// play your limitation/wild cards. Use them in more dangerous cases.
		if (hasRev && prevSize - nextSize >= 3) {
			idxBest = idxRev;
		} // if (hasRev && prevSize - nextSize >= 3)
		else if (hasZero) {
			idxBest = idxZero;
		} // else if (hasZero)
		else if (hasNum) {
			idxBest = idxNum;
		} // else if (hasNum)
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
			// Unless keeping or changing to your best color, you do not need to
			// play your limitation/wild cards when your next player still has a
			// number of cards. Use them in more dangerous cases.
			idxBest = idxSkip;
		} // else if (hasSkip && ...)
		else if (hasDraw2 &&
			hand.at(idxDraw2)->getRealColor() == bestColor) {
			idxBest = idxDraw2;
		} // else if (hasDraw2 && ...)
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
} // hardAI_bestCardIndexFor()

// E.O.F