package org.example.ncy.dataanalyst.demos.servise;

import org.example.ncy.dataanalyst.demos.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByParentId(Long parentId);

    @Query("SELECT f FROM FileEntity f WHERE f.parentId = :parentId ORDER BY f.uploadTime DESC")
    List<FileEntity> findFilesByParentId(@Param("parentId") Long parentId);


}
