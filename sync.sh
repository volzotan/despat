rsync -av                                   \
    --exclude "*.pb"                        \
    --exclude "detector/models"             \
    --exclude "Despat"                      \
    --exclude "*Playground"                 \
    --exclude "detection"                   \
    * bollogg-local:/home/volzotan/despat/