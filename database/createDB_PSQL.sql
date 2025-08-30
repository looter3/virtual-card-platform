-- 1. Enums
CREATE TYPE card_status AS ENUM ('ACTIVE', 'BLOCKED');
CREATE TYPE transaction_type AS ENUM ('SPEND', 'TOPUP');

-- 2. "User" table
CREATE TABLE "User" (
    id INTEGER NOT NULL PRIMARY KEY,
    username VARCHAR(36) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

-- Sequence and trigger for User.id
CREATE SEQUENCE user_id_seq START 1;

CREATE OR REPLACE FUNCTION user_id_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id IS NULL THEN
        NEW.id := nextval('user_id_seq');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER user_id_trigger
BEFORE INSERT ON "User"
FOR EACH ROW
EXECUTE FUNCTION user_id_trigger_func();

-- 3. "Card" table
CREATE TABLE "Card" (
    id INTEGER NOT NULL PRIMARY KEY,
    userId INTEGER NOT NULL,
    code VARCHAR(36) NOT NULL,
    cardholderName VARCHAR(255) NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status card_status NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    FOREIGN KEY (userId) REFERENCES "User"(id)
);

-- Sequence and trigger for Card.id
CREATE SEQUENCE card_id_seq START 1;

CREATE OR REPLACE FUNCTION card_id_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id IS NULL THEN
        NEW.id := nextval('card_id_seq');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER card_id_trigger
BEFORE INSERT ON "Card"
FOR EACH ROW
EXECUTE FUNCTION card_id_trigger_func();

-- 4. "Transaction" table
CREATE TABLE "Transaction" (
    id INTEGER NOT NULL PRIMARY KEY,
    code VARCHAR(36) NOT NULL,
    cardId INTEGER NOT NULL,
    type transaction_type NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cardId) REFERENCES "Card"(id)
);

-- Sequence and trigger for Transaction.id
CREATE SEQUENCE transaction_id_seq START 1;

CREATE OR REPLACE FUNCTION transaction_id_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id IS NULL THEN
        NEW.id := nextval('transaction_id_seq');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transaction_id_trigger
BEFORE INSERT ON "Transaction"
FOR EACH ROW
EXECUTE FUNCTION transaction_id_trigger_func();
