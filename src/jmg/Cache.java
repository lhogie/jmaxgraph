package jmg;

import java.util.function.Supplier;

import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.io.serialization.Serializer;

public class Cache<T>
{
	private T value;
	private final T invalidValue;
	private final RegularFile file;
	private final Serializer<T> serializer;
	private final Supplier<T> supplier;
	public int nbThreads;

	public Cache(T iv, String name, Directory d, Supplier<T> s)
	{
		this(iv, name, d, Serializer.getDefaultSerializer(), s);
	}

	public Cache(T iv, String name, Directory d, Serializer<T> serializer,
			Supplier<T> supplier)
	{
		this.invalidValue = iv;
		this.file = d == null ? null
				: new RegularFile(d, name + "." + serializer.getMIMEType());
		this.serializer = serializer == null ? Serializer.getDefaultSerializer()
				: serializer;
		this.value = file == null || ! file.exists() ? invalidValue
				: set(deserialize(file.getContent()));
		this.supplier = supplier;
	}

	protected T deserialize(byte[] bytes)
	{
		return (T) serializer.fromBytes(bytes);
	}

	protected byte[] serialize(T e)
	{
		return serializer.toBytes(e);
	}

	public T get()
	{
		if ( ! isValid())
		{
			T computedValue = supplier.get();
			set(computedValue);
		}

		return value;
	}

	public T set(T t)
	{
		if (file != null)
		{
			file.getParent().ensureExists();
			file.setContent(serialize(t));
		}

		return this.value = t;
	}

	public boolean isValid()
	{
		return ! value.equals(invalidValue);
	}

	public void invalidate()
	{
		set(invalidValue);
	}
}
