package works.hop.orm;

import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.*;

public class Connect {

    //before anything else, initialize app config class
    static {
        try {
            AppConfig.load("app.yaml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static HikariDataSource db = AppConfig.ds;

    public static void establishConnection() {
        Connection conn = null;
        try {
            // create a connection to the database
            conn = db.getConnection();

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:./data/" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTable(String tableDefinition) {

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(tableDefinition);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = db.getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static Transact transact() {
        return new Transact();
    }

    public static void main(String[] args) {
        establishConnection();
    }
}
