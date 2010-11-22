package project.client.gadgets;

import java.util.Map;

import project.client.ServletRpc;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GadgetCrawler extends Composite {
	
	private String STYLE_MAIN = "Crawler";	
	private String STYLE_PANEL = "panel";
	private String STYLE_NAME = "name";
	private String STYLE_ACTIVE = "active";
	private String STYLE_INACTIVE = "inactive";
	private String STYLE_LABEL_NAME = "label";
	private String STYLE_LABEL = "label-info";
	private String STYLE_EDIT_LABEL = "edit";

	private Panel mainPanel = null;
	private String crawlerName = null;
	private boolean active = false;
	
	public GadgetCrawler (String name) {
		
		mainPanel = new VerticalPanel ();
		mainPanel.setStyleName(STYLE_MAIN);
		initWidget(mainPanel);
		
		this.crawlerName = name;
		ServletRpc.getRpcInterface()
		.getCrawlers(this.crawlerName, new AsyncCallback<Map<String,Map<String,String>>> () {
			
			public void onFailure (Throwable t) {
				Window.alert("Exception : " + t.getMessage());
			}
			
			public void onSuccess (Map<String,Map<String, String>> result) {
				
				Map<String,String> map = result.get(crawlerName);
				if (map.get("status").equals("true"))
					active = true;
				else
					active = false;
				
				//
				//
				mainPanel.clear();
				Panel hPanel = null; Label lbl = null;
				
				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label(map.get("name"));
				lbl.addStyleName(STYLE_NAME);
				hPanel.add(lbl);
				
				final Label lblActive = new Label ();
				if (active) {
					lblActive.setText("(active)");
					lblActive.addStyleName(STYLE_ACTIVE);
				}
				else {
					lblActive.setText("(inactive)");
					lblActive.addStyleName(STYLE_INACTIVE);
				}
				
				lblActive.addClickListener(new ClickListener () {
						public void onClick (Widget w) {
							if (active)
								lblActive.removeStyleName(STYLE_ACTIVE);
							else
								lblActive.removeStyleName(STYLE_INACTIVE);
							
							active = !active;
							
							if (active)
								lblActive.addStyleName(STYLE_ACTIVE);
							else
								lblActive.addStyleName(STYLE_INACTIVE);
							
							ServletRpc.getRpcInterface().activateCrawler(crawlerName, active, 
									new AsyncCallback<String> () {
										public void onFailure (Throwable t) {
											Window.alert("Exception : " + t.getMessage());
										}
										
										public void onSuccess (String result) {
											
										}
							});
						}
				});
				
				hPanel.add(lblActive);
				mainPanel.add(hPanel);
				
				
				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label("Type: ");
				lbl.addStyleName(STYLE_LABEL_NAME);
				hPanel.add(lbl);
				
				lbl = new Label(map.get("type"));
				lbl.addStyleName(STYLE_LABEL);
				hPanel.add(lbl);
				mainPanel.add(hPanel);
				

				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label("Url: ");
				lbl.addStyleName(STYLE_LABEL_NAME);
				hPanel.add(lbl);
				
				lbl = new Label(map.get("url"));
				lbl.addStyleName(STYLE_LABEL);
				hPanel.add(lbl);
				mainPanel.add(hPanel);
				
				
				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label("Indexing (current / end): ");
				lbl.addStyleName(STYLE_LABEL_NAME);
				hPanel.add(lbl);
				
				lbl = new Label(map.get("current-page") + " / " + map.get("end-page"));
				lbl.addStyleName(STYLE_LABEL);
				hPanel.add(lbl);
				mainPanel.add(hPanel);
				
				
				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label("Sleep (per page / per thread): ");
				lbl.addStyleName(STYLE_LABEL_NAME);
				hPanel.add(lbl);
				
				lbl = new Label(map.get("sleep-per-page") + " / " + map.get("thread.sleep-per-thread"));
				lbl.addStyleName(STYLE_LABEL);
				hPanel.add(lbl);
				mainPanel.add(hPanel);
				
				
				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label("Productivity: ");
				lbl.addStyleName(STYLE_LABEL_NAME);
				hPanel.add(lbl);
				
				String strSleep = map.get("thread.sleep-per-thread");
				int avgSleep = 0;
				if (strSleep.contains("-")) {
					String s1 = strSleep.substring(0, strSleep.indexOf('-'));
					String s2 = strSleep.substring(strSleep.indexOf('-') + 1);
					avgSleep = (Integer.parseInt(s1) + Integer.parseInt(s2)) / 2;
				} else {
					avgSleep = Integer.parseInt(strSleep);
				}
				
				lbl = new Label("" + (3600 / avgSleep) + " threads / hour");
				lbl.addStyleName(STYLE_LABEL);
				hPanel.add(lbl);
				mainPanel.add(hPanel);
				
				hPanel = new HorizontalPanel ();
				hPanel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("edit this crawler");
				lbl.addStyleName(STYLE_EDIT_LABEL);
				lbl.addClickListener(new ClickListener () {
					public void onClick (Widget w) {
						//TODO relay to edit
					}
				});
				
				hPanel.add(lbl);
				mainPanel.add(hPanel);
			}
		});
	}
}
