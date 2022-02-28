package dev.simpletimer.bcdice_kt.bcdice_task.result

/**
 * ダイス目の詳細
 *
 * @property kind ダイスロールの種類。'nomal', 'tens_d10', 'd9'の3種類
 * @property sides ダイスロールしたダイスの面数
 * @property value 出目の値
 */
data class Rand(
    val kind: String,

    val sides: Int,

    val value: Int
)