package project.core.mbeans.analysis;

import java.io.IOException;
import java.util.Map;

import javax.ejb.Remote;


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
