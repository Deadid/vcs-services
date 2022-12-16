package org.serhii.makhov.vcsservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class VcsservicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(VcsservicesApplication.class, args);
	}

}
