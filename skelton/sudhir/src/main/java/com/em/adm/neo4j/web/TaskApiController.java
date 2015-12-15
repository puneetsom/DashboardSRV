/**
 * 
 */
package com.em.adm.neo4j.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.em.adm.neo4j.domain.Task;
import com.em.adm.neo4j.repositories.TaskRepository;
import com.em.adm.neo4j.services.TaskService;

/**
 * @author sudhir
 *
 */
@Controller("")
public class TaskApiController {
	
	@Autowired
    TaskService taskService;
	
	@Autowired
    TaskRepository repository; 

	@ResponseBody
    @RequestMapping(value = "api/tasks", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Task> tasks(@RequestParam(value = "limit",required = false) Integer limit) {
        return taskService.tasks(limit == null ? 100 : limit);
    }

	@ResponseBody
    @RequestMapping(value = "api/tasks", method= RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Task addTask(@RequestBody Task task) {
        return repository.save(task);
    }

	@ResponseBody
    @RequestMapping(value = "api/tasks/{id}", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Task findOne(@PathVariable Long id) {
        return repository.findOne(id);
    }
	
	@ResponseBody
    @RequestMapping(value = "api/tasks", method= RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addTask(@RequestParam Long id) {
        repository.delete(id);
    }
    
}
