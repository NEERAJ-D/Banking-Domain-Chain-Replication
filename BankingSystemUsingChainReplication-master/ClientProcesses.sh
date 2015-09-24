# Launch client processes simultaneously
java client.ClientProcess c1 $1 & 
java client.ClientProcess c2 $1 &
java client.ClientProcess c3 $1 &
