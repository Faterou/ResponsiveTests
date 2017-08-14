package ca.uqac.lif.responsivetests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnRevisitStatePlugin;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.StateVertex;

import ca.uqac.lif.cornipickle.Interpreter;
import ca.uqac.lif.cornipickle.Verdict;
import ca.uqac.lif.cornipickle.Interpreter.StatementMetadata;
import ca.uqac.lif.cornisel.CorniSelWebDriver;
import ca.uqac.lif.cornisel.EvaluationListener;

public class OnNewStateSizeChanger implements Plugin, OnNewStatePlugin, OnRevisitStatePlugin, EvaluationListener {
	
	private Dimension m_initialDimension;
	private Dimension m_minimumDimension;
	private Dimension m_maximumDimension;
	private int m_interval;
	
	private String m_filename;
	private String m_screenshotsDirectory;
	private int m_counter = 0;
	private double m_begin;
	private double m_end;
	private double m_lastExecTime;

	public OnNewStateSizeChanger(Dimension minimumDimension, Dimension maximumDimension, 
			int interval, String filename, String screenshotsDirectoryPath) {
		m_minimumDimension = minimumDimension;
		m_maximumDimension = maximumDimension;
		m_initialDimension = null;
		m_interval = interval;
		
		m_filename = filename;
		m_screenshotsDirectory = screenshotsDirectoryPath;
		m_begin = 0;
		m_end = 0;
		m_lastExecTime = 0;
		
		try {
			File file = new File(filename);
			File parent = file.getParentFile();
			parent.mkdirs();
			file.createNewFile();
			FileWriter fw = new FileWriter(file, false);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			File screenshotsDirectory = new File(screenshotsDirectoryPath);
			FileUtils.deleteDirectory(screenshotsDirectory);
			screenshotsDirectory.mkdirs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onNewState(CrawlerContext context, StateVertex newState) {
		CorniSelWebDriver driver = CorniSelWebDriverBrowserBuilder.corniSelDriver;
		
		if(m_initialDimension == null) {
			m_initialDimension = driver.manage().window().getSize();
			driver.manage().window().setSize(m_maximumDimension);
		}
		m_begin = (double)System.currentTimeMillis();
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
			m_begin = (double)System.currentTimeMillis();
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

	@Override
	public void evaluationEvent(CorniSelWebDriver driver, Interpreter interpreter) {
		m_end = (double)System.currentTimeMillis();
		m_lastExecTime = m_end - m_begin;
		
		Map<StatementMetadata,Verdict> verdicts = interpreter.getVerdicts();
		String currentURL = driver.getCurrentUrl();
		String width = String.valueOf(driver.manage().window().getSize().getWidth());
		String height = String.valueOf(driver.manage().window().getSize().getHeight());
		
		FileWriter fw;
		try {
			fw = new FileWriter(new File(m_filename), true);
		
			fw.write("-----------------------------------------------\n");
			fw.write("Evaluation\n");
			fw.write("URL: " + currentURL + "\n");
			fw.write("Width: " + width + " px\n");
			fw.write("Height: " + height + " px\n");
			fw.write("Time taken: " + String.valueOf(m_lastExecTime / 1000.0) + " seconds \n");
			fw.write("Overall result: ");
			
			Verdict.Value overallVerdict = Verdict.Value.TRUE;
			for(Entry<StatementMetadata, Verdict> entry : verdicts.entrySet())
			{
				if(entry.getValue().is(Verdict.Value.INCONCLUSIVE))
				{
					overallVerdict = Verdict.Value.INCONCLUSIVE;
				}
				else if(entry.getValue().is(Verdict.Value.FALSE))
				{
					overallVerdict = Verdict.Value.FALSE;
					break;
				}
			}
			fw.write(overallVerdict.toString() + "\n");
			
			File screenshotFile = driver.getScreenshotAs(OutputType.FILE);
			File screenshotFileCopy = new File(m_screenshotsDirectory + "screenshot" + m_counter++ + ".png");
			Files.move(screenshotFile.toPath(), screenshotFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
			fw.write("Screenshot path: " + screenshotFileCopy.toPath().toString() + "\n\n");
			
			for(Entry<StatementMetadata, Verdict> entry : verdicts.entrySet())
			{
				fw.write("Statement:\n" + entry.getKey().toString());
				fw.write("Verdict: " + entry.getValue().getValue().toString() + "\n");
				if(entry.getValue().getValue() == Verdict.Value.FALSE)
				{
					fw.write("Witness: " + entry.getValue().getWitnessFalse().toString() + "\n\n");
				}
				else
				{
					fw.write("\n");
				}
			}
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
