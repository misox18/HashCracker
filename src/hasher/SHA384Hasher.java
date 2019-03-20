package hasher;

import org.apache.commons.codec.digest.DigestUtils;

public class SHA384Hasher implements Hasher {

	private static final SHA384Hasher INSTANCE = new SHA384Hasher();
	
	private SHA384Hasher() {};
	
	public static SHA384Hasher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String getHash(String input) {
		return DigestUtils.sha384Hex(input);
	}
}