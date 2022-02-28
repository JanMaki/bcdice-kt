package dev.simpletimer.bcdice_kt.bcdice_task

import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.result.Rand
import dev.simpletimer.bcdice_kt.bcdice_task.result.Result
import org.jruby.Ruby
import org.jruby.RubyArray
import org.jruby.RubyHash
import org.jruby.RubyStruct

class OriginalTable(private val tableText: String) {
    constructor(tableData: TableData) : this(tableData.toString())

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
$tableText
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

    data class TableData(
        val tableName: String,
        val dice: String,
        val table: Map<Int, String>
    ) {
        override fun toString(): String {
            return "$tableName\n$dice\n${table.entries.joinToString("\n") { "${it.key}:${it.value}" }}"
        }
    }
}