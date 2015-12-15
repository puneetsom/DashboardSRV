package com.em.adm.neo4j;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author sudhir
 *
 */
@Configuration
@Import(MyNeo4jConfiguration.class)
@Controller("/")
public class Application extends WebMvcConfigurerAdapter {
	
	public static void main(String[] args) throws IOException {
		SpringApplication.run(Application.class, args);
	}
	
	
}
