package hasher;
import org.apache.commons.codec.digest.DigestUtils;

public class MD5Hasher implements Hasher {

	private static final MD5Hasher INSTANCE = new MD5Hasher();
	
	private MD5Hasher() {};
	
	public static MD5Hasher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String getHash(String input) {
		return DigestUtils.md5Hex(input);
	}
}