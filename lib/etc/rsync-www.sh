#!/bin/sh

if [ ! -e ~/.wab-passwd ]; then
  echo "Configuration file ~/.wab-passwd not found"
  exit
fi

if [ -d doc/www ]; then
  sed `cat ~/.wab-passwd` etc/acra.php.in > doc/www/acra.php
  cd doc/www
  chmod o-r acra.php
  rsync -aiv ./ apuder,webappbooster@web.sourceforge.net:/home/project-web/webappbooster/htdocs/
fi

