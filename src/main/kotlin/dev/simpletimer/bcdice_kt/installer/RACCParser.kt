package dev.simpletimer.bcdice_kt.installer

import dev.simpletimer.bcdice_kt.BCDice
import java.io.File

/**
 * raccのファイル(.y)の変換をおこなう
 *
 * @property bcdiceDirectory BCDiceのディレクトリ
 * @property targetPath 生成されるファイル(.rb)のパス
 */
class RACCParser(
    private val bcdiceDirectory: File,
    private val targetPath: String
) {
    //変換元のファイル(.y)のパスを生成
    private val parserFilePath = targetPath.replace(".rb", ".y")

    fun parse(): File {
        //Rubyのスクリプトを実行
        BCDice.jRubyEngine.eval(
            """
${BCDice.header}
require 'racc/static'
require 'optparse'

class RaccProfiler
  def initialize(really)
    @really = really
    @log = []
    unless ::Process.respond_to?(:times)
      # Ruby 1.6
      @class = ::Time
    else
      @class = ::Process
    end
  end

  def section(name)
    if @really
      t1 = @class.times.utime
      result = yield
      t2 = @class.times.utime
      @log.push [name, t2 - t1]
      result
    else
      yield
    end
  end
end

result = RaccProfiler.new(false).section('parse') {
    parser = Racc::GrammarFileParser.new(Racc::DebugFlags.new)
    parser.parse(File.read('${parserFilePath}'), File.basename('${parserFilePath}'))
}

generator = Racc::ParserFileGenerator.new(Racc::States.new(result.grammar).nfa, result.params.dup)
generator.generate_parser_file('${targetPath}')
        """
        )

        //ディレクトリを返す
        return File(bcdiceDirectory, targetPath)
    }
}