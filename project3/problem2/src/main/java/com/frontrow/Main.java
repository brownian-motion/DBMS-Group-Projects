package com.frontrow;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.TreeMap;

public class Main
{

	// Map option numbers to their descriptions
	private static final TreeMap<Integer, String> mainMenuOptionDescriptions = new TreeMap<>();
	
	// Object that executes the database queries
	private static DbManipulator db;

	static
	{
		// Define each option number and its description
		mainMenuOptionDescriptions.put(1, "Add a new problem for an author, with auto-computed Max_score");
		mainMenuOptionDescriptions.put(2, "Give a raise to a problem author");
		mainMenuOptionDescriptions.put(3, "Display information about the stored problems and authors");
		mainMenuOptionDescriptions.put(4, "Quit");
	}

	public static void main(String[] args)
	{
		setUpDbConnection(args);

		enterOptionLoop();
	}

	private static void setUpDbConnection(final String[] args)
	{
		for (String arg : args)
		{
			// If the user wants help, display helpful information without executing the rest of the program
			if (arg.equalsIgnoreCase("--help") || arg.equalsIgnoreCase("-h") || arg.equals("/?") /* windows */)
			{
				printHelp();
				System.exit(1);
			}
			// If the user doesn't want help, try to initialize the database manipulator using a command line argument
			else
			{
				db = new DbManipulator(arg);
			}
		}
		// If the database manipulator didn't initialize, use the default database to initialize the database manipulator
		if (db == null)
		{
			try
			{
				System.out.println("Loading JDBC string from config.toml resource...");

				File configFile = new File(Main.class.getResource("config.toml").toURI());
				AuthLoader azureAuthLoader = new TomlFileAzureAuthLoader(configFile);

				String jbdc = azureAuthLoader.getJbdc();

				db = new DbManipulator(jbdc);
			}
			// If there was a problem getting the default config settings, tell the user
			catch (URISyntaxException e)
			{
				throw new RuntimeException("Could not load the config.toml resource in the package " + Main.class.getPackage(), e);
			}
		}
	}

	// The main loop of functionality 
	private static void enterOptionLoop()
	{
		while (true)
		{
			// Display the options the user can choose
			printOptions(mainMenuOptionDescriptions);
			
			// Get the user's choice
			int input = getIntegerInRange(1, 4);

			try
			{
				// Based on the user's option choice, do different things
				switch (input)
				{
					case 1:
						// Get pid, pname, aid from user input
						addNewProblem();
						
						break;
					case 2:
						giveRaiseToAuthor();
						break;
					case 3:
						db.displayProblemsAndAuthors(System.out);
						break;
					case 4:
						return;
					default: // just in case
						System.err.printf("Unknown option selected: %d%nPlease enter an integer value from 1 to 4.%n", input);
				}
			}
			// If an error is encountered, display information about the error
			catch (SQLException e)
			{
				System.err.println("SQL error encountered:");
				e.printStackTrace(System.err);
			}
		}
	}

	private static void giveRaiseToAuthor() throws SQLException
	{
		System.out.println("Giving a raise to an author.");

		System.out.print("Please enter the author ID: ");
		int aid = getNumber(new Scanner(System.in));

		db.giveRaiseToAuthor(aid, System.out);
	}

	// Print out information about the utilisation of this program
	private static void printHelp()
	{
		System.err.println("Manipulates a database of competition problems and their authors.");
		System.err.println("Usage: java " + Main.class.getName() + " [<jbdc>]");
		System.err.println("\tConnects to a Transact-SQL database of problem and author data");
		System.err.println("\tusing the given JBDC and prompts the user to view/manipulate");
		System.err.println("\tproblem/author data.");
		System.err.println("\tIf not specified, connection and auth data are loaded");
		System.err.println("\tfrom the program's config.toml resource in " + Main.class.getPackage());
	}

	private static void addNewProblem() throws SQLException
	{
		int pid;
		int aid;
		String pname;
		Scanner in = new Scanner(System.in);

		System.out.println("Creating a new problem.");

		// Get the pid from the user
		System.out.print("Please enter a number for the problem ID: ");
		pid = getNumber(in);

		// Get the pname from the user
		System.out.print("Please enter a problem name: ");
		pname = in.nextLine().trim();
		
		// Get the author id from the user
		System.out.print("Please enter the author ID: ");
		aid = getNumber(in);
		db.addNewProblem(pid, pname, aid);
	}

	private static int getNumber(final Scanner in)
	{
		int pid;
		while(true)
		{
			try
			{
				pid = in.nextInt();
				in.nextLine(); // clear input to the next line, after the user hits enter
				break;
			}
			catch (InputMismatchException e)
			{
				System.err.println("Not a valid number.");
			}
		}
		return pid;
	}

	private static int getIntegerInRange(final int min, final int max)
	{
		Scanner in = new Scanner(System.in);
		while (true)
		{
			System.out.printf("Please enter a number from %d to %d%n", min, max);
			try
			{
				return in.nextInt();
			}
			catch (InputMismatchException e)
			{
				System.err.println("Not a valid number.");
			}
		}
	}
	
	// Display the list of possible options that this program can perform
	private static <K, V> void printOptions(TreeMap<K, V> options)
	{
		System.out.println("Please select an option:");
		options.forEach((option, description) -> System.out.printf("\t%s:\t%s%n", option, description));
	}
}
