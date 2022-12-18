# RemoESample
RemoEのAPIへ定期的にアクセスして画面に出します。
ケチDGSを実践するアプリで、Android4.1などのすでに終わったタブレットを活用できます。
dtab 01というHuawei製のタブレットにて動作確認をとりました。

# DEMO
動作の様子はブログへ
https://blogger.kinkuman.net/2022/12/nature-remo-e-lite.html

# Features
nature remoのweb apiへ定期的にアクセスし、画面に表示します。

# Requirement
* Android4.1(JELLY_BEAN) API Level 16 以上のタブレット
* Android Studioを使って実機インストールできる程度のスキル
* Remo

# Installation
実機をつないで実機へインストールしてください。(Usageの作業を行った後に）

# Usage
httpURLConnection.setRequestProperty("Authorization","Bearer ここへNature Remoのサイトhome.nature.globalで取得したアクセストークンをいれてください");
Bearer<sp>のうしろ日本語部分へhome.nature.globalから入手したアクセストークンを入れてアプリをビルドしてください。

# Note
RemoのAPIは300秒で30回アクセスして良いことになっていますが、分単位でしか取得できる値が変わらないため最短の10秒単位でアクセスしてもあまり意味がありません。
コードではThread.sleep(20000);とし、20秒1回の取得となっていますが30秒１回でも十分だと思います。
RemoEのAPIではリアルタイム電力は無理で（ローカルAPIも作成当時はない）、リアルタイム電力変化を取得したい場合はWebsocketで値を入手するRemoのスマホアプリを使う必要があります。
こちらはapiが公開されていません。
１分遅れの使用電力しか表示できませんが、電気の見える化は達成できるので無駄にONになっていたりする電化製品は発見できます。

# Author
kinkuman

# License
"RemoESample" is under [MIT license](https://en.wikipedia.org/wiki/MIT_License).

