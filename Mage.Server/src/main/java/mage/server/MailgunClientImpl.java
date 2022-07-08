package mage.server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import mage.server.managers.ConfigSettings;
import mage.server.managers.MailClient;

import javax.ws.rs.core.MediaType;

public class MailgunClientImpl implements MailClient {

    private final ConfigSettings config;

    public MailgunClientImpl(ConfigSettings config) {
        this.config = config;
    }

    public boolean sendMessage(String email, String subject, String text) {
        if (email.isEmpty()) {
            return false;
        }
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", config.getMailgunApiKey()));
        String domain = config.getMailgunDomain();
        WebResource webResource = client.resource("https://api.mailgun.net/v3/" + domain + "/messages");
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("from", "XMage <postmaster@" + domain + '>');
        formData.add("to", email);
        formData.add("subject", subject);
        formData.add("text", text);
        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formData);
        boolean succeeded = response.getStatus() == 200;
        if (!succeeded) {
        }
        return succeeded;
    }
}
