package cracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import hasher.Hasher;
import hasher.HasherFactory;

public class DictionaryHashCracker {
	
	private static final DictionaryHashCracker INSTANCE = new DictionaryHashCracker();
	
	private DictionaryHashCracker() {};
	
	public static DictionaryHashCracker getInstance( ) {
		return INSTANCE;
	}

	public Optional<String> crackHash(String hash, File dictionary) throws FileNotFoundException, IOException {
		Hasher hasher = HasherFactory.getHasher(hash);
		String line;
		
		try (BufferedReader br = new BufferedReader(new FileReader(dictionary))) {
			while ((line = br.readLine()) != null) {
				if (hasher.getHash(line).equalsIgnoreCase(hash)) {
					return Optional.of(line);
				}
			}
		}		
		return Optional.empty();
	}
}