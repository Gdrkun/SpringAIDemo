package com.drk.SpringAIDemo.pojo;

/**
 * @Author drk
 * @Date 2025/6/17 17:41
 * @Version 1.0
 */
public class ActorsFilms {
    private String actor;
    private java.util.List<String> movies;

    public ActorsFilms(String actor, java.util.List<String> movies) {
        this.actor = actor;
        this.movies = movies;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public java.util.List<String> getMovies() {
        return movies;
    }

    public void setMovies(java.util.List<String> movies) {
        this.movies = movies;
    }

    @Override
    public String toString() {
        return "ActorsFilms{" +
                "actor='" + actor + '\'' +
                ", movies=" + movies +
                '}';
    }
}
