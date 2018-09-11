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

import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.spawner.FlowerGrower;

/**
 * A 种子 can be planted.
 * The plant action defines the behaviour (e.g. only plantable on fertile ground).
 * The infostring stores what it will grow.
 */
public class Seed extends StackableItem {

	public Seed(final Seed item) {
		super(item);
	}

	/**
	 * Creates a new seed
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public Seed(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (!this.isContained()) {
			// the 种子 is on the ground, but not next to the player
			if (!this.nextTo(user)) {
				user.sendPrivateText(" " + this.getName() + " 离你太远");
				return false;
			}

			// the infostring of the seed stores what it should grow
			final String infostring = this.getInfoString();
			FlowerGrower flowerGrower;
			// choose the default flower grower if there is none set
			if (infostring == null) {
				flowerGrower = new FlowerGrower();
			} else {
				flowerGrower = new FlowerGrower(this.getInfoString());
			}
			user.getZone().add(flowerGrower);
			// add the FlowerGrower where the 种子 was on the ground
			flowerGrower.setPosition(this.getX(), this.getY());
			// The first stage of growth happens almost immediately
			TurnNotifier.get().notifyInTurns(3, flowerGrower);
			// remove the seed now that it is planted
			this.removeOne();
			return true;
		}
		// the 种子 was 'contained' in a slot and so it cannot be planted
		user.sendPrivateText("你把 " + this.getName() + " 直接放在地面上, 笨!");
		return false;
	}

	@Override
	public String describe() {
		final String flowerName = getInfoString();

		if (flowerName != null) {
			return "你看到 " + flowerName + " " + this.getName() + "."
                + "可以把它种在其他地方, 但它只能种在肥沃松软的土中.";
		} else {
			return "一粒种子. 可以把它种在其他地方, 但它只能种在肥沃松软的土中.";
		}
	}
}
