package com.appx.demo.p6spy;

import java.sql.SQLException;

import org.h2.tools.Server;

public final class H2Database {

	private static final H2Database _INSTANCE = new H2Database();

	private Server server;

	private H2Database() {
	}

	public static H2Database getInstance() {
		return _INSTANCE;
	}

	public void start() throws SQLException {
		if (server != null) {
			throw new RuntimeException("the database server has already been started.");
		}
		server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8043").start();
	}

	public void stop() {
		if (server == null) {
			throw new RuntimeException("the database server has not been started yet.");
		}
		server.stop();
	}
}
