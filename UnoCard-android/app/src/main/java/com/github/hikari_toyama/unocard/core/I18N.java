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

    String c(int i);

    String p(int i);

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

    String btn_keep();

    String btn_off();

    String btn_on();

    String btn_play();

    String btn_settings();

    String info_0_rotate();

    String info_7_swap(int i1, int i2);

    String info_cannotDraw(int i1, int i2);

    String info_cannotPlay(String s);

    String info_challenge(int i1, int i2, int i3);

    String info_challengeFailure(int i);

    String info_challengeSuccess(int i);

    String info_clickAgainToPlay();

    String info_dirChanged();

    String info_gameOver(int i);

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

    String label_level();

    String label_no();

    String label_players();

    String label_remain_used(int i1, int i2);

    String label_score();

    String label_snd();

    String label_yes();
}; // I18N Interface

// E.O.F