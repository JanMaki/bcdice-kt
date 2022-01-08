package dev.simpletimer.bcdice_kt.bcdice_task

import dev.simpletimer.bcdice_kt.BCDice
import org.jruby.RubyArray

/**
 * ゲームシステムの一覧を読み込む
 *
 */
class GameSystems: ArrayList<GameSystem>() {
    init {
        //Rubyのスクリプトを実行
        val result = BCDice.jRubyEngine.eval(
            """
${BCDice.header}
require "bcdice"
require "bcdice/game_system"
            
BCDice.all_game_systems.sort_by { |game| game::SORT_KEY }.map { |game| game::ID }
        """
        )

        //各IDを使って、ゲームシステムのインスタンスを生成
        addAll((result as RubyArray<*>).filterIsInstance<String>().map { GameSystem(it) })
    }
}