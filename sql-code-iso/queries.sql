-- code for the SQL queries run to analyze data once it is read in and store it in respective tables
-- These are stored in the QUERIES[][] data structure

-- Yearly data:
INSERT INTO avg_price_annual (
	summaryDateYear, avgOZPriceSum, avgOZPriceAvg, avgOZPriceCnt)

SELECT YEAR(dateSold) as yr,
	SUM(avgOZPrice), AVG(avgOZPrice), COUNT(avgOZPrice)
FROM avg_price_info
GROUP BY yr
ORDER BY yr;

INSERT INTO plant_info_annual (
	summaryDateYear, agrEmployeeCount, plantTrackedCount, plantBatchCount, immatureCount, plantVegCount, plantFlowerCount,
	harvestActiveCount, harvestedCount, plantDestroyedCount, activeProductCount)
SELECT YEAR(summaryDate) as yr,
    SUM(agrEmployeeCount),SUM(plantTrackedCount),SUM(plantBatchCount), SUM(immatureCount), SUM(plantVegCount),
    SUM(plantFlowerCount),SUM(harvestActiveCount),SUM(harvestedCount),SUM(plantDestroyedCount),SUM(activeProductCount)
FROM plant_info
GROUP BY yr
ORDER BY yr;

-- Monthly data:
INSERT INTO avg_price_monthly (
    summaryDateYear, summaryDateMonth, avgOZPriceSum, avgOZPriceAvg, avgOZPriceCnt)
SELECT YEAR(dateSold) as yr, MONTH(dateSold) as mo,
    SUM(avgOZPrice), AVG(avgOZPrice), COUNT(avgOZPrice)
FROM avg_price_info 
GROUP BY yr, mo
ORDER BY yr, mo;

INSERT INTO plant_info_monthly (
    summaryDateYear, summaryDateMonth, agrEmployeeCount, plantTrackedCount, plantBatchCount, immatureCount, plantVegCount, plantFlowerCount,
    harvestActiveCount, harvestedCount, plantDestroyedCount, activeProductCount)
SELECT YEAR(summaryDate) as yr, MONTH(summaryDate) as mo,
    SUM(agrEmployeeCount),SUM(plantTrackedCount),SUM(plantBatchCount), SUM(immatureCount), SUM(plantVegCount),
    SUM(plantFlowerCount),SUM(harvestActiveCount),SUM(harvestedCount),SUM(plantDestroyedCount),SUM(activeProductCount)
FROM plant_info
GROUP BY yr, mo
ORDER BY yr, mo;

-- Weekly data:
INSERT INTO avg_price_weekly (
    summaryDateYear, summaryDateWeek, avgOZPriceSum, avgOZPriceAvg, avgOZPriceCnt)
SELECT YEAR(dateSold) as yr, WEEK(dateSold) as wk,
    SUM(avgOZPrice), AVG(avgOZPrice), COUNT(avgOZPrice)
FROM avg_price_info
GROUP BY yr, wk
ORDER BY yr, wk;

INSERT INTO plant_info_weekly (
    summaryDateYear, summaryDateWeek, agrEmployeeCount, plantTrackedCount, plantBatchCount, immatureCount, plantVegCount, plantFlowerCount,
    harvestActiveCount, harvestedCount, plantDestroyedCount, activeProductCount)
SELECT YEAR(summaryDate) as yr, WEEK(summaryDate) as wk,
    SUM(agrEmployeeCount),SUM(plantTrackedCount),SUM(plantBatchCount), SUM(immatureCount), SUM(plantVegCount),
    SUM(plantFlowerCount),SUM(harvestActiveCount),SUM(harvestedCount),SUM(plantDestroyedCount),SUM(activeProductCount)
FROM plant_info
GROUP BY yr, wk
ORDER BY yr, wk;