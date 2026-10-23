-- Sample data so both endpoints can be exercised immediately.
-- The kata allows exactly two endpoints (no "create deposit"), so seeding happens here.
-- Rows are chosen to cover every branch of the interest rules:
--   1  basic    45 days  -> earns 1%/12
--   2  basic    30 days  -> boundary: no interest yet (rule is days > 30)
--   3  student  31 days  -> earns 3%/12
--   4  student 366 days  -> boundary: student interest stops at one year
--   5  premium  45 days  -> boundary: premium interest starts after 45 days (none yet)
--   6  premium  46 days  -> earns 5%/12
--   7  premium 400 days  -> premium keeps earning after one year

INSERT INTO time_deposits (id, plan_type, days, balance) VALUES
    (1, 'basic',    45,  1000.00),
    (2, 'basic',    30,  2500.00),
    (3, 'student',  31,  1200.00),
    (4, 'student', 366,  3000.00),
    (5, 'premium',  45, 10000.00),
    (6, 'premium',  46,  1200.00),
    (7, 'premium', 400,  7500.50);

INSERT INTO withdrawals (id, time_deposit_id, amount, date) VALUES
    (1, 1, 100.00, DATE '2026-09-15'),
    (2, 1,  50.00, DATE '2026-10-01'),
    (3, 4, 500.00, DATE '2026-08-20'),
    (4, 7, 250.25, DATE '2026-10-10');
