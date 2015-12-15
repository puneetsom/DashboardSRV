/**
 * 
 */
package com.em.adm.neo4j.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.em.adm.neo4j.domain.Task;
import com.em.adm.neo4j.repositories.TaskRepository;
import com.em.adm.neo4j.utils.AppUtils;

/**
 * @author sudhir
 *
 */
@Service
@Transactional
public class TaskService {
	
	@Autowired TaskRepository taskRepository;

	public List<Task> tasks(int limit) {
        Iterable<Task> result = taskRepository.findAll();
        return (List<Task>) AppUtils.makeCollection(result);
    }
}
