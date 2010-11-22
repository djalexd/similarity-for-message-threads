package project.core.persistence;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Remote;

import project.client.persistence.Message;
import project.client.persistence.MessageBoard;
import project.client.persistence.MessageThread;
import project.client.persistence.User;
import project.client.persistence.Word;
import project.persistence.properties.MessageWithProperties;


@Remote
public interface PersistenceLoaderMBean {

	public void start ();
	
	public void stop ();
	
	public MessageBoard loadMessageBoard (String name)
					throws SQLException, IllegalArgumentException;
	
	public void insertMessageThread (String url, String name, MessageBoard board) 
					throws SQLException, IllegalArgumentException;
	
	public MessageThread loadMessageThread (String url);
	
	public MessageThread loadMessageThreadByName (String name);
	
	
	public MessageThread loadMessageThread (int threadId) 
					throws SQLException;
	
	
	public boolean insertUser (String user, int boardID)
					throws SQLException;
	
	public User loadUser (String user, int boardID) 
					throws SQLException;	
	
	public boolean insertMessage (Message message) 
					throws SQLException;
	
	public Message loadMessage (String url);
	
	public List<MessageWithProperties> loadMessagesWithProperties (String boardName)
					throws SQLException;
	
	public List<Message> loadThreadMessages (int threadId);
	
	public Message loadMessage (int id);
	
	public List<Message> loadMessages (int[] ids);
	
	public List<Message> loadMessages (int minId, int maxId)
					throws SQLException;
	
	public void insertWords (String[] words)
					throws SQLException;
	
	public boolean insertWord (String word, String labels) 
					throws SQLException;
	
	public Word loadWord (String word)
					throws SQLException;
	
	/*
	public void calculateMessageProperties (int id)
					throws SQLException;
	
	public void calculateWordProperties (String word)
					throws SQLException;
	*/
	
	public boolean isStopWord (String word)
					throws SQLException;
	
	
	public int getNumWords (String boardName, boolean distinct);

	public int getNumUsers (String boardName);
	
	public int getNumMessages (String boardName);
	
	public int getNumInvalidMessages (String boardName);
	
	public int getNumThreads (String boardName);
	
	public int getNumBoards ();
	
	public MessageThread getMessageThread(int messageId)
							throws IllegalArgumentException;
	
	public User getMessageUser (int messageId)
							throws IllegalArgumentException;
}
