rm -f lan?  hout?  hin? rout?
java Router 0 0 1 &
java Router 1 1 2 &
java Router 2 2 3 &
java Router 3 3 0 &
java Controller host router 0 1 2 3 lan 0 1 2 3&
