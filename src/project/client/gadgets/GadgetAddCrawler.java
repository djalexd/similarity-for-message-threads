package project.client.gadgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.client.ServletRpc;
import project.client.gadgets.events.Events;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GadgetAddCrawler extends Composite {

	private static final String STYLE_MAIN = "addCrawler";
	private static final String STYLE_PANEL = "hPanel";
	private static final String STYLE_LABEL = "label";
	private static final String STYLE_TEXTBOX = "textbox";
	private static final String STYLE_BUTTON_OK = "btnOk";

	private Panel mainPanel;
	
	private TextBox m_tbCrawlerName;
	private TextBox m_tbForumUrl;
	private ListBox m_lbCrawlerType;

	private TextBox m_tbStartPage;
	private TextBox m_tbEndPage;
	private TextBox m_tbSleepPerThread;	
	private TextBox m_tbSleepPerPage;
	
	private CheckBox m_chkRoundRobin;
	private CheckBox m_chkStartImediately;
	
	private Button   m_btnStartStop;
	
	private boolean  m_bEditMode;

	private Map<String,Map<String, String>> crawlerProperties;
	
	public GadgetAddCrawler () {
		
		crawlerProperties = new HashMap<String, Map<String,String>> ();
		m_bEditMode = false;
		
		
		mainPanel = new VerticalPanel ();
		mainPanel.setStyleName(STYLE_MAIN);
		this.initWidget(mainPanel);
		
		
		Panel hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		Label lbl = new Label ("Crawler name");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbCrawlerName = new TextBox ();
		m_tbCrawlerName.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbCrawlerName);
		mainPanel.add(hPanel);
	
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Forum url");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbForumUrl = new TextBox ();
		m_tbForumUrl.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbForumUrl);
		mainPanel.add(hPanel);		
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Crawler type");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_lbCrawlerType = new ListBox (false);
		m_lbCrawlerType.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_lbCrawlerType);
		mainPanel.add(hPanel);
		
		//
		// Now create the request to populate m_lbCrawlerType
		ServletRpc.getRpcInterface()
				.getCrawlerSettings(
						null,
						new AsyncCallback<Map<String,Map<String,String>>> () {
							
							public void onFailure (Throwable t) {
								Window.alert (t.getMessage());
							}
							
							public void onSuccess (Map<String,Map<String,String>> result) {
								
								//
								// save for later use
								//
								crawlerProperties.putAll(result);
								
								Iterator<String> i = crawlerProperties.keySet().iterator();
								while (i.hasNext()) {
									
									String key = i.next();
									Map<String, String> map = crawlerProperties.get(key);
									m_lbCrawlerType.addItem(map.get("forum-type"));
									
								}
								
							}
							
						});
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Start page");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbStartPage = new TextBox ();
		m_tbStartPage.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbStartPage);
		mainPanel.add(hPanel);			
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("End page");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbEndPage = new TextBox ();
		m_tbEndPage.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbEndPage);
		mainPanel.add(hPanel);	
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Sleep time (per thread)");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbSleepPerThread = new TextBox ();
		m_tbSleepPerThread.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbSleepPerThread);
		mainPanel.add(hPanel);
		

		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Sleep time (per page)");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbSleepPerPage = new TextBox ();
		m_tbSleepPerPage.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbSleepPerPage);
		mainPanel.add(hPanel);
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);

		m_chkRoundRobin = new CheckBox("Enable Round-Robin strategy");
		m_chkRoundRobin.setChecked(true);
		hPanel.add(m_chkRoundRobin);		
		mainPanel.add(hPanel);
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);

		m_chkStartImediately = new CheckBox("Start now");
		m_chkStartImediately.setChecked(true);
		hPanel.add(m_chkStartImediately);
		mainPanel.add(hPanel);
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);

		final Button btnOk = new Button ("OK");
		btnOk.addStyleName(STYLE_BUTTON_OK);
		btnOk.addClickListener(new ClickListener () {
					public void onClick (Widget w) {
						
						final Map<String,String> map = findCrawlerProperties();
						
						RequestBuilder b = new RequestBuilder (RequestBuilder.GET, map.get("base-url") + "/" + m_tbForumUrl.getText());
						b.setCallback(new RequestCallback () {
							  public void onResponseReceived(Request arg0, com.google.gwt.http.client.Response arg1) {
								  
								  ServletRpc.getRpcInterface().
								  	insertCrawler(m_tbCrawlerName.getText(), 
								  				  m_tbForumUrl.getText(), 
								  				  map.get("forum-type"), 
								  				  Integer.parseInt(m_tbStartPage.getText()), 
								  				  Integer.parseInt(m_tbEndPage.getText()), 
								  				  m_tbSleepPerThread.getText(), 
								  				  m_tbSleepPerPage.getText(),  
								  				  m_chkRoundRobin.isChecked(), 
								  				  m_chkStartImediately.isChecked(), 
								  				  new AsyncCallback<String> () {

								  						public void onFailure (Throwable t) {
								  							Window.alert ("Exception : " + t.getMessage());
								  							Events.throwEvent("show", null);
								  						}
								  						
								  						public void onSuccess (String result) {
								  							Window.alert (result);
								  							Events.throwEvent("show", null);
								  						}
								  	});
								  
							  }
							  
							  public void onError(Request arg0, Throwable arg1) {
								  Window.alert("Invalid forum : " + m_tbForumUrl.getText());
							  }
						});
						
						try {
							b.send();
						} catch (Exception e) {
							Window.alert("Exception : " + e.getMessage());
						} 
					}
		});
		hPanel.add(btnOk);
		
		m_btnStartStop = new Button ("");
		m_btnStartStop.addStyleName(STYLE_BUTTON_OK);
		hPanel.add(m_btnStartStop);
		
		mainPanel.add(hPanel);
		
		this.updateVisualEditMode();
	}
	
	
	private Map<String,String> findCrawlerProperties () {
		Iterator<String> i = crawlerProperties.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			Map<String,String> map = crawlerProperties.get(key);
			if (map.get("forum-type").equals(m_lbCrawlerType.getItemText(m_lbCrawlerType.getSelectedIndex()))) {
				return map;
			}
			
		}
		
		return null;
	}
	
	
	public void setEditMode (boolean edit) {
		
		m_bEditMode = edit;
		this.updateVisualEditMode();
	}
	
	
	public void loadEditCrawler (String name) {
		
		final String finalName = name;
		
		ServletRpc.getRpcInterface().getCrawlers(finalName,new AsyncCallback<Map<String,Map<String,String>>> () {
						public void onFailure (Throwable t) {
							Window.alert("Exception : " + t.getMessage());
						}
						
						public void onSuccess (Map<String,Map<String,String>> result) {
							
							Iterator<String> i = result.keySet().iterator();
							while (i.hasNext()) {
								
								String keyName = i.next();
								if (finalName.equals(keyName)) {
									
									Map<String,String> map = result.get(keyName);
									m_tbCrawlerName.setText(finalName);
									m_tbStartPage.setText(map.get("current-page"));
									m_tbEndPage.setText(map.get("end-page"));
									m_tbForumUrl.setText(map.get("url"));
									m_tbSleepPerPage.setText(map.get("sleep-per-page"));
									m_tbSleepPerThread.setText(map.get("sleep-per-thread"));
									
									//m_lbCrawlerType.setSelectedIndex(0); TODO fix to find the index of correct forum type
									break;
								}
								
							}
							
						}
		});
	}
	
	private void updateVisualEditMode () {
		
		if (m_bEditMode) {
			
			m_lbCrawlerType.setEnabled(false);
			m_tbCrawlerName.setEnabled(false);
			m_chkStartImediately.setVisible(false);
			m_chkRoundRobin.setEnabled(false);
			m_btnStartStop.setVisible(true);
			
		} else {
			
			m_lbCrawlerType.setEnabled(true);
			m_tbCrawlerName.setEnabled(true);
			m_chkStartImediately.setVisible(true);
			m_chkRoundRobin.setEnabled(true);
			m_btnStartStop.setVisible(false);
		}
	}
}
