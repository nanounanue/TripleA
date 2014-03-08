package games.strategy.triplea.ai.proAI;

import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.engine.data.UnitType;
import games.strategy.triplea.attatchments.TerritoryAttachment;
import games.strategy.triplea.delegate.Matches;
import games.strategy.util.IntegerMap;
import games.strategy.util.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProAttackTerritoryData
{
	private Territory territory;
	private List<Unit> maxUnits;
	private List<Unit> units;
	private double TUVSwing;
	private Double attackValue;
	private boolean canHold;
	
	public ProAttackTerritoryData(Territory territory)
	{
		this.territory = territory;
		maxUnits = new ArrayList<Unit>();
		units = new ArrayList<Unit>();
		TUVSwing = 0;
		canHold = false;
	}
	
	public void addUnit(Unit unit)
	{
		this.units.add(unit);
	}
	
	public void addMaxUnits(List<Unit> units)
	{
		this.maxUnits.addAll(units);
	}
	
	public void addMaxUnit(Unit unit)
	{
		this.maxUnits.add(unit);
	}
	
	public void setTerritory(Territory territory)
	{
		this.territory = territory;
	}
	
	public Territory getTerritory()
	{
		return territory;
	}
	
	public void setMaxUnits(List<Unit> units)
	{
		this.maxUnits = units;
	}
	
	public List<Unit> getMaxUnits()
	{
		return maxUnits;
	}
	
	public double getTUVSwing()
	{
		return TUVSwing;
	}
	
	public void setTUVSwing(double tUVSwing)
	{
		TUVSwing = tUVSwing;
	}
	
	public void setAttackValue(double attackValue)
	{
		this.attackValue = attackValue;
	}
	
	public double getAttackValue()
	{
		return attackValue;
	}
	
	public void setUnits(List<Unit> units)
	{
		this.units = units;
	}
	
	public List<Unit> getUnits()
	{
		return units;
	}

	public void setCanHold(boolean canHold)
	{
		this.canHold = canHold;
	}

	public boolean isCanHold()
	{
		return canHold;
	}
	
}
