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

import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;

public class RemoveOfferHandler extends OfferHandler {
	@Override
	public void add(SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, "remove", null, ConversationStates.ATTENDING, null,
				new RemoveOfferChatAction());
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING, null, new ConfirmRemoveOfferChatAction());
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Ok, 还有需要我帮忙的吗?", null);
	}

	protected class RemoveOfferChatAction extends KnownOffersChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (sentence.hasError()) {
				npc.say("抱歉, 我不明白你的话. "
						+ sentence.getErrorString());
			} else if (sentence.getExpressions().iterator().next().toString().equals("remove")){
				handleSentence(player, sentence, npc);
			}
		}

		private void handleSentence(Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			try {
				String offerNumber = getOfferNumberFromSentence(sentence).toString();
				Map<String,Offer> offerMap = manager.getOfferMap();
				if (offerMap.isEmpty()) {
					npc.say("请先检查你的代销商品.");
					return;
				}
				if(offerMap.containsKey(offerNumber)) {
					Offer o = offerMap.get(offerNumber);
					if(o.getOfferer().equals(player.getName())) {
						setOffer(o);
						// Ask for confirmation only if the offer is still active
						if (TradeCenterZoneConfigurator.getShopFromZone(player.getZone()).contains(o)) {
							int quantity = 1;
							if (o.hasItem()) {
								quantity = getQuantity(o.getItem());
							}
							npc.say("要删除你的代销货物 " + o.getItemName() + " 吗?");
							npc.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							removeOffer(player, npc);
							// Changed the status, or it has been changed by expiration. Obsolete the offers
							((MarketManagerNPC) npc.getEntity()).getOfferMap().clear();
						}
						return;
					}
					npc.say("你只能删除自已的商品, 请对我说 #show #mine 查看自已的代销货物.");
					return;
				}
				npc.say("抱歉, 请在以上列表中选择要删除商品的序号. .");
			} catch (NumberFormatException e) {
				npc.say("抱歉, 请对我说 #remove #number");
			}
		}
	}

	protected class ConfirmRemoveOfferChatAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			removeOffer(player, npc);
			// Changed the status, or it has been changed by expiration. Obsolete the offers
			((MarketManagerNPC) npc.getEntity()).getOfferMap().clear();
		}
	}

	private void removeOffer(Player player, EventRaiser npc) {
		Offer offer = getOffer();
		Market m = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
		m.removeOffer(offer,player);
		npc.say("Ok.");
	}
}
