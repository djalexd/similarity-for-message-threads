package project.core.crawlers;

import java.util.Map;

import com.google.inject.Inject;

import project.core.mbeans.crawlers.ThreadCrawlerMBean;
import project.core.mbeans.database.ConnectionManager;


public class ThreadCrawlerImpl implements ThreadCrawlerMBean {
	ConnectionManager manager;
	
	@Inject
	public ThreadCrawlerImpl (ConnectionManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void start() {
		throw new RuntimeException();
	}

	@Override
	public void stop() {
		throw new RuntimeException();
	}

	@Override
	public void addCrawler(String crawlerName, String crawlerType, String url,
			int minSleepPerPage, int maxSleepPerPage, int minSleepPerThread,
			int maxSleepPerThread, int startPage, int maxPage,
			boolean roundRobin, boolean startNow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Map<String, String>> getCrawlers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getCrawlerSettings(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
