package util;

public class ContentHolder<T>
{
	T content;
	
	public ContentHolder(T _content)
	{
		content = _content;
	}
	
	public T get()
	{
		return content;
	}
	
	public ContentHolder<T> set(T _content)
	{
		content = _content;
		return this;
	}
	
	@Override
	public String toString()
	{
		return content.toString();
	}
}