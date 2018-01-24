package jmg;

public class JMGException extends RuntimeException
{
	public JMGException(Throwable cause)
	{
		super(cause);
	}

	public JMGException(String msg)
	{
		super(msg);
	}
}
