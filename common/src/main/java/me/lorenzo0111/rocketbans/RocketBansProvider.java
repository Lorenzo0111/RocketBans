package me.lorenzo0111.rocketbans;

public class RocketBansProvider {
    private static RocketBansPlugin provider;

    public static RocketBansPlugin get() {
        return provider;
    }

    public static void set(RocketBansPlugin provider) {
        RocketBansProvider.provider = provider;
    }
}
