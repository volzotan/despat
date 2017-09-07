the servpat folder needs to be writable for the uwsgi process (usually www-data) in order 
to access the *.db file.

chown www-data .