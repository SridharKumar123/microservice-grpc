package com.mini.project.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Component
public class AppStartup implements ApplicationListener<ApplicationReadyEvent>{

	private static final Logger LOGGER = LoggerFactory.getLogger(AppStartup.class);
	
	@Value("${health.service.host}")
	private String healthServiceHost;
	
	@Value("${grpc.server.port}")
	private Integer grpcServerPort;
	
	@Value("${publish.health.metrics}")
	private Boolean publishHealthMetrics;

	@Value("${publish.interval}")
	private Integer interval;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		LOGGER.info("publishHealthMetrics property set to -> {}", publishHealthMetrics);
		LOGGER.info("Time interval to publish health-metrics in millis is -> {}", interval * 1000);
		publishHealthMetrics();
	}
	
	public void publishHealthMetrics() {
		if (publishHealthMetrics) {
			try {
				long intervalInMillis = interval * 1000;
				ManagedChannel channel = ManagedChannelBuilder.forAddress(healthServiceHost, grpcServerPort)
				          .usePlaintext(true)
				          .build();
				HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
				HelloResponse resp = stub.hello(HelloRequest.newBuilder().setFirstName("John").setLastName("Rambo").build());
				String responseString = JsonFormat.printer().print(resp);
				LOGGER.info("Response received from health-service -> {}", responseString);
				channel.shutdown();
				Thread.sleep(intervalInMillis);
				publishHealthMetrics();
			} catch (InvalidProtocolBufferException | InterruptedException e) {
				LOGGER.error("GRPC exception. Exception -> {}", e);
			}
		}
	}

}
