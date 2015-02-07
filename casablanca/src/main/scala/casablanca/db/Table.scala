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
    
        val st = conn.createStatement();         // statement objects can be reused with
        try {
	        val rs = st.executeQuery(s"SELECT * FROM ${name}" );    // run the query
	        new Rows(rs).map[B](f)
        } finally {
          st.close 
        }
  }
  
  
  def filter(sql: String): Rows = {
    
        val st = conn.createStatement();         // statement objects can be reused with
        try {
	        val rs = st.executeQuery(s"SELECT * FROM ${name} WHERE ${sql}");    // run the query
	        new Rows(rs)
        } finally {
          st.close 
        }
  }
  
  def update(values: String, filter: String): Int = {
    val st = conn.createStatement();         // statement objects can be reused with
        try {
            
            val sql = s"UPDATE ${name} SET ${values} WHERE ${filter}"
	        st.executeUpdate(sql);    // run the query
	        
        } finally {
          st.close 
        }
  }
  
  def insert(values: Any*): Int = {
    val st = conn.createStatement();         // statement objects can be reused with
        try {
            val asStrs = values map { value =>
              value match {
                case v: String => s"'${v}'"
                case v: Date => v.getTime
                case null => s"null"
                case v => v
              }                
            }
            val str = asStrs.mkString(", ")
            val sql = s"INSERT INTO ${name} VALUES ( ${str})"
	        st.executeUpdate(sql);    // run the query
	        
        } finally {
          st.close 
        }
  }
}