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
package games.strategy.util;

import java.awt.Image;
import java.awt.image.ImageObserver;

public class NullImageObserver implements ImageObserver
{
	public NullImageObserver()
	{
	}
	
	public boolean imageUpdate(final Image image, final int flags, final int int2, final int int3, final int int4, final int int5)
	{
		return !((flags & ALLBITS) != 0) || ((flags & ABORT) != 0);
	}
}
