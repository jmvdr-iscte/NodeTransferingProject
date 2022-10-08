import java.util.ArrayList;
import java.util.List;


public class Countdownlatch {
	int countdown;
	List<CloudByte> cb;

	public Countdownlatch(int countdown) {
		this.countdown = countdown;

		cb = new ArrayList<>();

	}

	public synchronized CloudByte await() {
		while (countdown > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return cb.get(cb.size() - 1);
	}

	public synchronized void Countdown(CloudByte b) {
		if (cb.contains(b) && countdown > 0) {
			cb.add(b);
			countdown -= 2;
			notifyAll();
		} else if (countdown > 0) {
			cb.add(b);
			
		}

	}
}