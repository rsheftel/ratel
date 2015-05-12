package util;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Tag {
    private final Element element;

    public Tag(String name) {
        element = new Element(name);
    }

    public Tag(Element element) {
        this.element = element;
    }

    public Tag add(String name) {
        Tag child = Tag.tag(name);
        add(child);
        return child;
    }

    public Tag add(Tag child) {
        element.addContent(child.element);
        return child;
    }

    public String xml() {
        String compact = xml(Format.getCompactFormat());
        String sansPreamble = compact.replaceAll("<\\?xml .*>[\n\r]*", "");
        return chomp(sansPreamble);
    }

    public String longXml() {
        return xml(Format.getPrettyFormat());
    }
    
    public static Tag parse(String xml) {
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(new StringReader(xml));
        } catch (Exception e) {
            throw bomb("failed to parse :\n" + xml, e);
        }
        return new Tag(doc.detachRootElement());
    }

    private String xml(Format format) {
        XMLOutputter out = new XMLOutputter();
        try {
            StringWriter w = new StringWriter();
            out.setFormat(format);
            out.output(element, w);
            return w.toString();
        } catch (IOException e) {
            throw bomb("failed converting xml to string", e);
        }
    }

    public static Tag tag(String name) { 
        return new Tag(name);
    }

    @Override public String toString() {
        return xml();
    }
    
    public Tag parent() {
        return new Tag(element.getParentElement());
    }

    @Override public int hashCode() {
        return xml().hashCode();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Tag other = (Tag) obj;
        return xml().equals(other.xml());
    }

    public String name() {
        return element.getName();
    }

    public Tag requireName(String expected) {
        bombUnless(name().equals(expected), "expected name: " + expected + ", got: " + name());
        return this;
    }

    @SuppressWarnings("unchecked") public Tag child(String name) {
        try {
            return the(children(name));
        } catch (RuntimeException e) {
            throw bomb("single child not found: " + name + " on \n" + this, e);
        }
    }

    public Tag add(String name, String text) {
        Tag result = add(name);
        result.setText(text);
        return result;
    }

    public void setText(String text) {
        element.setText(text);
    }
    
    public List<Tag> children(String name) { 
        List<Tag> result = empty();
        for(Object e : element.getChildren(name, element.getNamespace()))
            result.add(new Tag((Element) e));
        return result;
    }

    public List<Tag> children() { 
        List<Tag> result = empty();
        for(Object e : element.getChildren()) 
            result.add(new Tag(((Element) e)));
        return result;
    }
    
    public boolean hasChild(String name, String text) {
        return hasChild(name) && child(name).text().equals(text);
    }

    public String text() {
        return element.getText();
    }

    public boolean hasChild(String name) {
        return !children(name).isEmpty();
    }

    public boolean hasChild() {
        return !children().isEmpty();
    }
    
    public void delete() {
        parent().delete(this);
    }

    private void delete(Tag tag) {
        element.removeContent(tag.element);
    }

    public String text(String childName) {
        return child(childName).text();
    }

    public double decimal(String name) {
        return Double.parseDouble(text(name));
    }

    public void requireEmpty() {
        bombIf(hasChild(), "unallowed child found on " + this);
    }

    public void removeChildren() {
        element.removeContent(); 
    }

    public int integer(String string) {
        try {
            return Integer.parseInt(text(string));
        } catch (NumberFormatException e) {
            throw bomb("couldn't parse " + sQuote(string) + " as an int", e);
        }
    }

    public long longg(String string) {
        try {
            return Long.parseLong(text(string));
        } catch (NumberFormatException e) {
            throw bomb("couldn't parse " + sQuote(string) + " as an long", e);
        }
    }

    public Date date(String string) {
        return Dates.date(text(string));
    }



}