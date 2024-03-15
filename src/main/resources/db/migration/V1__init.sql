create type req_status as enum ('NEW', 'SENT', 'RECEIVED', 'ERROR_BUILD');
create type req_type as enum ('GetRequest', 'GetResponse', 'SendRequest', 'SendResponse');

create table if not exists req_get_request
(
    id               bigserial primary key,
    create_timestamp timestamp,
    root_tag         varchar,
    msg_id           varchar,
    content          text,
    update           timestamp,
    status           req_status,
    req_row          text,
    ref_id           bigint
);
create table if not exists req_log_request
(
    id      bigserial primary key,
    req_row text
);


create table if not exists req_get_response
(
    id               bigserial primary key,
    create_timestamp timestamp,
    root_tag         varchar,
    msg_id           varchar,
    original_msg_id  varchar,
    content          text,
    update           timestamp,
    status           req_status,
    req_row          text,
    ref_id           bigint
);

create table if not exists req_log_response
(
    id      bigserial primary key,
    req_row text
);

create table if not exists req_send_request
(
    id               bigserial primary key,
    create_timestamp timestamp,
    root_tag         varchar,
    msg_id           varchar,
    response_msg_id  varchar,
    content          text,
    update           timestamp,
    status           req_status,
    req_row          text

);

create table if not exists req_send_response
(
    id      bigserial primary key,
    req_row text
);

create table if not exists req_attachments
(
    id               bigserial primary key,
    ref_id           bigint,
    req_type         req_type,
    attach_row       text,
    attach_blob      bytea,
    attach_name      varchar,
    create_timestamp timestamp
);

create table if not exists vs_list
(
    id            bigserial primary key,
    root_tag      varchar(250),
    mnemonic      varchar(250),
    mnemonic_desc varchar(250),
    description   text
);