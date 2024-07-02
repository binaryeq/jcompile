#!/bin/sh

# Suitable to run after Ctrl-C-ing a docker-build-project.sh run (or make run that calls it).

JCOMPILE_ROOT=$(git rev-parse --show-toplevel)

for c in $( docker ps -q --filter 'name=jdk-' ); do
	echo "Stopping and removing docker container $c..."
	docker stop $c
	docker rm $c
done

for d in $( cd worktrees && ls ); do
	foo=${d#pid*-}
	proj=${foo%-*.*.*}
	fullpath=$(realpath worktrees/$d)
	echo "Removing worktree $fullpath from project $proj..."
	git -C $JCOMPILE_ROOT/dataset/$proj worktree remove -f "$fullpath"
done
