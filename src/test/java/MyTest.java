import org.junit.Test;
import net.dckg.daogenerator.NotFoundException;
import net.dckg.daogenerator.TableInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MyTest {

    @Test
    public void testSql() throws NoSuchFieldException, IllegalAccessException, SQLException {
        MyTestDao d = new MyTestDao();
        Field table = d.getClass().getSuperclass().getDeclaredField("table");
        table.setAccessible(true);

        Field debugSql = d.getClass().getSuperclass().getDeclaredField("debugIntermediateSql");
        debugSql.setAccessible(true);

        Field debugAbortAfterSql = d.getClass().getSuperclass().getDeclaredField("debugAbortAfterSql");
        debugAbortAfterSql.setAccessible(true);
        debugAbortAfterSql.set(d, true);

        TableInfo t = (TableInfo) table.get(d);

        assertEquals("Table1", t.getTableName());
        assertEquals(1, t.getKeyColumns().size());
        assertEquals("id", t.getKeyColumns().get(0).getDbName());
        assertEquals("pkId", t.getKeyColumns().get(0).getFieldName());

        assertEquals(2, t.getColumns().size());

        assertEquals(1, t.getNonKeyColumns().size());
        assertEquals("dataColumn", t.getNonKeyColumns().get(0).getDbName());
        assertEquals("data", t.getNonKeyColumns().get(0).getFieldName());

        d.pkId = null; // AUTO increment
        d.data = "12345";

        assertEquals("12345", t.getFieldDataByColumnName("dataColumn"));

        d.create();
        assertEquals("INSERT INTO Table1 (dataColumn, id) VALUES (?, ?);", debugSql.get(d));

        d.data = "123";
        d.save();
        assertEquals("UPDATE Table1 SET dataColumn = ? WHERE (id = ?);", debugSql.get(d));

        d.countAll();
        assertEquals("SELECT Count(*) FROM Table1;", debugSql.get(d));

        d.deleteAll();
        assertEquals("DELETE FROM Table1;", debugSql.get(d));

        d.delete();
        assertEquals("DELETE FROM Table1 WHERE (id = ?);", debugSql.get(d));

        d.load();
        assertEquals("SELECT * FROM Table1 WHERE (id = ?);", debugSql.get(d));

        d.loadAll();
        assertEquals("SELECT * FROM Table1;", debugSql.get(d));
    }

    @Test
    public void testMySql() throws IOException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/mysql", "root", "");
        conn.createStatement().execute("DROP SCHEMA IF EXISTS `test2`;");
        conn.createStatement().execute("CREATE SCHEMA `test2`;");
        conn.createStatement().execute("USE `test2`;");
        conn.createStatement().execute("DROP TABLE IF EXISTS `table1`;");
        conn.createStatement().execute("CREATE TABLE `table1` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `dataColumn` varchar(45) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;");
        conn.createStatement().execute("ALTER TABLE table1 AUTO_INCREMENT = 1;");
        conn.createStatement().execute("CREATE TABLE `test2`.`table2` (\n" +
                "  `id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `data1` VARCHAR(45) NULL,\n" +
                "  `data2` VARCHAR(45) NULL,\n" +
                "  `data3` VARCHAR(45) NULL,\n" +
                "  `id2` VARCHAR(45) NOT NULL,\n" +
                "  `data4` VARCHAR(45) NULL,\n" +
                "  PRIMARY KEY (`id`, `id2`));\n");
        conn.createStatement().execute("ALTER TABLE table2 AUTO_INCREMENT = 1;");

        MyTestDao d = new MyTestDao();
        d.withConnection(conn);
        d.pkId = 1;
        d.data = "net/dckg/daogenerator";

        d.create();

        d = (MyTestDao) new MyTestDao().withConnection(conn);

        d.pkId = 1;
        d.load();
        assertEquals("net/dckg/daogenerator", d.data);


        assertEquals(new Integer(1), new Integer(d.countAll()));


        d.pkId = null;
        d.data = "test2";

        d.create();

        assertEquals(new Integer(2), new Integer(d.countAll()));

        List<MyTestDao> myList = (List<MyTestDao>)(List<?>) d.loadAll();
        //Collections.sort(myList);
        assertEquals(2, myList.size());
        assertEquals( new Integer(1), myList.get(0).pkId);
        assertEquals("net/dckg/daogenerator", myList.get(0).data);

        assertEquals( new Integer(2), myList.get(1).pkId);
        assertEquals( "test2", myList.get(1).data);

        d.pkId = 2;
        d.delete();

        d.pkId = 1;
        d.load();

        assertEquals("net/dckg/daogenerator", d.data);

        assertEquals(new Integer(1), new Integer(d.countAll()));

        d.data = "updated";

        d.save();

        d = null;

        d = (MyTestDao) new MyTestDao().withConnection(conn);

        d.pkId = 1;
        d.load();

        assertEquals("updated", d.data);

        d.deleteAll();

        assertEquals(0, d.countAll());



        MyTestDao2 d2 = (MyTestDao2) (new MyTestDao2()).withConnection(conn);
        boolean exceptionThrown = false;
        try {
            d2.create();
        }
        catch (SQLException e) { // com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Column 'id2' cannot be null
            exceptionThrown = true;
        }
        assertEquals(true, exceptionThrown);
        exceptionThrown = false;

        d2.extra = 0;
        d2.create();
        d2.id = 1;
        d2.load();
        try {
            //d2.id = 1;
            d2.create();
        } catch (SQLException e) {
            exceptionThrown = true;
        }

        assertEquals(true, exceptionThrown);

        d2.myData1 = "a";
        d2.save();

        d2.myData1 = null;
        d2.load();
        assertEquals("a", d2.myData1);

        d2.myData2 = "b";
        d2.myData3 = "c";
        d2.myData4 = "d";

        d2.extra = 50;
        exceptionThrown = false;
        try {
            d2.save();
        } catch(NotFoundException e) {
            exceptionThrown = true;
        }
        assertEquals(true, exceptionThrown);
        d2.extra = 0;
        d2.save();
        d2.load();

        assertEquals("a", d2.myData1);
        assertEquals("b", d2.myData2);
        assertEquals("c", d2.myData3);
        assertEquals("d", d2.myData4);
        assertEquals(Integer.valueOf(0), d2.extra);

        d2.extra = 50;
        d2.id = null;
        d2.myData1 = "a2";
        d2.myData2 = "b2";
        d2.myData3 = "c2";
        d2.myData4 = "d2";
        d2.create();

        MyTestDao2 d3 = (MyTestDao2) (new MyTestDao2()).withConnection(conn);

        exceptionThrown = false;
        try {
            d3.load();
        } catch (NotFoundException e) {
            exceptionThrown = true;
        }
        assertEquals(true, exceptionThrown);
        d3.id = 2;
        d3.extra = 50;
        d3.load();

        List<MyTestDao2> list = (List<MyTestDao2>) (List<?>) d3.loadAll();
        list.get(0).withConnection(conn).delete();
        assertEquals("a", list.get(0).myData1);
        assertEquals("b", list.get(0).myData2);
        assertEquals("c", list.get(0).myData3);
        assertEquals("d", list.get(0).myData4);
        assertEquals(Integer.valueOf(1), list.get(0).id);
        assertEquals(Integer.valueOf(0), list.get(0).extra);

        assertEquals(1, list.get(0).countAll());
        list.get(1).withConnection(conn).delete();
        assertEquals(0, list.get(0).countAll());

        assertEquals("a2", list.get(1).myData1);
        assertEquals("b2", list.get(1).myData2);
        assertEquals("c2", list.get(1).myData3);
        assertEquals("d2", list.get(1).myData4);
        assertEquals(Integer.valueOf(2), list.get(1).id);
        assertEquals(Integer.valueOf(50), list.get(1).extra);
    }

    @Test
    public void testSqlite() throws IOException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        testSqlite(conn);
    }

    public void testSqlite(Connection conn) throws IOException, SQLException {
        // import database
        Statement stmt = conn.createStatement();
//        stmt.execute(new String(Files.readAllBytes(Paths.get("./src/test/resources/MyTest.sql"))));
//        stmt.executeUpdate(new String(Files.readAllBytes(Paths.get("./src/test/resources/MyTest.sql"))));

        stmt.execute("CREATE TABLE `Table1` (\n" +
                "\t`id`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                "\t`dataColumn`\tTEXT\n" +
                ");");

        stmt.executeUpdate("INSERT INTO `Table1`(`id`,`dataColumn`) VALUES (1,'test');");
    }

}





















