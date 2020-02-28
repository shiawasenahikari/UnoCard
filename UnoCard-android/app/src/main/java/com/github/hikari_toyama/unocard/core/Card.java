////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import org.opencv.core.Mat;

import static com.github.hikari_toyama.unocard.core.Content.WILD;
import static com.github.hikari_toyama.unocard.core.Content.WILD_DRAW4;

/**
 * Uno Card Class.
 */
@SuppressWarnings("ALL")
public class Card implements Comparable<Card> {
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
     * Card's content, e.g. Content.NUM3
     */
    public final Content content;

    /**
     * Card's order. Used in the hand card sorting procedure.
     * Everyone's hand cards will be sorted based on the order sequence.
     */
    final int order;

    /**
     * Card's color, e.g. Color.BLUE
     */
    Color color;

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
     * @return Card's image resource.
     * @deprecated Please directly access this field (this.image).
     */
    @Deprecated
    public Mat getImage() {
        return image;
    } // getImage()

    /**
     * @return Card's dark image resource.
     * @deprecated Please directly access this field (this.darkImg).
     */
    @Deprecated
    public Mat getDarkImg() {
        return darkImg;
    } // getDarkImg()

    /**
     * @return Card's name.
     * @deprecated Please directly access this field (this.getName).
     */
    @Deprecated
    public String getName() {
        return name;
    } // getName()

    /**
     * @return For non-wild cards, return card's color. For wild cards,
     * return the specified wild color by the player who played this
     * card, or Color.NONE if this card is in hand or card deck.
     */
    public Color getRealColor() {
        return color;
    } // getRealColor()

    /**
     * @return Card's content.
     * @deprecated Please directly access this field (this.content).
     */
    @Deprecated
    public Content getContent() {
        return content;
    } // getContent()

    /**
     * @return Whether the card is a [wild] or [wild +4].
     */
    public boolean isWild() {
        return content == WILD || content == WILD_DRAW4;
    } // isWild()

    /**
     * @return Card's name.
     */
    @Override
    public String toString() {
        return name;
    } // toString()

    /**
     * @return Card's order.
     */
    @Override
    public int hashCode() {
        return order;
    } // hashCode()

    /**
     * Whether this card has the same color & content to another card.
     *
     * @param another Compare to which Card instance.
     * @return true if the two cards are same, or false if not.
     */
    @Override
    public boolean equals(Object another) {
        boolean result;

        if (this == another) {
            result = true;
        } // if (this == another)
        else if (another instanceof Card) {
            result = this.order == ((Card) another).order;
        } // else if (another instanceof Card)
        else {
            result = false;
        } // else

        return result;
    } // equals()

    /**
     * Used in the hand card sorting procedure. The "larger" the card is,
     * the righter the card will be hold in hand.
     *
     * @param another Compare to which Card instance.
     * @return 0 if the two cards are same,
     * or a positive number if current card is "larger",
     * or a negative number if current card is "smaller".
     */
    @Override
    public int compareTo(Card another) {
        return this.order - another.order;
    } // compareTo()
} // Card Class

// E.O.F