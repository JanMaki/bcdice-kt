package dev.simpletimer.bcdice_kt.bcdice_task

import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.result.Rand
import dev.simpletimer.bcdice_kt.bcdice_task.result.Result
import org.jruby.Ruby
import org.jruby.RubyArray
import org.jruby.RubyHash
import org.jruby.RubyStruct

/**
 * テーブルを作成し、テーブルのダイスロールを行う
 *
 * @property tableData 表のデータ
 */
class OriginalTable(private val tableData: TableData) {
    constructor(tableText: String) : this(TableData.valueOf(tableText))

    /**
     * テーブルのダイスロールを行う
     *
     * @return 結果
     */
    fun roll(): Result {
        val result = BCDice.jRubyEngine.eval(
            """
${BCDice.header}
            
require "bcdice"
require "bcdice/user_defined_dice_table"
            
text = <<~TEXT
$tableData
TEXT
            
result = BCDice::UserDefinedDiceTable.new(text).roll()

if result.nil?
{

}
else
{
text: result.text,
rands: result.rands
}
end
            """
        )?: RubyHash(Ruby.newInstance())

        //データを整形
        val data = (result as RubyHash).toMap().map { it.key.toString() to it.value }.toMap()
        //データがからの時
        if (data.isEmpty()) {
            //checkがfalseで適当なデータを返す
            return Result(
                false,
                "",
                secret = false,
                success = false,
                failure = false,
                critical = false,
                fumble = false,
                rands = emptyArray()
            )
        } else {
            //ダイス目の詳細を整形
            val rands = (data["rands"]!! as RubyArray<*>)
                .filterNotNull()
                .filterIsInstance<RubyStruct>()
                .map { struct ->
                    Rand(
                        kind = struct[0].toString(),
                        sides = struct[1].toString().toInt(),
                        value = struct[2].toString().toInt()
                    )
                }
                .toTypedArray()
            //各データを渡してデータを作り返す。
            return Result(
                true,
                data["text"].toString(),
                secret = false,
                success = false,
                failure = false,
                critical = false,
                fumble = false,
                rands
            )
        }
    }

    /**
     * テーブルのデータ
     *
     * @property tableName テーブル名
     * @property dice ロールするダイスの内容の文字列
     * @property table テーブルの内容
     */
    data class TableData(
        val tableName: String,
        val dice: String,
        val table: Map<Int, String>
    ) {
        companion object {
            /**
             * テーブルの内容のテキストから[TableData]に変換する
             *
             * @param text テーブルの内容のテキスト
             * @return [TableData]
             */
            fun valueOf(text: String): TableData {
                val rows = text.split("\n")
                return TableData(
                    rows[0],
                    rows[1],
                    rows.subList(2, rows.size).map { it.split(":") }.associate { Pair(it[0].toInt(), it[1]) })
            }
        }

        override fun toString(): String {
            return "$tableName\n$dice\n${table.entries.joinToString("\n") { "${it.key}:${it.value}" }}"
        }
    }
}