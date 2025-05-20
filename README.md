* [English](#enu)
* [日本語](#jpn)
* [简体中文](#chs)
***

<a name="enu" />

🔍Brief introduction
====================

This is a simple UNO card game running on PC and Android devices.

💻For Windows x64 PC Devices
============================

Download the binary release
---------------------------

1. Go to [release](https://github.com/shiawasenahikari/UnoCard/releases) page and download the
   latest version in zip file `(UnoCard-v3.8.zip)`.
2. Unzip the downloaded zip file, then execute `UnoCard.exe` and have fun!

Download the source code and compile manually
---------------------------------------------

1. Before continuing, make sure that you have `Qt 5.12.12 MinGW 64-bit` and `Visual Studio Code`
   installed on your computer. Download Qt 5.12 offline installer from:
   [Download Offline Installers](https://www.qt.io/offline-installers),
   and Visual Studio Code installer from:
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com).
   DO NOT CHANGE THE INSTALLATION DIRECTORIES. KEEP THEM AS DEFAULT.
2. Add the following directory into your `PATH` environment variable:
   `C:\Qt\Qt5.12.12\5.12.12\mingw73_64\bin`
3. Clone this repository by executing the following command in Windows Terminal or Git Bash (replace
   `<proj_root>` with a directory path where you want to store this repository):
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
4. Open `<proj_root>\UnoCard` directory in your Visual Studio Code.
5. Execute [Run]->[Run Without Debugging] menu command (or press Ctrl+F5) to run this program.

💻For MAC OS X PC Devices
=========================

1. Ensure that you have installed Qt Toolkit on your computer. If not, install it by executing the
   following command in your bash terminal:
```Bash
brew install qt5
brew link --force qt5
```
2. Ensure that you have installed Visual Studio Code on your computer. If not, get installer from:
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com).
3. Clone this repository by executing the following command in your bash terminal (replace
   `<proj_root>` with a directory path where you want to store this repository):
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
4. Open `<proj_root>/UnoCard` directory in your Visual Studio Code.
5. Execute [Run]->[Run Without Debugging] menu command (or press Ctrl+F5) to run this program.

💻For Linux x86_64 PC Devices
=============================

1. Ensure that you have installed Qt Toolkit on your computer. If not, install it by executing the
   following command in your bash terminal:
```Bash
# For Ubuntu/Debian users:
sudo apt install qt5-default qtmultimedia5-dev

# For Fedora/CentOS/RHEL users:
sudo yum install epel-release && sudo yum install qt5*
```
2. Ensure that you have installed GStreamer library on your computer. If not, install it by
   executing the following commands in your bash terminal:
```Bash
# For Ubuntu/Debian users:
sudo apt install libgstreamer1.0-dev
sudo apt install libgstreamer-plugins-base1.0-dev libgstreamer-plugins-bad1.0-dev
sudo apt install gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad
sudo apt install gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-doc gstreamer1.0-tools
sudo apt install gstreamer1.0-x gstreamer1.0-alsa gstreamer1.0-gl gstreamer1.0-gtk3 gstreamer1.0-qt5
sudo apt install gstreamer1.0-pulseaudio

# For Fedora/CentOS/RHEL users:
sudo yum install epel-release && sudo yum install gstreamer*
```
3. Ensure that you have installed Visual Studio Code on your computer. If not, get installer from:
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com).
4. Clone this repository by executing the following command in your bash terminal (replace
   `<proj_root>` with a directory path where you want to store this repository):
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
5. Open `<proj_root>/UnoCard` directory in your Visual Studio Code.
6. Execute [Run]->[Run Without Debugging] menu command (or press Ctrl+F5) to run this program.

📱For Android Phone Devices
===========================

Download the binary release
---------------------------

1. Go to [release](https://github.com/shiawasenahikari/UnoCard/releases) page and download the
   latest version in apk file `(UnoCard-v3.8.apk)`.
2. On your Android phone, open [Settings] app, go to [Security] page, then check the [Unknown
   sources] toggle.
3. Push the downloaded file to your Android phone, then install and launch it to have fun!

Download the source code and compile manually
---------------------------------------------

1. Clone this repository by executing the following command in Windows Terminal or Git Bash (replace
   `<proj_root>` with a directory path where you want to store this repository):
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
2. Open your Android Studio IDE (version 4.3 or higher), click [Open an existing Android Studio
   project], then select the `<proj_root>\UnoCard-android` directory. Click [OK].
3. You may need to install missing components during the project building procedure, such as Android
   SDK 30, and Android Build Tools 30.0.3.
4. Enable USB debugging function on your Android phone, connect your phone to computer, then execute
   [Run]->[Run 'app'] menu command to run this application on your phone.

🎴How to play
=============

1. Each player draws 7 cards to set up a new UNO game. Player to the left of the dealer plays first.
   Play passes to the left to start (YOU->WEST->NORTH->EAST).

2. Match the top card on the DISCARD pile either by color or content. For example, if the card is a
   Green 7 (pic #1), you must play a Green card (pic #2), or a 7 card with another color (pic #3).
   Or, you may play any [wild] (pic #4) or [wild +4] (pic #5) card. If you don't have anything that
   matches, or you just don't want to play any of your playable cards, you must pick a card from the
   DRAW pile. If you draw a card you can play, you may play it immediately. Otherwise, play moves to
   the next person.

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_g7.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_g9.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_b7.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw+.png" />
</p>

3. When you play a +2 (Draw Two) card, the next person to play must draw 2 cards and forfeit his/her
   turn. This card may only be played on a matching color or on another +2 card.

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_r+.png" />
</p>

4. When you play a 🔄 (Reverse), reverse direction of play. Play to the left now passes to the right,
   and vice versa. This card may only be played on a matching color or on another 🔄 card.

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_y$.png" />
</p>

5. When you play a 🚫 (Skip) card, the next person to play loses his/her turn and is "skipped". This
   card may only be played on a matching color or on another 🚫 card.

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_b@.png" />
</p>

6. When you play a ⊕ (Wild) card, you may change the color being played to any color (including
   current color) to continue play. You may play a ⊕ card even if you have another playable card in
   hand.

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw.png" />
</p>

7. When you play a +4 (Wild Draw Four) card, call the next color played, require the next player to
   pick 4 cards from the DRAW pile, and foreit his/her turn. However, there is a hitch! You can only
   play this card when you don't have a card in your hand that matches the color of the previously
   played card. If you suspect that your previous player has played a +4 card illegally, you may
   challenge him/her. A challenged player must show his/her hand to the player who challenged. If
   the challenged player actually used the +4 card illegally, the challenged player draws 4 cards
   instead. On the other hand, if the challenged player is not guilty, the challenger must draw the
   4 cards, plus 2 additional cards.

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw+.png" />
</p>

8. Before playing your next-to-last card, you must say "UNO". In this application, "UNO" will be
   said automatically. Who successfully played his/her last card becomes the winner.

💡Special Rules
===============

How to Enable
-------------
Click the `<OPTIONS>` button in the left-bottom corner, and enable your favorite special rules.

7-0
---
When someone plays a 7, that player must swap hands with another player.
When anyone plays a 0, everyone rotates hands in the direction of play.

Stack
-----
+2 and Wild +4 cards can be stacked. A player that can't add to the stack must draw the total.
If you make Wild +4 cards stackable, Wild +4 cards will become playable at any time, not just
`when you don't have a card in your hand that matches the color of the previously played card`.
For balance, Wild +4 cards will lose the `call the next color played` ability under this rule.

<a name="jpn" />

🔍簡単な紹介
============

UnoCard は、PC および Android デバイスで実行される簡易な UNO カードゲームです。

💻Windows x64 PC デバイスで実行する
===================================

バイナリリリースのダウンロード
------------------------------

1. [release](https://github.com/shiawasenahikari/UnoCard/releases) ページに入って、
   最新版の zip ファイルをダウンロードします `(UnoCard-v3.8.zip)`。
2. ダウンロードした zip ファイルを解凍し、`UnoCard.exe` を実行します。

手動でソースコードをコンパイル
------------------------------

1. 続行する前に、既に `Qt 5.12.12 MinGW 64-bit` と `Visual Studio Code`
   がインストールされているかを確認します。
   Qt 5.12 のオフラインインストーラーは次のページでダウンロードできます：
   [Download Offline Installers](https://www.qt.io/offline-installers)
   Visual Studio Code のインストーラーは次のページでダウンロードできます：
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com)
   インストールディレクトリは既定のままで、変更しないでください。
2. 次のディレクトリを `PATH` 環境変数に追加します：
   `C:\Qt\Qt5.12.12\5.12.12\mingw73_64\bin`
3. Windows ターミナルまたは Git Bash で次のコマンドを実行し、リポジトリをクローンします。
   `<proj_root>` を、リポジトリの保存したいパスに置き換えてください。
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
4. Visual Studio Code で `<proj_root>\UnoCard` ディレクトリを開きます。
5. [実行]->[デバッグなしで実行] メニューコマンドを選択し（または Ctrl+F5 を押し）、
   プログラムを実行します。

💻MAC OS X PC デバイスで実行する
================================

1. 既に Qt Toolkit がインストールされているかを確認します。
   未インストールの場合は、ターミナルで次のコマンドを実行し、インストールします：
```Bash
brew install qt5
brew link --force qt5
```
2. Visual Studio Code がインストールされているかを確認してください。
   未インストールの場合は、次のページへアクセスし、インストーラーを取得します：
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com)。
3. Bash で次のコマンドを実行し、リポジトリをクローンします。
   `<proj_root>` を、リポジトリの保存したいパスに置き換えてください。
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
4. Visual Studio Code で `<proj_root>\UnoCard` ディレクトリを開きます。
5. [実行]->[デバッグなしで実行] メニューコマンドを選択し（または Ctrl+F5 を押し）、
   プログラムを実行します。

💻Linux x86_64 PC デバイスで実行する
====================================

1. 既に Qt Toolkit がインストールされているかを確認します。
   未インストールの場合は、ターミナルで次のコマンドを実行し、インストールします：
```Bash
# Ubuntu/Debian のユーザーはこちら：
sudo apt install qt5-default qtmultimedia5-dev

# Fedora/CentOS/RHEL のユーザーはこちら：
sudo yum install epel-release && sudo yum install qt5*
```
2. 既に GStreamer がインストールされているかを確認します。
   未インストールの場合は、ターミナルで次のコマンドを実行し、インストールします：
```Bash
# Ubuntu/Debian のユーザーはこちら：
sudo apt install libgstreamer1.0-dev
sudo apt install libgstreamer-plugins-base1.0-dev libgstreamer-plugins-bad1.0-dev
sudo apt install gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad
sudo apt install gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-doc gstreamer1.0-tools
sudo apt install gstreamer1.0-x gstreamer1.0-alsa gstreamer1.0-gl gstreamer1.0-gtk3 gstreamer1.0-qt5
sudo apt install gstreamer1.0-pulseaudio

# Fedora/CentOS/RHEL のユーザーはこちら：
sudo yum install epel-release && sudo yum install gstreamer*
```
3. Visual Studio Code がインストールされているかを確認してください。
   未インストールの場合は、次のページへアクセスし、インストーラーを取得します：
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com).
4. Bash で次のコマンドを実行し、リポジトリをクローンします。
   `<proj_root>` を、リポジトリの保存したいパスに置き換えてください。
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
5. Visual Studio Code で `<proj_root>\UnoCard` ディレクトリを開きます。
6. [実行]->[デバッグなしで実行] メニューコマンドを選択し（または Ctrl+F5 を押し）、
   プログラムを実行します。

📱Android スマホで実行する
==========================

バイナリリリースのダウンロード
------------------------------

1. [release](https://github.com/shiawasenahikari/UnoCard/releases) ページに入って、
   最新版の apk ファイルをダウンロードします `(UnoCard-v3.8.apk)`。
2. Android スマホで「設定」アプリを開いて、「セキュリティ」ページに入って、
   スイッチ「不明なソースからアプリをインストールする」をオンにします。
3. ダウンロードしたファイルをスマホに転送し、インストールし、実行します。

手動でソースコードをコンパイル
------------------------------

1. Windows ターミナルまたは Git Bash で次のコマンドを実行し、リポジトリをクローンします。
   `<proj_root>` を、リポジトリの保存したいパスに置き換えてください。
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
2. Android Studio IDE を開き(バージョン 4.3 以上)、[Open an existing Android Studio project]
   をクリックし、`<proj_root>\UnoCard-android` ディレクトリを選んだら [OK] をクリックします。
3. プロジェクトをビルドしている途中に、Android SDK 30 や Android Build Tools 30.0.3
   などのコンポーネントをインストールする必要がある場合があります。
4. Android スマホの USB デバッグ機能を有効にし、コンピュータに接続し、[Run]->[Run 'app']
   メニューコマンドを選択し、アプリを実行します。

🎴遊び方
========

1. 親は、各プレイヤーに７枚ずつカードを伏せて配ります。親の左隣の人が最初のプレイヤーです。
   時計回り（あなた→西→北→東）にカードを捨てていきます。

2. 順番になったら、カードを１枚、引き札の山からカードを１枚捨てます。
   それから前の捨てたカードと同じ色、または同じ内容のカードを捨てます。
   例えば、前の捨てたカードが緑７（図 1）の場合は、任意の緑のカード（図 2）、
   または別の色の７カード（図 3）が捨てられます。
   それ以外に、ワイルド（図 4）とワイルド +4（図 5）も捨てられます。
   マッチするカードがない、または何も捨てたくない時は、引き札の山からカードを１枚引きます。
   引いてきたカードがマッチするカードの場合は、そのカードをすぐに捨てることができます。
   カードを捨てるか、１枚引くと、次の人に順番が移ります。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_g7.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_g9.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_b7.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw+.png" />
</p>

3. +2（ドロー２）カードを捨てると、次の番の人は引き札の山からカードを２枚引かなければなりません。
   カードは捨てられず、次の人に順番が移ります。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_r+.png" />
</p>

4. 🔄（リバース）カードを捨てると、順番の移る方向が逆になります。
   左回りだったのが右回りに、右回りだったのが左回りになるわけです。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_y$.png" />
</p>

5. 🚫（スキップ）カードを捨てると、次の番の人が一回抜かされます。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_b@.png" />
</p>

6. ⊕（ワイルド）カードは、場のカードが何であっても捨てることができます。
   このカードを出す人は、好きな色を宣言します。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw.png" />
</p>

7. +4（ワイルドドロー４）カードは、場のカードが何であっても捨てることができます。
   このカードを出す人は、好きな色を宣言します。
   他に使える同色のカードを持っている時は、このカードは使えません。
   ずるをして使うと罰則がありますが、反則してだますこともできます。
   次の人には２つの選択肢があります。
   ひとつは、４枚カードを引いて手持ちのカードを捨てられず、その次の人に順番が移ります。
   または、ワイルドドロー４が反則で使われていると思った時は、「チャレンジ」をコールします。
   疑われた人は、手持ちのカードをコールした人にだけ見せなければなりません。
   反則していなかった場合は、チャレンジをコールした人が罰を受けます。
   ワイルドドロー４の指示に従って４枚引くだけでなく、罰としてさらに２枚引かなければなりません。
   本当に反則だった場合は、罰としてカードを４枚引きます。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw+.png" />
</p>

8. 手持ちのカードが２枚になってそのうちの１枚を捨てるとき、
   その人はみんなに向かって「ウノ」と宣言しなければなりません。
   このアプリは自動的に「ウノ」を宣言します。最後の１枚のカードを捨てられた人が勝ちです。

💡特殊ルール
============

有効にする方法
--------------
左下隅の `<OPTIONS>` ボタンをクリックし、好きな特殊ルールを有効にします。

7-0
---
７のカードを出すと、誰か一人とカードを全て入れ替える。
０を出すとゲームと同じ回転向きで手札を入れ替える。

スタッキング
------------
+2 と +4 のカードが重ねられます。
+2 か +4 を重ねられないプレイヤーは全ての罰を受けます。
+4 カードが積み重ね可能に設定している場合は、+4 カードは
`他に使える同色のカードを持っている時は、このカードは使えません` の制限が解除され、
いつでも使えるようになります。バランスのため、`好きな色を宣言します` の能力が失います。

<a name="chs" />

🔍简介
======

这是运行在 PC 和 Android 设备上的简易 UNO 纸牌游戏。

💻在 Windows x64 PC 设备上运行
==============================

下载编译好的 binary release
---------------------------

1. 进入 [release](https://github.com/shiawasenahikari/UnoCard/releases) 页面下载最新版本的 zip 包
   `(UnoCard-v3.8.zip)`.
2. 解压并执行 `UnoCard.exe` 开始游戏。

下载源码并手动编译
------------------

1. 在您开始编译前，请确认您的电脑上已安装 `Qt 5.12.12 MinGW 64bit` 和 `Visual Studio Code`。
   访问以下页面下载 Qt 5.12 离线安装包：
   [Download Offline Installers](https://www.qt.io/offline-installers)。
   访问以下界面下载 Visual Studio Code 安装包：
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com)。
   软件的安装目录保持默认即可，请勿更改。
2. 将以下目录添加到你的 `PATH` 环境变量中：
   `C:\Qt\Qt5.12.12\5.12.12\mingw73_64\bin`
3. 在 Windows 命令提示符或 Git Bash 中执行如下命令以克隆本仓库
   (请将 `<proj_root>` 替换为存储本仓库源码的目录路径)
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
4. 用 Visual Studio Code 打开 `<proj_root>\UnoCard` 目录。
5. 执行 [运行]->[以非调试模式运行] 菜单命令 (或按 Ctrl+F5) 开始运行。

💻在 MAC OS X PC 设备上运行
===========================

1. 请确认您的电脑上已安装 Qt 工具包。若您尚未安装，则在 Bash 终端中执行如下命令安装：
```Bash
brew install qt5
brew link --force qt5
```
2. 请确认您的电脑上已安装 Visual Studio Code。若您尚未安装，则访问以下界面获取安装包：
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com)。
3. 在 Bash 中执行如下命令以克隆本仓库 (请将 `<proj_root>` 替换为存储本仓库源码的目录路径)
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
4. 用 Visual Studio Code 打开 `<proj_root>/UnoCard` 目录。
5. 执行 [运行]->[以非调试模式运行] 菜单命令 (或按 Ctrl+F5) 开始运行。

💻在 Linux x86_64 PC 设备上运行
===============================

1. 请确认您的电脑上已安装 Qt 工具包。若您尚未安装，则在 Bash 终端中执行如下命令以安装：
```Bash
# Ubuntu/Debian 发行版用户执行该条
sudo apt install qt5-default qtmultimedia5-dev

# Fedora/CentOS/RHEL 发行版用户执行该条
sudo yum install epel-release && sudo yum install qt5*
```
2. 请确认您的电脑上已安装 GStreamer。若您尚未安装，则在 Bash 终端中执行如下命令以安装：
```Bash
# Ubuntu/Debian 发行版用户执行以下命令
sudo apt install libgstreamer1.0-dev
sudo apt install libgstreamer-plugins-base1.0-dev libgstreamer-plugins-bad1.0-dev
sudo apt install gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad
sudo apt install gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-doc gstreamer1.0-tools
sudo apt install gstreamer1.0-x gstreamer1.0-alsa gstreamer1.0-gl gstreamer1.0-gtk3 gstreamer1.0-qt5
sudo apt install gstreamer1.0-pulseaudio

# Fedora/CentOS/RHEL 发行版用户执行以下命令
sudo yum install epel-release && sudo yum install gstreamer*
```
3. 请确认您的电脑上已安装 Visual Studio Code。若您尚未安装，则访问以下界面获取安装包：
   [Visual Studio Code - Code Editing. Redefined](https://code.visualstudio.com)。
4. 在 Bash 中执行如下命令以克隆本仓库 (请将 `<proj_root>` 替换为存储本仓库源码的目录路径)
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
5. 用 Visual Studio Code 打开 `<proj_root>/UnoCard` 目录。
6. 执行 [运行]->[以非调试模式运行] 菜单命令 (或按 Ctrl+F5) 开始运行。

📱在 Android 设备上运行
=======================

下载编译好的 binary release
---------------------------

1. 进入 [release](https://github.com/shiawasenahikari/UnoCard/releases) 页面下载最新版本的 apk 包
   `(UnoCard-v3.8.apk)`.
2. 打开您的 Android 设备中的 [设置] 应用，进入 [安全] 页面，勾选 [未知来源] 复选框。
3. 将已下载的安装包传到手机中，安装并运行即可开始游戏。

下载源码并手动编译
------------------

1. 在 Windows 命令提示符或 Git Bash 中执行如下命令以克隆本仓库
   (请将 `<proj_root>` 替换为存储本仓库源码的目录路径)
```Bash
git clone https://github.com/shiawasenahikari/UnoCard.git <proj_root>
```
2. 打开您的 Android Studio IDE (版本 4.3 或更高), 单击 [Open an existing Android Studio project]，
   并选择 `<proj_root>\UnoCard-android` 目录。然后单击 [OK] 按钮。
3. 在项目生成过程中，您可能需要安装缺失的组件，诸如 Android SDK 30 以及 Android Build Tools 30.0.3。
4. 打开您 Android 设备中的 USB 调试功能，然后将您的 Android 设备连接到电脑，执行 [Run]->[Run 'app']
   菜单命令以便在您的 Android 设备上运行此应用。

🎴玩法说明
==========

1. 每位玩家抓 7 张牌以开启一局新的 UNO 游戏。由庄家左手边的玩家最先出牌，起始出牌顺序为顺时针方向
   (玩家->西家->北家->东家)。

2. 第一位出牌的玩家依牌库翻出的第一张牌的颜色或内容跟牌。例如，第一张翻出的牌是绿 7 (图 1)，
   则您必须跟绿牌 (图 2) 或其他颜色的 7 牌 (图 3)。或者，您也可以跟万能牌 (图 4) 或王牌 (图 5)。
   后续的玩家则依前一张跟出的牌的颜色或内容跟牌。当您没有可跟的牌，或不愿跟出任何一张可跟的牌时，
   需要从牌库上方摸一张牌。如果摸到的是可跟的牌，则您可以立即将该牌跟出。然后轮到下一位玩家行动。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_g7.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_g9.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_b7.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw.png" />
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw+.png" />
</p>

3. 当您跟出一张 +2 (罚两张) 时，您的下家必须摸两张牌，并失去一次行动权。
   这张牌只有在与前一张跟出的牌同色或同为 +2 时才能跟出。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_r+.png" />
</p>

4. 当您跟出一张 🔄 (反转牌) 时，改变接下来的行动顺序。若原先为顺时针方向则改为逆时针方向，反之亦然。
   这张牌只有在与前一张跟出的牌同色或同为 🔄 时才能跟出。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_y$.png" />
</p>

5. 当您跟出一张 🚫 (阻挡牌) 时，您的下家“被跳过”，失去一次行动权。
   这张牌只有在与前一张跟出的牌同色或同为 🚫 时才能跟出。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_b@.png" />
</p>

6. 当您跟出一张 ⊕ (万能牌) 时，指定接下来跟牌的颜色 (可更换颜色，亦可保持当前的颜色) 并继续游戏。
   在您的行动回合里，您随时可跟出该牌，即使您手中仍有其他可跟的牌。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw.png" />
</p>

7. 当您跟出一张 +4 (王牌) 时，指定接下来跟牌的颜色，然后令您的下家摸四张牌, 并失去一次行动权。
   但是，有一个限制！您只有在手中没有与前一张跟牌同色的牌时才能跟这张牌。当您的上家对您使用 +4 时，
   您有权质疑您的上家没按规定跟出它。此时您的上家必须出示其所有手牌。若您的上家确实非法使用 +4 了，
   则改为其摸四张牌；反之，如果上家是合法使用的，则您除了原先的四张牌外，还要额外罚摸两张牌。

<p align="center">
<img src="https://github.com/shiawasenahikari/UnoCard/blob/master/UnoCard/resource/front_kw+.png" />
</p>

8. 当您打出了倒数第二张牌 (即出完此牌后只剩一张牌)，您必须大声喊出 UNO。
   本程序会在满足条件时自动喊出 UNO。最先出完手中所有牌的玩家获胜。

💡特殊规则
==========

如何开启
--------
点击左下角的 `<OPTIONS>` 按钮，然后在选项界面中开启您喜欢的特殊规则。

7-0
---
当一位玩家打出 7 时，该玩家需要和另一位玩家交换手牌。
当一位玩家打出 0 时，所有玩家将手牌传给各自的下家。

叠加
----
当一位玩家使用 +2/+4 牌时，下家可以叠加另一张 +2/+4 以免去惩罚。最终无法如此做的玩家摸取所有的罚牌。
如果您允许叠加 +4 牌，那么 +4 牌不再仅限于 `只有在手中没有与前一张跟牌同色的牌时才能跟这张牌`，
而是和普通万能牌一样随时可跟。作为平衡，此规则下 +4 牌无法 `指定接下来跟牌的颜色`。

🔗Acknowledgements
==================

* The card images are from [Wikipedia](https://commons.wikimedia.org/wiki/File:UNO_cards_deck.svg).
* The PC app is using the [Qt](https://www.qt.io) GUI library.
* The Android app is using the [OpenCV](https://opencv.org) GUI library.
* The Android app is using a custom UI when crashes, which is provided by the
  [CustomActivityOnCrash](https://github.com/Ereza/CustomActivityOnCrash) library.
* The background music is from:
  [兔子跳儿童欢快音乐_站长素材](https://sc.chinaz.com/yinxiao/210502415031.htm)

📄License
=========

    Copyright 2022 Hikari Toyama

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
