rsync -av                                   \
    --exclude "*.pb"                        \
    --exclude "*.jpg"                       \
    --exclude "*.png"                       \
    --exclude "detector/models"             \
    --exclude "Despat"                      \
    --exclude "*Playground"                 \
    --exclude "__pycache__"                 \
    * bollogg-local:/home/volzotan/despat/