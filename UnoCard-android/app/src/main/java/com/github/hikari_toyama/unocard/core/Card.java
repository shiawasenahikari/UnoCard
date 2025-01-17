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
     * Card name list.
     */
    static final String[] NAME = {
            "[R]0", "[R]1", "[R]2", "[R]3", "[R]4", "[R]5", "[R]6",
            "[R]7", "[R]8", "[R]9", "[R]+2", "[R]Reverse", "[R]Skip",
            "[B]0", "[B]1", "[B]2", "[B]3", "[B]4", "[B]5", "[B]6",
            "[B]7", "[B]8", "[B]9", "[B]+2", "[B]Reverse", "[B]Skip",
            "[G]0", "[G]1", "[G]2", "[G]3", "[G]4", "[G]5", "[G]6",
            "[G]7", "[G]8", "[G]9", "[G]+2", "[G]Reverse", "[G]Skip",
            "[Y]0", "[Y]1", "[Y]2", "[Y]3", "[Y]4", "[Y]5", "[Y]6",
            "[Y]7", "[Y]8", "[Y]9", "[Y]+2", "[Y]Reverse", "[Y]Skip",
            "Wild", "Wild +4"
    }; // NAME[]

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
     * Card's name, e.g. "[B]3"
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
        this.id = isWild()
                ? 39 + content.ordinal()
                : 13 * (color.ordinal() - 1) + content.ordinal();
        this.name = NAME[id];
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