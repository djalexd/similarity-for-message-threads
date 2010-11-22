package project.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;

import project.client.persistence.Message;
import project.client.persistence.MessageBoard;
import project.client.persistence.MessageThread;
import project.client.persistence.User;
import project.client.persistence.Word;
import project.core.mbeans.database.ConnectionManagerMysqlImpl;
import project.persistence.properties.MessageWithProperties;
import project.utils.statistics.impl.Stemmer;

@Stateful
@RemoteBinding(jndiBinding="PersistenceLoader")
@SerializedConcurrentAccess
@CacheConfig(removalTimeoutSeconds=18000L)
public class PersistenceLoader extends ConnectionManagerMysqlImpl implements PersistenceLoaderMBean {
	
	private static final String WORD_DB_DELIMITER = ";";

	private transient Connection connection = null;
	
	@PostConstruct
	public void start() {
		
		//setJndiName(this.getClass().getSimpleName());		
		
		//System.out.println ("Persistence MBean started");		
		this.initConnection ();
		
		// on startup, do a 'recap', try to fix all bad 
		// things (previously inserted)
		//
		
		//this.deleteMessageSettings ();
		//this.calculateNoSettingMessages ();
		
		//
		//
		//this.removeDuplicateWords ();
		//this.deleteWordProperties ();
		//this.calculateWordsProperties ();
	}
	
	@PreDestroy
	public void stop() {
		
		//unbind();
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println ("Persistence MBean stopped");
	}
	
	
	private void initConnection () {
		try {
			this.setConnectionParams("ebas", "gwtebas", "bachelor_project");
			connection = this.getConnection();
			if (connection == null)
				throw new IllegalStateException ("Unable to init connection");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void insertMessageThread (String url, String name, MessageBoard board) 
							throws SQLException, IllegalArgumentException {
		
		// check for null params
		//
		if (url == null || board == null)
			throw new IllegalArgumentException ("Failed to insert message thread (url or message board are null)");
		

		// make sure the thread doesn't exist 
		//
		if (loadMessageThread(url) != null) {
			return;
		}
		
		String query = "insert into MessageThread(name,url,boardID) values(?,?,?);";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, name);
		statement.setString(2, url);
		statement.setInt(3, board.getId());

		statement.execute();
		statement.close();
		
		//System.out.println ("Inserted a new message thread:");
		//System.out.println ("\t> name  : " + name);
		//System.out.println ("\t> url   : " + url);
		//if (board != null) {
		//	System.out.println ("\t> board : " + board.getName());
		//} else {
			// log this error
		//}
	}
	
	
	public MessageThread loadMessageThread (String url) {

		if (url == null)
			return null;

		if (connection == null) {
			this.initConnection ();
		}

		try {
			String query = "select * from MessageThread where url like ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, url);

			MessageThread thread = null;

			ResultSet set = statement.executeQuery();		
			if (set.next()) {
				thread = new MessageThread ();

				thread.setId  (set.getInt("id"));
				thread.setName(set.getString("name"));
				thread.setUrl (set.getString("url"));
			}

			set.close();
			statement.close();

			return thread;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	public boolean insertUser (String user, int boardID)
						throws SQLException {
		
		// thread null case
		//
		if (user == null)
			return false;
		
		// first check if user already exists
		//
		if (this.loadUser(user, boardID) != null) {
			return false;
		}
		
		String query = "insert into User(name,boardID) values(?,?)";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, user);
		statement.setInt(2, boardID);
		
		statement.execute();
		statement.close();
		
		//System.out.println ("Inserted a new user:");
		//System.out.println ("\t> name     : " + user);
		//System.out.println ("\t> board id : " + boardID);
		
		return true;
	}
	
	
	public User loadUser (String user, int boardId) 
						throws SQLException {
		
		// threat null case
		//
		if (user == null)
			throw new IllegalArgumentException ("User's name cannot be null when loading");
		
		String query = "select * from User where name like ? and boardID = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, user);
		statement.setInt(2, boardId);
		
		
		ResultSet set = statement.executeQuery();
		User u = null;
		
		if (set.next ()) {
			u = new User ();
			u.setId(set.getInt("id"));
			u.setName(set.getString("name"));
			u.setMessageBoard(null);
			u.setMessages(null);
		}
		
		set.close();
		statement.close();
		
		return u;
	}
	
	public boolean insertMessage (Message message) 
						throws SQLException {
		
		// threat null case
		//
		if (message == null)
			return false;
		
		// check if message exists
		//
		if (this.loadMessage(message.getUrl()) != null) {
			return false;
		}
		
		// make sure the message can be inserted
		//
		if (message.getUser() == null)
			throw new IllegalArgumentException ("Message's user cannot be null when inserting");
		if (message.getMessageThread() == null) 
			throw new IllegalArgumentException ("Message's thread cannot be null when inserting");
		
		//
		// persist the words
		//
		this.insertWords(this.getRegularWords(message.getFormattedContent()));
		
		//
		// persist the message
		//
		String query = "insert into Message(threadID,userID,publishDate,content,formatted_content,url,parentID) values(?,?,?,?,?,?,?);";
		PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
		
		statement.setInt   (1, message.getMessageThread().getId());
		statement.setInt   (2, message.getUser().getId());
		statement.setString(3, message.getPublishDate());
		statement.setString(4, message.getContent());
		statement.setString(5, message.getFormattedContent());
		statement.setString(6, message.getUrl());
		if (message.getParent() != null) {
			Message msg = this.loadMessage(message.getParent().getUrl());
			if (msg != null)
				statement.setInt (7, msg.getId());
			else
				statement.setInt (7, -1);
		}
		else
			statement.setInt(7, -1);
		
		statement.execute();
		ResultSet set = statement.getGeneratedKeys();
		
		if (!set.next()) {
			System.out.println ("!! Failed to read auto generated keys !!");
		} else {
			message.setId(set.getInt(1));
		}
		
		set.close();
		statement.close();
		
		System.out.println ("Inserted a new message:");
		System.out.println ("\t> url     : " + message.getUrl());
		//System.out.println ("\t> date    : " + message.getPublishDate());
		//System.out.println ("\t> content : " + message.getFormattedContent());
		
		//this.calculateMessageProperties(message.getId());
		return true;
	}
	
	
	public Message loadMessage (String url){

		// thread null case
		//
		if (url== null)
			return null;

		try {
			// prepare the statement
			// 
			String query = "select * from Message where url like ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, url);

			ResultSet set = statement.executeQuery();
			Message msg = null;

			if (set.next()) {

				msg = new Message ();
				msg.setId(set.getInt("id"));
				msg.setContent(set.getString("content"));
				msg.setFormattedContent(set.getString("formatted_content"));
				msg.setPublishDate(set.getString("publishDate"));
				msg.setUrl(set.getString("url"));

				// TODO Load the message thread and user

			}

			set.close();
			statement.close();

			return msg;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	
	// perform a 'bulk' insert of words (at least the ones
	// not found in database)
	//
	public void insertWords (String[] words)
						throws SQLException {
		
		// treat null case
		//
		if (words == null)
			return;

		String query = "insert into Word(content) values(?)";
		PreparedStatement statement = connection.prepareStatement(query);
		
		for (String w : words) {

			// check if word exists, by performing a load operation
			//
			if (loadWord(w) != null) {
				continue;
			} else {
				
				// perform a stemming 
				// 				
				String stemmedWord = Stemmer.stem(w);
				
				statement.setString(1, stemmedWord);
				statement.execute();
			}
		}
		
		statement.close();		
	}
	
	
	public boolean insertWord (String word, String labels) 
						throws SQLException {
		
		// treat null case
		//
		if (word == null)
			return false;
		
		
		// perform a stemming 
		// 
		String stemmedWord = Stemmer.stem(word);
		
		// check if word exists, by performing a load operation
		//
		if (loadWord(stemmedWord) != null) {
			
			// word exists
			//
			return false;
		} else {
			
			// word doesn't exist
			//
			String query = "insert into Word(content,labels) values(?,?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, stemmedWord);
			statement.setString(2, labels);
			
			statement.execute();
			statement.close();
			
			return true;
		}
		
	}
	
	
	public Word loadWord (String word)
						throws SQLException {
		
		
		// thread null case
		//
		if (word == null)
			return null;
		
		// perform a stemming
		//
		String stemmedWord = Stemmer.stem(word);
		
		// now prepare the stement
		//
		String query = "select * from Word where content like ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, stemmedWord);
		
		ResultSet set = statement.executeQuery();
		if (!set.next()) {
			
			set.close();
			statement.close();
			
			// no word was found
			//
			return null;
		} else {
			
			// load the word, then close the resources used
			//
			
			Word w = new Word ();
			w.setId(set.getInt(1));
			w.setContent(set.getString(2));
			w.setLanguage(null);
			
			set.close();
			statement.close();			
			
			return w;
		}
		
	}

	
	public List<Message> loadMessages(int minId, int maxId) throws SQLException {

		List<Message> messages = new LinkedList<Message> ();

		// prepare the statement
		// 
		String query = "select * from Message where id >= ? and id <= ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, minId);
		statement.setInt(2, maxId);
		
		ResultSet set = statement.executeQuery();
		Message msg = null;
		
		while (set.next()) {
			
			msg = new Message ();
			msg.setId(set.getInt("id"));
			msg.setContent(set.getString("content"));
			msg.setFormattedContent(set.getString("formatted_content"));
			msg.setPublishDate(set.getString("publishDate"));
			msg.setUrl(set.getString("url"));

			messages.add(msg);
			
		}
		
		set.close();
		statement.close();
		
		return messages;		
	}

	public Message loadMessage (int id) {

		try {
			// prepare the statement
			// 
			String query = "select * from Message where id = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);

			ResultSet set = statement.executeQuery();
			Message msg = null;

			if (set.next()) {

				msg = new Message ();
				msg.setId(set.getInt("id"));
				msg.setContent(set.getString("content"));
				msg.setFormattedContent(set.getString("formatted_content"));
				msg.setPublishDate(set.getString("publishDate"));
				msg.setUrl(set.getString("url"));

				// TODO Load the message thread and user

			}

			set.close();
			statement.close();

			return msg;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	

	public void calculateMessageProperties (int id)
	throws SQLException {
		
		String query = "select formatted_content from Message where id=" + id;
		Statement statement = connection.createStatement();
		
		ResultSet set = statement.executeQuery(query);
		
		Map<String,Integer> counts = new HashMap<String, Integer> ();
		while (set.next()) {
		
			String content = set.getString(1);
			
			String[] tokens = content.split(";");
			
			for (String t : tokens) {
				
				if (t.length() > 0) {

					if (!counts.containsKey(t)) {
						counts.put(t, new Integer (1));
					} else {
						Integer count = counts.get(t);
						counts.put(t, new Integer (count + 1));
					}

				}
			}
		}

		String str = "";
		Iterator<String> i = counts.keySet().iterator();
		while (i.hasNext()) {
			
			
			String w = i.next();
			Integer cnt = counts.get(w);
			
			str += w + ":" + cnt + ";";
		}
		
		set.close();
		statement.close();

		
		// insert in database
		//
		query = "insert into settings(tableName,tableID,propertyKey,propertyValue) " +
		        "values(?,?,?,?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, "Message");
		preparedStatement.setInt(2, id);
		preparedStatement.setString(3, "word_count");
		preparedStatement.setString(4, str);
		
		preparedStatement.execute();
		preparedStatement.close();
	}	
	

	
	
	/*
	private void removeDuplicateWords ()
						throws SQLException {
		
		String queryDelete = "delete from Word where content like ? and id != ?";
		String query = "select id,content from Word";
		Statement statement = connection.createStatement();
		
		PreparedStatement deleteStatement = connection.prepareStatement(queryDelete);
		
		ResultSet set = statement.executeQuery(query);
		while (set.next()) {
			
			
			int id = set.getInt(1);
			String word = set.getString(2);
			
			deleteStatement.setString(1, word);
			deleteStatement.setInt(2, id);
			
			if (!deleteStatement.execute()) {
				System.out.println ("Removed " + deleteStatement.getUpdateCount() + " references to " + word);
			}
			
			
		}
		
		set.close();
		statement.close();
		deleteStatement.close();
	}
	*/
	
	/*
	public void calculateWordProperties(String word) throws SQLException {
		
		//
		// create the pattern
		//
		Pattern p = Pattern.compile(word + ":([0-9]*)");
		Long count = 0L;
		
		Word w = this.loadWord(word);
		if (w == null)
			return;
	
		//
		//
		String query = "select propertyValue from settings where tableName like ? and propertyKey like ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, "Message");
		statement.setString(2, "word_count");
		
		ResultSet set = statement.executeQuery();
		
		while (set.next()) {
			
			//
			// find any matching group
			//
			Matcher m = p.matcher(set.getString(1));
			if (m.find()) {
				
				//
				// count the match
				//
				String strNum = m.group(1);
				count += Long.parseLong(strNum);
				
			}
			
		}
		
		set.close();
		statement.close();

		
		// insert in database
		//
		query = "insert into settings(tableName,tableID,propertyKey,propertyValue) " +
		        "values(?,?,?,?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, "Word");
		preparedStatement.setInt(2, w.getId());
		preparedStatement.setString(3, "frequency");
		preparedStatement.setString(4, "" + count);
		
		preparedStatement.execute();
		preparedStatement.close();
	}
	*/

	
	public boolean isStopWord(String word) throws SQLException {
		
		String query = "select labels from Word where content like ?";
		PreparedStatement statement = connection.prepareStatement(query);
		
		statement.setString(1, word);
		ResultSet set = statement.executeQuery();
		
		if (!set.next()) {
			
			set.close();
			statement.close();
			
			return false;
			
			
		} else {

			String label = set.getString(1);
			set.close();
			statement.close();
			
			if (label == null)
				return false;
			else if (label.indexOf("stop") != -1)
				return true;
			else 
				return false;
		}		
	}
	
	private String[] getRegularWords (String formattedContent) {
		
		String[] tokens = formattedContent.split(WORD_DB_DELIMITER);
		List<String> asList = new LinkedList<String> ();
		for (String t : tokens) {
			asList.add(t);
		}
		
		Iterator<String> i = asList.iterator();
		while (i.hasNext()) {
			String str = i.next();
			if (str.indexOf('-') != -1)
				i.remove();
		}
		
		return asList.toArray(new String[asList.size()]);
	}


	public List<MessageWithProperties> loadMessagesWithProperties (String boardName)
			throws SQLException {
		
		List<MessageWithProperties> messages = new LinkedList<MessageWithProperties> ();

		if (boardName == null) {
			// prepare the statement
			// 
			String query = "select * from Message";
			Statement statement = connection.createStatement();

			ResultSet set = statement.executeQuery(query);
			MessageWithProperties msg = null;

			while (set.next()) {

				msg = new MessageWithProperties ();
				msg.setId(set.getInt("id"));
				msg.setContent(set.getString("content"));
				msg.setFormattedContent(set.getString("formatted_content"));
				msg.setPublishDate(set.getString("publishDate"));
				msg.setUrl(set.getString("url"));

				msg.loadProperties(connection);
				messages.add (msg);
			}

			set.close();
			statement.close();

		} else {
			
			MessageBoard board = this.loadMessageBoard(boardName);
			if (board == null) {
				return messages;
			}
			
			
			// prepare the statement
			// 
			String query = "select m.* from Message m, MessageThread t where m.threadID = t.id and t.boardID = " + board.getId();
			Statement statement = connection.createStatement();

			ResultSet set = statement.executeQuery(query);
			MessageWithProperties msg = null;

			while (set.next()) {

				msg = new MessageWithProperties ();
				msg.setId(set.getInt("id"));
				msg.setContent(set.getString("content"));
				msg.setFormattedContent(set.getString("formatted_content"));
				msg.setPublishDate(set.getString("publishDate"));
				msg.setUrl(set.getString("url"));

				msg.loadProperties(connection);
				messages.add (msg);
			}

			set.close();
			statement.close();			
		}
		
		return messages;				
	}


	public int getNumBoards () {
		
		try {
		// prepare the statement
		// 
		String query = "select count(*) from MessageBoard";
		Statement statement = connection.createStatement();

		ResultSet set = statement.executeQuery(query);
		if (!set.next())
			return 0;

		int count = set.getInt(1);

		set.close();
		statement.close();
		
		return count;
		
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	
	public int getNumMessages(String boardName) {
		
		if (boardName == null) {

			try {
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(*) from Message");
				if (!set0.next())
					return 0;

				int count = set0.getInt(1);

				set0.close();
				s0.close();
				
				return count;
			
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		} else {

			try {
				
				MessageBoard board = this.loadMessageBoard(boardName);
				if (board == null) {
					return 0;
				}
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(m.id) from Message m, MessageThread t " + 
												 " where m.threadID = t.id and t.boardID = " + board.getId());

				int count = 0;
				while (set0.next()) {
					count += Integer.parseInt(set0.getString(1));
				}

				set0.close();
				s0.close();
				
				return count;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		}
		
	}

	
	public int getNumThreads(String boardName) {
		
		if (boardName == null) {

			try {
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(*) from MessageThread");
				if (!set0.next())
					return 0;

				int count = set0.getInt(1);

				set0.close();
				s0.close();
				
				return count;
			
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		} else {

			try {
				
				MessageBoard board = this.loadMessageBoard(boardName);
				if (board == null) {
					return 0;
				}
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(t.id) from MessageThread t " + 
												 " where t.boardID = " + board.getId());

				int count = 0;
				while (set0.next()) {
					count += Integer.parseInt(set0.getString(1));
				}

				set0.close();
				s0.close();
				
				return count;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		}		
	}

	
	public int getNumUsers(String boardName) {
		
		if (boardName == null) {

			try {
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(*) from User");
				if (!set0.next())
					return 0;

				int count = set0.getInt(1);

				set0.close();
				s0.close();
				
				return count;
			
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		} else {

			try {
				
				MessageBoard board = this.loadMessageBoard(boardName);
				if (board == null) {
					return 0;
				}
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(u.id) from User u " + 
												 " where u.boardID = " + board.getId());

				int count = 0;
				while (set0.next()) {
					count += Integer.parseInt(set0.getString(1));
				}

				set0.close();
				s0.close();
				
				return count;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		}		
	}

	
	
	public int getNumWords (String boardName, boolean distinct) {
		
		if (distinct) {

			try {
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(*) from Word");
				if (!set0.next())
					return 0;

				int count = set0.getInt(1);

				set0.close();
				s0.close();
				
				return count;
			
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		} else {

			try {
				
				if (boardName != null) {

					MessageBoard board = this.loadMessageBoard(boardName);
					if (board == null) {
						return 0;
					}

					Statement s0 = connection.createStatement();
					ResultSet set0 = s0.executeQuery("select s.propertyValue from settings s, Message m, MessageThread t " + 
							" where propertyKey like 'num-words' and s.tableID = m.id and m.threadID = t.id and t.boardID = " + board.getId());

					int count = 0;
					while (set0.next()) {
						count += Integer.parseInt(set0.getString(1));
					}

					set0.close();
					s0.close();

					return count;

				} else {
										
					Statement s0 = connection.createStatement();
					ResultSet set0 = s0.executeQuery("select s.propertyValue from settings s " + 
							" where propertyKey like 'num-words'");

					int count = 0;
					while (set0.next()) {
						count += Integer.parseInt(set0.getString(1));
					}

					set0.close();
					s0.close();

					return count;					
				}

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		}
		
	}

	
	public MessageBoard loadMessageBoard(String name) 
					throws IllegalArgumentException, SQLException {
		
		if (name == null)
			throw new IllegalArgumentException ("Name is null");

		Statement s0 = connection.createStatement();
		ResultSet set0 = s0.executeQuery("select * from MessageBoard where name like ?");
		if (!set0.next())
			return null;

		MessageBoard board = new MessageBoard ();
		board.setDescription(set0.getString("description"));
		board.setId(set0.getInt("id"));
		board.setName(set0.getString("name"));
		board.setUrl(set0.getString("url"));
		board.setMessageThreads(null);
		board.setUsers(null);
		
		set0.close();
		s0.close();

		return board;
	}

	
	
	public MessageThread getMessageThread (int messageId) 
							throws IllegalArgumentException {
		
		try {
			Statement s0 = connection.createStatement();
			ResultSet set0 = s0.executeQuery("select t.* from MessageThread t, Message m where m.id = " + messageId + " and m.threadID = t.id");
			if (!set0.next())
				throw new IllegalArgumentException ("Invalid message id : " + messageId);

			MessageThread thread = new MessageThread ();
			thread.setId(set0.getInt("id"));
			thread.setMessageBoard(null);
			thread.setMessages(null);
			thread.setName(set0.getString("name"));
			thread.setUrl(set0.getString("url"));

			set0.close();
			s0.close();

			return thread;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;		
	}

	
	
	public User getMessageUser(int messageId) {
		
		try {
			Statement s0 = connection.createStatement();
			ResultSet set0 = s0.executeQuery("select u.* from User u, Message m where m.id = " + messageId + " and m.userID = u.id");
			if (!set0.next())
				throw new IllegalArgumentException ("Invalid message id : " + messageId);

			User user = new User ();
			user.setId(set0.getInt("id"));
			user.setMessageBoard(null);
			user.setMessages(null);
			user.setName(set0.getString("name"));

			set0.close();
			s0.close();

			return user;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;			
	}

	
	public List<Message> loadThreadMessages(int threadId) {
		
		try {
			List<Message> messages = new LinkedList<Message> ();

			// prepare the statement
			// 
			String query = "select * from Message where threadID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, threadId);
			
			ResultSet set = statement.executeQuery();
			Message msg = null;
			
			while (set.next()) {
				
				msg = new Message ();
				msg.setId(set.getInt("id"));
				msg.setContent(set.getString("content"));
				msg.setFormattedContent(set.getString("formatted_content"));
				msg.setPublishDate(set.getString("publishDate"));
				msg.setUrl(set.getString("url"));

				messages.add(msg);
				
			}
			
			set.close();
			statement.close();
			
			return messages;		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new LinkedList<Message> ();
	}


	public List<Message> loadMessages(int[] ids) {

		try {
			List<Message> messages = new LinkedList<Message> ();

			// prepare the statement
			// 
			Statement statement = connection.createStatement();
			String query = "select * from Message where ";
			for (int i = 0; i < ids.length; i++) {
				query += "id = " + ids [i];
				if (i != (ids.length - 1)) {
					query += " or ";
				}
			}

			ResultSet set = statement.executeQuery(query);
			Message msg = null;



			while (set.next()) {

				msg = new Message ();
				msg.setId(set.getInt("id"));
				msg.setContent(set.getString("content"));
				msg.setFormattedContent(set.getString("formatted_content"));
				msg.setPublishDate(set.getString("publishDate"));
				msg.setUrl(set.getString("url"));

				messages.add(msg);

			}

			set.close();
			statement.close();

			return messages;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return new LinkedList<Message> ();
	}

	
	public MessageThread loadMessageThread(int threadId) throws SQLException {
		
		if (connection == null) {
			this.initConnection ();
		}
		
		String query = "select * from MessageThread where id = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, threadId);
		
		MessageThread thread = null;
		
		ResultSet set = statement.executeQuery();		
		if (set.next()) {
			thread = new MessageThread ();
			
			thread.setId  (set.getInt("id"));
			thread.setName(set.getString("name"));
			thread.setUrl (set.getString("url"));
		}
		
		set.close();
		statement.close();
		
		return thread;		
	}

	public int getNumInvalidMessages(String boardName) {
		
		if (boardName == null) {

			try {
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(*) from Message where length(formatted_content) < 10");
				if (!set0.next())
					return 0;

				int count = set0.getInt(1);

				set0.close();
				s0.close();
				
				return count;
			
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		} else {

			try {
				
				MessageBoard board = this.loadMessageBoard(boardName);
				if (board == null) {
					return 0;
				}
				
				Statement s0 = connection.createStatement();
				ResultSet set0 = s0.executeQuery("select count(m.id) from Message m, MessageThread t " + 
												 " where length(m.formatted_content) < 10 and m.threadID = t.id and t.boardID = " + board.getId());

				int count = 0;
				while (set0.next()) {
					count += Integer.parseInt(set0.getString(1));
				}

				set0.close();
				s0.close();
				
				return count;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
			
		}		
	}

	
	public MessageThread loadMessageThreadByName(String name) {
		
		if (name == null)
			return null;

		if (connection == null) {
			this.initConnection ();
		}

		try {
			String query = "select * from MessageThread where name like ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, name);

			MessageThread thread = null;

			ResultSet set = statement.executeQuery();		
			if (set.next()) {
				thread = new MessageThread ();

				thread.setId  (set.getInt("id"));
				thread.setName(set.getString("name"));
				thread.setUrl (set.getString("url"));
			}

			set.close();
			statement.close();

			return thread;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;		
	}
}
