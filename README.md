This is a simple UNO card game running on PC.

Download the binary release
---------------------------

1. Go to the [release](https://github.com/shiawasenahikari/UnoCard/releases) page and download the
   latest version in zip file (UnoCard-v1.1.zip).

2. Unzip the downloaded zip file, then execute `UnoCard.exe` and have fun!

Download the source code and compile manually
---------------------------------------------

1. Clone this repository by executing the following command in Windows Terminal or Git Bash (replace
   `<proj_root>` with a directory path where you want to store this repository):
```
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
2. Open `<proj_root>\UnoCard.sln` solution file in your Visual Studio 2015 IDE (or higher).
3. Execute [BUILD]->[Build Solution] menu command (or press F6) to build this project.
4. Execute [DEBUG]->[Start Without Debugging] menu command (or press Ctrl+F5) to run this program.

How to play
-----------

1. In welcome screen, select a difficulty (EASY / HARD) to start a new game.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/1.png)

2. You can only play a wild card, or a card whose color/content is same to the previous played card.
   For example, when the previous played card is Green 6 (like the scene below), you can play a wild
   card, a green card, or a 6 card with another color. When the game started, everyone needs to draw
   7 cards, and system will pick a non-wild card from the card deck, as the previous played card. In
   the scene below, you can play the following cards: Wild, Blue 6, Green 2, Yellow 6. If there are
   no card can be played legally, or you just don't want to play any of your legal cards now, draw a
   card from the card deck.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/2.png)

3. When a 🔄(reverse) card is played, change the following action sequence. For example, when the
   sequence was YOU->WEST->NORTH->EAST(clockwise) before, change it to YOU->EAST->NORTH->WEST
   (counter-clockwise) after a 🔄 card is played. In the scene below, NORTH changed the direction
   to counter-clockwise by playing the Yellow 🔄 card. Then instead of EAST, WEST starts its turn.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/3.png)

4. When a 🚫(skip) card is played, skip the next player's turn. In the scene below, NORTH skipped
   WEST's turn by playing the Green 🚫 card. You start your turn then.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/4.png)

5. When a +2(draw two) card is played, let the next player draw two cards and skip its turn. In the
   scene below, you let EAST draw two cards and skip its turn by playing the Blue +2 card. NORTH
   starts its turn then.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/5.png)

6. When a ⊕(wild) or +4(wild draw four) card is played(both of them are wild cards), select a color
   at first. The card becomes colored after you do that, which means the next played card must be a
   wild card or has the same color to your specified one. Specially, when the played card is +4, in
   addition, the next player draws four cards and skips its turn. In the scene below, NORTH changed
   the following legal color to yellow and let WEST draw four cards, by playing the +4 card. You
   start your turn then.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/6.png)

7. You need to say "UNO" when you have only one card in hand. In the scene above, NORTH played Wild
   +4 and said UNO. In the scene below, you played the Green 2 and said "UNO".

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/7.png)

8. Who played its all hand cards at first becomes the winner.

![screenshot](https://github.com/shiawasenahikari/UnoCard/blob/master/README.files/8.png)

Acknowledgements
----------------

* The card images are from [Wikipedia](https://commons.wikimedia.org/wiki/File:UNO_cards_deck.svg).
* This application is using the GUI module provided by the [OpenCV](https://opencv.org) library.

License
-------

    Copyright 2019 Hikari Toyama

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
