package project.client.gadgets;


import project.client.ServletRpc;
import project.client.gadgets.events.Events;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GadgetAddCrawlerSettings extends Composite {

	private static final String STYLE_MAIN = "addCrawlerSettings";
	private static final String STYLE_PANEL = "hPanel";
	private static final String STYLE_LABEL = "label";
	private static final String STYLE_TEXTBOX = "textbox";
	private static final String STYLE_BUTTON_OK = "btnOk";

	private Panel mainPanel;	
	
	private TextBox m_tbForumType;
	private TextBox m_tbCrawlerClass;
	private TextBox m_tbCrawlerThreadClass;
	private TextBox m_tbBaseUrl;
	
	public GadgetAddCrawlerSettings () {
	
		mainPanel = new VerticalPanel ();
		mainPanel.setStylePrimaryName(STYLE_MAIN);
		this.initWidget(mainPanel);
		
		Panel hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		Label lbl = new Label ("Forum type");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbForumType = new TextBox ();
		m_tbForumType.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbForumType);
		
		mainPanel.add(hPanel);

		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Crawler class");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbCrawlerClass = new TextBox ();
		m_tbCrawlerClass.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbCrawlerClass);
		
		mainPanel.add(hPanel);
		

		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Thread class");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbCrawlerThreadClass = new TextBox ();
		m_tbCrawlerThreadClass.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbCrawlerThreadClass);
		
		mainPanel.add(hPanel);		
	
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Base url");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbBaseUrl = new TextBox ();
		m_tbBaseUrl.addStyleName(STYLE_TEXTBOX);
		hPanel.add(m_tbBaseUrl);
		
		mainPanel.add(hPanel);		
		
		final Button btnOk = new Button ("OK");
		btnOk.addStyleName(STYLE_BUTTON_OK);
		btnOk.addClickListener(new ClickListener () { 
					public void onClick (Widget w) {
						RequestBuilder b = new RequestBuilder (RequestBuilder.GET, m_tbBaseUrl.getText());
						b.setCallback(new RequestCallback () {
							  public void onResponseReceived(Request arg0, com.google.gwt.http.client.Response arg1) {
								  
								  ServletRpc.getRpcInterface().
								  	insertCrawlerSettings(m_tbForumType.getText(), 
								  						  m_tbCrawlerClass.getText(), 
								  						  m_tbCrawlerThreadClass.getText(), 
								  						  m_tbBaseUrl.getText(), 
								  						  new AsyncCallback<String> () {
								  		
								  								public void onSuccess (String result) {
								  									Window.alert(result);
								  									Events.throwEvent("show", null);
								  								}
								  								
								  								public void onFailure (Throwable t) { 
								  									Window.alert(t.getMessage());
								  									Events.throwEvent("show", null);
								  								}
								  						});
							  }
							  
							  public void onError(Request arg0, Throwable arg1) {
								  Window.alert("Invalid Url : " + m_tbBaseUrl.getText());
							  }
						});
						
						try {
							b.send();
						} catch (Exception e) {
							Window.alert("Exception : " + e.getMessage());
						}
					}
		});
		
		mainPanel.add(btnOk);
	}
	
}
