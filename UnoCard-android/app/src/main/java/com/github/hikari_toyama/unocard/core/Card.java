////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard.core;

import org.opencv.core.Mat;

import static com.github.hikari_toyama.unocard.core.Color.BLACK;
import static com.github.hikari_toyama.unocard.core.Content.DRAW2;
import static com.github.hikari_toyama.unocard.core.Content.NUM0;
import static com.github.hikari_toyama.unocard.core.Content.NUM1;
import static com.github.hikari_toyama.unocard.core.Content.NUM2;
import static com.github.hikari_toyama.unocard.core.Content.NUM3;
import static com.github.hikari_toyama.unocard.core.Content.NUM4;
import static com.github.hikari_toyama.unocard.core.Content.NUM5;
import static com.github.hikari_toyama.unocard.core.Content.NUM6;
import static com.github.hikari_toyama.unocard.core.Content.NUM7;
import static com.github.hikari_toyama.unocard.core.Content.NUM8;
import static com.github.hikari_toyama.unocard.core.Content.NUM9;
import static com.github.hikari_toyama.unocard.core.Content.REV;
import static com.github.hikari_toyama.unocard.core.Content.SKIP;
import static com.github.hikari_toyama.unocard.core.Content.WILD;
import static com.github.hikari_toyama.unocard.core.Content.WILD_DRAW4;

/**
 * Uno Card Class.
 */
@SuppressWarnings("ALL")
public class Card implements Comparable<Card> {
    /**
     * Card's color, e.g. Color.BLUE
     */
    Color color;

    /**
     * Card's wild color. Valid only when this is a wild card.
     */
    Color wildColor;

    /**
     * Card's content, e.g. Content.NUM3
     */
    Content content;

    /**
     * Card's order. Used in the hand card sorting procedure.
     * Everyone's hand cards will be sorted based on the order sequence.
     */
    private int order;

    /**
     * Card's image resource ID, e.g. Imgcodecs.imread
     * ("/sdcard/Android/data/com.github.hikari_toyama/files/front_b3.png")
     */
    private Mat image;

    /**
     * Card's dark image resource, e.g. Imgcodecs.imread
     * ("/sdcard/Android/data/com.github.hikari_toyama/files/dark_b3.png")
     */
    private Mat darkImg;

    /**
     * Card's name, e.g. "Blue 3"
     */
    private String name;

    /**
     * Constructor. Provide parameters for an Uno card and create its instance.
     */
    Card(Mat image, Mat darkImg, Color color, Content content, String name) {
        if (image == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // if (image == null)
        else if (darkImg == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // else if (darkImg == null)
        else if (color == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // else if (color == null)
        else if (content == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // else if (content == null)
        else if (name == null) {
            throw new IllegalArgumentException("DO NOT PASS NULL PARAMETER!!!");
        } // else if (name == null)
        else {
            this.name = name;
            this.image = image;
            this.color = color;
            this.content = content;
            this.darkImg = darkImg;
            this.wildColor = BLACK;
            this.order = color.ordinal() << 8 | content.ordinal();
        } // else
    } // Card(Mat, Mat, Color, Content, String) (Class Constructor)

    /**
     * @return Card's image resource.
     */
    public Mat getImage() {
        return image;
    } // getImage()

    /**
     * @return Card's dark image resource.
     */
    public Mat getDarkImg() {
        return darkImg;
    } // getDarkImg()

    /**
     * @return Card's color.
     */
    public Color getColor() {
        return color;
    } // getColor()

    /**
     * Valid only when this is a wild card. Get the specified following legal
     * color by the player who played this wild card.
     *
     * @return Card's wild color.
     */
    public Color getWildColor() {
        return wildColor;
    } // getWildColor()

    /**
     * @return Card's content.
     */
    public Content getContent() {
        return content;
    } // getContent()

    /**
     * @return Card's name.
     */
    public String getName() {
        return name;
    } // getName()

    /**
     * @return Whether the card is an action card.
     */
    public boolean isAction() {
        return (content == DRAW2 || content == SKIP || content == REV);
    } // isAction()

    /**
     * @return Whether the card is a wild card.
     */
    public boolean isWild() {
        return (content == WILD || content == WILD_DRAW4);
    } // isWild()

    /**
     * @return Whether the card is a zero card.
     */
    public boolean isZero() {
        return (content == NUM0);
    } // isZero()

    /**
     * @return Whether the card is a non-zero number card.
     */
    public boolean isNonZeroNumber() {
        return (content == NUM1 || content == NUM2 || content == NUM3 ||
                content == NUM4 || content == NUM5 || content == NUM6 ||
                content == NUM7 || content == NUM8 || content == NUM9);
    } // isNonZeroNumber()

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
            result = (this.order == ((Card) another).order);
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