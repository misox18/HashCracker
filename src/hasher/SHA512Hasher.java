package hasher;

import org.apache.commons.codec.digest.DigestUtils;

public class SHA512Hasher implements Hasher {

	private static final SHA512Hasher INSTANCE = new SHA512Hasher();
	
	private SHA512Hasher() {};
	
	public static SHA512Hasher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String getHash(String input) {
		return DigestUtils.sha512Hex(input);
	}
}