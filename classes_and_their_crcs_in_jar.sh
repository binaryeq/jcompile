#!/bin/sh
unzip -lv "$1" |perl -lne 'print if s/^.*202\d-\d\d-\d\d \d\d:\d\d (\S+)\s+(.*\.class)$/$2\t$1/' | sort
