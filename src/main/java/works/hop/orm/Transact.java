package works.hop.orm;

import java.sql.Connection;
import java.sql.SQLException;

public class Transact {

    Connection conn;
    boolean started;

    public Connection connection() {
        if (conn != null) {
            return conn;
        } else {
            throw new RuntimeException("A connection has not been created yet");
        }
    }

    public void start(Transaction operation) throws SQLException {
        synchronized (this) {
            if (!started) {
                conn = Connect.connect();
                conn.setAutoCommit(false);
                try {
                    operation.execute(conn);
                    commit();
                } catch (Throwable e) {
                    rollback();
                } finally {
                    close();
                }
            } else {
                throw new RuntimeException("A transaction is already in progress");
            }
        }
    }

    public void commit() throws SQLException {
        if (started && conn != null) {
            conn.commit();
        }
    }

    public void rollback() throws SQLException {
        if (started && conn != null) {
            conn.rollback();
        }
    }

    public void close() {
        try {
            synchronized (this) {
                if (conn != null) {
                    conn.close();
                    conn = null;
                    started = false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to successfully close database connection", e);
        }
    }
}
