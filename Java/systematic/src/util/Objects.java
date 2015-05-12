package util;

import static java.util.Arrays.*;
import static mail.Email.*;
import static util.Errors.*;
import static util.Strings.*;

import java.io.*;
import java.lang.Thread.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import mail.*;
public class Objects {

	public static <T> ThreadLocal<T> local() {
		return new ThreadLocal<T>();
	}
	
	public static <T> ThreadLocal<T> local(final T init) {
		ThreadLocal<T> result = new ThreadLocal<T>() {
		    @Override protected T initialValue() {
		        return init;
		    }
		};
		return result;
	}
	
	/**
	 *  creates an array using varargs. cleaner than new Object[] { }, but 
	 *  does NOT work when the Ts have a generic type parameter in the class 
	 *  signature, due to an obscure implication of the generics implementation 
	 *  in java.
	 */
	public static <T> T[] array(T ... ts) {
		return ts;
	}

	public static <T> T the(T ... ts) {
		return the(list(ts));
	}
	
	public static <T> T theOrNull(Collection<T> ts) {
		if(ts.isEmpty())
			return null;
		return the(ts);
	}

	public static <T> T the(Collection<T> ts) {
		bombNull(ts, "null list in the");
		Iterator<T> i = ts.iterator();
		bombUnless(i.hasNext(), "empty list passed to the");
		T result = i.next();
		if(i.hasNext())
		    bomb("tried to take the of multiple elements in " + ts);
		return result;
	}

	public static <T, C extends Collection<T>> C nonEmpty(C ts) {
		bombNull(ts, "null passed to nonEmpty!");
		bombIf(ts.isEmpty(), "empty passed to nonempty");
		return ts;
	}
	
	public static <T, C extends Collection<T>> C nonEmpty(C ts, String message) {
		bombNull(ts, "null passed to nonEmpty: " + message);
		bombIf(ts.isEmpty(), "empty passed to nonempty: " + message);
		return ts;
	}
	
	public static void join(List<Thread> threads) {
	    for(Thread t : threads) join(t);
	}
	
	public static <T> List<List<T>> emptySynchronized() {
	    return new ArrayList<List<T>>();
	}
	
    public static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw bomb("girl, interrupted", e);
        }
    }

	public static <T> List<T> rest(T ... ts) {
		bombNull(ts, "null ts!");
		return rest(list(ts));
	}
	
	public static <T> T first(T ... ts) {
		bombNull(ts, "null ts!");
		return first(list(ts));
	}
	
	public static <T> T second(T ... ts) {
		bombNull(ts, "null ts!");
		return second(list(ts));
	}
	
	public static <T> T third(T ... ts) {
		bombNull(ts, "null ts!");
		return third(list(ts));
	}
	
	public static <T> T fourth(T ... ts) {
		bombNull(ts, "null ts!");
		return fourth(list(ts));
	}

	public static <T> T last(T ... ts) {
		bombNull(ts, "null ts!");
		return last(list(ts));
	}
	
	public static <T> T last(List<T> ts) {
		bombNull(ts, "null ts!");
		bombIf(ts.isEmpty(), "no last element on empty list!");
		return ts.get(ts.size() - 1);
	}
	
	public static <T> T penultimate(List<T> ts) {
		bombNull(ts, "null ts!");
		bombIf(ts.size() < 2, "no penultimate element on list  " + ts);
		return ts.get(ts.size() - 2);
	}
	
	public static <T> T first(Collection<T> ts) {
		bombNull(ts, "null ts!");
		bombIf(ts.isEmpty(), "no first element!");
		return ts.iterator().next();
	}
	
	public static <T> T second(Collection<T> ts) {
		return nth(ts, 2);
	}
	
	public static <T> T third(Collection<T> ts) {
		return nth(ts, 3);
	}
	
	public static <T> T fourth(Collection<T> ts) {
		return nth(ts, 4);
	}	
	
	public static <T> T fifth(Collection<T> ts) {
		return nth(ts, 5);
	}	
	
	public static <T> T sixth(Collection<T> ts) {
		return nth(ts, 6);
	}	
	
	public static <T> T seventh(Collection<T> ts) {
		return nth(ts, 7);
	}	
	
	public static <T> T eighth(Collection<T> ts) {
		return nth(ts, 8);
	}	
	
	public static <T> T ninth(Collection<T> ts) {
		return nth(ts, 9);
	}	
	
	public static <T> T tenth(Collection<T> ts) {
		return nth(ts, 10);
	}	
	
	public static <T> T eleventh(Collection<T> ts) {
		return nth(ts, 11);
	}	
	
	public static <T> T twelfth(Collection<T> ts) {
		return nth(ts, 12);
	}	
	
	public static <T> T thirteenth(Collection<T> ts) {
		return nth(ts, 13);
	}	
	
	public static <T> T fourteenth(Collection<T> ts) {
		return nth(ts, 14);
	}	
	
	public static <T> T fifteenth(Collection<T> ts) {
		return nth(ts, 15);
	}	
	
	public static <T> T sixteenth(Collection<T> ts) {
		return nth(ts, 16);
	}	
	
	public static <T> T seventeenth(Collection<T> ts) {
		return nth(ts, 17);
	}	
	
	public static <T> T eighteenth(Collection<T> ts) {
		return nth(ts, 18);
	}	
	
	public static <T> T nineteenth(Collection<T> ts) {
		return nth(ts, 19);
	}	
	
	public static <T> T twentieth(Collection<T> ts) {
		return nth(ts, 20);
	}	
	
	public static <T> T nth(Collection<T> ts, int n) {
		bombNull(ts, "null ts!");
		bombUnless(ts.size() >= n, "no element for " + n + " in " + ts);
		Iterator<T> it = ts.iterator();
		for(int i = 0; i < n - 1; i++) it.next();
		return it.next();
	}
	
	public static <T> List<T> rest(List<T> ts) {
		bombNull(ts, "null ts!");
		bombIf(ts.isEmpty(), "no elements to take the res of!");
		return ts.subList(1, ts.size());
	}
	
	public static <T> List<T> list(T ... ts) {
		return asList(ts);
	}

	public static <T> Set<T> set(T ... ts) {
		return new HashSet<T>(list(ts));
	}

	public static <T> Set<T> set(List<T> ts) {
		return new HashSet<T>(ts);
	}
	
	
	public static <K, V> Map<K, V> map(K k, V v) {
		Map<K, V> result = emptyMap();
		result.put(k, v);
		return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2) {
		Map<K, V> result = emptyMap();
		result.put(k, v);
		result.put(k2, v2);
		return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3) {
		Map<K, V> result = emptyMap();
		result.put(k, v);
		result.put(k2, v2);
		result.put(k3, v3);
		return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4) {
	    Map<K, V> result = map(k, v, k2, v2, k3, v3);
	    result.put(k4, v4);
	    return result;
	}	

	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
	    Map<K, V> result = map(k, v, k2, v2, k3, v3, k4, v4);
	    result.put(k5, v5);
	    return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
	    Map<K, V> result = map(k, v, k2, v2, k3, v3, k4, v4, k5, v5);
	    result.put(k6, v6);
	    return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
	    Map<K, V> result = map(k, v, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
	    result.put(k7, v7);
	    return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
	    Map<K, V> result = map(k, v, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
	    result.put(k8, v8);
	    return result;
	}	
	
	public static <K, V> Map<K, V> map(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
	    Map<K, V> result = map(k, v, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
	    result.put(k9, v9);
	    return result;
	}	
	
	public static <T> List<T> list(Collection<T> ts) {
		return new ArrayList<T>(ts);
	}
	
	public static <T> List<T> empty() {
		return new ArrayList<T>();
	}

	public static <T> Set<T> emptySet() {
		return new HashSet<T>();
	}
	
	public static <T> List<T> copy(List<T> list) {
		return new ArrayList<T>(list);
	}
	
	public static <K, V> Map<K, V> copyMap(Map<K, V> other) {
		return new HashMap<K, V>(other);
	}
	
	public static <K, V> Map<K, V> emptyMap() {
		return new HashMap<K, V>();
	}

	public static void exitOnUncaughtExceptions(final String[] args, final String emailAddress, final String name) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override public void uncaughtException(Thread t, Throwable e) {
				System.err.println(name + " Server shutting down due to error in thread " + t + "\n" + e.getMessage());
				e.printStackTrace(System.err);
				Email email = problem(name + " server (" + list(args) + ") shutting down.", "");
				email.append(trace(e));
				email.sendTo(emailAddress);
				System.exit(1);
			}
		});

	}
	
	public static <K, V> V requiredValue(Map<K, V> map, K key) {
        return bombNull(map.get(key), "no results found for key " + key + " in " + map);
	}

    public static String serialize(Object toSerialize) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            GZIPOutputStream gzOut = new GZIPOutputStream(bytes);
            ObjectOutputStream out = new ObjectOutputStream(gzOut);
            out.writeObject(toSerialize);
            gzOut.finish();
            bytes.close();
            return toBase64(bytes.toByteArray());
        } catch (Exception e) {
            throw bomb("error serializing " + toSerialize, e);
        }
    }

    public static Object deserialize(String text) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(fromBase64(text));
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(bytes));
            return in.readObject();
        } catch (Exception e) {
            throw bomb("exception caught while deserializing object", e);
        }
    }

    
    public static String guid(boolean useZeros) {
        UUID result = useZeros ? new UUID(0, 0) : UUID.randomUUID();
        return result.toString().toUpperCase();
    }

    public static String urlEncode(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, "UTF-8");
        } catch(Exception e) {
            throw bomb("UTF-8 is unsupported?", e);
        }
    }
    
    public static String urlDecode(String toDecode) {
        try {
            return URLDecoder.decode(toDecode, "UTF-8");
        } catch(Exception e) {
            throw bomb("UTF-8 is unsupported?", e);
        }
    }

}
