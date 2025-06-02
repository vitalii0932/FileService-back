package org.example.fileservice.repositories;

import org.example.fileservice.models.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    @Query(value = """
    select *
    from files
    where user_id = :userId
    """, nativeQuery = true)
    List<File> findAllByUserSub(@Param("userId") String userSub);
    @Query(value = """
    select * 
    from files
    where id = :id and user_id = :userId
    """, nativeQuery = true)
    Optional<File> findByIdAndUserSub(@Param("id") Long id, @Param("userId") String userSub);
}
