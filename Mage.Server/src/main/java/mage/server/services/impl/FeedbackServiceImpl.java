package mage.server.services.impl;

import mage.server.services.FeedbackService;

import java.util.Calendar;

/**
 * @author noxx
 */
public enum FeedbackServiceImpl implements FeedbackService {
    instance;

    @Override
    public void feedback(String username, String title, String type, String message, String email, String host) {
        Calendar cal = Calendar.getInstance();
    }
}
