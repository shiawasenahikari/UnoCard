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
    [[deprecated]] virtual String btn_2vs2(bool) = 0;
    [[deprecated]] virtual String btn_3p(bool) = 0;
    [[deprecated]] virtual String btn_4p(bool) = 0;
    [[deprecated]] virtual String btn_7_0(bool) = 0;
    virtual String btn_ask(bool) = 0;
    virtual String btn_auto() = 0;
    [[deprecated]] virtual String btn_d2(bool) = 0;
    [[deprecated]] virtual String btn_d4(bool) = 0;
    virtual String btn_keep(bool) = 0;
    virtual String btn_load() = 0;
    [[deprecated]] virtual String btn_none(bool) = 0;
    virtual String btn_play(bool) = 0;
    virtual String btn_save() = 0;
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
    virtual String info_save(String) = 0;
    virtual String info_skipped(int) = 0;
    virtual String info_welcome() = 0;
    virtual String info_yourTurn() = 0;
    virtual String info_yourTurn_stackDraw2(int, int = 1) = 0;
    virtual String label_bgm() = 0;
    [[deprecated]] virtual String label_draw2Stack() = 0;
    virtual String label_forcePlay() = 0;
    [[deprecated]] virtual String label_gameMode() = 0;
    virtual String label_gameMode(int) = 0;
    [[deprecated]] virtual String label_initialCards() = 0;
    virtual String label_initialCards(int) = 0;
    virtual String label_lacks(int, int, int, int) = 0;
    virtual String label_leftArrow() = 0;
    [[deprecated]] virtual String label_level() = 0;
    virtual String label_level(int) = 0;
    virtual String label_no() = 0;
    virtual String label_remain_used(int, int) = 0;
    virtual String label_rightArrow() = 0;
    virtual String label_score() = 0;
    virtual String label_snd() = 0;
    virtual String label_speed() = 0;
    virtual String label_stackRule(int) = 0;
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

    [[deprecated]]
    inline String btn_2vs2(bool active) {
        return active ? "[G]<2vs2>" : "<2vs2>";
    } // btn_2vs2(bool)

    [[deprecated]]
    inline String btn_3p(bool active) {
        return active ? "[R]<3P>" : "<3P>";
    } // btn_3p(bool)

    [[deprecated]]
    inline String btn_4p(bool active) {
        return active ? "[Y]<4P>" : "<4P>";
    } // btn_4p(bool)

    [[deprecated]]
    inline String btn_7_0(bool active) {
        return active ? "[B]<7-0>" : "<7-0>";
    } // btn_7_0(bool)

    inline String btn_ask(bool active) {
        return active ? "[Y]<ASK>" : "<ASK>";
    } // btn_ask(bool)

    inline String btn_auto() {
        return "<AUTO>";
    } // btn_auto()

    [[deprecated]]
    inline String btn_d2(bool active) {
        return active ? "[Y]<+2>" : "<+2>";
    } // btn_d2(bool)

    [[deprecated]]
    inline String btn_d4(bool active) {
        return active ? "[G]<+2+4>" : "<+2+4>";
    } // btn_d4(bool)

    inline String btn_keep(bool active) {
        return active ? "[R]<KEEP>" : "<KEEP>";
    } // btn_keep(bool)

    inline String btn_load() {
        return "[G]<LOAD>";
    } // btn_load()

    [[deprecated]]
    inline String btn_none(bool active) {
        return active ? "[R]<NONE>" : "<NONE>";
    } // btn_none(bool)

    inline String btn_play(bool active) {
        return active ? "[G]<PLAY>" : "<PLAY>";
    } // btn_play(bool)

    inline String btn_save() {
        return "[B]<SAVE>";
    } // btn_save()

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

    inline String info_save(String s) {
        return s == nullptr || s[0] == '\0'
            ? "Failed to save your game replay"
            : (sprintf(t, "Replay file saved as %s", s), t);
    } // info_save(String)

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

    inline String label_bgm() {
        return "BGM";
    } // label_bgm()

    [[deprecated]]
    inline String label_draw2Stack() {
        return "Stackable cards:";
    } // label_draw2Stack()

    inline String label_forcePlay() {
        return "When you draw a playable card:";
    } // label_forcePlay()

    [[deprecated]]
    inline String label_gameMode() {
        return "How to play:";
    } // label_gameMode()

    inline String label_gameMode(int i) {
        return i == 1
            ? "How to play:  7-0"
            : i == 2
            ? "How to play: 2vs2"
            : i == 3
            ? "How to play:   3P"
            : "How to play:   4P";
    } // label_gameMode(int)

    [[deprecated]]
    inline String label_initialCards() {
        return "Initial cards:";
    } // label_initialCards()

    inline String label_initialCards(int i) {
        sprintf(t, "Initial cards: %02d", i);
        return t;
    } // label_initialCards(int)

    inline String label_lacks(int n, int e, int w, int s) {
        String mark = "WRBGY";
        sprintf(t, "LACK:[%c]N[%c]E[%c]W[%c]S",
            (0 <= n && n <= 4 ? mark[n] : 'W'),
            (0 <= e && e <= 4 ? mark[e] : 'W'),
            (0 <= w && w <= 4 ? mark[w] : 'W'),
            (0 <= s && s <= 4 ? mark[s] : 'W'));
        return t;
    } // label_lacks(int, int, int, int)

    inline String label_leftArrow() {
        return "[Y]＜－";
    } // label_leftArrow()

    [[deprecated]]
    inline String label_level() {
        return "LEVEL";
    } // label_level()

    inline String label_level(int i) {
        return i == 0 ? "Level: EASY" : "Level: HARD";
    } // label_level(int)

    inline String label_no() {
        return "NO";
    } // label_no()

    inline String label_remain_used(int i1, int i2) {
        sprintf(t, "[Y]R%d[W]/[G]U%d", i1, i2);
        return t;
    } // label_remain_used(int, int)

    inline String label_rightArrow() {
        return "[Y]＋＞";
    } // label_rightArrow()

    inline String label_score() {
        return "SCORE";
    } // label_score()

    inline String label_snd() {
        return "SND";
    } // label_snd()

    inline String label_speed() {
        return "SPEED";
    } // label_speed()

    inline String label_stackRule(int i) {
        return i == 0
            ? "Stackable cards: NONE"
            : i == 1
            ? "Stackable cards:   +2"
            : "Stackable cards: +2+4";
    } // label_stackRule(int)

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

    [[deprecated]]
    inline String btn_2vs2(bool active) {
        return active ? "[G]<2vs2>" : "<2vs2>";
    } // btn_2vs2(bool)

    [[deprecated]]
    inline String btn_3p(bool active) {
        return active ? "[R]<3P>" : "<3P>";
    } // btn_3p(bool)

    [[deprecated]]
    inline String btn_4p(bool active) {
        return active ? "[Y]<4P>" : "<4P>";
    } // btn_4p(bool)

    [[deprecated]]
    inline String btn_7_0(bool active) {
        return active ? "[B]<7-0>" : "<7-0>";
    } // btn_7_0(bool)

    inline String btn_ask(bool active) {
        return active ? "[Y]<可选>" : "<可选>";
    } // btn_ask(bool)

    inline String btn_auto() {
        return "<托管>";
    } // btn_auto()

    [[deprecated]]
    inline String btn_d2(bool active) {
        return active ? "[Y]<+2>" : "<+2>";
    } // btn_d2(bool)

    [[deprecated]]
    inline String btn_d4(bool active) {
        return active ? "[G]<+2+4>" : "<+2+4>";
    } // btn_d4(bool)

    inline String btn_keep(bool active) {
        return active ? "[R]<保留>" : "<保留>";
    } // btn_keep(bool)

    inline String btn_load() {
        return "[G]<读取>";
    } // btn_load()

    [[deprecated]]
    inline String btn_none(bool active) {
        return active ? "[R]<无效>" : "<无效>";
    } // btn_none(bool)

    inline String btn_play(bool active) {
        return active ? "[G]<打出>" : "<打出>";
    } // btn_play(bool)

    inline String btn_save() {
        return "[B]<保存>";
    } // btn_save()

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

    inline String info_save(String s) {
        return s == nullptr || s[0] == '\0'
            ? "回放文件保存失败"
            : (sprintf(t, "回放文件已保存为 %s", s), t);
    } // info_save(String)

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

    inline String label_bgm() {
        return "音乐";
    } // label_bgm()

    [[deprecated]]
    inline String label_draw2Stack() {
        return "叠牌:";
    } // label_draw2Stack()

    inline String label_forcePlay() {
        return "摸到可出的牌时, 是否打出:";
    } // label_forcePlay()

    [[deprecated]]
    inline String label_gameMode() {
        return "玩法:";
    } // label_gameMode()

    inline String label_gameMode(int i) {
        return i == 1
            ? "玩法:  7-0"
            : i == 2
            ? "玩法: 2vs2"
            : i == 3
            ? "玩法:   3P"
            : "玩法:   4P";
    } // label_gameMode(int)

    [[deprecated]]
    inline String label_initialCards() {
        return "发牌张数:";
    } // label_initialCards()

    inline String label_initialCards(int i) {
        sprintf(t, "发牌张数: %02d", i);
        return t;
    } // label_initialCards(int)

    inline String label_lacks(int n, int e, int w, int s) {
        String mark = "WRBGY";
        sprintf(t, "缺色:[%c]N[%c]E[%c]W[%c]S",
            (0 <= n && n <= 4 ? mark[n] : 'W'),
            (0 <= e && e <= 4 ? mark[e] : 'W'),
            (0 <= w && w <= 4 ? mark[w] : 'W'),
            (0 <= s && s <= 4 ? mark[s] : 'W'));
        return t;
    } // label_lacks(int, int, int, int)

    inline String label_leftArrow() {
        return "[Y]＜－";
    } // label_leftArrow()

    [[deprecated]]
    inline String label_level() {
        return "难度";
    } // label_level()

    inline String label_level(int i) {
        return i == 0 ? "难易度: 简单" : "难易度: 困难";
    } // label_level(int)

    inline String label_no() {
        return "否";
    } // label_no()

    inline String label_remain_used(int i1, int i2) {
        sprintf(t, "[Y]剩%d[G]用%d", i1, i2);
        return t;
    } // label_remain_used(int, int)

    inline String label_rightArrow() {
        return "[Y]＋＞";
    } // label_rightArrow()

    inline String label_score() {
        return "分数";
    } // label_score()

    inline String label_snd() {
        return "音效";
    } // label_snd()

    inline String label_speed() {
        return "速度";
    } // label_speed()

    inline String label_stackRule(int i) {
        return i == 0
            ? "允许叠牌: 　　　无"
            : i == 1
            ? "允许叠牌: 只有＋２"
            : "允许叠牌: ＋２＋４";
    } // label_stackRule(int)

    inline String label_yes() {
        return "是";
    } // label_yes()
}; // I18N_zh_CN class

class I18N_ja_JP : public I18N {
private:
    char t[128];

    inline static String c(int i) {
        static String UNKNOWN = "????";
        static String S[] = {
            "無色", "[R]赤色", "[B]青色", "[G]緑色", "[Y]黄色"
        }; // S[]
        return 0 <= i && i <= 4 ? S[i] : UNKNOWN;
    } // c(int)

    inline static String p(int i) {
        static String UNKNOWN = "????";
        static String S[] = { "あなた", "西", "北", "東" };
        return 0 <= i && i <= 3 ? S[i] : UNKNOWN;
    } // p(int)

public:
    static inline I18N_ja_JP* getInstance() {
        static I18N_ja_JP instance;
        return &instance;
    } // getInstance()

    inline String act_drawCard(int i, String s) {
        sprintf(t, "%s: %s[W] を引く", p(i), s);
        return t;
    } // act_drawCard(int, String)

    inline String act_drawCardCount(int i1, int i2) {
        sprintf(t, "%s: 手札を %d 枚引く", p(i1), i2);
        return t;
    } // act_drawCardCount(int, int)

    inline String act_pass(int i) {
        sprintf(t, "%s: パス", p(i));
        return t;
    } // act_pass(int)

    inline String act_playCard(int i, String s) {
        sprintf(t, "%s: %s", p(i), s);
        return t;
    } // act_playCard(int, String)

    inline String act_playDraw2(int i1, int i2, int i3) {
        sprintf(t, "%s: %sに手札を %d 枚引かせる", p(i1), p(i2), i3);
        return t;
    } // act_playDraw2(int, int, int)

    inline String act_playRev(int i) {
        sprintf(t, "%s: 方向を変える", p(i));
        return t;
    } // act_playRev(int)

    inline String act_playSkip(int i1, int i2) {
        sprintf(t, "%s: %sの番をスキップ", p(i1), p(i2));
        return t;
    } // act_playSkip(int, int)

    inline String act_playWild(int i1, int i2) {
        sprintf(t, "%s: 次の色を%s[W]に変える", p(i1), c(i2));
        return t;
    } // act_playWild(int, int)

    inline String act_playWildDraw4(int i1, int i2) {
        sprintf(t, "%s: 色を変更 & %sに手札を 4 枚引かせる", p(i1), p(i2));
        return t;
    } // act_playWildDraw4(int, int)

    inline String ask_challenge(int i) {
        sprintf(t, "^ 前の方はまだ%sの手札[W]を持っていると思いますか?", c(i));
        return t;
    } // ask_challenge(int)

    inline String ask_color() {
        return "^ 次の色を指定してください";
    } // ask_color()

    inline String ask_keep_play() {
        return "^ 引いたカードすぐを出しますか?";
    } // ask_keep_play()

    inline String ask_target() {
        return "^ 手札を交換する相手を指定してください";
    } // ask_target()

    [[deprecated]]
    inline String btn_2vs2(bool active) {
        return active ? "[G]<2vs2>" : "<2vs2>";
    } // btn_2vs2(bool)

    [[deprecated]]
    inline String btn_3p(bool active) {
        return active ? "[R]<3P>" : "<3P>";
    } // btn_3p(bool)

    [[deprecated]]
    inline String btn_4p(bool active) {
        return active ? "[Y]<4P>" : "<4P>";
    } // btn_4p(bool)

    [[deprecated]]
    inline String btn_7_0(bool active) {
        return active ? "[B]<7-0>" : "<7-0>";
    } // btn_7_0(bool)

    inline String btn_ask(bool active) {
        return active ? "[Y]<任意>" : "<任意>";
    } // btn_ask(bool)

    inline String btn_auto() {
        return "<オート>";
    } // btn_auto()

    [[deprecated]]
    inline String btn_d2(bool active) {
        return active ? "[Y]<+2>" : "<+2>";
    } // btn_d2(bool)

    [[deprecated]]
    inline String btn_d4(bool active) {
        return active ? "[G]<+2+4>" : "<+2+4>";
    } // btn_d4(bool)

    inline String btn_keep(bool active) {
        return active ? "[R]<保留>" : "<保留>";
    } // btn_keep(bool)

    inline String btn_load() {
        return "[G]<読取>";
    } // btn_load()

    [[deprecated]]
    inline String btn_none(bool active) {
        return active ? "[R]<無効>" : "<無効>";
    } // btn_none(bool)

    inline String btn_play(bool active) {
        return active ? "[G]<出す>" : "<出す>";
    } // btn_play(bool)

    inline String btn_save() {
        return "[B]<保存>";
    } // btn_save()

    inline String btn_settings(bool active) {
        return active ? "[Y]<設定>" : "<設定>";
    } // btn_settings(bool)

    inline String info_0_rotate() {
        return "手札を次の方に転送しました";
    } // info_0_rotate()

    inline String info_7_swap(int i1, int i2) {
        sprintf(t, "%sは%sと手札を交換しました", p(i1), p(i2));
        return t;
    } // info_7_swap(int, int)

    inline String info_cannotDraw(int i1, int i2) {
        sprintf(t, "%sは手札を %d 枚以上持てません", p(i1), i2);
        return t;
    } // info_cannotDraw(int, int)

    inline String info_cannotPlay(String s) {
        sprintf(t, "%s[W] を出せません", s);
        return t;
    } // info_cannotPlay(String)

    inline String info_challenge(int i1, int i2, int i3) {
        sprintf(t, "%sは%sが%sの手札[W]を持っていると思う", p(i1), p(i2), c(i3));
        return t;
    } // info_challenge(int, int, int)

    inline String info_challengeFailure(int i) {
        sprintf(t, "チャレンジ失敗、%sは手札を 6 枚引く", p(i));
        return t;
    } // info_challengeFailure(int)

    inline String info_challengeSuccess(int i) {
        sprintf(t, "チャレンジ成功、%sは手札を 4 枚引く", p(i));
        return t;
    } // info_challengeSuccess(int)

    inline String info_clickAgainToPlay(String s) {
        sprintf(t, "もう一度クリックして %s[W] を出す", s);
        return t;
    } // info_clickAgainToPlay(String)

    inline String info_dirChanged() {
        return "方向が変わりました";
    } // info_dirChanged()

    inline String info_gameOver(int i1, int i2) {
        i2 < 0
            ? sprintf(t, "スコア: %d[R](%d)[W]. UNO をクリックして再開", i1, i2)
            : sprintf(t, "スコア: %d[G](%+d)[W]. UNO をクリックして再開", i1, i2);
        return t;
    } // info_gameOver(int, int)

    inline String info_ready() {
        return "準備完了";
    } // info_ready()

    inline String info_ruleSettings() {
        return "ルール設定";
    } // info_ruleSettings()

    inline String info_save(String s) {
        return s == nullptr || s[0] == '\0'
            ? "リプレイファイルは保存できませんでした"
            : (sprintf(t, "リプレイファイルは %s として保存しました", s), t);
    } // info_save(String)

    inline String info_skipped(int i) {
        sprintf(t, "%sの番はスキップされました", p(i));
        return t;
    } // info_skipped(int)

    inline String info_welcome() {
        return "UNO へようこそ! UNO をクリックしてゲームスタート";
    } // info_welcome()

    inline String info_yourTurn() {
        return "手札を一枚出すか、デッキから手札を一枚引く";
    } // info_yourTurn()

    inline String info_yourTurn_stackDraw2(int i1, int i2) {
        i2 == 1
            ? sprintf(t, "+2 を一枚重ねるか、デッキから手札を %d 枚引く", i1)
            : sprintf(t, "+2/+4 を一枚重ねるか、デッキから手札を %d 枚引く", i1);
        return t;
    } // info_yourTurn_stackDraw2(int, int)

    inline String label_bgm() {
        return "音楽";
    } // label_bgm()

    [[deprecated]]
    inline String label_draw2Stack() {
        return "積み重ね可能の手札:";
    } // label_draw2Stack()

    inline String label_forcePlay() {
        return "出せる手札を引いた時:";
    } // label_forcePlay()

    [[deprecated]]
    inline String label_gameMode() {
        return "遊び方:";
    } // label_gameMode()

    inline String label_gameMode(int i) {
        return i == 1
            ? "遊び方:  7-0"
            : i == 2
            ? "遊び方: 2vs2"
            : i == 3
            ? "遊び方:   3P"
            : "遊び方:   4P";
    } // label_gameMode(int)

    [[deprecated]]
    inline String label_initialCards() {
        return "最初の手札数:";
    } // label_initialCards()

    inline String label_initialCards(int i) {
        sprintf(t, "最初の手札数: %02d", i);
        return t;
    } // label_initialCards(int)

    inline String label_lacks(int n, int e, int w, int s) {
        String mark = "WRBGY";
        sprintf(t, "欠色:[%c]N[%c]E[%c]W[%c]S",
            (0 <= n && n <= 4 ? mark[n] : 'W'),
            (0 <= e && e <= 4 ? mark[e] : 'W'),
            (0 <= w && w <= 4 ? mark[w] : 'W'),
            (0 <= s && s <= 4 ? mark[s] : 'W'));
        return t;
    } // label_lacks(int, int, int, int)

    inline String label_leftArrow() {
        return "[Y]＜－";
    } // label_leftArrow()

    [[deprecated]]
    inline String label_level() {
        return "難易度";
    } // label_level()

    inline String label_level(int i) {
        return i == 0 ? "難易度: 　簡単" : "難易度: 難しい";
    } // label_level(int)

    inline String label_no() {
        return "いいえ";
    } // label_no()

    inline String label_remain_used(int i1, int i2) {
        sprintf(t, "[Y]残%d[G]使%d", i1, i2);
        return t;
    } // label_remain_used(int, int)

    inline String label_rightArrow() {
        return "[Y]＋＞";
    } // label_rightArrow()

    inline String label_score() {
        return "スコア";
    } // label_score()

    inline String label_snd() {
        return "音声";
    } // label_snd()

    inline String label_speed() {
        return "速さ";
    } // label_speed()

    inline String label_stackRule(int i) {
        return i == 0
            ? "積み重ね可: 　　なし"
            : i == 1
            ? "積み重ね可: ＋２のみ"
            : "積み重ね可: ＋２＋４";
    } // label_stackRule(int)

    inline String label_yes() {
        return "はい";
    } // label_yes()
}; // I18N_ja_JP class

#endif // __I18N_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F