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
package games.strategy.kingstable.delegate;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.GameParser;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.UnitType;

import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @author Lane Schwartz
 * @version $LastChangedDate: 2012-09-21 11:25:50 -0500 (Fri, 21 Sep 2012) $
 */
public class DelegateTest extends TestCase
{
	protected GameData m_data;
	protected PlayerID black;
	protected PlayerID white;
	protected Territory[][] territories;
	protected UnitType pawn;
	protected UnitType king;
	
	/**
	 * Creates new DelegateTest
	 */
	public DelegateTest(final String name)
	{
		super(name);
		// System.out.println("constructor");
	}
	
	@Override
	public void setUp() throws Exception
	{
		// get the xml file
		final URL url = this.getClass().getResource("DelegateTest.xml");
		final InputStream input = url.openStream();
		m_data = (new GameParser()).parse(input, false);
		input.close();
		black = m_data.getPlayerList().getPlayerID("Black");
		white = m_data.getPlayerList().getPlayerID("White");
		territories = new Territory[m_data.getMap().getXDimension()][m_data.getMap().getYDimension()];
		for (int x = 0; x < m_data.getMap().getXDimension(); x++)
			for (int y = 0; y < m_data.getMap().getYDimension(); y++)
				territories[x][y] = m_data.getMap().getTerritoryFromCoordinates(x, y);
		pawn = m_data.getUnitTypeList().getUnitType("pawn");
		king = m_data.getUnitTypeList().getUnitType("king");
		// System.out.println("setup");
	}
	
	/*
	
	public void testSample()
	{
		System.out.println("samelp");
	}
	*/
	public void assertValid(final String string)
	{
		assertNull(string);
	}
	
	public void assertError(final String string)
	{
		assertNotNull(string);
	}
	
	public void testTest()
	{
		assertValid(null);
		assertError("Can not do this");
	}
}
