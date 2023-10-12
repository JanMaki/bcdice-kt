package dev.simpletimer.bcdice_kt

import dev.simpletimer.bcdice_kt.installer.Installer
import dev.simpletimer.bcdice_kt.bcdice_task.GameSystem
import dev.simpletimer.bcdice_kt.bcdice_task.GameSystems
import dev.simpletimer.bcdice_kt.bcdice_task.OriginalTable
import dev.simpletimer.bcdice_kt.bcdice_task.result.Result
import org.jruby.embed.jsr223.JRubyEngine
import java.io.*
import java.net.URL
import java.nio.file.Paths
import javax.script.ScriptEngineManager

/**
 * BCDiceを扱うためのすべての起点
 *
 */
class BCDice {
    companion object {
        /**
         * BCDiceのURL
         */
        val bcdiceURL = URL("https://github.com/bcdice/BCDice/archive/refs/tags/v3.11.0.zip")

        /**
         * Rubyを動かすエンジン
         */
        val jRubyEngine: JRubyEngine = ScriptEngineManager().getEngineByName("ruby") as JRubyEngine

        /**
         * 実行ファイルのディレクトリ
         */
        lateinit var directory: File

        /**
         * Rubyを実行する際の共通のヘッダー
         */
        var header = ""

        /**
         * Rubyを実行する際の共通のヘッダーを生成する
         *
         * @param bcdiceDirectory BCDiceのディレクトリ
         * @param gemsDirectory gemのディレクトリ
         * @return ヘッダー
         */
        fun generateHeader(bcdiceDirectory: File, gemsDirectory: File) {
            val buffer = StringBuffer()
            gemsDirectory.listFiles()?.forEach { gemDirectory ->
                gemDirectory.listFiles()?.forEach file@{ directory ->
                    if (!directory.name.contains("lib")) {
                        return@file
                    }
                    var libDirectory = directory
                    if (directory.listFiles()?.size == 1 && directory.listFiles()
                            ?.first()?.name?.contains(".rb") != true
                    ) {
                        libDirectory = directory.listFiles()?.first()!!
                    }
                    buffer.append("${'$'}:.unshift(\'${libDirectory.absolutePath}\')\n")
                }
            }

            header = """
Dir.chdir('${bcdiceDirectory.absolutePath}')
${'$'}:.unshift(Dir.pwd+'/lib')
$buffer
            """
        }
    }

    /**
     * インストーラー
     */
    private val installer: Installer

    /**
     * ゲームシステムの一覧
     */
    private var gameSystems: GameSystems? = null

    init {
        //実行ファイルのディレクトリを取得
        directory = File(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()).toString()).parentFile

        //インストーラーのインスタンス
        installer = Installer()
    }

    /**
     * BCDiceのセットアップを行う
     * インストールをあらかじめしておく必要がある
     *
     * @return インストールの情報を返す
     */
    fun setup(): Installer.InstallInfo {
        //インストールの情報を確認する
        val installInfo = installer.check()
        if (!installInfo.check) {
            println("インストールを先に行ってください")
            return installInfo
        }

        //各ディレクトリを取得
        val bcdiceDirectory = installInfo.bcdiceDirectory!!
        val gemsDirectory = installInfo.gemsDirectory!!

        //headerが作られてない時は、作成をする
        if (header == "")
            generateHeader(bcdiceDirectory, gemsDirectory)

        //ゲームシステムを全て読み込む
        gameSystems = GameSystems()

        return installInfo
    }

    /**
     * 動作に必要なファイルをインストールする
     *
     */
    fun install() {
        try {
            //インストール
            installer.install()
        }catch (e: IOException) {
            //もう一度ためしてみる
            installer.install()
        }
    }

    /**
     * 必要なファイルがインストールがされているかを確認する
     *
     * @return インストールされていたらtrueを返す
     */
    fun wasInstalled(): Boolean {
        return installer.check().check
    }

    /**
     * ゲームシステムの一覧を取得する
     * 先にsetup()を実行しておく必要がある
     *
     * @return ゲームシステムの一覧
     */
    fun getGameSystems(): GameSystems? {
        return gameSystems
    }

    /**
     * ゲームシステムのIDから、ゲームシステムを取得する
     * 先にsetup()を実行しておく必要がある
     *
     * @param id ゲームシステムのID
     * @return ゲームシステム
     */
    fun getGameSystem(id: String): GameSystem {
        val gameSystem = gameSystems?.filter { it.id == id }
        if (gameSystem.isNullOrEmpty()){
            throw IllegalArgumentException("無効なゲームシステムのIDです: $id")
        }
        return gameSystem.first()
    }

    /**
     * テキスト形式のオリジナル表を実行する
     * [オリジナル表](https://docs.bcdice.org/original_table.html)
     *
     * @param text 表のテキスト
     * @return 結果
     */
    fun rollOriginalTable(text: String): Result {
        return OriginalTable(text).roll()
    }

    /**
     * オリジナル表を実行する
     *
     * @param tableData 表のデータ
     * @return 結果
     */
    fun rollOriginalTable(tableData: OriginalTable.TableData): Result {
        return OriginalTable(tableData).roll()
    }
}