package dev.tricht.lunaris.item;

import dev.tricht.lunaris.item.parser.*;
import dev.tricht.lunaris.item.types.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ItemParser {

    private String[] lines;

    public ItemParser(String[] lines) {
        this.lines = lines;
    }

    public Item parse() {
        ArrayList<ArrayList<String>> parts = getParts();

        if (parts.size() <= 1) {
            return new Item();
        }

        NamePart namePart = new NamePart(parts.get(0));
        StatsPart statsPart = new StatsPart(parts.get(1));

        ItemType itemType = namePart.getItemType();
        if (itemType instanceof UnknownItem) {
            itemType = statsPart.getWeaponType();
        }

        if (itemType instanceof MapItem) {
            ((MapItem) itemType).setTier(statsPart.getMapTier());
        }

        //TODO: Prophecy

        ItemProps itemProps = new ItemPropsParts(parts).getProps();

        int affixIndex = new AffixPartIndexCalculator(namePart.getRarity(), itemType, itemProps, parts).getAffixIndex();
        AffixPart affixPart = new AffixPart(parts.get(affixIndex));

        ImplicitPart implicitPart = new ImplicitPart(parts.get(affixIndex - 1));
        ArrayList<String> implicits = implicitPart.getImplicits();
        if (implicitPart.getImplicits().size() > 0 && implicitPart.isRealImplicit()) {
            implicits.addAll(new ImplicitPart(parts.get(affixIndex - 2)).getImplicits());
        }

        // TODO: Abyssal sockets

        Item item = new Item();
        item.setType(itemType);
        item.setRarity(namePart.getRarity());
        item.setBase(namePart.getNameWithoutAffixes(affixPart.getAffixes(), itemProps.isIdentified()));
        item.setAffixes(affixPart.getAffixes());
        item.setProps(itemProps);
        item.setName(namePart.getItemName());
        item.setImplicits(implicits);

        if (itemType instanceof GemItem) {
            ((GemItem) itemType).setLevel(statsPart.getGemLevel());
            if (statsPart.isVaal()) {
                item.setBase("Vaal " + item.getBase());
            }
        }

        return item;
    }

    public ArrayList<ArrayList<String>> getParts() {
        ArrayList<ArrayList<String>> parts = new ArrayList<>();

        ArrayList<String> currentPart = new ArrayList<>();
        boolean notEquippable = false;
        for (String line : lines) {
            if (line.equals("You cannot use this item. Its stats will be ignored")) {
                notEquippable = true;
                continue;
            }
            if (line.equals("--------")) {
                if (notEquippable && parts.size() == 0) {
                    notEquippable = false;
                    continue;
                }
                parts.add(currentPart);
                currentPart = new ArrayList<>();
                continue;
            }
            currentPart.add(line);
        }
        parts.add(currentPart);

        return parts;
    }
}
