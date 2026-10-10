-- Sample providers loaded at start up (in-memory H2 database)
INSERT INTO providers (name, unit_rate_pence, standing_charge_pence, renewable_percent, active) VALUES
('Northern Power Co', 24.50, 53.35, 35, TRUE),
('GreenGrid Energy', 26.10, 48.20, 100, TRUE),
('Tyne Valley Electric', 22.95, 60.10, 20, TRUE),
('Coastal Wind Supply', 25.40, 51.00, 85, TRUE);
