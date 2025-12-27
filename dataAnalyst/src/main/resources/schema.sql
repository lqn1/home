CREATE TABLE IF NOT EXISTS logininfo (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
logintime VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS directorytype (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
fatherid BIGINT null,
icon varchar(20) not null,
tablename varchar(25) null,
translate varchar(200) null,
createtime varchar(20) not null,
updatetime varchar(20) not null
);
CREATE TABLE IF NOT EXISTS loginInfo (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
logintime varchar(20) not null
);


CREATE TABLE IF NOT EXISTS files (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
fileName varchar(20) not null,
storedName varchar(25) null,
filePath varchar(200) null,
fileType varchar(20) not null,
fileSize varchar(20) not null,
parentId BIGINT not null,
excelData CLOB not null,
uploadTime TIMESTAMP,
updateTime TIMESTAMP
);


CREATE TABLE IF NOT EXISTS tablecolumns (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
fileId BIGINT ,
tableName varchar(30) not null,
column varchar(30) not null,
columnName varchar(100) null,
ord BIGINT
);


