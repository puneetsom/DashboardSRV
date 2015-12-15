/**
 * 
 */
package com.em.adm.neo4j.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.em.adm.neo4j.domain.Student;
import com.em.adm.neo4j.services.StudentService;

/**
 * @author sudhir
 *
 */
@Controller("")
public class StudentApiController {
	
	@Autowired
    StudentService studentService;

	@ResponseBody
    @RequestMapping(value = "/api/student-list", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Student> students(@RequestParam(value = "limit",required = false) Integer limit) {
        return studentService.students(limit == null ? 100 : limit);
    }
    
}
