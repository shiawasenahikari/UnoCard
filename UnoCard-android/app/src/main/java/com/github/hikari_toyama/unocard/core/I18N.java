////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

public interface I18N {
    I18N EN_US = new I18N_en_US();

    I18N ZH_CN = new I18N_zh_CN();

    String act_drawCard(int i, String s);

    String act_drawCardCount(int i1, int i2);

    String act_pass(int i);

    String act_playCard(int i, String s);

    String act_playDraw2(int i1, int i2, int i3);

    String act_playRev(int i);

    String act_playSkip(int i1, int i2);

    String act_playWild(int i1, int i2);

    String act_playWildDraw4(int i1, int i2);

    String ask_challenge(int i);

    String ask_color();

    String ask_target();

    String btn_auto();

    String btn_keep(boolean active);

    String btn_off(boolean active);

    String btn_on(boolean active);

    String btn_play(boolean active);

    String btn_settings(boolean active);

    String info_0_rotate();

    String info_7_swap(int i1, int i2);

    String info_cannotDraw(int i1, int i2);

    String info_cannotPlay(String s);

    String info_challenge(int i1, int i2, int i3);

    String info_challengeFailure(int i);

    String info_challengeSuccess(int i);

    String info_clickAgainToPlay(String s);

    String info_dirChanged();

    String info_gameOver(int i1, int i2);

    String info_ready();

    String info_ruleSettings();

    String info_skipped(int i);

    String info_welcome();

    String info_yourTurn();

    String info_yourTurn_stackDraw2(int i);

    String label_7_0();

    String label_bgm();

    String label_draw2Stack();

    String label_forcePlay();

    String label_initialCards();

    String label_level();

    String label_no();

    String label_players();

    String label_remain_used(int i1, int i2);

    String label_score();

    String label_snd();

    String label_yes();
} // I18N Interface

class I18N_en_US implements I18N {
    static final String[] P = {"YOU", "WEST", "NORTH", "EAST"};
    static final String[] C = {
            "NONE", "[R]RED", "[B]BLUE", "[G]GREEN", "[Y]YELLOW"
    }; // C[]

    String c(int i) {
        return 0 <= i && i <= 4 ? C[i] : "????";
    } // c(int)

    String p(int i) {
        return 0 <= i && i <= 3 ? P[i] : "????";
    } // p(int)

    @Override
    public String act_drawCard(int i, String s) {
        return p(i) + ": Draw " + s;
    } // act_drawCard(int, String)

    @Override
    public String act_drawCardCount(int i1, int i2) {
        return i2 == 1
                ? p(i1) + ": Draw a card"
                : p(i1) + ": Draw " + i2 + " cards";
    } // act_drawCardCount(int, int)

    @Override
    public String act_pass(int i) {
        return p(i) + ": Pass";
    } // act_pass(int)

    @Override
    public String act_playCard(int i, String s) {
        return p(i) + ": " + s;
    } // act_playCard(int, String)

    @Override
    public String act_playDraw2(int i1, int i2, int i3) {
        return p(i1) + ": Let " + p(i2) + " draw " + i3 + " cards";
    } // act_playDraw2(int, int, int)

    @Override
    public String act_playRev(int i) {
        return p(i) + ": Change direction";
    } // act_playRev(int)

    @Override
    public String act_playSkip(int i1, int i2) {
        return i2 == 0
                ? p(i1) + ": Skip your turn"
                : p(i1) + ": Skip " + p(i2) + "'s turn";
    } // act_playSkip(int, int)

    @Override
    public String act_playWild(int i1, int i2) {
        return p(i1) + ": Change color to " + c(i2);
    } // act_playWild(int, int)

    @Override
    public String act_playWildDraw4(int i1, int i2) {
        return p(i1) + ": Change color & let " + p(i2) + " draw 4";
    } // act_playWildDraw4(int, int)

    @Override
    public String ask_challenge(int i) {
        return "^ Do you think your previous player still has " + c(i) + "?";
    } // ask_challenge(int)

    @Override
    public String ask_color() {
        return "^ Specify the following legal color";
    } // ask_color()

    @Override
    public String ask_target() {
        return "^ Specify the target to swap hand cards with";
    } // ask_target()

    @Override
    public String btn_auto() {
        return "<AUTO>";
    } // btn_auto()

    @Override
    public String btn_keep(boolean active) {
        return active ? "[R]<KEEP>" : "<KEEP>";
    } // btn_keep(boolean)

    @Override
    public String btn_off(boolean active) {
        return active ? "[R]<OFF>" : "<OFF>";
    } // btn_off(boolean)

    @Override
    public String btn_on(boolean active) {
        return active ? "[G]<ON>" : "<ON>";
    } // btn_on(boolean)

    @Override
    public String btn_play(boolean active) {
        return active ? "[G]<PLAY>" : "<PLAY>";
    } // btn_play(boolean)

    @Override
    public String btn_settings(boolean active) {
        return active ? "[Y]<SETTINGS>" : "<SETTINGS>";
    } // btn_settings(boolean)

    @Override
    public String info_0_rotate() {
        return "Hand cards transferred to next";
    } // info_0_rotate()

    @Override
    public String info_7_swap(int i1, int i2) {
        return p(i1) + " swapped hand cards with " + p(i2);
    } // info_7_swap(int, int)

    @Override
    public String info_cannotDraw(int i1, int i2) {
        return p(i1) + " cannot hold more than " + i2 + " cards";
    } // info_cannotDraw(int, int)

    @Override
    public String info_cannotPlay(String s) {
        return "Cannot play " + s;
    } // info_cannotPlay(String)

    @Override
    public String info_challenge(int i1, int i2, int i3) {
        return i2 == 0
                ? p(i1) + " doubted that you still have " + c(i3)
                : p(i1) + " doubted that " + p(i2) + " still has " + c(i3);
    } // info_challenge(int, int, int)

    @Override
    public String info_challengeFailure(int i) {
        return i == 0
                ? "Challenge failure, you draw 6 cards"
                : "Challenge failure, " + p(i) + " draws 6 cards";
    } // info_challengeFailure(int)

    @Override
    public String info_challengeSuccess(int i) {
        return i == 0
                ? "Challenge success, you draw 4 cards"
                : "Challenge success, " + p(i) + " draws 4 cards";
    } // info_challengeSuccess(int)

    @Override
    public String info_clickAgainToPlay(String s) {
        return "Click again to play " + s;
    } // info_clickAgainToPlay(String)

    @Override
    public String info_dirChanged() {
        return "Direction changed";
    } // info_dirChanged()

    @Override
    public String info_gameOver(int i1, int i2) {
        String s = i2 < 0 ? "[R](" + i2 + ")[W]" : "[G](+" + i2 + ")[W]";

        return "Score: " + i1 + s + ". Click UNO to restart";
    } // info_gameOver(int, int)

    @Override
    public String info_ready() {
        return "GET READY";
    } // info_ready()

    @Override
    public String info_ruleSettings() {
        return "RULE SETTINGS";
    } // info_ruleSettings()

    @Override
    public String info_skipped(int i) {
        return p(i) + ": Skipped";
    } // info_skipped(int)

    @Override
    public String info_welcome() {
        return "WELCOME TO UNO CARD GAME, CLICK UNO TO START";
    } // info_welcome()

    @Override
    public String info_yourTurn() {
        return "Select a card to play, or draw a card from deck";
    } // info_yourTurn()

    @Override
    public String info_yourTurn_stackDraw2(int i) {
        return "Stack a +2 card, or draw " + i + " cards from deck";
    } // info_yourTurn_stackDraw2(int)

    @Override
    public String label_7_0() {
        return "7 to swap, 0 to rotate:";
    } // label_7_0()

    @Override
    public String label_bgm() {
        return "BGM";
    } // label_bgm()

    @Override
    public String label_draw2Stack() {
        return "+2 can be stacked:";
    } // label_draw2Stack()

    @Override
    public String label_forcePlay() {
        return "When you draw a playable card:";
    } // label_forcePlay()

    @Override
    public String label_initialCards() {
        return "Initial cards:";
    } // label_initialCards()

    @Override
    public String label_level() {
        return "LEVEL";
    } // label_level()

    @Override
    public String label_no() {
        return "NO";
    } // label_no()

    @Override
    public String label_players() {
        return "PLAYERS";
    } // label_players()

    @Override
    public String label_remain_used(int i1, int i2) {
        return "Remain/Used: " + i1 + "/" + i2;
    } // label_remain_used(int, int)

    @Override
    public String label_score() {
        return "SCORE";
    } // label_score()

    @Override
    public String label_snd() {
        return "SND";
    } // label_snd()

    @Override
    public String label_yes() {
        return "YES";
    } // label_yes()
} // I18N_en_US Class

class I18N_zh_CN implements I18N {
    static final String[] P = {"你", "西家", "北家", "东家"};
    static final String[] C = {
            "无色", "[R]红色", "[B]蓝色", "[G]绿色", "[Y]黄色"
    }; // C[]

    String c(int i) {
        return 0 <= i && i <= 4 ? C[i] : "????";
    } // c(int)

    String p(int i) {
        return 0 <= i && i <= 3 ? P[i] : "????";
    } // p(int)

    @Override
    public String act_drawCard(int i, String s) {
        return p(i) + ": 摸到 " + s;
    } // act_drawCard(int, String)

    @Override
    public String act_drawCardCount(int i1, int i2) {
        return p(i1) + ": 摸 " + i2 + " 张牌";
    } // act_drawCardCount(int, int)

    @Override
    public String act_pass(int i) {
        return p(i) + ": 过牌";
    } // act_pass(int)

    @Override
    public String act_playCard(int i, String s) {
        return p(i) + ": " + s;
    } // act_playCard(int, String)

    @Override
    public String act_playDraw2(int i1, int i2, int i3) {
        return p(i1) + ": 令" + p(i2) + "摸 " + i3 + " 张牌";
    } // act_playDraw2(int, int, int)

    @Override
    public String act_playRev(int i) {
        return p(i) + ": 改变方向";
    } // act_playRev(int)

    @Override
    public String act_playSkip(int i1, int i2) {
        return p(i1) + ": 跳过" + p(i2) + "的回合";
    } // act_playSkip(int, int)

    @Override
    public String act_playWild(int i1, int i2) {
        return p(i1) + ": 将接下来的合法颜色改为" + c(i2);
    } // act_playWild(int, int)

    @Override
    public String act_playWildDraw4(int i1, int i2) {
        return p(i1) + ": 变色 & 令" + p(i2) + "摸 4 张牌";
    } // act_playWildDraw4(int, int)

    @Override
    public String ask_challenge(int i) {
        return "^ 你是否认为你的上家仍有" + c(i) + "牌?";
    } // ask_challenge(int)

    @Override
    public String ask_color() {
        return "^ 指定接下来的合法颜色";
    } // ask_color()

    @Override
    public String ask_target() {
        return "^ 指定换牌目标";
    } // ask_target()

    @Override
    public String btn_auto() {
        return "<托管>";
    } // btn_auto()

    @Override
    public String btn_keep(boolean active) {
        return active ? "[R]<保留>" : "<保留>";
    } // btn_keep(boolean)

    @Override
    public String btn_off(boolean active) {
        return active ? "[R]<无效>" : "<无效>";
    } // btn_off(boolean)

    @Override
    public String btn_on(boolean active) {
        return active ? "[G]<有效>" : "<有效>";
    } // btn_on(boolean)

    @Override
    public String btn_play(boolean active) {
        return active ? "[G]<打出>" : "<打出>";
    } // btn_play(boolean)

    @Override
    public String btn_settings(boolean active) {
        return active ? "[Y]<设置>" : "<设置>";
    } // btn_options(boolean)

    @Override
    public String info_0_rotate() {
        return "所有人将牌传给下家";
    } // info_0_rotate()

    @Override
    public String info_7_swap(int i1, int i2) {
        return p(i1) + "和" + p(i2) + "换牌";
    } // info_7_swap(int, int)

    @Override
    public String info_cannotDraw(int i1, int i2) {
        return p(i1) + "最多保留 " + i2 + " 张牌";
    } // info_cannotDraw(int, int)

    @Override
    public String info_cannotPlay(String s) {
        return "无法打出 " + s;
    } // info_cannotPlay(String)

    @Override
    public String info_challenge(int i1, int i2, int i3) {
        return p(i1) + "认为" + p(i2) + "仍有" + c(i3) + "牌";
    } // info_challenge(int, int, int)

    @Override
    public String info_challengeFailure(int i) {
        return "挑战失败, " + p(i) + "摸 6 张牌";
    } // info_challengeFailure(int)

    @Override
    public String info_challengeSuccess(int i) {
        return "挑战成功, " + p(i) + "摸 4 张牌";
    } // info_challengeSuccess(int)

    @Override
    public String info_clickAgainToPlay(String s) {
        return "再次点击以打出 " + s;
    } // info_clickAgainToPlay()

    @Override
    public String info_dirChanged() {
        return "方向已改变";
    } // info_dirChanged()

    @Override
    public String info_gameOver(int i1, int i2) {
        String s = i2 < 0 ? "[G](" + i2 + ")[W]" : "[R](+" + i2 + ")[W]";

        return "你的分数为 " + i1 + s + ", 点击 UNO 重新开始游戏";
    } // info_gameOver(int, int)

    @Override
    public String info_ready() {
        return "准备";
    } // info_ready()

    @Override
    public String info_ruleSettings() {
        return "规则设置";
    } // info_ruleSettings()

    @Override
    public String info_skipped(int i) {
        return p(i) + ": 被跳过";
    } // info_skipped(int)

    @Override
    public String info_welcome() {
        return "欢迎来到 UNO, 点击 UNO 开始游戏";
    } // info_welcome()

    @Override
    public String info_yourTurn() {
        return "选择一张牌打出, 或从发牌堆摸一张牌";
    } // info_yourTurn()

    @Override
    public String info_yourTurn_stackDraw2(int i) {
        return "叠加一张 +2, 或从发牌堆摸 " + i + " 张牌";
    } // info_yourTurn_stackDraw2(int)

    @Override
    public String label_7_0() {
        return "7 换牌, 0 传给下家:";
    } // label_7_0()

    @Override
    public String label_bgm() {
        return "音乐";
    } // label_bgm()

    @Override
    public String label_draw2Stack() {
        return "+2 牌可以被叠加:";
    } // label_draw2Stack()

    @Override
    public String label_forcePlay() {
        return "摸到可出的牌时, 是否打出:";
    } // label_forcePlay()

    @Override
    public String label_initialCards() {
        return "发牌张数:";
    } // label_initialCards()

    @Override
    public String label_level() {
        return "难度";
    } // label_level()

    @Override
    public String label_no() {
        return "否";
    } // label_no()

    @Override
    public String label_players() {
        return "人数";
    } // label_players()

    @Override
    public String label_remain_used(int i1, int i2) {
        return "剩余/已出: " + i1 + "/" + i2;
    } // label_remain_used(int, int)

    @Override
    public String label_score() {
        return "分数";
    } // label_score()

    @Override
    public String label_snd() {
        return "音效";
    } // label_snd()

    @Override
    public String label_yes() {
        return "是";
    } // label_yes()
} // I18N_zh_CN Class

// E.O.F