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
package games.stendhal.server.maps.semos.tavern.market;

import java.util.Set;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Earning;
import games.stendhal.server.entity.trade.Market;
/**
 * chat action to let a player fetch his earnings from the market
 *
 * @author madmetzger
 *
 */
public class FetchEarningsChatAction implements ChatAction {

	@Override
	public void fire(Player player, Sentence sentence, EventRaiser npc) {
		if (sentence.hasError()) {
			npc.say("抱歉，我听不懂你说的话. "
					+ sentence.getErrorString());
			npc.setCurrentState(ConversationStates.ATTENDING);
		} else {
			handleSentence(player, npc);
		}
	}

	private void handleSentence(Player player, EventRaiser npc) {
		Market market = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
		Set<Earning> earnings = market.fetchEarnings(player);
		int collectedSum = 0;
		for (Earning earning : earnings) {
			collectedSum += earning.getValue().intValue();
		}
		if (collectedSum > 0) {
			player.sendPrivateText("你的销售总额为 "+Integer.valueOf(collectedSum).toString()+" 钱.");
			npc.say("欢迎来到 Semos 交易中心，我已把你代销的货款给你，还有其它事吗?");
		} else {
			//either you have no space in your bag or there isn't anything to collect
			npc.say("欢迎来到 Semos 交易中心. 需要什么服务吗 #help ?");
		}
		npc.setCurrentState(ConversationStates.ATTENDING);
	}
}
