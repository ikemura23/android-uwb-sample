package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

/**
 * Pixel 9 のデバイスタイプ、固定で4台定義しておく
 */
enum class DeviceType(val uuid: String, val displayName: String) {
    GREEN("50f6971d-9875-33c7-b231-8e2f99bdb811", "緑"),
    RED("27cd139d-5b54-38d6-9999-089e17cf9c23", "赤"),
    YELLOW("4247b308-be67-39a6-bd26-00e4d8f469cf", "黄"),
    BROWN("692ffa86-df4c-317d-8532-4f3f2deb1dba", "茶");

    companion object {
        val ALL = entries
    }
} 