package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Database driver
 * Created by wanbo on 15/4/16.
 */
trait IDriver {
    def setConfiguration(conf: EasyConfig)
    def close()
}
