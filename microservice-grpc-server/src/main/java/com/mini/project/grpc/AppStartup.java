package com.mini.project.grpc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AppStartup implements ApplicationListener<ApplicationReadyEvent>{

	private static final Logger LOGGER = LoggerFactory.getLogger(AppStartup.class);
	
	@Autowired
	private GrpcServer grpcServer;
	
	@Autowired
	private ApplicationContext context;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {
		try {
			grpcServer.start();
			grpcServer.blockUntilShutdown();
		}catch(IOException | InterruptedException e) {
			LOGGER.error("-> GRPC server could not be started on application startup. Exception: {}", e);
			SpringApplication.exit(context);
		}
	}
}
