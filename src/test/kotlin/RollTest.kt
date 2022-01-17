import dev.simpletimer.bcdice_kt.BCDice

fun main() {
    val bcdice = BCDice()

    //動作に必要なファイルがインストールされているかを確認
    if (!bcdice.wasInstalled()) {
        //インストール
        bcdice.install()
    }

    //ゲームシステム読み込みなどのセットアップを行う
    bcdice.setup()

    //ゲームシステムを取得
    val gameSystem = bcdice.getGameSystem("Cthulhu7th")

    val result = gameSystem.roll("CC+1")
    println(result.text) //結果のテキスト
}