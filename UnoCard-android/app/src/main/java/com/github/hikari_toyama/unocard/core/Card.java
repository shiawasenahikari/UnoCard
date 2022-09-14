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
public class Card implements Comparable<Card> {
    /**
     * Color part of name string.
     */
    static final String[] A = {"", "Red ", "Blue ", "Green ", "Yellow "};

    /**
     * Content part of name string.
     */
    static final String[] B = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "Reverse", "Skip", "+2", "Wild", "Wild +4"
    }; // B[]

    /**
     * Card's color, e.g. Color.BLUE
     */
    public final Color color;

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
     * Card's content, e.g. Content.NUM3
     */
    public final Content content;

    /**
     * Card's name, e.g. "Blue 3"
     */
    public final String name;

    /**
     * Card's ID, has the value of 39 + content.ordinal() (for a wild card) or
     * 13 * (color.ordinal() - 1) + content.ordinal() (for a non-wild card)
     */
    final int id;

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    Card(Mat image, Mat darkImg, Color color, Content content) {
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

        this.color = color;
        this.image = image;
        this.darkImg = darkImg;
        this.content = content;
        this.name = A[color.ordinal()] + B[content.ordinal()];
        this.id = isWild()
                ? 39 + content.ordinal()
                : 13 * (color.ordinal() - 1) + content.ordinal();
    } // Card(Mat, Mat, Color, Content) (Class Constructor)

    /**
     * Compare the id number of two cards.
     *
     * @param that Provide another Card instance.
     * @return 0 if this.id == that.id, or a positive number if
     * this.id > that.id, or a negative number if this.id < that.id.
     */
    @Override
    public int compareTo(Card that) {
        return this.id - that.id;
    } // compareTo(Card)

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    public boolean isWild() {
        return color == Color.NONE;
    } // isWild()
} // Card Class

// E.O.F