package org.example.ncy.dataanalyst.demos.servise;

import org.example.ncy.dataanalyst.demos.entity.FileEntity;
import org.example.ncy.dataanalyst.demos.entity.TabelColumns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabelColumnsRepository extends JpaRepository<TabelColumns, Long> {

    @Query("SELECT f FROM TabelColumns f WHERE f.fileId = :fileId ORDER BY f.id DESC")
    List<TabelColumns> findTableColumnsByFileId(@Param("fileId") Long fileId);

    @Query("select t from TabelColumns t where t.fileId in(select f.id from FileEntity f where f.parentId=:folderId) order by t.ord")
    List<TabelColumns> findTableColumnsByFolderId(@Param("folderId") Long folderId);






}
