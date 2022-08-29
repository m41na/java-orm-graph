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

public class Insert_v1 {

    public static <T extends Entity> int upsert(String targetTable, Object[][] criteria) throws SQLException {
        try (Connection conn = Connect.connect()) {
            List<Col> columns = Schema.columns(targetTable);
            List<IdxCol> uniques = Schema.indexed(targetTable);

            String query = generateUpsertQuery(targetTable, columns, uniques, criteria);
            System.out.printf("Execute query - %s%n", query);
            try (PreparedStatement prep = conn.prepareStatement(query)) {
                for (int i = 0; i < criteria.length; i++) {
                    prep.setObject(i + 1, criteria[i][1]);
                    prep.setObject(i + 1 + criteria.length, criteria[i][1]);
                }

                //execute query
                return prep.executeUpdate();
            }
        }
    }

    public static <T extends Entity> int insert(String targetTable, Object[][] criteria) throws SQLException {
        try (Connection conn = Connect.connect()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateInsertQuery(targetTable, columns, criteria);
            System.out.printf("Execute query - %s%n", query);
            try (PreparedStatement prep = conn.prepareStatement(query)) {
                for (int i = 0; i < criteria.length; i++) {
                    prep.setObject(i + 1, criteria[i][1]);
                    prep.setObject(i + 1 + criteria.length, criteria[i][1]);
                }

                //execute query
                return prep.executeUpdate();
            }
        }
    }

    private static String generateUpsertQuery(String targetTable, List<Col> columns, List<IdxCol> uniques, Object[][] params) {
        StringBuilder query = new StringBuilder("insert into ").append(targetTable).append(" (");
        List<String> queryColumns = Stream.of(params).map(arr -> (String) arr[0]).collect(Collectors.toList());
        Map<String, Col> targetColumns = new LinkedHashMap<>();
        for(String param : queryColumns){
            Optional<Col> colOption = columns.stream().filter(c -> c.name.equals(param)).findAny();
            if(colOption.isPresent()) {
                Col col = colOption.get();
                targetColumns.put(col.name, col);
            }
            else{
                throw new RuntimeException(String.format("Column '%s' is not defined in table '%s'", param, targetTable));
            }
        }

        int i = 0;
        for (Col col : targetColumns.values()) {
            if (!col.autoIncr) {
                String colName = col.name;
                query.append(colName);
                if (i < targetColumns.size() - 1) {
                    query.append(",");
                }
            }
            i++;
        }

        query.append(") values (");
        for (int j = 0; j < targetColumns.size(); j++) {
            query.append("?");
            if (j < targetColumns.size() - 1) {
                query.append(",");
            }
        }

        query.append(") on conflict (");
        for (int k = 0; k < uniques.size(); k++) {
            IdxCol idx = uniques.get(k);
            String colName = idx.column;
            query.append(colName);
            if (k < uniques.size() - 1) {
                query.append(",");
            }
        }

        query.append(") do update set ");
        int l = 0;
        for (Col col : targetColumns.values()) {
            String colName = col.name;
            query.append(colName).append("=?");
            if (l < targetColumns.size() - 1) {
                query.append(",");
            }
            l++;
        }
        return query.toString();
    }

    private static String generateInsertQuery(String targetTable, List<Col> columns, Object[][] params) {
        StringBuilder query = new StringBuilder("insert into ");
        query.append(targetTable).append(" (");
        List<String> queryColumns = Stream.of(params).map(arr -> (String) arr[0]).collect(Collectors.toList());
        Map<String, Col> targetColumns = new LinkedHashMap<>();
        for(String param : queryColumns){
            Optional<Col> colOption = columns.stream().filter(c -> c.name.equals(param)).findAny();
            if(colOption.isPresent()) {
                Col col = colOption.get();
                targetColumns.put(col.name, col);
            }
            else{
                throw new RuntimeException(String.format("Column '%s' is not defined in table '%s'", param, targetTable));
            }
        }

        int i = 0;
        for (Col col : targetColumns.values()) {
            if (!col.autoIncr) {
                String colName = col.name;
                query.append(colName);
                if (i < targetColumns.size() - 1) {
                    query.append(",");
                }
            }
            i++;
        }

        query.append(") select ");
        for (int j = 0; j < targetColumns.size(); j++) {
            query.append("?");
            if (j < targetColumns.size() - 1) {
                query.append(",");
            }
        }

        query.append(" where not exists (select * from ").append(targetTable).append(" where ");

        int k = 0;
        for (Col col : targetColumns.values()) {
            String colName = col.name;
            query.append(colName).append("=?");
            if (k < targetColumns.size() - 1) {
                query.append(" and ");
            }
            k++;
        }

        return query.append(")").toString();
    }

    public static void main(String[] args) throws SQLException {
        String addressTable = "tbl_address";
        List<Col> addrColumns = Schema.columns(addressTable);

        Object[][] data1 = new Object[][]{
                new Object[]{"street_name", "Daraja tatuv"},
                new Object[]{"city", "Madison"},
                new Object[]{"state", "WI"},
                new Object[]{"zip_code", "53718"},
        };
        String insertAddress = generateInsertQuery(addressTable, addrColumns, data1);
        System.out.printf("query -> %s%n", insertAddress);
        int rows1 = Insert_v1.insert(addressTable, data1);
        System.out.printf("Rows affected: %d%n", rows1);

//        String accountsTable = "tbl_account";
//        List<Col> accColumns = Schema.columns(accountsTable);
//        List<IdxCol> accUniques = Schema.indexed(accountsTable);
//
//        Object[][] data2 = new Object[][]{
//                new Object[]{"username", "Darajam"},
//                new Object[]{"password", "daraja-1"},
//                new Object[]{"date_created", LocalDateTime.now()},
//        };
//        String upsertAccount = generateUpsertQuery(accountsTable, accColumns, accUniques, data2);
//        System.out.printf("query -> %s%n", upsertAccount);
//        int rows2 = Insert.upsert(accountsTable, data2);
//        System.out.printf("Rows affected: %d%n", rows2);
//
//        String sellersTable = "tbl_seller";
//        List<Col> sellerColumns = Schema.columns(sellersTable);
//        List<IdxCol> sellerUniques = Schema.indexed(sellersTable);
//
//        Object[][] data3 = new Object[][]{
//                new Object[]{"first_name", "jina"},
//                new Object[]{"last_name", "mwisho"},
//                new Object[]{"email_address", "jina@email.com"},
//                new Object[]{"seller_address", 24},
//                new Object[]{"billing_address", 6},
//                new Object[]{"seller_account", 1},
//                new Object[]{"date_created", LocalDateTime.now()},
//        };
//        String upsertSeller = generateUpsertQuery(sellersTable, sellerColumns, sellerUniques, data3);
//        System.out.printf("query -> %s%n", upsertSeller);
//        int rows3 = Insert.upsert(sellersTable, data3);
//        System.out.printf("Rows affected: %d%n", rows3);
    }
}
