package com.yumemi.uwb.sample.oob.ble

import java.util.UUID

/** BLE で使う UUID */
object BleUuid {

    /** GATT サービスの UUID */
    val GATT_SERVICE_UUID: UUID = UUID.fromString("50f6971d-9875-33c7-b231-8e2f99bdb811")

    /** GATT キャラクタリスティックの UUID */
    val GATT_CHARACTERISTIC_UUID: UUID = UUID.fromString("e42ba363-eeaa-4e46-b7aa-049c19341f24")
}
