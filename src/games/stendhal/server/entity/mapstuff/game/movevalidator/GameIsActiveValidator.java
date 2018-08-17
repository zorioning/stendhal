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
package games.stendhal.server.entity.mapstuff.game.movevalidator;

import games.stendhal.server.entity.item.token.BoardToken;
import games.stendhal.server.entity.mapstuff.game.GameBoard;
import games.stendhal.server.entity.player.Player;

/**
 * checks that the game is currently in progress
 *
 * @author hendrik
 */
public class GameIsActiveValidator implements MoveValidator {

	@Override
	public boolean validate(GameBoard board, Player player, BoardToken token, int xIndex, int yIndex) {
		if (!board.isGameActive()) {
			player.sendPrivateText("请先和 " + board.getNPCName() + " 说开始游戏 start the game .");
			return false;
		}
		return true;
	}

}
