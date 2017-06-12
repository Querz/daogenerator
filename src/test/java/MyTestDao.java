import net.dckg.daogenerator.Column;
import net.dckg.daogenerator.Dao;
import net.dckg.daogenerator.Key;
import net.dckg.daogenerator.Table;

import java.sql.Types;

@Table(name="Table1")
public class MyTestDao extends Dao implements Comparable<MyTestDao> {

    @Column(name = "id", type = Types.INTEGER)
    @Key()
    public Integer pkId;

    @Column(name = "dataColumn", type = Types.VARCHAR)
    public String data;

    @Override
    public int compareTo(MyTestDao o) {
        return pkId.compareTo(o.pkId);
    }
}
