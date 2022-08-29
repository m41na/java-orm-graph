package works.hop.orm;

import java.sql.Connection;
import java.sql.SQLException;

public class Transact {

    final Object sync = new Object();
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
        synchronized (sync) {
            if (!started) {
                conn = Connect.connect();
                conn.setAutoCommit(false);
                started = true;
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

    void commit() throws SQLException {
        if (started && conn != null) {
            conn.commit();
            started = false;
        }
    }

    void rollback() throws SQLException {
        if (started && conn != null) {
            conn.rollback();
            started = false;
        }
    }

    void close() {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to successfully close database connection", e);
        }
    }
}
