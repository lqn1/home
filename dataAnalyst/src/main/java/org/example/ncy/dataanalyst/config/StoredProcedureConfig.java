package org.example.ncy.dataanalyst.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class StoredProcedureConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 应用启动后创建存储过程
     */
    @PostConstruct
    public void createStoredProcedures() {
        try {
            log.info("开始创建H2存储过程...");

            // 创建查询目录树的存储过程
            createGetDirectoryTreeProcedure();

            // 创建删除目录的存储过程
            createDeleteDirectoryProcedure();

            // 创建其他存储过程
            createUtilityProcedures();

            log.info("H2存储过程创建完成");

        } catch (Exception e) {
            log.error("创建存储过程失败", e);
        }
    }

    /**
     * 创建查询目录树存储过程
     */
    private void createGetDirectoryTreeProcedure() {
        try {
            String sql =
                    "CREATE ALIAS IF NOT EXISTS GET_DIRECTORY_TREE FOR " +
                            "\"org.example.ncy.dataanalyst.demos.entity.DirectoryProcedures.getDirectoryTree\"";

            jdbcTemplate.execute(sql);
            log.info("创建存储过程成功: GET_DIRECTORY_TREE");

        } catch (Exception e) {
            log.error("创建GET_DIRECTORY_TREE存储过程失败", e);
        }
    }

    /**
     * 创建删除目录存储过程
     */
    private void createDeleteDirectoryProcedure() {
        try {
            String sql =
                    "CREATE ALIAS IF NOT EXISTS DELETE_DIRECTORY_RECURSIVE FOR " +
                            "\"org.example.ncy.dataanalyst.demos.entity.DirectoryProcedures.deleteDirectoryRecursive\"";

            jdbcTemplate.execute(sql);
            log.info("创建存储过程成功: DELETE_DIRECTORY_RECURSIVE");

        } catch (Exception e) {
            log.error("创建DELETE_DIRECTORY_RECURSIVE存储过程失败", e);
        }
    }

    /**
     * 创建工具存储过程
     */
    private void createUtilityProcedures() {
        try {
            // 创建获取目录信息的存储过程
            String getInfoSql =
                    "CREATE ALIAS IF NOT EXISTS GET_DIRECTORY_INFO FOR " +
                            "\"org.example.ncy.dataanalyst.demos.entity.DirectoryProcedures.getDirectoryInfo\"";
            jdbcTemplate.execute(getInfoSql);

            // 创建统计存储过程
            String countSql =
                    "CREATE ALIAS IF NOT EXISTS COUNT_DIRECTORIES FOR " +
                            "\"org.example.ncy.dataanalyst.demos.entity.DirectoryProcedures.countDirectories\"";
            jdbcTemplate.execute(countSql);

            log.info("创建工具存储过程成功");

        } catch (Exception e) {
            log.error("创建工具存储过程失败", e);
        }
    }
}