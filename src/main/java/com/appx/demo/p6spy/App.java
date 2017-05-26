package com.appx.demo.p6spy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariDataSource;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		App app = new App();
		try {
			// start the database
			app.startDatabase();

			// initialize the datasource
			DataSource ds = app.initializeDataSource();

			// run test query
			app.testQuery(ds);
			
			// stop the database
			app.stopDatabase();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void startDatabase() throws SQLException {
		H2Database.getInstance().start();
	}

	private DataSource initializeDataSource() throws IOException, SQLException {
		Properties dbProperties = new Properties();
		dbProperties.load(App.class.getResourceAsStream("/jdbc.properties"));

		HikariDataSource ds = new HikariDataSource();
		ds.setDataSourceClassName(dbProperties.getProperty("jdbc.dataSourceClassName"));
		ds.setMinimumPoolSize(1);
		ds.setMaximumPoolSize(1);

		Properties props = new Properties();
		props.put("url", dbProperties.getProperty("jdbc.url"));
		props.put("user", dbProperties.getProperty("jdbc.username"));
		props.put("password", dbProperties.getProperty("jdbc.password"));
		ds.setDataSourceProperties(props);

		P6DataSource p6spyDs = new P6DataSource(ds);

		Connection conn = p6spyDs.getConnection();
		PreparedStatement pstmt = conn
				.prepareStatement("drop table user if exists; create table IF NOT EXISTS user (id varchar(255), name varchar(255)) ");
		pstmt.execute();
		pstmt.close();

		for (int i = 0; i < 3; i++) {
			pstmt = conn.prepareStatement("insert into user values (?,?)");
			pstmt.setString(1, UUID.randomUUID().toString());
			pstmt.setString(2, "username_" + System.nanoTime());
			pstmt.executeUpdate();
			pstmt.close();
		}

		conn.close();

		return p6spyDs;
	}

	private void testQuery(DataSource ds) throws SQLException {
		Connection conn = ds.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select * from user ");
		ResultSet rs = pstmt.executeQuery();
		
		LOGGER.info("============ query result ============");
		while(rs.next()){
			String id = rs.getString("id");
			String name = rs.getString("name");
			LOGGER.info(" user data => id: {}, name: {}", id, name);
		}
		
		rs.close();
		pstmt.close();
		conn.close();
	}

	private void stopDatabase() throws SQLException {
		H2Database.getInstance().stop();
	}
}
