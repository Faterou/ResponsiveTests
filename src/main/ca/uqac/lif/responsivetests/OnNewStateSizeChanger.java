package ca.uqac.lif.responsivetests;

import org.openqa.selenium.Dimension;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnRevisitStatePlugin;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.StateVertex;

import ca.uqac.lif.cornisel.CorniSelWebDriver;

public class OnNewStateSizeChanger implements Plugin, OnNewStatePlugin, OnRevisitStatePlugin {
	
	private Dimension m_initialDimension;
	private Dimension m_minimumDimension;
	private Dimension m_maximumDimension;
	private int m_interval;

	public OnNewStateSizeChanger(Dimension minimumDimension, Dimension maximumDimension, 
			int interval) {
		m_minimumDimension = minimumDimension;
		m_maximumDimension = maximumDimension;
		m_initialDimension = null;
		m_interval = interval;
	}
	
	public void onNewState(CrawlerContext context, StateVertex newState) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CorniSelWebDriver driver = CorniSelWebDriverBrowserBuilder.corniSelDriver;
		
		if(m_initialDimension == null) {
			m_initialDimension = driver.manage().window().getSize();
			driver.manage().window().setSize(m_maximumDimension);
		}
		
		driver.evaluateAll(null);
		
		//Go to lower bound with interval
		while(driver.manage().window().getSize().width - m_interval >= m_minimumDimension.width) {
			driver.manage().window().setSize(new Dimension(driver.manage().window().getSize().width - m_interval,
				driver.manage().window().getSize().height));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.evaluateAll(null);
		}
		
		driver.manage().window().setSize(m_maximumDimension);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onRevisitState(CrawlerContext context, StateVertex currentState) {
		if(currentState.getId() == 0) {
			CorniSelWebDriver driver = CorniSelWebDriverBrowserBuilder.corniSelDriver;
			driver.resetHistory();
		}
	}
}
