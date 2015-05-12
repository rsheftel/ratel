package systemdb.metadata;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import systemdb.data.*;
import util.*;
import file.*;

public class SymbolConfigFile {

    public static class WatchListItem {

        private final Tag tag;

        public WatchListItem(Tag item) {
            this.tag = item.requireName("WatchListItem");
        }
        
        Folder folder() {
            requireFolder(true);
            return new Folder(tag.child("Folder"));
        }
        
        Folder folder(String name) { 
            return folder().childFolder(name);
        }

        public boolean hasFolder(String name) {
            return folder().hasChildFolder(name);
        }
        
        private void requireFolder(boolean isFolder) {
            bombUnless(isFolder() == isFolder, "is wrong object type, expected " + (isFolder ? "folder" : "symbol") + " have " + tag.child("IsFolder").text());
        }

        private boolean isFolder() {
            return tag.hasChild("IsFolder", "true");
        }

        SymbolTag symbol() {
            requireFolder(false);
            return new SymbolTag(tag.child("Symbol"));
        }

        public void write(QFile file) {
            file.overwrite(tag.longXml());
        }

        public void deleteFolder(String name) {
            folder(name).delete();
        }

    }

    static class SymbolTag {

        private Tag tag;

        public SymbolTag(Tag child) {
            this.tag = child.requireName("Symbol");
        }

        public SymbolTag(Symbol symbol) {
            tag = new Tag("Symbol");
            tag.add("CurrencyType", "USD");
            tag.add("StrikePrice", "0");
            tag.add("ExpirationDate", "0001-01-01T00:00:00");
            tag.add("ContractType", "NoContract");
            tag.add("Name", symbol.name());
            tag.add("Exchange");
            Tag info = tag.add("SymbolInformation");
            info.add("Margin", "0");
            info.add("TickSize", "0");
            String size = String.valueOf(symbol.bigPointValue());
            info.add("ContractSize", size);
            info.add("DecimalPlaces", "6");
            tag.add("AssetClass", "Stock");
        }

        public void addTo(Tag parent) {
            parent.add(tag);
            parent.add("IsFolder", "false");
        }

        public Symbol symbol() {
            String symbolName = tag.child("Name").text();
            Tag infoSize = tag.child("SymbolInformation").child("ContractSize");
            double contractSize = Double.parseDouble(infoSize.text());
            return new Symbol(symbolName, contractSize);
        }
        
    }
    
    private WatchListItem root;
    private QFile file;

    public static class Folder {

        private Tag tag;

        public Folder(Tag child) {
            tag = child.requireName("Folder");
        }

        public void delete() {
            tag.parent().delete();
        }

        public Folder childFolder(String name) {
            Folder child = childFolderMaybe(name);
            bombNull(child, "child folder " + name + " not found");
            return child;
        }

        private Folder childFolderMaybe(String name) {
            for (WatchListItem item : contents())
                if(item.folder().name().equals(name))
                    return item.folder();
            return null;
        }

        public boolean hasChildFolder(String name) {
            return childFolderMaybe(name) != null;
        }

        private List<WatchListItem> contents() {
            List<WatchListItem> result = empty();
            for (Tag child : tag.child("Contents").children("WatchListItem"))
                result.add(new WatchListItem(child));
            return result ;
        }

        public String name() {
            return tag.child("FolderName").text();
        }


        public SymbolTag add(Symbol symbol) {
            SymbolTag symbolTag = new SymbolTag(symbol);
            symbolTag.addTo(tag.child("Contents").add("WatchListItem"));
            return symbolTag;
        }
        
        public Folder addFolder(String name) {
            Tag item = tag.child("Contents").add("WatchListItem");
            Tag folder = item.add("Folder");
            folder.add("FolderName", name);
            folder.add("Contents");
            item.add("IsFolder", "true");
            return new Folder(folder);
        }


        public List<Symbol> symbols() {
            List<Symbol> result = empty();
            for (WatchListItem item : contents())
                result.add(item.symbol().symbol());
            
            return result ;
        }

    }

    

    public SymbolConfigFile(String filename) {
        file = new QFile(filename);
        root = new WatchListItem(file.xml());
        
    }

    public Folder addFolder(String name) {
        return root.folder().addFolder(name);
    }

    public void save() {
        root.write(file);
    }

    public Folder folder(String name) {
        return root.folder(name);
    }

    public boolean hasFolder(String name) {
        return root.hasFolder(name);
    }

    public void deleteFolder(String name) {
        root.deleteFolder(name);
    }

}
