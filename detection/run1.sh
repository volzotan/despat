SIZE=1000

rm tiles/* processed/* resized/*
python tile_generator.py --input pedestriancrossing.jpg --output tiles --sizes $SIZE --mode tile
python tile_generator.py --input tiles --output resized --sizes $SIZE --mode resize
scp resized/* bollogg:/home/volzotan/despat_testimages