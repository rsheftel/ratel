using System.Collections.Generic;
using System.Text;

namespace Amazon.SQS {
    public class XMLHelper {
        public static string EscapeXML(IEnumerable<char> str) {
            var sb = new StringBuilder();
            foreach (var c in str) {
                switch (c) {
                    case '&': sb.Append("&amp;"); break;
                    case '<': sb.Append("&lt;"); break;
                    case '>': sb.Append("&gt;"); break;
                    case '\'': sb.Append("&#039;"); break;
                    case '"': sb.Append("&quot;"); break;
                    default: sb.Append(c); break;
                }
            }
            return sb.ToString();
        }
    }
}
