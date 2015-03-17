package casablanca.db

import java.sql.DriverManager
import java.sql.SQLException
import casablanca.util.Configure
import casablanca.util.Logging

object Db {

  def apply(dbName: String) = {
    new Db(dbName)
  }
}

class Db(dbConfigName: String) extends Configure with Logging {

  val myConfig = config(dbConfigName)

  // Load the HSQL Database Engine JDBC driver
  // hsqldb.jar should be in the class path or made part of the current jar
  Class.forName(myConfig.getString("driver")) // "org.hsqldb.jdbc.JDBCDriver");

  private val conn = DriverManager.getConnection(myConfig.getString("connection"),
    myConfig.getString("user"),
    myConfig.getString("pass"))

  {

    if (myConfig.hasPath("deleteSql")) {
      val deleteSql = myConfig.getString("deleteSql")

      if (deleteSql.length > 0) {
        val st = conn.createStatement()
        try {
          val deleted = st.executeUpdate(deleteSql)
          log.info(s"${deleteSql} Deleted count ${deleted}")
        } catch {
          case e: SQLException => {
            log.info(s"${deleteSql} failed, maybe object doesn't exist?!")
          }

        } finally st.close
      }
    }
    if (myConfig.hasPath("createSql")) {
      val createSql = myConfig.getString("createSql")
      if (createSql.length > 0) {
        val st = conn.createStatement()
        try {
          val created = st.executeUpdate(createSql)
          log.info(s"${createSql} Created count ${created}")
        } catch {
          case e: SQLException => log.warn(s"Failed to create ${createSql}", e)

        } finally st.close
      }
    }

  }

  def table(name: String): Table = {
    new Table(name, conn);
  }

  def shutdown {

    val st = conn.createStatement();
    try {
      // db writes out to files and performs clean shuts down
      // otherwise there will be an unclean shutdown
      // when program ends
      st.execute("SHUTDOWN");
    } finally {
      conn.close();
    }

  }
}