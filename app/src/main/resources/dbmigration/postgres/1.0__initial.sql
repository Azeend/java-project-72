create table url (
    id                            bigint PRIMARY KEY generated by default as identity not null,
    name                          varchar(255),
    created_at                    timestamptz not null,
);

create table url_check (
    id                            bigint PRIMARY KEY generated by default as identity not null,
    statusCode                    integer not null ,
    title                         varchar(255),
    h1                            varchar(255),
    description                   text,
    url_id                        bigint REFERENCES url (id),
    created_at                    timestamptz not null,
);

