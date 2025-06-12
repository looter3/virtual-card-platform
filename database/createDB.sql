-- Create schema
CREATE SCHEMA IF NOT EXISTS card_system;
USE card_system;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Create Card table first
CREATE TABLE Card (
    id CHAR(36) NOT NULL PRIMARY KEY,
    cardholderName VARCHAR(255) NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0
);

-- Create Transaction table with a foreign key reference to Card
CREATE TABLE `Transaction` (
    id CHAR(36) NOT NULL PRIMARY KEY,
    cardId CHAR(36) NULL,
    type ENUM('SPEND', 'TOPUP') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY cardId (cardId),
    CONSTRAINT Transaction_ibfk_1 FOREIGN KEY (cardId) REFERENCES Card(id)
);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
