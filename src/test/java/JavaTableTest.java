import dev.simpletimer.bcdice_kt.BCDice;
import dev.simpletimer.bcdice_kt.bcdice_task.OriginalTable;
import dev.simpletimer.bcdice_kt.bcdice_task.Result;

public class JavaTableTest {
    public static void main(String[] args) {
        BCDice bcdice = new BCDice();

        //動作に必要なファイルがインストールされているかを確認
        if (!bcdice.wasInstalled()) {
            //インストール
            bcdice.install();
        }

        //ゲームシステム読み込みなどのセットアップを行う
        bcdice.setup();

        //テーブルのデータをテキストで作成
        String text = """
        飲み物表
        1D6
        1:水
        2:緑茶
        3:麦茶
        4:コーラ
        5:オレンジジュース
        6:選ばれし者の知的飲料
        """;

        Result result = bcdice.rollOriginalTable(text);
        System.out.println(result.getText()); //結果のテキスト
    }
}
