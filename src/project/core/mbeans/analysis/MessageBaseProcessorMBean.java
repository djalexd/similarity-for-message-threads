package project.core.mbeans.analysis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import edu.mit.jwi.item.POS;

import project.client.persistence.Message;
import project.client.persistence.MessageBoard;
import project.client.persistence.MessageThread;
import project.client.persistence.Word;
import project.utils.statistics.WordRank;

@Remote
public interface MessageBaseProcessorMBean {


	/**
	 * Returns a list of synonyms for a specified word 
	 * @param word
	 * @return
	 * @throws IOException
	 */
	public String[] getSynonyms (String word)
							throws IOException;

	public Map<String,String> getWordStatistics (int minMsgId, int maxMsgId);
}
