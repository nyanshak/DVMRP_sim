import java.util.LinkedList;

public class AttachedRouter {
	int routerId;
	int nmr = 0;
	int[] attachedLans = new int[10];
	public AttachedRouter(int n){
		routerId = n;
		for (int i = 0; i < attachedLans.length; i++) {
			attachedLans[i] = -1;
		}
	}
}
