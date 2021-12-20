////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

class I18N_en_US implements I18N {
    static final String[] C = {"NONE", "RED", "BLUE", "GREEN", "YELLOW"};
    static final String[] P = {"YOU", "WEST", "NORTH", "EAST"};

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
    public String btn_keep() {
        return "<KEEP>";
    } // btn_keep()

    @Override
    public String btn_off() {
        return "<OFF>";
    } // btn_off()

    @Override
    public String btn_on() {
        return "<ON>";
    } // btn_on()

    @Override
    public String btn_play() {
        return "<PLAY>";
    } // btn_play()

    @Override
    public String btn_settings() {
        return "<SETTINGS>";
    } // btn_settings()

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
    public String info_clickAgainToPlay() {
        return "Click again to play";
    } // info_clickAgainToPlay()

    @Override
    public String info_dirChanged() {
        return "Direction changed";
    } // info_dirChanged()

    @Override
    public String info_gameOver(int i) {
        return "Your score is " + i + ". Click the card deck to restart";
    } // info_gameOver(int)

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

// E.O.F