package project.client.gadgets;

import java.util.Iterator;
import java.util.Map;

import project.client.ServletRpc;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GadgetStatisticsPage extends Composite {
	
	private static final NumberFormat FORMAT_FLOATS = NumberFormat.getFormat("#.00");	
	
	private static final String STYLE_MAIN = "StatisticsPage";
	private static final String STYLE_PANEL = "panel";
	private static final String STYLE_HTML = "html";
	private static final String STYLE_LABEL = "label";
	private static final String STYLE_INFO ="label-info";
	
	private static final String GENERAL_INFO = "These are general statistics, related to all boards";

	private Panel mainPanel = null;
	private Panel generalContentPanel = null;
	private Panel generalContentPanelInfo = null;
	
	public GadgetStatisticsPage () {
		mainPanel = new VerticalPanel ();
		mainPanel.setStyleName(STYLE_MAIN);
		initWidget(mainPanel);
		
		Panel hPanel = new VerticalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		HTML html = new HTML (GENERAL_INFO);
		html.addStyleName(STYLE_HTML);
		hPanel.add (html);
		
		generalContentPanel = new VerticalPanel ();
		generalContentPanel.addStyleName(STYLE_PANEL);
		generalContentPanel.add(html);
		mainPanel.add(generalContentPanel);
		
		generalContentPanelInfo = new VerticalPanel ();
		generalContentPanel.add(generalContentPanelInfo);
		
		displayGeneralStatistics();
	}
	
	public void displayGeneralStatistics () {
		
		generalContentPanelInfo.clear();
		
		ServletRpc.getRpcInterface().getStatistics(null, new AsyncCallback<Map<String,String>> () {
			public void onFailure (Throwable t) {
				Window.alert("Exception : " + t.getMessage());
			}
			
			public void onSuccess (Map<String,String> result) {
				Panel panel = null;
				Label lbl = null;
				generalContentPanelInfo.clear();
				
				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Total number of words");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				lbl = new Label (result.get("num-words"));
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);
				

				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Distinct words");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				lbl = new Label (result.get("num-distinct-words"));
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);
				
				
				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Total number of boards");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				lbl = new Label (result.get("num-boards"));
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);
				
				
				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Total number of discussions");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				lbl = new Label (result.get("num-threads"));
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);
				
				
				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Total number of messages");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				lbl = new Label (result.get("num-messages"));
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);
				
				
				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Invalid messages");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				int numInvalid = Integer.parseInt(result.get("num-invalid-messages"));
				int numMessages = Integer.parseInt(result.get("num-messages"));
				float proc = (float) numInvalid * 100.0f / numMessages;
				lbl = new Label ("" + numInvalid + "(" + FORMAT_FLOATS.format(proc) + " %)");
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);

				
				panel = new HorizontalPanel ();
				panel.addStyleName(STYLE_PANEL);
				
				lbl = new Label ("Total number of users");
				lbl.addStyleName(STYLE_LABEL);
				panel.add(lbl);
				
				lbl = new Label (result.get("num-users"));
				lbl.addStyleName(STYLE_INFO);
				panel.add(lbl);
				generalContentPanelInfo.add(panel);
				
			}
		});
		
	}
}
