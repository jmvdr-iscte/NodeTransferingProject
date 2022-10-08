import java.io.Serializable;
import java.util.List;

public class ByteBlockRequest implements Serializable {
	int startIndex;
	int length;


	public ByteBlockRequest(int startIndex, int length) {
		if (startIndex < 0 || length > 10000000) {
			throw new IllegalArgumentException("Invalid value for CloudByte");
		}

		this.startIndex = startIndex;
		this.length = length;

	}

	public int GetLength() {
		return this.length;
	}

	public int Getstartindex() {
		return this.startIndex;
	}

}