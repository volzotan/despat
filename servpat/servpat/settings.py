class Config(object):
    DEBUG = False
    TESTING = False
    DATABASE_URI = 'sqlite://:memory:'

class CorodiakConfig(Config):
    DEBUG = True
    DATABASE_URI = 'mysql://user@localhost/foo'

class GrinzoldConfig(Config):
    DATABASE_URI = 'mysql://user@localhost/foo'
