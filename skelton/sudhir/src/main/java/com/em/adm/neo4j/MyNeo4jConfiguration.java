package com.em.adm.neo4j;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
/**
 * @author sudhir
 *
 */
@EnableTransactionManagement
@Import(RepositoryRestMvcConfiguration.class)
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.em.adm.neo4j"})
@Configuration
@EnableNeo4jRepositories(basePackages = "com.em.adm.neo4j.repositories")
public class MyNeo4jConfiguration extends Neo4jConfiguration {

    public static final String URL = System.getenv("NEO4J_URL") != null ? System.getenv("NEO4J_URL") : "http://localhost:7474";

    @Override
    public Neo4jServer neo4jServer() {
        return new RemoteServer(URL,"neo4j","");
    }

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory("com.em.adm.neo4j.domain");
    }
}
