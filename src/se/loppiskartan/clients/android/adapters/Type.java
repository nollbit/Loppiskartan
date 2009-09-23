package se.loppiskartan.clients.android.adapters;

public class Type<E> {

	private String name;
	private E type;
	
	public Type(E type, String name)
	{
		this.type = type;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public E getType() {
		return type;
	}
	public String toString()
	{
		return getName();
	}
}
