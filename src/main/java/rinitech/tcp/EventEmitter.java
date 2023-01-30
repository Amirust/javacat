package rinitech.tcp;
import java.util.*;
public class EventEmitter<T>
{
	private final HashMap<String, LinkedList<Pair<String, Callback<T>>>> callbacks = new HashMap<>();
	public void emit(String channel, T object) {
		if (channel.equals("*")) {
			for (Map.Entry<String, LinkedList<Pair<String, Callback<T>>>> entry : callbacks.entrySet())
				for (Pair<String, Callback<T>> callbackPair : entry.getValue())
					callbackPair.getSecond().call(channel, object);
		} else {
			LinkedList<Pair<String, Callback<T>>> callbackPairList = callbacks.get("*");
			if (callbackPairList != null)
				for (Pair<String, Callback<T>> callbackPair : callbackPairList)
					callbackPair.getSecond().call(channel, object);
			callbackPairList = callbacks.get(channel);
			if (callbackPairList == null) return;
			for (Pair<String, Callback<T>> callbackPair : callbackPairList)
				callbackPair.getSecond().call(channel, object);
		}
	}

	public String on(String channel, Callback<T> callback) {
		String uuid = UUID.randomUUID().toString();
		LinkedList<Pair<String, Callback<T>>> callbackPairList = callbacks.get(channel);
		if (callbackPairList == null) {
			callbackPairList = new LinkedList<>();
			callbackPairList.add(new Pair<>(uuid, callback));
			callbacks.put(channel, callbackPairList);
		} else {
			callbackPairList.add(new Pair<>(uuid, callback));
		}
		return uuid;
	}

	public void off(String uuid) {
		for (Map.Entry<String, LinkedList<Pair<String, Callback<T>>>> entry : callbacks.entrySet())
			entry.getValue().removeIf(pair -> pair.getFirst().equals(uuid));
	}
}

class Pair<T1, T2> {

	private final T1 first;
	private final T2 second;

	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}
}

@FunctionalInterface
interface Callback<T> {
	void call(String channel, T object);
}