package cracker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hasher.Hasher;
import hasher.HasherFactory;
import Enums.HasherType;

public class BruteForceHashCracker {
	
	private static final BruteForceHashCracker INSTANCE = new BruteForceHashCracker();
	public List<ExecutorService> executors = new ArrayList<>();
	
	private BruteForceHashCracker() {};
	
	public static BruteForceHashCracker getInstance( ) {
		return INSTANCE;
	}
	
	/**
	 * Crack given hash. The type of hash will be determined by the hash length.
	 *
	 * @param hash Hash to crack
	 * @param supportedChars chars used to cracking
	 * @param maxLength max length of hashed value
	 * @param expectedStart expected start of hashed value
	 * @param expectedEnd expected end of hashed value
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public Optional<String> crackHash(String hash, List<Character> supportedChars, int maxLength
			, String expectedStart, String expectedEnd) throws InterruptedException, ExecutionException {
		return crackHash(hash, HasherFactory.getHasher(hash), supportedChars, maxLength, expectedStart, expectedEnd);
	}
	
	/**
	 * Crack given hash.
	 *
	 * @param hash Hash to crack
	 * @param hasherType Hasher type defining the hasher to use to crack the hash
	 * @param supportedChars chars used to cracking
	 * @param maxLength max length of hashed value
	 * @param expectedStart expected start of hashed value
	 * @param expectedEnd expected end of hashed value
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public Optional<String> crackHash(String hash, HasherType hasherType, List<Character> supportedChars, int maxLength
			, String expectedStart, String expectedEnd) throws InterruptedException, ExecutionException {
		return crackHash(hash, HasherFactory.getHasher(hasherType) , supportedChars, maxLength, expectedStart, expectedEnd);
	}
	
	/**
	 * Crack given hash.
	 *
	 * @param hash Hash to crack
	 * @param hasher Hasher to use to crack the hash
	 * @param supportedChars chars used to cracking
	 * @param maxLength max length of hashed value
	 * @param expectedStart expected start of hashed value
	 * @param expectedEnd expected end of hashed value
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Optional<String> crackHash(String hash, Hasher hasher, List<Character> supportedChars, int maxLength
			, String expectedStart, String expectedEnd) throws InterruptedException, ExecutionException {

		expectedStart = expectedStart == null ? "" : expectedStart;
		expectedEnd = expectedEnd == null ? "" : expectedEnd;
		
		//check if cracked hash isn't expected start/end/start+end
		if (!expectedStart.isEmpty() && !expectedEnd.isEmpty() && hash.equalsIgnoreCase(hasher.getHash(expectedStart + expectedEnd))) {
			return Optional.of(expectedStart + expectedEnd);
		} else if (!expectedStart.isEmpty() && hash.equalsIgnoreCase(hasher.getHash(expectedStart))) {
			return Optional.of(expectedStart);
		} else if (!expectedEnd.isEmpty() && hash.equalsIgnoreCase(hasher.getHash(expectedEnd))) {
			return Optional.of(expectedEnd);
		}
		
		//thread count - number of threads to start, it is the max combination length needed for hash cracking
		//e.g. if expected start is "aa" and expected end is "ee" and hashed string was aa123ee
		//then thread count will be 3 - only the middle combinations will be computed
		int threadCount= expectedStart.length() + expectedEnd.length();
		
		//create thread pool
		ExecutorService executor = Executors.newFixedThreadPool(maxLength - threadCount);
		executors.add(executor);
		Set<CallableHashCrack> hashCrackers = new HashSet<>();
		Optional<String> crackedHash;
		
		//create callable hash crackers collection, each cracker for one combinations length
		for (int i = 1 ; i <= (maxLength - threadCount) ; i++) {
			hashCrackers.add(new CallableHashCrack(hash, hasher, i, supportedChars, expectedStart, expectedEnd));
		}
		
		//execute and wait for one successfully executed hash cracker and get its return value
		//if no thread executes successfully (doesn't return value) it throws  execution exception
		try {
			crackedHash = executor.invokeAny(hashCrackers);
		} catch(ExecutionException ex) {
			crackedHash = Optional.empty();
		}
		
		//end all running threads - if one return and other ones are still running.
		shutDownExecutorService(executor);
		executors.remove(executor);
		
		return crackedHash;
	}
	
	private void shutDownExecutorService(ExecutorService service) {
		System.out.println("Shutting down threads");
		service.shutdown();
		try {
		    if (!service.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		    	service.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    service.shutdownNow();
		}
	}
	
	private class CallableHashCrack implements Callable<Optional<String>> {
		
		private String hash;
		private Hasher hasher;
		private int length;
		private List<Character> feedChars;
		private String expectedStart;
		private String expectedEnd;
		
		private CallableHashCrack(String hash, Hasher hasher, int length, List<Character> feedChars, String expectedStart, String expectedEnd) {
			this.hash = hash;
			this.hasher = hasher;
			this.length = length;
			this.feedChars = feedChars;
			this.expectedStart = expectedStart;
			this.expectedEnd = expectedEnd;
		}
		
		/**
		 * This method will be executed in thread.
		 */
		@Override
		public Optional<String> call() throws Exception {
			return crackHash(hash, hasher, length, feedChars, expectedStart, expectedEnd);
		}
		
		/**
		 * Compute combinations of given characters with given length and try to crack the hash for each one.
		 *
		 * @param hash hash to crack
		 * @param hasher hasher to crack the hash
		 * @param length length of combinations
		 * @param feedChars chars which will be combined
		 * @return List of combinations
		 * @throws Exception
		 */
		private Optional<String> crackHash(String hash, Hasher hasher, int length, List<Character> feedChars, String expectedStart, String expectedEnd) throws Exception {
			int printLength = length + expectedStart.length() + expectedEnd.length();
			System.out.println("Starting hash cracking thread for length: " + printLength);
			
			// array of integers with length of combination.
			//it represents the combination, where the array element index is position of character in combination string and the value is
			//index of character in feedChars
		    int[] indices = new int[length];
		    int carry;
		    StringBuilder sb = new StringBuilder();
		    int sbResetIndex = 0;
		    
			//set string builders reset index based on expectedStart length
		    //set string builders start to expectedStart 
			if (!expectedStart.isEmpty()) {
				sbResetIndex = expectedStart.length();
				sb.append(expectedStart);
			}
		    
		    do {
		    	//if thread is interrupted - return null. Interruption can be caused by stopping
		    	//threads when hash was cracked in another thread
		    	if (Thread.currentThread().isInterrupted()) {
		    		System.out.println("Hash cracking thread for length: " + printLength + " shut down.");
		    		return null;
		    	}
		    	
		    	//concatenate chars from feedChars based on indexes from indices into current combination.
		        for(int index : indices) {
		        	sb.append(feedChars.get(index));
		        }
		        
		        //add expectedEnd to the current combination
		        sb.append(expectedEnd);
		        
		        //compute hash and check if current combination equals
		        if (hash.equalsIgnoreCase(hasher.getHash(sb.toString()))) {
		        	return Optional.of(sb.toString());
		        }
		        
		        //reset string builder, only expectedStart string will remain in it
		        sb.setLength(sbResetIndex);

		        //Iterate through indices, from end to the beginning.
		        carry = 1;
		        for(int i = indices.length - 1; i >= 0; i--) {
		        	//if in the previous iteration the value after addition was equal to the feedChars, 
		        	//don't break but increment the current value.
		            if (carry == 0)
		                break;

		            //add +1 to the current indices value and set carry to 0
		            indices[i] += carry;
		            carry = 0;

		            //if current indices index value is after addition of 1 equal to the feedChars size (Out of bounds)
		            //set carry to 1 (so it won't break in next iteration but go to the previous indices index) and current indices index value to 0
		            if (indices[i] == feedChars.size())
		            {
		                carry = 1;
		                indices[i] = 0;
		            }
		        }
		    } while(carry != 1); // Call this method iteratively until a carry is left over
		    
		    System.out.println("Cracking hash thread for length: " + printLength + " didn't find match");
		    //due to invokeAny method - if unsuccessful don't return but throw
		    throw new Exception();
		}
	}
}