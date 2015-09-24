#/bin/bash

# Start Master Process
java master.MasterProcess $1 &


# Start the Servers for all the Three Banks
java server.ServerProcess s1 $1 &
java server.ServerProcess s2 $1 &
java server.ServerProcess s3 $1 &
java server.ServerProcess s4 $1 &
java server.ServerProcess s5 $1 &
