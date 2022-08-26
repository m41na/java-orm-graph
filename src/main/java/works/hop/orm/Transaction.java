package works.hop.orm;

import java.sql.Connection;

public interface Transaction {

    void execute(Connection conn);
}
