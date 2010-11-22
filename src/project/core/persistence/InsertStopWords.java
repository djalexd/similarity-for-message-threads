package project.core.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import project.client.persistence.Word;

public class InsertStopWords {
	
	private static final String STR_STOPWORD_LOCATION = "/home/alexd/workspace/proiect-diploma/config/stop-words.in";
	
	PersistenceLoaderMBean loader = new PersistenceLoader ();
	
	public InsertStopWords () {
		loader.start();
	}
	
	public void insertOrUpdateStopWords () {
		try {
			BufferedReader in = new BufferedReader (new FileReader (new File (STR_STOPWORD_LOCATION)));
			String word = null;
			
			while ( (word = in.readLine()) != null) {
				
				word = word.trim();
				Word w = loader.loadWord(word);
				if (w == null) {
					
					loader.insertWord(word, "stop");
					
				} else {
					
					//
					// word already exists, update its labels
					System.out.println ("No implemented yet : " + word + "(found " + w.getContent() + ")");
				}
			}
			
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args) {
		
		InsertStopWords i = new InsertStopWords ();
		i.insertOrUpdateStopWords();
	}
}
