package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.util.List;

public class StatisticCommand extends AbstractCommand {

    private enum Action { ADD, TAKE, SET }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_players = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                scriptEntry.addObject("action", arg.asElement());

            else if (arg.matchesPrefix("players")
                    && !scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class));
                specified_players = true;
            }

            else if (!scriptEntry.hasObject("statistic")
                    && arg.matchesEnum(Statistic.values()))
                scriptEntry.addObject("statistic", arg.asElement());

            else if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("amount", arg.asElement());

            else if (arg.matchesPrefix("qualifier, q")
                    && !scriptEntry.hasObject("material")
                    && !scriptEntry.hasObject("entity")) {
                if (arg.matchesArgumentType(dMaterial.class))
                    scriptEntry.addObject("material", arg.asType(dMaterial.class));
                else if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("entity", arg.asType(dEntity.class));
            }

        }

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify a valid action!");

        if (!scriptEntry.hasObject("statistic"))
            throw new InvalidArgumentsException("Must specify a valid Statistic!");

        if (!scriptEntry.hasObject("amount"))
            scriptEntry.addObject("amount", new Element(1));

        Statistic.Type type = Statistic.valueOf(scriptEntry.getElement("statistic").asString().toUpperCase()).getType();
        if (type != Statistic.Type.UNTYPED) {
            if ((type == Statistic.Type.BLOCK || type == Statistic.Type.ITEM) && !scriptEntry.hasObject("material"))
                throw new InvalidArgumentsException("Must specify a valid " + type.name() + " MATERIAL!");
            else if (type == Statistic.Type.ENTITY && !scriptEntry.hasObject("entity"))
                throw new InvalidArgumentsException("Must specify a valid ENTITY!");
        }

        if (!scriptEntry.hasObject("players") && scriptEntry.hasPlayer() && !specified_players)
            scriptEntry.addObject("players", new dList(scriptEntry.getPlayer().identify()));

        if (!scriptEntry.hasObject("players"))
            throw new InvalidArgumentsException("Must specify valid players!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        List<dPlayer> players = scriptEntry.getdObjectAs("players", dList.class).filter(dPlayer.class);
        Element statistic = scriptEntry.getElement("statistic");
        Element amount = scriptEntry.getElement("amount");
        dMaterial material = scriptEntry.hasObject("material")
                ? scriptEntry.getdObjectAs("material", dMaterial.class) : null;
        dEntity entity = scriptEntry.hasObject("entity")
                ? scriptEntry.getdObjectAs("entity", dEntity.class) : null;

        dB.report(scriptEntry, getName(), action.debug() + statistic.debug() + amount.debug()
                + (material != null ? material.debug() : entity != null ? entity.debug() : ""));

        Action act = Action.valueOf(action.asString().toUpperCase());
        Statistic stat = Statistic.valueOf(statistic.asString().toUpperCase());
        int amt = amount.asInt();
        switch (stat.getType()) {

            case BLOCK:
            case ITEM:
                Material mat = material.getMaterial();
                switch (act) {
                    case ADD:
                        for (dPlayer player : players)
                            player.incrementStatistic(stat, mat, amt);
                        break;
                    case TAKE:
                        for (dPlayer player : players)
                            player.decrementStatistic(stat, mat, amt);
                        break;
                    case SET:
                        for (dPlayer player : players)
                            player.setStatistic(stat, mat, amt);
                        break;
                }
                break;

            case ENTITY:
                EntityType ent = entity.getEntityType();
                switch (act) {
                    case ADD:
                        for (dPlayer player : players)
                            player.incrementStatistic(stat, ent, amt);
                        break;
                    case TAKE:
                        for (dPlayer player : players)
                            player.decrementStatistic(stat, ent, amt);
                        break;
                    case SET:
                        for (dPlayer player : players)
                            player.setStatistic(stat, ent, amt);
                        break;
                }
                break;

        }

    }

}
