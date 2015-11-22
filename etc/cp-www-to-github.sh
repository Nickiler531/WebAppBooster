#!/bin/sh

if [ -d doc/www ]; then
  cd doc/www
  cp -r * ../../../WebAppBooster-www/
fi

