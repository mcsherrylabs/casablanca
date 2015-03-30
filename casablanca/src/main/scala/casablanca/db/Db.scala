package casablanca.db

import java.sql.DriverManager
import java.sql.SQLException
import casablanca.util.Configure
import casablanca.util.Logging
import javax.sql.DataSource
import org.apache.commons.dbcp2.DriverManagerConnectionFactory
import org.apache.commons.dbcp2.PoolableConnectionFactory
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.dbcp2.PoolingDataSource
import java.util.Properties

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

  private val ds = setUpDataSource(myConfig.getString("connection"),
    myConfig.getString("user"),
    myConfig.getString("pass"))

  {

    if (myConfig.hasPath("deleteSql")) {
      val deleteSql = myConfig.getString("deleteSql")

      if (deleteSql.length > 0) {
        val conn = ds.getConnection
        val st = conn.createStatement()
        try {
          val deleted = st.executeUpdate(deleteSql)
          log.info(s"${deleteSql} Deleted count ${deleted}")
        } catch {
          case e: SQLException => {
            log.info(s"${deleteSql} failed, maybe object doesn't exist?!")
          }

        } finally {
          st.close
          conn.close
        }
      }
    }
    if (myConfig.hasPath("createSql")) {
      val createSql = myConfig.getString("createSql")
      if (createSql.length > 0) {
        val conn = ds.getConnection
        val st = conn.createStatement()
        try {
          val created = st.executeUpdate(createSql)
          log.info(s"${createSql} Created count ${created}")
        } catch {
          case e: SQLException => log.warn(s"Failed to create ${createSql}", e)

        } finally {
          st.close
          conn.close
        }
      }
    }

  }

  def table(name: String): Table = {
    new Table(name, ds);
  }

  def shutdown {
    val conn = ds.getConnection()
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

  private def setUpDataSource(connectURI: String, user: String, pass: String): DataSource = {
    //
    // First, we'll create a ConnectionFactory that the
    // pool will use to create Connections.
    // We'll use the DriverManagerConnectionFactory,
    // using the connect string passed in the command line
    // arguments.
    //
    val props = new Properties()
    props.setProperty("user", user)
    props.setProperty("password", pass)
    val connectionFactory = new DriverManagerConnectionFactory(connectURI, props)

    //
    // Next we'll create the PoolableConnectionFactory, which wraps
    // the "real" Connections created by the ConnectionFactory with
    // the classes that implement the pooling functionality.
    //
    val poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
    poolableConnectionFactory.setDefaultAutoCommit(false);

    //
    // Now we'll need a ObjectPool that serves as the
    // actual pool of connections.
    //
    // We'll use a GenericObjectPool instance, although
    // any ObjectPool implementation will suffice.
    //
    val connectionPool = new GenericObjectPool(poolableConnectionFactory)

    // Set the factory's pool property to the owning pool
    poolableConnectionFactory.setPool(connectionPool);

    //
    // Finally, we create the PoolingDriver itself,
    // passing in the object pool we created.
    //
    new PoolingDataSource(connectionPool)

  }
}