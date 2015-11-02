create table WIDGET.APP_USAGE_DETAILS
(	APP_ID          varchar2(10),
	ACTION_ID       varchar2(20),
	USER_ID         varchar2(20),
	USAGE_DATE      date
);


insert into APP_USAGE_DETAILS
 values ('EXECADV', 'LAUNCH', 'mh5659', SYSDATE);

