#!/bin/bash
set -e	# exit script on any failed command

MANIFEST=manifest.mf
VERSION=`grep ^OpenIDE-Module-Specification-Version: < $MANIFEST | cut -d " " -f2-`

# make sure we have no uncommited changes
if [ -n "$(git status --porcelain)" ]; then
	echo "You have uncommited changes, you dirty little girl!"
	git status --porcelain
	exit 1;
fi

# build .nbm
ant clean test nbm

# prepare for next version
echo "========================================================================="
echo "Current version will be tagged as \"$VERSION\"."
read -p "What it the next version? " NEXT_VERSION
git tag "$VERSION"
sed "s/^\(OpenIDE-Module-Specification-Version:\) .*/\1 $NEXT_VERSION/"  < $MANIFEST > $MANIFEST.next
rm $MANIFEST
mv $MANIFEST.next $MANIFEST
git add $MANIFEST
git commit -m "Released $VERSION. Started work on $NEXT_VERSION"

echo "Another successful build!"
echo "Now push the changes, remember the tags..."
