package com.wanbo.easyapi.server.actors

import java.net.{InetSocketAddress, ServerSocket}

import akka.actor.{Props, Actor}
import akka.io.Tcp._
import akka.io.{Tcp, IO}
import akka.routing.{DefaultResizer, RoundRobinRouter}
import com.wanbo.easyapi.server.lib.EasyConfig
import com.wanbo.easyapi.server.messages._

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class SeedWatcher(conf: EasyConfig, port: Int) extends Actor {

    import context.system

    val resizer = DefaultResizer(lowerBound=1, upperBound = conf.workersMaxThreads)

    val worker = context.actorOf(Props(new Worker(conf)).withRouter(RoundRobinRouter(resizer = Some(resizer))), name = "worker")

    IO(Tcp) ! Bind(self, new InetSocketAddress(conf.serverHost, port))

    override def receive: Receive = {

        case b @ Bound(localAddress) =>
            // Bound success

        case CommandFailed(_: Bind) =>
            // Close listeners and then
            sender() ! ListenerFailed

        case c @ Connected(remoteAddress, localAddress) =>
            sender() ! Register(worker)


        case ListenerWorkerStop =>
            context.stop(worker)
            context.stop(self)
    }

}
