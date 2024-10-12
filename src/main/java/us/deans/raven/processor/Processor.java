package us.deans.raven.processor;

public interface Processor {
    void upload() throws Exception;
    void log();
}
