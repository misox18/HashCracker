package hasher;

import org.apache.commons.codec.digest.DigestUtils;

public class SHA1Hasher implements Hasher {

	private static final SHA1Hasher INSTANCE = new SHA1Hasher();
	
	private SHA1Hasher() {};
	
	public static SHA1Hasher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String getHash(String input) {
		return DigestUtils.sha1Hex(input);
	}
}