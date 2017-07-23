package ca.uqac.lif.responsivetests;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.openqa.selenium.Dimension;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;

import ca.uqac.lif.cornipickle.util.AnsiPrinter;

public class Main{
	
	/**
	 * Return codes
	 */
	public static final int ERR_OK = 0;
	public static final int ERR_PARSE = 2;
	public static final int ERR_IO = 3;
	public static final int ERR_ARGUMENTS = 4;
	public static final int ERR_RUNTIME = 6;
	public static final int ERR_GRAMMAR = 7;
	public static final int ERR_INPUT = 9;
	
	/**
	 * Default source url
	 */
	protected static String s_defaultSourceUrl = "https://www.xkcd.com";
	
	/**
	 * Default browser for driver
	 */
	protected static String s_defaultBrowser = "chrome";
	
	/**
	 * Default output filename
	 */
	protected static String s_defaultOutputFilename = System.getProperty("user.dir") + "/out/out.txt";
	
	/**
	 * Default output screenshot directory
	 */
	protected static String s_defaultScreenshotsDirectory = System.getProperty("user.dir") + "/out/screenshots/";
	
	/**
	 * Default lower bound
	 */
	protected static int s_defaultLowerBound = 320;
	
	/**
	 * Default upper bound
	 */
	protected static int s_defaultUpperBound = 1600;
	
	/**
	 * Default interval
	 */
	protected static int s_defaultInterval = 10;
	
	/**
	 * Build string to identify versions
	 */
	protected static final String VERSION_STRING = "0.1";
	protected static final String BUILD_STRING = "20170608";

	public static void main(String[] args) {
		
		String sourceUrl = s_defaultSourceUrl;
		String browser = s_defaultBrowser;
		String outputFilename = s_defaultOutputFilename;
		String screenshotsDirectory = s_defaultScreenshotsDirectory;
		int upperBound = s_defaultUpperBound;
		int lowerBound = s_defaultLowerBound;
		int interval = s_defaultInterval;
		
		final AnsiPrinter stderr = new AnsiPrinter(System.err);
		final AnsiPrinter stdout = new AnsiPrinter(System.out);
		stdout.setForegroundColor(AnsiPrinter.Color.BLACK);
		stderr.setForegroundColor(AnsiPrinter.Color.BLACK);

		// Properly close print streams when closing the program
		// https://www.securecoding.cert.org/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				stderr.close();
				stdout.close();
			}
		}));

		// Parse command line arguments
		Options options = setupOptions();
		CommandLine c_line = setupCommandLine(args, options, stderr);
		assert c_line != null;
		
		if (c_line.hasOption("v"))
		{
			stderr.println("(C) 2015-2017 Laboratoire d'informatique formelle");
			stderr.println("This program comes with ABSOLUTELY NO WARRANTY.");
			stderr.println("This is a free software, and you are welcome to redistribute it");
			stderr.println("under certain conditions. See the file LICENSE for details.\n");
			System.exit(ERR_OK);
		}
		if (c_line.hasOption("h"))
		{
			showUsage(options);
			System.exit(ERR_OK);
		}
		if (c_line.hasOption("s"))
		{
			sourceUrl = c_line.getOptionValue("s");
		}
		if (c_line.hasOption("b"))
		{
			browser = c_line.getOptionValue("b");
		}
		if (c_line.hasOption("o"))
		{
			outputFilename = c_line.getOptionValue("o");
		}
		if(c_line.hasOption("f"))
		{
			screenshotsDirectory = c_line.getOptionValue("f");
		}
		if(c_line.hasOption("l"))
		{
			String cllowerBound = c_line.getOptionValue("l");
			try {
				lowerBound = Integer.parseInt(cllowerBound);
			} catch (NumberFormatException e) {
				stderr.println("Lower bound parameter is not an integer");
				System.exit(ERR_ARGUMENTS);
			}
		}
		if(c_line.hasOption("u"))
		{
			String clupperBound = c_line.getOptionValue("u");
			try {
				upperBound = Integer.parseInt(clupperBound);
			} catch (NumberFormatException e) {
				stderr.println("Upper bound parameter is not an integer");
				System.exit(ERR_ARGUMENTS);
			}
		}
		if(c_line.hasOption("i"))
		{
			String clinterval = c_line.getOptionValue("i");
			try {
				interval = Integer.parseInt(clinterval);
			} catch (NumberFormatException e) {
				stderr.println("Interval parameter is not an integer");
				System.exit(ERR_ARGUMENTS);
			}
		}
		
		List<String> remaining_args = c_line.getArgList();
		String properties = "";
		for (String filename : remaining_args)
		{
			stdout.setForegroundColor(AnsiPrinter.Color.BROWN);
			stdout.println("Reading properties in " + filename);
			try {
				properties = properties.concat(readFile(new File(filename)) + "\n");
			} catch (IOException e) {
				stderr.println("ERROR: Couldn't open " + filename);
				System.exit(ERR_ARGUMENTS);
			}
		}
		
		CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(sourceUrl);
		builder.crawlRules().insertRandomDataInInputForms(false);
		
		CorniSelWebDriverBrowserBuilder corniSelBrowserBuilder = new CorniSelWebDriverBrowserBuilder(properties);
		corniSelBrowserBuilder.addEvaluationListener(new CorniSelWebDriverListener(outputFilename, screenshotsDirectory));
		
		builder.addPlugin(new OnNewStateSizeChanger(new Dimension(lowerBound,1000), new Dimension(upperBound,1000), interval));
		
		if(browser.equals("chrome"))
		{
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME, 1, corniSelBrowserBuilder));
		}
		else if(browser.equals("firefox"))
		{
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1, corniSelBrowserBuilder));
		}
		else if(browser.equals("phantomjs"))
		{
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.PHANTOMJS, 1, corniSelBrowserBuilder));
		}
		else if(browser.equals("internetexplorer"))
		{
			builder.setBrowserConfig(new BrowserConfiguration(BrowserType.INTERNET_EXPLORER, 1, corniSelBrowserBuilder));
		}
		else
		{
			// oops, something went wrong
			stderr.println("ERROR: " + "The specified browser isn't supported" + "\n");
			System.exit(ERR_ARGUMENTS);
		}
		
		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();
		assert(true);
	}

	/**
	 * Sets up the command line parser
	 * @param args The command line arguments passed to the class' {@link main}
	 * method
	 * @param options The command line options to be used by the parser
	 * @return The object that parsed the command line parameters
	 */
	private static CommandLine setupCommandLine(String[] args, Options options, PrintStream stderr)
	{
		CommandLineParser parser = new DefaultParser();
		CommandLine c_line = null;
		try
		{
			// parse the command line arguments
			c_line = parser.parse(options, args);
		}
		catch (org.apache.commons.cli.ParseException exp)
		{
			// oops, something went wrong
			stderr.println("ERROR: " + exp.getMessage() + "\n");
			//HelpFormatter hf = new HelpFormatter();
			//hf.printHelp(t_gen.getAppName() + " [options]", options);
			System.exit(ERR_ARGUMENTS);
		}
		return c_line;
	}

	private static Options setupOptions() {
		Options options = new Options();
		Option opt;
		opt = Option.builder("h")
				.longOpt("help")
				.desc("Displays command line usage")
				.build();
		options.addOption(opt);
		opt = Option.builder("s")
				.longOpt("source")
				.argName("url")
				.hasArg()
				.desc("Sets url to be the source to test (default: " + s_defaultSourceUrl + ")")
				.build();
		options.addOption(opt);
		opt = Option.builder("b")
				.longOpt("browser")
				.argName("browser")
				.hasArg()
				.desc("Tests with browser (firefox, chrome, phantomjs, internetexplorer) (default: " + s_defaultBrowser +")")
				.build();
		options.addOption(opt);
		opt = Option.builder("v")
				.longOpt("version")
				.desc("Shows the application's version")
				.build();
		options.addOption(opt);
		opt = Option.builder("o")
				.longOpt("output")
				.argName("filename")
				.hasArg()
				.desc("Sets output file's name (default: " + s_defaultOutputFilename + ")")
				.build();
		options.addOption(opt);
		opt = Option.builder("f")
				.longOpt("screenshots")
				.argName("directory")
				.hasArg()
				.desc("Sets the directory for screenshots (default: " + s_defaultScreenshotsDirectory + ")")
				.build();
		options.addOption(opt);
		opt = Option.builder("l")
				.longOpt("lower")
				.argName("lowerbound")
				.hasArg()
				.desc("Sets the minimum width of the browser in pixels when decrementing (default: 320)")
				.build();
		options.addOption(opt);
		opt = Option.builder("u")
				.longOpt("upper")
				.argName("upperbound")
				.hasArg()
				.desc("Sets the maximum width of the browser in pixels when incrementing (default: 1920)")
				.build();
		options.addOption(opt);
		opt = Option.builder("i")
				.longOpt("interval")
				.argName("interval")
				.hasArg()
				.desc("Sets the interval for the decrement and increment on the width of the browser (default: 10)")
				.build();
		options.addOption(opt);
		return options;
	}
	
	/**
	 * Show the usage
	 * @param options The options created for the command line parser
	 */
	private static void showUsage(Options options)
	{
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("java -jar ResponsiveTests.jar [options] [file1 [file2 ...]]", options);
	}
	
	/**
	 * Reads a file and puts its contents in a string
	 * @param f The file to read
	 * @return The file's contents, and empty string if the file
	 * does not exist
	 * @throws IOException If the reading fails
	 */
	private static String readFile(File f) throws IOException
	{
		Scanner scanner = null;
		StringBuilder out = new StringBuilder();
		try
		{
			scanner = new java.util.Scanner(f);
			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine();
				out.append(line).append(System.getProperty("line.separator"));
			}
		}
		catch (java.io.IOException e)
		{
			throw e;
		}
		finally
		{
			if (scanner != null)
				scanner.close();
		}
		return out.toString();
	}
}
