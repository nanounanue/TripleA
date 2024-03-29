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
package games.strategy.triplea.ai.Dynamix_AI.CommandCenter;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 * @author Stephen
 */
public class ReconsiderSignalCenter
{
	private static HashMap<PlayerID, ReconsiderSignalCenter> s_RSCInstances = new HashMap<PlayerID, ReconsiderSignalCenter>();
	
	public static ReconsiderSignalCenter get(final GameData data, final PlayerID player)
	{
		if (!s_RSCInstances.containsKey(player))
			s_RSCInstances.put(player, create(data, player));
		return s_RSCInstances.get(player);
	}
	
	private static ReconsiderSignalCenter create(final GameData data, final PlayerID player)
	{
		return new ReconsiderSignalCenter(data, player);
	}
	
	public static void ClearStaticInstances()
	{
		s_RSCInstances.clear();
	}
	
	public static void NotifyStartOfRound()
	{
		s_RSCInstances.clear();
	}
	
	@SuppressWarnings("unused")
	private GameData m_data = null;
	@SuppressWarnings("unused")
	private PlayerID m_player = null;
	
	public ReconsiderSignalCenter(final GameData data, final PlayerID player)
	{
		m_data = data;
		m_player = player;
	}
	
	public HashSet<Object> ObjectsToReconsider = new HashSet<Object>();
}
