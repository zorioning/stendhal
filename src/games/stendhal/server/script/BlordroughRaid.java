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
 * @author gummipferd
 *
 * Not safe for players below level 150
 */
public class BlordroughRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("大巨头", 5);
		attackArmy.put("帝国巨人将军", 5);
		attackArmy.put("布拉德鲁军队", 9);
		attackArmy.put("布拉德鲁下士", 6);
		attackArmy.put("布拉德鲁风暴骑兵", 8);
		attackArmy.put("巨人之主", 2);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "不适合150级以下的玩家.";
	}
}
