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

    I18N JA_JP = new I18N_ja_JP();

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

    String ask_keep_play();

    String ask_target();

    @Deprecated
    String btn_2vs2(boolean active);

    @Deprecated
    String btn_3p(boolean active);

    @Deprecated
    String btn_4p(boolean active);

    @Deprecated
    String btn_7_0(boolean active);

    String btn_ask(boolean active);

    String btn_auto();

    @Deprecated
    String btn_d2(boolean active);

    @Deprecated
    String btn_d4(boolean active);

    String btn_keep(boolean active);

    String btn_load();

    @Deprecated
    String btn_none(boolean active);

    String btn_play(boolean active);

    String btn_save();

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

    String info_save(String s);

    String info_skipped(int i);

    String info_welcome();

    String info_yourTurn();

    default String info_yourTurn_stackDraw2(int i) {
        return info_yourTurn_stackDraw2(i, 1);
    } // info_yourTurn_stackDraw2(int)

    String info_yourTurn_stackDraw2(int i1, int i2);

    String label_bgm();

    @Deprecated
    String label_draw2Stack();

    String label_forcePlay();

    @Deprecated
    String label_gameMode();

    String label_gameMode(int i);

    @Deprecated
    String label_initialCards();

    String label_initialCards(int i);

    String label_lacks(int n, int e, int w, int s);

    String label_leftArrow();

    @Deprecated
    String label_level();

    String label_level(int i);

    String label_no();

    String label_remain_used(int i1, int i2);

    String label_rightArrow();

    String label_score();

    String label_snd();

    String label_speed();

    String label_stackRule(int i);

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
    public String ask_keep_play() {
        return "^ Play the drawn card?";
    } // ask_keep_play()

    @Override
    public String ask_target() {
        return "^ Specify the target to swap hand cards with";
    } // ask_target()

    @Override
    @Deprecated
    public String btn_2vs2(boolean active) {
        return active ? "[G]<2vs2>" : "<2vs2>";
    } // btn_2vs2(boolean)

    @Override
    @Deprecated
    public String btn_3p(boolean active) {
        return active ? "[R]<3P>" : "<3P>";
    } // btn_3p(boolean)

    @Override
    @Deprecated
    public String btn_4p(boolean active) {
        return active ? "[Y]<4P>" : "<4P>";
    } // btn_4p(boolean)

    @Override
    @Deprecated
    public String btn_7_0(boolean active) {
        return active ? "[B]<7-0>" : "<7-0>";
    } // btn_7_0(boolean)

    @Override
    public String btn_ask(boolean active) {
        return active ? "[Y]<ASK>" : "<ASK>";
    } // btn_ask(boolean)

    @Override
    public String btn_auto() {
        return "<AUTO>";
    } // btn_auto()

    @Override
    @Deprecated
    public String btn_d2(boolean active) {
        return active ? "[Y]<+2>" : "<+2>";
    } // btn_d2(boolean)

    @Override
    @Deprecated
    public String btn_d4(boolean active) {
        return active ? "[G]<+2+4>" : "<+2+4>";
    } // btn_d4(boolean)

    @Override
    public String btn_keep(boolean active) {
        return active ? "[R]<KEEP>" : "<KEEP>";
    } // btn_keep(boolean)

    @Override
    public String btn_load() {
        return "[G]<LOAD>";
    } // btn_load()

    @Override
    @Deprecated
    public String btn_none(boolean active) {
        return active ? "[R]<NONE>" : "<NONE>";
    } // btn_none(boolean)

    @Override
    public String btn_play(boolean active) {
        return active ? "[G]<PLAY>" : "<PLAY>";
    } // btn_play(boolean)

    @Override
    public String btn_save() {
        return "[B]<SAVE>";
    } // btn_save()

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
    public String info_save(String s) {
        return s == null || s.isEmpty()
                ? "Failed to save your game replay"
                : "Replay file saved as " + s;
    } // info_save(String)

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
    public String info_yourTurn_stackDraw2(int i1, int i2) {
        return i2 == 1
                ? "Stack a +2 card, or draw " + i1 + " cards"
                : "Stack a +2/+4 card, or draw " + i1 + " cards";
    } // info_yourTurn_stackDraw2(int, int)

    @Override
    public String label_bgm() {
        return "BGM";
    } // label_bgm()

    @Override
    @Deprecated
    public String label_draw2Stack() {
        return "Stackable cards:";
    } // label_draw2Stack()

    @Override
    public String label_forcePlay() {
        return "When you draw a playable card:";
    } // label_forcePlay()

    @Override
    @Deprecated
    public String label_gameMode() {
        return "How to play:";
    } // label_gameMode()

    @Override
    public String label_gameMode(int i) {
        return i == 1
                ? "How to play:  7-0"
                : i == 2
                ? "How to play: 2vs2"
                : i == 3
                ? "How to play:   3P"
                : "How to play:   4P";
    } // label_gameMode(int)

    @Override
    @Deprecated
    public String label_initialCards() {
        return "Initial cards:";
    } // label_initialCards()

    @Override
    public String label_initialCards(int i) {
        return "Initial cards: " + i / 10 + i % 10;
    } // label_initialCards(int)

    @Override
    public String label_lacks(int n, int e, int w, int s) {
        return "LACK:[" +
                (0 <= n && n <= 4 ? "WRBGY".charAt(n) : 'W') + "]N[" +
                (0 <= e && e <= 4 ? "WRBGY".charAt(e) : 'W') + "]E[" +
                (0 <= w && w <= 4 ? "WRBGY".charAt(w) : 'W') + "]W[" +
                (0 <= s && s <= 4 ? "WRBGY".charAt(s) : 'W') + "]S";
    } // label_lacks(int, int, int, int)

    @Override
    public String label_leftArrow() {
        return "[Y]＜－";
    } // label_leftArrow()

    @Override
    @Deprecated
    public String label_level() {
        return "LEVEL";
    } // label_level()

    @Override
    public String label_level(int i) {
        return i == 0 ? "Level: EASY" : "Level: HARD";
    } // label_level(int)

    @Override
    public String label_no() {
        return "NO";
    } // label_no()

    @Override
    public String label_remain_used(int i1, int i2) {
        return "[Y]R" + i1 + "[W]/[G]U" + i2;
    } // label_remain_used(int, int)

    @Override
    public String label_rightArrow() {
        return "[Y]＋＞";
    } // label_rightArrow()

    @Override
    public String label_score() {
        return "SCORE";
    } // label_score()

    @Override
    public String label_snd() {
        return "SND";
    } // label_snd()

    @Override
    public String label_speed() {
        return "SPEED";
    } // label_speed()

    @Override
    public String label_stackRule(int i) {
        return i == 0
                ? "Stackable cards: NONE"
                : i == 1
                ? "Stackable cards:   +2"
                : "Stackable cards: +2+4";
    } // label_stackRule(int)

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
    public String ask_keep_play() {
        return "^ 是否打出摸到的牌?";
    } // ask_keep_play()

    @Override
    public String ask_target() {
        return "^ 指定换牌目标";
    } // ask_target()

    @Override
    @Deprecated
    public String btn_2vs2(boolean active) {
        return active ? "[G]<2vs2>" : "<2vs2>";
    } // btn_2vs2(boolean)

    @Override
    @Deprecated
    public String btn_3p(boolean active) {
        return active ? "[R]<3P>" : "<3P>";
    } // btn_3p(boolean)

    @Override
    @Deprecated
    public String btn_4p(boolean active) {
        return active ? "[Y]<4P>" : "<4P>";
    } // btn_4p(boolean)

    @Override
    @Deprecated
    public String btn_7_0(boolean active) {
        return active ? "[B]<7-0>" : "<7-0>";
    } // btn_7_0(boolean)

    @Override
    public String btn_ask(boolean active) {
        return active ? "[Y]<可选>" : "<可选>";
    } // btn_ask(boolean)

    @Override
    public String btn_auto() {
        return "<托管>";
    } // btn_auto()

    @Override
    @Deprecated
    public String btn_d2(boolean active) {
        return active ? "[Y]<+2>" : "<+2>";
    } // btn_d2(boolean)

    @Override
    @Deprecated
    public String btn_d4(boolean active) {
        return active ? "[G]<+2+4>" : "<+2+4>";
    } // btn_d4(boolean)

    @Override
    public String btn_keep(boolean active) {
        return active ? "[R]<保留>" : "<保留>";
    } // btn_keep(boolean)

    @Override
    public String btn_load() {
        return "[G]<读取>";
    } // btn_load()

    @Override
    @Deprecated
    public String btn_none(boolean active) {
        return active ? "[R]<无效>" : "<无效>";
    } // btn_none(boolean)

    @Override
    public String btn_play(boolean active) {
        return active ? "[G]<打出>" : "<打出>";
    } // btn_play(boolean)

    @Override
    public String btn_save() {
        return "[B]<保存>";
    } // btn_save()

    @Override
    public String btn_settings(boolean active) {
        return active ? "[Y]<设置>" : "<设置>";
    } // btn_settings(boolean)

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
    } // info_clickAgainToPlay(String)

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
    public String info_save(String s) {
        return s == null || s.isEmpty()
                ? "回放文件保存失败"
                : "回放文件已保存为 " + s;
    } // info_save(String)

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
    public String info_yourTurn_stackDraw2(int i1, int i2) {
        return i2 == 1
                ? "叠加一张 +2, 或从发牌堆摸 " + i1 + " 张牌"
                : "叠加一张 +2/+4, 或从发牌堆摸 " + i1 + " 张牌";
    } // info_yourTurn_stackDraw2(int, int)

    @Override
    public String label_bgm() {
        return "音乐";
    } // label_bgm()

    @Override
    @Deprecated
    public String label_draw2Stack() {
        return "叠牌:";
    } // label_draw2Stack()

    @Override
    public String label_forcePlay() {
        return "摸到可出的牌时, 是否打出:";
    } // label_forcePlay()

    @Override
    @Deprecated
    public String label_gameMode() {
        return "玩法:";
    } // label_gameMode()

    @Override
    public String label_gameMode(int i) {
        return i == 1
                ? "玩法:  7-0"
                : i == 2
                ? "玩法: 2vs2"
                : i == 3
                ? "玩法:   3P"
                : "玩法:   4P";
    } // label_gameMode(int)

    @Override
    @Deprecated
    public String label_initialCards() {
        return "发牌张数:";
    } // label_initialCards()

    @Override
    public String label_initialCards(int i) {
        return "发牌张数: " + i / 10 + i % 10;
    } // label_initialCards(int)

    @Override
    public String label_lacks(int n, int e, int w, int s) {
        return "缺色:[" +
                (0 <= n && n <= 4 ? "WRBGY".charAt(n) : 'W') + "]N[" +
                (0 <= e && e <= 4 ? "WRBGY".charAt(e) : 'W') + "]E[" +
                (0 <= w && w <= 4 ? "WRBGY".charAt(w) : 'W') + "]W[" +
                (0 <= s && s <= 4 ? "WRBGY".charAt(s) : 'W') + "]S";
    } // label_lacks(int, int, int, int)

    @Override
    public String label_leftArrow() {
        return "[Y]＜－";
    } // label_leftArrow()

    @Override
    @Deprecated
    public String label_level() {
        return "难度";
    } // label_level()

    @Override
    public String label_level(int i) {
        return i == 0 ? "难度: 简单" : "难度: 困难";
    } // label_level(int)

    @Override
    public String label_no() {
        return "否";
    } // label_no()

    @Override
    public String label_remain_used(int i1, int i2) {
        return "[Y]剩" + i1 + "[G]用" + i2;
    } // label_remain_used(int, int)

    @Override
    public String label_rightArrow() {
        return "[Y]＋＞";
    } // label_rightArrow()

    @Override
    public String label_score() {
        return "分数";
    } // label_score()

    @Override
    public String label_snd() {
        return "音效";
    } // label_snd()

    @Override
    public String label_speed() {
        return "速度";
    } // label_speed()

    @Override
    public String label_stackRule(int i) {
        return i == 0
                ? "叠牌: 无效"
                : i == 1
                ? "叠牌: 仅+2"
                : "叠牌: +2+4";
    } // label_stackRule(int)

    @Override
    public String label_yes() {
        return "是";
    } // label_yes()
} // I18N_zh_CN Class

class I18N_ja_JP implements I18N {
    static final String[] P = {"あなた", "西", "北", "東"};
    static final String[] C = {
            "無色", "[R]赤色", "[B]青色", "[G]緑色", "[Y]黄色"
    }; // C[]

    String c(int i) {
        return 0 <= i && i <= 4 ? C[i] : "????";
    } // c(int)

    String p(int i) {
        return 0 <= i && i <= 3 ? P[i] : "????";
    } // p(int)

    @Override
    public String act_drawCard(int i, String s) {
        return p(i) + ": " + s + "[W] を引く";
    } // act_drawCard(int, String)

    @Override
    public String act_drawCardCount(int i1, int i2) {
        return p(i1) + ": 手札を " + i2 + " 枚引く";
    } // act_drawCardCount(int, int)

    @Override
    public String act_pass(int i) {
        return p(i) + ": パス";
    } // act_pass(int)

    @Override
    public String act_playCard(int i, String s) {
        return p(i) + ": " + s;
    } // act_playCard(int, String)

    @Override
    public String act_playDraw2(int i1, int i2, int i3) {
        return p(i1) + ": " + p(i2) + "に手札を " + i3 + " 枚引かせる";
    } // act_playDraw2(int, int, int)

    @Override
    public String act_playRev(int i) {
        return p(i) + ": 方向を変える";
    } // act_playRev(int)

    @Override
    public String act_playSkip(int i1, int i2) {
        return p(i1) + ": " + p(i2) + "の番をスキップ";
    } // act_playSkip(int, int)

    @Override
    public String act_playWild(int i1, int i2) {
        return p(i1) + ": 次の色を" + c(i2) + "[W]に変える";
    } // act_playWild(int, int)

    @Override
    public String act_playWildDraw4(int i1, int i2) {
        return p(i1) + ": 色を変更 & " + p(i2) + "に手札を 4 枚引かせる";
    } // act_playWildDraw4(int, int)

    @Override
    public String ask_challenge(int i) {
        return "^ 前の方はまだ" + c(i) + "の手札[W]を持っていると思いますか?";
    } // ask_challenge(int)

    @Override
    public String ask_color() {
        return "^ 次の色を指定してください";
    } // ask_color()

    @Override
    public String ask_keep_play() {
        return "^ 引いたカードすぐを出しますか?";
    } // ask_keep_play()

    @Override
    public String ask_target() {
        return "^ 手札を交換する相手を指定してください";
    } // ask_target()

    @Override
    @Deprecated
    public String btn_2vs2(boolean active) {
        return active ? "[G]<2vs2>" : "<2vs2>";
    } // btn_2vs2(boolean)

    @Override
    @Deprecated
    public String btn_3p(boolean active) {
        return active ? "[R]<3P>" : "<3P>";
    } // btn_3p(boolean)

    @Override
    @Deprecated
    public String btn_4p(boolean active) {
        return active ? "[Y]<4P>" : "<4P>";
    } // btn_4p(boolean)

    @Override
    @Deprecated
    public String btn_7_0(boolean active) {
        return active ? "[B]<7-0>" : "<7-0>";
    } // btn_7_0(boolean)

    @Override
    public String btn_ask(boolean active) {
        return active ? "[Y]<任意>" : "<任意>";
    } // btn_ask(boolean)

    @Override
    public String btn_auto() {
        return "<オート>";
    } // btn_auto()

    @Override
    @Deprecated
    public String btn_d2(boolean active) {
        return active ? "[Y]<+2>" : "<+2>";
    } // btn_d2(boolean)

    @Override
    @Deprecated
    public String btn_d4(boolean active) {
        return active ? "[G]<+2+4>" : "<+2+4>";
    } // btn_d4(boolean)

    @Override
    public String btn_keep(boolean active) {
        return active ? "[R]<保留>" : "<保留>";
    } // btn_keep(boolean)

    @Override
    public String btn_load() {
        return "[G]<読取>";
    } // btn_load()

    @Override
    @Deprecated
    public String btn_none(boolean active) {
        return active ? "[R]<無効>" : "<無効>";
    } // btn_none(boolean)

    @Override
    public String btn_play(boolean active) {
        return active ? "[G]<出す>" : "<出す>";
    } // btn_play(boolean)

    @Override
    public String btn_save() {
        return "[B]<保存>";
    } // btn_save()

    @Override
    public String btn_settings(boolean active) {
        return active ? "[Y]<設定>" : "<設定>";
    } // btn_settings(boolean)

    @Override
    public String info_0_rotate() {
        return "手札を次の方に転送しました";
    } // info_0_rotate()

    @Override
    public String info_7_swap(int i1, int i2) {
        return p(i1) + "は" + p(i2) + "と手札を交換しました";
    } // info_7_swap(int, int)

    @Override
    public String info_cannotDraw(int i1, int i2) {
        return p(i1) + "は手札を " + i2 + " 枚以上持てません";
    } // info_cannotDraw(int, int)

    @Override
    public String info_cannotPlay(String s) {
        return s + "[W] を出せません";
    } // info_cannotPlay(String)

    @Override
    public String info_challenge(int i1, int i2, int i3) {
        return p(i1) + "は" + p(i2) + "が" + c(i3) + "の手札[W]を持っていると思う";
    } // info_challenge(int, int, int)

    @Override
    public String info_challengeFailure(int i) {
        return "チャレンジ失敗、" + p(i) + "は手札を 6 枚引く";
    } // info_challengeFailure(int)

    @Override
    public String info_challengeSuccess(int i) {
        return "チャレンジ成功、" + p(i) + "は手札を 4 枚引く";
    } // info_challengeSuccess(int)

    @Override
    public String info_clickAgainToPlay(String s) {
        return "もう一度クリックして " + s + "[W] を出す";
    } // info_clickAgainToPlay(String)

    @Override
    public String info_dirChanged() {
        return "方向が変わりました";
    } // info_dirChanged()

    @Override
    public String info_gameOver(int i1, int i2) {
        return i2 < 0
                ? "スコア: " + i1 + "[R](" + i2 + ")[W]. UNO をクリックして再開"
                : "スコア: " + i1 + "[G](+" + i2 + ")[W]. UNO をクリックして再開";
    } // info_gameOver(int, int)

    @Override
    public String info_ready() {
        return "準備完了";
    } // info_ready()

    @Override
    public String info_ruleSettings() {
        return "ルール設定";
    } // info_ruleSettings()

    @Override
    public String info_save(String s) {
        return s == null || s.isEmpty()
                ? "リプレイファイルは保存できませんでした"
                : "リプレイファイルは " + s + " として保存しました";
    } // info_save(String)

    @Override
    public String info_skipped(int i) {
        return p(i) + "の番はスキップされました";
    } // info_skipped(int)

    @Override
    public String info_welcome() {
        return "UNO へようこそ! UNO をクリックしてゲームスタート";
    } // info_welcome()

    @Override
    public String info_yourTurn() {
        return "手札を一枚出すか、デッキから手札を一枚引く";
    } // info_yourTurn()

    @Override
    public String info_yourTurn_stackDraw2(int i1, int i2) {
        return i2 == 1
                ? "+2 を一枚重ねるか、デッキから手札を " + i1 + " 枚引く"
                : "+2/+4 を一枚重ねるか、デッキから手札を " + i1 + " 枚引く";
    } // info_yourTurn_stackDraw2(int, int)

    @Override
    public String label_bgm() {
        return "音楽";
    } // label_bgm()

    @Override
    @Deprecated
    public String label_draw2Stack() {
        return "積み重ね可能の手札:";
    } // label_draw2Stack()

    @Override
    public String label_forcePlay() {
        return "出せる手札を引いた時:";
    } // label_forcePlay()

    @Override
    @Deprecated
    public String label_gameMode() {
        return "遊び方:";
    } // label_gameMode()

    @Override
    public String label_gameMode(int i) {
        return i == 1
                ? "遊び方:  7-0"
                : i == 2
                ? "遊び方: 2vs2"
                : i == 3
                ? "遊び方:   3P"
                : "遊び方:   4P";
    } // label_gameMode(int)

    @Override
    @Deprecated
    public String label_initialCards() {
        return "最初の手札数:";
    } // label_initialCards()

    @Override
    public String label_initialCards(int i) {
        return "最初の手札数: " + i / 10 + i % 10;
    } // label_initialCards(int)

    @Override
    public String label_lacks(int n, int e, int w, int s) {
        return "欠色:[" +
                (0 <= n && n <= 4 ? "WRBGY".charAt(n) : 'W') + "]N[" +
                (0 <= e && e <= 4 ? "WRBGY".charAt(e) : 'W') + "]E[" +
                (0 <= w && w <= 4 ? "WRBGY".charAt(w) : 'W') + "]W[" +
                (0 <= s && s <= 4 ? "WRBGY".charAt(s) : 'W') + "]S";
    } // label_lacks(int, int, int, int)

    @Override
    public String label_leftArrow() {
        return "[Y]＜－";
    } // label_leftArrow()

    @Override
    @Deprecated
    public String label_level() {
        return "難易度";
    } // label_level()

    @Override
    public String label_level(int i) {
        return i == 0 ? "難易度: 簡　単" : "難易度：難しい";
    } // label_level(int)

    @Override
    public String label_no() {
        return "いいえ";
    } // label_no()

    @Override
    public String label_remain_used(int i1, int i2) {
        return "[Y]残" + i1 + "[G]使" + i2;
    } // label_remain_used(int, int)

    @Override
    public String label_rightArrow() {
        return "[Y]＋＞";
    } // label_rightArrow()

    @Override
    public String label_score() {
        return "スコア";
    } // label_score()

    @Override
    public String label_snd() {
        return "音声";
    } // label_snd()

    @Override
    public String label_speed() {
        return "速さ";
    } // label_speed()

    @Override
    public String label_stackRule(int i) {
        return i == 0
                ? "積み重ね可能の手札:なし"
                : i == 1
                ? "積み重ね可能の手札:  +2"
                : "積み重ね可能の手札:+2+4";
    } // label_stackRule(int)

    @Override
    public String label_yes() {
        return "はい";
    } // label_yes()
} // I18N_ja_JP Class

// E.O.F