package project.core.mbeans.search;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Remote;

import project.client.persistence.Message;

@Remote
public interface MessageSeachMBean {

	public List<Message> search (String[] keywords, int limit, boolean userRelevance)
							throws IllegalArgumentException;
}
