package project.client;

import project.client.gadgets.GadgetCrawlerPage;
import project.client.gadgets.GadgetSearchPage;
import project.client.gadgets.GadgetStatisticsPage;

import com.google.gwt.core.client.*;
import com.google.gwt.user.client.ui.*;

public class Main implements EntryPoint {
	
	private static final String STR_SERVLET_PATH = "/project-servlets/RpcServlet";
	
	public void onModuleLoad() {

		//
		// init servlet connection
		//
		ServletRpc.initAsyncInterface(STR_SERVLET_PATH);

		//
		// attach the main panel
		//
		final Panel contentPanel = new VerticalPanel ();
		RootPanel.get().add(contentPanel);

		//
		// create the content
		//
		final TabPanel panel = new TabPanel ();
		panel.add(new GadgetCrawlerPage (), "Crawling");
		panel.add(new GadgetStatisticsPage (), "Statistics");
		panel.add(new GadgetSearchPage (), "Search");
		panel.addTabListener(new TabListener () {
			public void onTabSelected (SourcesTabEvents event, int index) {
				if (index == 0) {
					GadgetCrawlerPage c = (GadgetCrawlerPage) panel.getWidget(0);
					c.displayCrawlers();
				} else if (index == 1) {
					GadgetStatisticsPage s = (GadgetStatisticsPage) panel.getWidget(1);
					s.displayGeneralStatistics();
				} else {
					// TODO reset the search
				}
			}
			
			public boolean onBeforeTabSelected (SourcesTabEvents event, int index) {
				return true;
			}
		});
		
		panel.selectTab(2); // select search tab
		
		//
		// add the content
		//
		contentPanel.add(panel);		
	}
}
