CREATE TABLE `ChineseStock`.`Company`
(
   stockid                 VARCHAR(20) NOT NULL COMMENT '股票ID',
   companyname             VARBINARY(20) NOT NULL COMMENT '公司名称',
   currentprice            FLOAT(4, 2) COMMENT 'Current stock price',
   currentpricetimestamp   DATETIME NOT NULL,
   openprice               FLOAT(4, 2) COMMENT '开盘价',
   closeprice              FLOAT(4, 2) COMMENT '昨日收盘价',
   marketcap               FLOAT(4, 2) COMMENT '总市值',
   capitalizationvalue     FLOAT(4, 2) COMMENT '流通市值',
   tradingvolume           FLOAT(4, 2) COMMENT '成交量',
   tradingvalue            FLOAT(4, 2) COMMENT '成交额',
   oscillation             FLOAT(4, 2) COMMENT '股票振幅',
   turnoverrate            FLOAT(4, 2) DEFAULT 0.0 COMMENT '换手率',
   `PBR`                   INT(5)
                              DEFAULT 0
                              COMMENT '市净率 Price-to-book ratio',
   `PER`                   INT(5) DEFAULT 0 COMMENT '市盈率',
   lastUpdateDateTime   DATETIME NOT NULL COMMENT '最后一次更新时间',
   PRIMARY KEY(stockid)
   )