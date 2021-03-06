package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.MessageQ
import com.wanbo.easyapi.server.messages.{CacheUpdate, UpdateCache}
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.{LoggerFactory, MDC}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Cache update manager
 * Created by wanbo on 15/5/6.
 */
class CacheManager(conf: EasyConfig) extends Actor {

    import context.system

    val log = LoggerFactory.getLogger(classOf[CacheManager])

    private var cacheList = List[(String, Map[String, String], Int)]()

    override def preStart(){
        //cacheList = cacheList :+ ("10003", Map("days" ->"7", "num" ->"10"), 200)
        cacheList = cacheList :+ ("11001", Map[String, String](), 72000)
    }

    override def receive: Receive = {

        case "init" =>
            MDC.put("destination", "cache")
            log.info("Start to initialize the cache list which need to update.")

            cacheList.foreach(x => {
                system.scheduler.schedule(10 seconds, x._3 seconds, self, UpdateCache(x._1, x._2))
            })

            MDC.clear()

        case UpdateCache(name, seed) =>

            log.info("----------- Push a CacheUpdate message [%s] to queue.".format(name))
            MessageQ.push("UpdateCache", CacheUpdate(name, seed))

    }
}
