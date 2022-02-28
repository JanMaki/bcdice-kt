import dev.simpletimer.bcdice_kt.BCDice;
import dev.simpletimer.bcdice_kt.bcdice_task.GameSystem;
import dev.simpletimer.bcdice_kt.bcdice_task.result.Result;

public class JavaRollTest {
    public static void main(String[] args) {
        BCDice bcdice = new BCDice();

        //動作に必要なファイルがインストールされているかを確認
        if (!bcdice.wasInstalled()) {
            //インストール
            bcdice.install();
        }

        //ゲームシステム読み込みなどのセットアップを行う
        bcdice.setup();

        //ゲームシステムを取得
        GameSystem gameSystem = bcdice.getGameSystem("Cthulhu7th");

        Result result = gameSystem.roll("CC+1");
        System.out.println(result.getText()); //結果のテキスト
    }
}
