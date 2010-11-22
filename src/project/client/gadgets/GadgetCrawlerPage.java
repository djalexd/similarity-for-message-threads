package project.client.gadgets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import project.client.ServletRpc;
import project.client.gadgets.events.EventListener;
import project.client.gadgets.events.Events;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GadgetCrawlerPage extends Composite implements EventListener {
	
	private static final String STYLE_MAIN = "CrawlerPage";
	private static final String STYLE_PANEL = "panel";
	private static final String STYLE_SETTINGS = "settings";

	private Panel mainPanel = null;
	private Panel mainCrawlerPanel = null;
	
	public GadgetCrawlerPage () {
		mainPanel = new VerticalPanel ();
		mainPanel.setStyleName(STYLE_MAIN);
		initWidget(mainPanel);
		
		//
		//
		Events.addListener("show", this);
	
		Panel hPanel = null;
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		final Label lblAddSettings = new Label ("Add new settings");
		lblAddSettings.addStyleName(STYLE_SETTINGS);
		lblAddSettings.addClickListener(new ClickListener () {
			public void onClick (Widget w) {
				mainPanel.clear();
				mainPanel.add(new GadgetAddCrawlerSettings ());
			}
		});
		
		hPanel.add(lblAddSettings);
		mainPanel.add(hPanel);
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		final Label lblAddCrawler = new Label ("Add a new crawler");
		lblAddCrawler.addStyleName(STYLE_SETTINGS);
		lblAddCrawler.addClickListener(new ClickListener () {
			public void onClick (Widget w) {
				mainPanel.clear();
				mainPanel.add(new GadgetAddCrawler ());
			}
		});
		
		hPanel.add(lblAddCrawler);
		mainPanel.add(hPanel);
		
		mainCrawlerPanel = new VerticalPanel ();
		mainPanel.add(mainCrawlerPanel);
		
		displayCrawlers();
	}
	
	
	public void displayCrawlers () {
		mainCrawlerPanel.clear();
		
		ServletRpc.getRpcInterface().getCrawlers(null, new AsyncCallback<Map<String,Map<String,String>>> () {
			public void onFailure (Throwable t) {
				Window.alert("Exception : " + t.getMessage());
			}
			
			public void onSuccess (Map<String,Map<String,String>> result) {
				Iterator<String> i = result.keySet().iterator();
				while (i.hasNext()) {
					String name = i.next();
					mainCrawlerPanel.add(new GadgetCrawler (name));
				}
			}
		});
		
	}

	
	public void handleEvent(String name, List<String> params) {
		if (name.equals("show")) {
			this.displayCrawlers();
		}
	}
}
