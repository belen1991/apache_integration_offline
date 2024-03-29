CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    limit_bal INT,
    sex VARCHAR(10),
    education VARCHAR(50),
    marriage VARCHAR(50),
    age INT,
    pay_0 INT,
    pay_2 INT,
    pay_3 INT,
    pay_4 INT,
    pay_5 INT,
    pay_6 INT,
    bill_1 INT,
    bill_2 INT,
    bill_3 INT,
    bill_4 INT,
    bill_5 INT,
    bill_6 INT,
    pay_amt_1 INT,
    pay_amt_2 INT,
    pay_amt_3 INT,
    pay_amt_4 INT,
    pay_amt_5 INT,
    pay_amt_6 INT,
    default_payment_next_month BOOLEAN
);
