insert into tbl_address (street_name, unit_number, city, state, zip_code)
select '186 Batson Court', '', 'New Lenox', 'IL', '60451'
where not exists (
    select * from tbl_address
             where street_name = '186 Batson Court' and
                   unit_number = '' and
                   city = 'New Lenox' and
                   state = 'IL' and
                   zip_code = '60451');

--select * from tbl_address;

insert into tbl_account (username, password, date_created)
values ('steve', 'maina', current_timestamp)
on conflict (username) do nothing ;

--select * from tbl_account;

insert into tbl_seller (first_name, last_name, email_address, date_created, seller_address, seller_account)
values ('steve', 'maina', 'steve@email.com', current_timestamp, 1, 1)
on conflict (email_address) do nothing;

--select * from tbl_seller;

insert into tbl_sale_item (title, description, price, quantity, date_added, item_seller, item_location)
values ('toyota corolla', '2001 sedan', 4500.00, 1, current_timestamp, 2, null)
on conflict (title, item_seller) do update;