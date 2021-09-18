drop table if exists payment;
drop table if exists payment_back;

create table payment
(
    id         bigint         not null auto_increment,
    amount     decimal(19, 2) not null,
    created_at datetime       not null,
    order_id   bigint         not null,
    updated_at datetime       not null,
    primary key (id)
) engine=InnoDB
;

create table payment_back
(
    id       bigint         not null auto_increment,
    amount   decimal(19, 2) not null,
    order_id bigint         not null,
    primary key (id)
) engine=InnoDB
;

create index idx_created_at on payment (created_at);