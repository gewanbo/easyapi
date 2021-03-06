package com.wanbo.easyapi.server.cache

/**
 * The abstract cache class.
 * Created by wanbo on 15/4/27.
 */
abstract class EasyCache {
    var ttl: Long = _
    protected var _expire: Int = _
    def get(name: String): String
    def set(name: String, data: String, inExpire: Int = -1): Boolean
    def del(name: String): Boolean
    def close()
}