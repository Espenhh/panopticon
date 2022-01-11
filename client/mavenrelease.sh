#!/usr/bin/env bash

mvn release:prepare && mvn release:perform -Darguments="-DskipTests"
