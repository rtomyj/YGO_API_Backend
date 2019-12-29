CREATE DATABASE yugioh_API_DB;
USE yugioh_API_DB;

DROP TABLE ban_lists;
DROP TABLE cards;
DROP TABLE card_colors;

CREATE TABLE card_colors
(
	color_id INT NOT NULL AUTO_INCREMENT,
	card_color VARCHAR(15) NOT NULL,
	PRIMARY KEY(color_id)
);

CREATE TABLE cards
(
	card_number CHAR(8) NOT NULL,
	color_id INT NOT NULL,
	card_name VARCHAR(50) NOT NULL,
	card_attribute VARCHAR(20) NOT NULL,
	card_effect VARCHAR(1500),
	monster_type VARCHAR(30),
	monster_attack INT,
	monster_defense INT,
	PRIMARY KEY(card_number),
	FOREIGN KEY(color_id) REFERENCES card_colors(color_id),
	INDEX(monster_type(15), card_attribute(1), card_name(18))
);

CREATE TABLE ban_lists
(
	ban_list_date DATE NOT NULL,
	card_number CHAR(8) NOT NULL,
	ban_status varchar(15) NOT NULL,
	PRIMARY KEY(ban_list_date, card_number),
	FOREIGN KEY(card_number) REFERENCES cards(card_number),
	INDEX(ban_status(1))
);

INSERT INTO card_colors(card_color)
	VALUES	('Normal')
				, ('Effect')
				, ('Fusion')
				, ('Ritual')
				, ('Synchro')
				, ('XYZ')
				, ('Pendulum-Normal')
				, ('Pendulum-Effect')
				, ('Link')
				, ('Spell')
				, ('Trap');

