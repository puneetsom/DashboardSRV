/**
 * 
 */
package com.em.adm.neo4j.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.em.adm.neo4j.domain.Student;
import com.em.adm.neo4j.repositories.StudentRepository;
import com.google.gson.Gson;

/**
 * @author sudhir
 *
 */
@Service
@Transactional
public class StudentService {
	
	@Autowired StudentRepository studentRepository;

	public List<Student> students(int limit) {
        Iterator<Map<String, Object>> result = studentRepository.students(limit).iterator();
        return toObject(result);
    }
	
	 private List<Student> toObject(Iterator<Map<String, Object>> result) {
        List<Student> nodes = new ArrayList<Student>();
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            LinkedHashMap<String, Object> objectJson = (LinkedHashMap<String, Object>) row.get("n");
            Student student = new Gson().fromJson(new Gson().toJson(objectJson), Student.class);
            nodes.add(student);
        }
        return nodes;
    }
}
