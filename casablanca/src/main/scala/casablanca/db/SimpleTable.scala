package casablanca.db

import java.sql.Connection
import java.util.Date
import util.control.Exception.allCatch
import javax.sql.DataSource

class SimpleTable(name: String, ds: DataSource) extends Table(name, ds) {

  def getRow(sql: String): Option[Row] = inTransaction[Option[Row]](getRowTx(sql))

  def getRow(id: Long): Option[Row] = inTransaction[Option[Row]](getRowTx(id))

  def map[B](f: Row => B): List[B] = inTransaction[List[B]](mapTx(f))

  def delete(sql: String): Int = inTransaction[Int](deleteTx(sql))

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
    inTransaction(filterTx(sql))
  }

  def update(values: String, filter: String): Int = {
    inTransaction(updateTx(values, filter))
  }

  def insert(values: Any*): Int = {
    inTransaction(insertTx(values: _*))
  }

}