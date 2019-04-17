jekyll build

rsync -av _site/ grinzold.de:/var/www/grinzold/despat

rsync -av ../visualization/map.html grinzold.de:/var/www/grinzold/despat/visualize/
rsync -av ../visualization/map.js grinzold.de:/var/www/grinzold/despat/visualize/
rsync -av ../visualization/despat-util.js grinzold.de:/var/www/grinzold/despat/visualize/
rsync -av ../visualization/lib grinzold.de:/var/www/grinzold/despat/visualize/
rsync -av ../visualization/data grinzold.de:/var/www/grinzold/despat/visualize/