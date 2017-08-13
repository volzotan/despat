drop table if exists entries;
drop table if exists status;

create table entries (
  id integer primary key autoincrement,
  title text not null,
  'text' text not null
);

create table status (
  id integer primary key autoincrement,

  deviceid text not null,
  timestamp numeric not null,

  numberImages integer not null,
  freeSpace real not null,

  batteryInternal real not null,
  batteryExternal real not null
);