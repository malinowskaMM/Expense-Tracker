#!/bin/bash
mvn clean install -DskipTests=true dockerfile:build dockerfile:push
