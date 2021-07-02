////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
// COPYRIGHT HIKARI TOYAMA, 1992-2021. ALL RIGHTS RESERVED.
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import org.opencv.core.Mat;

/**
 * Uno Card Class.
 */
class CardImpl extends Card {
    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    CardImpl(Mat image, Mat darkImg, Color color, Content content, String name) {
        super(image, darkImg, color, content, name);
    } // CardImpl(Mat, Mat, Color, Content, String) (Class Constructor)

    /**
     * @return For non-wild cards, return card's color. For wild cards, return
     * the specified wild color by the player who played this card, or
     * Color.NONE if this card is in hand or card deck.
     */
    @Override
    public Color getRealColor() {
        return color;
    } // getRealColor()

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    @Override
    public boolean isWild() {
        return content == Content.WILD || content == Content.WILD_DRAW4;
    } // isWild()

    /**
     * @return Card's name.
     */
    @Override
    public String toString() {
        return name;
    } // toString()
} // CardImpl Class

// E.O.F