#!/bin/sh

if [ -d doc/www ]; then
  cd doc/www
  chmod o-r acra.php
  rsync -aiv ./ apuder,webappbooster@web.sourceforge.net:/home/project-web/webappbooster/htdocs/
fi

