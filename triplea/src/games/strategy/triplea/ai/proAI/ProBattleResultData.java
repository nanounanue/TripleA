package games.strategy.triplea.ai.proAI;

import games.strategy.engine.data.Unit;

import java.util.ArrayList;
import java.util.List;

public class ProBattleResultData
{
	private double winPercentage;
	private double TUVSwing;
	private boolean hasLandUnitRemaining;
	private List<Unit> averageUnitsRemaining;
	
	public ProBattleResultData()
	{
		winPercentage = 0;
		TUVSwing = 0;
		hasLandUnitRemaining = false;
		averageUnitsRemaining = new ArrayList<Unit>();
	}
	
	public ProBattleResultData(double winPercentage, double TUVSwing, boolean hasLandUnitRemaining, List<Unit> averageUnitsRemaining)
	{
		this.winPercentage = winPercentage;
		this.TUVSwing = TUVSwing;
		this.hasLandUnitRemaining = hasLandUnitRemaining;
		this.averageUnitsRemaining = averageUnitsRemaining;
	}
	
	public double getWinPercentage()
	{
		return winPercentage;
	}
	
	public void setWinPercentage(double winPercentage)
	{
		this.winPercentage = winPercentage;
	}
	
	public double getTUVSwing()
	{
		return TUVSwing;
	}
	
	public void setTUVSwing(double tUVSwing)
	{
		TUVSwing = tUVSwing;
	}
	
	public boolean isHasLandUnitRemaining()
	{
		return hasLandUnitRemaining;
	}
	
	public void setHasLandUnitRemaining(boolean hasLandUnitRemaining)
	{
		this.hasLandUnitRemaining = hasLandUnitRemaining;
	}

	public void setAverageUnitsRemaining(List<Unit> averageUnitsRemaining)
	{
		this.averageUnitsRemaining = averageUnitsRemaining;
	}

	public List<Unit> getAverageUnitsRemaining()
	{
		return averageUnitsRemaining;
	}
	
}
