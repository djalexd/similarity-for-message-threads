package project.client.gadgets;

import java.util.Iterator;
import java.util.List;

import project.client.ServletRpc;
import project.client.persistence.Message;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GadgetSearchPage extends Composite implements ClickListener {

	private static final String STYLE_MAIN = "search";
	private static final String STYLE_PANEL = "hPanel";
	private static final String STYLE_LABEL = "label";
	private static final String STYLE_TEXTBOX = "textbox";
	private static final String STYLE_BUTTON_OK = "btnOk";	
	private static final String STYLE_CONTENT_PANEL = "contentPanel";
	
	private static final String STYLE_NO_MESSAGE = "noMessage";
	private static final String STYLE_NO_MESSAGE_HTML = "html";

	private Panel mainPanel;
	private TextBox m_tbSearch;
	private Panel contentPanel;
	private Button m_btnOk;
	private CheckBox m_chkUserRelevance;
	
	public GadgetSearchPage () {
		
		mainPanel = new VerticalPanel ();
		mainPanel.setStyleName(STYLE_MAIN);
		this.initWidget(mainPanel);
		
		Panel hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		
		Label lbl = new Label ("Type in your search here ");
		lbl.addStyleName(STYLE_LABEL);
		hPanel.add(lbl);
		
		m_tbSearch = new TextBox ();
		m_tbSearch.addStyleName(STYLE_TEXTBOX);
		m_tbSearch.addKeyboardListener(new KeyboardListener () {
			public void onKeyUp (Widget w, char code, int modifiers) {
				if (code == KEY_ENTER) {
					m_btnOk.click();
				}
			}
			
			public void onKeyDown (Widget w, char code, int modifiers) {
				
			}
			
			public void onKeyPress (Widget w, char code, int modifiers) {
				
			}			
		});
		hPanel.add(m_tbSearch);
		
		//
		//
		m_btnOk = new Button ("Search"); 
		m_btnOk.addStyleName(STYLE_BUTTON_OK);
		m_btnOk.addClickListener(this);
		hPanel.add(m_btnOk);
		mainPanel.add(hPanel);
		
		
		hPanel = new HorizontalPanel ();
		hPanel.addStyleName(STYLE_PANEL);
		m_chkUserRelevance = new CheckBox ("Apply user relevance when searching");
		hPanel.add(m_chkUserRelevance);
		mainPanel.add(hPanel);
		
		
		contentPanel = new VerticalPanel ();
		contentPanel.addStyleName(STYLE_CONTENT_PANEL);
		mainPanel.add(contentPanel);
	}

	
	public void onClick(Widget arg0) {
		
		final GadgetSearchPage searcher = this;
		contentPanel.clear();
		contentPanel.add(this.getSearchingPanel());
		
		if (arg0.equals(m_btnOk)) {

			if (m_tbSearch.getText().length() == 0) {
				Window.alert("Query is empty");
				return;
			}

			if (m_tbSearch.getText().startsWith("similar:")) {

				//
				// look for similar messages
				//
				
				String msgId = m_tbSearch.getText().substring(m_tbSearch.getText().indexOf(':') + 1);
				ServletRpc.getRpcInterface().getSimilarMessages(Integer.parseInt(msgId), 10, new AsyncCallback<List<Message>> () {
					public void onSuccess (List<Message> result) {

						contentPanel.clear();
						if (result == null || result.size() == 0) {
							contentPanel.add(getNoMessagePanel());
							return;
						}

						Iterator<Message> i = result.iterator();
						while (i.hasNext()) {

							Message msg = i.next();
							contentPanel.add(new GadgetMessage (searcher, msg));

						}

					}

					public void onFailure (Throwable t) {
						Window.alert("Exception : " + t.getMessage());
					}
				});
				
			} else if (m_tbSearch.getText().startsWith("thread:")) {
				
				//
				// look for the thread
				//
				
				String threadName = m_tbSearch.getText().substring(m_tbSearch.getText().indexOf(':') + 1);
				ServletRpc.getRpcInterface().searchForThread(threadName, new AsyncCallback<List<Message>> () {
					public void onSuccess (List<Message> result) {

						contentPanel.clear();
						if (result == null || result.size() == 0) {
							contentPanel.add(getNoMessagePanel());
							return;
						}

						Iterator<Message> i = result.iterator();
						while (i.hasNext()) {

							Message msg = i.next();
							contentPanel.add(new GadgetMessage (searcher, msg));

						}

					}

					public void onFailure (Throwable t) {
						Window.alert("Exception : " + t.getMessage());
					}
				});
				
			} else {
				
				String[] tokens = m_tbSearch.getText().split(" ");
				if (tokens.length == 0) {
					// this is weird, it should never happen
					//
					Window.alert("Query is empty (after tokenization)");
					return;
				}

				ServletRpc.getRpcInterface().search(tokens, 10, m_chkUserRelevance.isChecked(), new AsyncCallback<List<Message>> () {
					public void onSuccess (List<Message> result) {

						contentPanel.clear();
						if (result == null || result.size() == 0) {
							contentPanel.add(getNoMessagePanel());
							return;
						}

						Iterator<Message> i = result.iterator();
						while (i.hasNext()) {

							Message msg = i.next();
							contentPanel.add(new GadgetMessage (searcher, msg));

						}

					}

					public void onFailure (Throwable t) {
						Window.alert("Exception : " + t.getMessage());
					}
				});
				
			}
		} 
			
	}
	
	
	public void search (String text) {
		m_tbSearch.setText(text);
		m_btnOk.click();
	}
	
	
	private Panel getNoMessagePanel () {
		Panel pnl = new VerticalPanel ();
		pnl.setStyleName(STYLE_NO_MESSAGE);
		
		HTML html = new HTML ("Unfortunately, <b>no message</b> matched your query!");
		html.addStyleName(STYLE_NO_MESSAGE_HTML);
		pnl.add(html);
		
		return pnl;
	}
	
	
	private Panel getSearchingPanel () {
		Panel pnl = new VerticalPanel ();
		pnl.setStyleName(STYLE_NO_MESSAGE);
		
		HTML html = new HTML ("<i>Searching ...</i>");
		html.addStyleName(STYLE_NO_MESSAGE_HTML);
		pnl.add(html);
		
		return pnl;
	}
	
}
