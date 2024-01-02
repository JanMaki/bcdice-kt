import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.OriginalTable
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RollTest {
    companion object {
        private var bcdice: BCDice? = null

        @JvmStatic
        @BeforeAll
        fun beforeAll(): Unit {
            bcdice = BCDice().apply {
                //動作に必要なファイルがインストールされているかを確認
                if (!this.wasInstalled()) {
                    //インストール
                    this.install()
                }

                this.setup()
            }
        }
    }

    @Test
    fun rollTest1() {
        //ゲームシステムを取得
        val gameSystem = bcdice?.getGameSystem("Cthulhu7th")

        val result = gameSystem?.roll("CC+1")
        println(result?.text) //結果のテキスト

        //テスト結果
        assertEquals(result?.check, true)
    }

    @Test
    fun tableTest1() {

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

        val result = bcdice?.rollOriginalTable(text)
        println(result?.text) //結果のテキスト

        //テスト結果
        assertEquals(result?.check, true)
    }

    @Test
    fun tableTest2() {
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

        val result = bcdice?.rollOriginalTable(tableData)
        println(result?.text) //結果のテキスト

        //テスト結果
        assertEquals(result?.check, true)
    }
}