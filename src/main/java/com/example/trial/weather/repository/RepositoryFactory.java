package com.example.trial.weather.repository;

/**
 * This is a factory for the repository, meant for the moment there are several
 * implementations and one has to be chosen based on certain parameters.
 * This is a typical implementation of a factory pattern.
 * @author ignaciolarranaga@gmail.com
 */
public class RepositoryFactory {

    /**
     * This is the current shared repository instance.
     */
    private static Repository repository;

    /**
     * @return A new instance if none was yet created or the existing one
     */
    public static Repository getInstance() {
        if (repository == null) {
            repository = new StaticRepositoryImpl();
        }

        return repository;
    }

}
