package casablanca.db

import java.sql.DriverManager
import java.sql.SQLException
import casablanca.util.Configure


object Db {

  def apply(dbName : String) = {
    new Db(dbName)
  }
}

class Db(dbConfigName: String) extends Configure {
  
    val myConfig = config(dbConfigName)
    
    // Load the HSQL Database Engine JDBC driver
    // hsqldb.jar should be in the class path or made part of the current jar
    Class.forName(myConfig.getString("driver")) // "org.hsqldb.jdbc.JDBCDriver");

    // connect to the database.   This will load the db files and start the
    // database if it is not alread running.
    // db_file_name_prefix is used to open or create files that hold the state
    // of the db.
    // It can contain directory names relative to the
    // current working directory

    private val conn = DriverManager.getConnection(myConfig.getString("connection"),    // filenames
                                       myConfig.getString("user"),                     // username
                                       myConfig.getString("pass"));                      // password

    {
        
      if(myConfig.hasPath("deleteSql")) {
    		val deleteSql = myConfig.getString("deleteSql")
    		
    		if(deleteSql.length > 0) {
    			val st = conn.createStatement()
    			try {
    				val deleted =  st.executeUpdate(deleteSql)
    				println(s"${deleteSql} Deleted count ${deleted}")
    			} finally st.close
    		}
      }
      if(myConfig.hasPath("createSql")) {
    		val createSql = myConfig.getString("createSql")
    		if(createSql.length > 0) {
    		  val st = conn.createStatement()
    			try {    			  
    			  val created = st.executeUpdate(createSql)
    			  println(s"${createSql} Created count ${created}")
    			} catch {
    			  case e : SQLException => {
    			    println(e)
    			  } 
    			
    			} finally st.close
    		}
      }
        
    }
    
    
   def table(name: String): Table = {
     new Table(name, conn);
   }
   
   def shutdown  {
         
     val st = conn.createStatement();
        // db writes out to files and performs clean shuts down
        // otherwise there will be an unclean shutdown
        // when program ends
     st.execute("SHUTDOWN");
     conn.close(); 
     
   }
}