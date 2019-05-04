package com.isc.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.isc.service.ServerHandler;
import com.pingan.isc.ISCMessageBroker;

public class StartupListener implements ServletContextListener {

	public StartupListener() {
		super();
	}

	public void contextInitialized( ServletContextEvent arg0 ) {

		try {
			int corePoolSize = 10;
			int maximumPoolSize = 20;
			int keepAliveTime = 300;
			int queueSize = 100;
			ServerHandler handler = new ServerHandler();
			handler.setFilename( "/logs/fqueue/recvqueue/recv.txt" ); 

			ISCMessageBroker.MessageExecutor( corePoolSize, maximumPoolSize, keepAliveTime, queueSize, handler );
		} catch( Exception e ) {
			e.printStackTrace();
		}

	}

	public void contextDestroyed( ServletContextEvent arg0 ) {
		System.out.println( "ISCMessageBroker.destroyed" );
		ISCMessageBroker.destroyed();
	}

}
