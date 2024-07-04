.read create_EQ_table.sql
.mode tabs
.import --skip 1 "|sed 's/^/'$RUNID'\t/' < $EQTSV" EQ
