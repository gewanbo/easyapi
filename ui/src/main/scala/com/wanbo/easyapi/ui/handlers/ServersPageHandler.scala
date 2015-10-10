package com.wanbo.easyapi.ui.handlers

import java.io._
import java.net.Socket
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.UIUtils
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{ContextHandler, AbstractHandler}

import scala.xml.Node

/**
 * The handler for server list page.
 * Created by wanbo on 15/8/27.
 */
class ServersPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            var serverList = Map[String, Long]()
            val servers = availableServers

            servers.foreach(s => {
                val info = getSummary(s)
                if(info != "") {
                    try {
                        info.split("\\|").map(_.split("=")).foreach(i => {
                            if(i.size > 1)
                                serverList += i(0) -> i(1).toLong
                        })
                    } catch {
                        case e: Exception =>
                            log.error("Throws exception when parse server information.", e)
                    }
                }
            })

            page.title = "Servers"
            page.content = makeTable(serverList.toSeq)

            log.info("Response contents ------------ server")
            out.println(UIUtils.commonNavigationPage(page))
            log.info("Response contents ------------ server finish")

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def availableServers: Seq[String] = {
        var serverList = Seq[String]()
        val zk = new ZookeeperClient(conf.zkHosts)

        val serverNode = "/easyapi/servers"

        val servers = zk.getChildren(serverNode)

        serverList = servers.map(_.split(":")(0)).toSeq

        zk.close()
        serverList.distinct
    }

    private def getSummary(host: String): String ={
        var info = ""

        try {
            val socket = new Socket(host, 8860)

            val outStream = socket.getOutputStream

            val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)))

            val inStream = new InputStreamReader(socket.getInputStream)
            val in = new BufferedReader(inStream)

            out.println("workcount")
            out.flush()

            val msg = in.readLine()

            info = msg

            println(msg)

            out.close()
            outStream.close()

            in.close()
            inStream.close()

            socket.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        info
    }

    private def makeTable(data: Seq[(String, Long)]): Seq[Node] = {

        val rows = data.map(r => {
            <tr>
                <td>{r._1}</td>
                <td>{r._2}</td>
            </tr>
        })

        <h2>Server List</h2>
            <p>All the available servers.</p>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Server</th>
                        <th>Hits</th>
                    </tr>
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
    }
}