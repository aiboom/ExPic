package top.aiboom.lib.expic.builder;

public interface PropertyBuilder {
    
    PropertyBuilder type(String type);
    PropertyBuilder path(String path);
    PropertyBuilder size(int width, int height);
    PropertyBuilder quality(int quality);
    PropertyBuilder name(String name);
}
