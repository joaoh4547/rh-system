create table rh_user_group
(
    user_id  bigint not null,
    group_id bigint not null,
    constraint pk_user_group primary key (user_id, group_id),
    constraint fk_user_group_user foreign key (user_id) references rh_user (id),
    constraint fk_user_group_group foreign key (group_id) references rh_group (id)
);

create table rh_user_functionality
(
    user_id       bigint       not null,
    functionality varchar(255) not null,

    constraint pk_user_functionality primary key (user_id, functionality),
    constraint fk_user_functionality_user foreign key (user_id) references rh_user (id)
);