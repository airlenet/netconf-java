CREATE TABLE NETCONF_DEVICE(
ID BIGINT AUTO_INCREMENT NOT NULL comment '序号',
NAME VARCHAR(255),
IP VARCHAR (255),
PORT INT,
USER VARCHAR(255) ,
PASS VARCHAR(255),
primary key(ID)
);