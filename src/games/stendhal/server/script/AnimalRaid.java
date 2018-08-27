/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kymara
 *
 * Not safe for players below level 5
 */
public class AnimalRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("猴子", 2);
		attackArmy.put("草蛇", 2);
		attackArmy.put("海狸", 2);
		attackArmy.put("老虎", 2);
		attackArmy.put("狮子", 3);
		attackArmy.put("大熊猫", 2);
		attackArmy.put("penguin", 4);
		attackArmy.put("凯门鳄", 3);
		attackArmy.put("幼熊", 2);
		attackArmy.put("黑熊", 1);
		attackArmy.put("大象", 3);
		attackArmy.put("鳄鱼", 2);

		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return " * 不适合5级以下玩家.";
	}
}
