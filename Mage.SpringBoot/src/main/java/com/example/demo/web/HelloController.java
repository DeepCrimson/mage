package com.example.demo.web;

import mage.cards.*;
import mage.cards.decks.Deck;
import mage.cards.g.GrizzlyBears;
import mage.cards.repository.CardCriteria;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.constants.ColoredManaSymbol;
import mage.constants.Rarity;
import mage.util.RandomUtil;
import mage.util.TournamentUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class HelloController {
    @Value("${spring.application.name}")
    String appName;

//    @GetMapping("/")
//    public String homePage(Model model) {
//        model.addAttribute("appName", appName);
//        return "home";
//    }

    public static Deck buildRandomDeck(String colors) {

        List<ColoredManaSymbol> allowedColors = new ArrayList<>();
        for (int i = 0; i < colors.length(); i++) {
            char c = colors.charAt(i);
            allowedColors.add(ColoredManaSymbol.lookup(c));
        }

        List<String> allowedList = new ArrayList<>();

        List<Card> cardPool = Sets.generateRandomCardPool(45, allowedColors, true, allowedList);
        return buildDeck(60, cardPool, allowedColors);
    }

    public static Deck buildDeck(int deckMinSize, List<Card> cardPool, final List<ColoredManaSymbol> colors) {
        return buildDeckWithOnlyBasicLands(deckMinSize, cardPool);
    }

    public static Deck buildDeckWithOnlyBasicLands(int deckMinSize, List<Card> cardPool) {
        // random cards from card pool
        Deck deck = new Deck();
        final int DECK_SIZE = deckMinSize != 0 ? deckMinSize : 40;

        List<Card> sortedCards = new ArrayList<>(cardPool);
        if (!sortedCards.isEmpty()) {
            while (deck.getCards().size() < DECK_SIZE) {
                deck.getCards().add(sortedCards.get(RandomUtil.nextInt(sortedCards.size())));
            }
            return deck;
        } else {
            addBasicLands(deck, DECK_SIZE);
            return deck;
        }
    }

    private static void addBasicLands(Deck deck, int number) {
        Set<String> landSets = TournamentUtil.getLandSetCodeForDeckSets(deck.getExpansionSetCodes());

        CardCriteria criteria = new CardCriteria();
        if (!landSets.isEmpty()) {
            criteria.setCodes(landSets.toArray(new String[0]));
        }
        criteria.rarities(Rarity.LAND).name("Forest");
        List<CardInfo> cards = CardRepository.instance.findCards(criteria);

        if (cards.isEmpty()) {
            criteria = new CardCriteria();
            criteria.rarities(Rarity.LAND).name("Forest");
            criteria.setCodes("M15");
            cards = CardRepository.instance.findCards(criteria);
        }

        for (int i = 0; i < number; i++) {
            Card land = cards.get(RandomUtil.nextInt(cards.size())).getCard();
            deck.getCards().add(land);
        }
    }

    @GetMapping("/")
    public ModelAndView getTestData() {
        Card card = CardImpl.createCard(
                GrizzlyBears.class,
                new CardSetInfo(
                        "Tenth Edition",
                        "10E",
                        "268",
                        Rarity.COMMON,
                        new CardGraphicInfo(null, false))
        );

        ModelAndView mv = new ModelAndView();
        mv.setViewName("home");
        mv.getModel().put("data", card);

        return mv;
    }
}
