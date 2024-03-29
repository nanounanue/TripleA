/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package games.strategy.engine.message;

/**
 * Called when the connection to a node is lost while invoking a remote method.
 * <p>
 * 
 * Only returned on remotes or channels that wait for the results of the method invocation.
 * <p>
 * 
 * @author sgb
 */
public class ConnectionLostException extends MessengerException
{
	private static final long serialVersionUID = -5310065420171098696L;
	
	public ConnectionLostException(final String message)
	{
		super(message, new Exception("Invoker Stack"));
	}
}
