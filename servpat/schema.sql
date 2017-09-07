drop table if exists status;
drop table if exists events;

create table status (
  id integer primary key autoincrement,

  deviceid text not null,
  deviceName text,
  timestamp numeric not null,

  numberImages integer not null,
  freeSpaceInternal real,
  freeSpaceExternal real,

  batteryInternal real,
  batteryExternal real,

  stateCharging integer
);

create table events (
  id integer primary key autoincrement,

  deviceid text not null,
  timestamp numeric not null,

  eventtype integer not null,
  payload text
);