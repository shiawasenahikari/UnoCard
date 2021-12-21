////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

class I18N_zh_CN implements I18N {
    static final String[] C = {"无色", "红色", "蓝色", "绿色", "黄色"};
    static final String[] P = {"你", "西家", "北家", "东家"};

    @Override
    public String c(int i) {
        return 0 <= i && i <= 4 ? C[i] : "????";
    } // c(int)

    @Override
    public String p(int i) {
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
    public String btn_keep() {
        return "<保留>";
    } // btn_keep()

    @Override
    public String btn_off() {
        return "<无效>";
    } // btn_off()

    @Override
    public String btn_on() {
        return "<有效>";
    } // btn_on()

    @Override
    public String btn_play() {
        return "<打出>";
    } // btn_play()

    @Override
    public String btn_settings() {
        return "<设置>";
    } // btn_options()

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
    public String info_gameOver(int i) {
        return "你的分数为 " + i + ", 点击发牌堆重新开始游戏";
    } // info_gameOver(int)

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