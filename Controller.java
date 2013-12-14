import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


public class Controller {
	
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
		System.err.println("Usage: java Controller host <id>* router <id>* lan <id>*");
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
		
		Vector<File> inputVector = new Vector<>();
		ArrayList<Integer> lanList = new ArrayList<>();
		String mode = "";
		for (int i = 0; i < args.length; i++){
			String temp = args[i];
			if (temp.equalsIgnoreCase("host") || temp.equalsIgnoreCase("router") || temp.equalsIgnoreCase("lan")) {
				mode = temp;
			} else if (mode.equals("")) {
				usage();
			} else if (mode.equals("host")) {
				
				int n = getNumberFromString(temp);
				if (n < 0 || n > 9) {
					System.err.println("Host <id> must be a number between 0 and 9");
					System.exit(2);
				}
				
				File file = new File("hout" + n);
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(2);
					}
				}
				inputVector.add(file);
				
			} else if (mode.equals("router")) {
				
				int n = getNumberFromString(temp);
				if (n < 0 || n > 9) {
					System.err.println("Router <id> must be a number between 0 and 9");
					System.exit(2);
				}
				
				File file = new File("rout" + n);
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(2);
					}
				}
				inputVector.add(file);
				
			} else if (mode.equals("lan")) {
				
				int n = getNumberFromString(temp);
				if (n < 0 || n > 9) {
					System.err.println("LAN <id> must be a number between 0 and 9");
					System.exit(2);
				}
				
				lanList.add(n);
			}
		}
		
		Vector<BufferedReader> brVector = new Vector<>();
		
		for (int i = 0; i < inputVector.size(); i++) {
			BufferedReader temp = null;
			try {
				temp = new BufferedReader(new FileReader(inputVector.get(i)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(2);
			}
			brVector.add(temp);
		}
		
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < brVector.size(); j++) {
				String line;
				try {
					while((line = brVector.get(j).readLine()) != null) {
						if (line.matches("^receiver [0-9]$")){
							String[] temp = line.split(" ");
							int lanNum = getNumberFromString(temp[1]);
							if (lanList.contains(lanNum)) {
								writeToFile("lan" + lanNum, line);
							} else {
								System.err.println("LAN number from input not expected based on parameters to Controller");
								System.exit(2);
							}
						} else if (line.matches("^data [0-9] [0-9]$")) {
							String[] temp = line.split(" ");
							int lanNum = getNumberFromString(temp[1]);
							if (lanList.contains(lanNum)) {
								writeToFile("lan" + lanNum, line);
							} else {
								System.err.println("LAN number from input not expected based on parameters to Controller");
								System.exit(2);
							}
						} else if (line.matches("^DV [0-9] [0-9] .*")) {
							String[] temp = line.split(" ");
							int lanNum = getNumberFromString(temp[1]);
							if (lanList.contains(lanNum)) {
								writeToFile("lan" + lanNum, line);
							} else {
								System.err.println("LAN number from input not expected based on parameters to Controller");
								System.exit(2);
							}
						} else if (line.matches("^NMR [0-9] [0-9] [0-9]$")) {
							String[] temp = line.split(" ");
							int lanNum = getNumberFromString(temp[1]);
							if (lanList.contains(lanNum)) {
								writeToFile("lan" + lanNum, line);
							} else {
								System.err.println("LAN number from input not expected based on parameters to Controller");
								System.exit(2);
							}
						} else {
							System.err.println("UNEXPECTED INPUT to Controller: <" + line + ">");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(2);
				}
			}
			
			waitSecond(1);
		}
		
	}
}
