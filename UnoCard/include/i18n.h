////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 PC
// Author: Hikari Toyama
// Compile Environment: Qt 5 with Qt Creator
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __I18N_H_494649FDFA62B3C015120BCB9BE17613__
#define __I18N_H_494649FDFA62B3C015120BCB9BE17613__

#include <cstdio>

typedef const char* String;

class I18N {
public:
    virtual String act_drawCard(int, String) = 0;
    virtual String act_drawCardCount(int, int) = 0;
    virtual String act_pass(int) = 0;
    virtual String act_playCard(int, String) = 0;
    virtual String act_playDraw2(int, int, int) = 0;
    virtual String act_playRev(int) = 0;
    virtual String act_playSkip(int, int) = 0;
    virtual String act_playWild(int, int) = 0;
    virtual String act_playWildDraw4(int, int) = 0;
    virtual String ask_challenge(int) = 0;
    virtual String ask_color() = 0;
    virtual String ask_keep_play() = 0;
    virtual String ask_target() = 0;
    virtual String btn_auto() = 0;
    virtual String btn_d2(bool) = 0;
    virtual String btn_d4(bool) = 0;
    virtual String btn_keep(bool) = 0;
    virtual String btn_off(bool) = 0;
    virtual String btn_on(bool) = 0;
    virtual String btn_play(bool) = 0;
    virtual String btn_settings(bool) = 0;
    virtual String info_0_rotate() = 0;
    virtual String info_7_swap(int, int) = 0;
    virtual String info_cannotDraw(int, int) = 0;
    virtual String info_cannotPlay(String) = 0;
    virtual String info_challenge(int, int, int) = 0;
    virtual String info_challengeFailure(int) = 0;
    virtual String info_challengeSuccess(int) = 0;
    virtual String info_clickAgainToPlay(String) = 0;
    virtual String info_dirChanged() = 0;
    virtual String info_gameOver(int, int) = 0;
    virtual String info_ready() = 0;
    virtual String info_ruleSettings() = 0;
    virtual String info_skipped(int) = 0;
    virtual String info_welcome() = 0;
    virtual String info_yourTurn() = 0;
    virtual String info_yourTurn_stackDraw2(int, int = 1) = 0;
    virtual String label_7_0() = 0;
    virtual String label_bgm() = 0;
    virtual String label_draw2Stack() = 0;
    virtual String label_forcePlay() = 0;
    virtual String label_initialCards() = 0;
    virtual String label_level() = 0;
    virtual String label_no() = 0;
    virtual String label_players() = 0;
    virtual String label_remain_used(int, int) = 0;
    virtual String label_score() = 0;
    virtual String label_snd() = 0;
    virtual String label_yes() = 0;
}; // I18N class

class I18N_en_US : public I18N {
private:
    char t[128];

    inline static String c(int i) {
        static String UNKNOWN = "????";
        static String S[] = {
            "NONE", "[R]RED", "[B]BLUE", "[G]GREEN", "[Y]YELLOW"
        }; // S[]
        return 0 <= i && i <= 4 ? S[i] : UNKNOWN;
    } // c(int)

    inline static String p(int i) {
        static String UNKNOWN = "????";
        static String S[] = { "YOU", "WEST", "NORTH", "EAST" };
        return 0 <= i && i <= 3 ? S[i] : UNKNOWN;
    } // p(int)

public:
    static inline I18N_en_US* getInstance() {
        static I18N_en_US instance;
        return &instance;
    } // getInstance()

    inline String act_drawCard(int i, String s) {
        sprintf(t, "%s: Draw %s", p(i), s);
        return t;
    } // act_drawCard(int, String)

    inline String act_drawCardCount(int i1, int i2) {
        i2 == 1
            ? sprintf(t, "%s: Draw a card", p(i1))
            : sprintf(t, "%s: Draw %d cards", p(i1), i2);
        return t;
    } // act_drawCardCount(int, int)

    inline String act_pass(int i) {
        sprintf(t, "%s: Pass", p(i));
        return t;
    } // act_pass(int)

    inline String act_playCard(int i, String s) {
        sprintf(t, "%s: %s", p(i), s);
        return t;
    } // act_playCard(int, String)

    inline String act_playDraw2(int i1, int i2, int i3) {
        sprintf(t, "%s: Let %s draw %d cards", p(i1), p(i2), i3);
        return t;
    } // act_playDraw2(int, int, int)

    inline String act_playRev(int i) {
        sprintf(t, "%s: Change direction", p(i));
        return t;
    } // act_playRev(int)

    inline String act_playSkip(int i1, int i2) {
        i2 == 0
            ? sprintf(t, "%s: Skip your turn", p(i1))
            : sprintf(t, "%s: Skip %s's turn", p(i1), p(i2));
        return t;
    } // act_playSkip(int, int)

    inline String act_playWild(int i1, int i2) {
        sprintf(t, "%s: Change color to %s", p(i1), c(i2));
        return t;
    } // act_playWild(int, int)

    inline String act_playWildDraw4(int i1, int i2) {
        sprintf(t, "%s: Change color & let %s draw 4", p(i1), p(i2));
        return t;
    } // act_playWildDraw4(int, int)

    inline String ask_challenge(int i) {
        sprintf(t, "^ Do you think your previous player still has %s?", c(i));
        return t;
    } // ask_challenge(int)

    inline String ask_color() {
        return "^ Specify the following legal color";
    } // ask_color()

    inline String ask_keep_play() {
        return "^ Play the drawn card?";
    } // ask_keep_play()

    inline String ask_target() {
        return "^ Specify the target to swap hand cards with";
    } // ask_target()

    inline String btn_auto() {
        return "<AUTO>";
    } // btn_auto()

    inline String btn_d2(bool active) {
        return active ? "[G]<+2>" : "<+2>";
    } // btn_d2(bool)

    inline String btn_d4(bool active) {
        return active ? "[Y]<+2 & +4>" : "<+2 & +4>";
    } // btn_d4(bool)

    inline String btn_keep(bool active) {
        return active ? "[R]<KEEP>" : "<KEEP>";
    } // btn_keep(bool)

    inline String btn_off(bool active) {
        return active ? "[R]<OFF>" : "<OFF>";
    } // btn_off(bool)

    inline String btn_on(bool active) {
        return active ? "[G]<ON>" : "<ON>";
    } // btn_on(bool)

    inline String btn_play(bool active) {
        return active ? "[G]<PLAY>" : "<PLAY>";
    } // btn_play(bool)

    inline String btn_settings(bool active) {
        return active ? "[Y]<SETTINGS>" : "<SETTINGS>";
    } // btn_settings(bool)

    inline String info_0_rotate() {
        return "Hand cards transferred to next";
    } // info_0_rotate()

    inline String info_7_swap(int i1, int i2) {
        sprintf(t, "%s swapped hand cards with %s", p(i1), p(i2));
        return t;
    } // info_7_swap(int, int)

    inline String info_cannotDraw(int i1, int i2) {
        sprintf(t, "%s cannot hold more than %d cards", p(i1), i2);
        return t;
    } // info_cannotDraw(int, int)

    inline String info_cannotPlay(String s) {
        sprintf(t, "Cannot play %s", s);
        return t;
    } // info_cannotPlay(String)

    inline String info_challenge(int i1, int i2, int i3) {
        i2 == 0
            ? sprintf(t, "%s doubted that you still have %s", p(i1), c(i3))
            : sprintf(t, "%s doubted that %s still has %s", p(i1), p(i2), c(i3));
        return t;
    } // info_challenge(int, int, int)

    inline String info_challengeFailure(int i) {
        return i == 0
            ? "Challenge failure, you draw 6 cards"
            : (sprintf(t, "Challenge failure, %s draws 6 cards", p(i)), t);
    } // info_challengeFailure(int)

    inline String info_challengeSuccess(int i) {
        return i == 0
            ? "Challenge success, you draw 4 cards"
            : (sprintf(t, "Challenge success, %s draws 4 cards", p(i)), t);
    } // info_challengeSuccess(int)

    inline String info_clickAgainToPlay(String s) {
        sprintf(t, "Click again to play %s", s);
        return t;
    } // info_clickAgainToPlay(String)

    inline String info_dirChanged() {
        return "Direction changed";
    } // info_dirChanged()

    inline String info_gameOver(int i1, int i2) {
        i2 < 0
            ? sprintf(t, "Score: %d[R](%d)[W]. Click UNO to restart", i1, i2)
            : sprintf(t, "Score: %d[G](%+d)[W]. Click UNO to restart", i1, i2);
        return t;
    } // info_gameOver(int, int)

    inline String info_ready() {
        return "GET READY";
    } // info_ready()

    inline String info_ruleSettings() {
        return "RULE SETTINGS";
    } // info_ruleSettings()

    inline String info_skipped(int i) {
        sprintf(t, "%s: Skipped", p(i));
        return t;
    } // info_skipped(int)

    inline String info_welcome() {
        return "WELCOME TO UNO CARD GAME, CLICK UNO TO START";
    } // info_welcome()

    inline String info_yourTurn() {
        return "Select a card to play, or draw a card from deck";
    } // info_yourTurn()

    inline String info_yourTurn_stackDraw2(int i1, int i2) {
        i2 == 1
            ? sprintf(t, "Stack a +2 card, or draw %d cards", i1)
            : sprintf(t, "Stack a +2/+4 card, or draw %d cards", i1);
        return t;
    } // info_yourTurn_stackDraw2(int, int)

    inline String label_7_0() {
        return "7 to swap, 0 to rotate:";
    } // label_7_0()

    inline String label_bgm() {
        return "BGM";
    } // label_bgm()

    inline String label_draw2Stack() {
        return "Stackable cards:";
    } // label_draw2Stack()

    inline String label_forcePlay() {
        return "When you draw a playable card:";
    } // label_forcePlay()

    inline String label_initialCards() {
        return "Initial cards:";
    } // label_initialCards()

    inline String label_level() {
        return "LEVEL";
    } // label_level()

    inline String label_no() {
        return "NO";
    } // label_no()

    inline String label_players() {
        return "PLAYERS";
    } // label_players()

    inline String label_remain_used(int i1, int i2) {
        sprintf(t, "Remain/Used: %d/%d", i1, i2);
        return t;
    } // label_remain_used(int, int)

    inline String label_score() {
        return "SCORE";
    } // label_score()

    inline String label_snd() {
        return "SND";
    } // label_snd()

    inline String label_yes() {
        return "YES";
    } // label_yes()
}; // I18N_en_US class

class I18N_zh_CN : public I18N {
private:
    char t[128];

    inline static String c(int i) {
        static String UNKNOWN = "????";
        static String S[] = {
            "无色", "[R]红色", "[B]蓝色", "[G]绿色", "[Y]黄色"
        }; // S[]
        return 0 <= i && i <= 4 ? S[i] : UNKNOWN;
    } // c(int)

    inline static String p(int i) {
        static String UNKNOWN = "????";
        static String S[] = { "你", "西家", "北家", "东家" };
        return 0 <= i && i <= 3 ? S[i] : UNKNOWN;
    } // p(int)

public:
    static inline I18N_zh_CN* getInstance() {
        static I18N_zh_CN instance;
        return &instance;
    } // getInstance()

    inline String act_drawCard(int i, String s) {
        sprintf(t, "%s: 摸到 %s", p(i), s);
        return t;
    } // act_drawCard(int, String)

    inline String act_drawCardCount(int i1, int i2) {
        sprintf(t, "%s: 摸 %d 张牌", p(i1), i2);
        return t;
    } // act_drawCardCount(int, int)

    inline String act_pass(int i) {
        sprintf(t, "%s: 过牌", p(i));
        return t;
    } // act_pass(int)

    inline String act_playCard(int i, String s) {
        sprintf(t, "%s: %s", p(i), s);
        return t;
    } // act_playCard(int, String)

    inline String act_playDraw2(int i1, int i2, int i3) {
        sprintf(t, "%s: 令%s摸 %d 张牌", p(i1), p(i2), i3);
        return t;
    } // act_playDraw2(int, int, int)

    inline String act_playRev(int i) {
        sprintf(t, "%s: 改变方向", p(i));
        return t;
    } // act_playRev(int)

    inline String act_playSkip(int i1, int i2) {
        sprintf(t, "%s: 跳过%s的回合", p(i1), p(i2));
        return t;
    } // act_playSkip(int, int)

    inline String act_playWild(int i1, int i2) {
        sprintf(t, "%s: 将接下来的合法颜色改为%s", p(i1), c(i2));
        return t;
    } // act_playWild(int, int)

    inline String act_playWildDraw4(int i1, int i2) {
        sprintf(t, "%s: 变色 & 令%s摸 4 张牌", p(i1), p(i2));
        return t;
    } // act_playWildDraw4(int, int)

    inline String ask_challenge(int i) {
        sprintf(t, "^ 你是否认为你的上家仍有%s牌?", c(i));
        return t;
    } // ask_challenge(int)

    inline String ask_color() {
        return "^ 指定接下来的合法颜色";
    } // ask_color()

    inline String ask_keep_play() {
        return "^ 是否打出摸到的牌?";
    } // ask_keep_play()

    inline String ask_target() {
        return "^ 指定换牌目标";
    } // ask_target()

    inline String btn_auto() {
        return "<托管>";
    } // btn_auto()

    inline String btn_d2(bool active) {
        return active ? "[G]<+2>" : "<+2>";
    } // btn_d2(bool)

    inline String btn_d4(bool active) {
        return active ? "[Y]<+2 & +4>" : "<+2 & +4>";
    } // btn_d4(bool)

    inline String btn_keep(bool active) {
        return active ? "[R]<保留>" : "<保留>";
    } // btn_keep(bool)

    inline String btn_off(bool active) {
        return active ? "[R]<无效>" : "<无效>";
    } // btn_off(bool)

    inline String btn_on(bool active) {
        return active ? "[G]<有效>" : "<有效>";
    } // btn_on(bool)

    inline String btn_play(bool active) {
        return active ? "[G]<打出>" : "<打出>";
    } // btn_play(bool)

    inline String btn_settings(bool active) {
        return active ? "[Y]<设置>" : "<设置>";
    } // btn_settings(bool)

    inline String info_0_rotate() {
        return "所有人将牌传给下家";
    } // info_0_rotate()

    inline String info_7_swap(int i1, int i2) {
        sprintf(t, "%s和%s换牌", p(i1), p(i2));
        return t;
    } // info_7_swap(int, int)

    inline String info_cannotDraw(int i1, int i2) {
        sprintf(t, "%s最多保留 %d 张牌", p(i1), i2);
        return t;
    } // info_cannotDraw(int, int)

    inline String info_cannotPlay(String s) {
        sprintf(t, "无法打出 %s", s);
        return t;
    } // info_cannotPlay(String)

    inline String info_challenge(int i1, int i2, int i3) {
        sprintf(t, "%s认为%s仍有%s牌", p(i1), p(i2), c(i3));
        return t;
    } // info_challenge(int, int, int)

    inline String info_challengeFailure(int i) {
        sprintf(t, "挑战失败, %s摸 6 张牌", p(i));
        return t;
    } // info_challengeFailure(int)

    inline String info_challengeSuccess(int i) {
        sprintf(t, "挑战成功, %s摸 4 张牌", p(i));
        return t;
    } // info_challengeSuccess(int)

    inline String info_clickAgainToPlay(String s) {
        sprintf(t, "再次点击以打出 %s", s);
        return t;
    } // info_clickAgainToPlay(String)

    inline String info_dirChanged() {
        return "方向已改变";
    } // info_dirChanged()

    inline String info_gameOver(int i1, int i2) {
        i2 < 0
            ? sprintf(t, "你的分数为 %d[G](%d)[W], 点击 UNO 重新开始游戏", i1, i2)
            : sprintf(t, "你的分数为 %d[R](%+d)[W], 点击 UNO 重新开始游戏", i1, i2);
        return t;
    } // info_gameOver(int, int)

    inline String info_ready() {
        return "准备";
    } // info_ready()

    inline String info_ruleSettings() {
        return "规则设置";
    } // info_ruleSettings()

    inline String info_skipped(int i) {
        sprintf(t, "%s: 被跳过", p(i));
        return t;
    } // info_skipped(int)

    inline String info_welcome() {
        return "欢迎来到 UNO, 点击 UNO 开始游戏";
    } // info_welcome()

    inline String info_yourTurn() {
        return "选择一张牌打出, 或从发牌堆摸一张牌";
    } // info_yourTurn()

    inline String info_yourTurn_stackDraw2(int i1, int i2) {
        i2 == 1
            ? sprintf(t, "叠加一张 +2, 或从发牌堆摸 %d 张牌", i1)
            : sprintf(t, "叠加一张 +2/+4, 或从发牌堆摸 %d 张牌", i1);
        return t;
    } // info_yourTurn_stackDraw2(int, int)

    inline String label_7_0() {
        return "7 换牌, 0 传给下家:";
    } // label_7_0()

    inline String label_bgm() {
        return "音乐";
    } // label_bgm()

    inline String label_draw2Stack() {
        return "叠牌:";
    } // label_draw2Stack()

    inline String label_forcePlay() {
        return "摸到可出的牌时, 是否打出:";
    } // label_forcePlay()

    inline String label_initialCards() {
        return "发牌张数:";
    } // label_initialCards()

    inline String label_level() {
        return "难度";
    } // label_level()

    inline String label_no() {
        return "否";
    } // label_no()

    inline String label_players() {
        return "人数";
    } // label_players()

    inline String label_remain_used(int i1, int i2) {
        sprintf(t, "剩余/已出: %d/%d", i1, i2);
        return t;
    } // label_remain_used(int, int)

    inline String label_score() {
        return "分数";
    } // label_score()

    inline String label_snd() {
        return "音效";
    } // label_snd()

    inline String label_yes() {
        return "是";
    } // label_yes()
}; // I18N_zh_CN class

#endif // __I18N_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F