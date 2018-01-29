drop table if exists status;
drop table if exists session;
drop table if exists capture;
drop table if exists event;
drop table if exists upload;

create table status (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  statusId integer not null,
  timestamp text not null,
  deviceName text,

  imagesTaken integer not null,
  imagesInMemory integer not null,

  freeSpaceInternal real,
  freeSpaceExternal real,

  batteryInternal real,
  batteryExternal real,

  stateCharging integer
);

create table session (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  sessionid integer not null,
  start text not null,
  end text,

  latitude real,
  longitude real,

  -- exclusion_image
  -- compressed_image

  resumed integer
);

create table capture (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  captureId integer not null,
  sessionId integer,
  recordingTime text not null,
  -- imagePath

  FOREIGN KEY(sessionid) REFERENCES session(id)
);

create table event (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  eventId text not null,
  timestamp text not null,

  type integer not null,
  payload text
);

create table upload (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  timestamp integer not null,

  filename text not null
);