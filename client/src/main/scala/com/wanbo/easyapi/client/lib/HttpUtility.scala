package com.wanbo.easyapi.client.lib

import java.io._
import java.net.Socket

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{ServerNode, ServerNodeFactory}

/**
 * Http utility
 * Created by root on 15-12-11.
 */
object HttpUtility extends Logging {

    val jsonHeader = "HTTP/1.1 200 OK\nContent-Type: application/json\n\n"
    val textHeader = "HTTP/1.1 200 OK\nContent-Type: text/plain\n\n"

    def post(message: String): String ={
        var responseMsg = ""

        var serverNode: ServerNode = null

        try {

            log.info("Post request to server with message:" + message)

            val availableServer = AvailableServer.availableServer

            if(availableServer == "")
                throw new Exception("Can't get an available server!")

            serverNode = ServerNodeFactory.parse(availableServer)

            log.info("Post request to host {%s} and port {%s} !".format(serverNode.host, serverNode.port))

            val socket = new Socket(serverNode.host, serverNode.port)
            socket.setSoTimeout(5000)

            val outStream = socket.getOutputStream

            val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)))

            val inStream = new InputStreamReader(socket.getInputStream)
            val in = new BufferedReader(inStream)

            out.println(message)
            out.flush()

            val sb = new StringBuilder

            var line = in.readLine()

            while(line != null){
                sb.++=(line + "\r")
                line = in.readLine()
            }

            //log.info("Response message is1----:" + sb.toString())

            responseMsg = parseBody(sb.toString())

            //log.info("Response message is2----:" + responseMsg)

            out.close()
            outStream.close()

            in.close()
            inStream.close()

            socket.close()

            WorkCounter.push(serverNode, MetricsItem(MetricsStatus.success, 0))
        } catch {
            case e: Exception =>
                // Count the miss request.
                if(serverNode != null)
                    WorkCounter.push(serverNode, MetricsItem(MetricsStatus.failure, 0))
                log.error("Error:" + e)
        }

        responseMsg
    }

    /**
     * Extract http request body
     * @param msgData    The full request message.
     * @return           The part of body in full request message.
     */
    def parseBody(msgData: String): String ={
        var msgBody = ""

        var firstLine = true
        var hasBody = false
        var mark = false
        msgData.split("\r").foreach(x => {
            val body = x.trim

            if (mark)
                msgBody += body
            else {

                if (firstLine) {
                    if (body.contains("GET") || body.contains("POST") || body.contains("HTTP"))
                        hasBody = true
                }

                if (hasBody) {
                    if (body.isEmpty)
                        mark = true
                } else {
                    msgBody += body
                }

            }

            firstLine = false
        })

        msgBody
    }
}
