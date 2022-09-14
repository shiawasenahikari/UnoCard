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

#include <QString>

class I18N {
public:
    virtual QString act_drawCard(int, const QString&) = 0;
    virtual QString act_drawCardCount(int, int) = 0;
    virtual QString act_pass(int) = 0;
    virtual QString act_playCard(int, const QString&) = 0;
    virtual QString act_playDraw2(int, int, int) = 0;
    virtual QString act_playRev(int) = 0;
    virtual QString act_playSkip(int, int) = 0;
    virtual QString act_playWild(int, int) = 0;
    virtual QString act_playWildDraw4(int, int) = 0;
    virtual QString ask_challenge(int) = 0;
    virtual QString ask_color() = 0;
    virtual QString ask_target() = 0;
    virtual QString btn_auto() = 0;
    virtual QString btn_keep() = 0;
    virtual QString btn_off() = 0;
    virtual QString btn_on() = 0;
    virtual QString btn_play() = 0;
    virtual QString btn_settings() = 0;
    virtual QString info_0_rotate() = 0;
    virtual QString info_7_swap(int, int) = 0;
    virtual QString info_cannotDraw(int, int) = 0;
    virtual QString info_cannotPlay(const QString&) = 0;
    virtual QString info_challenge(int, int, int) = 0;
    virtual QString info_challengeFailure(int) = 0;
    virtual QString info_challengeSuccess(int) = 0;
    virtual QString info_clickAgainToPlay(const QString&) = 0;
    virtual QString info_dirChanged() = 0;
    virtual QString info_gameOver(int) = 0;
    virtual QString info_ready() = 0;
    virtual QString info_ruleSettings() = 0;
    virtual QString info_skipped(int) = 0;
    virtual QString info_welcome() = 0;
    virtual QString info_yourTurn() = 0;
    virtual QString info_yourTurn_stackDraw2(int) = 0;
    virtual QString label_7_0() = 0;
    virtual QString label_bgm() = 0;
    virtual QString label_draw2Stack() = 0;
    virtual QString label_forcePlay() = 0;
    virtual QString label_level() = 0;
    virtual QString label_no() = 0;
    virtual QString label_players() = 0;
    virtual QString label_remain_used(int, int) = 0;
    virtual QString label_score() = 0;
    virtual QString label_snd() = 0;
    virtual QString label_yes() = 0;
}; // I18N class

class I18N_en_US : public I18N {
private:
    inline static const QString& c(int i) {
        static const QString UNKNOWN = "????";
        static const QString S[] = { "NONE", "RED", "BLUE", "GREEN", "YELLOW" };
        return 0 <= i && i <= 4 ? S[i] : UNKNOWN;
    } // c(int)

    inline static const QString& p(int i) {
        static const QString UNKNOWN = "????";
        static const QString S[] = { "YOU", "WEST", "NORTH", "EAST" };
        return 0 <= i && i <= 3 ? S[i] : UNKNOWN;
    } // p(int)

public:
    static inline I18N_en_US* getInstance() {
        static I18N_en_US instance;
        return &instance;
    } // getInstance()

    inline QString act_drawCard(int i, const QString& s) {
        return p(i) + ": Draw " + s;
    } // act_drawCard(int, const QString&)

    inline QString act_drawCardCount(int i1, int i2) {
        return i2 == 1
            ? p(i1) + ": Draw a card"
            : p(i1) + ": Draw " + QString::number(i2) + " cards";
    } // act_drawCardCount(int, int)

    inline QString act_pass(int i) {
        return p(i) + ": Pass";
    } // act_pass(int)

    inline QString act_playCard(int i, const QString& s) {
        return p(i) + ": " + s;
    } // act_playCard(int, const QString&)

    inline QString act_playDraw2(int i1, int i2, int i3) {
        return p(i1) + ": Let " + p(i2) + " draw "
            + QString::number(i3) + " cards";
    } // act_playDraw2(int, int, int)

    inline QString act_playRev(int i) {
        return p(i) + ": Change direction";
    } // act_playRev(int)

    inline QString act_playSkip(int i1, int i2) {
        return i2 == 0
            ? p(i1) + ": Skip your turn"
            : p(i1) + ": Skip " + p(i2) + "'s turn";
    } // act_playSkip(int, int)

    inline QString act_playWild(int i1, int i2) {
        return p(i1) + ": Change color to " + c(i2);
    } // act_playWild(int, int)

    inline QString act_playWildDraw4(int i1, int i2) {
        return p(i1) + ": Change color & let " + p(i2) + " draw 4";
    } // act_playWildDraw4(int, int)

    inline QString ask_challenge(int i) {
        return "^ Do you think your previous player still has " + c(i) + "?";
    } // ask_challenge(int)

    inline QString ask_color() {
        return "^ Specify the following legal color";
    } // ask_color()

    inline QString ask_target() {
        return "^ Specify the target to swap hand cards with";
    } // ask_target()

    inline QString btn_auto() {
        return "<AUTO>";
    } // btn_auto()

    inline QString btn_keep() {
        return "<KEEP>";
    } // btn_keep()

    inline QString btn_off() {
        return "<OFF>";
    } // btn_off()

    inline QString btn_on() {
        return "<ON>";
    } // btn_on()

    inline QString btn_play() {
        return "<PLAY>";
    } // btn_play()

    inline QString btn_settings() {
        return "<SETTINGS>";
    } // btn_settings()

    inline QString info_0_rotate() {
        return "Hand cards transferred to next";
    } // info_0_rotate()

    inline QString info_7_swap(int i1, int i2) {
        return p(i1) + " swapped hand cards with " + p(i2);
    } // info_7_swap(int, int)

    inline QString info_cannotDraw(int i1, int i2) {
        return p(i1) + " cannot hold more than "
            + QString::number(i2) + " cards";
    } // info_cannotDraw(int, int)

    inline QString info_cannotPlay(const QString& s) {
        return "Cannot play " + s;
    } // info_cannotPlay(const QString&)

    inline QString info_challenge(int i1, int i2, int i3) {
        return i2 == 0
            ? p(i1) + " doubted that you still have " + c(i3)
            : p(i1) + " doubted that " + p(i2) + " still has " + c(i3);
    } // info_challenge(int, int, int)

    inline QString info_challengeFailure(int i) {
        return i == 0
            ? "Challenge failure, you draw 6 cards"
            : "Challenge failure, " + p(i) + " draws 6 cards";
    } // info_challengeFailure(int)

    inline QString info_challengeSuccess(int i) {
        return i == 0
            ? "Challenge success, you draw 4 cards"
            : "Challenge success, " + p(i) + " draws 4 cards";
    } // info_challengeSuccess(int)

    inline QString info_clickAgainToPlay(const QString& s) {
        return "Click again to play " + s;
    } // info_clickAgainToPlay(const QString&)

    inline QString info_dirChanged() {
        return "Direction changed";
    } // info_dirChanged()

    inline QString info_gameOver(int i) {
        return "Your score is " + QString::number(i)
            + ". Click the card deck to restart";
    } // info_gameOver(int)

    inline QString info_ready() {
        return "GET READY";
    } // info_ready()

    inline QString info_ruleSettings() {
        return "RULE SETTINGS";
    } // info_ruleSettings()

    inline QString info_skipped(int i) {
        return p(i) + ": Skipped";
    } // info_skipped(int)

    inline QString info_welcome() {
        return "WELCOME TO UNO CARD GAME, CLICK UNO TO START";
    } // info_welcome()

    inline QString info_yourTurn() {
        return "Select a card to play, or draw a card from deck";
    } // info_yourTurn()

    inline QString info_yourTurn_stackDraw2(int i) {
        return "Stack a +2 card, or draw "
            + QString::number(i) + " cards from deck";
    } // info_yourTurn_stackDraw2(int)

    inline QString label_7_0() {
        return "7 to swap, 0 to rotate:";
    } // label_7_0()

    inline QString label_bgm() {
        return "BGM";
    } // label_bgm()

    inline QString label_draw2Stack() {
        return "+2 can be stacked:";
    } // label_draw2Stack()

    inline QString label_forcePlay() {
        return "When you draw a playable card:";
    } // label_forcePlay()

    inline QString label_level() {
        return "LEVEL";
    } // label_level()

    inline QString label_no() {
        return "NO";
    } // label_no()

    inline QString label_players() {
        return "PLAYERS";
    } // label_players()

    inline QString label_remain_used(int i1, int i2) {
        return "Remain/Used: " + QString::number(i1)
            + "/" + QString::number(i2);
    } // label_remain_used(int, int)

    inline QString label_score() {
        return "SCORE";
    } // label_score()

    inline QString label_snd() {
        return "SND";
    } // label_snd()

    inline QString label_yes() {
        return "YES";
    } // label_yes()
}; // I18N_en_US class

class I18N_zh_CN : public I18N {
private:
    inline static const QString& c(int i) {
        static const QString UNKNOWN = "????";
        static const QString S[] = { "无色", "红色", "蓝色", "绿色", "黄色" };
        return 0 <= i && i <= 4 ? S[i] : UNKNOWN;
    } // c(int)

    inline static const QString& p(int i) {
        static const QString UNKNOWN = "????";
        static const QString S[] = { "你", "西家", "北家", "东家" };
        return 0 <= i && i <= 3 ? S[i] : UNKNOWN;
    } // p(int)

public:
    static inline I18N_zh_CN* getInstance() {
        static I18N_zh_CN instance;
        return &instance;
    } // getInstance()

    inline QString act_drawCard(int i, const QString& s) {
        return p(i) + ": 摸到 " + s;
    } // act_drawCard(int, const QString&)

    inline QString act_drawCardCount(int i1, int i2) {
        return p(i1) + ": 摸 " + QString::number(i2) + " 张牌";
    } // act_drawCardCount(int, int)

    inline QString act_pass(int i) {
        return p(i) + ": 过牌";
    } // act_pass(int)

    inline QString act_playCard(int i, const QString& s) {
        return p(i) + ": " + s;
    } // act_playCard(int, const QString&)

    inline QString act_playDraw2(int i1, int i2, int i3) {
        return p(i1) + ": 令" + p(i2) + "摸 " + QString::number(i3) + " 张牌";
    } // act_playDraw2(int, int, int)

    inline QString act_playRev(int i) {
        return p(i) + ": 改变方向";
    } // act_playRev(int)

    inline QString act_playSkip(int i1, int i2) {
        return p(i1) + ": 跳过" + p(i2) + "的回合";
    } // act_playSkip(int, int)

    inline QString act_playWild(int i1, int i2) {
        return p(i1) + ": 将接下来的合法颜色改为" + c(i2);
    } // act_playWild(int, int)

    inline QString act_playWildDraw4(int i1, int i2) {
        return p(i1) + ": 变色 & 令" + p(i2) + "摸 4 张牌";
    } // act_playWildDraw4(int, int)

    inline QString ask_challenge(int i) {
        return "^ 你是否认为你的上家仍有" + c(i) + "牌?";
    } // ask_challenge(int)

    inline QString ask_color() {
        return "^ 指定接下来的合法颜色";
    } // ask_color()

    inline QString ask_target() {
        return "^ 指定换牌目标";
    } // ask_target()

    inline QString btn_auto() {
        return "<托管>";
    } // btn_auto()

    inline QString btn_keep() {
        return "<保留>";
    } // btn_keep()

    inline QString btn_off() {
        return "<无效>";
    } // btn_off()

    inline QString btn_on() {
        return "<有效>";
    } // btn_on()

    inline QString btn_play() {
        return "<打出>";
    } // btn_play()

    inline QString btn_settings() {
        return "<设置>";
    } // btn_options()

    inline QString info_0_rotate() {
        return "所有人将牌传给下家";
    } // info_0_rotate()

    inline QString info_7_swap(int i1, int i2) {
        return p(i1) + "和" + p(i2) + "换牌";
    } // info_7_swap(int, int)

    inline QString info_cannotDraw(int i1, int i2) {
        return p(i1) + "最多保留 " + QString::number(i2) + " 张牌";
    } // info_cannotDraw(int, int)

    inline QString info_cannotPlay(const QString& s) {
        return "无法打出 " + s;
    } // info_cannotPlay(const QString&)

    inline QString info_challenge(int i1, int i2, int i3) {
        return p(i1) + "认为" + p(i2) + "仍有" + c(i3) + "牌";
    } // info_challenge(int, int, int)

    inline QString info_challengeFailure(int i) {
        return "挑战失败, " + p(i) + "摸 6 张牌";
    } // info_challengeFailure(int)

    inline QString info_challengeSuccess(int i) {
        return "挑战成功, " + p(i) + "摸 4 张牌";
    } // info_challengeSuccess(int)

    inline QString info_clickAgainToPlay(const QString& s) {
        return "再次点击以打出 " + s;
    } // info_clickAgainToPlay(const QString&)

    inline QString info_dirChanged() {
        return "方向已改变";
    } // info_dirChanged()

    inline QString info_gameOver(int i) {
        return "你的分数为 " + QString::number(i) + ", 点击发牌堆重新开始游戏";
    } // info_gameOver(int)

    inline QString info_ready() {
        return "准备";
    } // info_ready()

    inline QString info_ruleSettings() {
        return "规则设置";
    } // info_ruleSettings()

    inline QString info_skipped(int i) {
        return p(i) + ": 被跳过";
    } // info_skipped(int)

    inline QString info_welcome() {
        return "欢迎来到 UNO, 点击 UNO 开始游戏";
    } // info_welcome()

    inline QString info_yourTurn() {
        return "选择一张牌打出, 或从发牌堆摸一张牌";
    } // info_yourTurn()

    inline QString info_yourTurn_stackDraw2(int i) {
        return "叠加一张 +2, 或从发牌堆摸 " + QString::number(i) + " 张牌";
    } // info_yourTurn_stackDraw2(int)

    inline QString label_7_0() {
        return "7 换牌, 0 传给下家:";
    } // label_7_0()

    inline QString label_bgm() {
        return "音乐";
    } // label_bgm()

    inline QString label_draw2Stack() {
        return "+2 牌可以被叠加:";
    } // label_draw2Stack()

    inline QString label_forcePlay() {
        return "摸到可出的牌时, 是否打出:";
    } // label_forcePlay()

    inline QString label_level() {
        return "难度";
    } // label_level()

    inline QString label_no() {
        return "否";
    } // label_no()

    inline QString label_players() {
        return "人数";
    } // label_players()

    inline QString label_remain_used(int i1, int i2) {
        return "剩余/已出: " + QString::number(i1)
            + "/" + QString::number(i2);
    } // label_remain_used(int, int)

    inline QString label_score() {
        return "分数";
    } // label_score()

    inline QString label_snd() {
        return "音效";
    } // label_snd()

    inline QString label_yes() {
        return "是";
    } // label_yes()
}; // I18N_zh_CN class

#endif // __I18N_H_494649FDFA62B3C015120BCB9BE17613__

// E.O.F