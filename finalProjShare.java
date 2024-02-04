package cs286;

/*	@Author Jacob Elbirt
 *  Code for Final Project for CS-286 -- Database Design and Applications
 *  Methodology focusing on abstraction and flexibility/scalability
 *  Implementing Object[][] arrays and PreparedStatement() as key components
 *  12/6/2023 - Operational to connect to DB and URL, but incompatible with current Date format for dataset
 *  Likely fully operational for other date formats or with date format adjustments.
 */

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class finalProjShare {
	private boolean debug = false;		// DEBUG VARIABLE - if true, debug statements/checks will be run
	private String FILE_OUT_NAME = "YOUR FILE HERE";
	private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	// Data structure containing Query Strings
	private String[][] QUERIES = {
			// Annual:
			{
				"INSERT INTO avg_price_annual ("
				+ "summaryDateYear, avgOZPriceSum, avgOZPriceAvg, avgOZPriceCnt) "
				+ "SELECT YEAR(dateSold) as yr,"
				+ "SUM(avgOZPrice), AVG(avgOZPrice), COUNT(avgOZPrice)"
				+ " FROM avg_price_info "
				+ "GROUP BY yr "
				+ "ORDER BY yr",
				"avg_price_annual"
			},
			{
				"INSERT INTO plant_info_annual ("
				+ "summaryDateYear, agrEmployeeCount, plantTrackedCount, plantBatchCount, immatureCount, plantVegCount, plantFlowerCount,"
				+ "harvestActiveCount, harvestedCount, plantDestroyedCount, activeProductCount) "
				+ "select YEAR(summaryDate) as yr,"
				+ "SUM(agrEmployeeCount),SUM(plantTrackedCount),SUM(plantBatchCount), SUM(immatureCount), SUM(plantVegCount),"
				+ "SUM(plantFlowerCount),SUM(harvestActiveCount),SUM(harvestedCount),SUM(plantDestroyedCount),SUM(activeProductCount)"
				+ " FROM plant_info "
				+ "GROUP BY yr "
				+ "ORDER BY yr",
				"plant_info_annual"
			},
			//Monthly:
			{
				"INSERT INTO avg_price_monthly ("
				+ "summaryDateYear, summaryDateMonth, avgOZPriceSum, avgOZPriceAvg, avgOZPriceCnt) "
				+ "SELECT YEAR(dateSold) as yr, MONTH(dateSold) as mo,"
				+ "SUM(avgOZPrice), AVG(avgOZPrice), COUNT(avgOZPrice)"
				+ " FROM avg_price_info "
				+ "GROUP BY yr, mo "
				+ "ORDER BY yr, mo",
				"avg_price_monthly"
			},
			{
				"INSERT INTO plant_info_monthly ("
				+ "summaryDateYear, summaryDateMonth, agrEmployeeCount, plantTrackedCount, plantBatchCount, immatureCount, plantVegCount, plantFlowerCount,"
				+ "harvestActiveCount, harvestedCount, plantDestroyedCount, activeProductCount) "
				+ "select YEAR(summaryDate) as yr, MONTH(summaryDate) as mo,"
				+ "SUM(agrEmployeeCount),SUM(plantTrackedCount),SUM(plantBatchCount), SUM(immatureCount), SUM(plantVegCount),"
				+ "SUM(plantFlowerCount),SUM(harvestActiveCount),SUM(harvestedCount),SUM(plantDestroyedCount),SUM(activeProductCount)"
				+ " FROM plant_info "
				+ "GROUP BY yr, mo "
				+ "ORDER BY yr, mo",
				"plant_info_monthly"
			},
			//Weekly:
			{
				"INSERT INTO avg_price_weekly ("
				+ "summaryDateYear, summaryDateWeek, avgOZPriceSum, avgOZPriceAvg, avgOZPriceCnt) "
				+ "SELECT YEAR(dateSold) as yr, WEEK(dateSold) as wk,"
				+ "SUM(avgOZPrice), AVG(avgOZPrice), COUNT(avgOZPrice)"
				+ " FROM avg_price_info "
				+ "GROUP BY yr, wk "
				+ "ORDER BY yr, wk",
				"avg_price_weekly"
			},
			{
				"INSERT INTO plant_info_weekly ("
				+ "summaryDateYear, summaryDateWeek, agrEmployeeCount, plantTrackedCount, plantBatchCount, immatureCount, plantVegCount, plantFlowerCount,"
				+ "harvestActiveCount, harvestedCount, plantDestroyedCount, activeProductCount) "
				+ "select YEAR(summaryDate) as yr, WEEK(summaryDate) as wk,"
				+ "SUM(agrEmployeeCount),SUM(plantTrackedCount),SUM(plantBatchCount), SUM(immatureCount), SUM(plantVegCount),"
				+ "SUM(plantFlowerCount),SUM(harvestActiveCount),SUM(harvestedCount),SUM(plantDestroyedCount),SUM(activeProductCount)"
				+ " FROM plant_info "
				+ "GROUP BY yr, wk "
				+ "ORDER BY yr, wk",
				"plant_info_weekly"
			}
	};
	
	/* Data Structure for containing all necessary info for reading in data inputs
	 * 
	 * Must have: 
	 0 STRING: * url of file 
	 1 STRING: * DB Table Name
	 2 BOOLEAN: * header line? Y/N
	 3 STRING: * what is the delimiter?
	 4 STRING: * SQL insert statement
	 5 Arr[int]: * list of column indexes to store in the order of the SQL insert statement
	 6 Arr[String]: * corresponding data types for columns
	 */
	private Object[][] INPUT = {
		{
			"https://masscannabiscontrol.com/resource/meau-plav.csv",	// example data used for project
			"plant_info",
			true,
			",",
			"INSERT INTO plant_info (summaryDate,agrEmployeeCount,plantTrackedCount,plantBatchCount,immatureCount,plantVegCount,plantFlowerCount,harvestActiveCount,harvestedCount,plantDestroyedCount,activeProductCount) values (?,?,?,?,?,?,?,?,?,?,?)",
			new int[] {9,4,1,2,3,0,8,5,10,7,6},
			new String[] {"Date","int","int","int","int","int","int","int","int","int","int"}
		},
		{
			"https://masscannabiscontrol.com/resource/rqtv-uenj.csv",
			"avg_price_info",
			true,
			",",
			"INSERT INTO avg_price_info (dateSold,avgOZPrice) values (?,?)",
			new int[] {0,1},
			new String[] {"Date", "double"}
		}
	};
	
	/* Data structure containing all data needed for accessing SQL database and creating Excel Spreadsheets
	 * 
	 * Contains:
	0 STRING: * PROVIDE: Spreadsheet name
	1 STRING: * SQL Query - Select statement to get needed data from the DB
	2 BOOLEAN: * Boolean flag - whether I will output/include header row or not
	3 String[]: * Array of column names from Query --> will be output in the DESIRED order for spreadsheet
	4 String[]: * Corresponding Array containing a list of column names - Text titles for header if boolean is TRUE
	5 String[]: * Array of column TYPES - for cell types of output columns (i.e. date column)
	6 BOOLEAN[]: * Array of boolean flags representing if the sum/avg/counts of columns should be included
	 */
	
	private Object[][] SHEETS = {
			{	//Plant info sheets:
				"Plant Information",
				"SELECT * FROM plant_info ORDER BY summaryDate;",
				true,
				new String[] {"ID","summaryDate", "agrEmployeeCount", "plantTrackedCount", "plantBatchCount", 
				"immatureCount", "plantVegCount", "plantFlowerCount", "harvestActiveCount", "harvestedCount", 
				"plantDestroyedCount", "activeProductCount"},
				new String[] {},
				new String[] {"int","date","int","int","int","int","int","int","int","int","int","int"},
				new Boolean[] {true,true,true}
			},
			{
				"Annual Plant Information",
				"SELECT * FROM plant_info_annual ORDER BY summaryDateYear;",
				true,
				new String[] {"ID","summaryDateYear", "agrEmployeeCount", "plantTrackedCount", "plantBatchCount", 
				"immatureCount", "plantVegCount", "plantFlowerCount", "harvestActiveCount", "harvestedCount", 
				"plantDestroyedCount", "activeProductCount"},
				new String[] {},
				new String[] {"int","int","int","int","int","int","int","int","int","int","int","int"},
				new Boolean[] {true,true,true}
			},
			{
				"Monthly Plant Information",
				"SELECT * FROM plant_info_monthly ORDER BY summaryDateYear, summaryDateMonth;",
				true,
				new String[] {"ID","summaryDateYear", "summaryDateMonth", "agrEmployeeCount", "plantTrackedCount", "plantBatchCount", 
				"immatureCount", "plantVegCount", "plantFlowerCount", "harvestActiveCount", "harvestedCount", 
				"plantDestroyedCount", "activeProductCount"},
				new String[] {},
				new String[] {"int","int","int","int","int","int","int","int","int","int","int","int","int"},
				new Boolean[] {true,true,true}
			},
			{
				"Weekly Plant Information",
				"SELECT * FROM plant_info_weekly ORDER BY summaryDateYear, summaryDateWeek;",
				true,
				new String[] {"ID","summaryDateYear", "summaryDateWeek", "agrEmployeeCount", "plantTrackedCount", "plantBatchCount", 
				"immatureCount", "plantVegCount", "plantFlowerCount", "harvestActiveCount", "harvestedCount", 
				"plantDestroyedCount", "activeProductCount"},
				new String[] {},
				new String[] {"int","int","int","int","int","int","int","int","int","int","int","int","int"},
				new Boolean[] {true,true,true}
			},
			{	// Pricing info sheets:
				"Average Pricing",
				"SELECT * FROM avg_price_info ORDER BY dateSold",
				true,
				new String[] {"ID", "dateSold", "avgOZPrice"},
				new String[] {},
				new String[] {"int","date","double"},
				new Boolean[] {true,true,true}				
			},
			{	
				"Annual Average Pricing",
				"SELECT * FROM avg_price_annual ORDER BY summaryDateYear",
				true,
				new String[] {"ID", "summaryDateYear", "avgOZPriceSum", "avgOZPriceAvg", "avgOZPriceCnt"},
				new String[] {},
				new String[] {"int","int","double","double","int"},
				new Boolean[] {true,true,true}				
			},
			{	
				"Monthly Average Pricing",
				"SELECT * FROM avg_price_monthly ORDER BY summaryDateYear, summaryDateMonth",
				true,
				new String[] {"ID", "summaryDateYear","summaryDateMonth","avgOZPriceSum", "avgOZPriceAvg", "avgOZPriceCnt"},
				new String[] {},
				new String[] {"int","int","int","double","double","int"},
				new Boolean[] {true,true,true}				
			},
			{	
				"Weekly Average Pricing",
				"SELECT * FROM avg_price_weekly ORDER BY summaryDateYear, summaryDateWeek",
				true,
				new String[] {"ID", "summaryDateYear","summaryDateWeek","avgOZPriceSum", "avgOZPriceAvg", "avgOZPriceCnt"},
				new String[] {},
				new String[] {"int","int","int","double","double","int"},
				new Boolean[] {true,true,true}				
			},
			{	// Sheet of new/manipulated data tables:
				"Mixed Table Query (ratioDH,Price)",
				"select pim.summaryDateYear as summaryDateYear, "
				+ "pim.summaryDateMonth as summaryDateMonth, "
				+ "pim.plantDestroyedCount as plantDestroyedCount, "
				+ "pim.harvestedCount as harvestedCount, "
				+ "pim.plantDestroyedCount / pim.harvestedCount as ratioDH, "
				+ "apm.avgOZPriceAvg as avgOZPriceAvg "
				+ "FROM plant_info_monthly pim, avg_price_monthly apm "
				+ "WHERE apm.summaryDateYear = pim.summaryDateYear AND "
				+ "apm.summaryDateMonth = pim.summaryDateMonth "
				+ "ORDER BY pim.summaryDateYear, pim.summaryDateMonth;",
				true,
				new String[] {"summaryDateMonth", "plantDestroyedCount", "harvestedCount", "ratioDH", "avgOZPriceAvg"},
				new String[] {},
				new String[] {"int","int","int","double","double","double"},
				new Boolean[] {true,true,true}
			}
	};
	
	public static void main(String[] args) {
		new finalProjShare();
	}
	
	public finalProjShare() {
		// Establish database connection
		Connection DBCONN = null;
		try {
			DBCONN = getConnection();
		} catch (SQLException e) {
			System.out.println("Could not connect to the Database");
			e.printStackTrace();
			System.exit(0);
		}

		try {
			// Load the data
			for (int i=0; i < INPUT.length; i++) {
				URLConnection CONN = getURLConnect((String) INPUT[i][0]);	// access correct url from INPUT data structure
				processInput(DBCONN, CONN, INPUT[i]);
			}
			// Data Manipulation:
			calculate(DBCONN);
			// Generate outputs:
			output_Excel(DBCONN);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	// Method to connect to SQL database --> returns connection
	public Connection getConnection() throws SQLException {
		System.out.print("Establishing Database Connection...\t");
        Connection conn = null;
        
	    // DB parameters use your schema and password
    	String schema	 = "agr-data";	// YOUR SCHEMA - this was my example name
    	// Add schema name to url after "/" to specify the schema to be accessed
	    String url       = "YOUR_SQL_CONN_URL/" + schema;
	    String user      = "YOUR USERNAME";
	    String password  = "YOUR_PASSWORD";
		
	    // create a connection to the database
	    conn = DriverManager.getConnection(url, user, password);
		System.out.println("Connected.");
	    // return the DB connection
	    return conn; 
	}
	
	// Method to create/return URL connection (to access data)
	public URLConnection getURLConnect(String sourceURL) throws IOException {
			System.out.print("Establishing connection to " + sourceURL + "...\t");
		    URL myURL = URI.toURL(sourceURL);
		    URLConnection myURLConnection = myURL.openConnection();
		    myURLConnection.connect();
		    System.out.println("Connected");
		    return myURLConnection;
	}
	
	public void processInput(Connection DBCONN, URLConnection CONN, Object[] input) throws IOException, SQLException, ParseException {
		System.out.print("Processing input...\t");
		// Establish input reader
		BufferedReader br = new BufferedReader(new InputStreamReader(CONN.getInputStream()));
		
		// Clear the DB Table
		Statement statement = DBCONN.createStatement(); 			 
		statement.executeUpdate("truncate " + (String) input[1]);
		
		// Prepare SQL insert
		PreparedStatement ps = DBCONN.prepareStatement((String) input[4]);
		
		// Process the input
		String inputLine = "";
		int lineCount = 0;
		while ((inputLine = br.readLine()) != null) {
			// Check for a header row:
			if ((lineCount == 0 && (Boolean) input[2] == false) || lineCount > 0) {
				// Clean data (EoL chars, etc)
				inputLine = inputLine.replaceAll("\n", "");
				inputLine = inputLine.replaceAll("\r", "");
				
				// Create array of data based on delimiter
				String[] values = inputLine.split((String) input[3]);
				
				// Assign the data based on the indexes
				int[] indexes = (int[]) input[5];
				String[] types = (String[]) input[6];
				for (int i=0; i<indexes.length; i++) {
					if (types[i].toUpperCase().equals("DATE")) {
						Date d = dateFormat.parse(values[indexes[i]]);
						java.sql.Date dd = new java.sql.Date(d.getTime());
						ps.setDate(i+1, dd);
					} else if (types[i].toUpperCase().equals("INT")) {
						ps.setInt(i+1, Integer.valueOf(values[indexes[i]]));
					} else if (types[i].toUpperCase().equals("DOUBLE")) {
						ps.setDouble(i+1, Double.valueOf(values[indexes[i]]));
					} else {
						throw new SQLException ("Unexpected data type " + types[i]);
					}
				} // end of for(i) loop (indexes)
				
				// Execute the statement
				ps.execute();
			}
			// Increment lineCount 
			lineCount++;
		}	// end while()
		// Close the input reader
        br.close();
		// Close the preparedStatement
		ps.close();
		System.out.println("Complete");
	}
	
	public void calculate(Connection DBCONN) throws SQLException {
		System.out.print("Performing calculations...\t");
		Statement stmt = DBCONN.createStatement();
		for (int i=0; i<QUERIES.length; i++) {
			//Truncate the table:
			stmt.executeUpdate("TRUNCATE " + QUERIES[i][1]);
			// Execute Query:
			stmt.executeUpdate(QUERIES[i][0]);
		}
		stmt.close();
		System.out.println("Complete");
	}
	
	// Method to create Excel workbook/sheets and write the desired data into Excel
	public void output_Excel(Connection DBCONN) throws SQLException, IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet;	// introduces sheet variable without assigning value
		
		Statement stmt = DBCONN.createStatement();
		for (int i=0; i<SHEETS.length; i++) {	// for every sheet in the SHEETS data structure
			int rowCount = 0;
			String[] types = (String[]) SHEETS[i][5];	// Contains String names of the data types (int, date etc)
			String[] colNames = (String[]) SHEETS[i][3];// Contains Strings of column names
			sheet = workbook.createSheet((String) SHEETS[i][0]);
			// Handles header:
			if ((boolean) SHEETS[i][2]) {
				String[] names = null;
				if (((String[]) SHEETS[i][4]).length == 0) {
					names = (String[]) SHEETS[i][3];
				} else {
					names = (String[]) SHEETS[i][4];
				}
				// Create Cell variable in sheet, to then fill with value
				Row r = sheet.createRow(rowCount);	
				for(int j=0; j < names.length; j++) {
					Cell c = r.createCell(j);
					c.setCellValue(names[j]);
				}	
				rowCount++;
			}
			// Handles non-header data:
			ResultSet rs = stmt.executeQuery((String) SHEETS[i][1]);
			while(rs.next()) {
				Row r = sheet.createRow(rowCount);
				for(int j=0; j < colNames.length; j++) {
					// Create Cell variable in sheet, to then fill with value
					Cell c = r.createCell(j);
					// Handles data input properly depending on its data-type
					if(types[j].toUpperCase().equals("INT")) {
						int id = rs.getInt(colNames[j]);
						c.setCellValue(id);
					} else if (types[j].toUpperCase().equals("STRING") || types[j].toUpperCase().equals("DATE")) {
						String data = rs.getString(colNames[j]);
						c.setCellValue(data);
					} else if (types[j].toUpperCase().equals("DOUBLE")) {
						double decimal = rs.getDouble(colNames[j]);
						c.setCellValue(decimal);
					}
				}
				// rowCount incremented each loop to ensure the next row is made on the correct row
				rowCount++;
			} // end while()
			
			// Close processes in Memory once finished using them:
			rs.close();
		}
		stmt.close();
		FileOutputStream outputStream = new FileOutputStream(FILE_OUT_NAME);
        workbook.write(outputStream);	// these are what actually write the workbook
        workbook.close();
		System.out.println("Writing to Excel Complete.");
		System.out.println("Access the Excel output file at: " + FILE_OUT_NAME);
	}
	/* TODO : Buttoning up:
	*  1. Ensure DBConnections and URLConnections are closed at appropriate times
	*  2. Comment code thoroughly
	*  3. Running Comments - create boolean variable "debug"
	*  			- If debug == true, various System.out.println() messages will print throughout the code to help identify issues
	*  4. Remove "yellow-line" variables --> waste of memory
	*  
	*/ 
	
	
}
