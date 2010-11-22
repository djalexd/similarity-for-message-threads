
DROP DATABASE IF EXISTS       bachelor_project;
CREATE DATABASE IF NOT EXISTS bachelor_project;
USE bachelor_project;

CREATE TABLE IF NOT EXISTS `Language` (

	`id`          int(10) unsigned NOT NULL AUTO_INCREMENT,
	`name`        varchar(128) NOT NULL,
	`description` text,
	PRIMARY KEY (`id`)
			
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
     


CREATE TABLE IF NOT EXISTS `Word` (

	`id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
	`content`    varchar(64) NOT NULL,
	`labels`     varchar(128),
	`languageID` int(10) unsigned,
	PRIMARY KEY (`id`)
			
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `MessageBoard` (

	`id`           int(10) unsigned NOT NULL AUTO_INCREMENT,
	`name`         varchar(512) NOT NULL,
	`description`  text,
	`url`          varchar(512),
	PRIMARY KEY (`id`)
	
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



CREATE TABLE IF NOT EXISTS `MessageThread` (

	`id`      int(10) unsigned NOT NULL AUTO_INCREMENT,
	`name`    varchar(512) NOT NULL,
	`url`     varchar(512),
	`boardID` int(10) unsigned NOT NULL,
	
	PRIMARY KEY (`id`),
	FOREIGN KEY (`boardID`) REFERENCES
		`MessageBoard`(`id`)

) ENGINE = MyISAM DEFAULT CHARSET = latin1;



CREATE TABLE IF NOT EXISTS `User` (

	`id`      int(10) unsigned NOT NULL AUTO_INCREMENT,
	`name`    varchar(512) NOT NULL,
	`boardID` int(10) unsigned NOT NULL,
	
	PRIMARY KEY (`id`),
	FOREIGN KEY (`boardID`) REFERENCES
		`MessageBoard`(`id`)

) ENGINE = MyISAM DEFAULT CHARSET = latin1;



CREATE TABLE IF NOT EXISTS `Message` (

	`id`                int(10) unsigned NOT NULL AUTO_INCREMENT,
	`threadID`          int(10) unsigned NOT NULL,
	`userID`            int(10) unsigned NOT NULL,
	`publishDate`       varchar(128),
	`content`           text,
	`formatted_content` text,
	`url`               varchar(512),
	`parentID`          int(10),
	
	PRIMARY KEY (`id`),
	FOREIGN KEY (`threadID`) REFERENCES
		`MessageThread`(`id`),
	FOREIGN KEY (`userID`) REFERENCES
		`User`(`id`)

) ENGINE = MyISAM DEFAULT CHARSET = latin1;


CREATE TABLE IF NOT EXISTS `settings` (

	`id`            int(10) unsigned NOT NULL AUTO_INCREMENT,
	`tableName`     varchar(128) NOT NULL,
	`tableID`       int(10) unsigned NOT NULL,
	`propertyKey`   varchar(128) NOT NULL,
	`propertyValue` text NOT NULL,
	
	PRIMARY KEY (`id`),
    UNIQUE KEY (`tableName`,`tableID`,`propertyKey`)
	
) ENGINE=InnoDb DEFAULT CHARSET=latin1;

