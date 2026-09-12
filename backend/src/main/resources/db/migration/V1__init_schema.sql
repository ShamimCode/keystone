-- Project KEYSTONE - initial schema
-- Mirrors the domain model in Section 05 of the engineering brief.

create extension if not exists "pgcrypto";

create table customers (
    id              uuid primary key default gen_random_uuid(),
    name            varchar(255) not null,
    contact_email   varchar(255),
    created_at      timestamptz not null default now()
);

create table sites (
    id              uuid primary key default gen_random_uuid(),
    customer_id     uuid not null references customers(id) on delete cascade,
    name            varchar(255) not null,
    address         varchar(500) not null,
    created_at      timestamptz not null default now()
);
create index idx_sites_customer_id on sites(customer_id);

create table users (
    id              uuid primary key default gen_random_uuid(),
    name            varchar(255) not null,
    email           varchar(255) not null unique,
    password_hash   varchar(255) not null,
    role            varchar(20) not null check (role in ('DISPATCHER','TECHNICIAN','MANAGER','CUSTOMER')),
    customer_id     uuid references customers(id) on delete set null,
    enabled         boolean not null default true,
    created_at      timestamptz not null default now()
);
create index idx_users_customer_id on users(customer_id);

create table parts (
    id              uuid primary key default gen_random_uuid(),
    name            varchar(255) not null,
    sku             varchar(100) not null unique,
    unit_cost       numeric(12,2) not null default 0 check (unit_cost >= 0),
    stock_qty       integer not null default 0 check (stock_qty >= 0),
    version         bigint not null default 0
);

create table work_orders (
    id              uuid primary key default gen_random_uuid(),
    code            varchar(50) not null unique,
    title           varchar(255) not null,
    description     text,
    priority        varchar(20) not null check (priority in ('LOW','MEDIUM','HIGH','CRITICAL')),
    status          varchar(20) not null default 'NEW'
                        check (status in ('NEW','ASSIGNED','IN_PROGRESS','ON_HOLD','COMPLETED','CLOSED','CANCELLED')),
    customer_id     uuid not null references customers(id),
    site_id         uuid not null references sites(id),
    assigned_to     uuid references users(id),
    sla_due_at      timestamptz,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    version         bigint not null default 0
);
create index idx_work_orders_customer_id on work_orders(customer_id);
create index idx_work_orders_site_id on work_orders(site_id);
create index idx_work_orders_assigned_to on work_orders(assigned_to);
create index idx_work_orders_status on work_orders(status);

create table work_order_status_history (
    id              uuid primary key default gen_random_uuid(),
    work_order_id   uuid not null references work_orders(id) on delete cascade,
    from_status     varchar(20),
    to_status       varchar(20) not null,
    changed_by      uuid not null references users(id),
    changed_at      timestamptz not null default now(),
    note            text
);
create index idx_wo_status_history_work_order_id on work_order_status_history(work_order_id);

create table part_usages (
    id              uuid primary key default gen_random_uuid(),
    work_order_id   uuid not null references work_orders(id) on delete cascade,
    part_id         uuid not null references parts(id),
    qty_used        integer not null check (qty_used > 0),
    logged_at       timestamptz not null default now()
);
create index idx_part_usages_work_order_id on part_usages(work_order_id);

create table time_logs (
    id              uuid primary key default gen_random_uuid(),
    work_order_id   uuid not null references work_orders(id) on delete cascade,
    technician_id   uuid not null references users(id),
    minutes         integer not null check (minutes > 0),
    note            text,
    logged_at       timestamptz not null default now()
);
create index idx_time_logs_work_order_id on time_logs(work_order_id);