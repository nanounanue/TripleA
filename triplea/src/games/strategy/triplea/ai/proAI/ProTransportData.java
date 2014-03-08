package games.strategy.triplea.ai.proAI;

import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProTransportData
{
	private Unit transport;
	private Map<Territory, Set<Territory>> transportMap;
	
	public ProTransportData(Unit transport)
	{
		this.transport = transport;
		transportMap = new HashMap<Territory, Set<Territory>>();
	}
	
	public void addTerritories(Set<Territory> attackTerritories, Set<Territory> myUnitsToLoadTerritories)
	{
		for (Territory attackTerritory : attackTerritories)
		{
			// Populate enemy territories with sea unit
			if (transportMap.containsKey(attackTerritory))
			{
				transportMap.get(attackTerritory).addAll(myUnitsToLoadTerritories);
			}
			else
			{
				Set<Territory> territories = new HashSet<Territory>();
				territories.addAll(myUnitsToLoadTerritories);
				transportMap.put(attackTerritory, territories);
			}
		}
	}
	
	public void setTransportMap(Map<Territory, Set<Territory>> transportMap)
	{
		this.transportMap = transportMap;
	}
	
	public Map<Territory, Set<Territory>> getTransportMap()
	{
		return transportMap;
	}
	
	public void setTransport(Unit transport)
	{
		this.transport = transport;
	}
	
	public Unit getTransport()
	{
		return transport;
	}
	
}
