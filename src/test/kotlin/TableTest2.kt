import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.OriginalTable

fun main() {
    val bcdice = BCDice()

    //動作に必要なファイルがインストールされているかを確認
    if (!bcdice.wasInstalled()) {
        //インストール
        bcdice.install()
    }

    //ゲームシステム読み込みなどのセットアップを行う
    bcdice.setup()

    //テーブルのデータをTableDataとして作成
    val tableData = OriginalTable.TableData(
        "飲み物表",
        "1D6",
        mapOf(
            1 to "水",
            2 to "緑茶",
            3 to "麦茶",
            4 to "コーラ",
            5 to "オレンジジュース",
            6 to "選ばれし者の知的飲料"
        )
    )

    val result = bcdice.rollOriginalTable(tableData)
    println(result.text) //結果のテキスト
}