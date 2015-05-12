using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using Gui.Util;
using Microsoft.Windows.Controls;
using Q.Util;

namespace Gui.DataGridTest {
    public class MainWindow : DockingWindow {
        public MainWindow(string[] unused) : base("DataGridTest") {
            var grid = new DataGrid();
            dockManager.Content = grid;
            grid.ItemsSource = new OurCollection();
        }
    }

    public class OurCollection : IList {
        private readonly object theSyncRoot = new object();

        public IEnumerator GetEnumerator() { return new List<MysteryObj>().GetEnumerator(); }
        public int Count { get { return 1000; } }
        public object SyncRoot { get { return theSyncRoot; } }
        public bool IsReadOnly { get { return true; } }
        public bool IsFixedSize { get { return true; } }
        public int IndexOf(object value) { return -1; }
        public object this[int index] {
            get {  LogC.info("get(" + index + ") called."); return new MysteryObj(); }
            set { throw new NotImplementedException(); }
        }
        

        public void CopyTo(Array array, int index) { throw new NotImplementedException(); }
        public bool IsSynchronized { get { throw new NotImplementedException(); } }
        public int Add(object value) { throw new NotImplementedException(); }
        public bool Contains(object value) { throw new NotImplementedException(); }
        public void Clear() { throw new NotImplementedException(); } 
        public void Insert(int index, object value) { throw new NotImplementedException(); }
        public void Remove(object value) { throw new NotImplementedException(); }
        public void RemoveAt(int index) { throw new NotImplementedException(); }
    }

    public class MysteryObj : Objects {
        static readonly Random rng = new Random();
        internal readonly int number;
        internal readonly string text;
        static readonly TypeDescriptionProvider provider = new MysteryTypeDescriptionProvider();

        static MysteryObj() {
            TypeDescriptor.AddProvider(provider, typeof(MysteryObj));
        }

        public MysteryObj() {
            LogC.info("constructing mystery obj");
            lock(rng) number = rng.Next(1000);
            lock(rng) text = "this is a string " + rng.Next(1000);
            doNothing(number);
            doNothing(text);
            
        }

        public static ICustomTypeDescriptor typeDescriptor() {
            return new MysteryTypeDescriptor(TypeDescriptor.GetProvider(typeof(MysteryObj)).GetTypeDescriptor(typeof(MysteryObj)));
        }
    }

    public class MysteryTypeDescriptor : CustomTypeDescriptor {
        static readonly PropertyDescriptorCollection properties = new PropertyDescriptorCollection (new PropertyDescriptor[]{
            new MysteryPropertyDescriptor("Number"),
            new MysteryPropertyDescriptor("Text")
        });

        public MysteryTypeDescriptor(ICustomTypeDescriptor parent) : base(parent) {
            Bomb.ifNull(parent, () => "parent can not be null");
        }
        
        public override PropertyDescriptorCollection GetProperties() { return properties; }
        public override PropertyDescriptorCollection GetProperties(Attribute[] attributes) { return properties; }
    }

    internal class MysteryPropertyDescriptor : PropertyDescriptor {
        readonly string property;

        public MysteryPropertyDescriptor(string property) : base(property, new Attribute[]{}) {
            this.property = property;
        }

        public override bool CanResetValue(object component) { return false; }
        public override object GetValue(object component) {
            var obj = component as MysteryObj;
            if(obj == null) throw Bomb.toss("MysteryPropertyDescriptor only supports MysteryObj, not " + component.GetType().FullName);
            switch (property) {
                case "Number": return obj.number;
                case "Text": return obj.text;
            }
            throw Bomb.toss("unknown property " + property);
        }
        public override void ResetValue(object component) { throw new NotImplementedException(); }
        public override void SetValue(object component, object value) { throw new NotImplementedException(); }
        public override bool ShouldSerializeValue(object component) { return true; }
        public override Type ComponentType { get { return typeof(MysteryObj); } }
        public override bool IsReadOnly { get { return true; } }
        public override Type PropertyType { 
            get {              
                switch (property) {
                    case "Number": return typeof(int);
                    case "Text": return typeof(string);
                }
                throw Bomb.toss("unknown property " + property); 
            }
        }
    }

    internal class MysteryTypeDescriptionProvider : TypeDescriptionProvider {
        public override ICustomTypeDescriptor GetTypeDescriptor(Type objectType, object instance) {
            var obj = instance as MysteryObj;
            if(obj == null) throw Bomb.toss("MysteryTypeDescriptionProvider only supports MysteryObj, not " + instance.GetType().FullName);
            return MysteryObj.typeDescriptor();
        }
    }
}


