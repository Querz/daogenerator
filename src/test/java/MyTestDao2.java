import net.dckg.daogenerator.Column;
import net.dckg.daogenerator.Dao;
import net.dckg.daogenerator.Key;
import net.dckg.daogenerator.Table;

import java.sql.Types;

@Table(name="table2")
public class MyTestDao2 extends Dao {

    @Column(name = "id", type= Types.INTEGER)
    @Key
    public Integer id;

    @Column(name = "data1", type= Types.VARCHAR)
    public String myData1;

    @Column(name = "data2", type= Types.VARCHAR)
    public String myData2;

    @Column(name = "data3", type= Types.VARCHAR)
    public String myData3;

    @Key
    @Column(name = "id2", type= Types.INTEGER)
    public Integer extra;

    @Column(name = "data4", type= Types.VARCHAR)
    public String myData4;
}
