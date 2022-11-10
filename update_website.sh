#!/bin/bash
set -ex

ant website

# Another dir with the website branch checked out
cp -R docs/website/* ../neuroConstruct_website/docs/
