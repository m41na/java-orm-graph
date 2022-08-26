package works.hop.orm;

import works.hop.generate.Entity;
import works.hop.generate.EntityGen;
import works.hop.generate.TypeResolver;
import works.hop.parser.EntityNode;
import works.hop.parser.FieldNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Select_v4 {

    public static <T extends Entity> List<T> selectAll(Connection conn, String targetTable, Map<String, EntityNode> mappings) throws Throwable {
         try(Statement stmt = conn.createStatement()) {
             EntityNode mapping = mappings.get(targetTable);
             List<Col> columns = Schema.columns(targetTable);
             List<FkCol> fks = Schema.fkColumns(targetTable);
             List<String> fkColumns = fks.stream().map(fk -> fk.fkColumn).collect(Collectors.toList());

             String query = generateQuery(targetTable, columns, new Object[0][]);
             System.out.printf("Execute query - %s%n", query);
             try (ResultSet rs = stmt.executeQuery(query)) {
                 List<T> result = new ArrayList<>();
                 // check and loop through the result set
                 while (rs.next()) {
                     T entity = extractRecord(conn, mappings, mapping, fks, fkColumns, rs);
                     result.add(entity);
                 }
                 return result;
             }
         }
    }

    public static <T extends Entity> T selectOneWhere(Connection conn, String targetTable, Object[][] criteria, Map<String, EntityNode> mappings) throws Throwable {
        EntityNode mapping = mappings.get(targetTable);
        List<Col> columns = Schema.columns(targetTable);
        List<FkCol> fks = Schema.fkColumns(targetTable);
        List<String> fkColumns = fks.stream().map(fk -> fk.fkColumn).collect(Collectors.toList());

        String query = generateQuery(targetTable, columns, criteria);
        System.out.printf("Execute query - %s%n", query);
        try (PreparedStatement prep = conn.prepareStatement(query)) {
            for (int i = 0; i < criteria.length; i++) {
                prep.setObject(i + 1, criteria[i][1]);
            }

            try (ResultSet rs = prep.executeQuery()) {
                // check the result set
                if (rs.next()) {
                    return extractRecord(conn, mappings, mapping, fks, fkColumns, rs);
                }
            }
        }
        return null;
    }

    public static <T extends Entity> List<T> selectListWhere(Connection conn, String targetTable, Object[][] criteria, Map<String, EntityNode> mappings) throws Throwable {
        EntityNode mapping = mappings.get(targetTable);
        List<Col> columns = Schema.columns(targetTable);
        List<FkCol> fks = Schema.fkColumns(targetTable);
        List<String> fkColumns = fks.stream().map(fk -> fk.fkColumn).collect(Collectors.toList());

        String query = generateQuery(targetTable, columns, criteria);
        System.out.printf("Execute query - %s%n", query);
        try (PreparedStatement prep = conn.prepareStatement(query)) {
            for (int i = 0; i < criteria.length; i++) {
                prep.setObject(i + 1, criteria[i][1]);
            }

            List<T> result = new ArrayList<>();
            try (ResultSet rs = prep.executeQuery()) {
                // check and loop through the result set
                while (rs.next()) {
                    T entity = extractRecord(conn, mappings, mapping, fks, fkColumns, rs);
                    result.add(entity);
                }
            }
            return result;
        }
    }

    private static <T extends Entity> T extractRecord(Connection conn, Map<String, EntityNode> mappings, EntityNode mapping, List<FkCol> fks, List<String> fkColumns, ResultSet rs) throws Throwable {
        T entity = Entity.instance(TypeResolver.resolveClass(mapping.getNamespace(), mapping.getName()));
        for (FieldNode node : mapping.getFields().values()) {
            if (!fkColumns.contains(node.getColumn())) {
                Class<?> typeName = TypeResolver.resolveClass(mapping.getNamespace(), node.getType());
                entity.setProperty(node.getName(), typeName, rs.getObject(node.getColumn(), typeName));
            } else {
                Optional<FkCol> fkOptional = fks.stream().filter(f -> f.fkColumn.equals(node.getColumn())).findFirst();
                if (fkOptional.isPresent()) {
                    //retrieve pk value
                    FkCol fkCol = fkOptional.get();
                    Object pkValue = rs.getObject(node.getColumn());
                    if (pkValue != null) {
                        //retrieve fk value
                        Object[][] fkCriteria = new Object[1][];
                        fkCriteria[0] = new Object[]{fkCol.pkColumn, pkValue};
                        T fkValue = selectOneWhere(conn, fkCol.pkTable, fkCriteria, mappings);
                        Class<?> typeName = TypeResolver.resolveClass(mapping.getNamespace(), node.getType());
                        entity.setProperty(node.getName(), typeName, fkValue);
                    }
                }
            }
        }
        return entity;
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

    public static void main(String[] args) throws Throwable {
        EntityGen gen = new EntityGen();
        Map<String, EntityNode> mappings = gen.entities();
        Object[][] criteria = new Object[1][];
        try (Connection conn = Connect.connect()) {
            selectAll(conn, "tbl_address", mappings).forEach(System.out::println);
        }
        try (Connection conn = Connect.connect()) {
            criteria[0] = new Object[]{"id", 1};
            Object address = selectOneWhere(conn, "tbl_address", criteria, mappings);
            System.out.println(address);
        }
        try (Connection conn = Connect.connect()) {
            criteria[0] = new Object[]{"id", 2};
            selectListWhere(conn, "tbl_seller", criteria, mappings).forEach(System.out::println);
        }
        try (Connection conn = Connect.connect()) {
            criteria[0] = new Object[]{"item_seller", 2};
            selectListWhere(conn, "tbl_sale_item", criteria, mappings).forEach(System.out::println);
        }
    }
}
