package works.hop.orm;

import works.hop.graph.Adjacency;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Schema {

    public static final String DATA_TABLES = "TABLE";
    public static final String SYSTEM_TABLES = "SYSTEM TABLE";

    public static List<String> tables(String tableType) {
        try (Connection connection = Connect.connect()) {
            List<String> tableNames = new ArrayList<>();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{tableType})) {
                System.out.printf("Printing TABLE_TYPE \"%s\" %n", tableType);
                System.out.println("----------------------------------");
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString("TABLE_NAME"));
                }
            }
            return tableNames;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Col> columns(String tableName) {
        try (Connection connection = Connect.connect()) {
            List<Col> tableColumns = new ArrayList<>();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            //Review: https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html
            try (ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String typeName = columns.getString("TYPE_NAME");
                    String ordinal = columns.getString("ORDINAL_POSITION");
                    String isNullable = columns.getString("IS_NULLABLE");
                    String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
                    Col col = new Col(
                            columnName,
                            typeName,
                            Integer.parseInt(ordinal),
                            isNullable.equalsIgnoreCase("YES"),
                            isAutoIncrement.equalsIgnoreCase("YES"));
                    tableColumns.add(col);
                }
            }
            return tableColumns;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<PkCol> pkColumns(String tableName) {
        try (Connection connection = Connect.connect()) {
            List<PkCol> pks = new ArrayList<>();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet PK = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
                System.out.println("------------PRIMARY KEYS-------------");
                while (PK.next()) {
                    String name = PK.getString("COLUMN_NAME");
                    String pkName = PK.getString("PK_NAME");
                    String keySeq = PK.getString("KEY_SEQ");
                    PkCol pk = new PkCol(name, pkName, Integer.parseInt(keySeq));
                    pks.add(pk);
                }
            }
            return pks;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<FkCol> fkColumns(String tableName) {
        try (Connection connection = Connect.connect()) {
            List<FkCol> fks = new ArrayList<>();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet FK = databaseMetaData.getImportedKeys(null, null, tableName)) {
                System.out.println("------------FOREIGN KEYS-------------");
                while (FK.next()) {
                    String fkTable = FK.getString("FKTABLE_NAME");
                    String fkColumn = FK.getString("FKCOLUMN_NAME");
                    String fkName = FK.getString("FK_NAME");
                    String pkTable =  FK.getString("PKTABLE_NAME");
                    String pkColumn = FK.getString("PKCOLUMN_NAME");
                    FkCol fk = new FkCol(fkColumn, fkTable, fkName, pkColumn, pkTable);
                    fks.add(fk);
                }
            }
            return fks;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<IdxCol> indexed(String tableName) {
        try (Connection connection = Connect.connect()) {
            List<IdxCol> idxCols = new ArrayList<>();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet IDX = databaseMetaData.getIndexInfo(null, null, tableName, true, true);
                 ResultSet PK = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
                System.out.println("------------INDEXED COLUMNS-------------");
                while (IDX.next()) {
                    if(PK.next() && PK.getString("COLUMN_NAME").equals(IDX.getString("COLUMN_NAME"))) {
                        //ignore column if it's a pk
                        continue;
                    }
                    String name = IDX.getString("INDEX_NAME");
                    String column = IDX.getString("COLUMN_NAME");
                    String ordinal = IDX.getString("ORDINAL_POSITION");
                    IdxCol idx = new IdxCol(
                            name,
                            column,
                            Integer.parseInt(ordinal)
                    );
                    idxCols.add(idx);
                }
            }
            return idxCols;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> orderedTables(String tableType) {
        List<String> tables = tables(tableType);
        Adjacency<String> graph = new Adjacency<>();
        for(String table : tables){
            graph.add(table);
            List<FkCol> fks = fkColumns(table);
            for(FkCol fk : fks){
                if(tables.contains(fk.pkTable)){
                    graph.addChild(table, fk.pkTable);
                }
            }

        }
        return Adjacency.topSort(graph);
    }

    public static void main(String[] args) {
        for (String tableName : tables(DATA_TABLES)) {
            System.out.printf("Table name - %s%n", tableName);
            System.out.println("table columns");
            columns(tableName).forEach(col -> System.out.printf("%s%n", col));
            System.out.println("table pk columns");
            pkColumns(tableName).forEach(col -> System.out.printf("%s%n", col));
            System.out.println("table fk columns");
            fkColumns(tableName).forEach(col -> System.out.printf("%s%n", col));
            System.out.println("table idx columns");
            indexed(tableName).forEach(col -> System.out.printf("%s%n", col));
        }
        tables(SYSTEM_TABLES);
    }
}
