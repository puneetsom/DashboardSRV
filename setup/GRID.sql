drop table ea_grid;
create table ea_grid (
   grid_id                    varchar2(50)  not null -- id to identify columns
 , grid_version               number(3)     not null -- increment whenever there is a configuration change
 , locked_column_count        number(1)     not null default 0   -- number of locked columns on the left 
 , allow_locked_column_change varchar2(1)   not null default 'Y' -- Y/N 
 , show_footer                varchar2(1)   not null default 'Y' -- Y/N
 , allow_show_footer_change   varchar2(1)   not null default 'Y' -- Y/N
 , allow_filtering            varchar2(1)   not null default 'Y' -- Y/N
 , update_oper_id             number(6)     not null
 , create_date                date          not null
 , create_oper_id             number(6)     not null
 , constraint ea_grid_pk primary key (grid_id)
);

drop table ea_grid_columns;
create table ea_grid_columns (
   grid_id                varchar2(50)  not null -- id to identify columns
 , column_id              varchar2(50)  not null -- id to identify columns
 , column_order           number(3)     not null
 , data_field             varchar2(30)  not null
 , data_type              varchar2(10)  not null default 'string' -- string, checkbox, currency, date, datetime, number, percent, phone, zipcode
 , header_text            varchar2(50)
 , width                  number(3)
 , text_align             varchar2(10) -- center, left, right
 , domain_code            varchar2(30)
 , data_tip_field         varchar2(30) -- if different than data_field
 , item_renderer          varchar2(100)
 , fractional_digits      number(2)     not null default 2
 , leading_zero           varchar2(1)   not null default 'N' -- Y/N
 , use_grouping           varchar2(1)   not null default 'Y' -- Y/N
 , date_time_pattern      varchar2(30) 
 , visibile               varchar2(10)  not null default 'true' -- true, false, or always
 , item_renderer_sec_prof varchar2(50)  -- security profile needed to see column. ie. 100,914,HDDASH001
 , updatable              varchar2(1)   not null default 'N'
 , updatable_sec_prof     varchar2(50)  -- security profile needed to update column ie. 100,914,HDDASH001
 , input_type             varchar2(30)  -- TextInput, TextArea, ComboBox, DropDownList
 , item_editor            varchar2(100) -- use if different from input_type
 , footer_data_type       varchar2(10)  -- currency, number, percent
 , footer_function        varchar2(10)  -- avg, count, sum
 , footer_label           varchar2(40)
 , filter_field           varchar2(30)  -- if different from data_field
 , allow_filtering        varchar2(1)   not null default 'Y' -- Y/N - overrides grid-level value
 , locale                 varchar2(10)  not null default 'en_US'
 , update_oper_id         number(6)     not null
 , create_date            date          not null
 , create_oper_id         number(6)     not null
 , constraint ea_grid_columns_pk primary key (grid_id, column_id)
);

drop sequence ea_grid_favorite_seq;
create sequence ea_grid_favorite_seq 
  minvalue 0 maxvalue 9999999999 
  increment by 1 start with 1 
  nocache nocycle
/

drop table ea_grid_favorite;
create table ea_grid_favorite (
   favorite_id         number(10)    not null -- Generated from sequence
 , view_id             varchar2(50)  not null -- id from CustomDataGrid (detailGrid, grid)
 , grid_id             varchar2(100) not null -- id to identify columns
 , favorite_name       varchar2(100) not null -- entered by user My Favorite
 , locked_column_count number(1)     not null -- 
 , show_footer_ind     varchar2(1)   not null -- Y/N
 , public_ind          varchar2(1)   not null -- Y/N
 , update_date         date          not null
 , update_oper_id      number(6)     not null
 , create_date         date          not null
 , create_oper_id      number(6)     not null
 , constraint ea_grid_favorite_pk primary key (favorite_id)
);

create index ea_grid_favorite_1ix on ea_grid_favorite (grid_id, create_oper_id);
create index ea_grid_favorite_2ix on ea_grid_favorite (grid_id, public_ind);

drop table ea_grid_fav_cols;
create table ea_grid_fav_cols (
   favorite_id         number(10)    not null
 , column_id           varchar2(30)  not null -- matches "id" in columns XML
 , column_order        number(3)     not null
 , column_width        number(4)     not null
 , sort_order          number(3)     not null -- 0 if not sorted
 , descending_ind      varchar2(1)   not null -- Y/N
 , constraint ea_grid_fav_cols_pk primary key (favorite_id, column_id)
);

drop table ea_grid_fav_grp_cols;
create table ea_grid_fav_grp_cols (
   favorite_id         number(10)    not null 
 , column_id           varchar2(30)  not null -- matches "id" in columns XML
 , column_order        number(3)     not null
 , constraint ea_grid_fav_grp_cols_pk primary key (favorite_id, column_id)
);

drop table ea_grid_fav_filter_cols;
create table ea_grid_fav_filter_cols (
   favorite_id         number(10)     not null
 , data_field          varchar2(30)   not null -- matches "data_field" in columns XML
 , expression          varchar2(4000) not null -- expression to filter on
 , constraint ea_grid_fav_filters_pk primary key (favorite_id, data_field)
);
