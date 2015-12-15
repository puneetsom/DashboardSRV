package com.em.adm.neo4j.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author sudhir
 *
 */
@Controller
public class Neo4jController {

    @RequestMapping("/home")
	public String home() {
		return "index";
	}

}
