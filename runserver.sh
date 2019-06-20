#!/bin/sh

cd build
java -Djava.rmi.server.hostname=[2a02:8108:96bf:a95c:dc9d:33ac:b959:d97f] chAT.server.Server default --makeonetimekey
cd ..
