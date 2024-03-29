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
/*
 * GamePlayer.java
 * 
 * Created on October 27, 2001, 5:15 PM
 */
package games.strategy.engine.gamePlayer;

import games.strategy.engine.data.PlayerID;

/**
 * 
 * A player of the game.
 * <p>
 * Game players communicate to the game through a PlayerBridge.
 * 
 * @author Sean Bridges
 * @version 1.0
 * 
 */
public interface IGamePlayer extends IRemotePlayer
{
	/**
	 * Called before the game starts.
	 */
	public void initialize(IPlayerBridge bridge, PlayerID id);
	
	/**
	 * 
	 * @return the name of the game player (what nation we are)
	 */
	public String getName();
	
	/**
	 * 
	 * @return the type of player we are (human or a kind of ai)
	 */
	public String getType();
	
	/**
	 * Start the given step. stepName appears as it does in the game xml file.
	 * 
	 * The game step will finish executing when this method returns.
	 * 
	 */
	public void start(String stepName);
	
	/*
	 * (now in superclass)
	 * @return the id of this player. This id is initialized by the initialize method in IGamePlayer.
	public PlayerID getPlayerID();
	 */
}
