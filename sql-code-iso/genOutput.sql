-- SQL queries executed to extract data from SQL database to be written to an Excel or other file type as output
-- Data types are stored in an Object[][] to be formatted and handled by Java
-- Outputs each table as a separate sheet as well as an additional sheet to contain manipulated/queried data

-- Table contents sheets:

SELECT * FROM plant_info ORDER BY summaryDate;
SELECT * FROM plant_info_annual ORDER BY summaryDateYear;
SELECT * FROM plant_info_monthly ORDER BY summaryDateYear, summaryDateMonth;
SELECT * FROM plant_info_weekly ORDER BY summaryDateYear, summaryDateWeek;
SELECT * FROM avg_price_info ORDER BY dateSold;
SELECT * FROM avg_price_annual ORDER BY summaryDateYear;
SELECT * FROM avg_price_monthly ORDER BY summaryDateYear, summaryDateMonth;
SELECT * FROM avg_price_weekly ORDER BY summaryDateYear, summaryDateWeek;

-- Mixed-table query:
SELECT pim.summaryDateYear as summaryDateYear,
	pim.summaryDateMonth as summaryDateMonth,
	pim.plantDestroyedCount as plantDestroyedCount,
	pim.harvestedCount as harvestedCount,
	pim.plantDestroyedCount / pim.harvestedCount as ratioDH,
	apm.avgOZPriceAvg as avgOZPriceAvg
FROM plant_info_monthly pim, avg_price_monthly apm
    WHERE apm.summaryDateYear = pim.summaryDateYear
    AND apm.summaryDateMonth = pim.summaryDateMonth
ORDER BY pim.summaryDateYear, pim.summaryDateMonth;
