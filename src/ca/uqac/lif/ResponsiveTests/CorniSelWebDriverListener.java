package ca.uqac.lif.ResponsiveTests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ca.uqac.lif.CorniSel.CorniSelWebDriver;
import ca.uqac.lif.CorniSel.EvaluationListener;
import ca.uqac.lif.cornipickle.Interpreter;

class CorniSelWebDriverListener implements EvaluationListener {
	private String m_filename;
	
	public CorniSelWebDriverListener(String filename) {
		m_filename = filename;
		
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
	}
	
	public void evaluationEvent(CorniSelWebDriver driver, Interpreter interpreter) {
		String outputFilename = System.getProperty("user.dir") + "/out/out.txt";
		
		try {
			driver.outputEvaluation(outputFilename);
		} catch (IOException e) {
			System.out.println("Couldn't output evaluation due to IO exception.");
		}
	}
}
