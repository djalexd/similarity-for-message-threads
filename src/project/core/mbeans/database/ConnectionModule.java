package project.core.mbeans.database;

import com.google.inject.AbstractModule;


public class ConnectionModule extends AbstractModule {
	@Override
	protected void configure() {
		bind (ConnectionManager.class).to(ConnectionManagerMysqlImpl.class);
	}
}
