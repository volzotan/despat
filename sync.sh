rsync -av                                   \
    --exclude "*.pb"                        \
    --exclude "*.jpg"                       \
    --exclude "*.png"                       \
    --exclude "detector/models"             \
    --exclude "Despat"                      \
    --exclude "*Playground"                 \
    * bollogg-local:/home/volzotan/despat/