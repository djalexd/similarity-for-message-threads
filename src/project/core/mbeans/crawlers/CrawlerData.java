package project.core.mbeans.crawlers;

import java.util.HashMap;
import java.util.Map;

public class CrawlerData {
	
	private String name;
	private String url;
	private String type;
	private int currentPage, startPage, endPage;
	private int minSleepSecsPage, maxSleepSecsPage, minSleepSecsThread, maxSleepSecsThread;
	
	private CrawlerSettingsData settings;
	
	private boolean active;
	private boolean roundRobin;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getStartPage() {
		return startPage;
	}
	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}
	public int getEndPage() {
		return endPage;
	}
	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}
	public int getMinSleepSecsPage() {
		return minSleepSecsPage;
	}
	public void setMinSleepSecsPage(int minSleepSecsPage) {
		this.minSleepSecsPage = minSleepSecsPage;
	}
	public int getMaxSleepSecsPage() {
		return maxSleepSecsPage;
	}
	public void setMaxSleepSecsPage(int maxSleepSecsPage) {
		this.maxSleepSecsPage = maxSleepSecsPage;
	}
	public int getMinSleepSecsThread() {
		return minSleepSecsThread;
	}
	public void setMinSleepSecsThread(int minSleepSecsThread) {
		this.minSleepSecsThread = minSleepSecsThread;
	}
	public int getMaxSleepSecsThread() {
		return maxSleepSecsThread;
	}
	public void setMaxSleepSecsThread(int maxSleepSecsThread) {
		this.maxSleepSecsThread = maxSleepSecsThread;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isRoundRobin() {
		return roundRobin;
	}
	public void setRoundRobin(boolean roundRobin) {
		this.roundRobin = roundRobin;
	}
	
	
	public Map<String,String> getData () {
		
		Map<String,String> data = new HashMap<String, String> ();
		
		data.put("name", this.getName());
		data.put("url", this.getUrl());
		data.put("type", this.getType());
		data.put("current-page", "" + this.getCurrentPage());
		data.put("start-page", "" + this.getStartPage());
		data.put("end-page", "" + this.getEndPage());
		data.put("sleep-per-page", "" + this.getMinSleepSecsPage() + "-" + this.getMaxSleepSecsPage());
		data.put("thread.sleep-per-thread", "" + this.getMinSleepSecsThread() + "-" + this.getMaxSleepSecsThread());
		data.put("status", "" + this.isActive());
		data.put("strategy", "" + this.isRoundRobin());
		
		return data;
	}
	
	
	public void setData (Map<String,String> data) {
		
		this.setName(data.get("name"));
		this.setUrl(data.get("url"));
		this.setType(data.get("type"));
		this.setCurrentPage(Integer.parseInt(data.get("current-page")));
		this.setStartPage(Integer.parseInt(data.get("start-page")));
		this.setEndPage(Integer.parseInt(data.get("end-page")));
		this.setActive(Boolean.parseBoolean(data.get("status")));
		this.setRoundRobin(Boolean.parseBoolean(data.get("strategy")));
		
		String sleep1 = data.get("sleep-per-page");
		if (sleep1.contains("-")) {
			int x1 = Integer.parseInt(sleep1.substring(0,sleep1.indexOf('-')));
			int x2 = Integer.parseInt(sleep1.substring(sleep1.indexOf('-') + 1));
			this.setMinSleepSecsPage(x1);
			this.setMaxSleepSecsPage(x2);
		} else {
			int x = Integer.parseInt(sleep1);
			this.setMinSleepSecsPage(x);
			this.setMaxSleepSecsPage(x);
		}

		String sleep2 = data.get("thread.sleep-per-thread");
		if (sleep2.contains("-")) {
			int x1 = Integer.parseInt(sleep2.substring(0,sleep2.indexOf('-')));
			int x2 = Integer.parseInt(sleep2.substring(sleep2.indexOf('-') + 1));
			this.setMinSleepSecsThread(x1);
			this.setMaxSleepSecsThread(x2);
		} else {
			int x = Integer.parseInt(sleep2);
			this.setMinSleepSecsThread(x);
			this.setMaxSleepSecsThread(x);
		}
	}

	public CrawlerSettingsData getSettings() {
		return settings;
	}
	public void setSettings(CrawlerSettingsData settings) {
		this.settings = settings;
	}
		
	public static int getPropertyCount () {
		// TODO update this count everytime modifications occur to the number of 
		// properties in this class
		return 10;
	}
}
