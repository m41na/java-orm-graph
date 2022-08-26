package works.hop.orm;

import works.hop.generate.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Update {

    public static <T extends Entity> int update(String targetTable, Object[][] params, Object[][] criteria) throws SQLException {
        try (Connection conn = Connect.connect()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateUpdateQuery(targetTable, columns, params, criteria);
            System.out.printf("Execute query - %s%n", query);
            try (PreparedStatement prep = conn.prepareStatement(query)) {
                int i = 0;
                for (i = 0; i < params.length; i++) {
                    prep.setObject(i + 1, params[i][1]);
                }
                for (int j = 0; j < criteria.length; j++) {
                    prep.setObject(i + 1 + j, criteria[j][1]);
                }

                //execute query
                return prep.executeUpdate();
            }
        }
    }

    private static String generateUpdateQuery(String targetTable, List<Col> columns, Object[][] params, Object[][] where) {
        StringBuilder query = new StringBuilder("update ").append(targetTable).append(" set ");
        List<String> queryColumns = Stream.of(params).map(arr -> (String) arr[0]).collect(Collectors.toList());
        Map<String, Col> targetColumns = new LinkedHashMap<>();
        for (String param : queryColumns) {
            Optional<Col> colOption = columns.stream().filter(c -> c.name.equals(param)).findAny();
            if (colOption.isPresent()) {
                Col col = colOption.get();
                targetColumns.put(col.name, col);
            } else {
                throw new RuntimeException(String.format("Column '%s' is not defined in table '%s'", param, targetTable));
            }
        }

        int l = 0;
        for (Col col : targetColumns.values()) {
            String colName = col.name;
            query.append(colName).append("=?");
            if (l < targetColumns.size() - 1) {
                query.append(",");
            }
            l++;
        }

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
        List<Col> columns = Schema.columns(accountsTable);

        Object[][] where = new Object[][]{
                new Object[]{"id", 11}
        };

        Object[][] data4 = new Object[][]{
                new Object[]{"username", "Dreje"},
                new Object[]{"password", "dreje-2"}
        };

        String updateAddress = generateUpdateQuery(accountsTable, columns, data4, where);
        System.out.printf("query -> %s%n", updateAddress);
        int rows4 = update(accountsTable, data4, where);
        System.out.printf("Rows affected: %d%n", rows4);
    }
}
