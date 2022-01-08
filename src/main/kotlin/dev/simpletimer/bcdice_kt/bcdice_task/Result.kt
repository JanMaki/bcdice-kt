package dev.simpletimer.bcdice_kt.bcdice_task

/**
 * ダイスロールの結果
 *
 * @property check コマンドの実行の成否
 * @property text コマンドの出力
 * @property secret シークレットダイスか
 * @property success 結果が成功か
 * @property failure 結果が失敗か
 * @property critical 結果がクリティカルか
 * @property fumble 結果がファンブルか
 * @property rands ダイス目の詳細
 */
data class Result(
    val check: Boolean,

    val text: String,

    val secret: Boolean,

    val success: Boolean,

    val failure: Boolean,

    val critical: Boolean,

    val fumble: Boolean,

    val rands: Array<Rand>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Result

        if (text != other.text) return false
        if (secret != other.secret) return false
        if (success != other.success) return false
        if (failure != other.failure) return false
        if (critical != other.critical) return false
        if (fumble != other.fumble) return false
        if (!rands.contentEquals(other.rands)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + success.hashCode()
        result = 31 * result + failure.hashCode()
        result = 31 * result + critical.hashCode()
        result = 31 * result + fumble.hashCode()
        result = 31 * result + rands.contentHashCode()
        return result
    }
}