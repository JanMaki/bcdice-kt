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

    //テーブルのデータをテキストで作成
    val text = """
        飲み物表
        1D6
        1:水
        2:緑茶
        3:麦茶
        4:コーラ
        5:オレンジジュース
        6:選ばれし者の知的飲料
    """.trimIndent()

    val result = bcdice.rollOriginalTable(text)
    println(result.text) //結果のテキスト
}