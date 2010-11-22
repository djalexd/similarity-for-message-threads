package project.client.gadgets;

import project.client.persistence.Message;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GadgetMessage extends Composite {
	
	private static final int MAX_CHARACTERS = 255;
	private static final NumberFormat FORMAT_FLOATS = NumberFormat.getFormat("#0.00");	
	
	private static final String STYLE_MAIN    = "Message";
	private static final String STYLE_HEADER  = "header"; 
	private static final String STYLE_CONTENT = "content";
	private static final String STYLE_HTML    = "html";
	private static final String STYLE_PANEL   = "panel";
	private static final String STYLE_LABEL   = "label";
	private static final String STYLE_INFO    = "label-info";
	private static final String STYLE_LINK    = "label-link";

	private Panel mainPanel = null;	
	private Panel headerPanel = null;
	private Panel contentPanel = null;
	
	private Message message;
	private boolean shortedMessage = false;
	private GadgetSearchPage searcher = null;
	
	public GadgetMessage (GadgetSearchPage searcher, Message message) {
		this.message = message;
		this.searcher = searcher;
		
		mainPanel = new VerticalPanel ();
		mainPanel.setWidth("100%");
		mainPanel.setStyleName(STYLE_MAIN);
		initWidget(mainPanel);
		
		headerPanel = new VerticalPanel ();
		headerPanel.setWidth("100%");
		headerPanel.addStyleName(STYLE_HEADER);
		mainPanel.add(headerPanel);
		
	
		contentPanel = new VerticalPanel ();
		contentPanel.setWidth("100%");
		contentPanel.addStyleName(STYLE_CONTENT);
		mainPanel.add(contentPanel);
		
		this.populateContent();
		this.populateHeader();
	}
	
	private void populateHeader () {
		
		headerPanel.clear();
		Label lbl = null;
		Panel hPanel = null;
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
	
		lbl = new Label ("Original thread ");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		lbl = new Label (message.getMessageThread().getName());
		lbl.addStyleName(STYLE_INFO);
		hPanel.add(lbl);
		
		lbl = new Label (">> view thread");
		lbl.addStyleName(STYLE_LINK);
		lbl.addClickListener(new ClickListener () {
				public void onClick (Widget w) {
					searcher.search("thread:" + message.getMessageThread().getName());
				}
		});
		hPanel.add(lbl);
		headerPanel.add(hPanel);
		
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);	
		
		lbl = new Label ("User ");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		lbl = new Label (message.getUser().getName());
		lbl.addStyleName(STYLE_INFO);
		hPanel.add(lbl);
		
		lbl = new Label (">> view social data");
		lbl.addStyleName(STYLE_LINK);
		lbl.addClickListener(new ClickListener () {
				public void onClick (Widget w) {
					//TODO relay to social data
				}
		});
		hPanel.add(lbl);
		headerPanel.add(hPanel);
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		lbl = new Label ("Relevance ");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		lbl = new Label ("" + FORMAT_FLOATS.format(message.getRelevance()));
		lbl.addStyleName(STYLE_INFO);
		hPanel.add(lbl);
		
		lbl = new Label ("view similar messages");
		lbl.addStyleName(STYLE_LINK);
		lbl.addClickListener(new ClickListener () {
				public void onClick (Widget w) {
					searcher.search("similar:" + message.getId());
				}
		});
		hPanel.add(lbl);
		headerPanel.add(hPanel);
	}
	
	private void populateContent () {
		
		contentPanel.clear();
		
		//
		//
		//
		String content = message.getContent().replaceAll("\n", "<br/>");
		content = content.trim();
		while (content.startsWith("<br/>")) {
			content = content.substring(5);
			content = content.trim();
		}
		
		if (content.length() > MAX_CHARACTERS) {
			content = content.substring(0, MAX_CHARACTERS);
			content += " ... <br/><span style=\"color : lightgrey; font-style : italic;\">read the entire message</span>";
			shortedMessage = true;
		}
		
		final HTML html = new HTML (content);
		html.setWidth("100%");
		html.addStyleName(STYLE_HTML);
		contentPanel.add(html);

		if (isTooLong()) {
			html.addClickListener(new ClickListener () {
					public void onClick (Widget w) {
						if (shortedMessage) {
							
							String content = message.getContent().replaceAll("\n", "<br/>");
							content = content.trim();
							while (content.startsWith("<br/>")) {
								content = content.substring(5);
								content = content.trim();
							}							
							html.setHTML (content);
							shortedMessage = false;
							
						} else {
							
							String content = message.getContent().replaceAll("\n", "<br/>");
							content = content.trim();
							while (content.startsWith("<br/>")) {
								content = content.substring(5);
								content = content.trim();
							}
							
							if (content.length() > MAX_CHARACTERS) {
								content = content.substring(0, MAX_CHARACTERS);
								content += " ... <br/><span style=\"color : lightgrey; font-style : italic;\">read the entire message</span>";
								html.setHTML (content);
								shortedMessage = true;
							}							
						}
					}
			});
		}
	}
	
	private boolean isTooLong () {
		return (message.getContent().length() > MAX_CHARACTERS);
	}
}
