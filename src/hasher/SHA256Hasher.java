package hasher;

import org.apache.commons.codec.digest.DigestUtils;

public class SHA256Hasher implements Hasher {

	private static final SHA256Hasher INSTANCE = new SHA256Hasher();
	
	private SHA256Hasher() {};
	
	public static SHA256Hasher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String getHash(String input) {
		return DigestUtils.sha256Hex(input);
	}
}