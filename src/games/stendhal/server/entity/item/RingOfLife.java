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
package games.stendhal.server.entity.item;

import java.util.Map;

/**
 * A ring that protects from XP loss.
 */
public class RingOfLife extends Item {
	public RingOfLife(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item copied item
	 */
	public RingOfLife(final RingOfLife item) {
		super(item);
	}

	/**
	 * Create a RingOfLife.
	 */
	public RingOfLife() {
		super("翡翠戒指", "ring", "emerald-ring", null);
		put("amount", 1);
	}

	/**
	 * Check if the ring is broken.
	 *
	 * @return <code>true</code> if the ring is broken, <code>true</code> if
	 *	it's intact
	 */
	public boolean isBroken() {
		return  getInt("amount") == 0;
	}

	/**
	 * Use up the ring's power.
	 */
	public void damage() {
		put("amount", 0);
	}

	@Override
	public void repair() {
		put("amount", 1);
	}

	/**
	 * Gets the description.
	 *
	 * The description of RingOfLife depends on the ring's state.
	 *
	 * @return The description text.
	 */
	@Override
	public String describe() {
		String text;
		if (isBroken()) {
			text = "你看到 §'翡翠戒指', 看起来是生命戒指. 但上面宝石不再发光, 已失去能量.";
		} else {
			text = "你看到 §'翡翠戒指', 看起来是生命戒指. 戴着它, 能提高生存机率. ";
		}

		if (isBound()) {
			text = text + " 这是一件特殊任务中的 " + getBoundTo()
					+ " 物品, 不能用在其它地方.";
		}
		return text;
	}
}
