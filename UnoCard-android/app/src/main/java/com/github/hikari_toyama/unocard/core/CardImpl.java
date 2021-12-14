////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game 4 Droid
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
    CardImpl(Mat image, Mat darkImg, Color color, Content content) {
        super(image, darkImg, color, content);
    } // CardImpl(Mat, Mat, Color, Content) (Class Constructor)

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    @Override
    public boolean isWild() {
        return color == Color.NONE;
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