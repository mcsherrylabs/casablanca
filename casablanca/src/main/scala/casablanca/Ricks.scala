package casablanca

import casablanca.db.Rows

import casablanca.db.Row

import casablanca.db.Db

import casablanca.db.Table


object Ricks {

  def main(args: Array[String]): Unit = {
    println("Everybody comes to Ricks place")
    
    val db = Db("db_file")
    val table: Table = db.table("sample_table")
    val linkTable: Table = db.table("sample_table_linked")
    
    val rows: Rows = table.filter("num_col > 1")
    rows.map( row => {
      println(num(row))
      
      val linkId = num(row)
      linkTable.getRow("num_col = " + linkId) map {
        row => //println((row("NUM_COL"))) 
      }
    })
    
  }

  def num(r: Row): Int = {
    r("NUM_COL")
  }
}