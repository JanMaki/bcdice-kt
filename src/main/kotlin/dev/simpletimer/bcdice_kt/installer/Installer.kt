package dev.simpletimer.bcdice_kt.installer

import com.github.kittinunf.fuel.httpGet
import dev.simpletimer.bcdice_kt.BCDice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * BCDiceのインストーラー
 * インストールの確認なども行う
 *
 */
class Installer {
    //バイトのサイズ
    private val byteSize = ByteArray(1024)

    //インストールの情報を保管するファイル
    private val installInfoFile = File(BCDice.directory, "install.yml")

    /**
     * インストールの情報を確認する
     *
     * @return インストールの情報[InstallInfo]
     */
    fun check(): InstallInfo {
        //ファイルの有無を確認
        if (!installInfoFile.exists()) {
            return InstallInfo(false)
        }
        //ファイル読み込み
        var bcdiceDirectory: File? = null
        var gemsDirectory: File? = null
        BufferedReader(FileReader(installInfoFile)).use { bufferReader ->
            //行のデータ
            var line: String?
            while (bufferReader.readLine().also { line = it } != null) {
                val text = line ?: continue
                //BCDiceの行かを確認
                if (text.startsWith("BCDice")) {
                    bcdiceDirectory = File(text.split(": ")[1])
                }
                //Gemの行かを確認
                if (text.startsWith("Gems")) {
                    gemsDirectory = File(text.split(": ")[1])
                }
            }
        }
        //nullがある時は、第１引数をfalseになるようにして返す
        return InstallInfo(
            bcdiceDirectory != null && gemsDirectory != null,
            bcdiceDirectory,
            gemsDirectory
        )
    }


    /**
     * 必要なファイルのインストールなどを行う
     *
     */
    fun install() {
        //キャッシュのディレクトリ
        val cacheDirectory = File(BCDice.directory, "cache")
        cacheDirectory.mkdir()

        //BCDiceをインストール
        val bcdiceDirectory = installBCDice(BCDice.directory, cacheDirectory)

        //動作に必要なGemをインストール
        val gemsDirectory = installGems(BCDice.directory, cacheDirectory, File(bcdiceDirectory, "Gemfile").toURI())
        installGems(BCDice.directory, cacheDirectory, javaClass.classLoader.getResource("Gemfile")?.toURI()!!)

        //Rubyの実行に必要なヘッダーを生成
        BCDice.generateHeader(bcdiceDirectory, gemsDirectory)

        //raccのパースを行う
        parse(bcdiceDirectory)

        //インストールの情報を作成
        val installDataText = """
            BCDice: ${bcdiceDirectory.absolutePath}
            Gems: ${gemsDirectory.absolutePath}
        """.trimIndent()
        //ファイルに書き込む
        installInfoFile.writeText(installDataText)

        //キャッシュのディレクトリを削除
        removeDirectory(cacheDirectory)
    }

    /**
     * BCDiceをインストールする
     *
     * @param directory インストールするディレクトリ
     * @param cacheDirectory インストールのキャッシュに使うディレクトリ
     * @return インストール結果のディレクトリ
     */
    private fun installBCDice(directory: File, cacheDirectory: File): File {
        //BCDiceをダウンロード
        val readableByteChannel: ReadableByteChannel = Channels.newChannel(BCDice.bcdiceURL.openStream())
        val bcdiceZip = File(cacheDirectory, "bcdice.zip")
        FileOutputStream(bcdiceZip).use {
            it.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
        }

        //BCDiceを解凍
        val zipInputStream = ZipInputStream(FileInputStream(bcdiceZip))
        var zipEntry: ZipEntry?
        var bcdiceDirectory: File? = null
        while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
            //nullチェック
            val entryData = zipEntry ?: continue
            //作成されるファイル
            val entryFile = File(directory, entryData.name)
            //ディレクトリかどうかを確認
            if (entryData.isDirectory) {
                //ディレクトリを作成
                entryFile.mkdir()
                if (bcdiceDirectory == null) {
                    bcdiceDirectory = entryFile
                }
            } else {
                //ファイルをコピー
                val bufferOutputStream = BufferedOutputStream(FileOutputStream(entryFile))
                var length: Int
                while (zipInputStream.read(byteSize).also { length = it } != -1) {
                    bufferOutputStream.write(byteSize, 0, length)
                }
                bufferOutputStream.close()
            }
        }
        zipInputStream.close()

        //インストールの確認
        if (bcdiceDirectory == null) {
            throw RuntimeException("BCDiceのインストールに失敗しました")
        }

        return bcdiceDirectory
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Gemをインストールする
     *
     * @param directory インストールするディレクトリ
     * @param cacheDirectory インストールのキャッシュに使うディレクトリ
     * @param gemFile BCDiceのディレクトリ
     * @return インストール結果のディレクトリ
     */
    private fun installGems(directory: File, cacheDirectory: File, gemFile: URI): File {
        //Gemfileを読み込む
        val gems = HashMap<String, String>()
        //ファイル読み込み
        val gemFileStream = gemFile.toURL().openStream()
        gemFileStream.bufferedReader().use { bufferReader ->
            //行のデータ
            var line: String?
            while (bufferReader.readLine().also { line = it } != null) {
                //nullチェク
                var text = line ?: continue
                //gemの行かを確認
                if (text.indexOf("  gem") != 0) {
                    continue
                }
                //不要な部分を削除
                text = text.replace("gem ", "")
                    .replace("\"", "")
                    .replace("~>", "")
                    .replace(" ", "")
                    .replace("\'", "")
                //カンマで分ける
                val splittedText = text.split(",")
                if (File(cacheDirectory, splittedText[0]).exists()) {
                    //すでにインストール済みならスキップ
                    continue
                }
                //バージョンの表記があるかを確認
                if (splittedText.size < 2 || !Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(splittedText[1])) {
                    val response = "https://rubygems.org/api/v1/gems/${splittedText[0]}.json".httpGet().response()
                    gems[splittedText[0]] = json.decodeFromString(
                        GemVersion.serializer(),
                        String(response.second.data)
                    ).version
                } else {
                    gems[splittedText[0]] = splittedText[1]
                }
            }
        }
        gemFileStream.close()

        //gemをインストール
        //gemが収められるディレクトリ
        val gemsDirectory = File(directory, "Gems")
        gemsDirectory.mkdir()
        gems.forEach { entry ->
            val name = entry.key
            val version = entry.value
            //ダウンロードurlを生成
            val url = URL("https://rubygems.org/downloads/${name}-${version}.gem")
            //ダウンロード
            val gemReadChannel = Channels.newChannel(url.openStream())
            val gemCacheFile = File(cacheDirectory, "${name}.gem")
            FileOutputStream(gemCacheFile).use {
                it.channel.transferFrom(gemReadChannel, 0, Long.MAX_VALUE)
            }

            //.gemの解凍(tar形式)
            //結果を出力するディレクトリ
            val gemCacheDirectory = File(cacheDirectory, name)
            gemCacheDirectory.mkdir()
            val tarInputStream = TarArchiveInputStream(FileInputStream(gemCacheFile))
            var tarEntry: ArchiveEntry?
            while (tarInputStream.nextEntry.also { tarEntry = it } != null) {
                //nullチェック
                val entryData = tarEntry ?: continue
                //作成されるファイル
                val entryFile = File(gemCacheDirectory, entryData.name)
                //ディレクトリかどうかを確認
                if (entryData.isDirectory) {
                    //ディレクトリを作成
                    entryFile.mkdir()
                } else {
                    //ファイルをコピー
                    val bufferOutputStream = BufferedOutputStream(FileOutputStream(entryFile))
                    var length: Int
                    while (tarInputStream.read(byteSize).also { length = it } != -1) {
                        bufferOutputStream.write(byteSize, 0, length)
                    }
                    bufferOutputStream.close()
                }
            }
            tarInputStream.close()

            //data.tar.gzを解凍(gzip形式)
            //結果を出力するディレクトリ
            val gemDirectory = File(gemsDirectory, name)
            gemDirectory.mkdir()
            val gzipInputStream = GzipCompressorInputStream(FileInputStream(File(gemCacheDirectory, "data.tar.gz")))
            val gzipTarInputStream = TarArchiveInputStream(gzipInputStream)
            var gzipTarEntry: ArchiveEntry?
            while (gzipTarInputStream.nextEntry.also { gzipTarEntry = it } != null) {
                //nullチェック
                val entryData = gzipTarEntry ?: continue
                //作成されるファイル
                val entryFile = File(gemDirectory, entryData.name)
                //親ディレクトリを取得
                val parentFile = entryFile.parentFile
                //親ディレクトリがなかったら作成する
                if (!parentFile.exists()) {
                    parentFile.mkdirs()
                }
                //ディレクトリかどうかを確認
                if (entryData.isDirectory) {
                    //ディレクトリを作成
                    entryFile.mkdir()
                } else {
                    //ファイルをコピー
                    val bufferOutputStream = BufferedOutputStream(FileOutputStream(entryFile))
                    var length: Int
                    while (gzipTarInputStream.read(byteSize).also { length = it } != -1) {
                        bufferOutputStream.write(byteSize, 0, length)
                    }
                    bufferOutputStream.close()
                }

                //Gemfileだった場合はインストールを実行
                if (entryFile.name.equals("Gemfile", ignoreCase = true)) {
                    installGems(directory, cacheDirectory, entryFile.toURI())
                }
            }
            gzipTarInputStream.close()
            gzipInputStream.close()
        }
        return gemsDirectory
    }

    /**
     * raccのパースを実行する
     *
     * @param bcdiceDirectory BCDiceのディレクトリ
     */
    private fun parse(bcdiceDirectory: File) {
        val rakeFile = File(bcdiceDirectory, "Rakefile")
        //ファイル読み込み
        BufferedReader(FileReader(rakeFile)).use { bufferReader ->
            //対象の行かのフラグ
            var readFlag = false
            //対象の行を格納する
            val lineList = arrayListOf<String>()
            //行のデータ
            var line: String?
            while (bufferReader.readLine().also { line = it } != null) {
                //nullチェク
                val text = line ?: continue
                //開始地点を確認
                if (!readFlag && text.startsWith("RACC_TARGETS")) {
                    readFlag = true
                    continue
                }
                //終了地点を確認
                if (readFlag && text.endsWith("freeze")) {
                    break
                }
                //対象の行の場合は格納
                if (readFlag) {
                    lineList.add(text)
                }
            }
            //文字列を整形して、パースを行う
            lineList.map {
                RACCParser(
                    bcdiceDirectory,
                    it.replace("\"", "")
                        .replace(" ", "")
                        .replace(",", "")
                ).parse()
            }
        }
    }

    /**
     * ディレクトリを削除する
     *
     * @param directory 削除するディレクトリ
     */
    private fun removeDirectory(directory: File) {
        val listFiles = directory.listFiles() ?: return
        listFiles.forEach {
            if (it.isDirectory) {
                removeDirectory(it)
            } else {
                it.delete()
            }
        }
        directory.delete()
    }

    /**
     * インストールの情報
     *
     * @property check インストールされているかどうか
     * @property bcdiceDirectory　BCDiceのディレクトリ
     * @property gemsDirectory　Gemのディレクトリ
     */
    data class InstallInfo(
        val check: Boolean,
        val bcdiceDirectory: File? = null,
        val gemsDirectory: File? = null
    )

    /**
     * GemのバージョンをAPIから取得するときに用いるデータクラス
     *
     * @property version　バージョン
     */
    @Serializable
    data class GemVersion(
        @SerialName("version")
        val version: String
    )
}