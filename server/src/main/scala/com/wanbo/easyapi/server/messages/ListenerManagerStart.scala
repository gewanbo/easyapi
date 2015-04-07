package com.wanbo.easyapi.server.messages

import java.util.Properties

/**
 * Start listener message
 * Created by wanbo on 15/4/3.
 */
case class ListenerManagerStart(conf: Properties) extends SystemMessage