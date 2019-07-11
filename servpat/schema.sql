
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

  stateCharging integer,

  temperatureDevice real,
  temperatureBattery real,

  freeMemoryHeap integer,
  freeMemoryHeapNative integer
);

create table session (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  sessionId integer not null,
  start text not null,
  end text,

  latitude real,
  longitude real,

  homographyMatrix text,

  -- exclusion_image
  -- compressed_image

  resumed integer,

  shutterInterval integer,
  exposureThreshold real,
  exposureCompensation real
);

create table capture (
  id integer primary key autoincrement,
  deviceId text not null,
  serverTimestamp text not null,

  captureId integer not null,
  sessionId integer,
  recordingTime text not null,
  exposureTime real,
  aperture real,
  iso real,
  exposureValue real,
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

  timestamp text not null,

  filename text not null
);