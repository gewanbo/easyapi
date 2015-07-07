package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.{ZookeeperManager, EasyConfig, ZookeeperClient}
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory

/**
 * Elect a leader from cluster, and control the cache updating.
 * Created by wanbo on 15/7/1.
 */
class CacheLeader(conf: EasyConfig) extends ZookeeperManager with Actor{
    private val leader_root = "/cache_leader"

    private val _zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(callback))

    private val log = LoggerFactory.getLogger(classOf[CacheLeader])

    private var _serverId = ""

    init()

    def init(): Unit ={

        _serverId = conf.serverId

        if(!_zk.exists(server_root)){
            _zk.createPath(server_root)
        }

        if(!_zk.exists(leader_root)){
            _zk.createPath(leader_root)
        }
    }

    def callback(zk: ZookeeperClient): Unit ={

        if(zk != null) {
            zk.watchChildren(leader_root, (nodes: Seq[String]) => {

                try {
                    if (nodes != null && nodes.length > 0) {

                        log.info("Total number of workers:" + nodes.length)

                        val serverList = nodes.map(x => {
                            val fields = x.split("-")
                            (fields(0), fields(1))
                        }).sortBy(x => x._2).toList

                        serverList.foreach(x => {
                            log.info("node:" + x.toString)
                        })

                        if(serverList.count(x => x._1 == _serverId) < 1){
                            log.info("Can not find my id, start to register.")
                            zk.create("/servers/%s-".format(_serverId), Array("1".toByte), CreateMode.EPHEMERAL_SEQUENTIAL)
                        } else {

                            val tmpLeader = serverList(0)._1

                            log.info("Tmp leader is:" + tmpLeader)

                            log.info("The smallest one is:" + tmpLeader)

                            if (tmpLeader == _serverId)
                                log.info("I am the leader!!!")
                            else
                                log.info("I am follower!!....")
                        }
                    } else {
                        zk.create("/servers/%s-".format(_serverId), Array("1".toByte), CreateMode.EPHEMERAL_SEQUENTIAL)
                    }
                } catch {
                    case e: Exception =>
                        log.error("Error:", e)
                }
            })
        }
    }

    override def receive: Receive = {
        case "" =>
        // Do something
        case "shutdown" =>
            if(_zk != null) {
                log.info("Shutting down the zk...")
                _zk.close()
            }
            context.stop(self)
    }
}
