package me.lorenzo0111.rocketbans.api;

public class RocketBansProvider {
    private static RocketBansAPI provider;

    public static RocketBansAPI get() {
        return provider;
    }

    public static void set(RocketBansAPI provider) {
        RocketBansProvider.provider = provider;
    }
}
