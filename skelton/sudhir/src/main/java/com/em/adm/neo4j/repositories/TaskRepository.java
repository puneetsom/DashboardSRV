package com.em.adm.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.em.adm.neo4j.domain.Task;

@RepositoryRestResource(collectionResourceRel = "tasks", path = "tasks")
public interface TaskRepository extends GraphRepository<Task> {

	List<Task> findByTaskArchived(@Param("archivedfalse") int taskArchivedFalse);
	List<Task> findByTaskStatus(@Param("status") String taskStatus);

	/*Student findByFirstName(@Param("firstName") String fname);

    @Query("MATCH (n:Student) RETURN n LIMIT {limit}")
    List<Map<String,Object>> students(@Param("limit") int limit);*/
}

