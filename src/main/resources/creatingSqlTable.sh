CREATE TABLE `StockMarket`.`ChineseMarketCompany`
(
   stockid                 VARCHAR(20) NOT NULL COMMENT '股票ID',
   companyname             VARCHAR(255) NOT NULL COMMENT '公司名称',
   currentprice            Double(8, 2) COMMENT 'Current stock price',
   currentpricetimestamp   DATETIME NOT NULL,
   openprice               Double(8, 2) COMMENT '开盘价',
   closeprice              Double(8, 2) COMMENT '昨日收盘价',
   highest_price			  Double(8,2)  COMMENT '最高价',
   lowest_price			  Double(8,2)  COMMENT '最低价',
   marketcap               VARCHAR(20) COMMENT '总市值',
   capitalizationvalue     VARCHAR(20) COMMENT '流通市值',
   tradingvolume           VARCHAR(20) COMMENT '成交量',
   tradingvalue            VARCHAR(20) COMMENT '成交额',
   oscillation             VARCHAR(20) COMMENT '股票振幅',
   turnoverrate            VARCHAR(20) COMMENT '换手率',
   `PBR`                   DOUBLE(8,2)
                              DEFAULT 0
                              COMMENT '市净率 Price-to-book ratio',
   `PER`                   DOUBLE(8,2) DEFAULT 0 COMMENT '市盈率',
   last_update_date_time   DATETIME NOT NULL COMMENT '最后一次更新时间',
   PRIMARY KEY(stockid)
   )