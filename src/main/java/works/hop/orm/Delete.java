package works.hop.orm;

import works.hop.generate.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Delete {

    public static <T extends Entity> int delete(String targetTable, Object[][] criteria) throws SQLException {
        try (Connection conn = Connect.connect()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateDeleteQuery(targetTable, criteria);
            System.out.printf("Execute query - %s%n", query);
            try (PreparedStatement prep = conn.prepareStatement(query)) {
                for (int i = 0; i < criteria.length; i++) {
                    prep.setObject(i + 1, criteria[i][1]);
                }

                //execute query
                return prep.executeUpdate();
            }
        }
    }

    private static String generateDeleteQuery(String targetTable, Object[][] where) {
        StringBuilder query = new StringBuilder("delete from ").append(targetTable);
        query.append(" where ");
        int k = 0;
        for (Object[] criteria : where) {
            String colName = (String) criteria[0];
            query.append(colName).append("=?");
            if (k < where.length - 1) {
                query.append(",");
            }
            k++;
        }

        return query.toString();
    }

    public static void main(String[] args) throws SQLException {
        String accountsTable = "tbl_account";

        Object[][] criteria = new Object[][]{
                new Object[]{"id", 7}
        };

        String updateAddress = generateDeleteQuery(accountsTable, criteria);
        System.out.printf("query -> %s%n", updateAddress);
        int rows4 = delete(accountsTable, criteria);
        System.out.printf("Rows affected: %d%n", rows4);
    }
}
