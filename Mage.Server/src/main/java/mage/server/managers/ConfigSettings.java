package mage.server.managers;

public interface ConfigSettings {
    String getServerAddress();

    String getServerName();

    int getPort();

    int getSecondaryBindPort();

    int getLeasePeriod();

    int getSocketWriteTimeout();

    int getMaxPoolSize();

    int getNumAcceptThreads();

    int getBacklogSize();

    int getMaxGameThreads();

    int getMaxSecondsIdle();

    int getMinUserNameLength();

    int getMaxUserNameLength();

    String getInvalidUserNamePattern();

    int getMinPasswordLength();

    int getMaxPasswordLength();

    String getMaxAiOpponents();

    Boolean isSaveGameActivated();

    Boolean isAuthenticationActivated();

    String getGoogleAccount();

    String getMailgunApiKey();

    String getMailgunDomain();

    String getMailSmtpHost();

    String getMailSmtpPort();

    String getMailUser();

    String getMailPassword();

    String getMailFromAddress();
}
