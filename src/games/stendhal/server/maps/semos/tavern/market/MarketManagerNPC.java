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

import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TextHasParameterCondition;
import games.stendhal.server.entity.trade.Offer;

public final class MarketManagerNPC extends SpeakerNPC {

	private Map<String,Offer> offers = new HashMap<String, Offer>();

	MarketManagerNPC(String name) {
		super(name);
		// Use smaller than normal range to not interfere players trying to talk
		// to the other NPCs in the tavern.
		setPerceptionRange(3);
	}

	@Override
	protected void createPath() {
		// npc is lazy and does not move
	}

	@Override
	protected void onGoodbye(RPEntity player) {
		//clean the offer map on leaving of a player
		offers.clear();
		setDirection(Direction.DOWN);
	}

	@Override
	protected void createDialog() {
		addGreeting("欢迎来到交易中心. 有什么需要 #帮助 的吗? #help ?");
		addJob("听说要 #帮助 你想卖些东西 #help.");
		addOffer("想在这个市场上架代销商品，请对我说 #卖 #物品 #价格 或者 #sell #item #price " +
				" ,而且就算你不在线，其他人也可以购买，还有其他的事请说 #帮助 或 #help.");
		addHelp("你想知道如何 #购买 或 #卖出 吗？ #buying or  #selling?");
		addReply("购买", "如果你想买些东西，请先说 #show ，我会给出一张含有编号的的价目表 " +
				 ". 如果你看中某件东西，对我说 #买 #编号 或者 #accept #number  " +
				 "就可以买到这个编号对应的商品. 也可以查看某一种商品的列表，只用对我说： " +
				 "比如 #show #肉块 ，就会只显示肉块的价格表.");
		addReply("卖出", "说出 #卖 #物品 #价格 或者 #sell #item #price ，就可以把商品放到交易中心代销。" +
				 "如果想下架商品，对我说 #'show mine', 这时你会只看到你的代销商品，然后说  #remove " +
				 "#number ，就会在市场下架商品。如果你有过期商品，可以说 #show #expired " +
				 " . 然后可以延长期限，可以说： #prolong #number. 如果你的东西卖掉了，" +
				 "你就对我说 #fetch ，我就会把代收款项付给你.");
		new PrepareOfferHandler().add(this);
		add(ConversationStates.ATTENDING, "show", new NotCondition(new TextHasParameterCondition()),
				ConversationStates.ATTENDING, null, new ShowOfferItemsChatAction());
		add(ConversationStates.ATTENDING, "show", new TextHasParameterCondition(), ConversationStates.ATTENDING, null, new ShowOffersChatAction());
		// fetch earnings when starting to talk to the market manager
		add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, new PlayerHasEarningsToCollectCondition(), ConversationStates.ATTENDING, null, new FetchEarningsChatAction());
		new AcceptOfferHandler().add(this);
		new RemoveOfferHandler().add(this);
		new ProlongOfferHandler().add(this);
		add(ConversationStates.ATTENDING, "examine", null, ConversationStates.ATTENDING, null, new ExamineOfferChatAction());
		addGoodbye("下次可以来问我销售情况，也可以取走代收款!");
	}

	public Map<String, Offer> getOfferMap() {
		return offers;
	}
}
