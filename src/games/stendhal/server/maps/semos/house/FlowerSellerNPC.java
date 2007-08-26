package games.stendhal.server.maps.semos.house;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.TeleporterBehaviour;
import games.stendhal.server.maps.ZoneConfigurator;

import java.util.Map;

/**
 * Builds a Flower Seller NPC for the Elf Princess quest
 *
 * @author kymara
 */
public class FlowerSellerNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {

        	new TeleporterBehaviour(buildSemosHouseArea(), "Flowers! Get your fresh flowers here!");
	}

	private SpeakerNPC buildSemosHouseArea() {

	    SpeakerNPC rose = new SpeakerNPC("Rose Leigh") {
	                @Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}
	                @Override
			protected void createDialog() {
			    addJob("I'm a wandering flower woman.");
			    addGoodbye("Everything's coming up roses ... bye ...");
			    // the rest is in the ElfPrincess quest
			}

		};
		npcs.add(rose);
		rose.setEntityClass("gypsywomannpc");
		rose.initHP(100);

		// start in int_semos_house
		StendhalRPZone	zone = StendhalRPWorld.get().getZone("int_semos_house");
		zone.assignRPObjectID(rose);
		rose.setPosition(5, 6);
		zone.add(rose);

		return rose;
	}
}
