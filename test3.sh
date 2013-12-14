rm -f lan?  hout?  hin? rout?
java Host 0 0 sender 20 20&
java Router 0 0 1 &
java Router 1 1 2 &
java Controller host 0 1 router 0 1 lan 0 1 2&
