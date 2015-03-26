package casablanca.db

import org.scalatest._
import java.util.Date

class DbSpec extends FlatSpec with Matchers {

  "A Db " should " allow access to an existing table " in {

    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.table("test")
    dbUnderTest.shutdown
  }

  it should " allow insert into existing table " in {

    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")
    val numInserted = table.insert(0, "strId", new Date().getTime, 42)
    assert(numInserted == 1, s"Should be 1 row created not ${numInserted}!")
    dbUnderTest.shutdown
  }

  it should " be able to read all rows from a table " in {

    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")
    val time = new Date()
    table.insert(0, "strId", time, 42)
    val rows: List[Row] = table.map(r => r)
    assert(rows.size === 1, "Should only be one row!")
    val row: Row = rows(0)
    println(row)
    assert(row[String]("strId") === "strId")
    assert(row[Long]("createTime") === time.getTime)
    assert(row[Int]("intVal") === 42)
    dbUnderTest.shutdown
  }

  it should " be able to find the row inserted " in {

    val time = new Date()
    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")
    table.insert(0, "strId", time, 45)
    val rows = table.filter(s"createTime = ${time.getTime} ")
    assert(rows.size === 1, "Should only be one row found !")
    val row = rows.rows(0)
    assert(row[String]("strId") == "strId")
    assert(row[Long]("createTime") == time.getTime)
    assert(row[Int]("intVal") == 45)

    dbUnderTest.shutdown
  }

  it should " be able to find the row inserted by id " in {

    val time = new Date()
    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")
    table.insert(99, "strId", time, 45)
    table.getRow(99) match {
      case None => fail("oh oh, failed to find row by id")
      case Some(r) => assert(r[Int]("id") == 99)
    }

    dbUnderTest.shutdown
  }

  it should " be able to find the row searching by field name " in {

    val time = new Date()
    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")
    table.insert(99, "strId", time, 45)
    table.getRow(s"id = 99") match {
      case None => fail("oh oh, failed to find row by id")
      case Some(r) => assert(r[Int]("id") == 99)
    }

    dbUnderTest.shutdown
  }

  it should " not be able to find a single row when 2 are present " in {

    val time = new Date()
    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")
    table.insert(99, "strId", time, 45)
    table.insert(99, "strId", time, 45)
    try {
      table.getRow(s"id = 99")
      fail("there are 2 rows with 99,  should throw ...")
    } catch {
      case e: Error =>
    }

    dbUnderTest.shutdown

  }

  it should " support a transaction " in {

    val time = new Date()
    val dbUnderTest = Db("testDb")
    val table = dbUnderTest.simpleTable("test")

    try {
      table.inTransaction {
        table.insertTx(999999, "strId", time, 45)
        throw new Error("Ah HA!")

      }
    } catch {
      case e: Error => println(e)
    }

    try {
      table.getRow(s"id = 999999") match {
        case Some(r) => fail("there is a row with 999999,  should throw ...")
        case x =>
      }

    } catch {
      case e: Error =>
    }

    dbUnderTest.shutdown
  }
}