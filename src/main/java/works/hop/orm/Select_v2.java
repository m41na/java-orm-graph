package works.hop.orm;

import works.hop.generate.Entity;
import works.hop.generate.EntityGen;
import works.hop.generate.TypeResolver;
import works.hop.parser.EntityNode;
import works.hop.parser.FieldNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Select_v2 {

    public static <T extends Entity> List<T> selectAll(String targetTable, EntityNode mapping) {
        try (Connection conn = Connect.connect();
             Statement stmt = conn.createStatement()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateQuery(targetTable, columns, new Object[0][]);
            System.out.printf("Execute query - %s%n", query);
            try (ResultSet rs = stmt.executeQuery(query)) {
                List<T> result = new ArrayList<>();
                // loop through the result set
                while (rs.next()) {
                    T entity = Entity.instance(TypeResolver.resolveClass(mapping.getNamespace(), mapping.getName()));
                    System.out.println("Column values");
                    for (FieldNode node : mapping.getFields().values()) {
//                        System.out.printf("%s - %s%n", node.getName(), rs.getObject(node.getColumn()));
                        Class<?> typeName = TypeResolver.resolveClass(mapping.getNamespace(), node.getType());
                        //??? TODO check whether node is a foreign key, and if so, then use a different strategy
                        entity.setProperty(node.getName(), typeName, rs.getObject(node.getColumn(), typeName));
                    }
                    result.add(entity);
                }
                return result;
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        return Collections.emptyList();
    }

    public static <T extends Entity> List<T> selectWhere(String targetTable, Object[][] criteria, EntityNode mapping) {
        try (Connection conn = Connect.connect()) {
            List<Col> columns = Schema.columns(targetTable);

            String query = generateQuery(targetTable, columns, criteria);
            System.out.printf("Execute query - %s%n", query);
            try (PreparedStatement prep = conn.prepareStatement(query)) {
                for (int i = 0; i < criteria.length; i++) {
                    prep.setObject(i + 1, criteria[i][1]);
                }

                List<T> result = new ArrayList<>();
                try (ResultSet rs = prep.executeQuery()) {
                    // loop through the result set
                    while (rs.next()) {
                        T entity = Entity.instance(TypeResolver.resolveClass(mapping.getNamespace(), mapping.getName()));
                        System.out.println("Column values");
                        for (FieldNode node : mapping.getFields().values()) {
//                            System.out.printf("%s - %s%n", node.getName(), rs.getObject(node.getType()));
                            Class<?> typeName = TypeResolver.resolveClass(mapping.getNamespace(), node.getType());
                            entity.setProperty(node.getName(), typeName, rs.getObject(node.getColumn(), typeName));
                        }
                        result.add(entity);
                    }
                }
                return result;
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        return Collections.emptyList();
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

    public static void main(String[] args) throws IOException, URISyntaxException {
        Object[][] criteria = new Object[1][];
        EntityGen gen = new EntityGen();
//        EntityNode address = gen.entities().get("tbl_address");
//        selectAll("tbl_address", address).forEach(System.out::println);
//        criteria[0] = new Object[]{"id", 4};
//        selectWhere("tbl_address", criteria, address).forEach(System.out::println);
        EntityNode seller = gen.entities().get("tbl_seller");
        criteria[0] = new Object[]{"id", 2};
        selectWhere("tbl_seller", criteria, seller);
    }
}
