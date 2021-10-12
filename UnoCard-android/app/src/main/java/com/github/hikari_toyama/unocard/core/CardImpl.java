////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio Arctic Fox, with Android SDK 30
// COPYRIGHT HIKARI TOYAMA, 1992-2022. ALL RIGHTS RESERVED.
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
     * @return Card's color.
     * @deprecated Please access this.color directly.
     */
    @Override
    @Deprecated
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