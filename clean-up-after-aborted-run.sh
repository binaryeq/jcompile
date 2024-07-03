#!/bin/sh

# Suitable to run after Ctrl-C-ing a docker-build-project.sh run (or make run that calls it).

JCOMPILE_ROOT=$(git rev-parse --show-toplevel)

for c in $( docker ps -q --filter 'name=jdk-' ); do
	echo "Stopping and removing docker container $c..."
	docker stop $c
	docker rm $c
done

for d in worktrees/*; do
	echo "Removing worktree $d..."
	git -C $d worktree remove -f .		# Nice that this works
done
