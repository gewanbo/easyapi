package com.wanbo.easyapi.server.lib

/**
 * Error code.
 * Created by wanbo on 15/4/16.
 */
object ErrorConstant {
    private var errorList = Map[String, String]()

    errorList = errorList.+("ERROR_UNDEFINED" -> "Undefined exception.")

    errorList = errorList.+("0" -> "Successful.")
    errorList = errorList.+("12002" -> "Undefined exception.")

    errorList = errorList.+("99999" -> "Undefined exception.")

    def getErrorMessage(code: String): String = {
        if(errorList.contains(code))
            errorList.get(code).get
        else
            errorList.get("ERROR_UNDEFINED").get
    }
}
