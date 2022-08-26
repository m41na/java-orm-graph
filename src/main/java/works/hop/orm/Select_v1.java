package works.hop.orm;

import java.sql.*;
import java.util.List;

public class Select_v1 {

    public static void selectAll(String targetTable) {
        try (Connection conn = Connect.connect();
             Statement stmt = conn.createStatement()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateQuery(targetTable, columns, new Object[0][]);
            System.out.printf("Execute query - %s%n", query);
            try (ResultSet rs = stmt.executeQuery(query)) {
                // loop through the result set
                while (rs.next()) {
                    System.out.println("Column values");
                    for (Col col : columns) {
                        System.out.printf("%s - %s%n", col.name, rs.getObject(col.name));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void selectWhere(String targetTable, Object[][] criteria) {
        try (Connection conn = Connect.connect()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateQuery(targetTable, columns, criteria);
            System.out.printf("Execute query - %s%n", query);
            try (PreparedStatement prep = conn.prepareStatement(query)) {
                for (int i = 0; i < criteria.length; i++) {
                    prep.setObject(i + 1, criteria[i][1]);
                }

                try (ResultSet rs = prep.executeQuery()) {
                    // loop through the result set
                    while (rs.next()) {
                        System.out.println("Column values");
                        for (Col col : columns) {
                            System.out.printf("%s - %s%n", col.name, rs.getObject(col.name));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String generateQuery(String targetTable, List<Col> columns, Object[][] criteria) {
        StringBuilder query = new StringBuilder("select ");
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i).name;
            query.append(colName);
            if (i < columns.size() - 1) {
                query.append(",");
            }
        }

        query.append(" from ").append(targetTable);
        if (criteria.length > 0) {
            query.append(" where ");
            for (int i = 0; i < criteria.length; i++) {
                Object colName = criteria[i][0];
                query.append(colName).append("=?");
                if (i < criteria.length - 1) {
                    query.append(" and ");
                }
            }
        }
        return query.toString();
    }

    public static void main(String[] args) {
        selectAll("tbl_address");
        Object[][] criteria = new Object[1][];
        criteria[0] = new Object[]{"id", 5};
        selectWhere("tbl_address", criteria);
        criteria[0] = new Object[]{"id", 1};
        selectWhere("tbl_seller", criteria);
    }
}
