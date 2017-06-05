package ca.uqac.lif.ResponsiveTests;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class Main{

	public static void main(String[] args) {
		CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("https://www.xkcd.com");
		builder.crawlRules().insertRandomDataInInputForms(false);
		
		CorniSelWebDriverBrowserBuilder corniSelBrowserBuilder = new CorniSelWebDriverBrowserBuilder("");
		corniSelBrowserBuilder.addEvaluationListener(new CorniSelWebDriverListener(System.getProperty("user.dir") + "/out/out.txt"));
		
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME, 1, corniSelBrowserBuilder));
		builder.crawlRules().dontClick("a").underXPath("//DIV[@id='guser']");
		
		// limit the crawling scope
		builder.setMaximumStates(8);
		builder.setMaximumDepth(2);
		
		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();
		assert(true);
	}
}
