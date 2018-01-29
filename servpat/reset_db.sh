rm grinzold.db
sqlite3 grinzold.db < schema.sql
chown www-data .
chown www-data grinzold.db
sudo systemctl restart emperor.uwsgi