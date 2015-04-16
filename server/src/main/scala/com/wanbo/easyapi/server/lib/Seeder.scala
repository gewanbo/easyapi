package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.JSONObject

/**
 * The abstract class of seeders
 * Created by GWB on 2015/4/8.
 */
abstract class Seeder {

    protected var fruits: JSONObject = _

    protected def onDBHandle()
}