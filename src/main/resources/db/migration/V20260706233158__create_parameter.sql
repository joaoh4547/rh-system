create type parameter_type as enum('text', 'number','date', 'boolean', 'secret');

create table rh_parameter(
    parameter_code varchar(255) not null,
    parameter_name varchar(100) not null,
    parameter_description varchar(255),
    parameter_validator varchar(255) not null,
    parameter_type parameter_type not null default 'text',
    parameter_value varchar(255) not null,
    active boolean not null default true,
    deleted boolean not null default false,

    constraint pk_rh_parameter primary key (parameter_code)
);

create index  idx_parameter_active on rh_parameter(active);
create index  idx_parameter_deleted on rh_parameter(deleted);