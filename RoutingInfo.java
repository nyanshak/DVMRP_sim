import java.util.ArrayList;

public class RoutingInfo {
	ArrayList<AttachedRouter> routersAttached = new ArrayList<>();
	int nextHop = -1, nextLan = -1, hopCount = 10, receiver = 0, nmrTimer = 0;
	ArrayList<Integer> childLans = new ArrayList<>();

	public int getHopCount() {
		return hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}
	
	public RoutingInfo(int h) {
		this.nextHop = h;
	}
	
	public int getNextHop() {
		return nextHop;
	}

	public void setNextHop(int nextHop) {
		this.nextHop = nextHop;
	}

}
