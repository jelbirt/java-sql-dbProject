-- SQL Commands used to create tables to store data once it is read in from .csv URL source
-- These commands were executed through Java Database Connection

CREATE TABLE plant_info(
    ID INTEGER PRIMARY KEY AUTO_INCREMENT,
    summaryDate DATE,
    agrEmployeeCount INTEGER,
    plantTrackedCount INTEGER,
    plantBatchCount INTEGER,
    immatureCount INTEGER,
    plantVegCount INTEGER,
    plantFlowerCount INTEGER,
    harvestActiveCount INTEGER,
    harvestedCount INTEGER,
    plantDestroyedCount INTEGER,
    activeProductCount INTEGER;
)

CREATE TABLE avg_price_info(
    ID INTEGER PRIMARY KEY AUTO_INCREMENT,
    dateSold DATE,
    avgOzPrice DECIMAL;
)

