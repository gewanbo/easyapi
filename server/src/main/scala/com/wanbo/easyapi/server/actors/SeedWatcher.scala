package com.wanbo.easyapi.server.actors

import java.net.InetSocketAddress

import akka.actor.{Props, Actor}
import akka.io.Tcp._
import akka.io.{Tcp, IO}
import akka.routing.{BalancingPool, DefaultResizer, RoundRobinRouter}
import com.wanbo.easyapi.server.lib.WorkCounter
import com.wanbo.easyapi.server.messages._
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class SeedWatcher(conf: EasyConfig, port: Int) extends Actor with Logging {

    import context.system

    val resizer = DefaultResizer(lowerBound=1, upperBound = conf.workersMaxThreads)

    //val worker = context.actorOf(Props(new Worker(conf)).withRouter(RoundRobinRouter(resizer = Some(resizer))), name = "worker")
    val worker = context.actorOf(Props(new Worker(conf)).withRouter(BalancingPool(conf.workersMaxThreads)), name = "worker")

    IO(Tcp) ! Bind(self, new InetSocketAddress(conf.serverHost, port))

    override def receive: Receive = {

        case b @ Bound(localAddress) =>
            // Bound success

        case CommandFailed(_: Bind) =>
            // Close listeners and then
            sender() ! ListenerFailed

        case c @ Connected(remoteAddress, localAddress) =>
            try {
                log.info("-------------------------Connected Mark---------------")
                WorkCounter.push(conf.serverHost + ":" + localAddress.getPort)
            } catch {
                case e: Exception =>
                    log.error("Error when update work count:", e)
            }
            sender() ! Register(worker)


        case ListenerWorkerStop =>
            context.stop(worker)
            context.stop(self)
    }

}
