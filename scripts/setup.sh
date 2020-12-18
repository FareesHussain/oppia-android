#!/bin/sh

# INSTRUCTIONS:
# This script will move the pre-push hook from script folder to
# the .git/hooks folder
#
# Run the script from the oppia-android root folder:
#
#   bash scripts/setup.sh
#
# NOTE: this script should be run once after the initial codebase setup

# Download ktlint
KTLINT="0.37.0"
echo Using Ktlint $KTLINT
cp scripts/pre-push.sh .git/hooks/pre-push
cd ..
mkdir -p oppia-android-tools
cd oppia-android-tools
curl -sSL https://github.com/pinterest/ktlint/releases/download/$KTLINT/ktlint > ktlintt
chmod a+x ktlintt
echo Ktlint file downloaded
bash ktlintt --version
# Move file from script folder to .git/hooks folder
#cp scripts/pre-push.sh .git/hooks/pre-push
