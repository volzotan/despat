the servpat folder needs to be writable for the uwsgi process (usually www-data) in order 
to access the *.db file.

chown www-data .

# Upload to server

sh upload.sh

# Restart on server

systemctl restart emperor.uwsgi

# Logfiles

/var/log/uwsgi/servpat_uwsgi.log