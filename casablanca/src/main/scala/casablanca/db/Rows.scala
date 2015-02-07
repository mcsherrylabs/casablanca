package casablanca.db

import java.sql.ResultSet

class Row {
  def apply[T](col: String): T = {
    map(col.toUpperCase).asInstanceOf[T]
  }
    
  var map: Map[String, Any] = Map()
  
  override def toString: String = {
    map.foldLeft( ""){ case (a, (k,v)) => a + s" Key:${k}, Value: ${v}"}
  }
}

class Rows(private val rs: ResultSet) {

     
  var rows : List[Row] = List()
  
  
  dump(rs)
  
  def size = rows.size
  
  def dump(rs: ResultSet) {

        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        val meta   = rs.getMetaData();
        val colmax = meta.getColumnCount();

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        while(rs.next()) {
            var r = new Row()
            for (i <- 0 until colmax) {
                val o = rs.getObject(i + 1);    // Is SQL the first column is indexed
                r.map = r.map + (meta.getColumnName(i + 1) -> o) 
                // with 1 not 0
                //System.out.print(o.toString() + " ");
            }
            rows = r :: rows 
            //System.out.println(" ");
        }
    }
        
  def map[B](f: Row => B): List[B] = {
    for {
      row <- rows
    } yield {
      f(row)
    }
  }
}