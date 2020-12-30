package com.mini.project.grpc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mini.project.grpc.HelloServiceGrpc.HelloServiceImplBase;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

@Component
public class GrpcServer	 {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);
	
	@Value("${grpc.server.port}")
	private Integer grpcServerPort;

	private Server server;
	
	public void start() throws IOException {
		server = ServerBuilder.forPort(grpcServerPort).addService(new HelloServiceImpl()).build().start();
		LOGGER.info("");
		LOGGER.info("=====================================");
		LOGGER.info("-> GRPC server started on port {}.", grpcServerPort);
		LOGGER.info("=====================================");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.error("*** shutting down gRPC server since JVM is shutting down");
				GrpcServer.this.stop();
				LOGGER.error("*** server shut down");
			}
		});
	}
	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}
	
	public void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
	
	static class HelloServiceImpl extends HelloServiceImplBase {

		@Override
		public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
			
			String greeting = new StringBuilder()
			          .append("Hello, ")
			          .append(request.getFirstName())
			          .append(" ")
			          .append(request.getLastName())
			          .toString();

			HelloResponse reply = HelloResponse.newBuilder().setGreeting(greeting).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}

}
