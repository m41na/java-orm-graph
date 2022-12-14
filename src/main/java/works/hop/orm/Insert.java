package works.hop.orm;

import works.hop.entity.Address;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Insert {

    public static <T extends Entity> int insert(Connection conn, T entity, Map<String, EntityNode> mappings) {
        Optional<EntityNode> optNode = mappings.entrySet().stream()
                .filter(f -> String.format("%s.%s", f.getValue().getNamespace(), f.getValue().getName()).equals(entity.getClass().getName()))
                .findAny().map(Map.Entry::getValue);
        if (optNode.isEmpty()) {
            throw new RuntimeException(String.format("No mapping is available for %s", entity.getClass().getName()));
        }
        EntityNode node = optNode.get();
        String targetTable = node.getTable();
        List<Col> columns = Schema.columns(targetTable);

        try {
            List<String> pks = Schema.pkColumns(targetTable).stream().map(p -> p.name).collect(Collectors.toList());
            for (FieldNode fn : node.getFields().values()) {
                if (pks.contains(fn.getColumn()) &&
                        entity.getProperty(fn.getName(), TypeResolver.resolveClass(node.getNamespace(), fn.getType())) != null) {
                    throw new RuntimeException(String.format("Expected %s to be have no value", fn.getName()));
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        Object[][] criteria = extractInsertParams(entity, node);
        String query = generateInsertQuery(targetTable, columns, criteria);
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

    private static <T extends Entity> Object[][] extractInsertParams(T entity, EntityNode node) {
        List<String> pks = Schema.pkColumns(node.getTable()).stream()
                .map(pk -> pk.name)
                .collect(Collectors.toList());
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
                .filter(fNode -> !pks.contains(fNode.getColumn()))
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

    public static <T extends Entity> int insert(Connection conn, String targetTable, Object[][] criteria) {
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
        } catch (SQLException e) {
            System.err.printf("%d -> %s%n", e.getErrorCode(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String generateInsertQuery(String targetTable, List<Col> columns, Object[][] params) {
        StringBuilder query = new StringBuilder("insert into ");
        query.append(targetTable).append(" (");
        List<String> queryColumns = Stream.of(params)
                .filter(Objects::nonNull)
                .filter(arr -> arr[0] != null && arr[1] != null)
                .map(arr -> (String) arr[0]).collect(Collectors.toList());
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
            if (params[k][1] != null) {
                query.append(colName).append("=?");
            } else {
                query.append(colName).append(" is ?");
            }
            if (k < targetColumns.size() - 1) {
                query.append(" and ");
            }
            k++;
        }

        return query.append(")").toString();
    }

    public static void main(String[] args) throws SQLException, IOException, URISyntaxException {
//        String addressTable = "tbl_address";
//        List<Col> addrColumns = Schema.columns(addressTable);
//
//        Object[][] data1 = new Object[][]{
//                new Object[]{"street_name", "Daraja tatuv"},
//                new Object[]{"city", "Madison"},
//                new Object[]{"state", "WI"},
//                new Object[]{"zip_code", "53718"},
//        };
//        String insertAddress = generateInsertQuery(addressTable, addrColumns, data1);
//        System.out.printf("generated query -> %s%n", insertAddress);
//
        Transact tx = Connect.transact();
//        tx.start(conn -> {
//            int rows1 = insert(conn, addressTable, data1);
//            System.out.printf("Rows affected: %d%n", rows1);
//        });
//

        EntityGen gen = new EntityGen();
        Map<String, EntityNode> mappings = gen.entities();

        Address address = new Address();
        address.setStreetName("Daraja tatuv");
        address.setCity("Madison");
        address.setState("WI");
        address.setZipCode("53718");

        tx.start(conn -> {
            int rows4 = insert(conn, address, mappings);
            System.out.printf("rows affected -> %d%n", rows4);
        });
    }
}
