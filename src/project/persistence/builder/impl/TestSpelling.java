package project.persistence.builder.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import project.client.persistence.Message;
import project.client.persistence.Word;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.FileWordTokenizer;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.TeXWordFinder;
import com.swabunga.spell.event.WordTokenizer;

public class TestSpelling implements SpellCheckListener {

	SpellChecker checker;
	ArrayList misspelled;
	
	
	private Connection connection = null;
	
	int numWords = 0, numInvalidWords = 0;
	
	public List<Word> loadWords(int minId, int maxId) throws SQLException {

		List<Word> words = new LinkedList<Word> ();

		// prepare the statement
		// 
		String query = "select * from Word where id >= ? and id <= ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, minId);
		statement.setInt(2, maxId);
		
		ResultSet set = statement.executeQuery();
		
		while (set.next()) {

			Word w = new Word ();
			w.setId(set.getInt(1));
			w.setContent(set.getString(2));
			
			words.add(w);
		}
		
		set.close();
		statement.close();
		
		return words;		
	}		
	
	public TestSpelling () {
		
	    createDictionary();
	    
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + "bachelor_project", 
					 "ebas", "gwtebas");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    // how the heck does "misspelled" get populated? through the spellingError method? (possibly)
	    misspelled = new ArrayList();

	    checker.addSpellCheckListener(this);	    

	    try {
	    	List<Word> words = this.loadWords(0, 10000);
	    	numWords = words.size();
	    	Iterator<Word> i = words.iterator();
	    	while (i.hasNext()) {
	    		Word w = i.next();
	    		WordTokenizer texTok = new StringWordTokenizer(w.getContent());
	    		checker.checkSpelling(texTok);
	    	}
	    	
	    	System.out.println ("# words = " + numWords + ", # invalid = " + numInvalidWords);
	    } catch (Exception e) { e.printStackTrace(); }
	}
	
	
	  private void createDictionary()
	  {
	    File dict = new File("/home/alexd/workspace/proiect-diploma/input/english.0/english.0");
	    try
	    {
	      checker = new SpellChecker(new SpellDictionaryHashMap(dict));
	    }
	    catch (FileNotFoundException e)
	    {
	      System.err.println("Dictionary File " + dict + " not found! " + e);
	      System.exit(1);
	    }
	    catch (IOException ex)
	    {
	      System.err.println("IO problem: " + ex);
	      System.exit(2);
	    }
	  }


	  public void spellingError(SpellCheckEvent event) {
		  event.ignoreWord(true);
		  /*
		  if (event.getInvalidWord().length() > 3) {
			  misspelled.add(event.getInvalidWord());
			  System.out.println("misspelled: " + event.getInvalidWord());
			  List suggestions = event.getSuggestions();
			  Iterator i = suggestions.iterator();
			  while (i.hasNext()) {
				  String suggestion = i.next().toString();
				  System.out.println("   > " + suggestion);
			  }
		  }
		  */
		  numInvalidWords ++;
	}

	
	public static void main (String[] args) {
		Word w = new Word ();
		w.setContent("tu");
		System.out.println (w.getContent());
		modify (w);
		System.out.println (w.getContent());
	}
	
	public static void modify (Word s) {
		s.setContent("eu");
	}
}
