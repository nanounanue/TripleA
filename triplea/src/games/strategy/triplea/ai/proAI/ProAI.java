package games.strategy.triplea.ai.proAI;

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
import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Route;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.engine.gamePlayer.IGamePlayer;
import games.strategy.net.GUID;
import games.strategy.triplea.Constants;
import games.strategy.triplea.TripleAUnit;
import games.strategy.triplea.ai.AbstractAI;
import games.strategy.triplea.attatchments.TerritoryAttachment;
import games.strategy.triplea.delegate.Matches;
import games.strategy.triplea.delegate.remote.IAbstractPlaceDelegate;
import games.strategy.triplea.delegate.remote.IMoveDelegate;
import games.strategy.triplea.delegate.remote.IPurchaseDelegate;
import games.strategy.triplea.delegate.remote.ITechDelegate;
import games.strategy.triplea.player.ITripleaPlayer;
import games.strategy.util.CompositeMatch;
import games.strategy.util.CompositeMatchAnd;
import games.strategy.util.Match;
import games.strategy.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A stronger AI, based on some additional complexity, used the weak AI as a blueprint.
 * 
 * This still needs work. Known Issues:
 * <ol>
 * <li>Ships are moving 1 territory too close to a large pack of ships (better analysis)</li>
 * <li>*partial* No submerging or retreating has been implemented</li>
 * <li>Need to analyze 1 territory further and delay attack if it brings a set of units under overwhelming odds</li>
 * </ol>
 * 
 * @author Ron Murhammer
 * @year 2013
 */
@SuppressWarnings("deprecation")
public class ProAI extends AbstractAI implements IGamePlayer, ITripleaPlayer
{
	// Amphib Route will hold the water Terr, Land Terr combination for unloading units
	private final static Logger s_logger = Logger.getLogger(ProAI.class.getName());
	private boolean m_transports_may_die = true, m_natObjective = false;
	private final Collection<Territory> m_seaTerrAttacked = new ArrayList<Territory>();
	private final Collection<Territory> m_landTerrAttacked = new ArrayList<Territory>();
	private final Collection<Territory> m_impassableTerrs = new ArrayList<Territory>();
	
	/** Creates new TripleAPlayer */
	public ProAI(final String name, final String type)
	{
		super(name, type);
	}
	
	private void getEdition()
	{
		final GameData data = getPlayerBridge().getGameData();
		m_transports_may_die = !games.strategy.triplea.Properties.getTransportCasualtiesRestricted(data);
		m_natObjective = games.strategy.triplea.Properties.getNationalObjectives(data);
	}
	
	private void setImpassableTerrs(final PlayerID player)
	{
		final GameData data = getPlayerBridge().getGameData();
		m_impassableTerrs.clear();
		for (final Territory t : data.getMap().getTerritories())
		{
			if (Matches.TerritoryIsPassableAndNotRestricted(player, data).invert().match(t) && Matches.TerritoryIsLand.match(t))
				m_impassableTerrs.add(t);
		}
	}
	
	private Collection<Territory> getImpassableTerrs()
	{
		return m_impassableTerrs;
	}
	
	private void setSeaTerrAttacked(final Collection<Territory> seaTerr)
	{
		m_seaTerrAttacked.addAll(seaTerr);
	}
	
	private void clearSeaTerrAttacked()
	{
		m_seaTerrAttacked.clear();
	}
	
	private List<Territory> getSeaTerrAttacked()
	{
		final List<Territory> seaTerr = new ArrayList<Territory>(m_seaTerrAttacked);
		return seaTerr;
	}
	
	private void setLandTerrAttacked(final Collection<Territory> landTerr)
	{
		m_landTerrAttacked.addAll(landTerr);
	}
	
	private void clearLandTerrAttacked()
	{
		m_landTerrAttacked.clear();
	}
	
	private List<Territory> getLandTerrAttacked()
	{
		final List<Territory> landTerr = new ArrayList<Territory>(m_landTerrAttacked);
		return landTerr;
	}
	
	@SuppressWarnings("unused")
	private boolean getNationalObjectives()
	{
		return m_natObjective;
	}
	
	@Override
	protected void tech(final ITechDelegate techDelegate, final GameData data, final PlayerID player)
	{
		s_logger.fine("Starting tech: purchase nothing");
	}
	
	@Override
	protected void move(final boolean nonCombat, final IMoveDelegate moveDel, final GameData data, final PlayerID player)
	{
		if (nonCombat)
			doNonCombatMove(moveDel, player);
		else
			doCombatMove(moveDel, player);
		pause();
	}
	
	private void doNonCombatMove(final IMoveDelegate moveDel, final PlayerID player)
	{
		final GameData data = getPlayerBridge().getGameData();
		boolean foundOwnedUnits = false;
		for (final Territory ter : data.getMap().getTerritories())
		{
			if (ter.getUnits().someMatch(Matches.unitIsOwnedBy(player)))
			{
				foundOwnedUnits = true;
				break;
			}
		}
		if (!foundOwnedUnits)
			return; // If we don't own any units, just end right now
	}
	
	private void doCombatMove(final IMoveDelegate moveDel, final PlayerID player)
	{
		final GameData data = getPlayerBridge().getGameData();
		boolean foundOwnedUnits = false;
		for (final Territory ter : data.getMap().getTerritories())
		{
			if (ter.getUnits().getMatches(Matches.unitIsOwnedBy(player)).size() > 0)
			{
				foundOwnedUnits = true;
				break;
			}
		}
		if (!foundOwnedUnits)
			return; // If we don't own any units, just end right now
		getEdition();
		setImpassableTerrs(player);
	}
	
	private void doMove(final List<Collection<Unit>> moveUnits, final List<Route> moveRoutes, final List<Collection<Unit>> transportsToLoad, final IMoveDelegate moveDel)
	{
		for (int i = 0; i < moveRoutes.size(); i++)
		{
			pause();
			if (moveRoutes.get(i) == null || moveRoutes.get(i).getEnd() == null || moveRoutes.get(i).getStart() == null)
			{
				s_logger.fine("Route not valid" + moveRoutes.get(i) + " units:" + moveUnits.get(i));
				continue;
			}
			String result;
			if (transportsToLoad == null)
			{
				result = moveDel.move(moveUnits.get(i), moveRoutes.get(i));
			}
			else
				result = moveDel.move(moveUnits.get(i), moveRoutes.get(i), transportsToLoad.get(i));
			if (result != null)
			{
				s_logger.fine("could not move " + moveUnits.get(i) + " over " + moveRoutes.get(i) + " because : " + result + "\n");
			}
		}
	}
	
	private int countTransports(final GameData data, final PlayerID player)
	{
		final CompositeMatchAnd<Unit> ownedTransport = new CompositeMatchAnd<Unit>(Matches.UnitIsTransport, Matches.unitIsOwnedBy(player));
		int sum = 0;
		for (final Territory t : data.getMap())
		{
			sum += t.getUnits().countMatches(ownedTransport);
		}
		return sum;
	}
	
	private int countLandUnits(final GameData data, final PlayerID player)
	{
		final CompositeMatchAnd<Unit> ownedLandUnit = new CompositeMatchAnd<Unit>(Matches.UnitIsLand, Matches.unitIsOwnedBy(player));
		int sum = 0;
		for (final Territory t : data.getMap())
		{
			sum += t.getUnits().countMatches(ownedLandUnit);
		}
		return sum;
	}
	
	/**
	 * Count everything except transports
	 * 
	 * @param data
	 * @param player
	 * @return
	 */
	private int countSeaUnits(final GameData data, final PlayerID player)
	{
		final CompositeMatchAnd<Unit> ownedSeaUnit = new CompositeMatchAnd<Unit>(Matches.UnitIsSea, Matches.unitIsOwnedBy(player), Matches.UnitIsNotTransport);
		int sum = 0;
		for (final Territory t : data.getMap())
		{
			sum += t.getUnits().countMatches(ownedSeaUnit);
		}
		return sum;
	}
	
	@Override
	protected void purchase(final boolean purchaseForBid, int PUsToSpend, final IPurchaseDelegate purchaseDelegate, final GameData data, final PlayerID player)
	{
		long last, now;
		last = System.currentTimeMillis();
		s_logger.fine("Doing Purchase ");
		if (PUsToSpend == 0 && player.getResources().getQuantity(data.getResourceList().getResource(Constants.PUS)) == 0) // Check whether the player has ANY PU's to spend...
			return;
		// TODO: lot of tweaks have gone into this routine without good organization...need to cleanup
		// breakdown Rules by type and cost
		// final int currentRound = data.getSequence().getRound();
		
		now = System.currentTimeMillis();
		s_logger.finest("Time Taken " + (now - last));
		// purchaseDelegate.purchase(purchase);
	}
	
	@Override
	protected void place(final boolean bid, final IAbstractPlaceDelegate placeDelegate, final GameData data, final PlayerID player)
	{
		// if we have purchased a factory, it will be a priority for placing units
		// should place most expensive on it
		// need to be able to handle AA purchase
		long now, last;
		last = System.currentTimeMillis();
		s_logger.fine("Doing Placement ");
		if (player.getUnits().isEmpty())
			return;
		
		now = System.currentTimeMillis();
		s_logger.finest("Time Taken " + (now - last));
	}
	
	private void doPlace(final Territory where, final Collection<Unit> toPlace, final IAbstractPlaceDelegate del)
	{
		final String message = del.placeUnits(new ArrayList<Unit>(toPlace), where);
		if (message != null)
		{
			s_logger.fine(message);
			s_logger.fine("Attempt was at:" + where + " with:" + toPlace);
		}
		pause();
	}
	
	/*
	 * @see games.strategy.triplea.player.ITripleaPlayer#shouldBomberBomb(games.strategy.engine.data.Territory)
	 */
	@Override
	public boolean shouldBomberBomb(final Territory territory)
	{
		// only if not needed in a battle
		final GameData data = getPlayerBridge().getGameData();
		final PlayerID ePlayer = territory.getOwner();
		final List<PlayerID> attackPlayers = ProUtils.getEnemyPlayers(data, ePlayer); // list of players that could be the attacker
		boolean thisIsAnAttack = false;
		for (final PlayerID player : attackPlayers)
		{
			final CompositeMatch<Unit> noBomberUnit = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitIsNotStrategicBomber);
			final List<Unit> allAttackUnits = territory.getUnits().getMatches(noBomberUnit);
			if (!allAttackUnits.isEmpty())
				thisIsAnAttack = true;
		}
		return !thisIsAnAttack;
	}
	
	@Override
	public Unit whatShouldBomberBomb(final Territory territory, final Collection<Unit> potentialTargets, final Collection<Unit> bombers)
	{
		if (potentialTargets == null || potentialTargets.isEmpty())
			return null;
		final Collection<Unit> factories = Match.getMatches(potentialTargets, Matches.UnitCanProduceUnitsAndCanBeDamaged);
		if (factories.isEmpty())
			return potentialTargets.iterator().next();
		return factories.iterator().next();
	}
	
	@Override
	public boolean selectAttackSubs(final Territory unitTerritory)
	{
		return true;
	}
	
	@Override
	public boolean selectAttackUnits(final Territory unitTerritory)
	{
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see games.strategy.triplea.baseAI.AbstractAI#selectAttackTransports(games.strategy.engine.data.Territory)
	 */
	@Override
	public boolean selectAttackTransports(final Territory territory)
	{
		return true;
	}
	
	/*
	 * @see games.strategy.triplea.player.ITripleaPlayer#getNumberOfFightersToMoveToNewCarrier(java.util.Collection, games.strategy.engine.data.Territory)
	 */
	@Override
	public Collection<Unit> getNumberOfFightersToMoveToNewCarrier(final Collection<Unit> fightersThatCanBeMoved, final Territory from)
	{
		final List<Unit> rVal = new ArrayList<Unit>();
		for (final Unit fighter : fightersThatCanBeMoved)
			rVal.add(fighter);
		return rVal;
	}
	
	/*
	 * @see games.strategy.triplea.player.ITripleaPlayer#selectTerritoryForAirToLand(java.util.Collection, java.lang.String)
	 */
	@Override
	public Territory selectTerritoryForAirToLand(final Collection<Territory> candidates, final Territory currentTerritory, final String unitMessage)
	{
		// need to land in territory with infantry, especially if bomber
		return candidates.iterator().next();
	}
	
	@Override
	public boolean confirmMoveInFaceOfAA(final Collection<Territory> aaFiringTerritories)
	{
		return false;
	}
	
	/**
	 * Select the territory to bombard with the bombarding capable unit (eg battleship)
	 * 
	 * @param unit
	 *            - the bombarding unit
	 * @param unitTerritory
	 *            - where the bombarding unit is
	 * @param territories
	 *            - territories where the unit can bombard
	 * @param noneAvailable
	 * @return the Territory to bombard in, null if the unit should not bombard
	 */
	@Override
	public Territory selectBombardingTerritory(final Unit unit, final Territory unitTerritory, final Collection<Territory> territories, final boolean noneAvailable)
	{
		if (noneAvailable || territories.size() == 0)
			return null;
		else
		{
			for (final Territory t : territories)
				return t;
		}
		return null;
	}
	
	@Override
	public Territory retreatQuery(final GUID battleID, final boolean submerge, final Territory battleTerritory, final Collection<Territory> possibleTerritories, final String message)
	{
		if (battleTerritory == null)
			return null;
		// retreat anytime only air units are remaining
		// submerge anytime only subs against air units
		// don't understand how to use this routine
		final GameData data = getPlayerBridge().getGameData();
		// boolean iamOffense = get_onOffense();
		final boolean tFirst = m_transports_may_die;
		final boolean attacking = true; // determine whether player is offense or defense
		final PlayerID player = getPlayerID();
		final List<Unit> myUnits = battleTerritory.getUnits().getMatches(Matches.unitIsOwnedBy(player));
		final List<Unit> defendingUnits = battleTerritory.getUnits().getMatches(Matches.enemyUnit(player, data));
		if (Matches.TerritoryIsLand.match(battleTerritory))
		{
			final List<Unit> retreatUnits = new ArrayList<Unit>();
			final List<Unit> nonRetreatUnits = new ArrayList<Unit>();
			for (final Unit u : myUnits)
			{
				if (TripleAUnit.get(u).getWasAmphibious())
					nonRetreatUnits.add(u);
				else
					retreatUnits.add(u);
			}
			final float retreatStrength = ProUtils.strength(retreatUnits, true, false, false);
			final float nonRetreatStrength = ProUtils.strength(nonRetreatUnits, true, false, false);
			final float totalStrength = retreatStrength + nonRetreatStrength;
			final float enemyStrength = ProUtils.strength(defendingUnits, false, false, false);
			if (totalStrength > enemyStrength * 1.05F)
			{
				return null;
			}
			else
			{
				Territory retreatTo = null;
				float retreatDiff = 0.0F;
				if (possibleTerritories.size() == 1)
					retreatTo = possibleTerritories.iterator().next();
				else
				{
					final List<Territory> ourFriendlyTerr = new ArrayList<Territory>();
					final List<Territory> ourEnemyTerr = new ArrayList<Territory>();
					final HashMap<Territory, Float> rankMap = ProUtils.rankTerritories(data, ourFriendlyTerr, ourEnemyTerr, null, player, false, false, true);
					if (ourFriendlyTerr.containsAll(possibleTerritories))
						ProUtils.reorder(ourFriendlyTerr, rankMap, true);
					ourFriendlyTerr.retainAll(possibleTerritories);
					final Territory myCapital = TerritoryAttachment.getFirstOwnedCapitalOrFirstUnownedCapital(player, data);
					for (final Territory capTerr : ourFriendlyTerr)
					{
						if (Matches.territoryIsAlliedAndHasAlliedUnitMatching(data, player, Matches.UnitCanProduceUnits).match(capTerr))
						{
							final boolean isMyCapital = myCapital.equals(capTerr);
							final float strength1 = ProUtils.getStrengthOfPotentialAttackers(capTerr, data, player, false, true, null);
							float ourstrength = ProUtils.strengthOfTerritory(data, capTerr, player, false, false, false, true);
							if (isMyCapital)
							{
								ourstrength = ProUtils.strength(player.getUnits().getUnits(), false, false, false);
							}
							if (ourstrength < strength1 && (retreatTo == null || isMyCapital))
								retreatTo = capTerr;
						}
					}
					final Iterator<Territory> retreatTerrs = ourFriendlyTerr.iterator();
					if (retreatTo == null)
					{
						while (retreatTerrs.hasNext())
						{
							final Territory retreatTerr = retreatTerrs.next();
							final float existingStrength = ProUtils.strength(retreatTerr.getUnits().getUnits(), false, false, false);
							final float eRetreatStrength = ProUtils.getStrengthOfPotentialAttackers(retreatTerr, data, player, false, true, null);
							float firstDiff = eRetreatStrength - existingStrength;
							if (firstDiff < 0.0F)
							{
								firstDiff -= retreatStrength;
								if (firstDiff < 0.0F)
								{
									if (retreatDiff < firstDiff)
									{
										retreatTo = retreatTerr;
										retreatDiff = firstDiff;
									}
								}
								else if (retreatDiff > firstDiff || retreatTo == null)
								{
									retreatTo = retreatTerr;
									retreatDiff = firstDiff;
								}
							}
						}
					}
				}
				return retreatTo;
			}
		}
		else
		{
			final CompositeMatch<Unit> myShip = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitIsSea, Matches.unitIsNotSubmerged(data));
			final CompositeMatch<Unit> myPlane = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitIsAir);
			final CompositeMatch<Unit> enemyAirUnit = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsNotLand);
			final CompositeMatch<Unit> enemySeaUnit = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsSea);
			final List<Unit> myShips = battleTerritory.getUnits().getMatches(myShip);
			final List<Unit> myPlanes = battleTerritory.getUnits().getMatches(myPlane);
			final float myShipStrength = ProUtils.strength(myShips, attacking, true, tFirst);
			final float myPlaneStrength = ProUtils.strength(myPlanes, attacking, true, tFirst);
			final float totalStrength = myShipStrength + myPlaneStrength;
			final List<Unit> enemyAirUnits = battleTerritory.getUnits().getMatches(enemyAirUnit);
			final List<Unit> enemySeaUnits = battleTerritory.getUnits().getMatches(enemySeaUnit);
			if (submerge && enemySeaUnits.isEmpty() && enemyAirUnits.size() > 0)
				return battleTerritory;
			
			final float enemyAirStrength = ProUtils.strength(enemyAirUnits, !attacking, true, tFirst);
			final float enemySeaStrength = ProUtils.strength(enemySeaUnits, !attacking, true, tFirst);
			final float enemyStrength = enemyAirStrength + enemySeaStrength;
			if (attacking && enemyStrength > (totalStrength + 1.0F))
			{
				Territory retreatTo = null;
				if (possibleTerritories.size() > 0)
					retreatTo = possibleTerritories.iterator().next();
				// TODO: Create a selection for best seaTerritory
				return retreatTo;
			}
		}
		return null;
	}
	
	@Override
	public HashMap<Territory, Collection<Unit>> scrambleUnitsQuery(final Territory scrambleTo, final Map<Territory, Tuple<Collection<Unit>, Collection<Unit>>> possibleScramblers)
	{
		return null;
	}
	
	@Override
	public Collection<Unit> selectUnitsQuery(final Territory current, final Collection<Unit> possible, final String message)
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see games.strategy.triplea.player.ITripleaPlayer#selectFixedDice(int, java.lang.String)
	 */
	@Override
	public int[] selectFixedDice(final int numRolls, final int hitAt, final boolean hitOnlyIfEquals, final String message, final int diceSides)
	{
		final int[] dice = new int[numRolls];
		for (int i = 0; i < numRolls; i++)
		{
			dice[i] = (int) Math.ceil(Math.random() * diceSides);
		}
		return dice;
	}
	
	public static final Match<Unit> Transporting = new Match<Unit>()
	{
		@Override
		public boolean match(final Unit o)
		{
			return (TripleAUnit.get(o).getTransporting().size() > 0);
		}
	};
}
