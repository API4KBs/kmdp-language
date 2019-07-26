#!/bin/bash
set -x #echo on

##################################################
#### Verify

if [ "$#" -ne 4 ]; then
    echo "Usage ./release.sh {release_version} {server_version} {next_version} {next_server_version}"
    exit 2
fi

# Ensure to be on the develop branch
git checkout develop

##################################################
#### Release

# Prepare release branch
git checkout -b "rel_$1"

# Apply all necesssary version changes/fixtures
mvn versions:set -DnewVersion=$1

## cannot set parent version to a range with maven 3.3.9 / maven versions 2.7
## mvn versions:update-parent -DparentVersion=$1
sed -i -r "/<parent>/,/<\/parent>/ s|<version>(.*)</version>|<version>$2</version> |" pom.xml

# Ensure it builds!
mvn clean install -Prelease #-Dmaven.local.repo=../repo
if [[ "$?" -ne 0 ]] ; then
  mvn versions:revert
  git checkout develop
  git branch -d "rel_$1"
  echo 'release failed';
  exit -1
fi


# Commit changes
git commit -am "Candidate release $1"

##################################################
#### Rebase

# Rebase to ensure continuity
git checkout develop
git rebase "rel_$1"


##################################################
#### Move on

# Revert all necesssary version changes/fixtures
mvn versions:set -DnewVersion=$3

## cannot set parent version to a range with maven 3.3.9 / maven versions 2.7
sed -i -r "/<parent>/,/<\/parent>/ s|<version>([0-9]+\.[0-9]+\.[0-9]+)</version>|<version>[$2,$4]</version> |" pom.xml


# Commit changes
git commit -am "Start version $3+"