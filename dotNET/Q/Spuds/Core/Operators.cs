using Q.Util;

namespace Q.Spuds.Core {    
    internal delegate T BivariateOperatorFunction <T> (T a, T b);      

    class BivariateOperator<T> : Spud<T> {
        readonly Spud<T> a;
        readonly Spud<T> b;
        readonly BivariateOperatorFunction<T> op;

        public BivariateOperator(Spud<T> a, Spud<T> b,BivariateOperatorFunction<T> op) : base(a.manager) {
            this.a = dependsOn(a);
            this.b = dependsOn(b);
            this.op = op;
            Bomb.unless(a.manager == b.manager, () => "a and b must have same manager");
        }
        protected override T calculate() {            
            return op(a,b);
        }
    }

    class Plus : BivariateOperator<double> {      
        public Plus(Spud<double> a, Spud<double> b) : base(a,b,(x,y) => x + y) {}        
    }

    class Minus : BivariateOperator<double> {      
        public Minus(Spud<double> a, Spud<double> b) : base(a,b,(x,y) => x - y) {}        
    }

    class Times : BivariateOperator<double> {      
        public Times(Spud<double> a, Spud<double> b) : base(a,b,(x,y) => x * y) {}        
    }

    class Divide : BivariateOperator<double> {
        public Divide(Spud<double> a, Spud<double> b) : base(a,b,(x,y) => y == 0 ? 0 : x / y) {}
    }
    
}