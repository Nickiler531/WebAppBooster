#!/bin/sh

if [ -d doc/www ]; then
  cd doc/www
  rsync -aiv ./ apuder,webappbooster@web.sourceforge.net:/home/project-web/webappbooster/htdocs/
fi

