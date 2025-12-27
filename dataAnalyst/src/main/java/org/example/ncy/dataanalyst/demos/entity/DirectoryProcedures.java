package org.example.ncy.dataanalyst.demos.entity;

import lombok.extern.slf4j.Slf4j;
import java.sql.*;

@Slf4j
public class DirectoryProcedures {

    /**
     * 查询目录树
     */
    public static ResultSet getDirectoryTree(Connection conn, Long directoryId) throws SQLException {
        String sql =
                "WITH RECURSIVE directory_tree(id, name, fatherid, level) AS (" +
                        "    SELECT id, translate, fatherid, 1 FROM directorytype WHERE id = ?" +
                        "    UNION ALL" +
                        "    SELECT f.id, f.translate, f.fatherid, dt.level + 1 " +
                        "    FROM directorytype f" +
                        "    INNER JOIN directory_tree dt ON f.fatherid = dt.id" +
                        ")" +
                        "SELECT id, name, fatherid, level FROM directory_tree ORDER BY level, id";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, directoryId);
        return stmt.executeQuery();
    }

    /**
     * 递归删除目录
     */
    public static int deleteDirectoryRecursive(Connection conn, Long directoryId) throws SQLException {
        // 使用WITH RECURSIVE查询所有子目录ID
        String recursiveSql =
                "WITH RECURSIVE sub_dirs(id) AS (" +
                        "    SELECT id FROM directorytype WHERE id = ?" +
                        "    UNION ALL" +
                        "    SELECT f.id FROM directorytype f" +
                        "    INNER JOIN sub_dirs sd ON f.fatherid = sd.id" +
                        ")" +
                        "SELECT id FROM sub_dirs";

        PreparedStatement selectStmt = conn.prepareStatement(recursiveSql);
        selectStmt.setLong(1, directoryId);
        ResultSet rs = selectStmt.executeQuery();

        // 收集所有ID
        StringBuilder idList = new StringBuilder();
        while (rs.next()) {
            if (idList.length() > 0) idList.append(",");
            idList.append(rs.getLong("id"));
        }
        rs.close();
        selectStmt.close();

        if (idList.length() == 0) {
            return 0;
        }

        // 执行删除
        String deleteSql = "DELETE FROM directorytype WHERE id IN (" + idList + ")";
        Statement deleteStmt = conn.createStatement();
        int result = deleteStmt.executeUpdate(deleteSql);
        deleteStmt.close();

        return result;
    }

    /**
     * 获取目录信息
     */
    public static ResultSet getDirectoryInfo(Connection conn, Long directoryId) throws SQLException {
        String sql = "SELECT id, translate, fatherid, created_time FROM directorytype WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, directoryId);
        return stmt.executeQuery();
    }

    /**
     * 统计目录信息
     */
    public static ResultSet countDirectories(Connection conn) throws SQLException {
        String sql =
                "SELECT " +
                        "    COUNT(*) as total_count, " +
                        "    COUNT(CASE WHEN fatherid IS NULL THEN 1 END) as root_count, " +
                        "    COUNT(DISTINCT fatherid) as parent_count " +
                        "FROM directorytype";

        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * 搜索目录
     */
    public static ResultSet searchDirectories(Connection conn, String keyword) throws SQLException {
        String sql =
                "SELECT id, translate, fatherid " +
                        "FROM directorytype " +
                        "WHERE translate LIKE ? " +
                        "ORDER BY translate";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + keyword + "%");
        return stmt.executeQuery();
    }
}
