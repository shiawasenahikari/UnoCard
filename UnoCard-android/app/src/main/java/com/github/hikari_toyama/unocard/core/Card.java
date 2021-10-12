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
public abstract class Card {
    /**
     * Card's image resource ID, e.g. Imgcodecs.imread
     * ("/sdcard/Android/data/com.github.hikari_toyama/files/front_b3.png")
     */
    public final Mat image;

    /**
     * Card's dark image resource, e.g. Imgcodecs.imread
     * ("/sdcard/Android/data/com.github.hikari_toyama/files/dark_b3.png")
     */
    public final Mat darkImg;

    /**
     * Card's name, e.g. "Blue 3"
     */
    public final String name;

    /**
     * Card's color, e.g. Color.BLUE
     */
    public final Color color;

    /**
     * Card's content, e.g. Content.NUM3
     */
    public final Content content;

    /**
     * Card's order. Used in the hand card sorting procedure.
     * Everyone's hand cards will be sorted based on the order sequence.
     */
    int order;

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    Card(Mat image, Mat darkImg, Color color, Content content, String name) {
        if (image == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // if (image == null)

        if (darkImg == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // if (darkImg == null)

        if (color == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // if (color == null)

        if (content == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // if (content == null)

        if (name == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // if (name == null)

        this.name = name;
        this.image = image;
        this.color = color;
        this.content = content;
        this.darkImg = darkImg;
        this.order = color.ordinal() << 8 | content.ordinal();
    } // Card(Mat, Mat, Color, Content, String) (Class Constructor)

    /**
     * @return Card's color.
     * @deprecated Please access this.color directly.
     */
    @Deprecated
    public abstract Color getRealColor();

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    public abstract boolean isWild();
} // Card Abstract Class

// E.O.F