package com.wanbo.easyapi.server.database

import java.sql.{SQLException, Connection}

import com.wanbo.easyapi.server.database.mysql._
import com.wanbo.easyapi.server.lib.EasyException
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * Mysql driver
 * Created by wanbo on 15/4/16.
 */
case class MysqlDriver() extends DbDriver with IDriver {

    private var _conn:Connection = null

    override def setConfiguration(conf: EasyConfig): Unit = {
        this._conf = conf
    }

    def getConnector(dbName: String = "test", writable: Boolean = false): Connection ={

        try {

            var sourceList = MysqlDriver.dataSourceList.filter(x => x._1._1 == dbName && x._1._2 == writable)

            // If didn't find readable data source, can read from writable data source.
            if(sourceList.isEmpty && !writable){
                sourceList = MysqlDriver.dataSourceList.filter(x => x._1._1 == dbName)
            }

            if(sourceList.nonEmpty) {
                _conn = util.Random.shuffle(sourceList).head._2.getConnection
            } else {
                throw new Exception("Didn't find the available database source.")
            }

        } catch {
            case sqlE: SQLException =>
                throw new EasyException("40001")
            case e: Exception =>
                throw e
        }

        _conn
    }

    def close(): Unit ={
        try{
            if(_conn != null)
                _conn.close()
        } catch {
            case e: Exception =>
        }
    }
}

object MysqlDriver {

    private var dataSourceList: List[((String, Boolean), DataSource)] = List[((String, Boolean), DataSource)]()

    /**
     * Initialize all available data source.
     *
     * Called by manager when it start up.
     */
    def initializeDataSource(settings: List[Map[String, String]]): Unit ={

        if (settings.nonEmpty) {
            settings.foreach(x => {
                val db_host = x.getOrElse("host", "")
                val db_port = x.getOrElse("port", "")
                val db_username = x.getOrElse("uname", "")
                val db_password = x.getOrElse("upswd", "")
                val db_name = x.getOrElse("dbname", "")
                val db_writable = if (x.get("writable").get.toLowerCase == "true") true else false

                val ctx = new AnnotationConfigApplicationContext(classOf[BeanConfig])

                val ds: DataSource = ctx.getBean(classOf[DataSource])
                ds.setUrl("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8".format(db_host, db_port, db_name))
                ds.setUsername(db_username)
                ds.setPassword(db_password)

                MysqlDriver.dataSourceList = MysqlDriver.dataSourceList :+ ((db_name, db_writable), ds)
            })
        }

    }

}