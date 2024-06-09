-- code for SQL commands to insert data into database as it is read in from its source URL 
-- These are executed with the PreparedStatement() method and are thus followed by a (?) String to contain parameters
-- Data types are stored in an Object[][] to be handled by Java


-- Plant Activity Information
INSERT INTO plant_info (
    summaryDate,agrEmployeeCount,plantTrackedCount,plantBatchCount,immatureCount,plantVegCount,plantFlowerCount,
    harvestActiveCount,harvestedCount,plantDestroyedCount,activeProductCount) 
        values (?,?,?,?,?,?,?,?,?,?,?);

-- Sale Price Information
INSERT INTO avg_price_info (
    dateSold,avgOZPrice)
        values (?,?);
