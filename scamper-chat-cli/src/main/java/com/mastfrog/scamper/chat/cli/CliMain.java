package com.mastfrog.scamper.chat.cli;

import com.google.inject.AbstractModule;
import com.mastfrog.scamper.Address;
import com.mastfrog.scamper.chat.base.ScamperClient;
import java.io.IOException;

/**
 *
 * @author Tim Boudreau
 */
public class CliMain extends AbstractModule {

    public static void main(String[] args) throws IOException, InterruptedException {
        ScamperClient client = new ScamperClient(CliClient.class, new CliMain());
        CLI cli = client.start(CLI.class, args);
        cli.start().await();
    }

    @Override
    protected void configure() {
        bind(Address.class).toInstance(new Address("netbeans.ath.cx", 8007));
    }
}
