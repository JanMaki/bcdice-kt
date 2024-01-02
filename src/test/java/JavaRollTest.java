import dev.simpletimer.bcdice_kt.BCDice;
import dev.simpletimer.bcdice_kt.bcdice_task.GameSystem;
import dev.simpletimer.bcdice_kt.bcdice_task.OriginalTable;
import dev.simpletimer.bcdice_kt.bcdice_task.result.Result;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaRollTest {
    private static BCDice bcdice;

    @BeforeAll
    static void beforeAll() {
        bcdice = new BCDice();

        //動作に必要なファイルがインストールされているかを確認
        if (!bcdice.wasInstalled()) {
            //インストール
            bcdice.install();
        }

        //ゲームシステム読み込みなどのセットアップを行う
        bcdice.setup();
    }

    @Test
    void rollTest1() {
        //ゲームシステム読み込みなどのセットアップを行う
        bcdice.setup();

        //ゲームシステムを取得
        GameSystem gameSystem = bcdice.getGameSystem("Cthulhu7th");

        Result result = gameSystem.roll("CC+1");
        System.out.println(result.getText()); //結果のテキスト

        //テスト結果
        assertTrue(result.getCheck());
    }

    @Test
    void tableTest1() {
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
                """.trim();

        Result result = bcdice.rollOriginalTable(text);
        System.out.println(result.getText()); //結果のテキスト

        //テスト結果
        assertTrue(result.getCheck());
    }

    @Test
    void tableTest2() {
        //テーブルのデータをTableDataとして作成
        OriginalTable.TableData tableData = new OriginalTable.TableData(
                "飲み物表",
                "1D6",
                Map.of(
                        1, "水",
                        2, "緑茶",
                        3, "麦茶",
                        4, "コーラ",
                        5, "オレンジジュース",
                        6, "選ばれし者の知的飲料"
                )
        );

        Result result = bcdice.rollOriginalTable(tableData);
        System.out.println(result.getText()); //結果のテキスト

        //テスト結果
        assertTrue(result.getCheck());
    }
}
