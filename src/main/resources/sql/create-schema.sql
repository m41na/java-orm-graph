create table if not exists tbl_account (
    id SERIAL primary key ,
    username text not null,
    password text not null,
    date_created timestamp default CURRENT_TIMESTAMP,
    constraint uniq_username unique (username)
);

CREATE TABLE if not exists tbl_address (
    id          SERIAL PRIMARY KEY,
    street_name TEXT    NOT NULL,
    unit_number TEXT,
    city        TEXT    NOT NULL,
    state       TEXT    NOT NULL,
    zip_code    TEXT    NOT NULL
);

CREATE TABLE if not exists tbl_seller (
    id              SERIAL NOT null,
    first_name      TEXT    NOT NULL,
    last_name       TEXT    NOT NULL,
    email_address   TEXT    NOT null,
    date_created    timestamp DEFAULT CURRENT_TIMESTAMP,
    seller_address  INTEGER ,
    seller_account  INTEGER NOT NULL,
    billing_address INTEGER ,
    CONSTRAINT seller_id PRIMARY KEY (id),
    CONSTRAINT uniq_email unique (email_address),
    CONSTRAINT fk_seller_account foreign key (seller_account) REFERENCES tbl_account (id),
    CONSTRAINT fk_seller_billing_addr foreign key (billing_address) REFERENCES tbl_address(id),
    CONSTRAINT fk_seller_addr foreign key (seller_address) REFERENCES tbl_address(id)
);

create table if not exists tbl_sale_item (
    id SERIAL not null,
    title text not null,
    description text not null,
    price NUMERIC not null default 0.00,
    quantity INTEGER not null default 0,
    date_added timestamp default CURRENT_TIMESTAMP,
    item_seller INTEGER not NULL,
    item_location INTEGER ,
    constraint sale_item_id primary key (id),
    constraint uniq_sale_item unique (title, item_seller),
    constraint fk_item_seller foreign key (item_seller) references tbl_seller (id),
    constraint fk_item_location foreign key (item_location) references tbl_address(id)
);

create table if not exists tbl_music_album (
    title text not null,
    artist text not null,
    genre text not null,
    price NUMERIC not null default 0.00,
    release_date date default current_date not null,
    producer text,
    constraint pk_music_album primary key (title, artist)
);

create table if not exists tbl_songs (
    id SERIAL not null,
    title text not null,
    album text not null,
    artist text not null,
    description text not null,
    constraint pk_song primary key (id),
    constraint uniq_song unique (title, album, artist),
    constraint fk_song_album foreign key (album, artist) references tbl_music_album (title, artist)
);

create table if not exists tbl_listener (
    id SERIAL primary key,
    name text not null
);

create table if not exists tbl_requested (
    id SERIAL primary key,
    listener INTEGER not null,
    song INTEGER not null,
    constraint fk_song_listener foreign key (listener) references tbl_listener(id),
    constraint fk_requested_song foreign key (song) references tbl_songs(id)
);
