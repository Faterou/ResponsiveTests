package ca.uqac.lif.responsivetests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import ca.uqac.lif.cornipickle.Interpreter;
import ca.uqac.lif.cornipickle.Verdict;
import ca.uqac.lif.cornipickle.Interpreter.StatementMetadata;
import ca.uqac.lif.cornisel.CorniSelWebDriver;
import ca.uqac.lif.cornisel.EvaluationListener;

class CorniSelWebDriverListener implements EvaluationListener {
	private String m_filename;
	private String m_screenshotsDirectory;
	private int counter = 0;
	
	public CorniSelWebDriverListener(String filename, String screenshotsDirectoryPath) {
		m_filename = filename;
		m_screenshotsDirectory = screenshotsDirectoryPath;
		
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
	
	public void evaluationEvent(CorniSelWebDriver driver, Interpreter interpreter) {
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
			File screenshotFileCopy = new File(m_screenshotsDirectory + "screenshot" + counter++ + ".png");
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
