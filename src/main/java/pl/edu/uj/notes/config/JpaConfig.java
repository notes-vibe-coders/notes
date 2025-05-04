package pl.edu.uj.notes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
/*
TODO
Maybe it would fit better in note package?
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {}
