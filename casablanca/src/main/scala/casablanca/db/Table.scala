package casablanca.db

import java.sql.Connection
import java.util.Date
import util.control.Exception.allCatch
import javax.sql.DataSource
import scala.util.Try
import scala.util.Success
import scala.util.Failure

trait Tx {
  val ds: DataSource
  def start = Tx.set(ds.getConnection())
  implicit def conn: Connection = Tx.get()
  def close = Tx.remove()
}

object Tx extends ThreadLocal[Connection]

class Table(val name: String, val ds: DataSource) extends Tx {

  def inTransaction[T](f: Connection => T): T = {
    start

    try {
      if (conn == null) println("WHAT?? THE? GOOK?")
      conn.setAutoCommit(false)
      val r = f(conn)
      conn.commit()
      r
    } catch {
      case e: Exception =>
        println("ROLLING BACK" + e)
        conn.rollback()
        throw e
    } finally {
      conn.close
      close
    }

  }

  def inTransaction2[T](f: Connection => T): Try[T] = {
    val conn = ds.getConnection()

    try {
      if (conn == null) println("WHAT?? THE? GOOK?")
      conn.setAutoCommit(false)
      val r = f(conn)
      conn.commit()
      Success(r)
    } catch {
      case e: Exception =>
        println("ROLLING BACK" + e)
        conn.rollback()
        Failure(e)
    } finally {
      conn.close
    }

  }

  def getRowz(sql: String): Option[Row] = {
    val rows = filterTx(sql)

    rows.size match {
      case 0 => None
      case 1 => Some(rows.rows(0))
      case size => throw new Error(s"Too many ${size}")
    }
  }

  def getRowTx(sql: String)(implicit conn: Connection): Option[Row] = {
    val rows = filterTx(sql)

    rows.size match {
      case 0 => None
      case 1 => Some(rows.rows(0))
      case size => throw new Error(s"Too many ${size}")
    }
  }

  def getRowTx(id: Long)(implicit conn: Connection): Option[Row] = getRowTx(s"id = ${id}")(conn)

  def mapTx[B](f: Row => B)(implicit conn: Connection): List[B] = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {
      val rs = st.executeQuery(s"SELECT * FROM ${name}"); // run the query
      new Rows(rs).map[B](f)
    } finally {
      st.close
    }
  }

  def deleteTx(sql: String)(implicit conn: Connection): Int = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {
      st.executeUpdate(s"DELETE FROM ${name} WHERE ${sql}"); // run the query	       
    } finally {
      st.close()
    }
  }

  /*
   * Need distributedTo and distributedFrom columns
   * on client start up find last distributed from 
   * and GET /distribute/clinetNodeId/taskType/UUID
   * then save task to table including distributedFrom
   * 
   * Server looks for task with task id of client UUID
   * and distributed node id, this is deleted.
   * If deletedCount == 0 it means we're out of sync.
   * In that case seek a distributedTo and return it. 
   * In the normal case, checkout a task in a transaction/synchronized 
   * and return that. 
   * 
   *         
   */
  /*def toStream(sql: String, bufferSize: Int = 10): Stream[Option[Row]] = {

    def fetchBuffer(): Option[Row] = {
      //filter(s"${sql} LIMIT ${bufferSize}")
      None
    }

    Stream.cons(fetchBuffer(), fetchBuffer _)
  }*/

  def filterTx(sql: String)(implicit conn: Connection): Rows = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {
      val rs = st.executeQuery(s"SELECT * FROM ${name} WHERE ${sql}"); // run the query
      new Rows(rs)
    } finally {
      st.close
    }
  }

  def updateTx(values: String, filter: String)(implicit conn: Connection): Int = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {

      val sql = s"UPDATE ${name} SET ${values} WHERE ${filter}"
      st.executeUpdate(sql); // run the query

    } finally {
      st.close
    }
  }

  private def mapToSql(value: Any): Any = {
    value match {
      case v: String => s"'${v}'"
      case v: Date => v.getTime
      case null => "null"
      case Some(x) => mapToSql(x)
      case None => "null"
      case v => v
    }
  }

  def insertTx(values: Any*)(implicit conn: Connection): Int = {
    val st = conn.createStatement(); // statement objects can be reused with
    try {
      val asStrs = values map (mapToSql(_))

      val str = asStrs.mkString(", ")
      val sql = s"INSERT INTO ${name} VALUES ( ${str})"
      st.executeUpdate(sql); // run the query

    } finally {
      st.close
    }
  }
}