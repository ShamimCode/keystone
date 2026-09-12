-- Seed data so the app is usable out of the box.
-- All seed users share password: Password123!
-- The hash below is a real, verified BCrypt hash of that exact password.

insert into customers (id, name, contact_email) values
    ('11111111-1111-1111-1111-111111111111', 'Meridian Facilities Management', 'ops@meridianfm.example');

insert into sites (id, customer_id, name, address) values
    ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111', 'Meridian HQ Tower', '100 Meridian Way, Guwahati'),
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Meridian Warehouse 3', '45 Industrial Road, Guwahati');

insert into users (id, name, email, password_hash, role, customer_id) values
    ('33333333-3333-3333-3333-333333333331', 'Dana Dispatcher', 'dispatcher@keystone.example', '$2b$10$WGeo/FxQ5hY18fEhV6yDWeN95fGh6Dr5GODIvnaOCq5c6OXv4AaLe', 'DISPATCHER', null),
    ('33333333-3333-3333-3333-333333333332', 'Tom Technician', 'technician@keystone.example', '$2b$10$WGeo/FxQ5hY18fEhV6yDWeN95fGh6Dr5GODIvnaOCq5c6OXv4AaLe', 'TECHNICIAN', null),
    ('33333333-3333-3333-3333-333333333333', 'Mira Manager', 'manager@keystone.example', '$2b$10$WGeo/FxQ5hY18fEhV6yDWeN95fGh6Dr5GODIvnaOCq5c6OXv4AaLe', 'MANAGER', null),
    ('33333333-3333-3333-3333-333333333334', 'Cara Customer', 'customer@keystone.example', '$2b$10$WGeo/FxQ5hY18fEhV6yDWeN95fGh6Dr5GODIvnaOCq5c6OXv4AaLe', 'CUSTOMER', '11111111-1111-1111-1111-111111111111');

insert into parts (id, name, sku, unit_cost, stock_qty) values
    ('44444444-4444-4444-4444-444444444441', 'HVAC Air Filter 20x20', 'HVAC-FILT-2020', 18.50, 40),
    ('44444444-4444-4444-4444-444444444442', 'Copper Pipe Fitting 1/2"', 'PLMB-FIT-050', 4.25, 120),
    ('44444444-4444-4444-4444-444444444443', 'Circuit Breaker 20A', 'ELEC-BRK-20A', 12.00, 25);