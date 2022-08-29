package works.hop.orm;

import works.hop.entity.Account;
import works.hop.entity.Seller;
import works.hop.generate.Entity;
import works.hop.generate.EntityGen;
import works.hop.generate.TypeResolver;
import works.hop.parser.EntityNode;
import works.hop.parser.FieldNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Upsert {

    public static <T extends Entity> int upsert(Connection conn, T entity, Map<String, EntityNode> mappings) {
        Optional<EntityNode> optNode = mappings.entrySet().stream()
                .filter(f -> String.format("%s.%s", f.getValue().getNamespace(), f.getValue().getName()).equals(entity.getClass().getName()))
                .findAny().map(Map.Entry::getValue);
        if (optNode.isEmpty()) {
            throw new RuntimeException(String.format("No mapping is available for %s", entity.getClass().getName()));
        }

        EntityNode node = optNode.get();
        String targetTable = node.getTable();
        List<Col> columns = Schema.columns(targetTable);
        List<IdxCol> uniques = Schema.indexed(targetTable);

        Object[][] criteria = extractUpsertParams(entity, node);
        String query = generateUpsertQuery(targetTable, columns, uniques, criteria);
        System.out.printf("Execute query - %s%n", query);
        try (PreparedStatement prep = conn.prepareStatement(query)) {
            for (int i = 0; i < criteria.length; i++) {
                Object paramValue = criteria[i][1];
                prep.setObject(i + 1, paramValue);
                prep.setObject(i + 1 + criteria.length, criteria[i][1]);
            }

            //execute query
            return prep.executeUpdate();
        } catch (SQLException e) {
            System.err.printf("%d -> %s%n", e.getErrorCode(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static <T extends Entity> Object[][] extractUpsertParams(T entity, EntityNode node) {
        List<FieldNode> insertable = node.getFields().values().stream()
                .filter(fNode -> {
                    try {
                        return entity.getProperty(
                                fNode.getName(),
                                TypeResolver.resolveClass(node.getNamespace(), fNode.getType())
                        ) != null;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        Object[][] params = new Object[insertable.size()][];
        int i = 0;
        for (FieldNode fNode : insertable) {
            try {
                String fieldName = fNode.getName();
                Class<?> fieldType = TypeResolver.resolveClass(node.getNamespace(), fNode.getType());
                Object fieldValue = entity.getProperty(fieldName, fieldType);
                params[i++] = new Object[]{fNode.getColumn(), fieldValue};
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return params;
    }

    public static <T extends Entity> int upsert(Connection conn, String targetTable, Object[][] criteria) {
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
        } catch (SQLException e) {
            System.err.printf("%d -> %s%n", e.getErrorCode(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String generateUpsertQuery(String targetTable, List<Col> columns, List<IdxCol> uniques, Object[][] params) {
        StringBuilder query = new StringBuilder("insert into ").append(targetTable).append(" (");
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

        for(Iterator<String> iter = targetColumns.keySet().iterator(); iter.hasNext();){
            Col col = targetColumns.get(iter.next());
//            if (!col.autoIncr) {
                String colName = col.name;
                query.append(colName);
//            }
//            else{
//                iter.remove();
//            }
            //add comma if necessary
            if (iter.hasNext()) {
                query.append(",");
            }
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
            if (params[l][1] != null) {
                query.append(colName).append("=?");
            } else {
                query.append(colName).append(" is ?");
            }
            if (l < targetColumns.size() - 1) {
                query.append(",");
            }
            l++;
        }
        return query.toString();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException {
        Transact tx = Connect.transact();

        String accountsTable = "tbl_account";
//        List<Col> accColumns = Schema.columns(accountsTable);
//        List<IdxCol> accUniques = Schema.indexed(accountsTable);
//
//        Object[][] data2 = new Object[][]{
//                new Object[]{"username", "Darajam"},
//                new Object[]{"password", "daraja-1"},
//                new Object[]{"date_created", LocalDateTime.now()},
//        };
//        String upsertAccount = generateUpsertQuery(accountsTable, accColumns, accUniques, data2);
//        System.out.printf("generated query -> %s%n", upsertAccount);
//
//        tx.start(conn -> {
//            int rows2 = upsert(conn, accountsTable, data2);
//            System.out.printf("Rows affected: %d%n", rows2);
//        });
//
//        String sellersTable = "tbl_seller";
//        List<Col> sellerColumns = Schema.columns(sellersTable);
//        List<IdxCol> sellerUniques = Schema.indexed(sellersTable);
//
//        Object[][] data3 = new Object[][]{
//                new Object[]{"first_name", "jina"},
//                new Object[]{"last_name", "mwisho"},
//                new Object[]{"email_address", "jina@email.com"},
//                new Object[]{"seller_address", 10},
//                new Object[]{"billing_address", 6},
//                new Object[]{"seller_account", 1},
//                new Object[]{"date_created", LocalDateTime.now()},
//        };
//        String upsertSeller = generateUpsertQuery(sellersTable, sellerColumns, sellerUniques, data3);
//        System.out.printf("query -> %s%n", upsertSeller);
//
//        tx.start(conn -> {
//            int rows3 = Insert.upsert(conn, sellersTable, data3);
//            System.out.printf("Rows affected: %d%n", rows3);
//        });

        EntityGen gen = new EntityGen();
        Map<String, EntityNode> mappings = gen.entities();

        Account account = new Account();
        account.setId(7);
        account.setUsername("Daraja tatuv");
        account.setPassword("Madison");
        account.setDateCreated(LocalDateTime.now());

        tx.start(conn -> {
            int rows4 = upsert(conn, account, mappings);
            System.out.printf("rows affected -> %d%n", rows4);
        });

//        Seller seller = new Seller();
//        seller.setFirstName("jim");
//        seller.setLastName("bob");
//        seller.setEmailAddress("jim.bob@email.com");
//        seller.setSellerAccount(account);
    }
}
