import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class Router {
static boolean debug = false;
	
	public static void writeToFile(String fname, String str) {
		File file = new File(fname);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
		
		try (FileWriter fw = new FileWriter(file, true)) {
			if (debug) {
				System.out.println("Appending <" + str + "> to <" + file.getPath() + ">");
			}
			fw.append(str).append("\n");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void usage () {
		System.err.println("Usage: java Router <lan> ...");
		System.exit(2);
	}
	
	public static void waitSecond(long t) {
		try {
			TimeUnit.SECONDS.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	public static int getNumberFromString(String s) {
		int answer = -1;
		try {
			answer = Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			System.exit(2);
		}
		return answer;
	}
	
	public static void main(String[] args) {
		
		if (args.length < 2 || args.length > 11) {
			usage();
		}
		BufferedReader[] readerList = new BufferedReader[args.length-1];
		
		int routerId = getNumberFromString(args[0]);
		RoutingInfo[] routingTable = new RoutingInfo[10];
		for (int i = 0; i < routingTable.length; i++) {
			routingTable[i] = new RoutingInfo(routerId);
		}
		
		
		if (routerId < 0 || routerId > 9) {
			System.err.println("Router ID must be between 0 and 9");
			System.exit(2);
		}
		for (int i = 1; i < args.length; i++){
			int temp = getNumberFromString(args[i]);
			if (temp < 0 || temp > 9) {
				System.err.println("LANs must be between 0 and 9");
				System.exit(2);
			} else {
				routingTable[temp].setHopCount(0);
				routingTable[temp].nextHop = routerId;
				
				File file = new File("lan" + args[i]);
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(2);
					}
				}
				readerList[i-1] = null;
				try {
					readerList[i-1] = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(2);
				}
			}
		}
		String fname = "rout" + routerId;
		LinkedList<String> ignoreQueue = new LinkedList<>();
		
		for (int i = 0; i < 100; i++) {
			LinkedList<String> endQueue = new LinkedList<>();
			
			for (int k = 0; k < routingTable.length; k++) {
				// decide whether to send nmr
				boolean nmr = true;
				if (routingTable[k].hopCount == 10) {
					continue;
				}
				
				if (routingTable[k].hopCount == 0) {
					if (routingTable[k].childLans.size() == 0) {
						if (routingTable[k].nmrTimer == 0) {
							endQueue.add("NMR " + k + " " + routerId + " " + k);
							
						}
					}
				} else if (routingTable[k].childLans.size() == 0) {
					if (routingTable[k].nmrTimer == 0) {
						writeToFile(fname, "NMR " + routingTable[k].nextLan + " " + routerId + " " + k);
						ignoreQueue.add("NMR " + routingTable[k].nextLan + " " + routerId + " " + k);
					}
				} else {
					
					if (routingTable[k].receiver > 0) {
						routingTable[k].receiver--;
					}
					
					if (routingTable[k].receiver > 0) {
						nmr = false;
					}
					
					Iterator<AttachedRouter> itr = routingTable[k].routersAttached.iterator();
					
					ArrayList<AttachedRouter> routersAttached = new ArrayList<>();
					while(itr.hasNext()) {
						AttachedRouter r = itr.next();
						if (r.nmr > 0) {
							r.nmr--;
						}
						
						if (r.nmr == 0) {
							//System.out.println("R:" + r.nmr);
							nmr = false;
						}
						routersAttached.add(r);
					}
					routingTable[k].routersAttached = routersAttached;
					
					if (nmr) {
						if (routingTable[k].nmrTimer == 0) {
							writeToFile(fname, "NMR " + routingTable[k].nextLan + " " + routerId + " " + k);
							ignoreQueue.add("NMR " + routingTable[k].nextLan + " " + routerId + " " + k);
						}
					}
					
					
				}
				if (routingTable[k].nmrTimer == 0) {
					if (nmr) {
						routingTable[k].nmrTimer = 10;
					}
				} else {
					routingTable[k].nmrTimer--;
				}
				
			}
			
			
			for (int j = 0; j < readerList.length; j++) {
				String line;
				try {
					while((line = readerList[j].readLine()) != null) {
						String[] temp = line.split(" ");
						
						if (ignoreQueue.remove(line)) {
						} else if (line.matches("^receiver [0-9]$")){
							int lanNum = getNumberFromString(temp[1]);
							routingTable[lanNum].receiver = 20;
						} else if (line.matches("^data [0-9] [0-9]$")) {
							int lanNum = getNumberFromString(temp[1]);
							int hostLan = getNumberFromString(temp[2]);
							
							for (int z = 0; z < routingTable.length; z++) {
								if (z != lanNum && z != hostLan && routingTable[z].nextHop == routerId) {
									if (routingTable[z].receiver > 0) {
										writeToFile(fname, "data " + z + " " + hostLan);
										ignoreQueue.add("data " + z + " " + hostLan);
									} else {
										Iterator<AttachedRouter> itr = routingTable[z].routersAttached.iterator();
										while (itr.hasNext()) {
											int[] al = itr.next().attachedLans;
											for (int x = 0; x < al.length; x++) {
												if (al[x] == 0) {
													writeToFile(fname, "data " + z + " " + hostLan);
													ignoreQueue.add("data " + z + " " + hostLan);
													break;
												}
											}
										}
									}
								}
															}
						} else if (line.matches("^DV [0-9] [0-9] .*")) {
							int lanNum = getNumberFromString(temp[1]), routerNum = getNumberFromString(temp[2]);
							
							for (int q = 0; q < 10; q++) {
								int index = 3+2*q;
								int val = getNumberFromString(temp[index]), rt = getNumberFromString(temp[index+1]);
								
								if(q == lanNum && val == 0) {
									AttachedRouter r = new AttachedRouter(rt);
									if (!routingTable[q].routersAttached.contains(r)) {
									//	routingTable[q].routersAttached.add(r);
									}
								}
								
								if (routingTable[q].hopCount == 10) {
									if (val < 9) {

										routingTable[q].hopCount = val + 1;
										routingTable[q].nextHop = routerNum;
										routingTable[q].nextLan = lanNum;
										//System.out.println("CHANGE1");
										
										if (!routingTable[q].childLans.contains(lanNum)) {
											routingTable[q].childLans.add(lanNum);
										}
										
										if (val == 0) {
											AttachedRouter r = new AttachedRouter(rt);
											if (!routingTable[q].routersAttached.contains(r)) {
												routingTable[q].routersAttached.add(r);
											}
										}
										
									}
									continue;
								}
								
								if (routingTable[q].hopCount == 0) {
									if (rt == routerId) {
										if (!routingTable[q].childLans.contains(lanNum)) {
											routingTable[q].childLans.add(lanNum);
										}
									}
								} else if (routingTable[q].hopCount < val + 1 || (routingTable[q].hopCount == val+1 && routingTable[q].nextHop < routerNum)) {
									if ((routingTable[q].hopCount + 1 == val && rt < routerId) || (routingTable[q].hopCount == val && routerNum > routerId) || rt == routerId) {
										if (!routingTable[q].childLans.contains(lanNum)) {
											routingTable[q].childLans.add(lanNum);
										}
									}		
								} else {
									if (rt == routerId) {
										if (!routingTable[q].childLans.contains(lanNum)) {
											routingTable[q].childLans.add(lanNum);
										}
									} else {
										routingTable[q].hopCount = val + 1;
										routingTable[q].nextHop = routerNum;
										routingTable[q].nextLan = lanNum;
										
										//System.out.println("CHANGE");
									}
								}
									
									
								
							}
								
						} else if (line.matches("^NMR [0-9] [0-9] [0-9]$")) {
							int lanNum = getNumberFromString(temp[1]), routerNum = getNumberFromString(temp[2]), hostLanId = getNumberFromString(temp[3]);
							int xxyz = routingTable[hostLanId].routersAttached.lastIndexOf(routerNum);
							if (xxyz == -1) {
								AttachedRouter ar = new AttachedRouter(routerNum);
								ar.nmr = 20;
								routingTable[hostLanId].routersAttached.add(ar);
							} else {
								routingTable[hostLanId].routersAttached.get(routingTable[hostLanId].routersAttached.lastIndexOf(routerNum)).nmr = 20;
							}
						} else {
							System.err.println("UNEXPECTED INPUT to R" + routerId + ": <" + line + ">");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(2);
				}
				
				while (!endQueue.isEmpty()) {
					String str = endQueue.removeFirst();
					writeToFile(fname, str);
					ignoreQueue.add(str);
				}
			}
			
			
			if (i % 5 == 0) {
				for (int k = 0; k < routingTable.length; k++) {
					if (routingTable[k].getHopCount() == 0) {

						String dvstr = "DV " + k + " " + routerId;
						for (int m = 0; m < routingTable.length; m++) {
							if (routingTable[m].hopCount != 10) {
								dvstr += " " + routingTable[m].hopCount + " " + routingTable[m].nextHop;
							} else {
								dvstr += " 10 10";
							}
						}
						
						writeToFile(fname, dvstr);
						ignoreQueue.add(dvstr);
					}
					
				}
			}
			
			waitSecond(1);
			
		}
		
	}
}
