package com.example.demo.web;

import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.basiclands.Forest;
import mage.cards.decks.Deck;
import mage.cards.g.GrizzlyBears;
import mage.constants.Rarity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {
    @Value("${spring.application.name}")

    @GetMapping("/")
    public ModelAndView getTestData() {
        Card bears = CardImpl.createCard(
                GrizzlyBears.class,
                new CardSetInfo(
                        "Tenth Edition",
                        "10E",
                        "268",
                        Rarity.COMMON)
        );

        Card forest = CardImpl.createCard(
                Forest.class,
                new CardSetInfo(
                        "Tenth Edition",
                        "10E",
                        "380",
                        Rarity.COMMON)
        );

        Deck deck = new Deck();
        // Add 40 Grizzly Bears and 20 forests to the deck
        for (int i = 0; i < 40; i++) {
            deck.getCards().add(bears);
        }
        for (int i = 0; i < 20; i++) {
            deck.getCards().add(forest);
        }


        ModelAndView mv = new ModelAndView();
        mv.setViewName("home");
        mv.getModel().put("data", deck);

        return mv;
    }
}
