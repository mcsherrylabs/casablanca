package casablanca.db

import java.sql.Connection
import java.util.Date

class Table(val name: String, val conn: Connection) {

  def getRow(sql: String): Option[Row] = {
    val rows = filter(sql)

    rows.size match {
      case 0 => None
      case 1 => Some(rows.rows(0))
      case size => throw new Error(s"Too many ${size}")
    }
  }

  def getRow(id: Long): Option[Row] = getRow(s"id = ${id}")

  def map[B](f: Row => B): List[B] = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {
      val rs = st.executeQuery(s"SELECT * FROM ${name}"); // run the query
      new Rows(rs).map[B](f)
    } finally {
      st.close
    }
  }

  def delete(sql: String): Int = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {
      st.executeUpdate(s"DELETE FROM ${name} WHERE ${sql}"); // run the query	       
    } finally {
      st.close
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

  def filter(sql: String): Rows = {

    val st = conn.createStatement(); // statement objects can be reused with
    try {
      val rs = st.executeQuery(s"SELECT * FROM ${name} WHERE ${sql}"); // run the query
      new Rows(rs)
    } finally {
      st.close
    }
  }

  def update(values: String, filter: String): Int = {
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

  def insert(values: Any*): Int = {
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