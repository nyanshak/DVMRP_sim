import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Host {
	
	static boolean debug = false;
	
	public static void writeToFile(File file, String str) {
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
		System.err.println("Usage: java Host <host-id> <lan-id> <type> [<time-to-start> <period>]");
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
	
	public static void receiver(int host, int lan){
		File file = new File("hout" + host);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
		
		File lanFile = new File("lan" + lan);
		if (!lanFile.exists()) {
			try {
				lanFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
		
		File hin = new File("hin" + host);
		if (!hin.exists()) {
			try {
				hin.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(lanFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(2);
		}
		
		writeToFile(file, "receiver " + lan);
		for (int i = 1; i <= 100; i++) {
			if (i % 10 == 0) {
				writeToFile(file, "receiver " + lan);
			}
			String line;
			try {
				while((line = br.readLine()) != null) {
					if (line.matches("^data " + lan + " " + "[0-9]+")){
						writeToFile(hin, line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
			waitSecond(1);
		}
		try {
			br.close();
		} catch (IOException e) {
		}
	}
	
	public static void sender(int host, int lan, int tts, int period) {
		File file = new File("hout" + host);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
		
		waitSecond(tts);
		for (int i = 0; i < 100 - tts; i++) {
			if (i % period == 0) {
				writeToFile(file, "data " + lan + " " + lan);
			}
			waitSecond(1);
		}
	}
	
	public static void main(String[] args) {
		String type = "";
		int host = -1, lan = -1, tts = -1, period = 0;
		if (args.length == 3) {
			type = args[2];
			if (!type.equalsIgnoreCase("receiver")){
				usage();
			}
			host = Integer.parseInt(args[0]);
			lan = Integer.parseInt(args[1]);
			
			if (host < 0 || host > 9 || lan < 0 || lan > 9) {
				System.err.println("Error with bounds on arguments");
				System.exit(2);
			}
		} else if (args.length == 5) {
			type = args[2];
			if (!type.equalsIgnoreCase("sender")){
				usage();
			}
			host = Integer.parseInt(args[0]);
			lan = Integer.parseInt(args[1]);
			tts = Integer.parseInt(args[3]);
			period = Integer.parseInt(args[4]);
			if (host < 0 || host > 9 || lan < 0 || lan > 9 || tts < 0 || tts >= 100 || period < 10) {
				System.err.println("Error with bounds on arguments");
				System.exit(2);
			}
		} else {
			usage();
		}
		
		if (type.equalsIgnoreCase("receiver")) {
			receiver(host, lan);
		} else {
			sender(host, lan, tts, period);
		}
		
	}
}
