using System;

namespace Q.Trading {
    public class AbortBar : Exception {
        public AbortBar() {}
        public AbortBar(string s) : base(s) { }
    }
}