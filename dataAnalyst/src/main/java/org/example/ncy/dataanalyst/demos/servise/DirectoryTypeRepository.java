package org.example.ncy.dataanalyst.demos.servise;

import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DirectoryTypeRepository extends JpaRepository<DirectoryType, Long> {

    @Query("SELECT d FROM DirectoryType d where d.id = :id ")
    DirectoryType findDircetoryById(@Param("id")Long id);

    @Query("SELECT d FROM DirectoryType d where d.fatherid is null ORDER BY d.id desc ")
    List<DirectoryType> findTopDircetory();

    @Query("SELECT d FROM DirectoryType d where d.fatherid = :fatherid ORDER BY d.id desc ")
    List<DirectoryType> findNextDircetoryContents(@Param("fatherid")Long id);

    @Query("select d2 from DirectoryType d left join DirectoryType d2 on d2.fatherid = d.fatherid where d.id = :id ORDER BY d2.id desc ")
    List<DirectoryType> findLastDircetoryContents(@Param("id")Long id);




}