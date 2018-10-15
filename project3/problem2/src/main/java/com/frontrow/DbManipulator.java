package com.frontrow;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

class DbManipulator
{
	private static final String SELECT_PROBLEMS = "SELECT * FROM Problem";
	private static final String SELECT_AUTHORS = "SELECT * FROM Author";
	private String jbdc;

	DbManipulator(final String jbdc)
	{
		this.jbdc = jbdc;

		try
		{
			// Load SQL Server JDBC driver and establish connection.
			System.out.print("Connecting to SQL Server ... ");
			try (Connection connection = getDbConnection())
			{
				System.out.println("Connection established.");
			}
		}
		catch (Exception e)
		{
			System.err.println();
			e.printStackTrace(System.err);
		}
	}

	private Connection getDbConnection() throws SQLException
	{
		return DriverManager.getConnection(this.jbdc);
	}


	void addNewProblem(int pid, String pname, int aid) throws SQLException
	{
		try (Connection connection = getDbConnection())
		{
			Statement statement = connection.createStatement();
			statement.execute(String.format("EXEC option_1 @pid = %d, @pname = '%s', @aid = %d", pid, pname, aid));
		}
	}

	void giveRaiseToAuthor(int aid, final PrintStream out) throws SQLException
	{
		try (Connection connection = getDbConnection())
		{
			Statement statement = connection.createStatement();
			statement.execute(String.format("EXEC Give_Author_Raise @aid=%d", aid));
		}
	}

	void displayProblemsAndAuthors(final PrintStream out) throws SQLException
	{
		try (Connection connection = getDbConnection())
		{
			try (Statement statement = connection.createStatement(); ResultSet problems = statement.executeQuery(SELECT_PROBLEMS))
			{
				out.println("Problems:");
				printResultSet(out, problems);
				out.println();
			}

			try (Statement statement = connection.createStatement(); ResultSet authors = statement.executeQuery(SELECT_AUTHORS))
			{
				out.println("Authors:");
				printResultSet(out, authors);
				out.println();
			}
		}
	}

	private void printResultSet(final PrintStream out, final ResultSet resultSet) throws SQLException
	{
		ResultSetMetaData metaData = resultSet.getMetaData();
		int numCols = metaData.getColumnCount();

		{ // column names
			StringJoiner joiner = new StringJoiner(", ");
			for (int i = 1; i <= numCols; i++)
			{
				joiner.add(metaData.getColumnName(i));
			}
			out.println(joiner.toString());
		}

		while (resultSet.next())
		{
			StringJoiner joiner = new StringJoiner(", ");
			for (int i = 1; i <= numCols; i++)
			{
				joiner.add(resultSet.getString(i));
			}
			out.println(joiner.toString());
		}
	}
}
