package com.em.adm.neo4j.repositories;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.em.adm.neo4j.domain.Student;

/**
 * @author sudhir
 *
 */
@RepositoryRestResource(collectionResourceRel = "students", path = "students")
public interface StudentRepository extends GraphRepository<Student> {
    
	Student findByFirstName(@Param("firstName") String fname);

    @Query("MATCH (n:Student) RETURN n LIMIT {limit}")
    List<Map<String,Object>> students(@Param("limit") int limit);
}

