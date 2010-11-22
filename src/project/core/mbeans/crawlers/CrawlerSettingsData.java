package project.core.mbeans.crawlers;

import java.util.HashMap;
import java.util.Map;

public class CrawlerSettingsData {

	private String url;
	private String type;
	private String crawlerClass, crawlerThreadClass;
	
	
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
	public String getCrawlerClass() {
		return crawlerClass;
	}
	public void setCrawlerClass(String crawlerClass) {
		this.crawlerClass = crawlerClass;
	}
	public String getCrawlerThreadClass() {
		return crawlerThreadClass;
	}
	public void setCrawlerThreadClass(String crawlerThreadClass) {
		this.crawlerThreadClass = crawlerThreadClass;
	}
	
	public Map<String,String> getData () {
		
		Map<String,String> data = new HashMap<String, String> ();
		
		data.put("base-url",      this.getUrl());
		data.put("forum-type",    this.getType());
		data.put("crawler-class", this.getCrawlerClass());
		data.put("thread-class",  this.getCrawlerThreadClass());

		return data;
	}
	
	public void setData (Map<String,String> data) {
		
		this.setUrl(data.get("base-url"));
		this.setType(data.get("forum-type"));
		this.setCrawlerClass(data.get("crawler-class"));
		this.setCrawlerThreadClass(data.get("thread-class"));
		
	}
	
	
	public static int getPropertyCount () {
		// TODO update this count everytime modifications occur to the number of 
		// properties in this class
		return 4;
	}	
}
