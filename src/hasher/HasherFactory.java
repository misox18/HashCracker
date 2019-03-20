package hasher;

import Enums.HasherType;

public class HasherFactory {
	
	/**
	 * Get hasher based on hasher type
	 * @param type
	 * @return
	 */
	public static Hasher getHasher(HasherType type) {
		switch (type) {
			case MD5: 		return MD5Hasher.getInstance();
			case SHA1: 		return SHA1Hasher.getInstance();
			case SHA256: 	return SHA256Hasher.getInstance();
			case SHA384: 	return SHA384Hasher.getInstance();
			case SHA512: 	return SHA512Hasher.getInstance();
			default: throw new UnsupportedOperationException("Cannot get Hasher from type: " + type.name());
		}
	}
	
	/**
	 * Get hasher based on given hash
	 * @param hash
	 * @return
	 */
	public static Hasher getHasher(String hash) {
		switch (hash.length()) {
			case 32: return MD5Hasher.getInstance();
			case 40: return SHA1Hasher.getInstance();
			case 64: return SHA256Hasher.getInstance();
			case 96: return SHA384Hasher.getInstance();
			case 128: return SHA512Hasher.getInstance();
			default: throw new UnsupportedOperationException("Cannot get Hasher from hash with length: " + hash.length());
		}
	}
}