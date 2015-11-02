create table WIDGET.APP_USAGE
(	APP_ID          varchar2(10),
	ACTION_ID       varchar2(20),
	COUNT           number(7,0)
);


insert into APP_USAGE
values ('EXECADV', 'LAUNCH', 0);



update APP_USAGE set COUNT = (COUNT + 1)
 where APP_ID = ?
   and ACTION_ID = ?;
