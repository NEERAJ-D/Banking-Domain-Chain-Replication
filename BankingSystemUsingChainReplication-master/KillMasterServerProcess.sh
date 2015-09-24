#/bin/bash
ps -eaf | grep "java server.ServerProcess" | cut -d ' ' -f 5  | xargs kill -9
ps -eaf | grep "java master.MasterProcess" | cut -d ' ' -f 5  | xargs kill -9
ps -eaf | grep "java client.ClientProcess" | cut -d ' ' -f 5  | xargs kill -9
