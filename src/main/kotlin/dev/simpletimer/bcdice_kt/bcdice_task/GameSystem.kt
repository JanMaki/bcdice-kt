package dev.simpletimer.bcdice_kt.bcdice_task

import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.result.Rand
import dev.simpletimer.bcdice_kt.bcdice_task.result.Result
import org.jruby.*

/**
 * ゲームシステムを読み込み、ロールをする
 *
 * @property id ゲームシステムのID
 */
class GameSystem(val id: String) {

    /**
     * ゲームシステムの名前
     */
    val name: String

    /**
     * ゲームシステムをソートするためのキー
     */
    val sort_key: String

    /**
     * 実行可能なコマンドか判定するための正規表現。
     * これにマッチするテキストがコマンドとして実行できる可能性がある。利用する際には大文字か小文字かを無視すること
     */
    val command_pattern: Regex

    /**
     * ヘルプメッセージ
     */
    val help_message: String

    init {
        //Rubyのスクリプトを実行
        val result = BCDice.jRubyEngine.eval(
            """
${BCDice.header}
require "bcdice"
require "bcdice/game_system"

game_system = BCDice.game_system_class('${id}')

if game_system.nil?
{
}
else
{
name: game_system::NAME,
sort_key: game_system::SORT_KEY,
command_pattern: game_system.command_pattern.source,
help_message: game_system::HELP_MESSAGE
}
end
        """
        )?: throw RuntimeException("ゲームシステムのロードに失敗しました: $id")
        //データを整形
        val data = (result as RubyHash).toMap().values.filterIsInstance<String>()
        //各データを代入していく
        name = data[0]
        sort_key = data[1]
        command_pattern = Regex(data[2], RegexOption.IGNORE_CASE)
        help_message = data[3]
    }

    /**
     * ダイスロールを行う
     *
     * @param command コマンドの文字列
     * @return コマンドの結果
     */
    fun roll(command: String): Result {
        //正規表現を確認
        if (command_pattern.containsMatchIn(command)) {
            //Rubyのスクリプトを実行
            val result = BCDice.jRubyEngine.eval(
                """
${BCDice.header}
require "bcdice"
require "bcdice/game_system"
                
game_system = BCDice.game_system_class('${id}')
dice_result = game_system.eval('${command}')
               
if dice_result.nil?
{
}
else
{
text: dice_result.text,
secret: dice_result.secret?,
success: dice_result.success?,
failure: dice_result.failure?,
critical: dice_result.critical?,
fumble: dice_result.fumble?,
rands: dice_result.detailed_rands
}
end
            """
            )?: RubyHash(Ruby.newInstance())
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
                    data["secret"].toString().toBoolean(),
                    data["success"].toString().toBoolean(),
                    data["failure"].toString().toBoolean(),
                    data["critical"].toString().toBoolean(),
                    data["fumble"].toString().toBoolean(),
                    rands
                )
            }
        } else {
            //checkがfalseで適当なデータを返す
            return Result(
                false, "",
                secret = false,
                success = false,
                failure = false,
                critical = false,
                fumble = false,
                rands = emptyArray()
            )
        }
    }
}