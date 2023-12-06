#!/bin/sh

jq -r '.[] | { "class": .attachments[] | select(.name == "classQualifiedName").value } + ([.classification[] | { "key": .compatibility, "value": .severity }] | from_entries) | .class + "\t" + (.SOURCE // "-") + "\t" + (.BINARY // "-") + "\t" + (.SEMANTIC // "-")'
